package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.IndexJournalPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IndexJournalPage entity
 */
@Repository
public interface IndexJournalPageRepository extends JpaRepository<IndexJournalPage, Long> {
    
    /**
     * Find pages by company
     */
    List<IndexJournalPage> findByCompanyId(Long companyId);
    
    /**
     * Find pages by journal
     */
    List<IndexJournalPage> findByIndexJournalId(Long journalId);
    
    /**
     * Find pages by type
     */
    List<IndexJournalPage> findByType(String type);
    
    /**
     * Find page by URL
     */
    Optional<IndexJournalPage> findByUrl(String url);
    
    /**
     * Find pages by company and journal
     */
    List<IndexJournalPage> findByCompanyIdAndIndexJournalId(Long companyId, Long journalId);
    
    /**
     * Find pages by company and type
     */
    List<IndexJournalPage> findByCompanyIdAndType(Long companyId, String type);
    
    /**
     * Find pages by journal and type
     */
    List<IndexJournalPage> findByIndexJournalIdAndType(Long journalId, String type);
    
    /**
     * Count pages by company
     */
    long countByCompanyId(Long companyId);
    
    /**
     * Count pages by journal
     */
    long countByIndexJournalId(Long journalId);
    
    /**
     * Count pages by type
     */
    long countByType(String type);
    
    /**
     * Check if page exists by URL
     */
    boolean existsByUrl(String url);
}
