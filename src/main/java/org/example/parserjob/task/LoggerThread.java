package org.example.parserjob.task;

import org.example.parserjob.service.CounterService;

public class LoggerThread implements Runnable {

    private final CounterService counterService;
    private final int iterations;

    public LoggerThread(CounterService counterService, int iterations) {
        this.counterService = counterService;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        for (int i = 1; i <= iterations; i++) {
            counterService.printInfo(Thread.currentThread().getName(), i);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}