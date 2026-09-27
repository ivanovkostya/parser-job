package org.example.parserjob.controller;

import org.example.parserjob.task.sync.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.*;

@RestController
public class SyncController {

    private static final int THREADS = 8;
    private static final int PER_THREAD = 10_000;

    // ГОНКА ДАННЫХ (без synchronized)
    @GetMapping("/race-condition")
    public Map<String, Object> raceCondition() throws InterruptedException {
        UnsafeCollector c = new UnsafeCollector();
        runUnsafe(c);
        return Map.of(
                "expected", THREADS * PER_THREAD,
                "actual", c.getItemsSize(),
                "lost", THREADS * PER_THREAD - c.getItemsSize()
        );
    }

    // СИНХРОНИЗАЦИЯ (synchronized)
    @GetMapping("/race-condition/sync")
    public Map<String, Object> raceConditionSync() throws InterruptedException {
        DataCollector c = new DataCollector();
        runSafe(c);
        return Map.of(
                "expected", THREADS * PER_THREAD,
                "actual", c.getItemsSize(),
                "lost", THREADS * PER_THREAD - c.getItemsSize()
        );
    }

    // WAIT / NOTIFYALL
    @GetMapping("/wait-notify")
    public Map<String, Object> waitNotify() throws InterruptedException {
        DataCollector c = new DataCollector();
        Thread consumer = new Thread(() -> {
            try { c.waitForItems(100); } catch (InterruptedException e) { return; }
        });
        consumer.start();
        Thread.sleep(200);
        for (int i = 0; i < 100; i++) c.collectItem(new Item("k" + i));
        consumer.join();
        return Map.of("itemsAfterWait", c.getItemsSize());
    }

    // DEADLOCK (зависнет, вызывать осторожно, таймаут 3 сек)
    @GetMapping("/transfer-deadlock")
    public Map<String, Object> transferDeadlock() {
        BankAccount a = new BankAccount("A", 1000);
        BankAccount b = new BankAccount("B", 1000);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        Future<?> f1 = pool.submit(() -> TransferService.deadlockTransfer(a, b, 100));
        Future<?> f2 = pool.submit(() -> TransferService.deadlockTransfer(b, a, 100));

        try {
            f1.get(3, TimeUnit.SECONDS);
            f2.get(3, TimeUnit.SECONDS);
            pool.shutdownNow();
            return Map.of("status", "no deadlock (unexpected)");
        } catch (TimeoutException e) {
            pool.shutdownNow();
            return Map.of("status", "DEADLOCK detected (timeout)", "a", a.getBalance(), "b", b.getBalance());
        } catch (Exception e) {
            pool.shutdownNow();
            return Map.of("status", "error: " + e.getMessage());
        }
    }

    // БЕЗ DEADLOCK (безопасный порядок захвата)
    @GetMapping("/transfer-safe")
    public Map<String, Object> transferSafe() throws Exception {
        BankAccount a = new BankAccount("A", 1000);
        BankAccount b = new BankAccount("B", 1000);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        Future<?> f1 = pool.submit(() -> TransferService.safeTransfer(a, b, 100));
        Future<?> f2 = pool.submit(() -> TransferService.safeTransfer(b, a, 100));

        f1.get(3, TimeUnit.SECONDS);
        f2.get(3, TimeUnit.SECONDS);
        pool.shutdown();

        return Map.of("a", a.getBalance(), "b", b.getBalance(), "sum", a.getBalance() + b.getBalance());
    }

    // --- вспомогательные ---

    private void runSafe(DataCollector c) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < THREADS; t++) {
            final int id = t;
            Thread th = new Thread(() -> {
                for (int i = 0; i < PER_THREAD; i++) {
                    c.collectItem(new Item(id + "-" + i));
                    c.incrementProcessed();
                }
            });
            threads.add(th); th.start();
        }
        for (Thread th : threads) th.join();
    }

    private void runUnsafe(UnsafeCollector c) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < THREADS; t++) {
            final int id = t;
            Thread th = new Thread(() -> {
                for (int i = 0; i < PER_THREAD; i++) {
                    c.collectItem(new Item(id + "-" + i));
                    c.incrementProcessed();
                }
            });
            threads.add(th); th.start();
        }
        for (Thread th : threads) th.join();
    }
}