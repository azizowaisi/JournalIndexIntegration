package com.teckiz.journalindex.entity;

import jakarta.persistence.*;

/**
 * IndexCountry entity
 */
@Entity
@Table(name = "IndexCountry")
public class IndexCountry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "country_key", length = 255, nullable = false)
    private String countryKey;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "code", length = 20)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    // Constructors
    public IndexCountry() {
        this.countryKey = generateEntityKey();
    }

    // Utility method to generate entity key
    private String generateEntityKey() {
        return "COUNTRY_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountryKey() {
        return countryKey;
    }

    public void setCountryKey(String countryKey) {
        this.countryKey = countryKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "IndexCountry{" +
                "id=" + id +
                ", countryKey='" + countryKey + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}