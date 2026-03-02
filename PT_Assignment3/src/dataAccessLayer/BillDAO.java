package dataAccessLayer;

import connection.ConnectionFactory;
import model.Bill;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillDAO extends AbstractDAO<Bill> {
    public BillDAO() {
        super();
    }

    public Bill insert(Bill bill) {
        String sql = "INSERT INTO Log (logId, orderId, clientName, clientEmail, orderDate, totalAmount) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bill.logId());
            statement.setInt(2, bill.orderId());
            statement.setString(3, bill.clientName());
            statement.setString(4, bill.clientEmail());
            statement.setTimestamp(5, Timestamp.valueOf(bill.orderDate()));
            statement.setInt(6, bill.totalAmount());

            statement.executeUpdate();
            return bill;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getNextId() {
        String sql = "SELECT MAX(logId) FROM Log";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // daca tabela e goala sau apare o eroare, incepe cu ID 1
        return 1;
    }


    protected String getTableName() {
        return "Log"; // numele real al tabelei din MySQL
    }


}