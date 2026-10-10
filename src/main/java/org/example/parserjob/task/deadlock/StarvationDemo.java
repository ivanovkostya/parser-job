package org.example.parserjob.task.deadlock;

import java.util.concurrent.atomic.AtomicLong;

public class StarvationDemo {

    private final Object lock = new Object();
    private final AtomicLong starvingCount = new AtomicLong();
    private final AtomicLong hogCount = new AtomicLong();

    public void startStarvation() {
        // Поток постоянно захватывает монитор, не даёт другим
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                while (true) {
                    synchronized (lock) {
                        hogCount.incrementAndGet();
                    }
                }
            }, "Hog-" + i).start();
        }

        // «Голодающий» поток, получает монитор очень редко
        new Thread(() -> {
            while (true) {
                synchronized (lock) {
                    starvingCount.incrementAndGet();
                }
            }
        }, "Starving").start();
    }

    public long getStarvingCount() { return starvingCount.get(); }
    public long getHogCount() { return hogCount.get(); }
}