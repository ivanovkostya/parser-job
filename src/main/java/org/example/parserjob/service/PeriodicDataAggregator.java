package org.example.parserjob.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PeriodicDataAggregator {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ExecutorService workers = Executors.newFixedThreadPool(4);
    private final Random random = new Random();

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger failCount = new AtomicInteger();

    public void start(long periodSeconds) {
        scheduler.scheduleAtFixedRate(this::aggregate, 0, periodSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
        workers.shutdownNow();
    }

    private void aggregate() {
        List<String> ids = List.of("USD", "EUR", "GBP", "JPY", "CNY");

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String id : ids) {
            CompletableFuture<Void> f = CompletableFuture.runAsync(() -> process(id), workers);
            futures.add(f);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void process(String id) {
        try {
            String data = fetchFromExternal(id);
            cache.put(id, data);
            successCount.incrementAndGet();
        } catch (Exception e) {
            failCount.incrementAndGet();
        }
    }

    private String fetchFromExternal(String id) {
        try {
            Thread.sleep(200 + random.nextInt(300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        if (random.nextInt(10) < 2) {
            throw new RuntimeException("внешний источник недоступен для " + id);
        }
        return id + "=" + (100 + random.nextInt(100));
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("successCount", successCount.get());
        status.put("failCount", failCount.get());
        status.put("cacheSize", cache.size());
        status.put("cache", new LinkedHashMap<>(cache));
        return status;
    }
}