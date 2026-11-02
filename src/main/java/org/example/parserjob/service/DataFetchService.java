package org.example.parserjob.service;

import org.example.parserjob.task.async.FinalResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class DataFetchService {

    private final ExecutorService executor;
    private final Random random = new Random();

    public DataFetchService(@Qualifier("asyncExecutor") ExecutorService executor) {
        this.executor = executor;
    }

    public List<FinalResult> fetchAllDataAsync(List<String> ids) {
        return ids.stream()
                .map(this::fetchOneAsync)
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private CompletableFuture<FinalResult> fetchOneAsync(String id) {
        CompletableFuture<String> nameF =
                CompletableFuture.supplyAsync(() -> fetchName(id), executor);

        CompletableFuture<Double> priceF =
                CompletableFuture.supplyAsync(() -> fetchPrice(id), executor);

        CompletableFuture<Double> rateF =
                CompletableFuture.supplyAsync(() -> fetchRate(id), executor);

        return nameF.thenCombine(priceF.thenCombine(rateF, (p, r) -> p * r),
                        (name, finalPrice) -> FinalResult.ok(id, name, finalPrice))
                .handle((res, ex) -> ex != null
                        ? FinalResult.fail(id, ex.getMessage())
                        : res);
    }

    public List<FinalResult> fetchAllDataSync(List<String> ids) {
        List<FinalResult> results = new ArrayList<>();
        for (String id : ids) {
            try {
                results.add(FinalResult.ok(id, fetchName(id), fetchPrice(id) * fetchRate(id)));
            } catch (Exception e) {
                results.add(FinalResult.fail(id, e.getMessage()));
            }
        }
        return results;
    }

    private String fetchName(String id) {
        sleep(100 + random.nextInt(200));
        if (random.nextInt(10) < 1) throw new RuntimeException("источник имён недоступен для " + id);
        return "Товар-" + id;
    }

    private double fetchPrice(String id) {
        sleep(100 + random.nextInt(200));
        if (random.nextInt(10) < 1) throw new RuntimeException("источник цен недоступен для " + id);
        return 100 + random.nextInt(900);
    }

    private double fetchRate(String id) {
        sleep(50 + random.nextInt(100));
        return 1.1 + random.nextDouble() * 0.3;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}