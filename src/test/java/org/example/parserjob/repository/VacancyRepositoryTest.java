package org.example.parserjob.repository;

import org.example.parserjob.model.Vacancy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VacancyRepositoryTest {

    @Autowired
    private VacancyRepository repository;

    @Test
    void shouldSaveAndFindVacancy() {
        Vacancy v = new Vacancy("ext-1", "Java Dev", "Company", "Москва", "100k", "Spring", "2026");
        repository.save(v);

        assertNotNull(v.getId());
        assertEquals(1, repository.count());
    }

    @Test
    void shouldCheckIfExistsByExternalId() {
        repository.save(new Vacancy("ext-2", "Go Dev", "Company", "Питер", "", "", ""));

        assertTrue(repository.existsByExternalId("ext-2"));
        assertFalse(repository.existsByExternalId("ext-999"));
    }
}