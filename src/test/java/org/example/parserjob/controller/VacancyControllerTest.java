package org.example.parserjob.controller;

import org.example.parserjob.model.Vacancy;
import org.example.parserjob.repository.VacancyRepository;
import org.example.parserjob.service.VacancyParserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VacancyController.class)
class VacancyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VacancyRepository repository;

    @MockitoBean
    private VacancyParserService parserService;

    @Test
    void shouldReturnVacanciesList() throws Exception {
        Vacancy v = new Vacancy("1", "Java Developer", "Company", "Москва", "100k", "Spring", "2026-11-02");
        when(repository.findAll()).thenReturn(List.of(v));

        mockMvc.perform(get("/answer?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Java Developer"))
                .andExpect(jsonPath("$.content[0].company").value("Company"));
    }

    @Test
    void shouldFilterByCity() throws Exception {
        Vacancy v1 = new Vacancy("1", "Java", "A", "Москва", "", "", "");
        Vacancy v2 = new Vacancy("2", "Go", "B", "Питер", "", "", "");

        when(repository.findAll()).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/answer?city=Москва"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Java"));
    }
}