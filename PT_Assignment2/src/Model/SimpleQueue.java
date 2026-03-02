package Model;

import java.util.*;
public class SimpleQueue implements Runnable {
    private final int id;
    private final Queue<Client> clients=new LinkedList<>();
    private int waitingTime=0;
    private boolean isRunning=true;
    private Client currentClient=null;

    public SimpleQueue(int id) {
        this.id = id;
    }

    public synchronized void addClient(Client client) {
        clients.add(client);
        waitingTime+=client.getT_service();
        client.setWaiting_time(waitingTime-client.getT_service());
        notify();
    }

    public synchronized int getWaitingTime() {
        return waitingTime;
    }

    public synchronized void stop()
    {
        isRunning=false;
        notifyAll();
    }

    public synchronized int getClientCount() {
        int count = clients.size();
        if (currentClient != null) {
            count++;
        }
        return count;
    }

    @Override
    public void run() {
        while(isRunning||!clients.isEmpty()||currentClient!=null)
        {
            try{  // sectiune sincronizata pentru manipularea clientului curent
                synchronized(this)
                {
                    if(currentClient==null&&!clients.isEmpty())// daca nu exista client curent si coada nu e goala
                    {
                        currentClient=clients.poll();// extrage urmatorul client din coada
                    }
                    if(currentClient==null)// daca tot nu exista client curent
                    {
                        wait();// asteapta notificare pentru client nou sau oprire
                        continue;
                    }

                }
                Thread.sleep(1000);// simuleaza procesarea pentru 1 secunda

                synchronized(this){// sectiune sincronizata pentru actualizarea timpului de servire
                    currentClient.decreaseServiceTime();
                    if(currentClient.getT_service()<=0)  // verifica daca s-a terminat procesarea
                    {
                        waitingTime -=currentClient.getT_service();// actualizeaza timpul total de asteptare al cozii
                        currentClient=null; // elibereaza clientul curent
                    }
                }

            } catch (InterruptedException e) {// gestionare intrerupere thread
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    public synchronized String getStatus() {// returneaza starea curenta a cozii
        if (currentClient != null) {// afiseaza clientul in curs de procesare
            return String.format("Queue %d: %s (remaining: %d)", id, currentClient, currentClient.getT_service());
        } else if (!clients.isEmpty()) {// afiseaza ca asteapta urmatorul client
            return String.format("Queue %d: Waiting for next client", id);
        } else {
            return String.format("Queue %d: closed", id);// afiseaza ca coada este inchisa
        }
    }

    public synchronized boolean isEmpty() {
        return currentClient == null && clients.isEmpty();
    }
}