package com.teckiz.journalindex.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for article data extracted from OAI XML
 * Mirrors the PHP ArticleModel
 */
public class ArticleModel {
    
    private String status = "update"; // update or delete
    private String publisherRecordId;
    private String title;
    private String abstractText;
    private String keywords;
    private String pageUrl;
    private List<ArticleAuthorModel> authors = new ArrayList<>();
    private String volumeNumber;
    private LocalDateTime publishedAt;
    private String pages;
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPublisherRecordId() {
        return publisherRecordId;
    }
    
    public void setPublisherRecordId(String publisherRecordId) {
        this.publisherRecordId = publisherRecordId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAbstractText() {
        return abstractText;
    }
    
    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }
    
    public String getKeywords() {
        return keywords;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    public String getPageUrl() {
        return pageUrl;
    }
    
    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
    
    public List<ArticleAuthorModel> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<ArticleAuthorModel> authors) {
        this.authors = authors;
    }
    
    public String getVolumeNumber() {
        return volumeNumber;
    }
    
    public void setVolumeNumber(String volumeNumber) {
        this.volumeNumber = volumeNumber;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public String getPages() {
        return pages;
    }
    
    public void setPages(String pages) {
        this.pages = pages;
    }
    
    @Override
    public String toString() {
        return "ArticleModel{" +
                "status='" + status + '\'' +
                ", publisherRecordId='" + publisherRecordId + '\'' +
                ", title='" + title + '\'' +
                ", volumeNumber='" + volumeNumber + '\'' +
                ", authors=" + authors.size() +
                ", publishedAt=" + publishedAt +
                '}';
    }
}

