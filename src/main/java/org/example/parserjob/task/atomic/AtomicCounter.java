package org.example.parserjob.task.atomic;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter {

    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet();
    }

    public int get() {
        return count.get();
    }
}