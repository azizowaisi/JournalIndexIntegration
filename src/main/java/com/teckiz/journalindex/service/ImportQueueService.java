package com.teckiz.journalindex.service;

import com.teckiz.journalindex.entity.IndexImportQueue;
import com.teckiz.journalindex.repository.IndexImportQueueRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for managing IndexImportQueue entries
 */
@Service
public class ImportQueueService {
    
    private static final Logger logger = LogManager.getLogger(ImportQueueService.class);
    
    @Autowired
    private IndexImportQueueRepository importQueueRepository;
    
    /**
     * Create a new import queue entry
     */
    public IndexImportQueue createImportQueue(String systemType, String format, String data, 
                                            String websiteUrl, String journalKey, String companyKey) {
        try {
            IndexImportQueue importQueue = new IndexImportQueue();
            importQueue.setSystemType(systemType);
            importQueue.setFormat(format);
            importQueue.setData(data);
            importQueue.setCompanyKey(companyKey);
            importQueue.setJournalKey(journalKey);
            importQueue.setIndexed(false);
            importQueue.setError(false);
            importQueue.setMessage("Created from SQS message");
            importQueue.setCreatedAt(LocalDateTime.now());
            importQueue.setUpdatedAt(LocalDateTime.now());
            
            // Calculate total records from data
            int totalRecords = calculateTotalRecords(data, systemType);
            importQueue.setTotalRecords(String.valueOf(totalRecords));
            importQueue.setIndexedRecords("0");
            
            IndexImportQueue savedQueue = importQueueRepository.save(importQueue);
            logger.info("Created import queue entry with ID: {} for system: {}", savedQueue.getId(), systemType);
            
            return savedQueue;
            
        } catch (Exception e) {
            logger.error("Error creating import queue entry", e);
            throw new RuntimeException("Failed to create import queue entry", e);
        }
    }
    
    /**
     * Create OJS OAI identify queue entry
     */
    public IndexImportQueue createOjsOaiIdentifyQueue(String data, String websiteUrl, String journalKey, String companyKey) {
        return createImportQueue("ojs-identify", "xml", data, websiteUrl, journalKey, companyKey);
    }
    
    /**
     * Create OJS OAI record list queue entry
     */
    public IndexImportQueue createOjsOaiRecordListQueue(String data, String websiteUrl, String journalKey, String companyKey) {
        return createImportQueue("ojs-record-list", "xml", data, websiteUrl, journalKey, companyKey);
    }
    
    /**
     * Create DOAJ queue entry
     */
    public IndexImportQueue createDoajQueue(String data, String websiteUrl, String journalKey, String companyKey) {
        return createImportQueue("doaj", "xml", data, websiteUrl, journalKey, companyKey);
    }
    
    /**
     * Create Teckiz queue entry
     */
    public IndexImportQueue createTeckizQueue(String data, String websiteUrl, String journalKey, String companyKey) {
        return createImportQueue("teckiz", "json", data, websiteUrl, journalKey, companyKey);
    }
    
    /**
     * Calculate total records from data based on system type
     */
    private int calculateTotalRecords(String data, String systemType) {
        try {
            switch (systemType) {
                case "ojs-identify":
                    return 1; // Identify response is always 1 record
                    
                case "ojs-record-list":
                case "doaj":
                    // Count records in XML data
                    return countXmlRecords(data);
                    
                case "teckiz":
                    // Count records in JSON data
                    return countJsonRecords(data);
                    
                default:
                    return 1;
            }
        } catch (Exception e) {
            logger.warn("Error calculating total records for system: {}", systemType, e);
            return 1;
        }
    }
    
    /**
     * Count records in XML data
     */
    private int countXmlRecords(String xmlData) {
        if (xmlData == null || xmlData.trim().isEmpty()) {
            return 0;
        }
        
        // Simple count of <record> tags
        int count = 0;
        int index = 0;
        while ((index = xmlData.indexOf("<record", index)) != -1) {
            count++;
            index += 7; // Move past "<record"
        }
        
        return count;
    }
    
    /**
     * Count records in JSON data
     */
    private int countJsonRecords(String jsonData) {
        if (jsonData == null || jsonData.trim().isEmpty()) {
            return 0;
        }
        
        // Simple count of array elements or objects
        // This is a basic implementation - in production you'd use a proper JSON parser
        int count = 0;
        int index = 0;
        while ((index = jsonData.indexOf("{", index)) != -1) {
            count++;
            index += 1;
        }
        
        return count;
    }
    
    /**
     * Update import queue entry status
     */
    public void updateImportQueueStatus(Long queueId, boolean indexed, boolean error, String message) {
        try {
            IndexImportQueue importQueue = importQueueRepository.findById(queueId)
                    .orElseThrow(() -> new RuntimeException("Import queue not found with ID: " + queueId));
            
            importQueue.setIndexed(indexed);
            importQueue.setError(error);
            importQueue.setMessage(message);
            importQueue.setUpdatedAt(LocalDateTime.now());
            
            importQueueRepository.save(importQueue);
            logger.info("Updated import queue status for ID: {}", queueId);
            
        } catch (Exception e) {
            logger.error("Error updating import queue status for ID: {}", queueId, e);
            throw new RuntimeException("Failed to update import queue status", e);
        }
    }
    
    /**
     * Mark import queue as completed
     */
    public void markAsCompleted(Long queueId, int indexedRecords) {
        updateImportQueueStatus(queueId, true, false, "Processing completed successfully");
        
        try {
            IndexImportQueue importQueue = importQueueRepository.findById(queueId)
                    .orElseThrow(() -> new RuntimeException("Import queue not found with ID: " + queueId));
            
            importQueue.setIndexedRecords(String.valueOf(indexedRecords));
            importQueueRepository.save(importQueue);
            
        } catch (Exception e) {
            logger.error("Error updating indexed records for queue ID: {}", queueId, e);
        }
    }
    
    /**
     * Mark import queue as failed
     */
    public void markAsFailed(Long queueId, String errorMessage) {
        updateImportQueueStatus(queueId, false, true, errorMessage);
    }
}
