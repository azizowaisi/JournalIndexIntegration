package com.teckiz.journalindex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * IndexJournalLanguage entity representing languages supported by journals
 */
@Entity
@Table(name = "indexJournalLanguage", indexes = {
    @Index(name = "idx_language_code", columnList = "code_a")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IndexJournalLanguage {
    
    private static final Logger logger = LogManager.getLogger(IndexJournalLanguage.class);
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name", length = 20)
    private String name;
    
    @Column(name = "code_a", length = 20)
    private String codeA;
    
    @Column(name = "code_b", length = 20)
    private String codeB;
    
    @Column(name = "is_primary")
    private Boolean primary = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_id")
    private IndexJournal indexJournal;
    
    // Constructors
    public IndexJournalLanguage() {
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
    
    public Boolean getPrimary() {
        return primary;
    }
    
    public void setPrimary(Boolean primary) {
        this.primary = primary;
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
    public IndexJournal getIndexJournal() {
        return indexJournal;
    }
    
    public void setIndexJournal(IndexJournal indexJournal) {
        this.indexJournal = indexJournal;
    }
    
    // Utility methods
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "IndexJournalLanguage{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", codeA='" + codeA + '\'' +
                ", codeB='" + codeB + '\'' +
                '}';
    }
}
