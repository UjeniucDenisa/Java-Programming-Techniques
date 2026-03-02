package presentation;

import connection.ConnectionFactory;
import dataAccessLayer.BillDAO;
import dataAccessLayer.ClientDAO;
import dataAccessLayer.OrderDAO;
import dataAccessLayer.ProductDAO;
import model.Bill;
import model.Client;
import model.Orderr;
import model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class OrderWindow extends JFrame {
    private final OrderDAO orderDAO;
    private JTable orderTable;
    private DefaultTableModel tableModel;

    private static final Color PINK_BG = new Color(0xFFC0CB);
    public OrderWindow() {
        this.orderDAO = new OrderDAO();
        initializeUI();
        loadOrders();
    }

    private void initializeUI() {
        setTitle("Order Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(PINK_BG);

        // Table setup
        orderTable = new JTable();
        orderTable.setBackground(PINK_BG);                  // fundal celule
        orderTable.setGridColor(Color.WHITE);               // culoarea grilei
        orderTable.setSelectionBackground(Color.PINK.darker());  // selectare roz
        orderTable.setSelectionForeground(Color.WHITE);     // text selectat alb

      // Fundal antet tabel
        orderTable.getTableHeader().setBackground(Color.PINK.darker());
        orderTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.getViewport().setBackground(PINK_BG);  // fundal roz scroll
        scrollPane.setBackground(PINK_BG);                // fundal roz pentru bara

        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.setBackground(PINK_BG);

        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");
        JButton viewLogButton = new JButton("View Bill");

        // schimbam culoarea butoanelor si textului
        JButton[] buttons = {addButton, editButton, deleteButton, refreshButton, viewLogButton};
        for (JButton btn : buttons) {
            btn.setBackground(Color.PINK.darker());  // roz mai inchis
            btn.setForeground(Color.WHITE);          // text alb pentru contrast
            btn.setFocusPainted(false);               // eliminam conturul de selectie

        }

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewLogButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        addButton.addActionListener(this::addOrder);
        editButton.addActionListener(this::editOrder);
        deleteButton.addActionListener(this::deleteOrder);
        refreshButton.addActionListener(e -> loadOrders());
        viewLogButton.addActionListener(e -> {LogWindow logWindow = new LogWindow();logWindow.setVisible(true);});



    }

    private void loadOrders() {
        List<Orderr> orders = orderDAO.findAll();
        populateTable(orders);
    }

    private void populateTable(List<Orderr> orders) {
        if (orders.isEmpty()) {
            tableModel = new DefaultTableModel();
            orderTable.setModel(tableModel);
            return;
        }

        Field[] fields = Orderr.class.getDeclaredFields();
        String[] columnNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            columnNames[i] = fields[i].getName();
        }

        Object[][] data = new Object[orders.size()][fields.length];
        for (int i = 0; i < orders.size(); i++) {
            Orderr order = orders.get(i);
            for (int j = 0; j < fields.length; j++) {
                try {
                    fields[j].setAccessible(true);
                    data[i][j] = fields[j].get(order);
                } catch (IllegalAccessException e) {
                    data[i][j] = "N/A";
                }
            }
        }

        tableModel = new DefaultTableModel(data, columnNames);
        orderTable.setModel(tableModel);
    }

    private void addOrder(ActionEvent e) {
        // initializare dao-uri pentru acces la baza de date
        ClientDAO clientDAO = new ClientDAO();
        ProductDAO productDAO = new ProductDAO();
        BillDAO billDAO = new BillDAO();

        // obtine lista de clienti si produse din baza de date
        List<Client> clients = clientDAO.findAll();
        List<Product> products = productDAO.findAll();

        // creeaza componente pentru interfata grafica
        JComboBox<Client> clientComboBox = new JComboBox<>(clients.toArray(new Client[0]));
        JComboBox<Product> productComboBox = new JComboBox<>(products.toArray(new Product[0]));
        JTextField quantityField = new JTextField();

        // configureaza panoul pentru dialog
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.setBackground(PINK_BG);
        panel.add(new JLabel("Client:"));
        panel.add(clientComboBox);
        panel.add(new JLabel("Product:"));
        panel.add(productComboBox);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);

        // afiseaza dialogul si asteapta confirmarea utilizatorului
        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add New Order",
                JOptionPane.OK_CANCEL_OPTION
        );

        // daca utilizatorul apasa butonul OK
        if (result == JOptionPane.OK_OPTION) {
            try {
                // preia datele introduse de utilizator
                Client selectedClient = (Client) clientComboBox.getSelectedItem();
                Product selectedProduct = (Product) productComboBox.getSelectedItem();
                int quantity = Integer.parseInt(quantityField.getText());

                // valideaza selectia clientului si produsului
                if (selectedProduct == null || selectedClient == null) {
                    JOptionPane.showMessageDialog(this,
                            "Selectati un client si un produs.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // valideaza cantitatea introdusa
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Cantitatea trebuie sa fie pozitiva.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // verifica daca exista suficient stoc
                if (selectedProduct.getStock() < quantity) {
                    JOptionPane.showMessageDialog(this,
                            "Stoc insuficient! Disponibil: " + selectedProduct.getStock(),
                            "Stock Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // creeaza o noua comanda
                int newId = orderDAO.getNextId();
                Orderr newOrder = new Orderr(newId, selectedClient.getId(), selectedProduct.getId(), quantity);

                // incepe o tranzactie pentru operatii multiple
                try (Connection connection = ConnectionFactory.getConnection()) {
                    connection.setAutoCommit(false);

                    try {
                        // insereaza comanda in baza de date
                        Orderr insertedOrder = orderDAO.insert(newOrder);
                        if (insertedOrder == null) {
                            throw new SQLException("Failed to insert order");
                        }

                        // actualizeaza stocul produsului
                        selectedProduct.setStock(selectedProduct.getStock() - quantity);
                        if (productDAO.update(selectedProduct) == null) {
                            throw new SQLException("Failed to update product stock");
                        }

                        // creeaza si insereaza factura
                        int nextLogId = billDAO.getNextId();
                        int totalAmount = quantity * selectedProduct.getPrice();

                        Bill newBill = new Bill(
                                nextLogId,
                                insertedOrder.getOrderId(),
                                selectedClient.getName(),
                                selectedClient.getEmail(),
                                LocalDateTime.now(),
                                totalAmount
                        );

                        if (billDAO.insert(newBill) == null) {
                            throw new SQLException("Failed to insert bill");
                        }

                        // confirma tranzactia daca totul este ok
                        connection.commit();
                        JOptionPane.showMessageDialog(this,
                                "Comanda si factura create cu succes!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadOrders();

                    } catch (SQLException ex) {
                        // anuleaza tranzactia in caz de eroare
                        connection.rollback();
                        JOptionPane.showMessageDialog(this,
                                "Tranzactie esuata: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        // reseteaza auto-commit la valoarea initiala
                        connection.setAutoCommit(true);
                    }
                }
            } catch (NumberFormatException ex) {
                // gestioneaza erori de format numeric
                JOptionPane.showMessageDialog(this,
                        "Format numeric invalid!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                // gestioneaza erori de baza de date
                JOptionPane.showMessageDialog(this,
                        "Eroare baza de date: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void editOrder(ActionEvent e) {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to edit", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);
        Orderr order = orderDAO.findById(orderId);

        if (order == null) {
            JOptionPane.showMessageDialog(this, "Order not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.setBackground(PINK_BG);

        JTextField clientIdField = new JTextField(String.valueOf(order.getClientId()));
        JTextField productIdField = new JTextField(String.valueOf(order.getProductId()));
        JTextField quantityField = new JTextField(String.valueOf(order.getQuantity()));

        panel.add(new JLabel("Client ID:"));
        panel.add(clientIdField);
        panel.add(new JLabel("Product ID:"));
        panel.add(productIdField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Order",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                order.setClientId(Integer.parseInt(clientIdField.getText()));
                order.setProductId(Integer.parseInt(productIdField.getText()));
                order.setQuantity(Integer.parseInt(quantityField.getText()));

                Orderr updatedOrder = orderDAO.update(order);

                if (updatedOrder != null) {
                    JOptionPane.showMessageDialog(this, "Order updated successfully!");
                    loadOrders();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update order", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteOrder(ActionEvent e) {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to delete", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this order?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = orderDAO.delete(orderId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Order deleted successfully!");
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete order", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        UIManager.put("Panel.background", PINK_BG);
        UIManager.put("OptionPane.background", PINK_BG);
        UIManager.put("OptionPane.messageForeground", Color.DARK_GRAY);
        UIManager.put("Button.background", Color.PINK.darker());
        UIManager.put("Button.foreground", Color.WHITE);

        SwingUtilities.invokeLater(() -> {
            OrderWindow window = new OrderWindow();
            window.setVisible(true);
        });
    }
}
