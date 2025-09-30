package com.teckiz.journalindex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * IndexImportQueue entity representing import queue items
 */
@Entity
@Table(name = "index_import_queues")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IndexImportQueue {
    
    private static final Logger logger = LogManager.getLogger(IndexImportQueue.class);
    
    // System type constants
    public static final String OJS_OAI_IDENTIFY = "ojs-identify";
    public static final String OJS_OAI_RECORD_LIST = "ojs-record-list";
    public static final String DOAJ_TYPE_XML = "doaj";
    public static final String OJS_XML = "ojs";
    public static final String TECKIZ = "teckiz";
    
    // Format constants
    public static final String XML_FORMAT = "xml";
    public static final String JSON_FORMAT = "json";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "queue_key", length = 20, unique = true)
    private String queueKey;
    
    @Column(name = "data", columnDefinition = "TEXT")
    private String data;
    
    @Column(name = "format", length = 50)
    private String format = XML_FORMAT;
    
    @Column(name = "total_records", length = 10)
    private String totalRecords;
    
    @Column(name = "indexed_records", length = 10)
    private String indexedRecords = "0";
    
    @Column(name = "is_indexed")
    private Boolean indexed = false;
    
    @Column(name = "is_error")
    private Boolean error = false;
    
    @Column(name = "message", length = 255)
    private String message;
    
    @Column(name = "system_type", length = 50)
    private String systemType;
    
    @Column(name = "company_key", length = 50)
    private String companyKey;
    
    @Column(name = "journal_key", length = 50)
    private String journalKey;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public IndexImportQueue() {
        this.queueKey = generateEntityKey();
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
    
    public String getQueueKey() {
        return queueKey;
    }
    
    public void setQueueKey(String queueKey) {
        this.queueKey = queueKey;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(String totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public String getIndexedRecords() {
        return indexedRecords;
    }
    
    public void setIndexedRecords(String indexedRecords) {
        this.indexedRecords = indexedRecords;
    }
    
    public Boolean getIndexed() {
        return indexed;
    }
    
    public void setIndexed(Boolean indexed) {
        this.indexed = indexed;
    }
    
    public Boolean getError() {
        return error;
    }
    
    public void setError(Boolean error) {
        this.error = error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSystemType() {
        return systemType;
    }
    
    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }
    
    public String getCompanyKey() {
        return companyKey;
    }
    
    public void setCompanyKey(String companyKey) {
        this.companyKey = companyKey;
    }
    
    public String getJournalKey() {
        return journalKey;
    }
    
    public void setJournalKey(String journalKey) {
        this.journalKey = journalKey;
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
    
    // Utility methods
    private String generateEntityKey() {
        return "QUEUE_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "IndexImportQueue{" +
                "id=" + id +
                ", queueKey='" + queueKey + '\'' +
                ", systemType='" + systemType + '\'' +
                ", format='" + format + '\'' +
                ", indexed=" + indexed +
                '}';
    }
}
