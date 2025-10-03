package com.teckiz.journalindex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;

/**
 * IndexJournalAuthor entity representing journal article authors
 */
@Entity
@Table(name = "IndexJournalAuthor", indexes = {
    @Index(name = "indexed_article_author_name_index", columnList = "name")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IndexJournalAuthor {
    
    private static final Logger logger = LogManager.getLogger(IndexJournalAuthor.class);
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "author_id", length = 255)
    private String authorId;
    
    @Column(name = "name", length = 255)
    private String name;
    
    @Column(name = "affiliation_id", length = 255)
    private String affiliationId;
    
    @Column(name = "affiliation", columnDefinition = "TEXT")
    private String affiliation;
    
    @Column(name = "country", length = 10)
    private String country;
    
    @Column(name = "email", length = 255)
    private String email;
    
    @Column(name = "orcid", length = 255)
    private String orcid;
    
    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_article_id")
    private IndexJournalArticle indexJournalArticle;
    
    // Constructors
    public IndexJournalAuthor() {
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAffiliationId() {
        return affiliationId;
    }
    
    public void setAffiliationId(String affiliationId) {
        this.affiliationId = affiliationId;
    }
    
    public String getAffiliation() {
        return affiliation;
    }
    
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getOrcid() {
        return orcid;
    }
    
    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }
    
    public String getBiography() {
        return biography;
    }
    
    public void setBiography(String biography) {
        this.biography = biography;
    }
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Relationship getters and setters
    public IndexJournalArticle getIndexJournalArticle() {
        return indexJournalArticle;
    }
    
    public void setIndexJournalArticle(IndexJournalArticle indexJournalArticle) {
        this.indexJournalArticle = indexJournalArticle;
    }
    
    // Utility methods
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "IndexJournalAuthor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", affiliation='" + affiliation + '\'' +
                '}';
    }
}
