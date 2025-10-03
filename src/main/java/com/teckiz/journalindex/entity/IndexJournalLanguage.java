package com.teckiz.journalindex.entity;

import jakarta.persistence.*;

/**
 * IndexJournalLanguage entity
 */
@Entity
@Table(name = "IndexJournalLanguage", indexes = {
    @Index(name = "index_journal_language_search_index", columnList = "code_a")
})
public class IndexJournalLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 20)
    private String name;

    @Column(name = "code_a", length = 20)
    private String codeA;

    @Column(name = "code_b", length = 20)
    private String codeB;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_id", referencedColumnName = "id")
    private IndexJournal indexJournal;

    // Constructors
    public IndexJournalLanguage() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodeA() {
        return codeA;
    }

    public void setCodeA(String codeA) {
        this.codeA = codeA;
    }

    public String getCodeB() {
        return codeB;
    }

    public void setCodeB(String codeB) {
        this.codeB = codeB;
    }

    public IndexJournal getIndexJournal() {
        return indexJournal;
    }

    public void setIndexJournal(IndexJournal indexJournal) {
        this.indexJournal = indexJournal;
    }

    @Override
    public String toString() {
        return "IndexJournalLanguage{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", codeA='" + codeA + '\'' +
                ", codeB='" + codeB + '\'' +
                '}';
    }
}