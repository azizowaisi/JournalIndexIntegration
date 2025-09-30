package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.IndexJournalAuthor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IndexJournalAuthor entity
 */
@Repository
public interface IndexJournalAuthorRepository extends JpaRepository<IndexJournalAuthor, Long> {
    
    /**
     * Find authors by article
     */
    List<IndexJournalAuthor> findByIndexJournalArticleId(Long articleId);
    
    /**
     * Find author by name
     */
    Optional<IndexJournalAuthor> findByName(String name);
    
    /**
     * Find author by email
     */
    Optional<IndexJournalAuthor> findByEmail(String email);
    
    /**
     * Find author by ORCID
     */
    Optional<IndexJournalAuthor> findByOrcid(String orcid);
    
    /**
     * Find authors by country
     */
    List<IndexJournalAuthor> findByCountry(String country);
    
    /**
     * Find authors by affiliation
     */
    @Query("SELECT a FROM IndexJournalAuthor a WHERE a.affiliation LIKE %:affiliation%")
    List<IndexJournalAuthor> findByAffiliationContaining(@Param("affiliation") String affiliation);
    
    /**
     * Search authors by name containing
     */
    @Query("SELECT a FROM IndexJournalAuthor a WHERE a.name LIKE %:name%")
    List<IndexJournalAuthor> findByNameContaining(@Param("name") String name);
    
    /**
     * Find authors by article and country
     */
    List<IndexJournalAuthor> findByIndexJournalArticleIdAndCountry(Long articleId, String country);
    
    /**
     * Count authors by article
     */
    long countByIndexJournalArticleId(Long articleId);
    
    /**
     * Count authors by country
     */
    long countByCountry(String country);
    
    /**
     * Check if author exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if author exists by ORCID
     */
    boolean existsByOrcid(String orcid);
}
