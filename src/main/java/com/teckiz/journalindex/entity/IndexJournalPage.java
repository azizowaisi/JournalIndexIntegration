package com.teckiz.journalindex.entity;

import jakarta.persistence.*;

/**
 * IndexJournalPage entity
 */
@Entity
@Table(name = "IndexJournalPage")
public class IndexJournalPage {

    // Page type constants
    public static final String PAGE_EDITORIAL_BOARD = "Editorial-board";
    public static final String PAGE_ADVISORY_BOARD = "Advisory-board";
    public static final String PAGE_AUTHOR_GUIDELINE = "Author-guideline";
    public static final String PAGE_REVIEW_PROCESS = "Review-process";
    public static final String PAGE_AIMS_AND_SCOPE = "Aims-and-scope";
    public static final String PAGE_PLAGIARISM_POLICY = "Plagiarism-policy";
    public static final String PAGE_OPEN_JOURNAL_ACCESS = "open-journal-access";
    public static final String PAGE_CALL_FOR_PAPER = "call-for-paper";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "type", length = 20)
    private String type;

    @Column(name = "url", length = 255)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_id", referencedColumnName = "id")
    private IndexJournal indexJournal;

    // Constructors
    public IndexJournalPage() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public IndexJournal getIndexJournal() {
        return indexJournal;
    }

    public void setIndexJournal(IndexJournal indexJournal) {
        this.indexJournal = indexJournal;
    }

    @Override
    public String toString() {
        return "IndexJournalPage{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}