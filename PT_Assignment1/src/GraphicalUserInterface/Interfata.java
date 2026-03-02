package GraphicalUserInterface;

import BusinessLogic.TasksManagement;
import BusinessLogic.Utility;
import DataAccess.Serializare;
import DataModel.ComplexTask;
import DataModel.Employee;
import DataModel.SimpleTask;
import DataModel.Task;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Interfata {
    private static TasksManagement tasksManagement = new TasksManagement();
    private static List<Employee> employees = new ArrayList<>();
    private static DefaultComboBoxModel<Employee> employeeComboBoxModel = new DefaultComboBoxModel<>();
    private static DefaultComboBoxModel<Task> taskComboBoxModel = new DefaultComboBoxModel<>();
    private static DefaultComboBoxModel<ComplexTask> complexTaskComboBoxModel = new DefaultComboBoxModel<>();
    private static DefaultComboBoxModel<SimpleTask> simpleTaskComboBoxModel = new DefaultComboBoxModel<>();

    public static void main(String[] args) {
        // incarcam datele la pornirea aplicatiei
        Serializare.loadData(tasksManagement, employees);

        // shutdownhook pentru a salva datele la inchidere
        Serializare.addShutdownHook(tasksManagement, employees);


        JFrame frame = new JFrame("Task Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel addEmployeePanel = createAddEmployeePanel();
        mainPanel.add(addEmployeePanel);
        JPanel addTaskPanel = createAddTaskPanel();
        mainPanel.add(addTaskPanel);
        JPanel addSubTaskPanel = createAddSubTaskPanel();
        mainPanel.add(addSubTaskPanel);
        JPanel assignTaskPanel = createAssignTaskPanel();
        mainPanel.add(assignTaskPanel);
        JPanel modifyStatusPanel = createModifyStatusPanel();
        mainPanel.add(modifyStatusPanel);
        JPanel statisticsPanel = createStatisticsPanel();
        mainPanel.add(statisticsPanel);
        JPanel viewDataPanel = createViewDataPanel();
        mainPanel.add(viewDataPanel);
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        frame.add(mainPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static JPanel createAddEmployeePanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Add Employee"));
        JTextField employeeNameField = new JTextField();
        JButton addEmployeeButton = new JButton("Add Employee");
        panel.add(new JLabel("Employee Name:"));
        panel.add(employeeNameField);
        panel.add(new JLabel());
        panel.add(addEmployeeButton);

        addEmployeeButton.addActionListener(e -> {
            String name = employeeNameField.getText();
            if (!name.isEmpty()) {
                Employee employee = new Employee(employees.size() + 1, name);
                employees.add(employee);
                employeeComboBoxModel.addElement(employee);
                employeeNameField.setText("");
                JOptionPane.showMessageDialog(null, "Employee added: " + name);
            }
        });

        return panel;
    }

    private static JPanel createAddTaskPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Add Task"));
        JComboBox<String> taskTypeComboBox = new JComboBox<>(new String[]{"Simple Task", "Complex Task"});
        JTextField taskIdField = new JTextField(), taskStartHourField = new JTextField(), taskEndHourField = new JTextField();
        JButton addTaskButton = new JButton("Add Task");
        String[] labels = {"Task Type:", "Task ID:", "Start Hour (for Simple Task):", "End Hour (for Simple Task):"};
        Component[] components = {taskTypeComboBox, taskIdField, taskStartHourField, taskEndHourField};
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i]));
            panel.add(components[i]);}
        panel.add(new JLabel());
        panel.add(addTaskButton);
        addTaskButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(taskIdField.getText());
                if (tasksManagement.hasTaskWithId(id)) throw new IllegalArgumentException("A task with ID " + id + " already exists.");
                String taskType = (String) taskTypeComboBox.getSelectedItem();
                Task task = taskType.equals("Simple Task")
                        ? new SimpleTask("Uncompleted", id, Integer.parseInt(taskStartHourField.getText()), Integer.parseInt(taskEndHourField.getText()))
                        : new ComplexTask("Uncompleted", id);
                taskComboBoxModel.addElement(task);
                if (task instanceof SimpleTask) simpleTaskComboBoxModel.addElement((SimpleTask) task);
                if (task instanceof ComplexTask) complexTaskComboBoxModel.addElement((ComplexTask) task);
                tasksManagement.getMap().put(new Employee(0, "Temporary"), List.of(task));
                JOptionPane.showMessageDialog(null, taskType + " added: ID " + id);
                taskIdField.setText("");
                taskStartHourField.setText("");
                taskEndHourField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    private static JPanel createAddSubTaskPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Add SubTask to Complex Task"));
        JComboBox<ComplexTask> complexTaskComboBox = new JComboBox<>(complexTaskComboBoxModel);
        JComboBox<SimpleTask> simpleTaskComboBox = new JComboBox<>(simpleTaskComboBoxModel);
        JButton addSubTaskButton = new JButton("Add Existing Simple Task as SubTask");
        panel.add(new JLabel("Complex Task:"));
        panel.add(complexTaskComboBox);
        panel.add(new JLabel("Simple Task:"));
        panel.add(simpleTaskComboBox);
        panel.add(new JLabel());
        panel.add(addSubTaskButton);
        addSubTaskButton.addActionListener(e -> {
            try {
                ComplexTask complexTask = (ComplexTask) complexTaskComboBox.getSelectedItem();
                SimpleTask simpleTask = (SimpleTask) simpleTaskComboBox.getSelectedItem();
                if (complexTask == null || simpleTask == null) {
                    throw new IllegalArgumentException("Please select both a Complex Task and a Simple Task.");
                }
                if (complexTask.hasSubTaskWithId(simpleTask.getIdTask())) {
                    throw new IllegalArgumentException("The selected Simple Task is already a subtask of this Complex Task.");
                }
                complexTask.addSubTask(simpleTask);
                JOptionPane.showMessageDialog(null, "Simple Task (ID: " + simpleTask.getIdTask() + ") added as subtask to Complex Task (ID: " + complexTask.getIdTask() + ")");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    private static JPanel createAssignTaskPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Assign Task to Employee"));
        JComboBox<Employee> employeeComboBox = new JComboBox<>(employeeComboBoxModel);
        JComboBox<Task> taskComboBox = new JComboBox<>(taskComboBoxModel);
        JButton assignTaskButton = new JButton("Assign Task");
        panel.add(new JLabel("Employee:"));
        panel.add(employeeComboBox);
        panel.add(new JLabel("Task:"));
        panel.add(taskComboBox);
        panel.add(new JLabel());
        panel.add(assignTaskButton);

        assignTaskButton.addActionListener(e -> {
            Employee employee = (Employee) employeeComboBox.getSelectedItem();
            Task task = (Task) taskComboBox.getSelectedItem();
            if (employee != null && task != null) {
                tasksManagement.assignTaskToEmployee(employee, task);
                JOptionPane.showMessageDialog(null, "Task " + task.getIdTask() + " assigned to " + employee.getName());
            }
        });

        return panel;
    }

    private static JPanel createModifyStatusPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Modify Task Status"));
        JComboBox<Task> taskStatusComboBox = new JComboBox<>(taskComboBoxModel);
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Completed", "Uncompleted"});
        JButton modifyStatusButton = new JButton("Modify Status");
        panel.add(new JLabel("Task:"));
        panel.add(taskStatusComboBox);
        panel.add(new JLabel("New Status:"));
        panel.add(statusComboBox);
        panel.add(new JLabel());
        panel.add(modifyStatusButton);

        modifyStatusButton.addActionListener(e -> {
            Task task = (Task) taskStatusComboBox.getSelectedItem();
            String status = (String) statusComboBox.getSelectedItem();
            if (task != null) {
                task.setStatusTask(status);
                JOptionPane.showMessageDialog(null, "Task " + task.getIdTask() + " status updated to " + status);
            }
        });

        return panel;
    }

    private static JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        JButton viewStatisticsButton = new JButton("View Statistics");
        panel.add(viewStatisticsButton);

        viewStatisticsButton.addActionListener(e -> {
            Map<String, Map<String, Integer>> stats = Utility.countCompletedAndUncompletedTasks(employees);
            StringBuilder result = new StringBuilder("--- Statistics ---\n");
            for (Map.Entry<String, Map<String, Integer>> entry : stats.entrySet()) {
                result.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            result.append("\nEmployees with work duration > 40 hours:\n");
            Utility.filterAndSortEmployeesByWorkDuration(employees, tasksManagement);
            JOptionPane.showMessageDialog(null, result.toString());
        });

        return panel;
    }

    private static JPanel createViewDataPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("View Data"));
        JButton viewDataButton = new JButton("View Data");
        panel.add(viewDataButton);

        viewDataButton.addActionListener(e -> {
            StringBuilder result = new StringBuilder("--- Assigned Tasks ---\n");
            for (Employee employee : employees) {
                result.append("Employee: ").append(employee.getName()).append("\n");
                List<Task> tasks = tasksManagement.getMap().get(employee);
                if (tasks != null) {
                    for (Task task : tasks) {
                        result.append("  Task ID: ").append(task.getIdTask()).append(", Status: ").append(task.getStatusTask()).append("\n");
                    }
                }
                result.append("\n");
            }
            JOptionPane.showMessageDialog(null, result.toString());
        });

        return panel;
    }
}