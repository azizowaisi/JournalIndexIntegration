-- Database schema for Journal Index Integration
CREATE DATABASE IF NOT EXISTS journal_index;
USE journal_index;

-- Companies table - Main organization entity
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_key VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) UNIQUE,
    slug VARCHAR(64) UNIQUE NOT NULL,
    address VARCHAR(255),
    stripe_id VARCHAR(255),
    description TEXT,
    aboutus TEXT,
    city VARCHAR(255),
    country VARCHAR(2),
    time_zone VARCHAR(255),
    is_active BOOLEAN DEFAULT FALSE,
    is_archived BOOLEAN DEFAULT FALSE,
    email VARCHAR(255),
    phone VARCHAR(255),
    map_location TEXT,
    map_coordinates TEXT,
    logo TEXT,
    logo_size TEXT,
    favicon TEXT,
    start_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    workingdays VARCHAR(255),
    holidays VARCHAR(255),
    privacy_policy TEXT,
    lang VARCHAR(10),
    is_master BOOLEAN DEFAULT FALSE,
    master_company_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (master_company_id) REFERENCES companies(id) ON DELETE SET NULL,
    INDEX idx_company_key (company_key),
    INDEX idx_name (name),
    INDEX idx_slug (slug),
    INDEX idx_is_active (is_active)
);

-- Index Journal Subjects table
CREATE TABLE IF NOT EXISTS index_journal_subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_key VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_subject_key (subject_key, name),
    INDEX idx_company_id (company_id)
);

-- Index Countries table
CREATE TABLE IF NOT EXISTS index_countries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    country_key VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100),
    code VARCHAR(20),
    iso_code VARCHAR(3),
    is_active BOOLEAN DEFAULT TRUE,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_country_key (country_key),
    INDEX idx_company_id (company_id)
);

-- Index Languages table
CREATE TABLE IF NOT EXISTS index_languages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    language_key VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255),
    code_a VARCHAR(20),
    code_b VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_language_key (language_key),
    INDEX idx_company_id (company_id)
);

-- Index Journal Articles table
CREATE TABLE IF NOT EXISTS index_journal_articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_key VARCHAR(20) UNIQUE NOT NULL,
    received_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    published_at TIMESTAMP NULL,
    doi VARCHAR(50),
    publisher_record_id VARCHAR(50),
    article_type VARCHAR(255),
    pages VARCHAR(100),
    page_url VARCHAR(255),
    title_text TEXT,
    keywords_text TEXT,
    abstract_text TEXT,
    references_text TEXT,
    company_id BIGINT NOT NULL,
    index_journal_volume_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (index_journal_volume_id) REFERENCES index_journal_volumes(id) ON DELETE SET NULL,
    INDEX idx_article_page (article_key, doi, publisher_record_id),
    INDEX idx_article_publish_at (published_at),
    INDEX idx_company_id (company_id),
    INDEX idx_volume_id (index_journal_volume_id)
);

-- Index Journal Volumes table
CREATE TABLE IF NOT EXISTS index_journal_volumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    volume_key VARCHAR(50) UNIQUE NOT NULL,
    publisher_record_id VARCHAR(50),
    published_at TIMESTAMP NULL,
    vol_number VARCHAR(255),
    issue_number VARCHAR(255),
    page_url VARCHAR(255),
    cover_image_url VARCHAR(255),
    company_id BIGINT NOT NULL,
    index_journal_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (index_journal_id) REFERENCES index_journals(id) ON DELETE CASCADE,
    INDEX idx_volume_key (volume_key),
    INDEX idx_volume_publish_date (published_at),
    INDEX idx_company_id (company_id),
    INDEX idx_journal_id (index_journal_id)
);

-- Index Journal Authors table
CREATE TABLE IF NOT EXISTS index_journal_authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id VARCHAR(255),
    name VARCHAR(255),
    affiliation_id VARCHAR(255),
    affiliation TEXT,
    country VARCHAR(10),
    email VARCHAR(255),
    orcid VARCHAR(255),
    biography TEXT,
    index_journal_article_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (index_journal_article_id) REFERENCES index_journal_articles(id) ON DELETE CASCADE,
    INDEX idx_author_name (name),
    INDEX idx_article_id (index_journal_article_id)
);

-- Index Related Media table
CREATE TABLE IF NOT EXISTS index_related_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    related_media_key VARCHAR(255) UNIQUE NOT NULL,
    reference_key VARCHAR(255),
    mimetype VARCHAR(64),
    media_type VARCHAR(64),
    related_media_type VARCHAR(64),
    location TEXT,
    name VARCHAR(255),
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_related_media_key (related_media_key, reference_key, related_media_type),
    INDEX idx_company_id (company_id)
);

-- Index Journal Pages table
CREATE TABLE IF NOT EXISTS index_journal_pages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20),
    url VARCHAR(255),
    company_id BIGINT NOT NULL,
    index_journal_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (index_journal_id) REFERENCES index_journals(id) ON DELETE CASCADE,
    INDEX idx_company_id (company_id),
    INDEX idx_journal_id (index_journal_id)
);

-- Index Import Queue table
CREATE TABLE IF NOT EXISTS index_import_queues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    queue_key VARCHAR(20) UNIQUE NOT NULL,
    data TEXT,
    format VARCHAR(50) DEFAULT 'xml',
    total_records VARCHAR(10),
    indexed_records VARCHAR(10) DEFAULT '0',
    is_indexed BOOLEAN DEFAULT FALSE,
    is_error BOOLEAN DEFAULT FALSE,
    message VARCHAR(255),
    system_type VARCHAR(50),
    company_key VARCHAR(50),
    journal_key VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_queue_key (queue_key),
    INDEX idx_system_type (system_type),
    INDEX idx_company_key (company_key),
    INDEX idx_journal_key (journal_key)
);

-- Index Journals table - Main journal entity
CREATE TABLE IF NOT EXISTS index_journals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    journal_key VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255),
    keywords VARCHAR(255),
    e_issn VARCHAR(255),
    website VARCHAR(255),
    publisher VARCHAR(255),
    society VARCHAR(255),
    start_year VARCHAR(10),
    review_process_type VARCHAR(50),
    submission_date TIMESTAMP NULL,
    approval_date TIMESTAMP NULL,
    approved_by VARCHAR(255),
    status VARCHAR(10) DEFAULT 'received',
    country VARCHAR(10),
    email VARCHAR(255),
    phone VARCHAR(255),
    contact_person VARCHAR(255),
    company_id BIGINT NOT NULL,
    subject_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES index_journal_subjects(id) ON DELETE SET NULL,
    INDEX idx_journal_search (name, keywords, e_issn, country),
    INDEX idx_journal_key (journal_key),
    INDEX idx_company_id (company_id),
    INDEX idx_subject_id (subject_id)
);

-- Index Journal Languages table
CREATE TABLE IF NOT EXISTS index_journal_languages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20),
    code_a VARCHAR(20),
    code_b VARCHAR(20),
    is_primary BOOLEAN DEFAULT FALSE,
    index_journal_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (index_journal_id) REFERENCES index_journals(id) ON DELETE CASCADE,
    INDEX idx_language_code (code_a),
    INDEX idx_journal_id (index_journal_id)
);

-- Index Journal Settings table
CREATE TABLE IF NOT EXISTS index_journal_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    integrated_at TIMESTAMP NULL,
    journal_system VARCHAR(256),
    api_key VARCHAR(256),
    article_index BOOLEAN DEFAULT FALSE,
    impact_factor BOOLEAN DEFAULT FALSE,
    archive_service BOOLEAN DEFAULT FALSE,
    oai_accepted BOOLEAN DEFAULT FALSE,
    oai_scheme VARCHAR(255),
    repository_name VARCHAR(255),
    repository_scheme VARCHAR(255),
    delimiter VARCHAR(255),
    sample_oai_identifier VARCHAR(255),
    message TEXT,
    journal_id BIGINT NOT NULL UNIQUE,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (journal_id) REFERENCES index_journals(id) ON DELETE CASCADE,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_journal_id (journal_id),
    INDEX idx_company_id (company_id)
);


-- Legacy tables for OAI harvesting (keeping for backward compatibility)
CREATE TABLE IF NOT EXISTS journals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(500) NOT NULL,
    title VARCHAR(500),
    issn VARCHAR(20),
    eissn VARCHAR(20),
    publisher VARCHAR(300),
    subject_areas TEXT,
    metadata LONGTEXT,
    oai_endpoint VARCHAR(500),
    last_harvested TIMESTAMP NULL,
    harvest_status ENUM('pending', 'in_progress', 'completed', 'failed') DEFAULT 'pending',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_url (url),
    INDEX idx_issn (issn),
    INDEX idx_status (harvest_status),
    INDEX idx_created_at (created_at)
);

-- Journal records table to store individual OAI records
CREATE TABLE IF NOT EXISTS journal_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    journal_id BIGINT NOT NULL,
    record_identifier VARCHAR(500) NOT NULL,
    title VARCHAR(1000),
    creator VARCHAR(500),
    subject TEXT,
    description TEXT,
    publisher VARCHAR(300),
    date VARCHAR(100),
    type VARCHAR(100),
    format VARCHAR(100),
    language VARCHAR(10),
    rights TEXT,
    raw_metadata LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (journal_id) REFERENCES journals(id) ON DELETE CASCADE,
    INDEX idx_journal_id (journal_id),
    INDEX idx_record_identifier (record_identifier),
    INDEX idx_created_at (created_at)
);

-- Harvest logs table to track harvesting activities
CREATE TABLE IF NOT EXISTS harvest_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    journal_id BIGINT NOT NULL,
    harvest_type ENUM('initial', 'incremental', 'full') DEFAULT 'initial',
    records_harvested INT DEFAULT 0,
    records_processed INT DEFAULT 0,
    records_failed INT DEFAULT 0,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    status ENUM('running', 'completed', 'failed') DEFAULT 'running',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (journal_id) REFERENCES journals(id) ON DELETE CASCADE,
    INDEX idx_journal_id (journal_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
);

-- OAI endpoints table to store discovered OAI endpoints
CREATE TABLE IF NOT EXISTS oai_endpoints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_url VARCHAR(500) NOT NULL UNIQUE,
    repository_name VARCHAR(300),
    admin_email VARCHAR(200),
    earliest_datestamp VARCHAR(50),
    deleted_record_support ENUM('yes', 'no', 'transient', 'persistent') DEFAULT 'no',
    granularity VARCHAR(20) DEFAULT 'YYYY-MM-DD',
    available_metadata_formats TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    last_checked TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_base_url (base_url),
    INDEX idx_is_active (is_active)
);
