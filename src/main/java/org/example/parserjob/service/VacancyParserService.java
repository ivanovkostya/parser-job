package org.example.parserjob.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.ObjectMapper;
import org.example.parserjob.model.Vacancy;
import org.example.parserjob.repository.VacancyRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class VacancyParserService {

    private static final String BASE = "https://career.habr.com/api/frontend/vacancies";

    private final VacancyRepository repository;
    private final ExecutorService executor;

    // Используем RestTemplate
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private final AtomicBoolean isParsing = new AtomicBoolean(false);

    public VacancyParserService(VacancyRepository repository,
                                @Qualifier("asyncExecutor") ExecutorService executor) {
        this.repository = repository;
        this.executor = executor;
    }

    public int parsePages(int pages) {
        if (!isParsing.compareAndSet(false, true)) {
            System.out.println("[parser] Парсинг уже выполняется, пропускаем запрос.");
            return 0;
        }

        try {
            List<CompletableFuture<List<Vacancy>>> futures = new ArrayList<>();
            for (int p = 1; p <= pages; p++) {
                final int page = p;
                // Обработка исключений
                futures.add(CompletableFuture.supplyAsync(() -> parsePage(page), executor)
                        .exceptionally(ex -> {
                            System.err.println("[parser] Ошибка загрузки страницы " + page + ": " + ex.getMessage());
                            return List.of();
                        }));
            }

            List<Vacancy> allFetched = futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            // Убираем дубликаты внутри текущего запроса
            Map<String, Vacancy> uniqueMap = new LinkedHashMap<>();
            for (Vacancy v : allFetched) {
                uniqueMap.put(v.getExternalId(), v);
            }
            List<Vacancy> toSave = new ArrayList<>(uniqueMap.values());

            int savedCount = 0;
            for (Vacancy v : toSave) {
                try {
                    repository.saveAndFlush(v);
                    savedCount++;
                } catch (DataIntegrityViolationException e) {
                    System.out.println("[parser] Дубликат пропущен: " + v.getExternalId());
                } catch (Exception e) {
                    System.err.println("[parser] Ошибка сохранения " + v.getExternalId() + ": " + e.getMessage());
                }
            }
            return savedCount;
        } finally {
            isParsing.set(false);
        }
    }

    private List<Vacancy> parsePage(int page) {
        try {
            String url = BASE + "?page=" + page + "&per_page=25";

            // Используем RestTemplate
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null) return List.of();

            HabrResponse r = mapper.readValue(jsonResponse, HabrResponse.class);
            if (r == null || r.list == null) return List.of();

            return r.list.stream()
                    .map(this::toVacancy)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("[parser] Исключение при загрузке страницы " + page + ": " + e.getMessage());
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