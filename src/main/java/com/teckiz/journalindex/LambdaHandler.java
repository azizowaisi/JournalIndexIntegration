package com.teckiz.journalindex;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.engine.DefaultProducerTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * AWS Lambda handler for processing SQS messages containing website URLs
 * and harvesting OAI data to store in MySQL
 */
public class LambdaHandler implements RequestHandler<SQSEvent, String> {
    
    private static final Logger logger = LogManager.getLogger(LambdaHandler.class);
    private static CamelContext camelContext;
    private static ProducerTemplate producerTemplate;
    private static AnnotationConfigApplicationContext springContext;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        try {
            logger.info("=== INITIALIZING SPRING AND CAMEL CONTEXT ===");
            
            // Initialize Spring context
            springContext = new AnnotationConfigApplicationContext();
            springContext.register(com.teckiz.journalindex.config.ApplicationConfig.class);
            springContext.refresh();
            logger.info("Spring context initialized");
            
            // Initialize Camel context
            camelContext = new DefaultCamelContext();
            logger.info("Camel context created");

            // Get JournalIndexRoute from Spring context
            JournalIndexRoute journalIndexRoute = springContext.getBean(JournalIndexRoute.class);
            camelContext.addRoutes(journalIndexRoute);
            logger.info("Routes added to Camel context");

            camelContext.start();
            logger.info("Camel context started");

            // Initialize producer template
            producerTemplate = new DefaultProducerTemplate(camelContext);
            producerTemplate.start();
            logger.info("Producer template started");

            logger.info("Spring and Camel context initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Spring and Camel context", e);
            throw new RuntimeException("Failed to initialize Spring and Camel context", e);
        }
    }
    
    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        logger.info("=== LAMBDA FUNCTION STARTED ===");
        logger.info("Lambda Function Name: {}", context.getFunctionName());
        logger.info("Lambda Function Version: {}", context.getFunctionVersion());
        logger.info("Lambda Request ID: {}", context.getAwsRequestId());
        logger.info("Lambda Remaining Time: {} ms", context.getRemainingTimeInMillis());
        logger.info("Lambda Memory Limit: {} MB", context.getMemoryLimitInMB());
        
        logger.info("Received SQS event with {} records", sqsEvent.getRecords().size());
        logger.info("SQS Event Source ARN: {}", sqsEvent.getRecords().isEmpty() ? "N/A" : sqsEvent.getRecords().get(0).getEventSourceArn());
        logger.info("SQS Event Source: {}", sqsEvent.getRecords().isEmpty() ? "N/A" : sqsEvent.getRecords().get(0).getEventSource());
        logger.info("SQS Region: {}", sqsEvent.getRecords().isEmpty() ? "N/A" : sqsEvent.getRecords().get(0).getAwsRegion());
        
        logger.info("Environment variables:");
        logger.info("FUNCTION_TYPE: {}", System.getenv("FUNCTION_TYPE"));
        logger.info("LOG_LEVEL: {}", System.getenv("LOG_LEVEL"));
        logger.info("DB_URL: {}", System.getenv("DB_URL") != null ? "***CONFIGURED***" : "NOT_SET");
        logger.info("SQS_QUEUE_URL: {}", System.getenv("SQS_QUEUE_URL") != null ? "***CONFIGURED***" : "NOT_SET");
        logger.info("S3_BUCKET_NAME: {}", System.getenv("S3_BUCKET_NAME") != null ? "***CONFIGURED***" : "NOT_SET");

        if (sqsEvent.getRecords() == null || sqsEvent.getRecords().isEmpty()) {
            logger.warn("No SQS records found in event");
            return "No SQS records found";
        }

        int processedCount = 0;
        int errorCount = 0;
        long startTime = System.currentTimeMillis();

        try {
            for (int i = 0; i < sqsEvent.getRecords().size(); i++) {
                SQSEvent.SQSMessage message = sqsEvent.getRecords().get(i);
                logger.info("=== PROCESSING MESSAGE {}/{} ===", i + 1, sqsEvent.getRecords().size());
                logger.info("Message ID: {}", message.getMessageId());
                logger.info("Message Receipt Handle: {}", message.getReceiptHandle());
                logger.info("Message MD5: {}", message.getMd5OfBody());
                logger.info("Message Attributes: {}", message.getMessageAttributes());
                logger.info("Message Body Length: {}", message.getBody().length());
                
                String messageBody = message.getBody();
                logger.info("Raw Message Body: {}", messageBody);

                try {
                    // Parse the message body to extract URL and journal_key
                    logger.info("Parsing message body with ObjectMapper...");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> messageData = objectMapper.readValue(messageBody, Map.class);
                    logger.info("Parsed message data: {}", messageData);
                    
                    String websiteUrl = (String) messageData.get("url");
                    String journalKey = (String) messageData.get("journal_key");
                    
                    logger.info("Extracted websiteUrl: {}", websiteUrl);
                    logger.info("Extracted journalKey: {}", journalKey);

                    if (websiteUrl == null || websiteUrl.trim().isEmpty()) {
                        logger.warn("No URL found in message body, skipping record: {}", message.getMessageId());
                        errorCount++;
                        continue;
                    }

                    if (journalKey == null || journalKey.trim().isEmpty()) {
                        logger.warn("No journal_key found in message body, skipping record: {}", message.getMessageId());
                        errorCount++;
                        continue;
                    }

                    logger.info("Validation passed - Processing website URL: {} for journal: {}", websiteUrl, journalKey);

                    // Create headers for Camel route
                    Map<String, Object> headers = new HashMap<>();
                    headers.put("websiteUrl", websiteUrl);
                    headers.put("journalKey", journalKey);
                    headers.put("messageId", message.getMessageId());
                    headers.put("receiptHandle", message.getReceiptHandle());
                    headers.put("messageIndex", i);
                    headers.put("totalMessages", sqsEvent.getRecords().size());

                    logger.info("Created Camel headers: {}", headers);
                    logger.info("Sending message to Camel route: direct:processWebsite");

                    try {
                        long camelStartTime = System.currentTimeMillis();
                        // Send message to Camel route for processing
                        String result = producerTemplate.requestBodyAndHeaders("direct:processWebsite", null, headers, String.class);
                        long camelEndTime = System.currentTimeMillis();
                        
                        logger.info("Camel route processing completed in {} ms", camelEndTime - camelStartTime);
                        logger.info("Camel route result: {}", result);
                        processedCount++;
                        
                    } catch (Exception camelException) {
                        logger.error("Error in Camel route processing for message {}: {}", message.getMessageId(), camelException.getMessage(), camelException);
                        errorCount++;
                    }

                } catch (Exception e) {
                    logger.error("Error parsing message body for message {}: {}", message.getMessageId(), e.getMessage(), e);
                    errorCount++;
                }
            }

            long endTime = System.currentTimeMillis();
            String result = String.format("Successfully processed %d/%d messages (%d errors) in %d ms", 
                                        processedCount, sqsEvent.getRecords().size(), errorCount, endTime - startTime);
            logger.info("=== LAMBDA FUNCTION COMPLETED: {} ===", result);
            return result;

        } catch (Exception e) {
            logger.error("=== LAMBDA FUNCTION ERROR ===", e);
            throw new RuntimeException("Error processing SQS event", e);
        }
    }
}
