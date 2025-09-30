package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.IndexJournalSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IndexJournalSubject entity
 */
@Repository
public interface IndexJournalSubjectRepository extends JpaRepository<IndexJournalSubject, Long> {
    
    /**
     * Find subject by subject key
     */
    Optional<IndexJournalSubject> findBySubjectKey(String subjectKey);
    
    /**
     * Find subjects by company
     */
    List<IndexJournalSubject> findByCompanyId(Long companyId);
    
    /**
     * Find active subjects
     */
    List<IndexJournalSubject> findByActiveTrue();
    
    /**
     * Find subjects by name
     */
    Optional<IndexJournalSubject> findByName(String name);
    
    /**
     * Search subjects by name containing
     */
    @Query("SELECT s FROM IndexJournalSubject s WHERE s.name LIKE %:name% AND s.active = true")
    List<IndexJournalSubject> findByNameContainingAndActive(@Param("name") String name);
    
    /**
     * Find subjects by company and active status
     */
    List<IndexJournalSubject> findByCompanyIdAndActive(Long companyId, Boolean active);
    
    /**
     * Count subjects by company
     */
    long countByCompanyId(Long companyId);
    
    /**
     * Count active subjects by company
     */
    long countByCompanyIdAndActive(Long companyId, Boolean active);
    
    /**
     * Check if subject exists by subject key
     */
    boolean existsBySubjectKey(String subjectKey);
    
    /**
     * Check if subject exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Check if subject exists by name and company
     */
    boolean existsByNameAndCompanyId(String name, Long companyId);
}
