package com.teckiz.journalindex;

import com.teckiz.journalindex.service.ImportCommandService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * AWS Lambda handler for processing IndexImportQueue entries (ImportCommand functionality)
 * This handles the second step of the process - mapping data to database
 */
public class ImportCommandLambdaHandler implements RequestHandler<Map<String, Object>, String> {
    
    private static final Logger logger = LogManager.getLogger(ImportCommandLambdaHandler.class);
    
    // This would be injected in a real Spring Boot application
    private final ImportCommandService importCommandService;
    
    public ImportCommandLambdaHandler() {
        // In a real implementation, this would be injected by Spring
        // For now, we'll create it manually
        this.importCommandService = new ImportCommandService();
    }
    
    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        logger.info("Starting ImportCommand processing");
        
        try {
            // Process one import queue entry (equivalent to ImportCommand.execute)
            boolean processed = importCommandService.processOneImportQueue();
            
            if (processed) {
                logger.info("Successfully processed one import queue entry");
                return "Successfully processed import queue entry";
            } else {
                logger.info("No import queue entries to process");
                return "No import queue entries to process";
            }
            
        } catch (Exception e) {
            logger.error("Error in ImportCommand processing", e);
            throw new RuntimeException("Failed to process import queue", e);
        }
    }
    
    /**
     * Process all pending import queue entries
     * This can be called for batch processing
     */
    public String processAllPending(Map<String, Object> input, Context context) {
        logger.info("Starting batch ImportCommand processing");
        
        try {
            int processedCount = importCommandService.processAllPendingQueues();
            
            logger.info("Processed {} import queue entries", processedCount);
            return String.format("Processed %d import queue entries", processedCount);
            
        } catch (Exception e) {
            logger.error("Error in batch ImportCommand processing", e);
            throw new RuntimeException("Failed to process import queues", e);
        }
    }
    
    /**
     * Get import queue statistics
     */
    public String getStats(Map<String, Object> input, Context context) {
        logger.info("Getting import queue statistics");
        
        try {
            ImportCommandService.ImportQueueStats stats = importCommandService.getImportQueueStats();
            
            logger.info("Import queue statistics: {}", stats);
            return stats.toString();
            
        } catch (Exception e) {
            logger.error("Error getting import queue statistics", e);
            throw new RuntimeException("Failed to get statistics", e);
        }
    }
}
