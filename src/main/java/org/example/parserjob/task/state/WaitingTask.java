package org.example.parserjob.task.state;

public class WaitingTask extends Thread {

    private final Object lock;

    public WaitingTask(Object lock) {
        super("WaitingTask");
        this.lock = lock;
    }

    @Override
    public void run() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}