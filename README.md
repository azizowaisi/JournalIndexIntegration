# Journal Index Integration

A serverless AWS Lambda function built with Java 17, Spring Boot, and Hibernate for processing batched journal article messages from SQS and persisting them to MySQL database with automatic schema management.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Article Processing](#article-processing)
- [SQS Message Format](#sqs-message-format)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)
- [Environment Configuration](#environment-configuration)
- [Deployment](#deployment)
- [Testing](#testing)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)
- [Performance](#performance)

---

## Overview

This service is part of a two-service architecture:
1. **IndexJournalsDataScraping** - Scrapes OAI-PMH data, parses articles to JSON, and sends batched messages
2. **JournalIndexIntegration** (this service) - Processes batched article messages and saves to MySQL database

### Technology Stack

- **Java 17**: Latest LTS with SnapStart support
- **Spring Boot 3.3.5**: Application framework without web components
- **Spring Data JPA**: Repository abstraction
- **Hibernate 6.5.3**: ORM with automatic schema management
- **HikariCP 5.1.0**: High-performance connection pool
- **MySQL 8.4.0**: Relational database
- **AWS Lambda**: Serverless compute with SnapStart
- **AWS SQS**: Message queuing for article batches
- **Jackson 2.18.0**: JSON processing
- **Log4j2 2.24.1**: Structured logging
- **Serverless Framework 4.x**: Deployment automation

---

## Features

### Core Features
- ✅ **Batch Processing**: Handles up to 50 articles per SQS message
- ✅ **Independent Transactions**: Each article processed in separate transaction
- ✅ **Error Resilience**: One failed article doesn't affect others in batch
- ✅ **Auto Schema Management**: Hibernate creates/updates tables automatically
- ✅ **Duplicate Detection**: Checks existing articles by URL before insert
- ✅ **Author Parsing**: Extracts and saves multiple authors per article
- ✅ **Volume Management**: Creates journal volumes as needed
- ✅ **Fast Cold Starts**: SnapStart enabled for near-instant initialization

### Advanced Features
- **Connection Pooling**: HikariCP for optimal database performance
- **VPC Integration**: Private subnet deployment with RDS access
- **Comprehensive Logging**: Detailed CloudWatch logs for debugging
- **JAR Optimization**: Minimal 36MB deployment package
- **Multi-Environment**: Separate local and production configurations
- **CI/CD Ready**: GitHub Actions workflows included

---

## Architecture

### Processing Flow

```
SQS (Article Batch) → Lambda Handler → Spring Boot App
                              ↓
                      JsonArticleProcessor
                              ↓
                   ┌──────────┴──────────┐
                   ↓                     ↓
            Find/Create Journal   Process Each Article
                   ↓                     ↓
            Find/Create Volume    Independent Transaction
                   ↓                     ↓
              MySQL Database ← Save Article + Authors
```

### Detailed Flow

#### For Each SQS Message (ArticleBatch):
1. Parse message → Extract 50 articles
2. **For each article independently**:
   - Start new transaction
   - Find or create IndexJournal
   - Find or create IndexJournalVolume
   - Check if article exists (by URL)
   - Insert or update IndexJournalArticle
   - Parse and save IndexJournalAuthor(s)
   - Commit transaction
3. Return success/error counts

#### Error Handling:
- Article #1 fails → Transaction rolled back for #1 only
- Articles #2-50 → Continue processing normally
- Final result: "Batch processed: 49 success, 1 errors out of 50 articles"

---

## Quick Start

### 1. Prerequisites

```bash
# Required tools
- Java 17 (Amazon Corretto recommended)
- Maven 3.8+ 
- AWS CLI configured
- Serverless Framework 4.x

# Configure AWS credentials
aws configure
```

### 2. Install Dependencies

```bash
mvn clean install
```

### 3. Configure Environment

Update environment files in `environments/` directory:

```bash
# Edit configuration
nano environments/env.local       # For local development
nano environments/env.production  # For production
```

### 4. Run Tests

```bash
mvn test
```

### 5. Build

```bash
mvn clean package -DskipTests
```

### 6. Deploy

```bash
# Using deployment script (recommended)
./scripts/deploy.sh production ap-south-1

# Or manually
source environments/env.production
serverless deploy --stage production --region ap-south-1
```

---

## Article Processing

### Supported Message Types

| Message Type | Description | Articles Per Message |
|--------------|-------------|---------------------|
| `Article` | Single article | 1 |
| `ArticleBatch` | Batch of articles | Up to 50 |

### Processing Logic

**ArticleBatch Processing**:
1. Receive 1 SQS message with 50 articles
2. Process each article in independent transaction
3. Continue on errors (failure isolation)
4. Log success/error counts

**Independent Transaction Benefits**:
- ✅ If article #5 fails → Articles #1-4 already saved
- ✅ Articles #6-50 continue processing normally
- ✅ Maximum data persistence even with partial failures

### Field Mapping

| JSON Field | Database Column | Notes |
|-----------|-----------------|-------|
| `title` | `title_text` | Article title |
| `description` | `abstract_text` | Article abstract |
| `identifier` | `page_url` | Used for duplicate detection |
| `date` | `published_at` | Parsed to LocalDateTime |
| `subjects` | `keywords_text` | Comma-separated keywords |
| `types[0]` | `article_type` | First type from array |
| `sources[0]` | `pages` | Extracted page range (e.g., "39-50") |
| `creator` | → `IndexJournalAuthor` | Split by comma/semicolon |

---

## SQS Message Format

### ArticleBatch Message (Primary Format)

```json
{
  "journalKey": "68653804af297",
  "oaiUrl": "https://pjss.bzu.edu.pk/index.php/pjss",
  "s3Url": "https://index-journal-files.s3.ap-south-1.amazonaws.com/...",
  "s3Key": "2025/10/10/68653804af297-listrecords-page-1/...",
  "s3Path": "s3://index-journal-files/...",
  "s3FileName": "68653804af297-listrecords-page-1_20251010_153216.xml",
  "messageType": "ArticleBatch",
  "source": "scraping-service",
  "pageNumber": 1,
  "batchNumber": 2,
  "totalBatches": 2,
  "articlesInBatch": 50,
  "totalArticlesInPage": 100,
  "totalRecordsProcessed": 100,
  "success": true,
  "errorCode": null,
  "errorMessage": null,
  "timestamp": "2025-10-10T15:32:17.204Z",
  
  "articles": [
    {
      "journal_key": "68653804af297",
      "created_at": "2025-10-10T15:32:17.134Z",
      "type": "ListRecords",
      "title": "Article Title Here",
      "title_lang": "en-US",
      "creator": "Author Name",
      "subjects": ["Keyword 1", "Keyword 2"],
      "description": "Article abstract...",
      "description_lang": "en-US",
      "publisher": "Publisher Name",
      "publisher_lang": "en-US",
      "date": "2022-06-15",
      "types": ["info:eu-repo/semantics/article", "publishedVersion"],
      "format": "application/pdf",
      "identifier": "https://example.com/article/123",
      "sources": ["Journal Vol. 30 No. 2 (2010); 219-233", "ISSN 2708-4175"],
      "language": "eng",
      "relation": "https://example.com/article/123/pdf",
      "datestamp": "2022-06-15T09:45:26Z",
      "setSpec": "journal:ART"
    }
    // ... up to 50 articles
  ]
}
```

---

## Database Schema

### Tables Created Automatically

The system uses Hibernate's `ddl-auto=update` to create tables automatically:

#### IndexJournal
- Primary table for journal information
- Key field: `journal_key` (unique)
- Contains: website, publisher, status, etc.

#### IndexJournalVolume
- Journal volumes (e.g., "Vol. 30 No. 2")
- Foreign key: `index_journal_id`
- Key field: `volume_number`

#### IndexJournalArticle
- Individual articles
- Foreign key: `index_journal_volume_id`
- Unique constraint: `page_url`
- Contains: title, abstract, keywords, DOI, dates, etc.

#### IndexJournalAuthor
- Article authors (one-to-many with articles)
- Foreign key: `index_journal_article_id`
- Contains: author name

### Schema Management

```properties
# Configured in application.properties
spring.jpa.hibernate.ddl-auto=update

# Hibernate automatically:
- Creates tables if they don't exist
- Adds new columns when entities change
- Preserves existing data
- Never drops columns or tables
```

**Important**: For production, consider using Liquibase or Flyway for controlled migrations.

---

## Project Structure

```
JournalIndexIntegration/
├── .github/workflows/              # CI/CD pipelines
│   ├── build.yml                  # Build and test (PRs)
│   └── ci-cd.yml                  # Deployment (master branch)
├── environments/                   # Environment configuration
│   ├── env.local                  # Local development settings
│   └── env.production             # Production settings
├── scripts/                        # Deployment scripts
│   └── deploy.sh                  # Main deployment script
├── src/
│   ├── main/java/com/teckiz/journalindex/
│   │   ├── LambdaHandler.java     # Main Lambda entry point
│   │   ├── config/
│   │   │   └── ApplicationConfig.java  # Spring & Hibernate config
│   │   ├── entity/                # JPA entities
│   │   │   ├── IndexJournal.java
│   │   │   ├── IndexJournalVolume.java
│   │   │   ├── IndexJournalArticle.java
│   │   │   ├── IndexJournalAuthor.java
│   │   │   └── ... (other entities)
│   │   ├── model/                 # Data transfer objects
│   │   │   ├── SqsArticleMessage.java
│   │   │   ├── ArticleModel.java
│   │   │   └── ArticleAuthorModel.java
│   │   ├── repository/            # Spring Data JPA repositories
│   │   │   ├── IndexJournalRepository.java
│   │   │   ├── IndexJournalArticleRepository.java
│   │   │   └── IndexJournalVolumeRepository.java
│   │   └── service/               # Business logic
│   │       ├── JsonArticleProcessor.java  # Main processing service
│   │       └── OjsOaiXmlImporter.java     # Legacy XML support
│   └── resources/
│       ├── application.properties # Spring Boot config
│       ├── application-local.properties
│       └── log4j2.xml             # Logging configuration
├── pom.xml                         # Maven dependencies
├── serverless.yml                  # Serverless config
└── README.md                       # This file
```

---

## Environment Configuration

### Available Environments

- **local** - Development environment (`environments/env.local`)
- **production** - Production environment (`environments/env.production`)

### Environment File Structure

```bash
# AWS Configuration
SERVERLESS_DEPLOYMENT_BUCKET=teckiz-deployment-bucket
AWS_REGION=ap-south-1

# Database Configuration
DB_URL=jdbc:mysql://your-rds-endpoint:3306/database_name?useSSL=true&requireSSL=true
DB_USER=your_db_user
DB_PASSWORD=your_db_password

# SQS Configuration
SQS_QUEUE_ARN=arn:aws:sqs:ap-south-1:xxx:journal-integration-queue
SQS_QUEUE_URL=https://sqs.ap-south-1.amazonaws.com/xxx/journal-integration-queue

# VPC Configuration (for RDS access)
VPC_ID=vpc-xxxxxxxxx
VPC_CIDR=10.0.0.0/16
VPC_SECURITY_GROUP_ID=sg-xxxxxxxxx
VPC_SUBNET_ID_1=subnet-xxxxxxxxx
VPC_SUBNET_ID_2=subnet-yyyyyyyyy

# Processing Configuration
FUNCTION_TYPE=integration
LOG_LEVEL=INFO
```

---

## Deployment

### Prerequisites

- Java 17 (Amazon Corretto or OpenJDK)
- Maven 3.8+
- AWS CLI configured with credentials
- Serverless Framework 4.x
- Node.js 22+ (for Serverless Framework)

### Deployment Options

#### Option 1: Using Deployment Script (Recommended)

```bash
# Deploy to production
./scripts/deploy.sh production ap-south-1

# The script automatically:
# 1. Validates environment configuration
# 2. Checks prerequisites
# 3. Runs Maven build
# 4. Deploys to AWS
# 5. Verifies deployment
```

#### Option 2: Manual Deployment

```bash
# Build JAR
mvn clean package -DskipTests

# Load environment variables
set -a
source environments/env.production
set +a

# Deploy
serverless deploy --stage production --region ap-south-1
```

#### Option 3: Using GitHub Actions

Push to `master` branch triggers automatic deployment:
1. Lint and format checks
2. Run tests with coverage
3. Security scanning
4. SonarCloud analysis
5. Build JAR
6. Deploy to AWS
7. Verify deployment

### Post-Deployment

```bash
# View deployment info
serverless info --stage production --region ap-south-1

# View logs
aws logs tail /aws/lambda/journal-index-integration-production-processor --follow

# Test the function
serverless invoke --function journalProcessor --stage production
```

---

## Testing

### Test Coverage

Current test suite includes:

- **Integration tests** for Lambda handler
- **Unit tests** for OAI XML parsing
- **JaCoCo code coverage** reporting

### Running Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Skip tests during build
mvn package -DskipTests
```

---

## Monitoring

### CloudWatch Logs

```bash
# View real-time logs
aws logs tail /aws/lambda/journal-index-integration-production-processor --follow

# Filter errors only
aws logs filter-log-events \
  --log-group-name /aws/lambda/journal-index-integration-production-processor \
  --filter-pattern "ERROR"

# View specific time range
aws logs filter-log-events \
  --log-group-name /aws/lambda/journal-index-integration-production-processor \
  --start-time $(date -u -d '1 hour ago' +%s)000
```

### Key Log Messages

**Successful Batch Processing**:
```
Processing article batch from JSON message
Journal Key: 68653804af297
Message Type: ArticleBatch
Batch Number: 1/2
Articles in Batch: 50
Processing article 1/50 in batch
Processing article: Article Title Here
Using journal ID: 123
Using volume ID: 456
✅ Article saved with ID: 7890
✅ Saved 1 authors
Processing article 2/50 in batch
...
Batch processed: 50 success, 0 errors out of 50 articles
```

**Error Handling Example**:
```
Processing article 5/50 in batch
Error processing article 5/50: Data too long for column 'article_key'
Processing article 6/50 in batch
...
Batch processed: 49 success, 1 errors out of 50 articles
```

### CloudWatch Metrics

Monitor these key metrics:

| Metric | What to Watch |
|--------|---------------|
| **Invocations** | Number of Lambda executions |
| **Errors** | Failed executions (should be near zero) |
| **Duration** | Processing time (typically 500-2000ms for 50 articles) |
| **InitDuration** | Cold start time (<1s with SnapStart!) |
| **Database Connections** | HikariCP pool metrics |
| **SQS Messages** | Queue depth and age |

---

## Database Schema

### Auto-Creation Process

On first deployment, Hibernate automatically creates all tables:

```sql
-- Tables created automatically by Hibernate:

CREATE TABLE Company (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_name VARCHAR(255),
  -- ... other fields
);

CREATE TABLE IndexJournal (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  journal_key VARCHAR(255) UNIQUE,
  website VARCHAR(500),
  publisher VARCHAR(500),
  status VARCHAR(50),
  company_id BIGINT,
  -- ... other fields
  FOREIGN KEY (company_id) REFERENCES Company(id)
);

CREATE TABLE IndexJournalVolume (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  index_journal_id BIGINT NOT NULL,
  volume_number VARCHAR(50),
  -- ... other fields
  FOREIGN KEY (index_journal_id) REFERENCES IndexJournal(id)
);

CREATE TABLE IndexJournalArticle (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  index_journal_volume_id BIGINT,
  title_text TEXT,
  abstract_text TEXT,
  page_url VARCHAR(500) UNIQUE,
  published_at DATETIME,
  keywords_text TEXT,
  pages VARCHAR(50),
  doi VARCHAR(255),
  article_type VARCHAR(100),
  -- ... other fields
  FOREIGN KEY (index_journal_volume_id) REFERENCES IndexJournalVolume(id)
);

CREATE TABLE IndexJournalAuthor (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  index_journal_article_id BIGINT NOT NULL,
  name VARCHAR(500),
  -- ... other fields
  FOREIGN KEY (index_journal_article_id) REFERENCES IndexJournalArticle(id)
);
```

### Repository Methods

```java
// Find journal by unique key
Optional<IndexJournal> findByJournalKey(String journalKey);

// Find article by URL (duplicate detection)
Optional<IndexJournalArticle> findByPageURL(String pageUrl);

// Find volume by journal and number
Optional<IndexJournalVolume> findByIndexJournalIdAndVolumeNumber(
    Long journalId, 
    String volumeNumber
);
```

---

## Configuration Details

### Lambda Configuration

```yaml
# serverless.yml
functions:
  journalProcessor:
    handler: com.teckiz.journalindex.LambdaHandler
    runtime: java17
    memorySize: 2048       # Higher memory = more CPU
    timeout: 900           # 15 minutes max
    snapStart: true        # Fast cold starts!
    vpc:
      securityGroupIds:
        - ${env:VPC_SECURITY_GROUP_ID}
      subnetIds:
        - ${env:VPC_SUBNET_ID_1}
        - ${env:VPC_SUBNET_ID_2}
    events:
      - sqs:
          arn: ${env:SQS_QUEUE_ARN}
          batchSize: 1     # Process 1 SQS message at a time
          functionResponseType: ReportBatchItemFailures
```

### Spring Boot Configuration

```properties
# application.properties

# Database
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# HikariCP Connection Pool
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

### Logging Configuration

```xml
<!-- log4j2.xml -->
<Configuration status="ERROR">
  <Appenders>
    <Console name="Lambda" target="SYSTEM_OUT">
      <PatternLayout>
        <Pattern>%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1} - %m%n</Pattern>
      </PatternLayout>
    </Console>
  </Appenders>
  
  <Loggers>
    <Root level="INFO">
      <AppenderRef ref="Lambda"/>
    </Root>
    
    <!-- Reduce framework noise -->
    <Logger name="org.springframework" level="WARN"/>
    <Logger name="org.hibernate" level="WARN"/>
    <Logger name="com.zaxxer.hikari" level="WARN"/>
  </Loggers>
</Configuration>
```

---

## Performance

### Current Metrics

| Metric | Value | Notes |
|--------|-------|-------|
| **JAR Size** | 36 MB | Optimized with dependency exclusions |
| **Cold Start** | <1s | With SnapStart enabled |
| **Warm Start** | <100ms | Typical invocation |
| **Batch Processing** | 50 articles | 500-2000ms depending on DB |
| **Memory Usage** | ~1GB | With 2GB allocated |
| **Database Connections** | 1-5 | HikariCP pool |

### Configuration by Environment

| Setting | Local | Production |
|---------|-------|------------|
| Memory | 2048 MB | 2048 MB |
| Timeout | 900s (15 min) | 900s (15 min) |
| SQS Batch Size | 1 message | 1 message |
| Articles per Message | 50 | 50 |
| SnapStart | Enabled | Enabled |
| VPC | Yes | Yes |
| Log Retention | 7 days | 30 days |

### Optimization Highlights

**JAR Size Reduction**:
- Excluded Spring Boot web components
- Excluded unnecessary Spring Data metrics
- Excluded JAXB from Hibernate
- Excluded Protobuf from MySQL connector
- Removed transitive logging dependencies

**Result**: **47MB → 36MB** (23% reduction)

### Performance Tips

1. **Database**: Use RDS Proxy for connection pooling
2. **Memory**: 2GB gives more CPU allocation
3. **SnapStart**: Enabled for sub-second cold starts
4. **Batch Size**: 1 SQS message = 50 articles (optimal)
5. **Transactions**: Independent per article for max throughput

---

## Troubleshooting

### Common Issues

#### 1. Table Doesn't Exist

**Error**: `Table 'database.IndexJournalArticle' doesn't exist`

**Solution**: Ensure `spring.jpa.hibernate.ddl-auto=update` is set in `application.properties`

```properties
spring.jpa.hibernate.ddl-auto=update
```

#### 2. Database Connection Timeout

**Error**: `Communications link failure`

**Solutions**:
- Check VPC security group allows MySQL (port 3306)
- Verify Lambda is in correct VPC subnets
- Confirm RDS security group allows Lambda security group
- Check database endpoint and credentials

#### 3. SnapStart Optimization Off

**Check**: 
```bash
aws lambda get-function --function-name journal-index-integration-production-processor:32
```

**Should show**: `"OptimizationStatus": "On"`

If "Off", redeploy or publish new version.

#### 4. Transaction Rollback Errors

**Error**: `Transaction silently rolled back`

**Cause**: Multiple `@Transactional` annotations nesting

**Solution**: Ensure only `processArticleData()` has `@Transactional`, not `processBatch()`

#### 5. Null ID in Entity Error

**Error**: `null id in com.teckiz.journalindex.entity.IndexJournalArticle entry`

**Cause**: Hibernate session corrupted by failed transaction

**Solution**: Remove `@Transactional` from batch method (already fixed)

#### 6. Log4j2 Format Errors

**Error**: `Unrecognized format specifier [d]`

**Cause**: Conflicting logging configurations

**Solution**: Proper Log4j2 pattern in `log4j2.xml` (already configured)

#### 7. Data Too Long for Column

**Error**: `Data truncation: Data too long for column 'article_key'`

**Solution**: 
- Check database column size
- Article continues processing (error isolation working!)
- Increase column size if needed: `ALTER TABLE IndexJournalArticle MODIFY article_key VARCHAR(500);`

---

## CI/CD Pipeline

### GitHub Actions Workflows

#### Build Workflow (`.github/workflows/build.yml`)
- **Triggered on**: Pull requests to master, develop, integration-work-flow
- **Steps**:
  1. Lint and format check
  2. Run tests with JaCoCo coverage
  3. Security scan with Trivy
  4. Build verification

#### CI/CD Workflow (`.github/workflows/ci-cd.yml`)
- **Triggered on**: Push to `master` branch
- **Steps**:
  1. Lint and format check
  2. Run tests with coverage
  3. Security scan
  4. SonarCloud analysis
  5. Build JAR
  6. Deploy to AWS production
  7. Verify deployment
  8. Post-deployment checks

### Required GitHub Secrets

```
# AWS Credentials
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION

# Serverless
SERVERLESS_ACCESS_KEY
SERVERLESS_DEPLOYMENT_BUCKET

# Database
DB_URL
DB_USER
DB_PASSWORD

# SQS
SQS_QUEUE_ARN
SQS_QUEUE_URL

# VPC
VPC_ID
VPC_CIDR
VPC_SECURITY_GROUP_ID
VPC_SUBNET_ID_1
VPC_SUBNET_ID_2

# SonarCloud (Optional)
SONAR_TOKEN
SONAR_PROJECT_KEY
SONAR_ORGANIZATION
```

---

## Advanced Topics

### Batch Processing Details

**Lambda receives**: 1 SQS message  
**Message contains**: 50 articles  
**Processing**:
```java
for (int i = 0; i < 50; i++) {
    @Transactional
    processArticleData(article[i]);  // Independent transaction
}
```

**Benefits**:
- Article #1 fails → Rolled back independently
- Articles #2-50 → Continue normally
- Maximum data persistence

### Volume Extraction

From source string: `"Pakistan Journal; Vol. 30 No. 2 (2010); 219-233"`

**Extracted**:
- Volume: `"30"`
- Pages: `"219-233"`

**Regex patterns**:
```java
// Volume: "Vol. 30" or "Volume 30"
Pattern.compile("Vol\\.?\\s*(\\d+)", CASE_INSENSITIVE)

// Pages: "219-233"
Pattern.compile("(\\d+)-(\\d+)")
```

### Date Parsing

Supports multiple formats:
```java
// ISO date: "2022-06-15"
LocalDateTime.parse("2022-06-15T00:00:00")

// ISO timestamp: "2025-10-10T15:32:17.134Z"
LocalDateTime.parse("2025-10-10T15:32:17")

// Year only: "2009"
LocalDateTime.parse("2009-01-01T00:00:00")
```

### Author Parsing

From creator string: `"Smith, John; Doe, Jane; Brown, Bob"`

**Split by**: Comma (`,`) or semicolon (`;`)  
**Result**: 3 separate `IndexJournalAuthor` records

---

## Batch Processing Example

### Input: 1 SQS Message

```json
{
  "messageType": "ArticleBatch",
  "journalKey": "68653804af297",
  "articlesInBatch": 50,
  "articles": [ /* 50 article objects */ ]
}
```

### Processing Flow

```
1. Parse SQS message
2. Validate journalKey and messageType
3. Loop through 50 articles:
   
   Article #1:
   ├─ Start transaction
   ├─ Find/Create journal
   ├─ Find/Create volume
   ├─ Check for duplicate (by URL)
   ├─ Save article
   ├─ Parse & save authors
   └─ Commit transaction ✅
   
   Article #2:
   ├─ Start NEW transaction
   ├─ ... (same steps)
   └─ Commit transaction ✅
   
   Article #5:
   ├─ Start NEW transaction
   ├─ ... (processing)
   └─ ERROR! Rollback ❌ (only #5)
   
   Article #6:
   ├─ Start NEW transaction (continues!)
   ├─ ... (same steps)
   └─ Commit transaction ✅
   
   ... articles #7-50 continue normally

4. Return: "Batch processed: 49 success, 1 errors out of 50 articles"
```

### Database Impact

**Before (single transaction for batch)**:
- Article #5 fails → **All 50 articles rolled back**
- Database writes: **0 articles**

**After (independent transactions)**:
- Article #5 fails → **Only #5 rolled back**
- Database writes: **49 articles saved** ✅

---

## Integration with Scraping Service

### Message Flow

```
Scraping Service (Node.js)
         ↓
  Sends ArticleBatch to SQS
  (50 articles per message)
         ↓
Integration Service (This - Java)
         ↓
   Saves to MySQL Database
```

### Compatibility

This service is compatible with:
- **IndexJournalsDataScraping** v1.0.2+ (with batching)
- **IndexJournalsDataScraping** v1.0.1 (single article messages)

Both message types are supported:
- `messageType: "Article"` → Single article
- `messageType: "ArticleBatch"` → Batch of 50 articles

---

## Development Workflow

### Local Development

```bash
# 1. Setup
git clone <repository>
cd JournalIndexIntegration
mvn clean install

# 2. Configure local environment
cp environments/env.local environments/env.local.custom
nano environments/env.local.custom
# Update with your local database and AWS settings

# 3. Run tests
mvn test

# 4. Build
mvn clean package

# 5. Deploy to local
source environments/env.local.custom
serverless deploy --stage local --region ap-south-1
```

### Making Changes

```bash
# 1. Create feature branch
git checkout -b feature/your-feature

# 2. Make changes

# 3. Run tests
mvn test

# 4. Build
mvn clean package -DskipTests

# 5. Commit
git add .
git commit -m "feat: your feature description"

# 6. Push and create PR
git push origin feature/your-feature
```

---

## Scripts Reference

### Available Scripts

| Script | Description |
|--------|-------------|
| `./scripts/deploy.sh [stage] [region]` | Full deployment workflow |
| `mvn clean` | Clean build artifacts |
| `mvn test` | Run all tests |
| `mvn package` | Build JAR file |
| `mvn clean package -DskipTests` | Quick build without tests |

### Deployment Script Usage

```bash
# Full deployment with all checks
./scripts/deploy.sh production ap-south-1

# What it does:
# - Validates environment configuration
# - Checks Java 17, Maven, Serverless CLI
# - Runs Maven tests
# - Builds optimized JAR (36MB)
# - Deploys to AWS Lambda
# - Verifies function is running
# - Shows deployment summary
```

---

## Security

### Best Practices

- ✅ **VPC Isolation**: Lambda runs in private subnets
- ✅ **Least Privilege IAM**: Minimal required permissions
- ✅ **Database Encryption**: RDS encryption at rest
- ✅ **TLS/SSL**: All database connections encrypted
- ✅ **No Secrets in Code**: All credentials in environment variables
- ✅ **CloudWatch Monitoring**: All actions logged
- ✅ **Security Scanning**: Trivy scans in CI/CD

### IAM Permissions Required

```yaml
# Minimal IAM permissions for Lambda:
- logs:CreateLogGroup
- logs:CreateLogStream
- logs:PutLogEvents
- sqs:ReceiveMessage
- sqs:DeleteMessage
- sqs:GetQueueAttributes
- ec2:CreateNetworkInterface
- ec2:DescribeNetworkInterfaces
- ec2:DeleteNetworkInterface
```

---

## Architecture Decisions

### Why Spring Boot (No Apache Camel)?

**Previous**: Used Apache Camel for routing  
**Current**: Pure Spring Boot  

**Benefits**:
- **Smaller JAR**: 47MB → 36MB (23% reduction)
- **Simpler**: Less framework overhead
- **Faster**: No Camel context initialization
- **Clearer**: Direct Spring service calls

### Why Independent Transactions?

**Problem**: Batch transaction failed if one article had error  
**Solution**: Each article in own transaction  

**Code**:
```java
// processBatch() - NO @Transactional
public String processBatch(SqsArticleMessage message) {
    for (Article article : message.getArticles()) {
        processArticleData(article);  // Has @Transactional
    }
}

// processArticleData() - HAS @Transactional
@Transactional
private void processArticleData(...) {
    // Save article in independent transaction
}
```

### Why SnapStart?

**Without SnapStart**:
- Cold start: **8-12 seconds**
- Slow Spring/Hibernate initialization

**With SnapStart**:
- Cold start: **<1 second** ✅
- Pre-initialized snapshot restored instantly

**Configuration**:
```yaml
snapStart: true
```

---

## Quick Reference

### Essential Commands

```bash
# Build and Test
mvn clean install          # Full build with tests
mvn clean package -DskipTests  # Quick build
mvn test                   # Run tests only

# Deployment
./scripts/deploy.sh production ap-south-1  # Deploy to production
serverless deploy --stage production       # Direct deploy
serverless remove --stage production       # Remove deployment

# Monitoring
aws logs tail /aws/lambda/journal-index-integration-production-processor --follow
serverless info --stage production
aws lambda get-function --function-name journal-index-integration-production-processor

# Database
mysql -h your-rds-endpoint -u user -p database_name
SELECT COUNT(*) FROM IndexJournalArticle;
SELECT * FROM IndexJournalArticle ORDER BY id DESC LIMIT 10;
```

### Environment Files

- `environments/env.local` - Local development
- `environments/env.production` - Production deployment

### AWS Resources Created

| Resource | Name Pattern |
|----------|-------------|
| Lambda Function | `journal-index-integration-{stage}-processor` |
| CloudWatch Log Group | `/aws/lambda/journal-index-integration-{stage}-processor` |
| IAM Role | Auto-generated by Serverless Framework |
| Event Source Mapping | Auto-generated (Lambda ↔ SQS) |

**Example for production**:
- Function: `journal-index-integration-production-processor`
- Log Group: `/aws/lambda/journal-index-integration-production-processor`

---

## Related Services

### IndexJournalsDataScraping

The scraping service that sends messages to this integration service:
- **Technology**: Node.js 22, Serverless
- **Function**: Scrapes OAI-PMH endpoints
- **Output**: ArticleBatch messages (50 articles each)
- **Repository**: `../IndexJournalsDataScraping`

### Message Contract

Both services must agree on message format (see [SQS Message Format](#sqs-message-format))

---

## Support & Maintenance

### Getting Help

1. **Check Logs**: CloudWatch logs for detailed execution traces
2. **Run Tests**: `mvn test` to verify functionality
3. **Check Status**: `serverless info` for deployment details
4. **Review Config**: Verify `environments/env.production` settings
5. **Monitor Metrics**: Check CloudWatch dashboard

### Common Maintenance Tasks

```bash
# Update dependencies
mvn versions:display-dependency-updates
mvn versions:use-latest-releases

# Redeploy after changes
mvn clean package -DskipTests
./scripts/deploy.sh production ap-south-1

# View recent logs
aws logs tail /aws/lambda/journal-index-integration-production-processor --since 1h

# Check database
mysql -h endpoint -u user -p -e "SELECT COUNT(*) FROM IndexJournalArticle"

# Update Lambda environment variables
aws lambda update-function-configuration \
  --function-name journal-index-integration-production-processor \
  --environment Variables="{LOG_LEVEL=DEBUG}"
```

---

## Version

**Current Version**: 1.0.0

### Changelog

**v1.0.0** (Latest)
- ✅ Implemented ArticleBatch processing (50 articles per message)
- ✅ Independent transaction per article for error isolation
- ✅ Removed Apache Camel (pure Spring Boot)
- ✅ Removed S3 SDK (direct JSON from SQS)
- ✅ Added SnapStart for <1s cold starts
- ✅ Updated all libraries to latest stable versions
- ✅ JAR size optimized to 36MB
- ✅ Fixed Log4j2 configuration
- ✅ Auto schema management with Hibernate
- ✅ Duplicate detection by article URL
- ✅ Volume extraction from sources
- ✅ Author parsing and relationship management

---

## Technical Details

### Message Type Routing

```java
// LambdaHandler routes based on messageType
if ("Article".equalsIgnoreCase(messageType)) {
    processor.processArticle(message);  // Single article
} else if ("ArticleBatch".equalsIgnoreCase(messageType)) {
    processor.processBatch(message);    // 50 articles
}
```

### Processing Limits

- Max articles per batch: **50**
- Max SQS messages per invocation: **1** (configurable via `batchSize`)
- Lambda timeout: **900 seconds** (15 minutes)
- Lambda memory: **2048 MB**
- Database connections: **5** (HikariCP pool)

### Error Handling Strategy

The system uses **fail-soft** approach:

1. **Article Level**: Individual article failures don't affect batch
2. **Batch Level**: Batch processing continues on individual errors
3. **Lambda Level**: Returns partial success with error counts
4. **SQS Level**: Uses `ReportBatchItemFailures` for selective retry

**Result**: Maximum data persistence even with partial failures! 🎯

---

## License

MIT License - See LICENSE file for details

---

**Made with ❤️ by Teckiz**
