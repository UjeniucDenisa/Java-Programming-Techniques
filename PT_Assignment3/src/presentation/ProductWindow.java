package presentation;

import dataAccessLayer.ProductDAO;
import model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;

public class ProductWindow extends JFrame {
    private final ProductDAO productDAO;
    private JTable productTable;
    private DefaultTableModel tableModel;

    public ProductWindow() {
        this.productDAO = new ProductDAO();
        initializeUI();
        loadProducts();
    }

    private void initializeUI() {
        setTitle("Product Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Tabel produse
        productTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel butoane jos
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

        // Actiuni butoane
        addButton.addActionListener(this::addProduct);
        editButton.addActionListener(this::editProduct);
        deleteButton.addActionListener(this::deleteProduct);
        refreshButton.addActionListener(e -> loadProducts());
    }

    private void loadProducts() {
        List<Product> products = productDAO.findAll();
        populateTable(products);
    }

    private void populateTable(List<Product> products) {
        if (products.isEmpty()) {
            tableModel = new DefaultTableModel();
            productTable.setModel(tableModel);
            return;
        }

        Field[] fields = Product.class.getDeclaredFields();
        String[] columnNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            columnNames[i] = fields[i].getName();
        }

        Object[][] data = new Object[products.size()][fields.length];
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            for (int j = 0; j < fields.length; j++) {
                try {
                    fields[j].setAccessible(true);
                    data[i][j] = fields[j].get(product);
                } catch (IllegalAccessException e) {
                    data[i][j] = "N/A";
                }
            }
        }

        tableModel = new DefaultTableModel(data, columnNames);
        productTable.setModel(tableModel);
    }

    private void addProduct(ActionEvent e) {
        JPanel panel = new JPanel(new GridLayout(0, 2));

        JTextField nameField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField priceField = new JTextField();

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Stock:"));
        panel.add(stockField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add New Product",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                int stock = Integer.parseInt(stockField.getText());
                int price = Integer.parseInt(priceField.getText());

                // id se auto-incrementeaza in DB, deci setam 0
                Product newProduct = new Product(0, name, stock, price);
                Product insertedProduct = productDAO.insert(newProduct);

                if (insertedProduct != null) {
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                    loadProducts();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add product", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editProduct(ActionEvent e) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        Product product = productDAO.findById(id);

        if (product == null) {
            JOptionPane.showMessageDialog(this, "Product not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2));

        JTextField nameField = new JTextField(product.getName());
        JTextField stockField = new JTextField(String.valueOf(product.getStock()));
        JTextField priceField = new JTextField(String.valueOf(product.getPrice()));

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Stock:"));
        panel.add(stockField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Product",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                product.setName(nameField.getText());
                product.setStock(Integer.parseInt(stockField.getText()));
                product.setPrice(Integer.parseInt(priceField.getText()));

                Product updatedProduct = productDAO.update(product);

                if (updatedProduct != null) {
                    JOptionPane.showMessageDialog(this, "Product updated successfully!");
                    loadProducts();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update product", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteProduct(ActionEvent e) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this product?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = productDAO.delete(id);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProductWindow window = new ProductWindow();
            window.setVisible(true);
        });
    }
}
