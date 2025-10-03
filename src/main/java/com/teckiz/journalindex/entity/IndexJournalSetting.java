package com.teckiz.journalindex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * IndexJournalSetting entity representing journal settings
 */
@Entity
@Table(name = "IndexJournalSetting")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IndexJournalSetting {
    
    private static final Logger logger = LogManager.getLogger(IndexJournalSetting.class);
    
    // System constants
    public static final String SYSTEM_OJS = "ojs";
    public static final String SYSTEM_OJS_OAI = "ojs-oai";
    public static final String SYSTEM_TECKIZ = "teckiz";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "integrated_at")
    private LocalDateTime integratedAt;
    
    @Column(name = "journal_system", length = 256)
    private String system;
    
    @Column(name = "api_key", length = 256)
    private String apiKey;
    
    @Column(name = "article_index")
    private Boolean articleIndex = false;
    
    @Column(name = "impact_factor")
    private Boolean impactFactor = false;
    
    @Column(name = "archive_service")
    private Boolean archiveService = false;
    
    @Column(name = "oai_accepted")
    private Boolean oaiAccepted = false;
    
    @Column(name = "oai_scheme", length = 255)
    private String oaiScheme;
    
    @Column(name = "repository_name", length = 255)
    private String repositoryName;
    
    @Column(name = "repository_scheme", length = 255)
    private String repositoryScheme;
    
    @Column(name = "delimiter", length = 255)
    private String delimiter;
    
    @Column(name = "sample_oai_identifier", length = 255)
    private String sampleOaiIdentifier;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", unique = true)
    private IndexJournal journal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    // Constructors
    public IndexJournalSetting() {
        this.integratedAt = LocalDateTime.now().minusYears(1); // Old date equivalent
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
    
    public LocalDateTime getIntegratedAt() {
        return integratedAt;
    }
    
    public void setIntegratedAt(LocalDateTime integratedAt) {
        this.integratedAt = integratedAt;
    }
    
    public String getSystem() {
        return system;
    }
    
    public void setSystem(String system) {
        this.system = system;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public Boolean getArticleIndex() {
        return articleIndex;
    }
    
    public void setArticleIndex(Boolean articleIndex) {
        this.articleIndex = articleIndex;
    }
    
    public Boolean getImpactFactor() {
        return impactFactor;
    }
    
    public void setImpactFactor(Boolean impactFactor) {
        this.impactFactor = impactFactor;
    }
    
    public Boolean getArchiveService() {
        return archiveService;
    }
    
    public void setArchiveService(Boolean archiveService) {
        this.archiveService = archiveService;
    }
    
    public Boolean getOaiAccepted() {
        return oaiAccepted;
    }
    
    public void setOaiAccepted(Boolean oaiAccepted) {
        this.oaiAccepted = oaiAccepted;
    }
    
    public String getOaiScheme() {
        return oaiScheme;
    }
    
    public void setOaiScheme(String oaiScheme) {
        this.oaiScheme = oaiScheme;
    }
    
    public String getRepositoryName() {
        return repositoryName;
    }
    
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
    
    public String getRepositoryScheme() {
        return repositoryScheme;
    }
    
    public void setRepositoryScheme(String repositoryScheme) {
        this.repositoryScheme = repositoryScheme;
    }
    
    public String getDelimiter() {
        return delimiter;
    }
    
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
    
    public String getSampleOaiIdentifier() {
        return sampleOaiIdentifier;
    }
    
    public void setSampleOaiIdentifier(String sampleOaiIdentifier) {
        this.sampleOaiIdentifier = sampleOaiIdentifier;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
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
    public IndexJournal getJournal() {
        return journal;
    }
    
    public void setJournal(IndexJournal journal) {
        this.journal = journal;
    }
    
    public Company getCompany() {
        return company;
    }
    
    public void setCompany(Company company) {
        this.company = company;
    }
    
    // Utility methods
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "IndexJournalSetting{" +
                "id=" + id +
                ", system='" + system + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                '}';
    }
}
