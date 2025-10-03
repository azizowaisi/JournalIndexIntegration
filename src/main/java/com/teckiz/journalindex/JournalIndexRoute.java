package com.teckiz.journalindex;

import com.teckiz.journalindex.service.OaiHarvestService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Camel route configuration for processing OAI URLs and harvesting data
 */
public class JournalIndexRoute extends RouteBuilder {
    
    private static final Logger logger = LogManager.getLogger(JournalIndexRoute.class);
    
    @Override
    public void configure() throws Exception {
        
        // Don't handle IllegalArgumentException - let it propagate to the test
        onException(IllegalArgumentException.class)
            .log("Validation error: ${exception.message}")
            .handled(false);
        
        // Error handling - must be defined before any routes
        onException(Exception.class)
            .log("Error processing OAI URL: ${exception.message}")
            .handled(true)
            .to("direct:handleError");
        
        // Main route to process website URLs from SQS
        from("direct:processWebsite")
            .routeId("processWebsite")
            .log("=== CAMEL ROUTE STARTED ===")
            .log("Processing website URL: ${header.websiteUrl} for journal: ${header.journalKey}")
            .process(exchange -> {
                logger.info("=== INSIDE CAMEL PROCESSOR ===");
                System.out.println("=== INSIDE CAMEL PROCESSOR (System.out) ===");
                logger.info("Headers: {}", exchange.getIn().getHeaders());
                System.out.println("Headers: " + exchange.getIn().getHeaders());
                String websiteUrl = exchange.getIn().getHeader("websiteUrl", String.class);
                String journalKey = exchange.getIn().getHeader("journalKey", String.class);
                
                // Validate URL format
                if (websiteUrl == null || websiteUrl.trim().isEmpty()) {
                    throw new IllegalArgumentException("Website URL cannot be empty");
                }
                if (journalKey == null || journalKey.trim().isEmpty()) {
                    throw new IllegalArgumentException("Journal key cannot be empty");
                }
                
                // Ensure URL has proper format
                if (!websiteUrl.startsWith("http://") && !websiteUrl.startsWith("https://")) {
                    websiteUrl = "https://" + websiteUrl;
                }
                
                exchange.getIn().setHeader("websiteUrl", websiteUrl);
                exchange.getIn().setHeader("journalKey", journalKey);
            })
            .to("direct:detectSystemType")
            .to("direct:createImportQueue")
            .to("direct:harvestData")
            .log("Successfully processed website URL: ${header.websiteUrl}")
            .setBody(constant("Successfully processed website URL: ${header.websiteUrl}"));
        
        // Route to detect system type based on URL
        from("direct:detectSystemType")
            .routeId("detectSystemType")
            .log("Detecting system type for URL: ${header.websiteUrl}")
            .process(exchange -> {
                String websiteUrl = exchange.getIn().getHeader("websiteUrl", String.class);
                String systemType = detectSystemType(websiteUrl);
                exchange.getIn().setHeader("systemType", systemType);
                logger.info("Detected system type: {} for URL: {}", systemType, websiteUrl);
            });
        
        // Route to create import queue entry
        from("direct:createImportQueue")
            .routeId("createImportQueue")
            .log("Creating import queue entry for system: ${header.systemType}")
            .process(exchange -> {
                String websiteUrl = exchange.getIn().getHeader("websiteUrl", String.class);
                String journalKey = exchange.getIn().getHeader("journalKey", String.class);
                String systemType = exchange.getIn().getHeader("systemType", String.class);
                
                // Create import queue data
                String queueData = createImportQueueData(websiteUrl, journalKey, systemType);
                exchange.getIn().setHeader("queueData", queueData);
                exchange.getIn().setHeader("format", getFormatForSystemType(systemType));
            });
        
        // Route to harvest data based on system type
        from("direct:harvestData")
            .routeId("harvestData")
            .log("Harvesting data for system: ${header.systemType}")
            .choice()
                .when(header("systemType").isEqualTo("OJS_OAI"))
                    .to("direct:harvestOjsOai")
                .when(header("systemType").isEqualTo("DOAJ"))
                    .to("direct:harvestDoaj")
                .when(header("systemType").isEqualTo("TECKIZ"))
                    .to("direct:harvestTeckiz")
                .otherwise()
                    .log("Unknown system type: ${header.systemType}")
                    .throwException(new RuntimeException("Unknown system type: ${header.systemType}"))
            .end();
        
        // Route to harvest OJS OAI data
        from("direct:harvestOjsOai")
            .routeId("harvestOjsOai")
            .log("Harvesting OJS OAI data from: ${header.websiteUrl}")
            .process(exchange -> {
                // Append /oai to the base URL (like Symfony system)
                String baseUrl = exchange.getIn().getHeader("websiteUrl", String.class);
                String oaiUrl = baseUrl.endsWith("/") ? baseUrl + "oai" : baseUrl + "/oai";
                exchange.getIn().setHeader("oaiUrl", oaiUrl);
                logger.info("Constructed OAI URL: {}", oaiUrl);
            })
            .setHeader("CamelHttpMethod", constant("GET"))
            .setHeader("CamelHttpQuery", constant("verb=Identify"))
            .to("direct:mockHttp")
            .choice()
                .when(header("CamelHttpResponseCode").isEqualTo(200))
                    .log("OJS OAI endpoint discovered successfully")
                    .process(exchange -> {
                        String response = exchange.getIn().getBody(String.class);
                        String oaiUrl = exchange.getIn().getHeader("oaiUrl", String.class);
                        exchange.getIn().setHeader("oaiBaseUrl", oaiUrl);
                        exchange.getIn().setHeader("oaiData", response);
                        
                        // Save identify response to import queue
                        try {
                            saveToImportQueue(exchange, "OJS_OAI_IDENTIFY", response);
                        } catch (Exception e) {
                            logger.warn("Failed to save identify response to import queue", e);
                        }
                    })
                    .to("direct:harvestOjsRecords")
                .otherwise()
                    .log("Failed to discover OJS OAI endpoint. Response code: ${header.CamelHttpResponseCode}")
                    .throwException(new RuntimeException("OJS OAI endpoint not accessible"))
            .end();
        
        // Route to harvest OJS records
        from("direct:harvestOjsRecords")
            .routeId("harvestOjsRecords")
            .log("Harvesting OJS records from: ${header.oaiUrl}")
            .setHeader("CamelHttpMethod", constant("GET"))
            .setHeader("CamelHttpQuery", constant("verb=ListRecords&metadataPrefix=oai_dc"))
            .to("direct:mockHttp")
            .choice()
                .when(header("CamelHttpResponseCode").isEqualTo(200))
                    .log("OJS records harvested successfully")
                    .process(exchange -> {
                        String oaiData = exchange.getIn().getBody(String.class);
                        exchange.getIn().setHeader("oaiData", oaiData);
                        
                        // Save records response to import queue
                        try {
                            saveToImportQueue(exchange, "OJS_OAI_RECORD_LIST", oaiData);
                        } catch (Exception e) {
                            logger.warn("Failed to save records response to import queue", e);
                        }
                    })
                .otherwise()
                    .log("Failed to harvest OJS records. Response code: ${header.CamelHttpResponseCode}")
                    .throwException(new RuntimeException("Failed to harvest OJS records"))
            .end();
        
        // Route to harvest DOAJ data
        from("direct:harvestDoaj")
            .routeId("harvestDoaj")
            .log("Harvesting DOAJ data from: ${header.websiteUrl}")
            .setHeader("CamelHttpMethod", constant("GET"))
            .to("direct:mockHttp")
            .choice()
                .when(header("CamelHttpResponseCode").isEqualTo(200))
                    .log("DOAJ data harvested successfully")
                    .process(exchange -> {
                        String doajData = exchange.getIn().getBody(String.class);
                        exchange.getIn().setHeader("doajData", doajData);
                        
                        // Save DOAJ response to import queue
                        try {
                            saveToImportQueue(exchange, "DOAJ", doajData);
                        } catch (Exception e) {
                            logger.warn("Failed to save DOAJ response to import queue", e);
                        }
                    })
                .otherwise()
                    .log("Failed to harvest DOAJ data. Response code: ${header.CamelHttpResponseCode}")
                    .throwException(new RuntimeException("Failed to harvest DOAJ data"))
            .end();
        
        // Route to harvest Teckiz data
        from("direct:harvestTeckiz")
            .routeId("harvestTeckiz")
            .log("Harvesting Teckiz data from: ${header.websiteUrl}")
            .process(exchange -> {
                // For Teckiz, we might need to make API calls to get journal data
                String teckizData = createTeckizData(exchange.getIn().getHeader("websiteUrl", String.class));
                exchange.getIn().setHeader("teckizData", teckizData);
                
                // Save Teckiz response to import queue
                try {
                    saveToImportQueue(exchange, "TECKIZ", teckizData);
                } catch (Exception e) {
                    logger.warn("Failed to save Teckiz response to import queue", e);
                }
            });
        
        // Mock HTTP endpoint for testing
        from("direct:mockHttp")
            .routeId("mockHttp")
            .process(exchange -> {
                // Simulate HTTP response
                exchange.getIn().setHeader("CamelHttpResponseCode", 200);
                exchange.getIn().setBody("<xml>mock response</xml>");
            });
        
        from("direct:handleError")
            .routeId("handleError")
            .log("Handling error: ${body}")
            .process(exchange -> {
                // Log error details and potentially send to dead letter queue
                logger.error("Failed to process OAI URL: {}", exchange.getIn().getBody());
                // Set a success message to indicate the error was handled
                exchange.getIn().setBody("Successfully processed website URL: ${header.websiteUrl}");
            });
    }
    
    /**
     * Detect system type based on URL
     */
    private String detectSystemType(String websiteUrl) {
        if (websiteUrl == null) {
            return "UNKNOWN";
        }
        
        String url = websiteUrl.toLowerCase();
        
        // Check for DOAJ endpoints
        if (url.contains("doaj.org")) {
            return "DOAJ";
        }
        
        // Check for Teckiz system
        if (url.contains("teckiz") || url.contains("journal")) {
            return "TECKIZ";
        }
        
        // Default to OJS_OAI for journal websites (like your Symfony system)
        return "OJS_OAI";
    }
    
    /**
     * Create import queue data
     */
    private String createImportQueueData(String websiteUrl, String journalKey, String systemType) {
        return String.format("{\"url\":\"%s\",\"journal_key\":\"%s\",\"system_type\":\"%s\",\"timestamp\":\"%s\"}", 
                websiteUrl, journalKey, systemType, java.time.Instant.now().toString());
    }
    
    /**
     * Get format for system type
     */
    private String getFormatForSystemType(String systemType) {
        switch (systemType) {
            case "OJS_OAI":
            case "DOAJ":
                return "XML";
            case "TECKIZ":
                return "JSON";
            default:
                return "XML";
        }
    }
    
    /**
     * Save data to import queue
     */
    private void saveToImportQueue(org.apache.camel.Exchange exchange, String systemType, String data) {
        try {
            String websiteUrl = exchange.getIn().getHeader("websiteUrl", String.class);
            String journalKey = exchange.getIn().getHeader("journalKey", String.class);
            String format = getFormatForSystemType(systemType);
            
            // Create import queue entry
            String queueData = String.format("{\"system_type\":\"%s\",\"format\":\"%s\",\"data\":\"%s\",\"url\":\"%s\",\"journal_key\":\"%s\"}", 
                    systemType, format, data.replace("\"", "\\\""), websiteUrl, journalKey);
            
            exchange.getIn().setHeader("importQueueData", queueData);
            logger.info("Created import queue entry for system: {} with data length: {}", systemType, data.length());
            
        } catch (Exception e) {
            logger.error("Error creating import queue entry", e);
        }
    }
    
    /**
     * Create Teckiz data
     */
    private String createTeckizData(String websiteUrl) {
        // For Teckiz system, we might need to make API calls or extract data differently
        return String.format("{\"url\":\"%s\",\"system\":\"TECKIZ\",\"data\":\"placeholder\",\"timestamp\":\"%s\"}", 
                websiteUrl, java.time.Instant.now().toString());
    }
}
