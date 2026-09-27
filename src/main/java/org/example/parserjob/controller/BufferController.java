package org.example.parserjob.controller;

import org.example.parserjob.task.buffer.BoundedBuffer;
import org.example.parserjob.task.buffer.Consumer;
import org.example.parserjob.task.buffer.Producer;
import org.example.parserjob.task.buffer.SynchronizedBuffer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class BufferController {

    private static final int CAPACITY = 10;
    private static final int PRODUCERS = 4;
    private static final int CONSUMERS = 4;
    private static final int PER_THREAD = 10_000;

    // ReentrantLock + Condition
    @GetMapping("/buffer/reentrant")
    public Map<String, Object> reentrantLock() throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(CAPACITY);
        AtomicInteger consumed = new AtomicInteger();
        long time = run(buffer, consumed);
        return Map.of(
                "type", "ReentrantLock + Condition",
                "produced", PRODUCERS * PER_THREAD,
                "consumed", consumed.get(),
                "timeMs", time / 1_000_000
        );
    }

    // synchronized + wait/notifyAll
    @GetMapping("/buffer/synchronized")
    public Map<String, Object> synchronizedBuffer() throws InterruptedException {
        SynchronizedBuffer<Integer> buffer = new SynchronizedBuffer<>(CAPACITY);
        AtomicInteger consumed = new AtomicInteger();
        long time = runSync(buffer, consumed);
        return Map.of(
                "type", "synchronized + wait/notifyAll",
                "produced", PRODUCERS * PER_THREAD,
                "consumed", consumed.get(),
                "timeMs", time / 1_000_000
        );
    }

    // --- вспомогательные ---

    private long run(BoundedBuffer<Integer> buffer, AtomicInteger consumed) throws InterruptedException {
        long start = System.nanoTime();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < PRODUCERS; i++) {
            threads.add(new Thread(new Producer(buffer, PER_THREAD, i), "producer-" + i));
        }
        for (int i = 0; i < CONSUMERS; i++) {
            threads.add(new Thread(new Consumer(buffer, PER_THREAD, consumed), "consumer-" + i));
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        return System.nanoTime() - start;
    }

    private long runSync(SynchronizedBuffer<Integer> buffer, AtomicInteger consumed) throws InterruptedException {
        long start = System.nanoTime();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < PRODUCERS; i++) {
            final int id = i;
            threads.add(new Thread(() -> {
                try {
                    for (int j = 0; j < PER_THREAD; j++) buffer.put(id * 1000 + j);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "sync-producer-" + i));
        }
        for (int i = 0; i < CONSUMERS; i++) {
            threads.add(new Thread(() -> {
                try {
                    for (int j = 0; j < PER_THREAD; j++) {
                        buffer.take();
                        consumed.incrementAndGet();
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "sync-consumer-" + i));
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        return System.nanoTime() - start;
    }
}