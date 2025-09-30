# Entities Summary - Journal Index Integration

## Overview
This document summarizes all the entities that have been copied from the original codebase to the JournalIndexIntegration project, along with their relationships and functionality.

## Copied Entities

### 1. Company Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/Company.java`
**Table**: `companies`

**Key Features**:
- Main organization entity
- Self-referencing relationship (master/sub companies)
- Comprehensive company information (address, contact, settings)
- Slug generation for URLs
- Active/archived status management

**Relationships**:
- One-to-many with IndexJournal
- One-to-many with IndexJournalSubject
- One-to-many with IndexCountry
- One-to-many with IndexLanguage
- One-to-many with ResearchJournalIndexing
- Self-referencing (master/sub companies)

### 2. IndexJournal Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexJournal.java`
**Table**: `index_journals`

**Key Features**:
- Main journal entity for the indexing system
- Journal metadata (name, ISSN, publisher, etc.)
- Status management (approved, received, pending, suspend)
- Review process tracking
- Contact information

**Relationships**:
- Many-to-one with Company
- Many-to-one with IndexJournalSubject
- One-to-many with IndexJournalLanguage
- One-to-one with IndexJournalSetting

### 3. IndexJournalSubject Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexJournalSubject.java`
**Table**: `index_journal_subjects`

**Key Features**:
- Journal subject/category management
- Company-specific subjects
- Active/inactive status

**Relationships**:
- Many-to-one with Company
- One-to-many with IndexJournal

### 4. IndexJournalLanguage Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexJournalLanguage.java`
**Table**: `index_journal_languages`

**Key Features**:
- Language support for journals
- Primary language designation
- Language codes (A and B)

**Relationships**:
- Many-to-one with IndexJournal

### 5. IndexCountry Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexCountry.java`
**Table**: `index_countries`

**Key Features**:
- Country information management
- ISO codes support
- Company-specific countries

**Relationships**:
- Many-to-one with Company

### 6. IndexLanguage Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexLanguage.java`
**Table**: `index_languages`

**Key Features**:
- Language information management
- Language codes (A and B)
- Company-specific languages

**Relationships**:
- Many-to-one with Company

### 7. IndexJournalArticle Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexJournalArticle.java`
**Table**: `index_journal_articles`

**Key Features**:
- Journal article information
- DOI and publisher record ID support
- Article metadata (title, abstract, keywords, references)
- Publication date tracking

**Relationships**:
- Many-to-one with Company
- Many-to-one with IndexJournalVolume
- One-to-many with IndexJournalAuthor

### 8. IndexJournalVolume Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexJournalVolume.java`
**Table**: `index_journal_volumes`

**Key Features**:
- Journal volume and issue information
- Volume and issue number tracking
- Cover image support
- Publication date management

**Relationships**:
- Many-to-one with Company
- Many-to-one with IndexJournal
- One-to-many with IndexJournalArticle

### 9. IndexJournalAuthor Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexJournalAuthor.java`
**Table**: `index_journal_authors`

**Key Features**:
- Author information and affiliations
- ORCID support
- Country and email tracking
- Biography support

**Relationships**:
- Many-to-one with IndexJournalArticle

### 10. IndexRelatedMedia Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexRelatedMedia.java`
**Table**: `index_related_media`

**Key Features**:
- Related media file management
- MIME type and media type support
- Reference key tracking
- Location and name management

**Relationships**:
- Many-to-one with Company

### 11. IndexJournalPage Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexJournalPage.java`
**Table**: `index_journal_pages`

**Key Features**:
- Journal-specific pages (editorial board, guidelines, etc.)
- Page type constants
- URL management

**Relationships**:
- Many-to-one with Company
- Many-to-one with IndexJournal

### 12. IndexImportQueue Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexImportQueue.java`
**Table**: `index_import_queues`

**Key Features**:
- Import queue management
- System type support (OJS, DOAJ, etc.)
- Format support (XML, JSON)
- Processing status tracking

**Relationships**:
- None (standalone entity)

### 13. IndexJournalSetting Entity
**File**: `src/main/java/com/teckiz/journalindex/entity/IndexJournalSetting.java`
**Table**: `index_journal_settings`

**Key Features**:
- Journal-specific settings
- OAI endpoint configuration
- Harvest settings and status
- Error tracking

**Relationships**:
- One-to-one with IndexJournal

## Repository Classes

### 1. CompanyRepository
**File**: `src/main/java/com/teckiz/journalindex/repository/CompanyRepository.java`

**Key Methods**:
- `findByCompanyKey(String companyKey)`
- `findByName(String name)`
- `findBySlug(String slug)`
- `findByActiveTrue()`
- `findByCountry(String country)`
- `findByMasterTrue()`

### 2. IndexJournalRepository
**File**: `src/main/java/com/teckiz/journalindex/repository/IndexJournalRepository.java`

**Key Methods**:
- `findByJournalKey(String journalKey)`
- `findByCompanyId(Long companyId)`
- `findByStatus(String status)`
- `findByEissn(String eissn)`
- `findByPublisher(String publisher)`
- `findByMultipleCriteria(...)`

### 3. IndexJournalSubjectRepository
**File**: `src/main/java/com/teckiz/journalindex/repository/IndexJournalSubjectRepository.java`

**Key Methods**:
- `findBySubjectKey(String subjectKey)`
- `findByCompanyId(Long companyId)`
- `findByActiveTrue()`
- `findByNameContainingAndActive(String name)`

### 4. IndexCountryRepository
**File**: `src/main/java/com/teckiz/journalindex/repository/IndexCountryRepository.java`

**Key Methods**:
- `findByCountryKey(String countryKey)`
- `findByCompanyId(Long companyId)`
- `findByCode(String code)`
- `findByIsoCode(String isoCode)`

### 5. IndexLanguageRepository
**File**: `src/main/java/com/teckiz/journalindex/repository/IndexLanguageRepository.java`

**Key Methods**:
- `findByLanguageKey(String languageKey)`
- `findByCompanyId(Long companyId)`
- `findByCodeA(String codeA)`
- `findByCodeB(String codeB)`

### 6. IndexJournalArticleRepository
**File**: `src/main/java/com/teckiz/journalindex/repository/IndexJournalArticleRepository.java`

**Key Methods**:
- `findByArticleKey(String articleKey)`
- `findByCompanyId(Long companyId)`
- `findByDoi(String doi)`
- `findByTitleContaining(String title)`

### 7. IndexJournalVolumeRepository
**File**: `src/main/java/com/teckiz/journalindex/repository/IndexJournalVolumeRepository.java`

**Key Methods**:
- `findByVolumeKey(String volumeKey)`
- `findByCompanyId(Long companyId)`
- `findByIndexJournalId(Long journalId)`
- `findByVolumeNumber(String volumeNumber)`

### 8. IndexJournalAuthorRepository
**File**: `src/main/java/com/teckiz/journalindex/repository/IndexJournalAuthorRepository.java`

**Key Methods**:
- `findByIndexJournalArticleId(Long articleId)`
- `findByName(String name)`
- `findByEmail(String email)`
- `findByOrcid(String orcid)`

### 9. IndexRelatedMediaRepository
**File**: `src/main/java/com/teckiz/journalindex/repository/IndexRelatedMediaRepository.java`

**Key Methods**:
- `findByRelatedMediaKey(String relatedMediaKey)`
- `findByCompanyId(Long companyId)`
- `findByMediaType(String mediaType)`
- `findByRelatedMediaType(String relatedMediaType)`

### 10. IndexJournalPageRepository
**File**: `src/main/java/com/teckiz/journalindex/repository/IndexJournalPageRepository.java`

**Key Methods**:
- `findByCompanyId(Long companyId)`
- `findByIndexJournalId(Long journalId)`
- `findByType(String type)`
- `findByUrl(String url)`

### 11. IndexImportQueueRepository
**File**: `src/main/java/com/teckiz/journalindex/repository/IndexImportQueueRepository.java`

**Key Methods**:
- `findByQueueKey(String queueKey)`
- `findBySystemType(String systemType)`
- `findByIndexedTrue()`
- `findByErrorTrue()`

## Database Schema

### Key Relationships
1. **Company** is the central entity with relationships to all other entities
2. **IndexJournal** is the main journal entity with relationships to subjects, languages, and settings
3. **IndexJournalVolume** contains journal volumes and issues
4. **IndexJournalArticle** contains individual articles within volumes
5. **IndexJournalAuthor** contains author information for articles
6. **IndexJournalSubject**, **IndexCountry**, and **IndexLanguage** provide reference data
7. **IndexRelatedMedia** and **IndexJournalPage** provide additional journal resources

### Indexes
- Primary keys on all tables
- Unique constraints on key fields (company_key, journal_key, etc.)
- Search indexes on frequently queried fields
- Foreign key indexes for relationship performance

## Integration with OAI Harvesting

### OaiHarvestService Updates
The `OaiHarvestService` has been updated to:
1. Create or find a default company for OAI harvested journals
2. Create `IndexJournal` entities from OAI data
3. Extract and populate journal metadata
4. Create `IndexJournalSetting` entities with OAI configuration
5. Handle journal settings and OAI endpoint configuration

### Data Flow
1. SQS message received with website URL
2. OAI endpoint discovered
3. OAI data harvested and parsed
4. Company created/found
5. IndexJournal created with metadata
6. IndexJournalSetting created with OAI configuration
7. Data saved to database

## Configuration

### JPA Configuration
- Spring Data JPA enabled
- Hibernate as JPA provider
- MySQL 8.0 dialect
- Connection pooling with HikariCP
- Transaction management enabled

### Database Configuration
- MySQL 8.0+ required
- UTF-8 character set
- InnoDB storage engine
- Proper foreign key constraints
- Indexes for performance

## Usage Examples

### Creating a Company
```java
Company company = new Company();
company.setName("Example Publishing");
company.setDescription("Academic publisher");
company.setActive(true);
company = companyRepository.save(company);
```

### Creating a Journal
```java
IndexJournal journal = new IndexJournal();
journal.setName("Journal of Example Research");
journal.setPublisher("Example Publishing");
journal.setEissn("1234-5678");
journal.setCompany(company);
journal = indexJournalRepository.save(journal);
```

### Finding Journals by Company
```java
List<IndexJournal> journals = indexJournalRepository.findByCompanyId(companyId);
```

### Searching Journals
```java
List<IndexJournal> journals = indexJournalRepository.findByMultipleCriteria(
    "Research", "Example Publishing", "US", "approved", null
);
```

## Benefits

1. **Comprehensive Data Model**: Complete representation of the journal indexing system
2. **Flexible Relationships**: Support for complex organizational structures
3. **Efficient Queries**: Optimized repository methods for common operations
4. **Data Integrity**: Proper foreign key constraints and validation
5. **Scalability**: Indexed fields and connection pooling for performance
6. **Maintainability**: Clean entity relationships and repository patterns

This entity structure provides a solid foundation for the journal indexing system while maintaining compatibility with the OAI harvesting functionality.
