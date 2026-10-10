package org.example.parserjob.task.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpFetcher {

    private final HttpClient client;

    public HttpFetcher() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public HttpResult fetch(String url) {
        long start = System.nanoTime();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long time = (System.nanoTime() - start) / 1_000_000;
            return HttpResult.ok(url, response.statusCode(), time);

        } catch (Exception e) {
            long time = (System.nanoTime() - start) / 1_000_000;
            return HttpResult.fail(url, time, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}