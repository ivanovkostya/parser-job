package org.example.parserjob.task.buffer;

import java.util.concurrent.atomic.AtomicInteger;

public class Consumer implements Runnable {

    private final BoundedBuffer<Integer> buffer;
    private final int count;
    private final AtomicInteger consumed;

    public Consumer(BoundedBuffer<Integer> buffer, int count, AtomicInteger consumed) {
        this.buffer = buffer;
        this.count = count;
        this.consumed = consumed;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < count; i++) {
                buffer.take();
                consumed.incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}