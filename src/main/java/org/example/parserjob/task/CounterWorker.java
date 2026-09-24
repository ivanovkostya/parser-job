package org.example.parserjob.task;

import org.example.parserjob.service.CounterService;

public class CounterWorker extends Thread {

    private final CounterService counterService;
    private final int iterations;

    public CounterWorker(CounterService counterService, int iterations) {
        super("CounterWorker");
        this.counterService = counterService;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        for (int i = 1; i <= iterations; i++) {
            counterService.printInfo(getName(), i);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}