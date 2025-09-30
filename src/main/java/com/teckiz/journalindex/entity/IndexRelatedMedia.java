package com.teckiz.journalindex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * IndexRelatedMedia entity representing related media files
 */
@Entity
@Table(name = "index_related_media", indexes = {
    @Index(name = "idx_related_media_key", columnList = "related_media_key, reference_key, related_media_type")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IndexRelatedMedia {
    
    private static final Logger logger = LogManager.getLogger(IndexRelatedMedia.class);
    
    // Media type constants
    public static final String JOURNAL = "journal";
    public static final String ARTICLE = "article";
    public static final String VOLUME = "volume";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "related_media_key", length = 255, unique = true)
    private String relatedMediaKey;
    
    @Column(name = "reference_key", length = 255)
    private String referenceKey;
    
    @Column(name = "mimetype", length = 64)
    private String mimeType;
    
    @Column(name = "media_type", length = 64)
    private String mediaType;
    
    @Column(name = "related_media_type", length = 64)
    private String relatedMediaType;
    
    @Column(name = "location", columnDefinition = "TEXT")
    private String location;
    
    @Column(name = "name", length = 255)
    private String name;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    // Constructors
    public IndexRelatedMedia() {
        this.relatedMediaKey = generateEntityKey();
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRelatedMediaKey() {
        return relatedMediaKey;
    }
    
    public void setRelatedMediaKey(String relatedMediaKey) {
        this.relatedMediaKey = relatedMediaKey;
    }
    
    public String getReferenceKey() {
        return referenceKey;
    }
    
    public void setReferenceKey(String referenceKey) {
        this.referenceKey = referenceKey;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    
    public String getRelatedMediaType() {
        return relatedMediaType;
    }
    
    public void setRelatedMediaType(String relatedMediaType) {
        this.relatedMediaType = relatedMediaType;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }
    
    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
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
        return "MEDIA_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "IndexRelatedMedia{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", relatedMediaType='" + relatedMediaType + '\'' +
                '}';
    }
}
