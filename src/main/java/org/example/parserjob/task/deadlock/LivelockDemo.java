package org.example.parserjob.task.deadlock;

public class LivelockDemo {

    private static class Worker {
        private final String name;
        private boolean active = true;

        Worker(String name) { this.name = name; }

        void workWith(Worker other) {
            while (active) {
                if (other.active) {
                    System.out.println(name + ": вижу, что " + other.name + " активен, уступаю");
                    try { Thread.sleep(100); } catch (InterruptedException e) { return; }
                } else {
                    System.out.println(name + ": работаю");
                    active = false;
                }
            }
        }
    }

    public void startLivelock() {
        Worker w1 = new Worker("Worker-1");
        Worker w2 = new Worker("Worker-2");

        new Thread(() -> w1.workWith(w2), "Livelock-1").start();
        new Thread(() -> w2.workWith(w1), "Livelock-2").start();
    }
}