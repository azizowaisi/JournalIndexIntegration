package com.teckiz.journalindex;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teckiz.journalindex.model.SqsArticleMessage;
import com.teckiz.journalindex.service.JsonArticleProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * AWS Lambda handler for processing SQS messages containing article data
 * Processes JSON messages directly from SQS and saves to MySQL
 */
public class LambdaHandler implements RequestHandler<SQSEvent, String> {
    
    private static final Logger logger = LogManager.getLogger(LambdaHandler.class);
    private static AnnotationConfigApplicationContext springContext;
    private static JsonArticleProcessor articleProcessor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        try {
            logger.info("=== INITIALIZING SPRING CONTEXT ===");
            
            // Initialize Spring context
            springContext = new AnnotationConfigApplicationContext();
            springContext.register(com.teckiz.journalindex.config.ApplicationConfig.class);
            springContext.refresh();
            logger.info("Spring context initialized");
            
            // Get article processor service
            articleProcessor = springContext.getBean(JsonArticleProcessor.class);
            logger.info("JsonArticleProcessor bean retrieved");

            logger.info("Spring context initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Spring context", e);
            throw new RuntimeException("Failed to initialize Spring context", e);
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
                logger.info("Raw Message Body: {}", messageBody.length() > 500 ? messageBody.substring(0, 500) + "..." : messageBody);

                try {
                    // Parse the message body as SqsArticleMessage
                    logger.info("Parsing message body with ObjectMapper...");
                    SqsArticleMessage articleMessage = objectMapper.readValue(messageBody, SqsArticleMessage.class);
                    
                    logger.info("Parsed message data:");
                    logger.info("  journalKey: {}", articleMessage.getJournalKey());
                    logger.info("  messageType: {}", articleMessage.getMessageType());
                    logger.info("  source: {}", articleMessage.getSource());
                    logger.info("  success: {}", articleMessage.getSuccess());
                    
                    if (articleMessage.getArticle() != null) {
                        logger.info("  article.title: {}", articleMessage.getArticle().getTitle());
                        logger.info("  article.creator: {}", articleMessage.getArticle().getCreator());
                    }

                    // Validate required fields
                    if (articleMessage.getJournalKey() == null || articleMessage.getJournalKey().trim().isEmpty()) {
                        logger.warn("No journalKey found in message body, skipping record: {}", message.getMessageId());
                        errorCount++;
                        continue;
                    }

                    if (articleMessage.getMessageType() == null) {
                        logger.warn("No messageType found in message body, skipping record: {}", message.getMessageId());
                        errorCount++;
                        continue;
                    }

                    logger.info("Validation passed - Processing article for journal: {}", articleMessage.getJournalKey());

                    try {
                        long processingStart = System.currentTimeMillis();
                        
                        // Route based on message type
                        String result;
                        if ("Article".equalsIgnoreCase(articleMessage.getMessageType())) {
                            logger.info("Processing single Article message type");
                            result = articleProcessor.processArticle(articleMessage);
                        } else if ("ArticleBatch".equalsIgnoreCase(articleMessage.getMessageType())) {
                            logger.info("Processing ArticleBatch message type with {} articles", 
                                    articleMessage.getArticlesInBatch());
                            result = articleProcessor.processBatch(articleMessage);
                        } else {
                            logger.warn("Unsupported message type: {}", articleMessage.getMessageType());
                            result = "Unsupported message type: " + articleMessage.getMessageType();
                            errorCount++;
                            continue;
                        }
                        
                        long processingEnd = System.currentTimeMillis();
                        logger.info("Processing completed in {} ms", processingEnd - processingStart);
                        logger.info("Result: {}", result);
                        processedCount++;
                        
                    } catch (Exception processingException) {
                        logger.error("Error processing message {}: {}", message.getMessageId(), processingException.getMessage(), processingException);
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
