package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.IndexJournalVolume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IndexJournalVolume entity
 */
@Repository
public interface IndexJournalVolumeRepository extends JpaRepository<IndexJournalVolume, Long> {
    
    /**
     * Find volume by volume key
     */
    Optional<IndexJournalVolume> findByVolumeKey(String volumeKey);
    
    /**
     * Find volumes by company
     */
    List<IndexJournalVolume> findByCompanyId(Long companyId);
    
    /**
     * Find volumes by journal
     */
    List<IndexJournalVolume> findByIndexJournalId(Long journalId);
    
    /**
     * Find volume by publisher record ID
     */
    Optional<IndexJournalVolume> findByPublisherRecordId(String publisherRecordId);
    
    /**
     * Find volumes by volume number
     */
    List<IndexJournalVolume> findByVolumeNumber(String volumeNumber);
    
    /**
     * Find volumes by issue number
     */
    List<IndexJournalVolume> findByIssueNumber(String issueNumber);
    
    /**
     * Find volumes published after date
     */
    List<IndexJournalVolume> findByPublishedAtAfter(LocalDateTime publishedAt);
    
    /**
     * Find volumes published between dates
     */
    List<IndexJournalVolume> findByPublishedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find volumes by company and journal
     */
    List<IndexJournalVolume> findByCompanyIdAndIndexJournalId(Long companyId, Long journalId);
    
    /**
     * Find volumes by company and volume number
     */
    List<IndexJournalVolume> findByCompanyIdAndVolumeNumber(Long companyId, String volumeNumber);
    
    /**
     * Count volumes by company
     */
    long countByCompanyId(Long companyId);
    
    /**
     * Count volumes by journal
     */
    long countByIndexJournalId(Long journalId);
    
    /**
     * Check if volume exists by volume key
     */
    boolean existsByVolumeKey(String volumeKey);
    
    /**
     * Check if volume exists by publisher record ID
     */
    boolean existsByPublisherRecordId(String publisherRecordId);
    
    /**
     * Find volumes by journal ID and volume number
     * Returns all matches (handles duplicates)
     */
    @Query("SELECT v FROM IndexJournalVolume v WHERE v.indexJournal.id = :journalId AND v.volumeNumber = :volumeNumber ORDER BY v.id ASC")
    List<IndexJournalVolume> findAllByIndexJournalIdAndVolumeNumber(@Param("journalId") Long journalId, @Param("volumeNumber") String volumeNumber);
    
    default Optional<IndexJournalVolume> findByIndexJournalIdAndVolumeNumber(Long journalId, String volumeNumber) {
        List<IndexJournalVolume> results = findAllByIndexJournalIdAndVolumeNumber(journalId, volumeNumber);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
