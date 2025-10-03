package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.IndexImportQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for IndexImportQueue entity
 */
@Repository
public interface IndexImportQueueRepository extends JpaRepository<IndexImportQueue, Long> {
    
    /**
     * Find all queue entries for a specific journal
     */
    List<IndexImportQueue> findByIndexJournalIdOrderByCreatedAtDesc(Long indexJournalId);
    
    /**
     * Find all pending queue entries
     */
    List<IndexImportQueue> findByStatusOrderByCreatedAtAsc(String status);
    
    /**
     * Find all queue entries by system type
     */
    List<IndexImportQueue> findBySystemTypeOrderByCreatedAtDesc(String systemType);
    
    /**
     * Find all queue entries for a specific journal and system type
     */
    List<IndexImportQueue> findByIndexJournalIdAndSystemTypeOrderByCreatedAtDesc(Long indexJournalId, String systemType);
    
    /**
     * Find all queue entries that are not indexed and have no errors
     */
    List<IndexImportQueue> findByIndexedFalseAndErrorFalseOrderByCreatedAtAsc();
    
    /**
     * Count queue entries that are not indexed and have no errors
     */
    long countByIndexedFalseAndErrorFalse();
    
    /**
     * Count queue entries that are indexed and have no errors
     */
    long countByIndexedTrueAndErrorFalse();
    
    /**
     * Count queue entries that have errors
     */
    long countByErrorTrue();
    
    /**
     * Find all queue entries that have errors
     */
    List<IndexImportQueue> findByErrorTrue();
}