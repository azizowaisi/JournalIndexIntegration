package com.teckiz.journalindex.dao;

import com.teckiz.journalindex.db.DatabaseManager;
import com.teckiz.journalindex.entity.IndexJournalArticle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Lightweight DAO for Article operations using plain JDBC
 */
public class ArticleDao {
    
    private static final Logger logger = LogManager.getLogger(ArticleDao.class);
    
    /**
     * Find article by page URL (returns first if multiple exist)
     */
    public static Optional<IndexJournalArticle> findByPageURL(String pageURL) {
        String sql = "SELECT id, article_key, title_text, abstract_text, page_url, pages, keywords_text, " +
                     "doi, publisher_record_id, article_type, published_at, received_at, updated_at, " +
                     "index_journal_volume_id, company_id " +
                     "FROM IndexJournalArticle WHERE page_url = ? ORDER BY id ASC LIMIT 1";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, pageURL);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    IndexJournalArticle article = mapResultSetToArticle(rs);
                    logger.debug("Found article by page URL: {} (ID: {})", pageURL, article.getId());
                    return Optional.of(article);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding article by page URL: {}", pageURL, e);
            throw new RuntimeException("Failed to find article: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Save article (insert or update)
     */
    public static IndexJournalArticle save(IndexJournalArticle article) {
        if (article.getId() == null) {
            return insert(article);
        } else {
            return update(article);
        }
    }
    
    /**
     * Insert new article
     */
    private static IndexJournalArticle insert(IndexJournalArticle article) {
        String sql = "INSERT INTO IndexJournalArticle " +
                     "(article_key, title_text, abstract_text, page_url, pages, keywords_text, " +
                     "doi, publisher_record_id, article_type, published_at, received_at, updated_at, " +
                     "index_journal_volume_id, company_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            conn.setAutoCommit(false);
            
            try {
                // Generate article key if not set
                if (article.getArticleKey() == null || article.getArticleKey().isEmpty()) {
                    article.setArticleKey("ART_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000));
                }
                
                stmt.setString(1, article.getArticleKey());
                stmt.setString(2, article.getTitle());
                stmt.setString(3, article.getAbstractText());
                stmt.setString(4, article.getPageURL());
                stmt.setString(5, article.getPages());
                stmt.setString(6, article.getKeywords());
                stmt.setString(7, article.getDoi());
                stmt.setString(8, article.getPublisherRecordId());
                stmt.setString(9, article.getArticleType());
                
                if (article.getPublishedAt() != null) {
                    stmt.setTimestamp(10, Timestamp.valueOf(article.getPublishedAt()));
                } else {
                    stmt.setNull(10, Types.TIMESTAMP);
                }
                
                if (article.getIndexJournalVolume() != null && article.getIndexJournalVolume().getId() != null) {
                    stmt.setLong(11, article.getIndexJournalVolume().getId());
                } else {
                    stmt.setNull(11, Types.BIGINT);
                }
                
                if (article.getCompany() != null && article.getCompany().getId() != null) {
                    stmt.setLong(12, article.getCompany().getId());
                } else {
                    stmt.setNull(12, Types.BIGINT);
                }
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating article failed, no rows affected.");
                }
                
                Long id;
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        id = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Creating article failed, no ID obtained.");
                    }
                }
                
                conn.commit();
                
                article.setId(id);
                article.setReceivedAt(LocalDateTime.now());
                article.setUpdatedAt(LocalDateTime.now());
                
                logger.info("Created new article: {} (ID: {})", article.getTitle(), id);
                return article;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            logger.error("Error creating article: {}", article.getTitle(), e);
            throw new RuntimeException("Failed to create article: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update existing article
     */
    private static IndexJournalArticle update(IndexJournalArticle article) {
        String sql = "UPDATE IndexJournalArticle SET " +
                     "title_text = ?, abstract_text = ?, page_url = ?, pages = ?, keywords_text = ?, " +
                     "doi = ?, publisher_record_id = ?, article_type = ?, published_at = ?, updated_at = NOW(), " +
                     "index_journal_volume_id = ?, company_id = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            try {
                stmt.setString(1, article.getTitle());
                stmt.setString(2, article.getAbstractText());
                stmt.setString(3, article.getPageURL());
                stmt.setString(4, article.getPages());
                stmt.setString(5, article.getKeywords());
                stmt.setString(6, article.getDoi());
                stmt.setString(7, article.getPublisherRecordId());
                stmt.setString(8, article.getArticleType());
                
                if (article.getPublishedAt() != null) {
                    stmt.setTimestamp(9, Timestamp.valueOf(article.getPublishedAt()));
                } else {
                    stmt.setNull(9, Types.TIMESTAMP);
                }
                
                if (article.getIndexJournalVolume() != null && article.getIndexJournalVolume().getId() != null) {
                    stmt.setLong(10, article.getIndexJournalVolume().getId());
                } else {
                    stmt.setNull(10, Types.BIGINT);
                }
                
                if (article.getCompany() != null && article.getCompany().getId() != null) {
                    stmt.setLong(11, article.getCompany().getId());
                } else {
                    stmt.setNull(11, Types.BIGINT);
                }
                
                stmt.setLong(12, article.getId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Updating article failed, no rows affected.");
                }
                
                conn.commit();
                article.setUpdatedAt(LocalDateTime.now());
                
                logger.info("Updated article: {} (ID: {})", article.getTitle(), article.getId());
                return article;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            logger.error("Error updating article ID: {}", article.getId(), e);
            throw new RuntimeException("Failed to update article: " + e.getMessage(), e);
        }
    }
    
    /**
     * Map ResultSet to IndexJournalArticle entity
     */
    private static IndexJournalArticle mapResultSetToArticle(ResultSet rs) throws SQLException {
        IndexJournalArticle article = new IndexJournalArticle();
        article.setId(rs.getLong("id"));
        article.setArticleKey(rs.getString("article_key"));
        article.setTitle(rs.getString("title_text"));
        article.setAbstractText(rs.getString("abstract_text"));
        article.setPageURL(rs.getString("page_url"));
        article.setPages(rs.getString("pages"));
        article.setKeywords(rs.getString("keywords_text"));
        article.setDoi(rs.getString("doi"));
        article.setPublisherRecordId(rs.getString("publisher_record_id"));
        article.setArticleType(rs.getString("article_type"));
        
        Timestamp publishedAt = rs.getTimestamp("published_at");
        if (publishedAt != null) {
            article.setPublishedAt(publishedAt.toLocalDateTime());
        }
        
        Timestamp receivedAt = rs.getTimestamp("received_at");
        if (receivedAt != null) {
            article.setReceivedAt(receivedAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            article.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        article.setAuthors(new ArrayList<>());
        
        // Note: Company relationship would need to be loaded separately if needed
        // For now, we just store the company_id in the article when saving
        
        return article;
    }
}

