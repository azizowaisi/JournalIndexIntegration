# Journal Index Integration

A lightweight serverless AWS Lambda function built with Java 17 and plain JDBC for processing batched journal article messages from SQS and persisting them to MySQL database. Optimized for minimal memory footprint and fast cold starts.

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

- **Java 17**: Lightweight runtime optimized for Lambda
- **Plain JDBC**: Direct database access without ORM overhead
- **HikariCP**: Lightweight connection pool (minimal configuration)
- **MySQL 9.1.0**: Relational database
- **AWS Lambda**: Serverless compute (Java 17 runtime)
- **AWS SQS**: Message queuing for article batches
- **Jackson 2.18.2**: JSON processing
- **Log4j2**: Structured logging
- **Serverless Framework 4.x**: Deployment automation

---

## Features

### Core Features
- ✅ **Batch Processing**: Handles up to 50 articles per SQS message
- ✅ **Independent Transactions**: Each article processed in separate transaction
- ✅ **Error Resilience**: One failed article doesn't affect others in batch
- ✅ **Lightweight Architecture**: Plain JDBC without Spring/Hibernate overhead
- ✅ **Duplicate Detection**: Checks existing articles by URL before insert
- ✅ **Author Parsing**: Extracts and saves multiple authors per article
- ✅ **Volume Management**: Creates journal volumes as needed
- ✅ **Minimal Memory**: Optimized for 256MB Lambda memory
- ✅ **Fast Cold Starts**: Lightweight initialization without framework overhead

### Advanced Features
- **Connection Pooling**: HikariCP with minimal configuration (0 min idle, max 5 connections)
- **VPC Integration**: Private subnet deployment with RDS access
- **Comprehensive Logging**: Detailed CloudWatch logs for debugging
- **Ultra-Lightweight**: No Spring/Hibernate dependencies (significantly smaller JAR)
- **Memory Optimized**: 256MB memory with 128MB heap allocation
- **DAO Pattern**: Clean data access layer using plain JDBC
- **Multi-Environment**: Separate local and production configurations
- **CI/CD Ready**: GitHub Actions workflows included

---

## Architecture

### Processing Flow

```
SQS (Article Batch) → Lambda Handler → JsonArticleProcessor
                              ↓
                   ┌──────────┴──────────┐
                   ↓                     ↓
            DAO Layer (JDBC)    Process Each Article
                   ↓                     ↓
            Find/Create Journal   Independent Transaction
                   ↓                     ↓
            Find/Create Volume    Save Article + Authors
                   ↓                     ↓
              MySQL Database ← Direct JDBC Operations
```

### Detailed Flow

#### Lambda Invocation (processes up to 5 SQS messages):
1. Lambda receives up to 5 SQS messages per invocation (batchSize: 5)
2. **For each SQS message**:
   - Parse message → Extract ArticleBatch (up to 50 articles)
   - **For each article independently**:
     - Start new transaction
     - Find or create IndexJournal
     - Find or create IndexJournalVolume
     - Check if article exists (by URL)
     - Insert or update IndexJournalArticle
     - Parse and save IndexJournalAuthor(s)
     - Commit transaction
   - Return success/error counts for that message
3. Return overall processing summary

#### Error Handling:
- **Article-level**: Article #1 fails → Transaction rolled back for #1 only
- **Message-level**: Articles #2-50 in same message → Continue processing normally
- **Invocation-level**: Failed messages don't affect other messages in batch
- Final result: "Batch processed: 49 success, 1 errors out of 50 articles" (per message)

**Maximum throughput**: Up to 250 articles per Lambda invocation (5 messages × 50 articles)

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

### Tables Required

The system expects the following tables to exist in the database:

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

**Note**: This application uses plain JDBC and does not automatically create tables. Tables must be created manually or via a migration tool (Liquibase/Flyway).

The DAO layer expects the following table structure to exist:

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
│   │   ├── db/
│   │   │   └── DatabaseManager.java  # HikariCP connection manager
│   │   ├── dao/                   # Lightweight DAO layer (JDBC)
│   │   │   ├── JournalDao.java
│   │   │   ├── VolumeDao.java
│   │   │   ├── ArticleDao.java
│   │   │   └── AuthorDao.java
│   │   ├── entity/                # Plain POJO entities
│   │   │   ├── IndexJournal.java
│   │   │   ├── IndexJournalVolume.java
│   │   │   ├── IndexJournalArticle.java
│   │   │   ├── IndexJournalAuthor.java
│   │   │   └── ... (other entities)
│   │   ├── model/                 # Data transfer objects
│   │   │   ├── SqsArticleMessage.java
│   │   │   ├── ArticleModel.java
│   │   │   └── ArticleAuthorModel.java
│   │   └── service/               # Business logic
│   │       └── JsonArticleProcessor.java  # Main processing service
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

- Java 21 (Amazon Corretto or OpenJDK)
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

### DAO Methods

```java
// JournalDao - Find or create journal
IndexJournal findOrCreateByJournalKey(String journalKey, String website, String publisher);

// VolumeDao - Find or create volume
IndexJournalVolume findOrCreateByJournalIdAndVolumeNumber(Long journalId, String volumeNumber);

// ArticleDao - Find article or save new
Optional<IndexJournalArticle> findByPageURL(String pageUrl);
IndexJournalArticle save(IndexJournalArticle article);

// AuthorDao - Save authors for article
void saveAuthors(Long articleId, List<String> authorNames);
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
    memorySize: 256        # Optimized memory allocation
    timeout: 300           # 5 minutes max
    vpc:
      securityGroupIds:
        - ${env:VPC_SECURITY_GROUP_ID}
      subnetIds:
        - ${env:VPC_SUBNET_ID_1}
        - ${env:VPC_SUBNET_ID_2}
    events:
      - sqs:
          arn: ${env:SQS_QUEUE_ARN}
          batchSize: 5     # Process 5 SQS messages at a time
          functionResponseType: ReportBatchItemFailures
    environment:
      JAVA_TOOL_OPTIONS: "-XX:MaxHeapSize=128m -XX:+UseG1GC -XX:MaxMetaspaceSize=64m"
```

### Database Configuration

The application uses `DatabaseManager` for connection management:

```java
// DatabaseManager.java - Lightweight HikariCP configuration
HikariConfig config = new HikariConfig();
config.setMinimumIdle(0);              // No connections during init
config.setMaximumPoolSize(5);          // Max 5 connections
config.setConnectionTimeout(5000);     // 5 seconds
config.setIdleTimeout(300000);         // 5 minutes
config.setMaxLifetime(600000);         // 10 minutes
config.setRegisterMbeans(false);       // No JMX overhead
```

Configuration via environment variables:
- `DB_URL`: JDBC connection URL (or extracted from DB_URL)
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `MYSQL_HOST`: MySQL host (auto-extracted if not set)
- `MYSQL_PORT`: MySQL port (default: 3306)
- `MYSQL_DATABASE`: Database name
- `MYSQL_SSL_MODE`: SSL mode (REQUIRED/FALSE)
- `MYSQL_CONNECTION_TIMEOUT`: Connection timeout (ms)
- `MYSQL_SOCKET_TIMEOUT`: Socket timeout (ms)

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
    <Logger name="com.zaxxer.hikari" level="WARN"/>
  </Loggers>
</Configuration>
```

---

## Performance

### Current Metrics

| Metric | Value | Notes |
|--------|-------|-------|
| **JAR Size** | ~20-25 MB | Ultra-lightweight (no Spring/Hibernate) |
| **Cold Start** | <2s | Lightweight JDBC initialization |
| **Warm Start** | <100ms | Typical invocation |
| **Batch Processing** | 50 articles | 500-2000ms depending on DB |
| **Memory Usage** | ~128-150MB | With 256MB allocated (128MB heap) |
| **Database Connections** | 0-5 | HikariCP pool (0 min idle) |

### Configuration by Environment

| Setting | Local | Production |
|---------|-------|------------|
| Memory | 256 MB | 256 MB |
| Heap Size | 128 MB | 128 MB |
| Timeout | 300s (5 min) | 300s (5 min) |
| SQS Batch Size | 5 messages | 5 messages |
| Articles per Message | 50 | 50 |
| VPC | Yes | Yes |
| Log Retention | 7 days | 30 days |

### Optimization Highlights

**Architecture Transformation**:
- Removed Spring Framework (entirely)
- Removed Spring Boot
- Removed Spring Data JPA
- Removed Hibernate ORM
- Implemented lightweight DAO pattern with plain JDBC
- Minimal dependencies: JDBC, HikariCP, Jackson, Log4j2 only

**Result**: 
- **JAR Size**: ~20-25MB (vs 36MB+ with Spring)
- **Memory**: 256MB (vs 1024MB+ with Spring/Hibernate)
- **Cold Start**: Faster initialization without framework overhead
- **Cost**: Significantly reduced Lambda costs

### Performance Tips

1. **Database**: Use RDS Proxy for connection pooling (optional)
2. **Memory**: 256MB optimized for cost-effective operation
3. **Connection Pool**: 0 min idle for Lambda (connections created on-demand)
4. **Batch Size**: 5 SQS messages per invocation = up to 250 articles (optimal throughput)
5. **Transactions**: Independent per article for max throughput
6. **Heap Size**: 128MB allows ~128MB for non-heap (metaspace, code cache, etc.)

---

## Troubleshooting

### Common Issues

#### 1. Table Doesn't Exist

**Error**: `Table 'database.IndexJournalArticle' doesn't exist`

**Solution**: Create tables manually or use a migration tool. This application uses plain JDBC and does not auto-create tables. Use Liquibase or Flyway for schema management.

#### 2. Database Connection Timeout

**Error**: `Communications link failure`

**Solutions**:
- Check VPC security group allows MySQL (port 3306)
- Verify Lambda is in correct VPC subnets
- Confirm RDS security group allows Lambda security group
- Check database endpoint and credentials

#### 3. Memory Issues (OutOfMemoryError)

**Error**: `java.lang.OutOfMemoryError: Java heap space`

**Solution**: 
- Current configuration: 256MB memory, 128MB heap
- If you see OOM errors, increase memory to 384MB or 512MB in `serverless.yml`
- Monitor CloudWatch metrics for actual memory usage

#### 4. Transaction Rollback Errors

**Error**: `Transaction rolled back`

**Cause**: Database constraint violations or data errors

**Solution**: Check CloudWatch logs for specific SQL errors. Each article transaction is independent, so one failure doesn't affect others.

#### 5. Connection Pool Exhaustion

**Error**: `Connection is not available, request timed out after 5000ms`

**Cause**: Too many concurrent requests or pool size too small

**Solution**: 
- Current pool: max 5 connections, min 0 idle
- Increase `maximumPoolSize` in `DatabaseManager.java` if needed
- Consider using RDS Proxy for better connection management

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

Add these secrets in: `Settings → Secrets and variables → Actions → Repository secrets`

#### ✅ Already Configured (You have these)

```
AWS_ACCESS_KEY_ID              # Your AWS access key
AWS_SECRET_ACCESS_KEY          # Your AWS secret key  
AWS_REGION                     # ap-south-1
SERVERLESS_ACCESS_KEY          # Serverless Framework key
DEPLOYMENT_BUCKET_NAME         # teckiz-deployment-bucket (used as SERVERLESS_DEPLOYMENT_BUCKET)
S3_BUCKET_NAME                 # index-journal-files
DB_URL                         # jdbc:mysql://teckiz-prod-sql8...
DB_USERNAME                    # teckiz
DB_PASSWORD                    # Your DB password
DB_NAME                        # teckiz_test
SQS_QUEUE_ARN                  # arn:aws:sqs:ap-south-1:518624980012:journal-integration-queue
SQS_QUEUE_URL                  # https://sqs.ap-south-1.amazonaws.com/518624980012/...
SONAR_TOKEN                    # SonarCloud token
SONAR_PROJECT_KEY              # SonarCloud project key
SONAR_ORGANIZATION             # SonarCloud organization
```

#### ✅ All Secrets Now Configured!

**VPC Configuration** (recently added):

| Secret Name | Value | Status |
|-------------|-------|--------|
| `VPC_ID` | `vpc-09dca361` | ✅ Added |
| `VPC_CIDR` | `172.31.0.0/16` | ✅ Added |
| `VPC_SECURITY_GROUP_ID` | `sg-06a9ab999bcbf0681` | ✅ Added |
| `VPC_SUBNET_ID_1` | `subnet-81e887e9` | ✅ Added |
| `VPC_SUBNET_ID_2` | `subnet-3044987c` | ✅ Added |

**Notes**: 
- `MYSQL_HOST` is **automatically extracted** from `DB_URL` in the workflow
- `MYSQL_PORT` is hardcoded to `3306` (standard MySQL port)
- `MYSQL_DATABASE` uses the `DB_NAME` secret
- `MYSQL_SSL_MODE`, timeouts are hardcoded to production values

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
# - Checks Java 21, Maven, Serverless CLI
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

### Why Plain JDBC Instead of Spring/Hibernate?

**Previous**: Spring Boot + Spring Data JPA + Hibernate  
**Current**: Plain JDBC with DAO pattern  

**Benefits**:
- **Significantly Smaller JAR**: ~20-25MB (vs 36MB+ with Spring)
- **Lower Memory**: 256MB (vs 1024MB+ with Spring/Hibernate)
- **Faster Cold Starts**: No Spring context initialization
- **Simpler**: Direct JDBC operations, no ORM overhead
- **Lower Cost**: Reduced Lambda memory costs
- **More Control**: Direct SQL control, easier debugging

### Why Independent Transactions?

**Problem**: Batch transaction failed if one article had error  
**Solution**: Each article in own transaction with explicit commit/rollback  

**Code**:
```java
// processBatch() - No transaction annotation
public String processBatch(SqsArticleMessage message) {
    for (Article article : message.getArticles()) {
        processArticleData(article);  // Each has own transaction
    }
}

// processArticleData() - Explicit transaction management
private void processArticleData(...) {
    Connection conn = DatabaseManager.getConnection();
    conn.setAutoCommit(false);
    try {
        // Save article operations
        conn.commit();
    } catch (Exception e) {
        conn.rollback();
        throw e;
    }
}
```

### Why 256MB Memory?

**Lightweight Architecture**:
- No Spring Framework overhead: ~200MB saved
- No Hibernate ORM overhead: ~100MB saved
- Plain JDBC: Minimal memory footprint
- Small connection pool (0 min idle): Lower memory usage

**Configuration**:
```yaml
memorySize: 256
environment:
  JAVA_TOOL_OPTIONS: "-XX:MaxHeapSize=128m -XX:+UseG1GC -XX:MaxMetaspaceSize=64m"
```

**Result**: Significant cost reduction while maintaining performance

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
- ✅ **Removed Spring Framework/Hibernate** (migrated to lightweight plain JDBC)
- ✅ **Implemented DAO pattern** with plain JDBC operations
- ✅ **Memory optimized** to 256MB (128MB heap)
- ✅ **Java 17 runtime** for optimal Lambda performance
- ✅ Removed S3 SDK (direct JSON from SQS)
- ✅ Updated all libraries to latest stable versions
- ✅ JAR size significantly reduced (no Spring/Hibernate overhead)
- ✅ Fixed Log4j2 configuration
- ✅ Fixed `received_at` and `updated_at` timestamp fields
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
- Max SQS messages per invocation: **5** (configurable via `batchSize`)
- Lambda timeout: **300 seconds** (5 minutes)
- Lambda memory: **256 MB** (128MB heap)
- Database connections: **5** (HikariCP pool, 0 min idle)

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
