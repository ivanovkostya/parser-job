package org.example.parserjob.task.atomic;

import java.util.concurrent.atomic.AtomicReference;

public class AtomicCache {

    private final AtomicReference<String> value = new AtomicReference<>(null);

    public String getOrCreate() {
        String current = value.get();
        if (current != null) return current;

        String created = "cached-" + System.currentTimeMillis();
        if (value.compareAndSet(null, created)) {
            return created;
        }
        return value.get();
    }

    public String get() {
        return value.get();
    }
}