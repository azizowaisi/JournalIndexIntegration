package com.teckiz.journalindex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * IndexJournal entity - matches Symfony AppBundle\Entity\IndexJournal
 */
@Entity
@Table(name = "IndexJournal", indexes = {
    @Index(name = "index_journal_search_index", columnList = "name, keywords, eissn, country"),
    @Index(name = "index_journal_key_index", columnList = "journal_key")
})
public class IndexJournal {
    
    // Status constants - matches Symfony
    public static final String RJ_APPROVED = "approved";
    public static final String RJ_RECEIVED = "received";
    public static final String RJ_PENDING = "pending";
    public static final String RJ_SUSPEND = "suspend";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "journal_key", length = 20, unique = true)
    private String journalKey;
    
    @Column(name = "name", length = 255)
    private String name;
    
    @Column(name = "keywords", length = 255)
    private String keywords;
    
    @Column(name = "eissn", length = 255)
    private String eissn;
    
    @Column(name = "website", length = 255)
    private String website;
    
    @Column(name = "publisher", length = 255)
    private String publisher;
    
    @Column(name = "society", length = 255)
    private String society;
    
    @Column(name = "start_year", length = 10)
    private String startYear;
    
    @Column(name = "description", length = 50)
    private String reviewProcessType;
    
    @Column(name = "submission_date")
    private LocalDateTime submissionDate;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "approved_by", length = 255)
    private String approvedBy;
    
    @Column(name = "status", length = 10)
    private String status = RJ_RECEIVED;
    
    @Column(name = "country", length = 10)
    private String country;
    
    @Column(name = "email", length = 255)
    private String email;
    
    @Column(name = "phone", length = 255)
    private String phone;
    
    @Column(name = "contact_person", length = 255)
    private String contactPerson;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships - matches Symfony exactly
    @OneToOne(mappedBy = "journal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private IndexJournalSetting setting;
    
    @OneToMany(mappedBy = "indexJournal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexJournalLanguage> languages = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private IndexJournalSubject subject;
    
    // Constructors
    public IndexJournal() {
        this.journalKey = generateEntityKey();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Utility method to generate entity key
    private String generateEntityKey() {
        return "JRNL_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getJournalKey() {
        return journalKey;
    }
    
    public void setJournalKey(String journalKey) {
        this.journalKey = journalKey;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getKeywords() {
        return keywords;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    public String getEissn() {
        return eissn;
    }
    
    public void setEissn(String eissn) {
        this.eissn = eissn;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getSociety() {
        return society;
    }
    
    public void setSociety(String society) {
        this.society = society;
    }
    
    public String getStartYear() {
        return startYear;
    }
    
    public void setStartYear(String startYear) {
        this.startYear = startYear;
    }
    
    public String getReviewProcessType() {
        return reviewProcessType;
    }
    
    public void setReviewProcessType(String reviewProcessType) {
        this.reviewProcessType = reviewProcessType;
    }
    
    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }
    
    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }
    
    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }
    
    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getContactPerson() {
        return contactPerson;
    }
    
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
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
    public IndexJournalSetting getSetting() {
        return setting;
    }
    
    public void setSetting(IndexJournalSetting setting) {
        this.setting = setting;
    }
    
    public List<IndexJournalLanguage> getLanguages() {
        return languages;
    }
    
    public void setLanguages(List<IndexJournalLanguage> languages) {
        this.languages = languages;
    }
    
    public Company getCompany() {
        return company;
    }
    
    public void setCompany(Company company) {
        this.company = company;
    }
    
    public IndexJournalSubject getSubject() {
        return subject;
    }
    
    public void setSubject(IndexJournalSubject subject) {
        this.subject = subject;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "IndexJournal{" +
                "id=" + id +
                ", journalKey='" + journalKey + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}