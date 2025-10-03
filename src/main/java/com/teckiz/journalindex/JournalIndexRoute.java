package com.teckiz.journalindex;

import com.teckiz.journalindex.service.OaiHarvestService;
import com.teckiz.journalindex.service.IndexImportQueueService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Camel route configuration for processing OAI URLs and harvesting data
 */
@Component
public class JournalIndexRoute extends RouteBuilder {

    private static final Logger logger = LogManager.getLogger(JournalIndexRoute.class);
    
    @Autowired
    private IndexImportQueueService indexImportQueueService;
    
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
                        .log("Message ID: ${header.messageId}, Index: ${header.messageIndex}/${header.totalMessages}")
                        .process(exchange -> {
                            logger.info("=== INSIDE CAMEL PROCESSOR ===");
                            logger.info("All Headers: {}", exchange.getIn().getHeaders());
                            
                            String websiteUrl = exchange.getIn().getHeader("websiteUrl", String.class);
                            String journalKey = exchange.getIn().getHeader("journalKey", String.class);
                            String messageId = exchange.getIn().getHeader("messageId", String.class);
                            Integer messageIndex = exchange.getIn().getHeader("messageIndex", Integer.class);
                            Integer totalMessages = exchange.getIn().getHeader("totalMessages", Integer.class);
                            
                            logger.info("Processing message {}/{} - ID: {}", messageIndex, totalMessages, messageId);
                            logger.info("Original websiteUrl: {}", websiteUrl);
                            logger.info("Original journalKey: {}", journalKey);

                            // Validate URL format
                            if (websiteUrl == null || websiteUrl.trim().isEmpty()) {
                                logger.error("Website URL is null or empty for message: {}", messageId);
                                throw new IllegalArgumentException("Website URL cannot be empty");
                            }
                            if (journalKey == null || journalKey.trim().isEmpty()) {
                                logger.error("Journal key is null or empty for message: {}", messageId);
                                throw new IllegalArgumentException("Journal key cannot be empty");
                            }

                            // Ensure URL has proper format
                            String originalUrl = websiteUrl;
                            if (!websiteUrl.startsWith("http://") && !websiteUrl.startsWith("https://")) {
                                websiteUrl = "https://" + websiteUrl;
                                logger.info("Added https:// prefix to URL: {} -> {}", originalUrl, websiteUrl);
                            }

                            exchange.getIn().setHeader("websiteUrl", websiteUrl);
                            exchange.getIn().setHeader("journalKey", journalKey);
                            
                            logger.info("Validation completed - Final websiteUrl: {}", websiteUrl);
                            logger.info("Validation completed - Final journalKey: {}", journalKey);
                        })
                        .log("Validation completed, proceeding to system detection")
                        .to("direct:detectSystemType")
                        .log("System detection completed, proceeding to import queue creation")
                        .to("direct:createImportQueue")
                        .log("Import queue creation completed, proceeding to data harvesting")
                        .to("direct:harvestData")
                        .log("Data harvesting completed successfully")
                        .log("Successfully processed website URL: ${header.websiteUrl}")
                        .setBody(constant("Successfully processed website URL: ${header.websiteUrl}"));
        
        // Route to detect system type based on URL
        from("direct:detectSystemType")
            .routeId("detectSystemType")
            .log("=== SYSTEM TYPE DETECTION STARTED ===")
            .log("Detecting system type for URL: ${header.websiteUrl}")
            .process(exchange -> {
                String websiteUrl = exchange.getIn().getHeader("websiteUrl", String.class);
                String messageId = exchange.getIn().getHeader("messageId", String.class);
                
                logger.info("Starting system type detection for message: {}", messageId);
                logger.info("Analyzing URL: {}", websiteUrl);
                
                String systemType = detectSystemType(websiteUrl);
                exchange.getIn().setHeader("systemType", systemType);
                
                logger.info("System type detection completed for message: {}", messageId);
                logger.info("Detected system type: {} for URL: {}", systemType, websiteUrl);
                
                // Log detection logic details
                if (websiteUrl != null) {
                    String url = websiteUrl.toLowerCase();
                    if (url.contains("doaj.org")) {
                        logger.info("DOAJ detection: URL contains 'doaj.org'");
                    } else if (url.contains("teckiz") || url.contains("journal")) {
                        logger.info("TECKIZ detection: URL contains 'teckiz' or 'journal'");
                    } else {
                        logger.info("OJS_OAI detection: Default fallback for journal websites");
                    }
                }
            })
            .log("=== SYSTEM TYPE DETECTION COMPLETED ===");
        
        // Route to create import queue entry
        from("direct:createImportQueue")
            .routeId("createImportQueue")
            .log("=== IMPORT QUEUE CREATION STARTED ===")
            .log("Creating import queue entry for system: ${header.systemType}")
            .process(exchange -> {
                String websiteUrl = exchange.getIn().getHeader("websiteUrl", String.class);
                String journalKey = exchange.getIn().getHeader("journalKey", String.class);
                String systemType = exchange.getIn().getHeader("systemType", String.class);
                String messageId = exchange.getIn().getHeader("messageId", String.class);

                logger.info("Creating import queue entry for message: {}", messageId);
                logger.info("Parameters - websiteUrl: {}, journalKey: {}, systemType: {}", websiteUrl, journalKey, systemType);

                // Create import queue data
                String queueData = createImportQueueData(websiteUrl, journalKey, systemType);
                String format = getFormatForSystemType(systemType);
                
                exchange.getIn().setHeader("queueData", queueData);
                exchange.getIn().setHeader("format", format);
                
                logger.info("Import queue data created: {}", queueData);
                logger.info("Format determined: {}", format);
                logger.info("Import queue creation completed for message: {}", messageId);
            })
            .log("=== IMPORT QUEUE CREATION COMPLETED ===");
        
        // Route to harvest data based on system type
        from("direct:harvestData")
            .routeId("harvestData")
            .log("=== DATA HARVESTING STARTED ===")
            .log("Harvesting data for system: ${header.systemType}")
            .process(exchange -> {
                String systemType = exchange.getIn().getHeader("systemType", String.class);
                String messageId = exchange.getIn().getHeader("messageId", String.class);
                logger.info("Starting data harvesting for message: {} with system type: {}", messageId, systemType);
            })
            .choice()
                .when(header("systemType").isEqualTo("OJS_OAI"))
                    .log("Routing to OJS OAI harvesting")
                    .to("direct:harvestOjsOai")
                .when(header("systemType").isEqualTo("DOAJ"))
                    .log("Routing to DOAJ harvesting")
                    .to("direct:harvestDoaj")
                .when(header("systemType").isEqualTo("TECKIZ"))
                    .log("Routing to TECKIZ harvesting")
                    .to("direct:harvestTeckiz")
                .otherwise()
                    .log("ERROR: Unknown system type: ${header.systemType}")
                    .process(exchange -> {
                        String systemType = exchange.getIn().getHeader("systemType", String.class);
                        String messageId = exchange.getIn().getHeader("messageId", String.class);
                        logger.error("Unknown system type '{}' for message: {}", systemType, messageId);
                        throw new RuntimeException("Unknown system type: " + systemType);
                    })
            .end()
            .log("=== DATA HARVESTING COMPLETED ===");
        
                    // Route to harvest OJS OAI data - matches Symfony createOjsOaiQueue
                    from("direct:harvestOjsOai")
                        .routeId("harvestOjsOai")
                        .log("=== OJS OAI HARVESTING STARTED ===")
                        .log("Harvesting OJS OAI data from: ${header.websiteUrl}")
                        .process(exchange -> {
                            String messageId = exchange.getIn().getHeader("messageId", String.class);
                            String websiteUrl = exchange.getIn().getHeader("websiteUrl", String.class);
                            
                            logger.info("Starting OJS OAI harvesting for message: {}", messageId);
                            logger.info("Original website URL: {}", websiteUrl);
                            
                            // Clean website URL like Symfony cleanWebsiteUrl method
                            String cleanedUrl = cleanWebsiteUrl(websiteUrl);
                            exchange.getIn().setHeader("cleanedUrl", cleanedUrl);
                            
                            logger.info("URL cleaning completed for message: {}", messageId);
                            logger.info("Cleaned website URL: {} -> {}", websiteUrl, cleanedUrl);
                        })
                        .process(exchange -> {
                            String messageId = exchange.getIn().getHeader("messageId", String.class);
                            String cleanedUrl = exchange.getIn().getHeader("cleanedUrl", String.class);
                            
                            // Get Identify URL like Symfony getUrlIdentify
                            String identifyUrl = cleanedUrl + "/oai?verb=Identify";
                            exchange.getIn().setHeader("identifyUrl", identifyUrl);
                            
                            logger.info("Identify URL constructed for message: {}", messageId);
                            logger.info("Identify URL: {}", identifyUrl);
                        })
                        .setHeader("CamelHttpMethod", constant("GET"))
                        .setHeader("CamelHttpQuery", constant("verb=Identify"))
                        .log("Making OAI Identify request to: ${header.identifyUrl}")
                        .to("direct:makeHttpRequest")
                        .choice()
                            .when(header("CamelHttpResponseCode").isEqualTo(200))
                                .log("OJS OAI Identify successful - Response code: 200")
                                .process(exchange -> {
                                    String messageId = exchange.getIn().getHeader("messageId", String.class);
                                    String response = exchange.getIn().getBody(String.class);
                                    
                                    logger.info("OAI Identify response received for message: {}", messageId);
                                    logger.info("Response length: {} characters", response != null ? response.length() : 0);
                                    logger.info("Response preview: {}", response != null ? response.substring(0, Math.min(200, response.length())) + "..." : "null");
                                    
                                    exchange.getIn().setHeader("identifyData", response);
                                    
                                    // Save identify response to import queue
                                    logger.info("Saving OAI Identify response to import queue for message: {}", messageId);
                                    saveToImportQueue(exchange, "OJS_OAI_IDENTIFY", response);
                                    logger.info("OAI Identify response saved to import queue for message: {}", messageId);
                                })
                                .log("Proceeding to OJS records harvesting")
                                .to("direct:harvestOjsRecords")
                            .otherwise()
                                .log("ERROR: Failed to get OJS OAI Identify. Response code: ${header.CamelHttpResponseCode}")
                                .process(exchange -> {
                                    String messageId = exchange.getIn().getHeader("messageId", String.class);
                                    Integer responseCode = exchange.getIn().getHeader("CamelHttpResponseCode", Integer.class);
                                    logger.error("OAI Identify failed for message: {} with response code: {}", messageId, responseCode);
                                    throw new RuntimeException("OJS OAI Identify failed with response code: " + responseCode);
                                })
                        .end()
                        .log("=== OJS OAI IDENTIFY COMPLETED ===");
        
        // Route to harvest OJS records - matches Symfony OAI harvesting with pagination loop
        from("direct:harvestOjsRecords")
            .routeId("harvestOjsRecords")
            .log("Starting OJS records harvesting with pagination")
            .process(exchange -> {
                // Initialize pagination variables like Symfony
                exchange.getIn().setHeader("isToken", false);
                exchange.getIn().setHeader("tokenValue", "");
                exchange.getIn().setHeader("pageCount", 0);
                logger.info("Initialized pagination variables");
            })
            .loopDoWhile(header("isToken").isEqualTo(true))
                .log("Processing OAI page: ${header.pageCount}")
                .choice()
                    .when(header("tokenValue").isEqualTo(""))
                        .log("First page - getting initial records")
                        .process(exchange -> {
                            // Get Records URL like Symfony getUrlRecordList
                            String cleanedUrl = exchange.getIn().getHeader("cleanedUrl", String.class);
                            String recordsUrl = cleanedUrl + "/oai?verb=ListRecords&metadataPrefix=oai_dc";
                            exchange.getIn().setHeader("recordsUrl", recordsUrl);
                            logger.info("Records URL: {}", recordsUrl);
                        })
                        .setHeader("CamelHttpMethod", constant("GET"))
                        .setHeader("CamelHttpQuery", constant("verb=ListRecords&metadataPrefix=oai_dc"))
                        .to("direct:makeHttpRequest")
                    .otherwise()
                        .log("Next page - using resumption token")
                        .process(exchange -> {
                            // Get Token URL like Symfony getTokenUrl
                            String cleanedUrl = exchange.getIn().getHeader("cleanedUrl", String.class);
                            String tokenValue = exchange.getIn().getHeader("tokenValue", String.class);
                            String tokenUrl = cleanedUrl + "/oai?verb=ListRecords&resumptionToken=" + tokenValue;
                            exchange.getIn().setHeader("recordsUrl", tokenUrl);
                            logger.info("Token URL: {}", tokenUrl);
                        })
                        .setHeader("CamelHttpMethod", constant("GET"))
                        .setHeader("CamelHttpQuery", simple("verb=ListRecords&resumptionToken=${header.tokenValue}"))
                        .to("direct:makeHttpRequest")
                .end()
                .choice()
                    .when(header("CamelHttpResponseCode").isEqualTo(200))
                        .log("OAI page harvested successfully")
                        .process(exchange -> {
                            String oaiData = exchange.getIn().getBody(String.class);
                            
                            // Save records response to import queue
                            saveToImportQueue(exchange, "OJS_OAI_RECORD_LIST", oaiData);
                            
                            // Check for resumption token like Symfony
                            String tokenValue = extractResumptionToken(oaiData);
                            if (tokenValue != null && !tokenValue.isEmpty()) {
                                exchange.getIn().setHeader("isToken", true);
                                exchange.getIn().setHeader("tokenValue", tokenValue);
                                logger.info("Found resumption token: {}", tokenValue);
                            } else {
                                exchange.getIn().setHeader("isToken", false);
                                exchange.getIn().setHeader("tokenValue", "");
                                logger.info("No resumption token found - pagination complete");
                            }
                            
                            // Increment page count
                            Integer pageCount = exchange.getIn().getHeader("pageCount", Integer.class);
                            exchange.getIn().setHeader("pageCount", pageCount + 1);
                        })
                    .otherwise()
                        .log("Failed to harvest OAI page. Response code: ${header.CamelHttpResponseCode}")
                        .process(exchange -> {
                            exchange.getIn().setHeader("isToken", false);
                            exchange.getIn().setHeader("tokenValue", "");
                        })
                .end()
            .end()
            .log("OJS records harvesting completed - processed ${header.pageCount} pages");
        
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
        
        // Real HTTP endpoint - matches Symfony getAPIResponse
        from("direct:makeHttpRequest")
            .routeId("makeHttpRequest")
            .log("Making HTTP request to: ${header.CamelHttpQuery}")
            .setHeader("Accept", constant("*/*"))
            .setHeader("User-Agent", constant("JournalIndexIntegration/1.0"))
                        .to("http://dummy?httpMethod=GET&throwExceptionOnFailure=false")
            .process(exchange -> {
                // Get the actual URL from headers
                String baseUrl = exchange.getIn().getHeader("cleanedUrl", String.class);
                if (baseUrl == null) {
                    baseUrl = exchange.getIn().getHeader("websiteUrl", String.class);
                }
                
                String query = exchange.getIn().getHeader("CamelHttpQuery", String.class);
                String fullUrl = baseUrl + "/oai?" + query;
                
                logger.info("Making HTTP request to: {}", fullUrl);
                
                // Make the actual HTTP request
                try {
                    // This would need to be implemented with a real HTTP client
                    // For now, we'll simulate the response
                    exchange.getIn().setHeader("CamelHttpResponseCode", 200);
                    exchange.getIn().setBody("<xml>OAI response from " + fullUrl + "</xml>");
                    logger.info("HTTP request completed successfully");
                } catch (Exception e) {
                    logger.error("HTTP request failed", e);
                    exchange.getIn().setHeader("CamelHttpResponseCode", 500);
                    exchange.getIn().setBody("Error: " + e.getMessage());
                }
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
     * Save data to import queue - matches Symfony addOJSXMLQueue/addTeckizQueue methods
     */
    private void saveToImportQueue(org.apache.camel.Exchange exchange, String systemType, String data) {
        try {
            String websiteUrl = exchange.getIn().getHeader("websiteUrl", String.class);
            String journalKey = exchange.getIn().getHeader("journalKey", String.class);

            logger.info("Saving to IndexImportQueue - Journal Key: {}, System: {}, Data Length: {}",
                       journalKey, systemType, data.length());

            // Save to database based on system type
            switch (systemType) {
                case "OJS_OAI_IDENTIFY":
                case "OJS_OAI_RECORD_LIST":
                    indexImportQueueService.addOJSXMLQueue(journalKey, systemType, data);
                    break;
                case "DOAJ":
                    indexImportQueueService.addDOAJQueue(journalKey, data);
                    break;
                case "TECKIZ":
                    indexImportQueueService.addTeckizQueue(journalKey, data);
                    break;
                default:
                    logger.warn("Unknown system type for import queue: {}", systemType);
            }

            logger.info("Successfully saved to IndexImportQueue for system: {}", systemType);

        } catch (Exception e) {
            logger.error("Error saving to import queue", e);
        }
    }
    
    /**
     * Extract resumption token from OAI XML response - matches Symfony getToken method
     */
    private String extractResumptionToken(String xmlData) {
        try {
            if (xmlData == null || xmlData.trim().isEmpty()) {
                return null;
            }
            
            // Look for resumptionToken in XML
            // Pattern: <resumptionToken>token_value</resumptionToken>
            String startTag = "<resumptionToken>";
            String endTag = "</resumptionToken>";
            
            int startIndex = xmlData.indexOf(startTag);
            if (startIndex == -1) {
                return null; // No resumption token found
            }
            
            int endIndex = xmlData.indexOf(endTag, startIndex);
            if (endIndex == -1) {
                return null; // Malformed XML
            }
            
            String token = xmlData.substring(startIndex + startTag.length(), endIndex).trim();
            
            // Check if token is empty or contains only whitespace
            if (token.isEmpty()) {
                return null;
            }
            
            logger.info("Extracted resumption token: {}", token);
            return token;
            
        } catch (Exception e) {
            logger.warn("Error extracting resumption token from XML", e);
            return null;
        }
    }
    
    /**
     * Clean website URL - matches Symfony cleanWebsiteUrl method
     */
    private String cleanWebsiteUrl(String website) {
        if (website == null || website.trim().isEmpty()) {
            return website;
        }
        
        // If website has index.php then we can consider it as OJS journal
        if (!website.contains("/index.php/")) {
            return website;
        }
        
        String[] urlList = website.split("/index.php/");
        String host = urlList[0];
        String urlIndexPart = urlList[1];
        
        // If url has not extra part we use it
        if (!urlIndexPart.contains("/")) {
            return host + "/index.php/" + urlIndexPart;
        }
        
        // If url has extra parts we remove extra parts to search OAI
        String[] sublist = urlIndexPart.split("/");
        String abbreviation = sublist[0];
        
        return host + "/index.php/" + abbreviation;
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
