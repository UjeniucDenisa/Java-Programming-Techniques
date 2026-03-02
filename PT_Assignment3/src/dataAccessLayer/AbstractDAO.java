package dataAccessLayer;

import connection.ConnectionFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * generic dao care face operatii crud folosind reflection
 * @param <T> tipul entitatii pe care o manipuleaza dao-ul
 */
public abstract class AbstractDAO<T> {
    private static final Logger LOGGER = Logger.getLogger(AbstractDAO.class.getName());
    final Class<T> type;  // clasa entitatii (ex: User.class)

    @SuppressWarnings("unchecked")
    public AbstractDAO() {
        // obtinem clasa parametrizata in runtime (de exemplu User daca DAO<User>)
        this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    // metoda care construieste un query select pe baza unui camp (ex: id)
    protected String createSelectQuery(String field) {
        // construim query de forma: SELECT * FROM NumeTabela WHERE field = ?
        return "SELECT * FROM " + getTableName() + " WHERE " + field + " = ?";
    }

    /**
     * gaseste un obiect dupa id-ul lui in baza de date
     * @param id id-ul obiectului cautat
     * @return obiectul gasit sau null daca nu exista
     */
    public T findById(int id) {
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(createSelectQuery("id"))) {

            // setam parametrul id in query
            statement.setInt(1, id);

            // executam query si preluam rezultatul
            try (ResultSet resultSet = statement.executeQuery()) {
                // transformam rezultatul intr-o lista de obiecte
                List<T> results = createObjects(resultSet);
                if (!results.isEmpty()) {
                    // daca gasim cel putin un rezultat, returnam primul
                    return results.get(0);
                }
            }
        } catch (SQLException e) {
            // logam orice eroare sql
            LOGGER.log(Level.WARNING, type.getName() + "DAO:findById " + e.getMessage());
        }
        // daca nu am gasit sau a fost eroare, returnam null
        return null;
    }

    /**
     * transforma un ResultSet intr-o lista de obiecte de tip T folosind reflection
     * @param resultSet rezultatul interogarii SQL
     * @return lista de obiecte populate cu date din baza
     */
    List<T> createObjects(ResultSet resultSet) {
        List<T> list = new ArrayList<>();

        try {
            if (type.isRecord()) {
                // Pentru record-uri
                Constructor<T> ctor = (Constructor<T>) type.getDeclaredConstructors()[0];
                ctor.setAccessible(true);
                RecordComponent[] components = type.getRecordComponents();

                while (resultSet.next()) {
                    Object[] params = new Object[components.length];
                    for (int i = 0; i < components.length; i++) {
                        String name = components[i].getName();
                        params[i] = resultSet.getObject(name);
                    }
                    T instance = ctor.newInstance(params);
                    list.add(instance);
                }
            } else {
                // Pentru clase normale (cu constructor fara parametri)
                Constructor<?> ctor = Arrays.stream(type.getDeclaredConstructors())
                        .filter(c -> c.getParameterCount() == 0)
                        .findFirst()
                        .orElse(null);

                if (ctor == null) {
                    LOGGER.warning("nu exista constructor fara parametri pentru " + type.getName());
                    return list;
                }

                while (resultSet.next()) {
                    ctor.setAccessible(true);
                    T instance = (T) ctor.newInstance();

                    for (Field field : type.getDeclaredFields()) {
                        String fieldName = field.getName();
                        Object value = resultSet.getObject(fieldName);

                        PropertyDescriptor pd = new PropertyDescriptor(fieldName, type);
                        Method setter = pd.getWriteMethod();

                        if (setter != null) {
                            setter.invoke(instance, value);
                        }
                    }
                    list.add(instance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * gaseste toate obiectele din tabela corespunzatoare clasei T
     * @return lista cu toate obiectele gasite
     */
    public List<T> findAll() {
        // query simplu: SELECT * FROM numeTabela
        String query = "SELECT * FROM " + getTableName();

        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // transformam rezultatul in lista de obiecte
            return createObjects(resultSet);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, type.getName() + "DAO:findAll " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * insereaza un obiect in baza de date si seteaza id-ul generat inapoi in obiect
     * @param t obiectul de inserat
     * @return obiectul inserat cu id-ul setat sau null daca a esuat
     */
    public T insert(T t) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            Field[] fields = type.getDeclaredFields();

            // construim partea de coloane si valorile parametrilor, fara primul camp (id)
            StringBuilder sbCols = new StringBuilder();
            StringBuilder sbVals = new StringBuilder();

            IntStream.range(1, fields.length).forEach(i -> {   // aici folosim lambda pentru fiecare i din stream
                sbCols.append(fields[i].getName());
                sbVals.append("?");
                if (i < fields.length - 1) {
                    sbCols.append(", ");
                    sbVals.append(", ");
                }
            });

            // construim query complet de insert
            String sql = "INSERT INTO " + type.getSimpleName() + " (" + sbCols + ") VALUES (" + sbVals + ")";

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // setam parametrii query-ului cu valorile din obiectul t
                IntStream.range(1, fields.length).forEach(i -> {
                    try {
                        fields[i].setAccessible(true);
                        statement.setObject(i, fields[i].get(t));
                    } catch (IllegalAccessException | SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                // executam insert-ul
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("insert failed, no rows affected");
                }

                // preluam id-ul generat si il setam inapoi in obiect
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Field idField = type.getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(t, generatedKeys.getInt(1));
                    }
                }
                return t;
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, type.getName() + "DAO:insert " + e.getMessage());
            return null;
        }
    }

    /**
     * actualizeaza un obiect in baza de date pe baza id-ului
     * @param t obiectul cu datele actualizate
     * @return obiectul actualizat sau null daca a esuat
     */
    public T update(T t) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            Field[] fields = type.getDeclaredFields();

            // construim query-ul de update SET campuri = ? fara id, apoi WHERE id = ?
            StringBuilder sb = new StringBuilder("UPDATE ");
            sb.append(type.getSimpleName()).append(" SET ");

            IntStream.range(1, fields.length).forEach(i -> {
                sb.append(fields[i].getName()).append("=?");
                if (i < fields.length - 1) {
                    sb.append(", ");
                }
            });
            sb.append(" WHERE id=?");

            try (PreparedStatement statement = connection.prepareStatement(sb.toString())) {
                // setam valorile campurilor in statement
                IntStream.range(1, fields.length).forEach(i -> {
                    try {
                        fields[i].setAccessible(true);
                        statement.setObject(i, fields[i].get(t));
                    } catch (IllegalAccessException | SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                // setam id-ul pentru clauza WHERE
                Field idField = type.getDeclaredField("id");
                idField.setAccessible(true);
                statement.setObject(fields.length, idField.get(t));

                // executam update
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("update failed, no rows affected");
                }
                return t;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, type.getName() + "DAO:update " + e.getMessage());
            return null;
        }
    }

    /**
     * sterge un obiect din baza dupa id
     * @param id id-ul obiectului de sters
     * @return true daca s-a sters, false daca nu sau a fost eroare
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // setam id-ul pentru stergere
            statement.setInt(1, id);

            // executam stergerea si verificam daca a sters ceva
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, type.getName() + "DAO:delete " + e.getMessage());
            return false;
        }
    }
    protected String getTableName() {
        return type.getSimpleName();
    }


}
