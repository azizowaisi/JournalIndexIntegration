package com.teckiz.journalindex.service;

import com.teckiz.journalindex.entity.*;
import com.teckiz.journalindex.parser.OaiDataParser;
import com.teckiz.journalindex.repository.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for handling OAI harvesting operations
 */
@Service
public class OaiHarvestService implements Processor {
    
    private static final Logger logger = LogManager.getLogger(OaiHarvestService.class);
    private final OaiDataParser parser = new OaiDataParser();
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private IndexJournalRepository indexJournalRepository;
    
    @Autowired
    private IndexJournalSubjectRepository subjectRepository;
    
    @Autowired
    private IndexCountryRepository countryRepository;
    
    @Autowired
    private IndexLanguageRepository languageRepository;
    
    @Autowired
    private ImportQueueService importQueueService;
    
    
    @Override
    public void process(Exchange exchange) throws Exception {
        String oaiData = exchange.getIn().getHeader("oaiData", String.class);
        String oaiBaseUrl = exchange.getIn().getHeader("oaiBaseUrl", String.class);
        String websiteUrl = exchange.getIn().getHeader("websiteUrl", String.class);
        String journalKey = exchange.getIn().getHeader("journalKey", String.class);
        String systemType = exchange.getIn().getHeader("systemType", String.class);

        logger.info("Creating import queue entries for: {} (system: {})", oaiBaseUrl, systemType);

        try {
            // Create or find company (default company for OAI harvested journals)
            Company company = findOrCreateDefaultCompany();
            
            // Create import queue entries for the harvested data (CreatorCommand functionality)
            createImportQueueEntries(oaiData, websiteUrl, journalKey, company.getCompanyKey(), systemType);

            // Create response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", oaiBaseUrl);
            responseData.put("journal_key", journalKey);
            responseData.put("system_type", systemType);
            responseData.put("queue_status", "created");
            responseData.put("data_length", oaiData != null ? oaiData.length() : 0);
            responseData.put("company_key", company.getCompanyKey());

            // Set the response data in the exchange
            exchange.getIn().setBody(responseData);

            logger.info("Successfully created import queue entries for journal: {} (system: {})", journalKey, systemType);

        } catch (Exception e) {
            logger.error("Error creating import queue entries", e);

            // Set error information
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("url", oaiBaseUrl);
            errorData.put("journal_key", journalKey);
            errorData.put("queue_status", "failed");
            errorData.put("error_message", e.getMessage());

            exchange.getIn().setBody(errorData);
        }
    }
    
    /**
     * Find or create default company for OAI harvested journals
     */
    private Company findOrCreateDefaultCompany() {
        Optional<Company> existingCompany = companyRepository.findByName("OAI Harvested Journals");
        
        if (existingCompany.isPresent()) {
            return existingCompany.get();
        }
        
        Company company = new Company();
        company.setName("OAI Harvested Journals");
        company.setDescription("Default company for journals harvested via OAI-PMH");
        company.setActive(true);
        company.setEmail("admin@oai-harvested.com");
        company.setCountry("US");
        company.setLang("en");
        
        return companyRepository.save(company);
    }
    
    
    /**
     * Extract repository name from OAI base URL
     */
    private String extractRepositoryName(String oaiBaseUrl) {
        try {
            // Extract domain name from URL
            String domain = oaiBaseUrl.replaceAll("https?://", "").replaceAll("/.*", "");
            return domain + " Repository";
        } catch (Exception e) {
            return "OAI Repository";
        }
    }
    
    /**
     * Create import queue entries for harvested data
     */
    private void createImportQueueEntries(String oaiData, String websiteUrl, String journalKey, String companyKey, String systemType) {
        try {
            if (oaiData == null || oaiData.trim().isEmpty()) {
                logger.warn("No OAI data to save to import queue");
                return;
            }
            
            // Create import queue entry based on system type
            switch (systemType) {
                case "OJS_OAI":
                    // For OJS OAI, we need to check if this is identify or records data
                    if (oaiData.contains("<Identify>")) {
                        importQueueService.createOjsOaiIdentifyQueue(oaiData, websiteUrl, journalKey, companyKey);
                        logger.info("Created OJS OAI identify queue entry for journal: {}", journalKey);
                    } else if (oaiData.contains("<ListRecords>")) {
                        importQueueService.createOjsOaiRecordListQueue(oaiData, websiteUrl, journalKey, companyKey);
                        logger.info("Created OJS OAI record list queue entry for journal: {}", journalKey);
                    }
                    break;
                    
                case "DOAJ":
                    importQueueService.createDoajQueue(oaiData, websiteUrl, journalKey, companyKey);
                    logger.info("Created DOAJ queue entry for journal: {}", journalKey);
                    break;
                    
                case "TECKIZ":
                    importQueueService.createTeckizQueue(oaiData, websiteUrl, journalKey, companyKey);
                    logger.info("Created Teckiz queue entry for journal: {}", journalKey);
                    break;
                    
                default:
                    logger.warn("Unknown system type for import queue: {}", systemType);
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Error creating import queue entries for journal: {}", journalKey, e);
        }
    }
    
    /**
     * Extract journal name from OAI record
     */
    private String extractJournalName(Map<String, Object> record) {
        String title = (String) record.get("title");
        if (title != null && !title.trim().isEmpty()) {
            return title;
        }
        return "Unknown Journal";
    }
    
    /**
     * Extract e-ISSN from OAI record
     */
    private String extractEissn(Map<String, Object> record) {
        String identifier = (String) record.get("identifier");
        if (identifier != null && identifier.contains("ISSN")) {
            // Extract ISSN from identifier
            return identifier.replaceAll(".*ISSN[\\s:]*([0-9-]+).*", "$1");
        }
        return null;
    }
    
    /**
     * Extract keywords from OAI records
     */
    private String extractKeywords(List<Map<String, Object>> records) {
        Set<String> keywords = new HashSet<>();
        
        for (Map<String, Object> record : records) {
            String subject = (String) record.get("subject");
            if (subject != null && !subject.trim().isEmpty()) {
                keywords.add(subject);
            }
        }
        
        return String.join(", ", keywords);
    }
    
    /**
     * Extract country from OAI record
     */
    private String extractCountry(Map<String, Object> record) {
        // This would need to be implemented based on your OAI data structure
        return "US"; // Default country
    }
    
    /**
     * Extract email from OAI record
     */
    private String extractEmail(Map<String, Object> record) {
        // This would need to be implemented based on your OAI data structure
        return null;
    }
    
    /**
     * Extract contact person from OAI record
     */
    private String extractContactPerson(Map<String, Object> record) {
        String creator = (String) record.get("creator");
        return creator;
    }
    
    /**
     * Extract subject areas from OAI records
     */
    private String extractSubjectAreas(List<Map<String, Object>> records) {
        Set<String> subjects = new HashSet<>();
        
        for (Map<String, Object> record : records) {
            String subject = (String) record.get("subject");
            if (subject != null && !subject.trim().isEmpty()) {
                subjects.add(subject);
            }
        }
        
        return String.join(", ", subjects);
    }
    
    /**
     * Extract languages from OAI records
     */
    private String extractLanguages(List<Map<String, Object>> records) {
        Set<String> languages = new HashSet<>();
        
        for (Map<String, Object> record : records) {
            String language = (String) record.get("language");
            if (language != null && !language.trim().isEmpty()) {
                languages.add(language);
            }
        }
        
        return String.join(", ", languages);
    }
}
