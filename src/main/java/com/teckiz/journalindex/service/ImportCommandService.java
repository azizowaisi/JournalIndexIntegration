package com.teckiz.journalindex.service;

import com.teckiz.journalindex.entity.IndexImportQueue;
import com.teckiz.journalindex.repository.IndexImportQueueRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for processing IndexImportQueue entries (ImportCommand functionality)
 * This service handles the second step of the process - mapping data to database
 */
@Service
public class ImportCommandService {
    
    private static final Logger logger = LogManager.getLogger(ImportCommandService.class);
    
    @Autowired
    private IndexImportQueueRepository importQueueRepository;
    
    @Autowired
    private IndexQueueImporter indexQueueImporter;
    
    /**
     * Process one import queue entry (equivalent to ImportCommand.execute)
     */
    public boolean processOneImportQueue() {
        try {
            // Get one import queue entry for processing
            List<IndexImportQueue> importQueues = importQueueRepository.findByIndexedFalseAndErrorFalseOrderByCreatedAtAsc();
            Optional<IndexImportQueue> importQueueOpt = importQueues.isEmpty() ? Optional.empty() : Optional.of(importQueues.get(0));
            
            if (importQueueOpt.isEmpty()) {
                logger.info("Index Journal Import queue is empty and waiting for new data...");
                return false; // No work to do
            }
            
            IndexImportQueue importQueue = importQueueOpt.get();
            logger.info("Processing import queue entry: {} (system: {})", importQueue.getId(), importQueue.getSystemType());
            
            try {
                // Process the import queue entry
                indexQueueImporter.handleImportQueue(importQueue);
                
                // Check if processing is complete
                if (isProcessingComplete(importQueue)) {
                    // Mark as completed
                    importQueue.setIndexed(true);
                    importQueue.setMessage("Data imported successfully");
                    importQueue.setUpdatedAt(LocalDateTime.now());
                    
                    importQueueRepository.save(importQueue);
                    logger.info("Import queue entry {} completed successfully", importQueue.getId());
                } else {
                    // Still processing, update progress
                    importQueue.setMessage("Processing in progress...");
                    importQueue.setUpdatedAt(LocalDateTime.now());
                    importQueueRepository.save(importQueue);
                    logger.info("Import queue entry {} still processing", importQueue.getId());
                }
                
                return true;
                
            } catch (Exception e) {
                logger.error("Error processing import queue entry: {}", importQueue.getId(), e);
                
                // Mark as failed
                importQueue.setIndexed(true);
                importQueue.setError(true);
                importQueue.setMessage("Error: " + e.getMessage());
                importQueue.setUpdatedAt(LocalDateTime.now());
                
                importQueueRepository.save(importQueue);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error in ImportCommandService", e);
            return false;
        }
    }
    
    /**
     * Process all pending import queue entries
     */
    public int processAllPendingQueues() {
        int processedCount = 0;
        int maxIterations = 100; // Prevent infinite loops
        int iteration = 0;
        
        while (iteration < maxIterations) {
            boolean processed = processOneImportQueue();
            if (!processed) {
                break; // No more work to do
            }
            processedCount++;
            iteration++;
        }
        
        logger.info("Processed {} import queue entries", processedCount);
        return processedCount;
    }
    
    /**
     * Check if processing is complete for an import queue entry
     */
    private boolean isProcessingComplete(IndexImportQueue importQueue) {
        try {
            String totalRecordsStr = importQueue.getTotalRecords();
            String indexedRecordsStr = importQueue.getIndexedRecords();
            
            if (totalRecordsStr == null || indexedRecordsStr == null) {
                return true; // Assume complete if no record counts
            }
            
            Integer totalRecords = Integer.parseInt(totalRecordsStr);
            Integer indexedRecords = Integer.parseInt(indexedRecordsStr);
            
            int total = totalRecords;
            int indexed = indexedRecords;
            
            return indexed >= total;
            
        } catch (NumberFormatException e) {
            logger.warn("Error parsing record counts for queue {}: total={}, indexed={}", 
                    importQueue.getId(), importQueue.getTotalRecords(), importQueue.getIndexedRecords());
            return true; // Assume complete on parsing error
        }
    }
    
    /**
     * Get statistics about import queue
     */
    public ImportQueueStats getImportQueueStats() {
        ImportQueueStats stats = new ImportQueueStats();
        
        stats.setTotalQueues(importQueueRepository.count());
        stats.setPendingQueues(importQueueRepository.countByIndexedFalseAndErrorFalse());
        stats.setCompletedQueues(importQueueRepository.countByIndexedTrueAndErrorFalse());
        stats.setFailedQueues(importQueueRepository.countByErrorTrue());
        
        return stats;
    }
    
    /**
     * Reset failed import queue entries for retry
     */
    public int resetFailedQueues() {
        List<IndexImportQueue> failedQueues = importQueueRepository.findByErrorTrue();
        
        for (IndexImportQueue queue : failedQueues) {
            queue.setError(false);
            queue.setIndexed(false);
            queue.setMessage("Reset for retry");
            queue.setUpdatedAt(LocalDateTime.now());
            importQueueRepository.save(queue);
        }
        
        logger.info("Reset {} failed import queue entries for retry", failedQueues.size());
        return failedQueues.size();
    }
    
    /**
     * Statistics class for import queue
     */
    public static class ImportQueueStats {
        private long totalQueues;
        private long pendingQueues;
        private long completedQueues;
        private long failedQueues;
        
        // Getters and setters
        public long getTotalQueues() { return totalQueues; }
        public void setTotalQueues(long totalQueues) { this.totalQueues = totalQueues; }
        
        public long getPendingQueues() { return pendingQueues; }
        public void setPendingQueues(long pendingQueues) { this.pendingQueues = pendingQueues; }
        
        public long getCompletedQueues() { return completedQueues; }
        public void setCompletedQueues(long completedQueues) { this.completedQueues = completedQueues; }
        
        public long getFailedQueues() { return failedQueues; }
        public void setFailedQueues(long failedQueues) { this.failedQueues = failedQueues; }
        
        @Override
        public String toString() {
            return String.format("ImportQueueStats{total=%d, pending=%d, completed=%d, failed=%d}", 
                    totalQueues, pendingQueues, completedQueues, failedQueues);
        }
    }
}
