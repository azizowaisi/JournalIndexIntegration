package com.teckiz.journalindex.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Model for SQS message containing article data
 */
public class SqsArticleMessage {
    
    @JsonProperty("journalKey")
    private String journalKey;
    
    @JsonProperty("oaiUrl")
    private String oaiUrl;
    
    @JsonProperty("s3Url")
    private String s3Url;
    
    @JsonProperty("s3Key")
    private String s3Key;
    
    @JsonProperty("s3Path")
    private String s3Path;
    
    @JsonProperty("s3FileName")
    private String s3FileName;
    
    @JsonProperty("messageType")
    private String messageType;
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("pageNumber")
    private Integer pageNumber;
    
    @JsonProperty("batchNumber")
    private Integer batchNumber;
    
    @JsonProperty("totalBatches")
    private Integer totalBatches;
    
    @JsonProperty("articlesInBatch")
    private Integer articlesInBatch;
    
    @JsonProperty("totalArticlesInPage")
    private Integer totalArticlesInPage;
    
    @JsonProperty("totalRecordsProcessed")
    private Integer totalRecordsProcessed;
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("errorCode")
    private String errorCode;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("article")
    private ArticleData article;
    
    @JsonProperty("articles")
    private List<ArticleData> articles;
    
    // Getters and Setters
    public String getJournalKey() { return journalKey; }
    public void setJournalKey(String journalKey) { this.journalKey = journalKey; }
    
    public String getOaiUrl() { return oaiUrl; }
    public void setOaiUrl(String oaiUrl) { this.oaiUrl = oaiUrl; }
    
    public String getS3Url() { return s3Url; }
    public void setS3Url(String s3Url) { this.s3Url = s3Url; }
    
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    
    public String getS3Path() { return s3Path; }
    public void setS3Path(String s3Path) { this.s3Path = s3Path; }
    
    public String getS3FileName() { return s3FileName; }
    public void setS3FileName(String s3FileName) { this.s3FileName = s3FileName; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    
    public Integer getBatchNumber() { return batchNumber; }
    public void setBatchNumber(Integer batchNumber) { this.batchNumber = batchNumber; }
    
    public Integer getTotalBatches() { return totalBatches; }
    public void setTotalBatches(Integer totalBatches) { this.totalBatches = totalBatches; }
    
    public Integer getArticlesInBatch() { return articlesInBatch; }
    public void setArticlesInBatch(Integer articlesInBatch) { this.articlesInBatch = articlesInBatch; }
    
    public Integer getTotalArticlesInPage() { return totalArticlesInPage; }
    public void setTotalArticlesInPage(Integer totalArticlesInPage) { this.totalArticlesInPage = totalArticlesInPage; }
    
    public Integer getTotalRecordsProcessed() { return totalRecordsProcessed; }
    public void setTotalRecordsProcessed(Integer totalRecordsProcessed) { this.totalRecordsProcessed = totalRecordsProcessed; }
    
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public ArticleData getArticle() { return article; }
    public void setArticle(ArticleData article) { this.article = article; }
    
    public List<ArticleData> getArticles() { return articles; }
    public void setArticles(List<ArticleData> articles) { this.articles = articles; }
    
    /**
     * Nested class for article data
     */
    public static class ArticleData {
        
        @JsonProperty("journal_key")
        private String journalKey;
        
        @JsonProperty("created_at")
        private String createdAt;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("title_lang")
        private String titleLang;
        
        @JsonProperty("creator")
        private String creator;
        
        @JsonProperty("subjects")
        private List<String> subjects;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("description_lang")
        private String descriptionLang;
        
        @JsonProperty("publisher")
        private String publisher;
        
        @JsonProperty("publisher_lang")
        private String publisherLang;
        
        @JsonProperty("date")
        private String date;
        
        @JsonProperty("types")
        private List<String> types;
        
        @JsonProperty("format")
        private String format;
        
        @JsonProperty("identifier")
        private String identifier;
        
        @JsonProperty("sources")
        private List<String> sources;
        
        @JsonProperty("language")
        private String language;
        
        @JsonProperty("relation")
        private String relation;
        
        @JsonProperty("datestamp")
        private String datestamp;
        
        @JsonProperty("setSpec")
        private String setSpec;
        
        // Getters and Setters
        public String getJournalKey() { return journalKey; }
        public void setJournalKey(String journalKey) { this.journalKey = journalKey; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getTitleLang() { return titleLang; }
        public void setTitleLang(String titleLang) { this.titleLang = titleLang; }
        
        public String getCreator() { return creator; }
        public void setCreator(String creator) { this.creator = creator; }
        
        public List<String> getSubjects() { return subjects; }
        public void setSubjects(List<String> subjects) { this.subjects = subjects; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getDescriptionLang() { return descriptionLang; }
        public void setDescriptionLang(String descriptionLang) { this.descriptionLang = descriptionLang; }
        
        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        
        public String getPublisherLang() { return publisherLang; }
        public void setPublisherLang(String publisherLang) { this.publisherLang = publisherLang; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public List<String> getTypes() { return types; }
        public void setTypes(List<String> types) { this.types = types; }
        
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        
        public List<String> getSources() { return sources; }
        public void setSources(List<String> sources) { this.sources = sources; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public String getRelation() { return relation; }
        public void setRelation(String relation) { this.relation = relation; }
        
        public String getDatestamp() { return datestamp; }
        public void setDatestamp(String datestamp) { this.datestamp = datestamp; }
        
        public String getSetSpec() { return setSpec; }
        public void setSetSpec(String setSpec) { this.setSpec = setSpec; }
    }
}

