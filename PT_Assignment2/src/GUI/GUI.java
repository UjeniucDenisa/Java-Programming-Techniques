package GUI;

import BusinessLogic.SimulationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GUI {
    private JFrame frame;
    private JTextField clientsField;
    private JTextField queuesField;
    private JTextField simTimeField;
    private JTextField minArrivalField;
    private JTextField maxArrivalField;
    private JTextField minServiceField;
    private JTextField maxServiceField;
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private SimulationManager simulationManager;
    private Thread simulationThread;

    public GUI() {
        createUI();
    }

    private void createUI() {
        frame = new JFrame("Queue Management Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        //panel pt input
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Simulation Parameters"));

        inputPanel.add(new JLabel("Number of clients (N):"));
        clientsField = new JTextField();
        inputPanel.add(clientsField);

        inputPanel.add(new JLabel("Number of queues (Q):"));
        queuesField = new JTextField();
        inputPanel.add(queuesField);

        inputPanel.add(new JLabel("Simulation time (seconds):"));
        simTimeField = new JTextField();
        inputPanel.add(simTimeField);

        inputPanel.add(new JLabel("Min arrival time:"));
        minArrivalField = new JTextField();
        inputPanel.add(minArrivalField);

        inputPanel.add(new JLabel("Max arrival time:"));
        maxArrivalField = new JTextField();
        inputPanel.add(maxArrivalField);

        inputPanel.add(new JLabel("Min service time:"));
        minServiceField = new JTextField();
        inputPanel.add(minServiceField);

        inputPanel.add(new JLabel("Max service time:"));
        maxServiceField = new JTextField();
        inputPanel.add(maxServiceField);

        //panel pt butoane
        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start Simulation");
        stopButton = new JButton("Stop Simulation");
        stopButton.setEnabled(false);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);


        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Simulation Log"));
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        logPanel.add(scrollPane, BorderLayout.CENTER);


        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.add(logPanel, BorderLayout.SOUTH);


        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSimulation();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopSimulation();
            }
        });

        frame.setVisible(true);
    }

    private void startSimulation() {
        try {
            int N = Integer.parseInt(clientsField.getText());
            int Q = Integer.parseInt(queuesField.getText());
            int simulationTime = Integer.parseInt(simTimeField.getText());
            int minArrival = Integer.parseInt(minArrivalField.getText());
            int maxArrival = Integer.parseInt(maxArrivalField.getText());
            int minService = Integer.parseInt(minServiceField.getText());
            int maxService = Integer.parseInt(maxServiceField.getText());

            // verifica sa fie datele introd corecte
            if (minArrival > maxArrival || minService > maxService || N <= 0 || Q <= 0) {
                JOptionPane.showMessageDialog(frame, "Invalid input parameters!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            logArea.setText("");
            simulationManager = new SimulationManager(N, Q, simulationTime,
                    minArrival, maxArrival, minService, maxService);

            // Creaza un thread pt update pt GUI cu simulation logs
            simulationThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    simulationManager.run();
                    List<String> logs = simulationManager.get_log_events();
                    for (String log : logs) {
                        SwingUtilities.invokeLater(() -> {
                            logArea.append(log + "\n");
                        });
                    }
                    SwingUtilities.invokeLater(() -> {
                        logArea.append("\nSimulation completed!\n");
                        logArea.append("Average waiting time: " +
                                String.format("%.2f", simulationManager.get_average_waiting_time()) + "\n");
                        startButton.setEnabled(true);
                        stopButton.setEnabled(false);
                    });
                }
            });

            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            simulationThread.start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopSimulation() {
        if (simulationManager != null) {
            simulationManager.stop_simulation();
        }
        if (simulationThread != null && simulationThread.isAlive()) {
            simulationThread.interrupt();
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI();
            }
        });
    }
}