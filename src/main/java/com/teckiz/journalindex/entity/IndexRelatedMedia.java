package com.teckiz.journalindex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * IndexRelatedMedia entity - matches Symfony AppBundle\Entity\IndexRelatedMedia
 */
@Entity
@Table(name = "IndexRelatedMedia", indexes = {
    @Index(name = "index_related_media_key_index", columnList = "related_media_key, reference_key, related_media_type")
})
public class IndexRelatedMedia {

    // Media type constants - matches teckiz6
    public static final String JOURNAL = "journal";
    public static final String ARTICLE = "article";
    public static final String VOLUME = "volume";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "related_media_key", length = 255)
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    // Constructors
    public IndexRelatedMedia() {
        this.relatedMediaKey = generateEntityKey();
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    // Utility method to generate entity key
    private String generateEntityKey() {
        return "MEDIA_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "IndexRelatedMedia{" +
                "id=" + id +
                ", relatedMediaKey='" + relatedMediaKey + '\'' +
                ", referenceKey='" + referenceKey + '\'' +
                ", relatedMediaType='" + relatedMediaType + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}