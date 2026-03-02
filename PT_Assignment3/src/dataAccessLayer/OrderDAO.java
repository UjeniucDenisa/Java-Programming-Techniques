package dataAccessLayer;

import connection.ConnectionFactory;
import model.Orderr;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OrderDAO extends AbstractDAO<Orderr> {
    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());
    private Connection conn;

    public OrderDAO() {
        // obtinem conexiunea la baza de date o singura data cand cream obiectul
        conn = ConnectionFactory.getConnection();
    }

    // numele campului care este cheia primara in tabel
    protected String getIdFieldName() {
        return "orderId";
    }

    // numele tabelului din baza de date
    protected String getTableName() {
        return "Orderr";
    }

    @Override
    public boolean delete(int id) {
        // sterge o comanda dupa id
        String sql = "DELETE FROM Orderr WHERE orderId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // setam parametrul pentru id
            stmt.setInt(1, id);
            // executam comanda de stergere
            int rowsAffected = stmt.executeUpdate();
            // returnam true daca s-a sters cel putin o linie
            return rowsAffected > 0;
        } catch (SQLException e) {
            // logam eroarea in caz ca ceva nu merge bine
            LOGGER.log(Level.WARNING, getClass().getSimpleName() + ":delete " + e.getMessage());
        }
        // daca a aparut o eroare returnam false
        return false;
    }

    public int getNextId() {
        // calculam urmatorul id pentru comanda (max + 1)
        String sql = "SELECT MAX(orderId) FROM Orderr";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                // luam valoarea maxima si adaugam 1
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // daca tabela e goala, incepem de la 1
        return 1;
    }

    public Orderr insert(Orderr order) {
        // inseram o comanda si produsele aferente folosind tranzactie
        String insertOrderSql = "INSERT INTO Orderr (clientId) VALUES (?)";
        String insertOrderItemSql = "INSERT INTO OrderItem (orderId, productId, quantity) VALUES (?, ?, ?)";

        try {
            // dezactivam auto-commit pentru tranzactie
            conn.setAutoCommit(false);

            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                // setam clientId pentru comanda
                orderStmt.setInt(1, order.getClientId());
                orderStmt.executeUpdate();

                // obtinem id-ul generat automat pentru comanda noua
                ResultSet generatedKeys = orderStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    order.setOrderId(orderId);

                    try (PreparedStatement itemStmt = conn.prepareStatement(insertOrderItemSql)) {
                        // inseram produsele pentru comanda
                        itemStmt.setInt(1, orderId);
                        itemStmt.setInt(2, order.getProductId());
                        itemStmt.setInt(3, order.getQuantity());
                        itemStmt.executeUpdate();
                    }

                    // comitem tranzactia daca totul a mers bine
                    conn.commit();
                    return order;
                } else {
                    // daca nu s-a generat id pentru comanda, anulam tranzactia
                    conn.rollback();
                    System.out.println("nu s-a generat id pentru comanda");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                // la eroare, anulam tranzactia
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                // reactiva auto-commit
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // daca inserarea a esuat, returnam null
        return null;
    }

    @Override
    public List<Orderr> findAll() {
        // luam toate comenzile impreuna cu produsele aferente
        String sql = "SELECT o.orderId, o.clientId, oi.productId, oi.quantity " +
                "FROM Orderr o " +
                "JOIN OrderItem oi ON o.orderId = oi.orderId";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // convertim rezultatul in lista de obiecte Orderr
            return resultSetToList(rs);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // daca a fost o eroare, returnam lista goala
        return List.of();
    }

    // metoda care transforma un ResultSet in List<Orderr> folosind stream si lambda
    private List<Orderr> resultSetToList(ResultSet rs) throws SQLException {
        List<Orderr> orders = new java.util.ArrayList<>();

        // parcurgem fiecare rand din ResultSet
        while (rs.next()) {
            // construim obiect Orderr din coloanele returnate
            Orderr order = new Orderr(
                    rs.getInt("orderId"),
                    rs.getInt("clientId"),
                    rs.getInt("productId"),
                    rs.getInt("quantity")
            );
            // adaugam in lista
            orders.add(order);
        }

        // folosim stream
        return orders.stream()
                .collect(Collectors.toList());
    }

    @Override
    public Orderr findById(int id) {
        // cautam o comanda dupa id
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String query = createSelectQuery(getIdFieldName());

        try {
            connection = ConnectionFactory.getConnection();
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            resultSet = statement.executeQuery();

            // convertim rezultatul in lista de comenzi
            List<Orderr> results = createObjects(resultSet);
            if (!results.isEmpty()) {
                // returnam prima comanda gasita
                return results.get(0);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, getClass().getSimpleName() + ":findById " + e.getMessage());
        } finally {
            // inchidem toate resursele dupa folosire
            ConnectionFactory.close(resultSet);
            ConnectionFactory.close(statement);
            ConnectionFactory.close(connection);
        }
        // daca nu gasim comanda, returnam null
        return null;
    }
}
