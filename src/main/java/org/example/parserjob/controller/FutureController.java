package org.example.parserjob.controller;

import org.example.parserjob.service.PeriodicDataAggregator;
import org.example.parserjob.task.future.LongTask;
import org.example.parserjob.task.future.ShortTask;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.*;

@RestController
public class FutureController {

    private final PeriodicDataAggregator aggregator;

    public FutureController(PeriodicDataAggregator aggregator) {
        this.aggregator = aggregator;
    }

    // 1. Долгая задача + Future + cancel()
    @GetMapping("/future/cancel")
    public Map<String, Object> futureCancel() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<String> future = pool.submit(new LongTask(8000));

        Thread.sleep(2500);

        boolean cancelled = future.cancel(true);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cancelled", cancelled);
        result.put("isCancelled", future.isCancelled());
        result.put("isDone", future.isDone());

        try {
            future.get();
            result.put("result", "задача успела завершиться");
        } catch (CancellationException e) {
            result.put("result", "CancellationException - задача отменена");
        }

        pool.shutdown();
        return result;
    }

    // 2. Список задач + invokeAll()
    @GetMapping("/future/invoke-all")
    public Map<String, Object> invokeAll() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        Random random = new Random();

        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            long duration = 1000 + random.nextInt(2000);
            tasks.add(new ShortTask(i, duration));
        }

        long start = System.nanoTime();
        List<Future<Integer>> futures = pool.invokeAll(tasks);
        long totalTime = (System.nanoTime() - start) / 1_000_000;

        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("taskId", i + 1);
            m.put("result", futures.get(i).get());
            results.add(m);
        }

        pool.shutdown();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalTasks", tasks.size());
        summary.put("totalTimeMs", totalTime);
        summary.put("results", results);
        return summary;
    }

    // 3. Периодический агрегатор
    @GetMapping("/aggregator/start")
    public Map<String, Object> startAggregator() {
        aggregator.start(5);
        return Map.of("status", "started", "periodSeconds", 5);
    }

    @GetMapping("/aggregator/status")
    public Map<String, Object> aggregatorStatus() {
        return aggregator.getStatus();
    }

    @GetMapping("/aggregator/stop")
    public Map<String, Object> stopAggregator() {
        aggregator.stop();
        return Map.of("status", "stopped");
    }
}