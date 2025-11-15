package com.teckiz.journalindex.dao;

import com.teckiz.journalindex.db.DatabaseManager;
import com.teckiz.journalindex.entity.IndexJournalAuthor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight DAO for Author operations using plain JDBC
 */
public class AuthorDao {
    
    private static final Logger logger = LogManager.getLogger(AuthorDao.class);
    
    /**
     * Delete all authors for an article
     */
    public static void deleteByArticleId(Long articleId) {
        String sql = "DELETE FROM IndexJournalAuthor WHERE index_journal_article_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            try {
                stmt.setLong(1, articleId);
                int rowsAffected = stmt.executeUpdate();
                conn.commit();
                
                logger.debug("Deleted {} authors for article ID: {}", rowsAffected, articleId);
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting authors for article ID: {}", articleId, e);
            throw new RuntimeException("Failed to delete authors: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save multiple authors for an article
     */
    public static void saveAuthors(Long articleId, List<String> authorNames) {
        if (authorNames == null || authorNames.isEmpty()) {
            return;
        }
        
        // Delete existing authors first
        deleteByArticleId(articleId);
        
        // Insert new authors
        String sql = "INSERT INTO IndexJournalAuthor (index_journal_article_id, name) VALUES (?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            try {
                for (String authorName : authorNames) {
                    if (authorName != null && !authorName.trim().isEmpty()) {
                        stmt.setLong(1, articleId);
                        stmt.setString(2, authorName.trim());
                        stmt.addBatch();
                    }
                }
                
                int[] results = stmt.executeBatch();
                conn.commit();
                
                logger.info("Saved {} authors for article ID: {}", results.length, articleId);
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            logger.error("Error saving authors for article ID: {}", articleId, e);
            throw new RuntimeException("Failed to save authors: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all authors for an article
     */
    public static List<IndexJournalAuthor> findByArticleId(Long articleId) {
        String sql = "SELECT id, index_journal_article_id, name, email, country, affiliation, orcid " +
                     "FROM IndexJournalAuthor WHERE index_journal_article_id = ?";
        
        List<IndexJournalAuthor> authors = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, articleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IndexJournalAuthor author = mapResultSetToAuthor(rs);
                    authors.add(author);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding authors for article ID: {}", articleId, e);
            throw new RuntimeException("Failed to find authors: " + e.getMessage(), e);
        }
        
        return authors;
    }
    
    /**
     * Map ResultSet to IndexJournalAuthor entity
     */
    private static IndexJournalAuthor mapResultSetToAuthor(ResultSet rs) throws SQLException {
        IndexJournalAuthor author = new IndexJournalAuthor();
        author.setId(rs.getLong("id"));
        author.setName(rs.getString("name"));
        author.setEmail(rs.getString("email"));
        author.setCountry(rs.getString("country"));
        author.setAffiliation(rs.getString("affiliation"));
        author.setOrcid(rs.getString("orcid"));
        
        return author;
    }
}

