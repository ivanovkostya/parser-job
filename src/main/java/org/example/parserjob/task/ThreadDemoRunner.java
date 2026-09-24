package org.example.parserjob.task;

import org.example.parserjob.service.CounterService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// @Component
public class ThreadDemoRunner implements CommandLineRunner {

    private final CounterService counterService;

    public ThreadDemoRunner(CounterService counterService) {
        this.counterService = counterService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Запуск демонстрации потоков ===");

        CounterWorker counterWorker = new CounterWorker(counterService, 5);
        counterWorker.start();

        Thread loggerThread = new Thread(
                new LoggerThread(counterService, 5),
                "LoggerThread"
        );
        loggerThread.start();

        counterWorker.join();
        loggerThread.join();

        System.out.println("=== Потоки завершены ===");
        System.out.println("=== Активные потоки ===");
        Thread.getAllStackTraces().keySet().forEach(t ->
                System.out.println("Имя: " + t.getName()
                        + ", состояние: " + t.getState()
                        + ", daemon: " + t.isDaemon())
        );
    }
}