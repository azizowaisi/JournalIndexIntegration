package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.IndexJournalArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IndexJournalArticle entity
 */
@Repository
public interface IndexJournalArticleRepository extends JpaRepository<IndexJournalArticle, Long> {
    
    /**
     * Find article by article key
     */
    Optional<IndexJournalArticle> findByArticleKey(String articleKey);
    
    /**
     * Find articles by company
     */
    List<IndexJournalArticle> findByCompanyId(Long companyId);
    
    /**
     * Find articles by volume
     */
    List<IndexJournalArticle> findByIndexJournalVolumeId(Long volumeId);
    
    /**
     * Find articles by DOI
     */
    Optional<IndexJournalArticle> findByDoi(String doi);
    
    /**
     * Find articles by publisher record ID
     */
    Optional<IndexJournalArticle> findByPublisherRecordId(String publisherRecordId);
    
    /**
     * Find articles by article type
     */
    List<IndexJournalArticle> findByArticleType(String articleType);
    
    /**
     * Find articles published after date
     */
    List<IndexJournalArticle> findByPublishedAtAfter(LocalDateTime publishedAt);
    
    /**
     * Find articles published between dates
     */
    List<IndexJournalArticle> findByPublishedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Search articles by title containing
     */
    @Query("SELECT a FROM IndexJournalArticle a WHERE a.title LIKE %:title%")
    List<IndexJournalArticle> findByTitleContaining(@Param("title") String title);
    
    /**
     * Search articles by keywords containing
     */
    @Query("SELECT a FROM IndexJournalArticle a WHERE a.keywords LIKE %:keywords%")
    List<IndexJournalArticle> findByKeywordsContaining(@Param("keywords") String keywords);
    
    /**
     * Find articles by company and volume
     */
    List<IndexJournalArticle> findByCompanyIdAndIndexJournalVolumeId(Long companyId, Long volumeId);
    
    /**
     * Find articles by company and article type
     */
    List<IndexJournalArticle> findByCompanyIdAndArticleType(Long companyId, String articleType);
    
    /**
     * Count articles by company
     */
    long countByCompanyId(Long companyId);
    
    /**
     * Count articles by volume
     */
    long countByIndexJournalVolumeId(Long volumeId);
    
    /**
     * Count articles by article type
     */
    long countByArticleType(String articleType);
    
    /**
     * Check if article exists by article key
     */
    boolean existsByArticleKey(String articleKey);
    
    /**
     * Check if article exists by DOI
     */
    boolean existsByDoi(String doi);
    
    /**
     * Check if article exists by publisher record ID
     */
    boolean existsByPublisherRecordId(String publisherRecordId);
    
    /**
     * Find article by page URL
     */
    Optional<IndexJournalArticle> findByPageURL(String pageURL);
}
