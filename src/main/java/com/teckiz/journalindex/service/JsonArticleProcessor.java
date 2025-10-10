package com.teckiz.journalindex.service;

import com.teckiz.journalindex.entity.*;
import com.teckiz.journalindex.model.SqsArticleMessage;
import com.teckiz.journalindex.repository.IndexJournalArticleRepository;
import com.teckiz.journalindex.repository.IndexJournalRepository;
import com.teckiz.journalindex.repository.IndexJournalVolumeRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Service to process JSON articles from SQS messages
 */
@Service
public class JsonArticleProcessor {

    private static final Logger logger = LogManager.getLogger(JsonArticleProcessor.class);

    @Autowired
    private IndexJournalRepository journalRepository;

    @Autowired
    private IndexJournalArticleRepository articleRepository;
    
    @Autowired
    private IndexJournalVolumeRepository volumeRepository;

    /**
     * Process batch of articles from SQS JSON message
     * Note: No @Transactional here - each article gets its own transaction via processArticleData()
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
    @Transactional
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
            IndexJournal journal = journalRepository.findByJournalKey(journalKey)
                    .orElseGet(() -> {
                        logger.info("Journal not found, creating new journal: {}", journalKey);
                        IndexJournal newJournal = new IndexJournal();
                        newJournal.setJournalKey(journalKey);
                        newJournal.setWebsite(oaiUrl);
                        newJournal.setPublisher(articleData.getPublisher());
                        return journalRepository.save(newJournal);
                    });
            
            logger.info("Using journal ID: {}", journal.getId());
            
            // Find or create volume if we have volume information
            IndexJournalVolume volume = null;
            if (articleData.getSources() != null && !articleData.getSources().isEmpty()) {
                String volumeNumber = extractVolume(articleData.getSources().get(0));
                if (volumeNumber != null) {
                    // Try to find existing volume by journal and volume number
                    volume = volumeRepository.findByIndexJournalIdAndVolumeNumber(journal.getId(), volumeNumber)
                            .orElseGet(() -> {
                                logger.info("Creating new volume: {} for journal ID: {}", volumeNumber, journal.getId());
                                IndexJournalVolume newVolume = new IndexJournalVolume();
                                newVolume.setIndexJournal(journal);
                                newVolume.setVolumeNumber(volumeNumber);
                                return volumeRepository.save(newVolume);
                            });
                    logger.info("Using volume ID: {}", volume.getId());
                }
            }
            
            // Check if article already exists by identifier URL
            IndexJournalArticle existingArticle = null;
            if (articleData.getIdentifier() != null) {
                existingArticle = articleRepository.findByPageURL(articleData.getIdentifier()).orElse(null);
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
            article = articleRepository.save(article);
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
            // Clear existing authors
            if (article.getAuthors() != null) {
                article.getAuthors().clear();
            } else {
                article.setAuthors(new ArrayList<>());
            }
            
            // Split by comma or semicolon
            String[] authorNames = creatorString.split("[,;]");
            
            for (String authorName : authorNames) {
                authorName = authorName.trim();
                if (!authorName.isEmpty()) {
                    IndexJournalAuthor author = new IndexJournalAuthor();
                    author.setIndexJournalArticle(article);
                    author.setName(authorName);
                    article.getAuthors().add(author);
                    logger.info("Added author: {}", authorName);
                }
            }
            
            articleRepository.save(article);
            logger.info("✅ Saved {} authors", article.getAuthors().size());
            
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
