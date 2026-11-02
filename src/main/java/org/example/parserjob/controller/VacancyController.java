package org.example.parserjob.controller;

import org.example.parserjob.model.Vacancy;
import org.example.parserjob.repository.VacancyRepository;
import org.example.parserjob.service.VacancyParserService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class VacancyController {

    private final VacancyParserService parser;
    private final VacancyRepository repository;

    public VacancyController(VacancyParserService parser, VacancyRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    @GetMapping("/parse")
    public Map<String, Object> parse(@RequestParam(defaultValue = "3") int pages) {
        int saved = parser.parsePages(pages);
        return Map.of("pages", pages, "saved", saved);
    }

    @GetMapping("/answer")
    public Map<String, Object> answer(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "postedDate") String sort) {

        Comparator<Vacancy> cmp = switch (sort) {
            case "name" -> Comparator.comparing(Vacancy::getName);
            case "company" -> Comparator.comparing(Vacancy::getCompany);
            case "city" -> Comparator.comparing(Vacancy::getCity);
            default -> Comparator.comparing(Vacancy::getPostedDate).reversed();
        };

        List<Vacancy> filtered = repository.findAll().parallelStream()
                .filter(v -> city == null || city.isBlank() || city.equalsIgnoreCase(v.getCity()))
                .sorted(cmp)
                .collect(Collectors.toList());

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());

        return Map.of(
                "total", filtered.size(),
                "page", page,
                "size", size,
                "content", filtered.subList(from, to)
        );
    }
}