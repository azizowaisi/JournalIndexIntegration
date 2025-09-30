package com.teckiz.journalindex.repository;

import com.teckiz.journalindex.entity.IndexRelatedMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for IndexRelatedMedia entity
 */
@Repository
public interface IndexRelatedMediaRepository extends JpaRepository<IndexRelatedMedia, Long> {
    
    /**
     * Find media by related media key
     */
    Optional<IndexRelatedMedia> findByRelatedMediaKey(String relatedMediaKey);
    
    /**
     * Find media by company
     */
    List<IndexRelatedMedia> findByCompanyId(Long companyId);
    
    /**
     * Find media by reference key
     */
    List<IndexRelatedMedia> findByReferenceKey(String referenceKey);
    
    /**
     * Find media by media type
     */
    List<IndexRelatedMedia> findByMediaType(String mediaType);
    
    /**
     * Find media by related media type
     */
    List<IndexRelatedMedia> findByRelatedMediaType(String relatedMediaType);
    
    /**
     * Find media by MIME type
     */
    List<IndexRelatedMedia> findByMimeType(String mimeType);
    
    /**
     * Find media by name
     */
    Optional<IndexRelatedMedia> findByName(String name);
    
    /**
     * Search media by name containing
     */
    @Query("SELECT m FROM IndexRelatedMedia m WHERE m.name LIKE %:name%")
    List<IndexRelatedMedia> findByNameContaining(@Param("name") String name);
    
    /**
     * Find media by company and media type
     */
    List<IndexRelatedMedia> findByCompanyIdAndMediaType(Long companyId, String mediaType);
    
    /**
     * Find media by company and related media type
     */
    List<IndexRelatedMedia> findByCompanyIdAndRelatedMediaType(Long companyId, String relatedMediaType);
    
    /**
     * Count media by company
     */
    long countByCompanyId(Long companyId);
    
    /**
     * Count media by media type
     */
    long countByMediaType(String mediaType);
    
    /**
     * Count media by related media type
     */
    long countByRelatedMediaType(String relatedMediaType);
    
    /**
     * Check if media exists by related media key
     */
    boolean existsByRelatedMediaKey(String relatedMediaKey);
    
    /**
     * Check if media exists by name
     */
    boolean existsByName(String name);
}
