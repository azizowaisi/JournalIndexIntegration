package com.teckiz.journalindex.entity;

import jakarta.persistence.*;

/**
 * IndexJournalAuthor entity
 */
@Entity
@Table(name = "IndexJournalAuthor", indexes = {
    @Index(name = "indexed_article_author_name_index", columnList = "name")
})
public class IndexJournalAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "author_id", length = 255)
    private String authorId;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "affiliation_id", length = 255)
    private String affiliationId;

    @Column(name = "affiliation", columnDefinition = "TEXT")
    private String affiliation;

    @Column(name = "country", length = 10)
    private String country;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "orcid", length = 255)
    private String orcid;

    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_article_id", referencedColumnName = "id")
    private IndexJournalArticle indexJournalArticle;

    // Constructors
    public IndexJournalAuthor() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAffiliationId() {
        return affiliationId;
    }

    public void setAffiliationId(String affiliationId) {
        this.affiliationId = affiliationId;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
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

    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public IndexJournalArticle getIndexJournalArticle() {
        return indexJournalArticle;
    }

    public void setIndexJournalArticle(IndexJournalArticle indexJournalArticle) {
        this.indexJournalArticle = indexJournalArticle;
    }

    @Override
    public String toString() {
        return "IndexJournalAuthor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", affiliation='" + (affiliation != null ? affiliation.substring(0, Math.min(50, affiliation.length())) + "..." : null) + '\'' +
                '}';
    }
}