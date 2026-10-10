package org.example.parserjob.controller;

import org.example.parserjob.task.http.HttpFetcher;
import org.example.parserjob.task.http.HttpResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class HttpController {

    private final ThreadPoolExecutor httpExecutor;
    private final HttpFetcher fetcher = new HttpFetcher();

    public HttpController(ThreadPoolExecutor httpExecutor) {
        this.httpExecutor = httpExecutor;
    }

    // Список URL по умолчанию — 20 публичных API
    private static final List<String> DEFAULT_URLS = List.of(
            "https://api.github.com",
            "https://api.github.com/users/octocat",
            "https://api.github.com/repos/spring-projects/spring-boot",
            "https://api.github.com/repos/openjdk/jdk",
            "https://api.github.com/repos/google/guava",
            "https://api.github.com/repos/apache/kafka",
            "https://api.github.com/repos/elastic/elasticsearch",
            "https://api.github.com/repos/docker/docker-ce",
            "https://api.github.com/repos/kubernetes/kubernetes",
            "https://api.github.com/repos/rust-lang/rust",
            "https://api.github.com/repos/python/cpython",
            "https://api.github.com/repos/nodejs/node",
            "https://api.github.com/repos/facebook/react",
            "https://api.github.com/repos/vuejs/vue",
            "https://api.github.com/repos/angular/angular",
            "https://api.github.com/repos/torvalds/linux",
            "https://api.github.com/repos/microsoft/vscode",
            "https://api.github.com/repos/jetbrains/kotlin",
            "https://api.github.com/repos/golang/go",
            "https://api.github.com/repos/spring-projects/spring-framework"
    );

    @GetMapping("/fetch")
    public Map<String, Object> fetch(
            @RequestParam(required = false) List<String> urls) throws InterruptedException, ExecutionException {

        List<String> targets = (urls == null || urls.isEmpty()) ? DEFAULT_URLS : urls;

        // Сводка
        AtomicInteger okCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        long start = System.nanoTime();

        // Отправляем все задачи в ThreadPoolExecutor
        List<Future<HttpResult>> futures = new ArrayList<>();
        for (String url : targets) {
            futures.add(httpExecutor.submit(() -> fetcher.fetch(url)));
        }

        // Собираем результаты
        List<HttpResult> results = new ArrayList<>();
        for (Future<HttpResult> f : futures) {
            HttpResult r = f.get();
            results.add(r);
            if (r.isOk()) okCount.incrementAndGet();
            else failCount.incrementAndGet();
        }

        long totalTime = (System.nanoTime() - start) / 1_000_000;

        // Статистика по времени
        long minTime = results.stream().filter(HttpResult::isOk).mapToLong(HttpResult::getTimeMs).min().orElse(0);
        long maxTime = results.stream().filter(HttpResult::isOk).mapToLong(HttpResult::getTimeMs).max().orElse(0);
        double avgTime = results.stream().filter(HttpResult::isOk).mapToLong(HttpResult::getTimeMs).average().orElse(0);

        // Результаты по каждому URL (сортировка по времени)
        List<Map<String, Object>> details = results.stream()
                .sorted(Comparator.comparingLong(HttpResult::getTimeMs))
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("url", r.getUrl());
                    m.put("status", r.getStatus());
                    m.put("timeMs", r.getTimeMs());
                    if (!r.isOk()) m.put("error", r.getError());
                    return m;
                })
                .toList();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalUrls", targets.size());
        summary.put("ok", okCount.get());
        summary.put("failed", failCount.get());
        summary.put("totalTimeMs", totalTime);
        summary.put("minTimeMs", minTime);
        summary.put("maxTimeMs", maxTime);
        summary.put("avgTimeMs", Math.round(avgTime));
        summary.put("poolSize", httpExecutor.getPoolSize());
        summary.put("queueSize", httpExecutor.getQueue().size());
        summary.put("details", details);

        return summary;
    }
}