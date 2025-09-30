package com.teckiz.journalindex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * IndexJournalVolume entity representing journal volumes
 */
@Entity
@Table(name = "indexJournalVolume", indexes = {
    @Index(name = "idx_volume_key", columnList = "volume_key"),
    @Index(name = "idx_volume_publish_date", columnList = "published_at")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IndexJournalVolume {
    
    private static final Logger logger = LogManager.getLogger(IndexJournalVolume.class);
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "volume_key", length = 50, unique = true)
    private String volumeKey;
    
    @Column(name = "publisher_record_id", length = 50)
    private String publisherRecordId;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "vol_number", length = 255)
    private String volumeNumber;
    
    @Column(name = "issue_number", length = 255)
    private String issueNumber;
    
    @Column(name = "page_url", length = 255)
    private String pageUrl;
    
    @Column(name = "cover_image_url", length = 255)
    private String coverImageUrl;
    
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
    
    @OneToMany(mappedBy = "indexJournalVolume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexJournalArticle> articles = new ArrayList<>();
    
    // Constructors
    public IndexJournalVolume() {
        this.volumeKey = generateEntityKey();
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
    
    public String getVolumeKey() {
        return volumeKey;
    }
    
    public void setVolumeKey(String volumeKey) {
        this.volumeKey = volumeKey;
    }
    
    public String getPublisherRecordId() {
        return publisherRecordId;
    }
    
    public void setPublisherRecordId(String publisherRecordId) {
        this.publisherRecordId = publisherRecordId;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public String getVolumeNumber() {
        return volumeNumber;
    }
    
    public void setVolumeNumber(String volumeNumber) {
        this.volumeNumber = volumeNumber;
    }
    
    public String getIssueNumber() {
        return issueNumber;
    }
    
    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }
    
    public String getPageUrl() {
        return pageUrl;
    }
    
    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
    
    public String getCoverImageUrl() {
        return coverImageUrl;
    }
    
    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
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
    
    public List<IndexJournalArticle> getArticles() {
        return articles;
    }
    
    public void setArticles(List<IndexJournalArticle> articles) {
        this.articles = articles;
    }
    
    // Utility methods
    private String generateEntityKey() {
        return "VOL_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "IndexJournalVolume{" +
                "id=" + id +
                ", volumeNumber='" + volumeNumber + '\'' +
                ", issueNumber='" + issueNumber + '\'' +
                ", publishedAt=" + publishedAt +
                '}';
    }
}
