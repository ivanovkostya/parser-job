package org.example.parserjob.task.atomic;

public class StoppableTask extends Thread {

    private volatile boolean stopped = false;
    private long iterations = 0;

    public StoppableTask() {
        super("StoppableTask");
    }

    public void stopWork() {
        stopped = true;
    }

    public long getIterations() {
        return iterations;
    }

    @Override
    public void run() {
        while (!stopped) {
            iterations++;
        }
    }
}