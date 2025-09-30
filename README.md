# Journal Index Integration

A standalone fanout Lambda service built with Apache Camel that processes SQS messages containing website URLs, discovers OAI (Open Archives Initiative) endpoints, harvests journal data, and stores it in MySQL.

## Project Location
This project is now located at: `/Users/aziz.clipsource/Sites/JournalIndexIntegration`

## Overview
This service provides a two-step process for journal data integration:
1. **CreatorCommand**: Fetches data from OAI endpoints and saves to IndexImportQueue
2. **ImportCommand**: Processes IndexImportQueue entries and maps data to database entities

## Features

- **SQS Integration**: Processes messages from AWS SQS containing website URLs
- **OAI Discovery**: Automatically discovers OAI endpoints from website URLs
- **Data Harvesting**: Harvests journal metadata using OAI-PMH protocol
- **MySQL Storage**: Stores harvested data in MySQL database with comprehensive entity relationships
- **Entity Management**: Full CRUD operations for companies, journals, subjects, languages, and countries
- **JPA Integration**: Spring Data JPA repositories for efficient database operations
- **Error Handling**: Comprehensive error handling and retry mechanisms
- **Logging**: Detailed logging for monitoring and debugging

## Architecture

```
SQS Queue → Lambda Function → Apache Camel Routes → MySQL Database
                ↓
        OAI Endpoint Discovery
                ↓
        Data Harvesting & Parsing
                ↓
        Database Storage
```

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- AWS CLI configured
- Docker (optional, for local testing)

## Setup

### 1. Database Setup

Create the MySQL database and tables:

```sql
mysql -u root -p < src/main/resources/schema.sql
```

### 2. Environment Variables

Set the following environment variables:

```bash
export DB_URL="jdbc:mysql://localhost:3306/journal_index?useSSL=false&serverTimezone=UTC"
export DB_USERNAME="your_username"
export DB_PASSWORD="your_password"
export AWS_REGION="us-east-1"
export SQS_QUEUE_URL="https://sqs.us-east-1.amazonaws.com/123456789012/journal-index-queue"
```

### 3. Build the Project

```bash
mvn clean package
```

## Deployment

### AWS Lambda Deployment

1. **Create Lambda Function**:
   ```bash
   aws lambda create-function \
     --function-name journal-index-integration \
     --runtime java11 \
     --role arn:aws:iam::123456789012:role/lambda-execution-role \
     --handler com.teckiz.journalindex.LambdaHandler \
     --zip-file fileb://target/journal-index-integration-1.0.0.jar
   ```

2. **Configure SQS Trigger**:
   ```bash
   aws lambda create-event-source-mapping \
     --event-source-arn arn:aws:sqs:us-east-1:123456789012:journal-index-queue \
     --function-name journal-index-integration \
     --batch-size 10
   ```

3. **Set Environment Variables**:
   ```bash
   aws lambda update-function-configuration \
     --function-name journal-index-integration \
     --environment Variables='{
       "DB_URL":"jdbc:mysql://your-rds-endpoint:3306/journal_index",
       "DB_USERNAME":"your_username",
       "DB_PASSWORD":"your_password",
       "AWS_REGION":"us-east-1"
     }'
   ```

### Local Testing

1. **Start MySQL**:
   ```bash
   docker run --name mysql-journal -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=journal_index -p 3306:3306 -d mysql:8.0
   ```

2. **Run Tests**:
   ```bash
   mvn test
   ```

3. **Run Locally** (for testing):
   ```bash
   java -jar target/journal-index-integration-1.0.0.jar
   ```

## Usage

### Sending Messages to SQS

Send a message to the SQS queue with a website URL:

```bash
aws sqs send-message \
  --queue-url https://sqs.us-east-1.amazonaws.com/123456789012/journal-index-queue \
  --message-body "https://example.com"
```

### Message Format

The SQS message should contain a simple string with the website URL:

```
https://example.com
```

## Configuration

### Database Configuration

The application uses the following database tables:

#### Core Entities
- **companies**: Main organization entity with company information
- **index_journals**: Main journal entity with journal metadata
- **index_journal_subjects**: Journal subject categories
- **index_journal_languages**: Languages supported by journals
- **index_countries**: Country information
- **index_languages**: Language information
- **index_journal_settings**: Journal-specific settings including OAI endpoints
- **index_journal_articles**: Journal articles with metadata
- **index_journal_volumes**: Journal volumes and issues
- **index_journal_authors**: Article authors and affiliations
- **index_related_media**: Related media files and resources
- **index_journal_pages**: Journal-specific pages (editorial board, guidelines, etc.)
- **index_import_queues**: Import queue for processing data

#### Legacy Tables (for backward compatibility)
- **journals**: Legacy journal storage
- **journal_records**: Individual OAI records
- **harvest_logs**: Harvesting activity logs
- **oai_endpoints**: Discovered OAI endpoints

### OAI Configuration

- **Default Metadata Prefix**: `oai_dc`
- **Batch Size**: 100 records per request
- **Timeout**: 30 seconds

### Error Handling

- **Max Retries**: 3 attempts
- **Retry Delay**: 5 seconds
- **Dead Letter Queue**: Failed messages are logged and can be sent to a DLQ

## Monitoring

### CloudWatch Logs

The application logs to CloudWatch with the following log groups:
- `/aws/lambda/journal-index-integration`

### Database Monitoring

Monitor the following tables for processing status:
- `harvest_logs` - Processing status and timing
- `journals` - Journal metadata and status
- `journal_records` - Individual record processing

## Troubleshooting

### Common Issues

1. **OAI Endpoint Not Found**:
   - Check if the website supports OAI-PMH
   - Verify the URL format and accessibility

2. **Database Connection Issues**:
   - Verify database credentials and connection string
   - Check network connectivity and security groups

3. **SQS Message Processing**:
   - Check Lambda function logs in CloudWatch
   - Verify SQS queue permissions

### Logs

Check the following log levels for debugging:
- `INFO`: General processing information
- `DEBUG`: Detailed SQL and HTTP operations
- `ERROR`: Error conditions and stack traces

## Development

### Adding New OAI Metadata Formats

1. Update the `OaiDataParser` class
2. Add new parsing logic for the metadata format
3. Update the database schema if needed

### Customizing Data Processing

1. Modify the `OaiHarvestService` class
2. Update the Camel routes in `JournalIndexRoute`
3. Add new database fields as needed

## License

This project is licensed under the MIT License - see the LICENSE file for details.
