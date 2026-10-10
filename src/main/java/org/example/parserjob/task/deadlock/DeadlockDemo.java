package org.example.parserjob.task.deadlock;

public class DeadlockDemo {

    private final Object resourceA = new Object();
    private final Object resourceB = new Object();

    public void startDeadlock() {
        Thread t1 = new Thread(() -> {
            synchronized (resourceA) {
                System.out.println("[Thread-1] захватил resourceA");
                sleep(100);
                System.out.println("[Thread-1] ждёт resourceB...");
                synchronized (resourceB) {
                    System.out.println("[Thread-1] захватил resourceB");
                }
            }
        }, "Deadlock-Thread-1");

        Thread t2 = new Thread(() -> {
            synchronized (resourceB) {
                System.out.println("[Thread-2] захватил resourceB");
                sleep(100);
                System.out.println("[Thread-2] ждёт resourceA...");
                synchronized (resourceA) {
                    System.out.println("[Thread-2] захватил resourceA");
                }
            }
        }, "Deadlock-Thread-2");

        t1.start();
        t2.start();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}