package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Company entity
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    /**
     * Find company by company key
     */
    Optional<Company> findByCompanyKey(String companyKey);
    
    /**
     * Find company by name
     */
    Optional<Company> findByName(String name);
    
    /**
     * Find company by slug
     */
    Optional<Company> findBySlug(String slug);
    
    /**
     * Find active companies
     */
    List<Company> findByActiveTrue();
    
    /**
     * Find companies by country
     */
    List<Company> findByCountry(String country);
    
    /**
     * Find master companies
     */
    List<Company> findByMasterTrue();
    
    /**
     * Find sub companies by master company
     */
    List<Company> findByMasterCompanyId(Long masterCompanyId);
    
    /**
     * Search companies by name containing
     */
    @Query("SELECT c FROM Company c WHERE c.name LIKE %:name% AND c.active = true")
    List<Company> findByNameContainingAndActive(@Param("name") String name);
    
    /**
     * Find companies by email
     */
    Optional<Company> findByEmail(String email);
    
    /**
     * Check if company exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Check if company exists by slug
     */
    boolean existsBySlug(String slug);
    
    /**
     * Check if company exists by company key
     */
    boolean existsByCompanyKey(String companyKey);
}
