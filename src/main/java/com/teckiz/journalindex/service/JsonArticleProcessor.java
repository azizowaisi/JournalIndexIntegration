package com.teckiz.journalindex.service;

import com.teckiz.journalindex.dao.ArticleDao;
import com.teckiz.journalindex.dao.AuthorDao;
import com.teckiz.journalindex.dao.JournalDao;
import com.teckiz.journalindex.dao.VolumeDao;
import com.teckiz.journalindex.entity.*;
import com.teckiz.journalindex.model.SqsArticleMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to process JSON articles from SQS messages
 * Lightweight implementation without Spring Framework
 */
public class JsonArticleProcessor {
    
    private static final Logger logger = LogManager.getLogger(JsonArticleProcessor.class);
    
    /**
     * Process batch of articles from SQS JSON message
     */
    public String processBatch(SqsArticleMessage message) {
        try {
            logger.info("Processing article batch from JSON message");
            logger.info("Journal Key: {}", message.getJournalKey());
            logger.info("Message Type: {}", message.getMessageType());
            logger.info("Batch Number: {}/{}", message.getBatchNumber(), message.getTotalBatches());
            logger.info("Articles in Batch: {}", message.getArticlesInBatch());
            
            if (message.getArticles() == null || message.getArticles().isEmpty()) {
                logger.warn("No articles found in batch message");
                return "No articles found in batch";
            }
            
            int processedCount = 0;
            int errorCount = 0;
            
            for (int i = 0; i < message.getArticles().size(); i++) {
                try {
                    logger.info("Processing article {}/{} in batch", i + 1, message.getArticles().size());
                    SqsArticleMessage.ArticleData articleData = message.getArticles().get(i);
                    processArticleData(message.getJournalKey(), message.getOaiUrl(), articleData);
                    processedCount++;
                } catch (Exception e) {
                    logger.error("Error processing article {}/{}: {}", i + 1, message.getArticles().size(), e.getMessage(), e);
                    errorCount++;
                }
            }
            
            String result = String.format("Batch processed: %d success, %d errors out of %d articles", 
                                        processedCount, errorCount, message.getArticles().size());
            logger.info(result);
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process batch: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process a single article from SQS JSON message
     */
    public String processArticle(SqsArticleMessage message) {
        try {
            logger.info("Processing single article from JSON message");
            logger.info("Journal Key: {}", message.getJournalKey());
            logger.info("Message Type: {}", message.getMessageType());
            
            if (message.getArticle() == null) {
                logger.warn("No article data found in message");
                return "No article data found";
            }
            
            SqsArticleMessage.ArticleData articleData = message.getArticle();
            processArticleData(message.getJournalKey(), message.getOaiUrl(), articleData);
            return "Article processed successfully: " + articleData.getTitle();
            
        } catch (Exception e) {
            logger.error("Error processing article from JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process article: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process article data and save to database
     */
    private void processArticleData(String journalKey, String oaiUrl, SqsArticleMessage.ArticleData articleData) {
        try {
            logger.info("Processing article: {}", articleData.getTitle());
            
            // Find or create the journal
            IndexJournal journal = JournalDao.findOrCreateByJournalKey(
                    journalKey, 
                    oaiUrl, 
                    articleData.getPublisher());
            
            logger.info("Using journal ID: {}", journal.getId());
            
            // Find or create volume if we have volume information
            IndexJournalVolume volume = null;
            if (articleData.getSources() != null && !articleData.getSources().isEmpty()) {
                String volumeNumber = extractVolume(articleData.getSources().get(0));
                if (volumeNumber != null) {
                    // Try to find existing volume by journal and volume number
                    volume = VolumeDao.findOrCreateByJournalIdAndVolumeNumber(journal.getId(), volumeNumber);
                    logger.info("Using volume ID: {}", volume.getId());
                }
            }
            
            // Check if article already exists by identifier URL
            IndexJournalArticle existingArticle = null;
            if (articleData.getIdentifier() != null) {
                existingArticle = ArticleDao.findByPageURL(articleData.getIdentifier()).orElse(null);
            }
            
            IndexJournalArticle article;
            if (existingArticle != null) {
                logger.info("Updating existing article ID: {}", existingArticle.getId());
                article = existingArticle;
            } else {
                logger.info("Creating new article");
                article = new IndexJournalArticle();
            }
            
            // Set article fields from JSON
            article.setTitle(articleData.getTitle());
            article.setAbstractText(articleData.getDescription());
            article.setPageURL(articleData.getIdentifier());
            
            // Set volume relationship
            if (volume != null) {
                article.setIndexJournalVolume(volume);
            }
            
            // Set company from journal (if journal has company)
            if (journal.getCompany() != null) {
                article.setCompany(journal.getCompany());
            }
            
            // Parse and set published date
            if (articleData.getDate() != null && !articleData.getDate().isEmpty()) {
                article.setPublishedAt(parseDate(articleData.getDate()));
            }
            
            // Extract pages from sources
            if (articleData.getSources() != null && !articleData.getSources().isEmpty()) {
                String pages = extractPages(articleData.getSources().get(0));
                article.setPages(pages);
            }
            
            // Set keywords from subjects
            if (articleData.getSubjects() != null && !articleData.getSubjects().isEmpty()) {
                article.setKeywords(String.join(", ", articleData.getSubjects()));
            }
            
            // Set article type from types
            if (articleData.getTypes() != null && !articleData.getTypes().isEmpty()) {
                article.setArticleType(articleData.getTypes().get(0));
            }
            
            // Set DOI if available (extract from identifier if it contains DOI)
            if (articleData.getIdentifier() != null && articleData.getIdentifier().contains("doi.org/")) {
                String doi = articleData.getIdentifier().substring(articleData.getIdentifier().lastIndexOf("doi.org/") + 8);
                article.setDoi(doi);
            }
            
            // Save article
            article = ArticleDao.save(article);
            logger.info("✅ Article saved with ID: {}", article.getId());
            
            // Process authors
            if (articleData.getCreator() != null && !articleData.getCreator().isEmpty()) {
                processAuthors(article, articleData.getCreator());
            }
            
            logger.info("Article processing completed: {}", article.getTitle());
            
        } catch (Exception e) {
            logger.error("Error processing article data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process article data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process authors from creator field
     */
    private void processAuthors(IndexJournalArticle article, String creatorString) {
        try {
            // Split by comma or semicolon
            String[] authorNames = creatorString.split("[,;]");
            List<String> authorNameList = new ArrayList<>();
            
            for (String authorName : authorNames) {
                authorName = authorName.trim();
                if (!authorName.isEmpty()) {
                    authorNameList.add(authorName);
                    logger.debug("Added author: {}", authorName);
                }
            }
            
            // Save authors using DAO
            if (!authorNameList.isEmpty()) {
                AuthorDao.saveAuthors(article.getId(), authorNameList);
                logger.info("✅ Saved {} authors", authorNameList.size());
            }
            
        } catch (Exception e) {
            logger.error("Error processing authors: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Parse date string in various formats
     */
    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try ISO date format (2009-06-30)
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDateTime.parse(dateStr + "T00:00:00");
            }
            
            // Try ISO timestamp format (2025-10-10T07:58:10.789Z)
            if (dateStr.contains("T")) {
                // Remove Z and milliseconds if present
                String cleanDate = dateStr.replace("Z", "").split("\\.")[0];
                return LocalDateTime.parse(cleanDate);
            }
            
            // Try year only
            if (dateStr.matches("\\d{4}")) {
                return LocalDateTime.parse(dateStr + "-01-01T00:00:00");
            }
            
            logger.warn("Could not parse date: {}", dateStr);
            return null;
            
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse date '{}': {}", dateStr, e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract volume from source string
     * Example: "Vol. 29 No. 1 (2009)" -> "29"
     */
    private String extractVolume(String source) {
        if (source == null) {
            return null;
        }
        
        // Try to match "Vol. XX" or "Volume XX"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Vol\\.?\\s*(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * Extract pages from source string
     * Example: "39-50" from the source
     */
    private String extractPages(String source) {
        if (source == null) {
            return null;
        }
        
        // Try to match "XX-YY" page range
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)-(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return matcher.group(0); // Return the full match (e.g., "39-50")
        }
        
        return null;
    }
}
