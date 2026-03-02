package BusinessLogic;

import DataModel.Employee;
import DataModel.Task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

public class TasksManagement implements Serializable {
    private Map<Employee, List<Task>> map = new HashMap<>();

    public void assignTaskToEmployee(Employee employee, Task task) {
        if (!map.containsKey(employee)) {
            map.put(employee, new ArrayList<>());
        }
        map.get(employee).add(task);
        employee.addTask(task);  // Adăugăm sarcina și în lista din Employee
    }

    public int calculateEmployeeWorkDuration(Employee employee) {
        int totalDuration = 0;
        if (map.containsKey(employee)) {
            for (Task task : map.get(employee)) {
                if (task.getStatusTask().equalsIgnoreCase("Completed")) {
                    totalDuration += task.estimateDuration();
                }
            }
        }
        return totalDuration;
    }

    public void modifyTaskStatus(Employee employee, Task task, String status) {
        if (map.containsKey(employee)) {
            for (Task task1 : map.get(employee)) {
                if (task1.equals(task)) {
                    task1.setStatusTask(status);
                }
            }
        }
    }

    public boolean hasTaskWithId(int id) {
        for (List<Task> tasks : map.values()) {
            for (Task task : tasks) {
                if (task.getIdTask() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<Employee, List<Task>> getMap() {
        return map;
    }
}
