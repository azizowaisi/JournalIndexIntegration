package com.teckiz.journalindex.service;

import com.teckiz.journalindex.entity.IndexImportQueue;
import com.teckiz.journalindex.repository.IndexImportQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing IndexImportQueue operations
 * Matches Symfony ImportQueueHelper functionality
 */
@Service
public class IndexImportQueueService {
    
    @Autowired
    private IndexImportQueueRepository indexImportQueueRepository;
    
    /**
     * Add OJS OAI XML queue entry - matches Symfony addOJSXMLQueue method
     */
    @Transactional
    public void addOJSXMLQueue(Long indexJournalId, String systemType, String xmlData) {
        IndexImportQueue queue = new IndexImportQueue(indexJournalId, systemType, xmlData);
        queue.setStatus("pending");
        indexImportQueueRepository.save(queue);
    }
    
    /**
     * Add Teckiz queue entry - matches Symfony addTeckizQueue method
     */
    @Transactional
    public void addTeckizQueue(Long indexJournalId, String jsonData) {
        IndexImportQueue queue = new IndexImportQueue(indexJournalId, IndexImportQueue.TECKIZ, jsonData);
        queue.setStatus("pending");
        indexImportQueueRepository.save(queue);
    }
    
    /**
     * Add DOAJ queue entry
     */
    @Transactional
    public void addDOAJQueue(Long indexJournalId, String jsonData) {
        IndexImportQueue queue = new IndexImportQueue(indexJournalId, IndexImportQueue.DOAJ, jsonData);
        queue.setStatus("pending");
        indexImportQueueRepository.save(queue);
    }
    
    /**
     * Remove old queue entries for a journal - matches Symfony removeOldQueue method
     */
    @Transactional
    public void removeOldQueue(Long indexJournalId) {
        List<IndexImportQueue> oldQueues = indexImportQueueRepository.findByIndexJournalIdOrderByCreatedAtDesc(indexJournalId);
        indexImportQueueRepository.deleteAll(oldQueues);
    }
    
    /**
     * Get all pending queue entries
     */
    public List<IndexImportQueue> getPendingQueues() {
        return indexImportQueueRepository.findByStatusOrderByCreatedAtAsc("pending");
    }
    
    /**
     * Mark queue entry as processed
     */
    @Transactional
    public void markAsProcessed(Long queueId) {
        IndexImportQueue queue = indexImportQueueRepository.findById(queueId).orElse(null);
        if (queue != null) {
            queue.setStatus("processed");
            queue.setProcessedAt(LocalDateTime.now());
            queue.setUpdatedAt(LocalDateTime.now());
            indexImportQueueRepository.save(queue);
        }
    }
    
    /**
     * Mark queue entry as failed
     */
    @Transactional
    public void markAsFailed(Long queueId, String errorMessage) {
        IndexImportQueue queue = indexImportQueueRepository.findById(queueId).orElse(null);
        if (queue != null) {
            queue.setStatus("failed");
            queue.setErrorMessage(errorMessage);
            queue.setRetryCount(queue.getRetryCount() + 1);
            queue.setUpdatedAt(LocalDateTime.now());
            indexImportQueueRepository.save(queue);
        }
    }
}
