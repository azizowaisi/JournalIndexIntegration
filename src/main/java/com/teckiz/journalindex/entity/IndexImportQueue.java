package com.teckiz.journalindex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * IndexImportQueue entity - matches Symfony AppBundle\Entity\IndexImportQueue
 */
@Entity
@Table(name = "IndexImportQueue")
public class IndexImportQueue {
    
    public static final String OJS_OAI_IDENTIFY = "OJS_OAI_IDENTIFY";
    public static final String OJS_OAI_RECORD_LIST = "OJS_OAI_RECORD_LIST";
    public static final String DOAJ = "DOAJ";
    public static final String TECKIZ = "TECKIZ";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "index_journal_id")
    private Long indexJournalId;
    
    @Column(name = "system_type")
    private String systemType;
    
    @Column(name = "data", columnDefinition = "LONGTEXT")
    private String data;
    
    @Column(name = "status")
    private String status = "pending";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    // Constructors
    public IndexImportQueue() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public IndexImportQueue(Long indexJournalId, String systemType, String data) {
        this();
        this.indexJournalId = indexJournalId;
        this.systemType = systemType;
        this.data = data;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getIndexJournalId() {
        return indexJournalId;
    }
    
    public void setIndexJournalId(Long indexJournalId) {
        this.indexJournalId = indexJournalId;
    }
    
    public String getSystemType() {
        return systemType;
    }
    
    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
}