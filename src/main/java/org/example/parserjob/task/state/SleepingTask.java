package org.example.parserjob.task.state;

public class SleepingTask extends Thread {

    public SleepingTask() {
        super("SleepingTask");
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}