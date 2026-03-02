package DataAccess;

import BusinessLogic.TasksManagement;
import DataModel.Employee;

import java.io.*;
import java.util.List;

public class Serializare {
    private static final String FILENAME = "data.ser"; // numele fisierului pentru serializare

    // metoda pentru salvarea datelor
    public static void saveData(TasksManagement tasksManagement, List<Employee> employees) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILENAME))) {
            oos.writeObject(tasksManagement);
            oos.writeObject(employees);
            System.out.println("datele au fost salvate in fisierul " + FILENAME);
        } catch (IOException e) {
            System.err.println("eroare la salvarea datelor: " + e.getMessage());
        }
    }

    // metoda pentru incarcarea datelor
    public static void loadData(TasksManagement tasksManagement, List<Employee> employees) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILENAME))) {
            TasksManagement loadedTasksManagement = (TasksManagement) ois.readObject();
            List<Employee> loadedEmployees = (List<Employee>) ois.readObject();

            // actualizam referintele in aplicatie
            tasksManagement.getMap().clear();
            tasksManagement.getMap().putAll(loadedTasksManagement.getMap());

            employees.clear();
            employees.addAll(loadedEmployees);

            System.out.println("datele au fost incarcate din fisierul " + FILENAME);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("eroare la incarcarea datelor: " + e.getMessage());
        }
    }

    // adaugam un shutdownhook pentru a salva datele la inchiderea programului
    public static void addShutdownHook(TasksManagement tasksManagement, List<Employee> employees) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveData(tasksManagement, employees);
        }));
    }
}