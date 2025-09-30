package com.teckiz.journalindex.service;

import com.teckiz.journalindex.entity.IndexImportQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for processing different types of import queue entries
 * This mirrors the PHP IndexQueueImporter functionality
 */
@Service
public class IndexQueueImporter {
    
    private static final Logger logger = LogManager.getLogger(IndexQueueImporter.class);
    
    @Autowired
    private DoajXmlImporter doajXmlImporter;
    
    @Autowired
    private OjsOaiXmlImporter ojsOaiXmlImporter;
    
    @Autowired
    private JsonImporter jsonImporter;
    
    /**
     * Handle import queue based on system type
     * This mirrors the PHP IndexQueueImporter.handleImportQueue method
     */
    public void handleImportQueue(IndexImportQueue importQueue) {
        String systemType = importQueue.getSystemType();
        
        logger.info("Processing import queue entry: {} (system: {})", importQueue.getId(), systemType);
        
        switch (systemType) {
            case "doaj":
                logger.info("Processing DOAJ-XML import");
                doajXmlImporter.importData(importQueue);
                break;
                
            case "teckiz":
                logger.info("Processing TECKIZ-JSON import");
                jsonImporter.importTeckiz(importQueue);
                break;
                
            case "ojs-identify":
                logger.info("Processing OJS_OAI_IDENTIFY import");
                ojsOaiXmlImporter.importIdentify(importQueue);
                break;
                
            case "ojs-record-list":
                logger.info("Processing OJS_OAI_RECORD_LIST import");
                ojsOaiXmlImporter.importRecords(importQueue);
                break;
                
            default:
                logger.warn("Unknown system type for import queue: {}", systemType);
                throw new IllegalArgumentException("Unknown system type: " + systemType);
        }
    }
}
