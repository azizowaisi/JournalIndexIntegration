package com.teckiz.journalindex.dao;

import com.teckiz.journalindex.db.DatabaseManager;
import com.teckiz.journalindex.entity.Company;
import com.teckiz.journalindex.entity.IndexJournal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Lightweight DAO for Journal operations using plain JDBC
 */
public class JournalDao {
    
    private static final Logger logger = LogManager.getLogger(JournalDao.class);
    
    /**
     * Find journal by journal key, or create if not exists
     */
    public static IndexJournal findOrCreateByJournalKey(String journalKey, String website, String publisher) {
        Optional<IndexJournal> existing = findByJournalKey(journalKey);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new journal
        return createJournal(journalKey, website, publisher);
    }
    
    /**
     * Find journal by journal key
     */
    public static Optional<IndexJournal> findByJournalKey(String journalKey) {
        String sql = "SELECT id, journal_key, name, website, publisher, status, country, " +
                     "email, phone, contact_person, keywords, eissn, created_at, company_id " +
                     "FROM IndexJournal WHERE journal_key = ? LIMIT 1";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, journalKey);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    IndexJournal journal = mapResultSetToJournal(rs);
                    logger.debug("Found journal: {} (ID: {})", journalKey, journal.getId());
                    return Optional.of(journal);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding journal by key: {}", journalKey, e);
            throw new RuntimeException("Failed to find journal: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Create a new journal
     */
    private static IndexJournal createJournal(String journalKey, String website, String publisher) {
        String sql = "INSERT INTO IndexJournal (journal_key, website, publisher, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, 'received', NOW(), NOW())";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            conn.setAutoCommit(false);
            
            try {
                stmt.setString(1, journalKey);
                stmt.setString(2, website);
                stmt.setString(3, publisher);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating journal failed, no rows affected.");
                }
                
                Long id;
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        id = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Creating journal failed, no ID obtained.");
                    }
                }
                
                conn.commit();
                
                IndexJournal journal = new IndexJournal();
                journal.setId(id);
                journal.setJournalKey(journalKey);
                journal.setWebsite(website);
                journal.setPublisher(publisher);
                journal.setStatus("received");
                journal.setCreatedAt(LocalDateTime.now());
                
                logger.info("Created new journal: {} (ID: {})", journalKey, id);
                return journal;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            logger.error("Error creating journal: {}", journalKey, e);
            throw new RuntimeException("Failed to create journal: " + e.getMessage(), e);
        }
    }
    
    /**
     * Map ResultSet to IndexJournal entity
     */
    private static IndexJournal mapResultSetToJournal(ResultSet rs) throws SQLException {
        IndexJournal journal = new IndexJournal();
        journal.setId(rs.getLong("id"));
        journal.setJournalKey(rs.getString("journal_key"));
        journal.setName(rs.getString("name"));
        journal.setWebsite(rs.getString("website"));
        journal.setPublisher(rs.getString("publisher"));
        journal.setStatus(rs.getString("status"));
        journal.setCountry(rs.getString("country"));
        journal.setEmail(rs.getString("email"));
        journal.setPhone(rs.getString("phone"));
        journal.setContactPerson(rs.getString("contact_person"));
        journal.setKeywords(rs.getString("keywords"));
        journal.setEissn(rs.getString("eissn"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            journal.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // Set company reference if company_id exists
        Long companyId = rs.getLong("company_id");
        if (!rs.wasNull() && companyId > 0) {
            Company company = new Company();
            company.setId(companyId);
            journal.setCompany(company);
        }
        
        return journal;
    }
}

