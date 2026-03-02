package BusinessLogic;

import DataModel.Employee;
import DataModel.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utility {
    public static void filterAndSortEmployeesByWorkDuration(List<Employee> employees, TasksManagement tasksManagement) {
        List<Employee> filteredEmployees = new ArrayList<>();

        for (Employee employee : employees) {
            int workDuration = tasksManagement.calculateEmployeeWorkDuration(employee);
            if (workDuration > 40) {
                filteredEmployees.add(employee);
            }
        }
        for (int i = 0; i < filteredEmployees.size() - 1; i++) {
            for (int j = i + 1; j < filteredEmployees.size(); j++) {
                if (tasksManagement.calculateEmployeeWorkDuration(filteredEmployees.get(i)) >
                        tasksManagement.calculateEmployeeWorkDuration(filteredEmployees.get(j))) {
                    Employee temp = filteredEmployees.get(i);
                    filteredEmployees.set(i, filteredEmployees.get(j));
                    filteredEmployees.set(j, temp);
                }
            }
        }
        for (Employee employee : filteredEmployees) {
            System.out.println(employee.getName());
        }
    }

    public static Map<String, Map<String, Integer>> countCompletedAndUncompletedTasks(List<Employee> employees) {
        Map<String, Map<String, Integer>> taskCounts = new HashMap<>();

        for (Employee employee : employees) {
            int completed = 0;
            int uncompleted = 0;

            for (Task task : employee.getTasks()) {
                if (task.getStatusTask().equals("Completed")) {
                    completed++;
                } else {
                    uncompleted++;
                }
            }

            Map<String, Integer> statusCount = new HashMap<>();
            statusCount.put("Completed", completed);
            statusCount.put("Uncompleted", uncompleted);

            taskCounts.put(employee.getName(), statusCount);
        }

        return taskCounts;
    }

}
