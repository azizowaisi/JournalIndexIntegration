package com.teckiz.journalindex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * IndexJournalSubject entity representing journal subjects/categories
 */
@Entity
@Table(name = "indexJournalSubject", indexes = {
    @Index(name = "idx_subject_key", columnList = "subject_key, name")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IndexJournalSubject {
    
    private static final Logger logger = LogManager.getLogger(IndexJournalSubject.class);
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "subject_key", length = 20, unique = true)
    private String subjectKey;
    
    @Column(name = "name", length = 255)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_active")
    private Boolean active = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexJournal> journals = new ArrayList<>();
    
    // Constructors
    public IndexJournalSubject() {
        this.subjectKey = generateEntityKey();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Relationship getters and setters
    public Company getCompany() {
        return company;
    }
    
    public void setCompany(Company company) {
        this.company = company;
    }
    
    public List<IndexJournal> getJournals() {
        return journals;
    }
    
    public void setJournals(List<IndexJournal> journals) {
        this.journals = journals;
    }
    
    // Utility methods
    private String generateEntityKey() {
        return "SUBJ_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "IndexJournalSubject{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", subjectKey='" + subjectKey + '\'' +
                '}';
    }
}
