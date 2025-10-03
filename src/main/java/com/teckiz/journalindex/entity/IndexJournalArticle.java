package com.teckiz.journalindex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * IndexJournalArticle entity representing journal articles
 */
@Entity
@Table(name = "IndexJournalArticle", indexes = {
    @Index(name = "indexed_article_page_index", columnList = "article_key, doi, publisher_record_id"),
    @Index(name = "indexed_article_publish_at_index", columnList = "published_at")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IndexJournalArticle {
    
    private static final Logger logger = LogManager.getLogger(IndexJournalArticle.class);
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "article_key", length = 20, unique = true)
    private String articleKey;
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "doi", length = 50)
    private String doi;
    
    @Column(name = "publisher_record_id", length = 50)
    private String publisherRecordId;
    
    @Column(name = "article_type")
    private String articleType;
    
    @Column(name = "pages", length = 100)
    private String pages;
    
    @Column(name = "page_url", length = 255)
    private String pageUrl;
    
    @Column(name = "title_text", columnDefinition = "TEXT")
    private String title;
    
    @Column(name = "keywords_text", columnDefinition = "TEXT")
    private String keywords;
    
    @Column(name = "abstract_text", columnDefinition = "TEXT")
    private String abstractText;
    
    @Column(name = "references_text", columnDefinition = "TEXT")
    private String references;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_volume_id")
    private IndexJournalVolume indexJournalVolume;
    
    @OneToMany(mappedBy = "indexJournalArticle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexJournalAuthor> authors = new ArrayList<>();
    
    // Constructors
    public IndexJournalArticle() {
        this.articleKey = generateEntityKey();
        this.receivedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getArticleKey() {
        return articleKey;
    }
    
    public void setArticleKey(String articleKey) {
        this.articleKey = articleKey;
    }
    
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
    
    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
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
    
    public String getDoi() {
        return doi;
    }
    
    public void setDoi(String doi) {
        this.doi = doi;
    }
    
    public String getPublisherRecordId() {
        return publisherRecordId;
    }
    
    public void setPublisherRecordId(String publisherRecordId) {
        this.publisherRecordId = publisherRecordId;
    }
    
    public String getArticleType() {
        return articleType;
    }
    
    public void setArticleType(String articleType) {
        this.articleType = articleType;
    }
    
    public String getPages() {
        return pages;
    }
    
    public void setPages(String pages) {
        this.pages = pages;
    }
    
    public String getPageUrl() {
        return pageUrl;
    }
    
    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getKeywords() {
        return keywords;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    public String getAbstractText() {
        return abstractText;
    }
    
    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }
    
    public String getReferences() {
        return references;
    }
    
    public void setReferences(String references) {
        this.references = references;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Relationship getters and setters
    public Company getCompany() {
        return company;
    }
    
    public void setCompany(Company company) {
        this.company = company;
    }
    
    public IndexJournalVolume getIndexJournalVolume() {
        return indexJournalVolume;
    }
    
    public void setIndexJournalVolume(IndexJournalVolume indexJournalVolume) {
        this.indexJournalVolume = indexJournalVolume;
    }
    
    public List<IndexJournalAuthor> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<IndexJournalAuthor> authors) {
        this.authors = authors;
    }
    
    // Utility methods
    private String generateEntityKey() {
        return "ART_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "IndexJournalArticle{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", doi='" + doi + '\'' +
                ", publishedAt=" + publishedAt +
                '}';
    }
}
