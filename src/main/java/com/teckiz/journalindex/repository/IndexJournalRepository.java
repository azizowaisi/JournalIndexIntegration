package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.IndexJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IndexJournal entity
 */
@Repository
public interface IndexJournalRepository extends JpaRepository<IndexJournal, Long> {
    
    /**
     * Find journal by journal key
     */
    Optional<IndexJournal> findByJournalKey(String journalKey);
    
    /**
     * Find journals by company
     */
    List<IndexJournal> findByCompanyId(Long companyId);
    
    /**
     * Find journals by subject
     */
    List<IndexJournal> findBySubjectId(Long subjectId);
    
    /**
     * Find journals by status
     */
    List<IndexJournal> findByStatus(String status);
    
    /**
     * Find journals by e-ISSN
     */
    Optional<IndexJournal> findByEissn(String eissn);
    
    /**
     * Find journals by publisher
     */
    List<IndexJournal> findByPublisher(String publisher);
    
    /**
     * Find journals by country
     */
    List<IndexJournal> findByCountry(String country);
    
    /**
     * Search journals by name containing
     */
    @Query("SELECT j FROM IndexJournal j WHERE j.name LIKE %:name%")
    List<IndexJournal> findByNameContaining(@Param("name") String name);
    
    /**
     * Search journals by keywords containing
     */
    @Query("SELECT j FROM IndexJournal j WHERE j.keywords LIKE %:keywords%")
    List<IndexJournal> findByKeywordsContaining(@Param("keywords") String keywords);
    
    /**
     * Find journals by website
     */
    Optional<IndexJournal> findByWebsite(String website);
    
    /**
     * Find journals by email
     */
    Optional<IndexJournal> findByEmail(String email);
    
    /**
     * Find journals by contact person
     */
    List<IndexJournal> findByContactPerson(String contactPerson);
    
    /**
     * Find journals by start year
     */
    List<IndexJournal> findByStartYear(String startYear);
    
    /**
     * Find journals by review process type
     */
    List<IndexJournal> findByReviewProcessType(String reviewProcessType);
    
    /**
     * Find journals by society
     */
    List<IndexJournal> findBySociety(String society);
    
    /**
     * Search journals by multiple criteria
     */
    @Query("SELECT j FROM IndexJournal j WHERE " +
           "(:name IS NULL OR j.name LIKE %:name%) AND " +
           "(:publisher IS NULL OR j.publisher LIKE %:publisher%) AND " +
           "(:country IS NULL OR j.country = :country) AND " +
           "(:status IS NULL OR j.status = :status) AND " +
           "(:subjectId IS NULL OR j.subject.id = :subjectId)")
    List<IndexJournal> findByMultipleCriteria(@Param("name") String name,
                                            @Param("publisher") String publisher,
                                            @Param("country") String country,
                                            @Param("status") String status,
                                            @Param("subjectId") Long subjectId);
    
    /**
     * Count journals by company
     */
    long countByCompanyId(Long companyId);
    
    /**
     * Count journals by status
     */
    long countByStatus(String status);
    
    /**
     * Check if journal exists by journal key
     */
    boolean existsByJournalKey(String journalKey);
    
    /**
     * Check if journal exists by e-ISSN
     */
    boolean existsByEissn(String eissn);
    
    /**
     * Check if journal exists by website
     */
    boolean existsByWebsite(String website);
}
