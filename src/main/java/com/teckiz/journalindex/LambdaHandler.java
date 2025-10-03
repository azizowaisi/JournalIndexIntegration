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
        logger.info("Received SQS event with {} records", sqsEvent.getRecords().size());
        logger.info("SQS Event: {}", sqsEvent);
        logger.info("Records count: {}", sqsEvent.getRecords().size());
        logger.info("Environment variables:");
        logger.info("FUNCTION_TYPE: {}", System.getenv("FUNCTION_TYPE"));
        logger.info("LOG_LEVEL: {}", System.getenv("LOG_LEVEL"));
        
        if (sqsEvent.getRecords() == null || sqsEvent.getRecords().isEmpty()) {
            logger.warn("No SQS records found in event");
            return "No SQS records found";
        }
        
        try {
            for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {
                String messageBody = message.getBody();
                logger.info("Processing message: {}", messageBody);
                
                try {
                    // Parse the message body to extract URL and journal_key
                    Map<String, Object> messageData = objectMapper.readValue(messageBody, Map.class);
                    String websiteUrl = (String) messageData.get("url");
                    String journalKey = (String) messageData.get("journal_key");
                    
                    if (websiteUrl == null || websiteUrl.trim().isEmpty()) {
                        logger.warn("No URL found in message body, skipping record: {}", message.getMessageId());
                        continue;
                    }
                    
                    if (journalKey == null || journalKey.trim().isEmpty()) {
                        logger.warn("No journal_key found in message body, skipping record: {}", message.getMessageId());
                        continue;
                    }
                    
                    logger.info("Processing website URL: {} for journal: {}", websiteUrl, journalKey);
                    
                    // Create headers for Camel route
                    Map<String, Object> headers = new HashMap<>();
                    headers.put("websiteUrl", websiteUrl);
                    headers.put("journalKey", journalKey);
                    headers.put("messageId", message.getMessageId());
                    headers.put("receiptHandle", message.getReceiptHandle());
                    
                    logger.info("Sending message to Camel route with headers: {}", headers);
                    
                    try {
                        // Send message to Camel route for processing
                        String result = producerTemplate.requestBodyAndHeaders("direct:processWebsite", null, headers, String.class);
                        logger.info("Message sent to Camel route successfully. Result: {}", result);
                    } catch (Exception camelException) {
                        logger.error("Error in Camel route processing", camelException);
                    }
                    
                } catch (Exception e) {
                    logger.error("Error parsing message body: {}", messageBody, e);
                }
            }
            
            String result = "Successfully processed " + sqsEvent.getRecords().size() + " messages";
            logger.info("=== LAMBDA FUNCTION COMPLETED: {} ===", result);
            return result;
            
        } catch (Exception e) {
            logger.error("=== LAMBDA FUNCTION ERROR ===", e);
            throw new RuntimeException("Error processing SQS event", e);
        }
    }
}
