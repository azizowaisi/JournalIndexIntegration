package com.teckiz.journalindex.entity;

import jakarta.persistence.*;

/**
 * IndexJournalSubject entity - matches Symfony AppBundle\Entity\IndexJournalSubject
 */
@Entity
@Table(name = "IndexJournalSubject", indexes = {
    @Index(name = "index_journal_subject_key_index", columnList = "subject_key, name")
})
public class IndexJournalSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "subject_key", length = 20)
    private String subjectKey;

    @Column(name = "name", length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    // Constructors
    public IndexJournalSubject() {
        this.subjectKey = generateEntityKey();
    }

    // Utility method to generate entity key
    private String generateEntityKey() {
        return "SUBJ_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectKey() {
        return subjectKey;
    }

    public void setSubjectKey(String subjectKey) {
        this.subjectKey = subjectKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "IndexJournalSubject{" +
                "id=" + id +
                ", subjectKey='" + subjectKey + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}