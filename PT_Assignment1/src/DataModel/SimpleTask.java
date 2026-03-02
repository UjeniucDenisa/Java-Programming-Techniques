package DataModel;

public non-sealed class SimpleTask extends Task{
    private int startHour;
    private int endHour;

    public SimpleTask(String statusTask, int idTask, int startHour, int endHour) {
        super(statusTask, idTask);
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    @Override
    public String toString() {
        return "SimpleTask{" +
                "idTask=" + idTask +" estimateDuration="+this.estimateDuration()+
                '}';
    }

    @Override
    public int estimateDuration() {
        return endHour - startHour;
    }
}
