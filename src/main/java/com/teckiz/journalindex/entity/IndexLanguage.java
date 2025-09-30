package com.teckiz.journalindex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * IndexLanguage entity representing languages in the indexing system
 */
@Entity
@Table(name = "indexLanguage")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IndexLanguage {
    
    private static final Logger logger = LogManager.getLogger(IndexLanguage.class);
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "language_key", length = 50, nullable = false, unique = true)
    private String languageKey;
    
    @Column(name = "name", length = 255)
    private String name;
    
    @Column(name = "code_a", length = 20)
    private String codeA;
    
    @Column(name = "code_b", length = 20)
    private String codeB;
    
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
    
    // Constructors
    public IndexLanguage() {
        this.languageKey = generateEntityKey();
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
    
    // Utility methods
    private String generateEntityKey() {
        return "LANG_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "IndexLanguage{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", codeA='" + codeA + '\'' +
                ", codeB='" + codeB + '\'' +
                '}';
    }
}
