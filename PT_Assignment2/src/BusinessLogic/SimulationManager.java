package BusinessLogic;

import Model.Client;
import Model.SimpleQueue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationManager implements Runnable {
    private final int N;
    private final int Q;
    private final int simulation_time;
    private final int min_arrival_time;
    private final int max_arrival_time;
    private final int min_service_time;
    private final int max_service_time;

    private final List<Client> generated_clients = new ArrayList<>();
    private final List<SimpleQueue> queues = new ArrayList<>();
    private final List<Client> waiting_clients = new ArrayList<>();
    private final List<String> log_events = new ArrayList<>();
    private final AtomicInteger current_time = new AtomicInteger(0);
    private double average_waiting_time = 0;

    private final List<Thread> queueThreads = new ArrayList<>();
    private BufferedWriter log_writer;

    public SimulationManager(int N, int Q, int simulation_time,
                             int min_arrival_time, int max_arrival_time,
                             int min_service_time, int max_service_time) {
        this.N = N;
        this.Q = Q;
        this.simulation_time = simulation_time;
        this.min_arrival_time = min_arrival_time;
        this.max_arrival_time = max_arrival_time;
        this.min_service_time = min_service_time;
        this.max_service_time = max_service_time;

        try {
            this.log_writer = new BufferedWriter(new FileWriter("simulation_log.txt"));
        } catch (IOException e) {
            System.err.println("Error creating log file: " + e.getMessage());
        }

        generate_clients();
        initialize_queues();
    }

    private void generate_clients() {
        Random random = new Random();
        for (int i = 1; i <= N; i++) {
            int arrival_time = min_arrival_time + random.nextInt(max_arrival_time - min_arrival_time + 1);
            int service_time = min_service_time + random.nextInt(max_service_time - min_service_time + 1);
            generated_clients.add(new Client(i, arrival_time, service_time));
        }
        Collections.sort(generated_clients, Comparator.comparingInt(Client::getT_arrival));
        waiting_clients.addAll(generated_clients);
    }

    private void initialize_queues() {
        for (int i = 1; i <= Q; i++) {
            SimpleQueue queue = new SimpleQueue(i);
            queues.add(queue);
            Thread thread = new Thread(queue);
            thread.start();
            queueThreads.add(thread);
        }
    }

    private SimpleQueue get_shortest_queue() {
        SimpleQueue shortest = null;
        int min_waiting_time = Integer.MAX_VALUE;

        for (SimpleQueue queue : queues) {
            int waiting_time = queue.getWaitingTime();
            if (waiting_time < min_waiting_time) {
                min_waiting_time = waiting_time;
                shortest = queue;
            }
        }
        return shortest;
    }

    private void update_waiting_clients() {
        Iterator<Client> iterator = waiting_clients.iterator();
        while (iterator.hasNext()) {
            Client client = iterator.next();
            if (client.getT_arrival() <= current_time.get()) {
                SimpleQueue shortest_queue = get_shortest_queue();
                if (shortest_queue != null) {
                    shortest_queue.addClient(client);
                    iterator.remove();
                }
            }
        }
    }

    private void log_current_state() {
        StringBuilder sb = new StringBuilder();
        sb.append("Time ").append(current_time.get()).append("\n");

        sb.append("Waiting clients: ");
        if (waiting_clients.isEmpty()) {
            sb.append("none");
        } else {
            for (Client client : waiting_clients) {
                sb.append(client).append("; ");
            }
        }
        sb.append("\n");

        for (SimpleQueue queue : queues) {
            sb.append(queue.getStatus()).append("\n");
        }

        String log_entry = sb.toString();
        log_events.add(log_entry);

        try {
            log_writer.write(log_entry);
            log_writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to log: " + e.getMessage());
        }

        System.out.println(log_entry);
    }

    private void calculate_average_waiting_time() {
        if (generated_clients.isEmpty()) return;

        double total_waiting_time = 0;
        for (Client client : generated_clients) {
            total_waiting_time += client.getWaiting_time();
        }
        average_waiting_time = total_waiting_time / N;

        String result = String.format("Average waiting time: %.2f", average_waiting_time);
        log_events.add(result);

        try {
            log_writer.write(result);
            log_writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing result: " + e.getMessage());
        }

        System.out.println(result);
    }

    private boolean all_queues_empty() {
        for (SimpleQueue queue : queues) {
            if (!queue.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        try {//scrie header-ul simularii in fisierul de log
            log_writer.write("Simulation started with " + N + " clients and " + Q + " queues.");
            log_writer.newLine();
            log_writer.write("Simulation time: " + simulation_time + " seconds");
            log_writer.newLine();
            log_writer.write("Arrival time range: [" + min_arrival_time + ", " + max_arrival_time + "]");
            log_writer.newLine();
            log_writer.write("Service time range: [" + min_service_time + ", " + max_service_time + "]");
            log_writer.newLine();
            log_writer.newLine();
        } catch (IOException e) {
            System.err.println("Error initializing log: " + e.getMessage());
        }

        // bucla principala a simularii
        while (current_time.get() <= simulation_time && (!waiting_clients.isEmpty() || !all_queues_empty())) {
            update_waiting_clients();// actualizeaza lista clientilor care asteapta
            log_current_state();// inregistreaza starea curenta in log

            try {
                Thread.sleep(1000);// asteapta 1 secunda
                current_time.incrementAndGet();// incr timpul curent
            } catch (InterruptedException e) {
                // gestionare intrerupere thread
                Thread.currentThread().interrupt();
                break;
            }
        }

        stop_simulation(); // opreste simularea la final
    }

    public void stop_simulation() { // opreste toate cozile
        for (SimpleQueue queue : queues) {
            queue.stop();
        }

        // asteapta terminarea thread-urilor cozilor
        for (Thread thread : queueThreads) {
            try {
                thread.join(1000);// asteapta maxim 1 secunda terminarea thread-ului
                if (thread.isAlive()) { // daca thread-ul inca ruleaza, il intrerupe
                    thread.interrupt();
                }
            } catch (InterruptedException e) {
                thread.interrupt();
            }
        }

        calculate_average_waiting_time();  // calc si afi timpul mediu de asteptare

        // inchide fisierul de log
        try {
            log_writer.close();
        } catch (IOException e) {
            System.err.println("Error closing log file: " + e.getMessage());
        }
    }

    // returneaza lista de evenimente din log
    public List<String> get_log_events() {
        return Collections.unmodifiableList(log_events);
    }

    public double get_average_waiting_time() {
        return average_waiting_time;
    }
}