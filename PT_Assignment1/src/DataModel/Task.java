package DataModel;

import java.io.Serializable;

public sealed abstract class Task implements Serializable permits ComplexTask,SimpleTask{
    protected int idTask;
    protected String statusTask;

    public Task(String statusTask, int idTask) {
        this.statusTask = statusTask;
        this.idTask = idTask;
    }

    public int getIdTask() {
        return idTask;
    }

    @Override
    public String toString() {
        return "Task{" +
                "idTask=" + idTask +
                ", statusTask='" + statusTask + '\'' +
                '}';
    }

    public void setIdTask(int idTask) {
        this.idTask = idTask;
    }

    public String getStatusTask() {
        return statusTask;
    }

    public void setStatusTask(String statusTask) {
        this.statusTask = statusTask;
    }

    public abstract int estimateDuration();
}
