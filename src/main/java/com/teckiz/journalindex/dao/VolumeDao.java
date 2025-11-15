package com.teckiz.journalindex.dao;

import com.teckiz.journalindex.db.DatabaseManager;
import com.teckiz.journalindex.entity.IndexJournal;
import com.teckiz.journalindex.entity.IndexJournalVolume;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Lightweight DAO for Journal Volume operations using plain JDBC
 */
public class VolumeDao {
    
    private static final Logger logger = LogManager.getLogger(VolumeDao.class);
    
    /**
     * Find volume by journal ID and volume number, or create if not exists
     */
    public static IndexJournalVolume findOrCreateByJournalIdAndVolumeNumber(Long journalId, String volumeNumber) {
        Optional<IndexJournalVolume> existing = findByJournalIdAndVolumeNumber(journalId, volumeNumber);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new volume
        return createVolume(journalId, volumeNumber);
    }
    
    /**
     * Find volume by journal ID and volume number
     */
    public static Optional<IndexJournalVolume> findByJournalIdAndVolumeNumber(Long journalId, String volumeNumber) {
        String sql = "SELECT id, index_journal_id, vol_number, issue_number, published_at, created_at " +
                     "FROM IndexJournalVolume WHERE index_journal_id = ? AND vol_number = ? LIMIT 1";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, journalId);
            stmt.setString(2, volumeNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    IndexJournalVolume volume = mapResultSetToVolume(rs);
                    logger.debug("Found volume: {} for journal ID: {} (Volume ID: {})", 
                                volumeNumber, journalId, volume.getId());
                    return Optional.of(volume);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding volume by journal ID {} and volume number {}", journalId, volumeNumber, e);
            throw new RuntimeException("Failed to find volume: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Create a new volume
     */
    private static IndexJournalVolume createVolume(Long journalId, String volumeNumber) {
        String sql = "INSERT INTO IndexJournalVolume (index_journal_id, vol_number, created_at, updated_at) " +
                     "VALUES (?, ?, NOW(), NOW())";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            conn.setAutoCommit(false);
            
            try {
                stmt.setLong(1, journalId);
                stmt.setString(2, volumeNumber);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating volume failed, no rows affected.");
                }
                
                Long id;
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        id = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Creating volume failed, no ID obtained.");
                    }
                }
                
                conn.commit();
                
                IndexJournalVolume volume = new IndexJournalVolume();
                volume.setId(id);
                volume.setVolumeNumber(volumeNumber);
                volume.setCreatedAt(LocalDateTime.now());
                volume.setUpdatedAt(LocalDateTime.now());
                
                // Set journal reference (just the ID, not full object)
                IndexJournal journal = new IndexJournal();
                journal.setId(journalId);
                volume.setIndexJournal(journal);
                
                logger.info("Created new volume: {} for journal ID: {} (Volume ID: {})", 
                           volumeNumber, journalId, id);
                return volume;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            logger.error("Error creating volume for journal ID {} and volume {}", journalId, volumeNumber, e);
            throw new RuntimeException("Failed to create volume: " + e.getMessage(), e);
        }
    }
    
    /**
     * Map ResultSet to IndexJournalVolume entity
     */
    private static IndexJournalVolume mapResultSetToVolume(ResultSet rs) throws SQLException {
        IndexJournalVolume volume = new IndexJournalVolume();
        volume.setId(rs.getLong("id"));
        volume.setVolumeNumber(rs.getString("vol_number"));
        volume.setIssueNumber(rs.getString("issue_number"));
        
        Timestamp publishedAt = rs.getTimestamp("published_at");
        if (publishedAt != null) {
            volume.setPublishedAt(publishedAt.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            volume.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // Set journal reference
        Long journalId = rs.getLong("index_journal_id");
        IndexJournal journal = new IndexJournal();
        journal.setId(journalId);
        volume.setIndexJournal(journal);
        
        return volume;
    }
}

