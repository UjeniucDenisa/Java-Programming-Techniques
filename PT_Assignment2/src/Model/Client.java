package Model;

public class Client implements Comparable<Client>{
    private final int ID;
    private final int t_arrival;
    private int t_service;
    private int waiting_time;
    private final int initialServiceTime;

    public Client(int ID, int t_arrival, int t_service) {
        this.ID = ID;
        this.t_arrival = t_arrival;
        this.t_service = t_service;
        this.waiting_time = 0;
        this.initialServiceTime = t_service;
    }

    public int getID() {
        return ID;
    }

    public int getInitialServiceTime() {
        return initialServiceTime;
    }

    public int getT_arrival() {
        return t_arrival;
    }

    public int getT_service() {
        return t_service;
    }

    public int getWaiting_time() {
        return waiting_time;
    }

    public void setWaiting_time(int waiting_time) {
        this.waiting_time = waiting_time;
    }

    public void decreaseServiceTime() {
        if(t_service > 0) {
            t_service--;
        }
    }

    @Override
    public String toString() {
        return "(" + ID + "," + t_arrival + "," + t_service + ")";
    }

    @Override
    public int compareTo(Client o) {
        return Integer.compare(this.t_arrival, o.t_arrival);
    }
}