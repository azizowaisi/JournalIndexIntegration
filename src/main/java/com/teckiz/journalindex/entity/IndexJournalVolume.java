package com.teckiz.journalindex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * IndexJournalVolume entity
 */
@Entity
@Table(name = "IndexJournalVolume", indexes = {
    @Index(name = "indexed_journal_volume_key_index", columnList = "volume_key"),
    @Index(name = "indexed_journal_volume_publish_date_index", columnList = "published_at")
})
public class IndexJournalVolume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "volume_key", length = 50)
    private String volumeKey;

    @Column(name = "publisher_record_id", length = 50)
    private String publisherRecordId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "vol_number", length = 255)
    private String volumeNumber;

    @Column(name = "issue_number", length = 255)
    private String issueNumber;

    @Column(name = "page_url", length = 255)
    private String pageURL;

    @Column(name = "cover_image_url", length = 255)
    private String coverImageURL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_id", referencedColumnName = "id")
    private IndexJournal indexJournal;

    // Constructors
    public IndexJournalVolume() {
        this.volumeKey = generateEntityKey();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Utility method to generate entity key
    private String generateEntityKey() {
        return "VOL_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
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

    public String getPageURL() {
        return pageURL;
    }

    public void setPageURL(String pageURL) {
        this.pageURL = pageURL;
    }

    public String getCoverImageURL() {
        return coverImageURL;
    }

    public void setCoverImageURL(String coverImageURL) {
        this.coverImageURL = coverImageURL;
    }

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

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "IndexJournalVolume{" +
                "id=" + id +
                ", volumeKey='" + volumeKey + '\'' +
                ", volumeNumber='" + volumeNumber + '\'' +
                ", issueNumber='" + issueNumber + '\'' +
                ", publishedAt=" + publishedAt +
                '}';
    }
}