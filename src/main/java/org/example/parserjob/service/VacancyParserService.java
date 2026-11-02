package org.example.parserjob.service;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.ObjectMapper;
import org.example.parserjob.model.Vacancy;
import org.example.parserjob.repository.VacancyRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class VacancyParserService {

    private static final String BASE = "https://career.habr.com/api/frontend/vacancies";

    private final VacancyRepository repository;
    private final ExecutorService executor;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public VacancyParserService(VacancyRepository repository,
                                @Qualifier("asyncExecutor") ExecutorService executor) {
        this.repository = repository;
        this.executor = executor;
    }

    public int parsePages(int pages) {
        List<CompletableFuture<List<Vacancy>>> futures = new ArrayList<>();
        for (int p = 1; p <= pages; p++) {
            final int page = p;
            futures.add(CompletableFuture.supplyAsync(() -> parsePage(page), executor));
        }

        List<Vacancy> toSave = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .filter(v -> !repository.existsByExternalId(v.getExternalId()))
                .collect(Collectors.toList());

        repository.saveAll(toSave);
        return toSave.size();
    }

    private List<Vacancy> parsePage(int page) {
        try {
            String url = BASE + "?page=" + page + "&per_page=25";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return List.of();

            HabrResponse r = mapper.readValue(resp.body(), HabrResponse.class);
            if (r == null || r.list == null) return List.of();

            return r.list.stream()
                    .map(this::toVacancy)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("[parser] ошибка страницы " + page + ": " + e.getMessage());
            return List.of();
        }
    }

    private Vacancy toVacancy(HabrResponse.HabrVacancy h) {
        if (h.id == null || h.title == null) return null;

        String company = h.company != null ? h.company.title : "";
        String city = h.remoteWork ? "Remote"
                : (h.locations != null && !h.locations.isEmpty() ? h.locations.get(0).title : "");
        String salary = h.salary != null && h.salary.formatted != null && !h.salary.formatted.isEmpty()
                ? h.salary.formatted
                : (h.predictedSalary != null ? h.predictedSalary.formatted : "");
        String req = h.skills == null ? ""
                : h.skills.stream().map(s -> s.title).collect(Collectors.joining(", "));
        String date = h.publishedDate != null ? h.publishedDate.date : "";

        return new Vacancy(String.valueOf(h.id), h.title, company, city, salary, req, date);
    }

    // === POJO для Habr Career ===

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HabrResponse {
        public List<HabrVacancy> list;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class HabrVacancy {
            public Long id;
            public String title;
            public Company company;
            public Salary salary;
            public Salary predictedSalary;
            public List<Skill> skills;
            public List<Location> locations;
            public boolean remoteWork;
            public PublishedDate publishedDate;
        }
        @JsonIgnoreProperties(ignoreUnknown = true) public static class Company { public String title; }
        @JsonIgnoreProperties(ignoreUnknown = true) public static class Salary { public String formatted; }
        @JsonIgnoreProperties(ignoreUnknown = true) public static class Skill { public String title; }
        @JsonIgnoreProperties(ignoreUnknown = true) public static class Location { public String title; }
        @JsonIgnoreProperties(ignoreUnknown = true) public static class PublishedDate { public String date; }
    }
}