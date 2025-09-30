package com.teckiz.journalindex.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for OAI-PMH XML data
 */
public class OaiDataParser {
    
    private static final Logger logger = LogManager.getLogger(OaiDataParser.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Parse OAI Identify response to extract repository information
     */
    public Map<String, Object> parseIdentifyResponse(String xmlData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Document doc = parseXml(xmlData);
            Element root = doc.getDocumentElement();
            
            // Extract repository information
            result.put("repositoryName", getTextContent(root, "repositoryName"));
            result.put("baseURL", getTextContent(root, "baseURL"));
            result.put("protocolVersion", getTextContent(root, "protocolVersion"));
            result.put("adminEmail", getTextContent(root, "adminEmail"));
            result.put("earliestDatestamp", getTextContent(root, "earliestDatestamp"));
            result.put("deletedRecord", getTextContent(root, "deletedRecord"));
            result.put("granularity", getTextContent(root, "granularity"));
            
            // Extract available metadata formats
            List<Map<String, String>> metadataFormats = new ArrayList<>();
            NodeList formatNodes = root.getElementsByTagName("metadataFormat");
            for (int i = 0; i < formatNodes.getLength(); i++) {
                Element formatElement = (Element) formatNodes.item(i);
                Map<String, String> format = new HashMap<>();
                format.put("metadataPrefix", getTextContent(formatElement, "metadataPrefix"));
                format.put("schema", getTextContent(formatElement, "schema"));
                format.put("metadataNamespace", getTextContent(formatElement, "metadataNamespace"));
                metadataFormats.add(format);
            }
            result.put("metadataFormats", metadataFormats);
            
        } catch (Exception e) {
            logger.error("Error parsing OAI Identify response", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Parse OAI ListRecords response to extract journal records
     */
    public List<Map<String, Object>> parseListRecordsResponse(String xmlData) {
        List<Map<String, Object>> records = new ArrayList<>();
        
        try {
            Document doc = parseXml(xmlData);
            NodeList recordNodes = doc.getElementsByTagName("record");
            
            for (int i = 0; i < recordNodes.getLength(); i++) {
                Element recordElement = (Element) recordNodes.item(i);
                Map<String, Object> record = parseRecord(recordElement);
                if (record != null) {
                    records.add(record);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error parsing OAI ListRecords response", e);
        }
        
        return records;
    }
    
    /**
     * Parse individual OAI record
     */
    private Map<String, Object> parseRecord(Element recordElement) {
        Map<String, Object> record = new HashMap<>();
        
        try {
            // Extract header information
            Element headerElement = (Element) recordElement.getElementsByTagName("header").item(0);
            if (headerElement != null) {
                record.put("identifier", getTextContent(headerElement, "identifier"));
                record.put("datestamp", getTextContent(headerElement, "datestamp"));
                record.put("setSpec", getTextContent(headerElement, "setSpec"));
                record.put("status", headerElement.getAttribute("status"));
            }
            
            // Extract metadata
            Element metadataElement = (Element) recordElement.getElementsByTagName("metadata").item(0);
            if (metadataElement != null) {
                // Try to find oai_dc:dc element first, then fall back to dc
                Element dcElement = (Element) metadataElement.getElementsByTagName("oai_dc:dc").item(0);
                if (dcElement == null) {
                    dcElement = (Element) metadataElement.getElementsByTagName("dc").item(0);
                }
                
                if (dcElement != null) {
                    record.put("title", getTextContent(dcElement, "title"));
                    record.put("creator", getTextContent(dcElement, "creator"));
                    record.put("subject", getTextContent(dcElement, "subject"));
                    record.put("description", getTextContent(dcElement, "description"));
                    record.put("publisher", getTextContent(dcElement, "publisher"));
                    record.put("date", getTextContent(dcElement, "date"));
                    record.put("type", getTextContent(dcElement, "type"));
                    record.put("format", getTextContent(dcElement, "format"));
                    record.put("language", getTextContent(dcElement, "language"));
                    record.put("rights", getTextContent(dcElement, "rights"));
                    record.put("metadataIdentifier", getTextContent(dcElement, "identifier"));
                }
            }
            
        } catch (Exception e) {
            logger.error("Error parsing OAI record", e);
            return null;
        }
        
        return record;
    }
    
    /**
     * Parse XML string to Document
     */
    private Document parseXml(String xmlData) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlData.getBytes("UTF-8")));
    }
    
    /**
     * Get text content from XML element
     */
    private String getTextContent(Element parent, String tagName) {
        // Try namespaced version first (dc:title, dc:creator, etc.)
        NodeList nodes = parent.getElementsByTagName("dc:" + tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        
        // Try non-namespaced version
        nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        
        return null;
    }
    
    /**
     * Convert parsed data to JSON string
     */
    public String toJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            logger.error("Error converting to JSON", e);
            return "{}";
        }
    }
}
