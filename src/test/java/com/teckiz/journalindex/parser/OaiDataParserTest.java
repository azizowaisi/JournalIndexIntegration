package com.teckiz.journalindex.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for OaiDataParser
 */
@ExtendWith(MockitoExtension.class)
public class OaiDataParserTest {
    
    private OaiDataParser parser;
    private String sampleOaiResponse;
    
    @BeforeEach
    public void setUp() throws IOException {
        parser = new OaiDataParser();
        // Load sample OAI response from test resources
        sampleOaiResponse = new String(Files.readAllBytes(
            Paths.get("src/test/resources/sample-oai-response.xml")));
    }
    
    @Test
    public void testParseListRecordsResponse() {
        List<Map<String, Object>> records = parser.parseListRecordsResponse(sampleOaiResponse);
        
        assertNotNull(records);
        assertEquals(2, records.size());
        
        // Test first record
        Map<String, Object> firstRecord = records.get(0);
        assertEquals("oai:example.com:12345", firstRecord.get("identifier"));
        assertEquals("Journal of Example Research", firstRecord.get("title"));
        assertEquals("John Doe", firstRecord.get("creator"));
        assertEquals("Example Publishing House", firstRecord.get("publisher"));
        assertEquals("Computer Science", firstRecord.get("subject"));
        assertEquals("Journal Article", firstRecord.get("type"));
        assertEquals("en", firstRecord.get("language"));
        
        // Test second record
        Map<String, Object> secondRecord = records.get(1);
        assertEquals("oai:example.com:12346", secondRecord.get("identifier"));
        assertEquals("Advanced Data Structures", secondRecord.get("title"));
        assertEquals("Jane Smith", secondRecord.get("creator"));
        assertEquals("Example Publishing House", secondRecord.get("publisher"));
    }
    
    @Test
    public void testParseEmptyResponse() {
        String emptyResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\"><ListRecords></ListRecords></OAI-PMH>";
        List<Map<String, Object>> records = parser.parseListRecordsResponse(emptyResponse);
        
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }
    
    @Test
    public void testParseInvalidXml() {
        String invalidXml = "This is not valid XML";
        List<Map<String, Object>> records = parser.parseListRecordsResponse(invalidXml);
        
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }
    
    @Test
    public void testToJson() {
        Map<String, Object> testData = Map.of(
            "title", "Test Journal",
            "publisher", "Test Publisher",
            "year", 2024
        );
        
        String json = parser.toJson(testData);
        assertNotNull(json);
        assertTrue(json.contains("Test Journal"));
        assertTrue(json.contains("Test Publisher"));
        assertTrue(json.contains("2024"));
    }
    
    @Test
    public void testParseIdentifyResponse() {
        String identifyResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\">" +
            "<Identify>" +
            "<repositoryName>Example Repository</repositoryName>" +
            "<baseURL>https://example.com/oai</baseURL>" +
            "<protocolVersion>2.0</protocolVersion>" +
            "<adminEmail>admin@example.com</adminEmail>" +
            "<earliestDatestamp>2020-01-01</earliestDatestamp>" +
            "<deletedRecord>no</deletedRecord>" +
            "<granularity>YYYY-MM-DD</granularity>" +
            "<metadataFormat>" +
            "<metadataPrefix>oai_dc</metadataPrefix>" +
            "<schema>http://www.openarchives.org/OAI/2.0/oai_dc.xsd</schema>" +
            "<metadataNamespace>http://www.openarchives.org/OAI/2.0/oai_dc/</metadataNamespace>" +
            "</metadataFormat>" +
            "</Identify>" +
            "</OAI-PMH>";
        
        Map<String, Object> result = parser.parseIdentifyResponse(identifyResponse);
        
        assertNotNull(result);
        assertEquals("Example Repository", result.get("repositoryName"));
        assertEquals("https://example.com/oai", result.get("baseURL"));
        assertEquals("2.0", result.get("protocolVersion"));
        assertEquals("admin@example.com", result.get("adminEmail"));
        assertEquals("2020-01-01", result.get("earliestDatestamp"));
        assertEquals("no", result.get("deletedRecord"));
        assertEquals("YYYY-MM-DD", result.get("granularity"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, String>> metadataFormats = (List<Map<String, String>>) result.get("metadataFormats");
        assertNotNull(metadataFormats);
        assertEquals(1, metadataFormats.size());
        assertEquals("oai_dc", metadataFormats.get(0).get("metadataPrefix"));
    }
}
