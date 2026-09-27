package org.example.parserjob.controller;

import org.example.parserjob.task.atomic.AtomicCache;
import org.example.parserjob.task.atomic.AtomicCounter;
import org.example.parserjob.task.atomic.StoppableTask;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class AtomicController {

    private static final int THREADS = 8;
    private static final int PER_THREAD = 100_000;

    // Остановка потока через volatile
    @GetMapping("/volatile-stop")
    public Map<String, Object> volatileStop() throws InterruptedException {
        StoppableTask task = new StoppableTask();
        task.start();
        Thread.sleep(200);       // даём поработать
        task.stopWork();         // просим остановиться
        task.join(1000);         // ждём завершения

        return Map.of(
                "stopped", !task.isAlive(),
                "iterations", task.getIterations()
        );
    }

    // Счётчик через AtomicInteger
    @GetMapping("/atomic-counter")
    public Map<String, Object> atomicCounter() throws InterruptedException {
        AtomicCounter counter = new AtomicCounter();
        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < THREADS; t++) {
            Thread th = new Thread(() -> {
                for (int i = 0; i < PER_THREAD; i++) counter.increment();
            });
            threads.add(th); th.start();
        }
        for (Thread th : threads) th.join();

        return Map.of(
                "expected", THREADS * PER_THREAD,
                "actual", counter.get()
        );
    }

    // Счётчик без атомарности (для сравнения)
    @GetMapping("/atomic-counter/unsafe")
    public Map<String, Object> unsafeCounter() throws InterruptedException {
        int[] count = {0};   // обычный int, не атомарный
        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < THREADS; t++) {
            Thread th = new Thread(() -> {
                for (int i = 0; i < PER_THREAD; i++) count[0]++;
            });
            threads.add(th); th.start();
        }
        for (Thread th : threads) th.join();

        return Map.of(
                "expected", THREADS * PER_THREAD,
                "actual", count[0],
                "lost", THREADS * PER_THREAD - count[0]
        );
    }

    // Singleton-кэш через AtomicReference
    @GetMapping("/atomic-cache")
    public Map<String, Object> atomicCache() throws InterruptedException {
        AtomicCache cache = new AtomicCache();
        Set<String> values = Collections.synchronizedSet(new HashSet<>());
        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < THREADS; t++) {
            Thread th = new Thread(() -> values.add(cache.getOrCreate()));
            threads.add(th); th.start();
        }
        for (Thread th : threads) th.join();

        return Map.of(
                "uniqueValues", values.size(),   // должен быть 1
                "value", cache.get()
        );
    }
}