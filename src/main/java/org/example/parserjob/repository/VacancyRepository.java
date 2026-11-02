package org.example.parserjob.repository;

import org.example.parserjob.model.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    boolean existsByExternalId(String externalId);
}