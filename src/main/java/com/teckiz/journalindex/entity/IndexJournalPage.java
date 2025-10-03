package com.teckiz.journalindex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * IndexJournalPage entity representing journal pages
 */
@Entity
@Table(name = "IndexJournalPage")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IndexJournalPage {
    
    private static final Logger logger = LogManager.getLogger(IndexJournalPage.class);
    
    // Page type constants
    public static final String PAGE_EDITORIAL_BOARD = "Editorial-board";
    public static final String PAGE_ADVISORY_BOARD = "Advisory-board";
    public static final String PAGE_AUTHOR_GUIDELINE = "Author-guideline";
    public static final String PAGE_REVIEW_PROCESS = "Review-process";
    public static final String PAGE_AIMS_AND_SCOPE = "Aims-and-scope";
    public static final String PAGE_PLAGIARISM_POLICY = "Plagiarism-policy";
    public static final String PAGE_OPEN_JOURNAL_ACCESS = "open-journal-access";
    public static final String PAGE_CALL_FOR_PAPER = "call-for-paper";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "type", length = 20)
    private String type;
    
    @Column(name = "url", length = 255)
    private String url;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_id")
    private IndexJournal indexJournal;
    
    // Constructors
    public IndexJournalPage() {
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
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
        return "IndexJournalPage{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
