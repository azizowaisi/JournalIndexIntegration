package com.teckiz.journalindex;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.engine.DefaultProducerTemplate;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

/**
 * Test class for JournalIndexRoute
 */
@ExtendWith(MockitoExtension.class)
public class JournalIndexRouteTest extends CamelTestSupport {
    
    private ProducerTemplate producerTemplate;
    
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        producerTemplate = new DefaultProducerTemplate(context);
        producerTemplate.start();
    }
    
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new JournalIndexRoute();
    }
    
    @Test
    public void testProcessOaiUrl() throws Exception {
        // Test data - simulate SQS message format
        String testUrl = "https://example.com/oai";
        String journalKey = "TEST_JOURNAL";
        
        // Create JSON message body as expected by LambdaHandler
        String messageBody = String.format("{\"url\":\"%s\",\"journal_key\":\"%s\"}", testUrl, journalKey);
        
        // Send message to the route with required headers
        String result = producerTemplate.requestBodyAndHeaders("direct:processWebsite", messageBody, 
            Map.of("websiteUrl", testUrl, "journalKey", journalKey), String.class);
        
        // Verify the result
        assertNotNull(result);
        assertTrue(result.contains("Successfully processed"));
    }
    
    @Test
    public void testInvalidUrl() throws Exception {
        // Test with empty URL - simulate SQS message format
        String emptyUrl = "";
        String journalKey = "TEST_JOURNAL";
        
        // Create JSON message body with empty URL
        String messageBody = String.format("{\"url\":\"%s\",\"journal_key\":\"%s\"}", emptyUrl, journalKey);
        
        try {
            producerTemplate.requestBodyAndHeaders("direct:processWebsite", messageBody, 
                Map.of("websiteUrl", emptyUrl, "journalKey", journalKey), String.class);
            fail("Should have thrown an exception for empty URL");
        } catch (Exception e) {
            // Check if the exception message contains our validation error
            String message = e.getMessage();
            assertTrue(message.contains("Website URL cannot be empty") || 
                      message.contains("IllegalArgumentException") ||
                      (e.getCause() != null && e.getCause().getMessage().contains("Website URL cannot be empty")),
                      "Expected exception message to contain 'Website URL cannot be empty', but got: " + message);
        }
    }
    
    @Test
    public void testUrlNormalization() throws Exception {
        // Test URL without protocol - simulate SQS message format
        String urlWithoutProtocol = "example.com/oai";
        String journalKey = "TEST_JOURNAL";
        
        // Create JSON message body as expected by LambdaHandler
        String messageBody = String.format("{\"url\":\"%s\",\"journal_key\":\"%s\"}", urlWithoutProtocol, journalKey);
        
        // This should be normalized to include https://
        String result = producerTemplate.requestBodyAndHeaders("direct:processWebsite", messageBody, 
            Map.of("websiteUrl", urlWithoutProtocol, "journalKey", journalKey), String.class);
        
        assertNotNull(result);
        assertTrue(result.contains("Successfully processed"));
    }
}
