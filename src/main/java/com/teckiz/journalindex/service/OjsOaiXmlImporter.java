package com.teckiz.journalindex.service;

import com.teckiz.journalindex.entity.Company;
import com.teckiz.journalindex.entity.IndexJournal;
import com.teckiz.journalindex.entity.IndexJournalArticle;
import com.teckiz.journalindex.entity.IndexJournalAuthor;
import com.teckiz.journalindex.entity.IndexJournalSetting;
import com.teckiz.journalindex.model.ArticleAuthorModel;
import com.teckiz.journalindex.model.ArticleModel;
import com.teckiz.journalindex.repository.IndexJournalArticleRepository;
import com.teckiz.journalindex.repository.IndexJournalRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for importing OJS OAI XML data
 * Based on PHP OjsOaiXmlImport functionality
 */
@Service
public class OjsOaiXmlImporter {
    
    private static final Logger logger = LogManager.getLogger(OjsOaiXmlImporter.class);
    
    // OAI-DC namespaces
    private static final String OAI_DC_NAMESPACE = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    private static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";
    
    @Autowired
    private IndexJournalRepository journalRepository;
    
    @Autowired
    private IndexJournalArticleRepository articleRepository;
    
    /**
     * Import OJS OAI Identify data directly
     * Updates journal settings with OAI repository information
     */
    @Transactional
    public void importIdentifyDirectly(String journalKey, String xmlData) {
        try {
            logger.info("=== Starting OJS OAI Identify Import ===");
            logger.info("Journal Key: {}", journalKey);
            
            if (xmlData == null || xmlData.trim().isEmpty()) {
                throw new IllegalArgumentException("XML data is empty");
            }
            
            // Parse XML
            Document doc = parseXml(xmlData);
            
            // Get Identify element
            NodeList identifyNodes = doc.getElementsByTagName("Identify");
            if (identifyNodes.getLength() == 0) {
                throw new IllegalArgumentException("Identify element not found in XML");
            }
            
            Element identify = (Element) identifyNodes.item(0);
            
            // Get journal
            IndexJournal journal = journalRepository.findByJournalKey(journalKey)
                    .orElseThrow(() -> new IllegalArgumentException("Journal not found: " + journalKey));
            
            // Check if journal is approved
            if (!IndexJournal.RJ_APPROVED.equals(journal.getStatus())) {
                logger.warn("Journal is not approved, status: {}", journal.getStatus());
                return;
            }
            
            IndexJournalSetting setting = journal.getSetting();
            if (setting == null) {
                logger.warn("Journal setting not found, skipping Identify import");
                return;
            }
            
            if (!Boolean.TRUE.equals(setting.getArticleIndex())) {
                logger.warn("Journal is not indexed for articles, skipping");
                return;
            }
            
            // Extract repository information
            String repositoryName = getElementText(identify, "repositoryName");
            
            // Get description element and navigate to oai-identifier
            NodeList descriptionNodes = identify.getElementsByTagName("description");
            if (descriptionNodes.getLength() > 0) {
                Element description = (Element) descriptionNodes.item(0);
                NodeList oaiIdentifierNodes = description.getElementsByTagName("oai-identifier");
                
                if (oaiIdentifierNodes.getLength() > 0) {
                    Element oaiIdentifier = (Element) oaiIdentifierNodes.item(0);
                    
                    String scheme = getElementText(oaiIdentifier, "scheme");
                    String repositoryIdentifier = getElementText(oaiIdentifier, "repositoryIdentifier");
                    String delimiter = getElementText(oaiIdentifier, "delimiter");
                    String sampleIdentifier = getElementText(oaiIdentifier, "sampleIdentifier");
                    
                    // Update journal setting
                    setting.setOaiScheme(scheme);
                    setting.setRepositoryName(repositoryName);
                    setting.setRepositoryScheme(repositoryIdentifier);
                    setting.setDelimiter(delimiter);
                    setting.setSampleOAIIdentifier(sampleIdentifier);
                    setting.setIntegratedAt(LocalDateTime.now());
                    
                    logger.info("✅ Updated journal setting with OAI Identify data");
                    logger.info("Repository: {}, Scheme: {}", repositoryName, scheme);
                }
            }
            
            logger.info("=== OJS OAI Identify Import Completed ===");
            
        } catch (Exception e) {
            logger.error("Error importing OJS OAI Identify data for journal: {}", journalKey, e);
            throw new RuntimeException("Failed to import OJS OAI Identify data", e);
        }
    }
    
    /**
     * Import OJS OAI Records data directly
     * Processes all records in XML and saves articles to database
     * Returns number of articles processed
     */
    @Transactional
    public int importRecordsDirectly(String journalKey, String xmlData) {
        try {
            logger.info("=== Starting OJS OAI Records Import ===");
            logger.info("Journal Key: {}", journalKey);
            
            if (xmlData == null || xmlData.trim().isEmpty()) {
                throw new IllegalArgumentException("XML data is empty");
            }
            
            logger.info("XML data length: {} characters", xmlData.length());
            
            // Parse XML
            Document doc = parseXml(xmlData);
            
            // Check for required namespaces
            Element root = doc.getDocumentElement();
            logger.info("Root element: {}", root.getNodeName());
            
            // Get ListRecords element
            NodeList listRecordsNodes = doc.getElementsByTagName("ListRecords");
            if (listRecordsNodes.getLength() == 0) {
                throw new IllegalArgumentException("ListRecords element not found in XML");
            }
            
            Element listRecords = (Element) listRecordsNodes.item(0);
            
            // Get all record elements
            NodeList recordNodes = listRecords.getElementsByTagName("record");
            int totalRecords = recordNodes.getLength();
            
            logger.info("Found {} records in XML", totalRecords);
            logger.info("Processing ALL records in one run");
            
            // Process ALL records
            int processedCount = 0;
            for (int i = 0; i < totalRecords; i++) {
                try {
                    Element recordElement = (Element) recordNodes.item(i);
                    ArticleModel article = parseArticle(recordElement);
                    
                    if (article != null && "update".equals(article.getStatus())) {
                        logger.info("Article {}/{}: {} (ID: {})", 
                                (i + 1), totalRecords, article.getTitle(), article.getPublisherRecordId());
                        
                        // Save article to database
                        saveArticle(journalKey, article);
                        
                        processedCount++;
                        
                        // Log progress every 10 records
                        if ((i + 1) % 10 == 0) {
                            logger.info("Progress: {}/{} articles processed", i + 1, totalRecords);
                        }
                    } else {
                        logger.info("Skipping record {}/{}: status={}", 
                                (i + 1), totalRecords, article != null ? article.getStatus() : "null");
                    }
                    
                } catch (Exception e) {
                    logger.error("Error processing record {}: {}", i, e.getMessage(), e);
                }
            }
            
            logger.info("=== OJS OAI Records Import Completed ===");
            logger.info("Processed {} articles out of {} total records", processedCount, totalRecords);
            
            return processedCount;
            
        } catch (Exception e) {
            logger.error("Error importing OJS OAI Records data for journal: {}", journalKey, e);
            throw new RuntimeException("Failed to import OJS OAI Records data", e);
        }
    }
    
    /**
     * Parse XML string to Document
     */
    private Document parseXml(String xmlData) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlData.getBytes("UTF-8")));
    }
    
    /**
     * Parse a record element to ArticleModel
     */
    private ArticleModel parseArticle(Element recordElement) {
        try {
            ArticleModel article = new ArticleModel();
            
            // Get header element
            NodeList headerNodes = recordElement.getElementsByTagName("header");
            if (headerNodes.getLength() == 0) {
                logger.warn("No header element found in record");
                return null;
            }
            
            Element header = (Element) headerNodes.item(0);
            
            // Check status attribute
            String status = header.getAttribute("status");
            if (status != null && !status.isEmpty()) {
                article.setStatus(status);
            }
            
            // Get identifier
            String identifier = getElementText(header, "identifier");
            if (identifier != null) {
                // Extract publisher record ID (part after last /)
                int lastSlash = identifier.lastIndexOf('/');
                if (lastSlash >= 0) {
                    String publisherRecordId = identifier.substring(lastSlash + 1).replace("/", "");
                    article.setPublisherRecordId(publisherRecordId);
                }
            }
            
            // If status is not update, return early
            if (!"update".equals(article.getStatus())) {
                return article;
            }
            
            // Get metadata element
            NodeList metadataNodes = recordElement.getElementsByTagName("metadata");
            if (metadataNodes.getLength() == 0) {
                logger.warn("No metadata element found in record");
                return article;
            }
            
            Element metadata = (Element) metadataNodes.item(0);
            
            // Get oai_dc:dc element
            NodeList dcNodes = metadata.getElementsByTagNameNS(OAI_DC_NAMESPACE, "dc");
            if (dcNodes.getLength() == 0) {
                logger.warn("No oai_dc:dc element found in metadata");
                return article;
            }
            
            Element dcElement = (Element) dcNodes.item(0);
            
            // Extract DC fields
            article.setTitle(getElementTextNS(dcElement, DC_NAMESPACE, "title"));
            article.setAbstractText(getElementTextNS(dcElement, DC_NAMESPACE, "description"));
            article.setPageUrl(getElementTextNS(dcElement, DC_NAMESPACE, "identifier"));
            
            // Extract and parse date
            String dateStr = getElementTextNS(dcElement, DC_NAMESPACE, "date");
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    article.setPublishedAt(parseDate(dateStr));
                } catch (Exception e) {
                    logger.warn("Could not parse date: {}", dateStr);
                }
            }
            
            // Extract authors (dc:creator - multiple)
            List<String> creators = getElementTextsNS(dcElement, DC_NAMESPACE, "creator");
            article.setAuthors(parseAuthors(creators));
            
            // Extract subjects/keywords (dc:subject - multiple)
            List<String> subjects = getElementTextsNS(dcElement, DC_NAMESPACE, "subject");
            article.setKeywords(joinSubjects(subjects));
            
            // Extract source (volume details)
            List<String> sources = getElementTextsNS(dcElement, DC_NAMESPACE, "source");
            if (!sources.isEmpty()) {
                String source = sources.get(0);
                article.setVolumeNumber(extractVolume(source));
                article.setPages(extractPages(source));
            }
            
            return article;
            
        } catch (Exception e) {
            logger.error("Error parsing article from record", e);
            return null;
        }
    }
    
    /**
     * Get text content of first element with tag name
     */
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }
    
    /**
     * Get text content of first element with namespace and tag name
     */
    private String getElementTextNS(Element parent, String namespace, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(namespace, localName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }
    
    /**
     * Get text contents of all elements with namespace and tag name
     */
    private List<String> getElementTextsNS(Element parent, String namespace, String localName) {
        List<String> texts = new ArrayList<>();
        NodeList nodes = parent.getElementsByTagNameNS(namespace, localName);
        for (int i = 0; i < nodes.getLength(); i++) {
            String text = nodes.item(i).getTextContent().trim();
            if (!text.isEmpty()) {
                texts.add(text);
            }
        }
        return texts;
    }
    
    /**
     * Parse authors from creator list
     */
    private List<ArticleAuthorModel> parseAuthors(List<String> creators) {
        List<ArticleAuthorModel> authors = new ArrayList<>();
        for (String creator : creators) {
            if (creator != null && !creator.trim().isEmpty()) {
                authors.add(new ArticleAuthorModel(creator.trim()));
            }
        }
        return authors;
    }
    
    /**
     * Join subjects into semicolon-separated string
     */
    private String joinSubjects(List<String> subjects) {
        if (subjects.isEmpty()) {
            return "";
        }
        return String.join("; ", subjects);
    }
    
    /**
     * Extract volume number from source string
     * Format: "Journal Name; Vol X No Y; Pages"
     */
    private String extractVolume(String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        
        String[] parts = source.split(";");
        if (parts.length < 2) {
            return "";
        }
        
        String volumePart = parts[1].trim();
        
        // Remove everything after : or (
        if (volumePart.contains(":")) {
            volumePart = volumePart.substring(0, volumePart.indexOf(":"));
        }
        if (volumePart.contains("(")) {
            volumePart = volumePart.substring(0, volumePart.indexOf("("));
        }
        
        return volumePart.trim();
    }
    
    /**
     * Extract pages from source string
     */
    private String extractPages(String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        
        String[] parts = source.split(";");
        if (parts.length < 3) {
            return "";
        }
        
        return parts[2].trim();
    }
    
    /**
     * Parse date string to LocalDateTime
     */
    private LocalDateTime parseDate(String dateStr) {
        try {
            // Try ISO format first
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e1) {
            try {
                // Try date only
                return LocalDateTime.parse(dateStr + "T00:00:00");
            } catch (Exception e2) {
                logger.warn("Could not parse date: {}", dateStr);
                return LocalDateTime.now();
            }
        }
    }
    
    /**
     * Save article to database
     * Based on PHP CreateArticleHelper::updateArticle
     */
    private void saveArticle(String journalKey, ArticleModel articleModel) {
        try {
            logger.debug("=== Saving Article to Database ===");
            logger.debug("Publisher Record ID: {}", articleModel.getPublisherRecordId());
            logger.debug("Title: {}", articleModel.getTitle());
            
            // Get journal
            IndexJournal journal = journalRepository.findByJournalKey(journalKey)
                    .orElseThrow(() -> new IllegalArgumentException("Journal not found: " + journalKey));
            
            logger.debug("Found journal: {}", journal.getName());
            
            // Check if journal is approved
            if (!IndexJournal.RJ_APPROVED.equals(journal.getStatus())) {
                logger.warn("Journal is not approved, status: {}", journal.getStatus());
                return;
            }
            
            // Check if journal setting exists and is indexed
            if (journal.getSetting() == null) {
                logger.warn("Journal setting not found for journal: {}", journal.getJournalKey());
                return;
            }
            
            // Get company from journal
            Company company = journal.getCompany();
            if (company == null) {
                logger.warn("Company not found for journal: {}", journal.getJournalKey());
                return;
            }
            
            // Find or create article
            IndexJournalArticle article = articleRepository.findByPublisherRecordId(articleModel.getPublisherRecordId())
                    .orElse(new IndexJournalArticle());
            
            boolean isNewArticle = article.getId() == null;
            
            // Set article properties
            article.setPublisherRecordId(articleModel.getPublisherRecordId());
            article.setTitle(articleModel.getTitle());
            article.setAbstractText(articleModel.getAbstractText());
            article.setKeywords(articleModel.getKeywords());
            article.setPageURL(articleModel.getPageUrl());
            article.setPages(articleModel.getPages());
            article.setPublishedAt(articleModel.getPublishedAt());
            article.setCompany(company);
            
            // Handle volume if specified
            if (articleModel.getVolumeNumber() != null && !articleModel.getVolumeNumber().isEmpty()) {
                // For now, just log - volume creation can be added later
                logger.debug("Volume: {}", articleModel.getVolumeNumber());
            }
            
            // Save article
            article = articleRepository.save(article);
            
            logger.info("✅ {} article ID {} - Title: {}", 
                    isNewArticle ? "Created" : "Updated", 
                    article.getId(), 
                    article.getTitle());
            
            // Save authors
            saveAuthors(article, articleModel.getAuthors());
            
        } catch (Exception e) {
            logger.error("Error saving article: {}", articleModel.getPublisherRecordId(), e);
            throw new RuntimeException("Failed to save article", e);
        }
    }
    
    /**
     * Save authors for an article
     */
    private void saveAuthors(IndexJournalArticle article, List<ArticleAuthorModel> authorModels) {
        if (authorModels == null || authorModels.isEmpty()) {
            logger.debug("No authors to save");
            return;
        }
        
        logger.debug("Saving {} authors for article ID {}", authorModels.size(), article.getId());
        
        // Clear existing authors (simplification - could be optimized)
        article.getAuthors().clear();
        
        // Add new authors
        for (ArticleAuthorModel authorModel : authorModels) {
            IndexJournalAuthor author = new IndexJournalAuthor();
            author.setName(authorModel.getName());
            author.setIndexJournalArticle(article);
            
            article.getAuthors().add(author);
            
            logger.debug("Added author: {}", authorModel.getName());
        }
        
        logger.info("✅ Saved {} authors", authorModels.size());
    }
}
