package org.example.parserjob.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ParserScheduler {

    private final VacancyParserService parser;

    @Value("${parser.pages:3}")
    private int pages;

    public ParserScheduler(VacancyParserService parser) {
        this.parser = parser;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 5000)
    public void parse() {
        int saved = parser.parsePages(pages);
        System.out.println("[scheduler] сохранено новых вакансий: " + saved);
    }
}