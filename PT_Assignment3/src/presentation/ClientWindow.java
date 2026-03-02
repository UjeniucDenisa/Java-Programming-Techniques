package presentation;

import dataAccessLayer.ClientDAO;
import model.Client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;


public class ClientWindow extends JFrame {
    private final ClientDAO clientDAO;
    private JTable clientTable;
    private DefaultTableModel tableModel;

    public ClientWindow() {
        this.clientDAO = new ClientDAO();
        initializeUI();
        loadClients();
    }

    private void initializeUI() {
        setTitle("Client Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table setup
        clientTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(clientTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        addButton.addActionListener(this::addClient);
        editButton.addActionListener(this::editClient);
        deleteButton.addActionListener(this::deleteClient);
        refreshButton.addActionListener(e -> loadClients());
    }

    private void loadClients() {
        List<Client> clients = clientDAO.findAll();
        populateTable(clients);
    }

    private void populateTable(List<Client> clients) {
        if (clients.isEmpty()) {
            tableModel = new DefaultTableModel();
            clientTable.setModel(tableModel);
            return;
        }

        // Get field names using reflection
        Field[] fields = Client.class.getDeclaredFields();
        String[] columnNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            columnNames[i] = fields[i].getName();
        }

        // Create table data
        Object[][] data = new Object[clients.size()][fields.length];
        for (int i = 0; i < clients.size(); i++) {
            Client client = clients.get(i);
            for (int j = 0; j < fields.length; j++) {
                try {
                    fields[j].setAccessible(true);
                    data[i][j] = fields[j].get(client);
                } catch (IllegalAccessException e) {
                    data[i][j] = "N/A";
                }
            }
        }

        tableModel = new DefaultTableModel(data, columnNames);
        clientTable.setModel(tableModel);
    }

    private void addClient(ActionEvent e) {
        JPanel panel = new JPanel(new GridLayout(0, 2));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField phoneField = new JTextField();

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Address:"));
        panel.add(addressField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add New Client",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            int newId=clientDAO.getNextId();
            Client newClient = new Client(
                    newId, // ID ul urmator calculat
                    nameField.getText(),
                    emailField.getText(),
                    addressField.getText(),
                    phoneField.getText()
            );

            Client insertedClient = clientDAO.insert(newClient);
            if (insertedClient != null) {
                JOptionPane.showMessageDialog(this, "Client added successfully!");
                loadClients();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add client", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editClient(ActionEvent e) {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a client to edit", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int clientId = (int) tableModel.getValueAt(selectedRow, 0);
        Client client = clientDAO.findById(clientId);

        if (client == null) {
            JOptionPane.showMessageDialog(this, "Client not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2));

        JTextField nameField = new JTextField(client.getName());
        JTextField emailField = new JTextField(client.getEmail());
        JTextField addressField = new JTextField(client.getAddress());
        JTextField phoneField = new JTextField(client.getPhone());

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Address:"));
        panel.add(addressField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Client",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            client.setName(nameField.getText());
            client.setEmail(emailField.getText());
            client.setAddress(addressField.getText());
            client.setPhone(phoneField.getText());

            Client updatedClient = clientDAO.update(client);
            if (updatedClient != null) {
                JOptionPane.showMessageDialog(this, "Client updated successfully!");
                loadClients();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update client", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteClient(ActionEvent e) {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a client to delete", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int clientId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this client?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = clientDAO.delete(clientId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Client deleted successfully!");
                loadClients();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete client", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientWindow window = new ClientWindow();
            window.setVisible(true);
        });
    }
}