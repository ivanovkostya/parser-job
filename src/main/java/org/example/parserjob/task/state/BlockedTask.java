package org.example.parserjob.task.state;

public class BlockedTask extends Thread {

    private final Object lock;

    public BlockedTask(Object lock) {
        super("BlockedTask");
        this.lock = lock;
    }

    @Override
    public void run() {
        synchronized (lock) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}