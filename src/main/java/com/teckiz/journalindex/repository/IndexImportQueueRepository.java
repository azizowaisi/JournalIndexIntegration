package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.IndexImportQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IndexImportQueue entity
 */
@Repository
public interface IndexImportQueueRepository extends JpaRepository<IndexImportQueue, Long> {
    
    /**
     * Find queue item by queue key
     */
    Optional<IndexImportQueue> findByQueueKey(String queueKey);
    
    /**
     * Find queue items by system type
     */
    List<IndexImportQueue> findBySystemType(String systemType);
    
    /**
     * Find queue items by format
     */
    List<IndexImportQueue> findByFormat(String format);
    
    /**
     * Find indexed queue items
     */
    List<IndexImportQueue> findByIndexedTrue();
    
    /**
     * Find non-indexed queue items
     */
    List<IndexImportQueue> findByIndexedFalse();
    
    /**
     * Find queue items with errors
     */
    List<IndexImportQueue> findByErrorTrue();
    
    /**
     * Find queue items without errors
     */
    List<IndexImportQueue> findByErrorFalse();
    
    /**
     * Find queue items by company key
     */
    List<IndexImportQueue> findByCompanyKey(String companyKey);
    
    /**
     * Find queue items by journal key
     */
    List<IndexImportQueue> findByJournalKey(String journalKey);
    
    /**
     * Find queue items created after date
     */
    List<IndexImportQueue> findByCreatedAtAfter(LocalDateTime createdAt);
    
    /**
     * Find queue items created between dates
     */
    List<IndexImportQueue> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find queue items by system type and indexed status
     */
    List<IndexImportQueue> findBySystemTypeAndIndexed(String systemType, Boolean indexed);
    
    /**
     * Find queue items by system type and error status
     */
    List<IndexImportQueue> findBySystemTypeAndError(String systemType, Boolean error);
    
    /**
     * Count queue items by system type
     */
    long countBySystemType(String systemType);
    
    /**
     * Count indexed queue items
     */
    long countByIndexedTrue();
    
    /**
     * Count queue items with errors
     */
    long countByErrorTrue();
    
    /**
     * Count queue items by company key
     */
    long countByCompanyKey(String companyKey);
    
    /**
     * Count queue items by journal key
     */
    long countByJournalKey(String journalKey);
    
    /**
     * Check if queue item exists by queue key
     */
    boolean existsByQueueKey(String queueKey);
    
    /**
     * Find one pending import queue entry for processing
     */
    Optional<IndexImportQueue> findByIndexedFalseAndErrorFalseOrderByCreatedAtAsc();
    
    /**
     * Count pending import queue entries
     */
    long countByIndexedFalseAndErrorFalse();
    
    /**
     * Count completed import queue entries
     */
    long countByIndexedTrueAndErrorFalse();
    
}
