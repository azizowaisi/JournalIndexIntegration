package com.teckiz.journalindex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * IndexJournalArticle entity
 */
@Entity
@Table(name = "IndexJournalArticle", indexes = {
    @Index(name = "indexed_article_page_index", columnList = "article_key, doi, publisher_record_id"),
    @Index(name = "indexed_article_publish_at_index", columnList = "published_at")
})
@Cacheable
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "my_entity_cache")
public class IndexJournalArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "article_key", length = 20)
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
    private String pageURL;

    @Column(name = "title_text", columnDefinition = "TEXT")
    private String title;

    @Column(name = "keywords_text", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "abstract_text", columnDefinition = "TEXT")
    private String abstractText;

    @Column(name = "references_text", columnDefinition = "TEXT")
    private String references;

    @OneToMany(mappedBy = "indexJournalArticle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexJournalAuthor> authors = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_volume_id", referencedColumnName = "id")
    private IndexJournalVolume indexJournalVolume;

    // Constructors
    public IndexJournalArticle() {
        this.articleKey = generateEntityKey();
        this.receivedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Utility method to generate entity key
    private String generateEntityKey() {
        return "ART_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
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

    public String getPageURL() {
        return pageURL;
    }

    public void setPageURL(String pageURL) {
        this.pageURL = pageURL;
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

    public List<IndexJournalAuthor> getAuthors() {
        return authors;
    }

    public void setAuthors(List<IndexJournalAuthor> authors) {
        this.authors = authors;
    }

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

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "IndexJournalArticle{" +
                "id=" + id +
                ", articleKey='" + articleKey + '\'' +
                ", title='" + (title != null ? title.substring(0, Math.min(50, title.length())) + "..." : null) + '\'' +
                ", publishedAt=" + publishedAt +
                ", doi='" + doi + '\'' +
                '}';
    }
}