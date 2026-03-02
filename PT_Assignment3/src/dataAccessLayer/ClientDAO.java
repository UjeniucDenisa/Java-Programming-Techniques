package dataAccessLayer;

import connection.ConnectionFactory;
import dataAccessLayer.AbstractDAO;
import model.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientDAO extends AbstractDAO<Client> {
    public ClientDAO() {
        super();
    }

    // metoda care returneaza urmatorul id liber in tabela client
    public int getNextId() {
        int nextId = 1; // daca tabela e goala, pornim de la 1

        // folosim try-with-resources pentru a inchide automat resursele
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT MAX(id) FROM client");
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                // luam valoarea maxima existenta in coloana id
                int maxId = resultSet.getInt(1);
                // calculam urmatorul id disponibil
                nextId = maxId + 1;
            }

        } catch (SQLException e) {
            // afisam eroarea in caz ca ceva nu merge bine
            e.printStackTrace();
        }

        return nextId;
    }
}
