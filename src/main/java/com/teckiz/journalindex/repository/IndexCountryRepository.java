package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.IndexCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IndexCountry entity
 */
@Repository
public interface IndexCountryRepository extends JpaRepository<IndexCountry, Long> {
    
    /**
     * Find country by country key
     */
    Optional<IndexCountry> findByCountryKey(String countryKey);
    
    /**
     * Find countries by company
     */
    List<IndexCountry> findByCompanyId(Long companyId);
    
    /**
     * Find country by name
     */
    Optional<IndexCountry> findByName(String name);
    
    /**
     * Find country by code
     */
    Optional<IndexCountry> findByCode(String code);
    
    /**
     * Search countries by name containing
     */
    @Query("SELECT c FROM IndexCountry c WHERE c.name LIKE %:name%")
    List<IndexCountry> findByNameContaining(@Param("name") String name);
    
    /**
     * Count countries by company
     */
    long countByCompanyId(Long companyId);
    
    /**
     * Check if country exists by country key
     */
    boolean existsByCountryKey(String countryKey);
    
    /**
     * Check if country exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Check if country exists by code
     */
    boolean existsByCode(String code);
}
