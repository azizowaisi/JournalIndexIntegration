package com.teckiz.journalindex;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Local server for testing Lambda function
 * This creates a REST API that mimics the Lambda function behavior
 */
@SpringBootApplication
@RestController
@RequestMapping("/api")
public class LocalServer {

    private static LambdaHandler lambdaHandler;
    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        System.out.println("=== Starting Local Lambda Server ===");
        
        // Start Spring Boot application
        context = SpringApplication.run(LocalServer.class, args);
        
        // Initialize Lambda handler
        lambdaHandler = new LambdaHandler();
        
        System.out.println("Local server started at: http://localhost:8080");
        System.out.println("API endpoints:");
        System.out.println("  POST /api/process-sqs - Process SQS message");
        System.out.println("  GET  /api/health - Health check");
        System.out.println("  GET  /api/test - Test with sample data");
        System.out.println("=====================================");
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Journal Index Integration Local Server");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * Test endpoint with sample data
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        System.out.println("=== Processing Test Request ===");
        
        try {
            // Create test SQS event
            SQSEvent sqsEvent = createTestSQSEvent();
            Context lambdaContext = createMockContext();
            
            // Process the event
            String result = lambdaHandler.handleRequest(sqsEvent, lambdaContext);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("result", result);
            response.put("message", "Test processed successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            System.out.println("Test completed successfully");
            return response;
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return response;
        }
    }

    /**
     * Process SQS message endpoint
     */
    @PostMapping("/process-sqs")
    public Map<String, Object> processSQS(@RequestBody Map<String, Object> requestBody) {
        System.out.println("=== Processing SQS Message ===");
        System.out.println("Request body: " + requestBody);
        
        try {
            // Convert request body to SQS event
            SQSEvent sqsEvent = convertToSQSEvent(requestBody);
            Context lambdaContext = createMockContext();
            
            // Process the event
            String result = lambdaHandler.handleRequest(sqsEvent, lambdaContext);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("result", result);
            response.put("message", "SQS message processed successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            System.out.println("SQS message processed successfully");
            return response;
            
        } catch (Exception e) {
            System.err.println("SQS processing failed: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return response;
        }
    }

    /**
     * Create test SQS event
     */
    private SQSEvent createTestSQSEvent() {
        SQSEvent sqsEvent = new SQSEvent();
        List<SQSEvent.SQSMessage> records = new ArrayList<>();
        
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("test-message-" + System.currentTimeMillis());
        message.setReceiptHandle("test-receipt-handle");
        message.setBody("{\"journalKey\":\"TEST_JOURNAL_001\",\"companyKey\":\"TEST_COMPANY_001\",\"oaiUrl\":\"https://example.com/oai\",\"metadataPrefix\":\"oai_dc\"}");
        message.setMd5OfBody("test-md5");
        message.setEventSource("aws:sqs");
        message.setEventSourceArn("arn:aws:sqs:us-east-1:123456789012:test-queue");
        message.setAwsRegion("us-east-1");
        
        records.add(message);
        sqsEvent.setRecords(records);
        
        return sqsEvent;
    }

    /**
     * Convert request body to SQS event
     */
    private SQSEvent convertToSQSEvent(Map<String, Object> requestBody) {
        SQSEvent sqsEvent = new SQSEvent();
        List<SQSEvent.SQSMessage> records = new ArrayList<>();
        
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId((String) requestBody.getOrDefault("messageId", "local-message-" + System.currentTimeMillis()));
        message.setReceiptHandle((String) requestBody.getOrDefault("receiptHandle", "local-receipt-handle"));
        message.setBody((String) requestBody.getOrDefault("body", "{}"));
        message.setMd5OfBody((String) requestBody.getOrDefault("md5OfBody", "local-md5"));
        message.setEventSource((String) requestBody.getOrDefault("eventSource", "aws:sqs"));
        message.setEventSourceArn((String) requestBody.getOrDefault("eventSourceArn", "arn:aws:sqs:us-east-1:123456789012:local-queue"));
        message.setAwsRegion((String) requestBody.getOrDefault("awsRegion", "us-east-1"));
        
        records.add(message);
        sqsEvent.setRecords(records);
        
        return sqsEvent;
    }

    /**
     * Create mock Lambda context
     */
    private Context createMockContext() {
        return new Context() {
            @Override
            public String getAwsRequestId() {
                return "local-request-" + System.currentTimeMillis();
            }
            
            @Override
            public String getLogGroupName() {
                return "/aws/lambda/local-server";
            }
            
            @Override
            public String getLogStreamName() {
                return "local-log-stream";
            }
            
            @Override
            public String getFunctionName() {
                return "local-journal-processor";
            }
            
            @Override
            public String getFunctionVersion() {
                return "1";
            }
            
            @Override
            public String getInvokedFunctionArn() {
                return "arn:aws:lambda:us-east-1:123456789012:function:local-journal-processor";
            }
            
            @Override
            public com.amazonaws.services.lambda.runtime.CognitoIdentity getIdentity() {
                return null;
            }
            
            @Override
            public com.amazonaws.services.lambda.runtime.ClientContext getClientContext() {
                return null;
            }
            
            @Override
            public int getRemainingTimeInMillis() {
                return 300000; // 5 minutes
            }
            
            @Override
            public int getMemoryLimitInMB() {
                return 1024;
            }
            
            @Override
            public com.amazonaws.services.lambda.runtime.LambdaLogger getLogger() {
                return new com.amazonaws.services.lambda.runtime.LambdaLogger() {
                    @Override
                    public void log(String message) {
                        System.out.println("[LAMBDA LOG] " + message);
                    }
                    
                    @Override
                    public void log(byte[] message) {
                        System.out.println("[LAMBDA LOG] " + new String(message));
                    }
                };
            }
        };
    }
}
