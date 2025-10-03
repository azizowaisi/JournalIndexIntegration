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
    public void addOJSXMLQueue(String journalKey, String systemType, String xmlData) {
        IndexImportQueue queue = new IndexImportQueue(journalKey, systemType, xmlData);
        queue.setFormat(IndexImportQueue.XML_FORMAT);
        indexImportQueueRepository.save(queue);
    }
    
    /**
     * Add Teckiz queue entry - matches Symfony addTeckizQueue method
     */
    @Transactional
    public void addTeckizQueue(String journalKey, String jsonData) {
        IndexImportQueue queue = new IndexImportQueue(journalKey, IndexImportQueue.TECKIZ, jsonData);
        queue.setFormat(IndexImportQueue.JSON_FORMAT);
        indexImportQueueRepository.save(queue);
    }
    
    /**
     * Add DOAJ queue entry
     */
    @Transactional
    public void addDOAJQueue(String journalKey, String jsonData) {
        IndexImportQueue queue = new IndexImportQueue(journalKey, IndexImportQueue.DOAJ_TYPE_XML, jsonData);
        queue.setFormat(IndexImportQueue.XML_FORMAT);
        indexImportQueueRepository.save(queue);
    }
    
    /**
     * Remove old queue entries for a journal - matches Symfony removeOldQueue method
     */
    @Transactional
    public void removeOldQueue(String journalKey) {
        List<IndexImportQueue> oldQueues = indexImportQueueRepository.findByJournalKeyOrderByCreatedAtDesc(journalKey);
        indexImportQueueRepository.deleteAll(oldQueues);
    }
    
    /**
     * Get all pending queue entries
     */
    public List<IndexImportQueue> getPendingQueues() {
        return indexImportQueueRepository.findByIndexedFalseOrderByCreatedAtAsc();
    }
    
    /**
     * Mark queue entry as processed
     */
    @Transactional
    public void markAsProcessed(Long queueId) {
        IndexImportQueue queue = indexImportQueueRepository.findById(queueId).orElse(null);
        if (queue != null) {
            queue.setIndexed(true);
            queue.setError(false);
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
            queue.setError(true);
            queue.setMessage(errorMessage);
            queue.setUpdatedAt(LocalDateTime.now());
            indexImportQueueRepository.save(queue);
        }
    }
}
