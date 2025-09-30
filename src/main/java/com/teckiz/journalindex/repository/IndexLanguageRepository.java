package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.IndexLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IndexLanguage entity
 */
@Repository
public interface IndexLanguageRepository extends JpaRepository<IndexLanguage, Long> {
    
    /**
     * Find language by language key
     */
    Optional<IndexLanguage> findByLanguageKey(String languageKey);
    
    /**
     * Find languages by company
     */
    List<IndexLanguage> findByCompanyId(Long companyId);
    
    /**
     * Find active languages
     */
    List<IndexLanguage> findByActiveTrue();
    
    /**
     * Find language by name
     */
    Optional<IndexLanguage> findByName(String name);
    
    /**
     * Find language by code A
     */
    Optional<IndexLanguage> findByCodeA(String codeA);
    
    /**
     * Find language by code B
     */
    Optional<IndexLanguage> findByCodeB(String codeB);
    
    /**
     * Search languages by name containing
     */
    @Query("SELECT l FROM IndexLanguage l WHERE l.name LIKE %:name% AND l.active = true")
    List<IndexLanguage> findByNameContainingAndActive(@Param("name") String name);
    
    /**
     * Find languages by company and active status
     */
    List<IndexLanguage> findByCompanyIdAndActive(Long companyId, Boolean active);
    
    /**
     * Count languages by company
     */
    long countByCompanyId(Long companyId);
    
    /**
     * Count active languages by company
     */
    long countByCompanyIdAndActive(Long companyId, Boolean active);
    
    /**
     * Check if language exists by language key
     */
    boolean existsByLanguageKey(String languageKey);
    
    /**
     * Check if language exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Check if language exists by code A
     */
    boolean existsByCodeA(String codeA);
    
    /**
     * Check if language exists by code B
     */
    boolean existsByCodeB(String codeB);
}
