package org.example.parserjob.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vacancies",
        uniqueConstraints = @UniqueConstraint(columnNames = "external_id"))
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    private String name;
    private String company;
    private String city;
    private String salary;
    private String requirements;
    private String postedDate;

    public Vacancy() {}

    public Vacancy(String externalId, String name, String company, String city,
                   String salary, String requirements, String postedDate) {
        this.externalId = externalId;
        this.name = name;
        this.company = company;
        this.city = city;
        this.salary = salary;
        this.requirements = requirements;
        this.postedDate = postedDate;
    }

    public Long getId() { return id; }
    public String getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getCompany() { return company; }
    public String getCity() { return city; }
    public String getSalary() { return salary; }
    public String getRequirements() { return requirements; }
    public String getPostedDate() { return postedDate; }
}