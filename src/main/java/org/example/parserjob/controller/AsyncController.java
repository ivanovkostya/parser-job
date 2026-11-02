package org.example.parserjob.controller;

import org.example.parserjob.service.DataFetchService;
import org.example.parserjob.task.async.FinalResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AsyncController {

    private final DataFetchService service;

    public AsyncController(DataFetchService service) {
        this.service = service;
    }

    private static final List<String> DEFAULT_IDS =
            List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

    @GetMapping("/async/fetch")
    public Map<String, Object> fetchAsync(@RequestParam(required = false) List<String> ids) {
        List<String> targets = (ids == null || ids.isEmpty()) ? DEFAULT_IDS : ids;
        long start = System.nanoTime();
        List<FinalResult> results = service.fetchAllDataAsync(targets);
        long time = (System.nanoTime() - start) / 1_000_000;
        return buildResponse("async", results, time);
    }

    @GetMapping("/sync/fetch")
    public Map<String, Object> fetchSync(@RequestParam(required = false) List<String> ids) {
        List<String> targets = (ids == null || ids.isEmpty()) ? DEFAULT_IDS : ids;
        long start = System.nanoTime();
        List<FinalResult> results = service.fetchAllDataSync(targets);
        long time = (System.nanoTime() - start) / 1_000_000;
        return buildResponse("sync", results, time);
    }

    private Map<String, Object> buildResponse(String type, List<FinalResult> results, long time) {
        long ok = results.stream().filter(FinalResult::isOk).count();
        long fail = results.size() - ok;

        List<Map<String, Object>> details = results.stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", r.getId());
                    if (r.isOk()) {
                        m.put("name", r.getName());
                        m.put("finalPrice", Math.round(r.getFinalPrice()));
                    } else {
                        m.put("error", r.getError());
                    }
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", type);
        response.put("total", results.size());
        response.put("ok", ok);
        response.put("failed", fail);
        response.put("timeMs", time);
        response.put("results", details);
        return response;
    }
}