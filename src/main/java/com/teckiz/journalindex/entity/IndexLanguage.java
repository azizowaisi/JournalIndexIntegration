package com.teckiz.journalindex.entity;

import jakarta.persistence.*;

/**
 * IndexLanguage entity - matches Symfony AppBundle\Entity\IndexLanguage
 */
@Entity
@Table(name = "IndexLanguage")
public class IndexLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "language_key", length = 50, nullable = false)
    private String languageKey;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "code_a", length = 20)
    private String codeA;

    @Column(name = "code_b", length = 20)
    private String codeB;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    // Constructors
    public IndexLanguage() {
        this.languageKey = generateEntityKey();
    }

    // Utility method to generate entity key
    private String generateEntityKey() {
        return "LANG_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLanguageKey() {
        return languageKey;
    }

    public void setLanguageKey(String languageKey) {
        this.languageKey = languageKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodeA() {
        return codeA;
    }

    public void setCodeA(String codeA) {
        this.codeA = codeA;
    }

    public String getCodeB() {
        return codeB;
    }

    public void setCodeB(String codeB) {
        this.codeB = codeB;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "IndexLanguage{" +
                "id=" + id +
                ", languageKey='" + languageKey + '\'' +
                ", name='" + name + '\'' +
                ", codeA='" + codeA + '\'' +
                ", codeB='" + codeB + '\'' +
                '}';
    }
}