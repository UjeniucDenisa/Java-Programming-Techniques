package presentation;

import model.Bill;
import dataAccessLayer.BillDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LogWindow extends JFrame {
    public LogWindow() {
        setTitle("Order Logs");
        setSize(800, 600);
        setLocationRelativeTo(null);

        BillDAO billDAO = new BillDAO();
        List<Bill> bills = billDAO.findAll();

        String[] columnNames = {"Log ID", "Order ID", "Client Name", "Client Email", "Order Date", "Total Amount"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (Bill bill : bills) {
            Object[] row = {
                    bill.logId(),
                    bill.orderId(),
                    bill.clientName(),
                    bill.clientEmail(),
                    bill.orderDate(),
                    bill.totalAmount()
            };
            model.addRow(row);
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }
}