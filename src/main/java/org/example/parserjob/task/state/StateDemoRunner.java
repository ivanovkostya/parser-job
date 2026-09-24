package org.example.parserjob.task.state;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// @Component
public class StateDemoRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Демонстрация состояний потоков ===");

        Object lock = new Object();

        SleepingTask sleeping = new SleepingTask();
        WaitingTask waiting = new WaitingTask(lock);
        BlockedTask blocked1 = new BlockedTask(lock);
        BlockedTask blocked2 = new BlockedTask(lock);

        printState("После создания", sleeping, waiting, blocked1, blocked2);

        sleeping.start();
        waiting.start();
        blocked1.start();
        Thread.sleep(100);
        blocked2.start();
        Thread.sleep(100);

        printState("Во время работы", sleeping, waiting, blocked1, blocked2);

        synchronized (lock) {
            lock.notifyAll();
        }

        sleeping.join();
        waiting.join();
        blocked1.join();
        blocked2.join();

        printState("После завершения", sleeping, waiting, blocked1, blocked2);
    }

    private void printState(String label, Thread... threads) {
        System.out.println("--- " + label + " ---");
        for (Thread t : threads) {
            System.out.println("  " + t.getName() + ": " + t.getState());
        }
    }
}