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
        // Test data
        String testUrl = "https://example.com/oai";
        
        // Send message to the route with required headers
        String result = producerTemplate.requestBodyAndHeader("direct:processWebsite", testUrl, "journalKey", "TEST_JOURNAL", String.class);
        
        // Verify the result
        assertNotNull(result);
        assertTrue(result.contains("Successfully processed"));
    }
    
    @Test
    public void testInvalidUrl() throws Exception {
        // Test with empty URL
        String emptyUrl = "";
        
        try {
            producerTemplate.requestBodyAndHeader("direct:processWebsite", emptyUrl, "journalKey", "TEST_JOURNAL", String.class);
            fail("Should have thrown an exception for empty URL");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Website URL cannot be empty"));
        }
    }
    
    @Test
    public void testUrlNormalization() throws Exception {
        // Test URL without protocol
        String urlWithoutProtocol = "example.com/oai";
        
        // This should be normalized to include https://
        String result = producerTemplate.requestBodyAndHeader("direct:processWebsite", urlWithoutProtocol, "journalKey", "TEST_JOURNAL", String.class);
        
        assertNotNull(result);
        assertTrue(result.contains("Successfully processed"));
    }
}
