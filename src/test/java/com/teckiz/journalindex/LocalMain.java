package com.teckiz.journalindex;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.teckiz.journalindex.LambdaHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class for running Lambda function locally
 * This allows you to test the Lambda function without deploying to AWS
 */
public class LocalMain {

    public static void main(String[] args) {
        System.out.println("=== Starting Local Lambda Test ===");
        
        try {
            // Start Spring Boot application
            ConfigurableApplicationContext context = SpringApplication.run(LambdaHandler.class, args);
            
            // Create Lambda handler instance
            LambdaHandler handler = new LambdaHandler();
            
            // Create mock SQS event
            SQSEvent sqsEvent = createMockSQSEvent();
            
            // Create mock context
            Context lambdaContext = createMockContext();
            
            // Call the Lambda function
            System.out.println("Calling Lambda function...");
            String result = handler.handleRequest(sqsEvent, lambdaContext);
            System.out.println("Lambda execution result: " + result);
            
            // Close Spring context
            context.close();
            
            System.out.println("=== Local Lambda Test Completed Successfully ===");
            
        } catch (Exception e) {
            System.err.println("Lambda execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a mock SQS event for testing
     */
    private static SQSEvent createMockSQSEvent() {
        SQSEvent sqsEvent = new SQSEvent();
        List<SQSEvent.SQSMessage> records = new ArrayList<>();
        
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("test-message-id-123");
        message.setReceiptHandle("test-receipt-handle");
        message.setBody("{\"journalKey\":\"TEST_JOURNAL_001\",\"companyKey\":\"TEST_COMPANY_001\",\"oaiUrl\":\"https://example.com/oai\",\"metadataPrefix\":\"oai_dc\"}");
        message.setMd5OfBody("test-md5");
        message.setEventSource("aws:sqs");
        message.setEventSourceArn("arn:aws:sqs:us-east-1:123456789012:test-queue");
        message.setAwsRegion("us-east-1");
        
        records.add(message);
        sqsEvent.setRecords(records);
        
        System.out.println("Created mock SQS event with message: " + message.getBody());
        return sqsEvent;
    }
    
    /**
     * Create a mock Lambda context
     */
    private static Context createMockContext() {
        return new Context() {
            @Override
            public String getAwsRequestId() {
                return "test-request-id-123";
            }
            
            @Override
            public String getLogGroupName() {
                return "/aws/lambda/test-function";
            }
            
            @Override
            public String getLogStreamName() {
                return "test-log-stream";
            }
            
            @Override
            public String getFunctionName() {
                return "test-journal-processor";
            }
            
            @Override
            public String getFunctionVersion() {
                return "1";
            }
            
            @Override
            public String getInvokedFunctionArn() {
                return "arn:aws:lambda:us-east-1:123456789012:function:test-journal-processor";
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
