package DataModel;

import java.util.ArrayList;
import java.util.List;

public non-sealed class ComplexTask extends Task {
    private List<Task> subTasks = new ArrayList<>();

    public ComplexTask(String statusTask, int idTask) {
        super(statusTask, idTask);
    }

    public List<Task> getSubTasks() {
        return subTasks;
    }

    public void addSubTask(Task task) {
        if (hasSubTaskWithId(task.getIdTask())) {
            throw new IllegalArgumentException("Un subtask cu ID-ul " + task.getIdTask() + " deja exista.");
        }
        subTasks.add(task);
    }

    public boolean hasSubTaskWithId(int id) {
        for (Task subTask : subTasks) {
            if (subTask.getIdTask() == id) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ComplexTask{" +
                "subTasks=" + subTasks +
                ", idTask=" + idTask +
                '}';
    }

    @Override
    public int estimateDuration() {
        int total = 0;
        for (Task task : subTasks) {
            total += task.estimateDuration();
        }
        return total;
    }
}