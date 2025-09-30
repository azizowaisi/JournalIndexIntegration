# SQS Integration Summary

## Overview
The JournalIndexIntegration service has been updated to handle SQS messages containing `url` and `journal_key`, and automatically detect the system type to create appropriate IndexImportQueue entries with file locations saved in the `data` column.

## SQS Message Format
```json
{
  "url": "https://example.com/journal",
  "journal_key": "JRN_123456789"
}
```

## System Type Detection
The service automatically detects the system type based on the URL:

### OJS OAI System
- **Detection**: URLs containing `/index.php/` or `/oai`
- **Example**: `https://example.com/index.php/journal`
- **Process**:
  1. Calls `/oai?verb=Identify` to get repository information
  2. Calls `/oai?verb=ListRecords&metadataPrefix=oai_dc` to get records
  3. Creates two IndexImportQueue entries:
     - `ojs-identify` with identify response
     - `ojs-record-list` with records response

### DOAJ System
- **Detection**: URLs containing `doaj.org`
- **Example**: `https://doaj.org/api/v2/journals/123`
- **Process**:
  1. Calls the DOAJ API endpoint
  2. Creates one IndexImportQueue entry:
     - `doaj` with API response

### Teckiz System
- **Detection**: URLs containing `teckiz` or `journal`
- **Example**: `https://teckiz.com/journal/123`
- **Process**:
  1. Makes API calls to get journal data
  2. Creates one IndexImportQueue entry:
     - `teckiz` with JSON response

## IndexImportQueue Data Storage
The `data` column in IndexImportQueue stores the complete response from the system:

### OJS OAI Identify
```xml
<?xml version="1.0" encoding="UTF-8"?>
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/">
  <responseDate>2024-01-01T00:00:00Z</responseDate>
  <request verb="Identify">https://example.com/oai</request>
  <Identify>
    <repositoryName>Example Journal Repository</repositoryName>
    <baseURL>https://example.com/oai</baseURL>
    <!-- ... more identify data ... -->
  </Identify>
</OAI-PMH>
```

### OJS OAI Records
```xml
<?xml version="1.0" encoding="UTF-8"?>
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/">
  <responseDate>2024-01-01T00:00:00Z</responseDate>
  <request verb="ListRecords" metadataPrefix="oai_dc">https://example.com/oai</request>
  <ListRecords>
    <record>
      <header>
        <identifier>oai:example.com:123</identifier>
        <datestamp>2024-01-01</datestamp>
      </header>
      <metadata>
        <oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/">
          <dc:title>Article Title</dc:title>
          <dc:creator>Author Name</dc:creator>
          <!-- ... more metadata ... -->
        </oai_dc:dc>
      </metadata>
    </record>
    <!-- ... more records ... -->
  </ListRecords>
</OAI-PMH>
```

### DOAJ Response
```json
{
  "id": "123456789",
  "title": "Journal Title",
  "issn": "1234-5678",
  "eissn": "9876-5432",
  "publisher": "Publisher Name",
  "subjects": ["Computer Science", "Information Technology"],
  "languages": ["English"],
  "country": "US"
}
```

### Teckiz Response
```json
{
  "url": "https://teckiz.com/journal/123",
  "system": "TECKIZ",
  "data": "placeholder",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Processing Flow

1. **SQS Message Received**: Lambda handler receives message with `url` and `journal_key`
2. **URL Validation**: Ensures URL has proper format (adds https:// if missing)
3. **System Detection**: Analyzes URL to determine system type
4. **Data Harvesting**: Makes appropriate API calls based on system type
5. **Import Queue Creation**: Creates IndexImportQueue entries with harvested data
6. **Journal Processing**: Creates/updates IndexJournal and related entities
7. **Settings Configuration**: Sets up IndexJournalSetting with OAI configuration

## Key Features

### Automatic System Detection
- **OJS OAI**: Detects Open Journal Systems with OAI-PMH support
- **DOAJ**: Detects Directory of Open Access Journals
- **Teckiz**: Detects internal Teckiz journal system

### Data Persistence
- **IndexImportQueue**: Stores raw response data for processing
- **IndexJournal**: Main journal entity with metadata
- **IndexJournalSetting**: OAI endpoint configuration
- **Company**: Default company for harvested journals

### Error Handling
- **URL Validation**: Ensures proper URL format
- **System Detection**: Handles unknown system types
- **API Failures**: Graceful handling of network errors
- **Data Processing**: Error logging and recovery

## Configuration

### Database Tables
- `index_import_queues`: Stores harvested data
- `index_journals`: Journal metadata
- `index_journal_settings`: OAI configuration
- `companies`: Organization data

### Environment Variables
```properties
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/journal_index
DB_USERNAME=root
DB_PASSWORD=password

# SQS Configuration
SQS_QUEUE_URL=https://sqs.region.amazonaws.com/account/queue-name
```

## Usage Examples

### Sending SQS Message
```bash
aws sqs send-message \
  --queue-url https://sqs.us-east-1.amazonaws.com/123456789012/journal-index-queue \
  --message-body '{"url":"https://example.com/index.php/journal","journal_key":"JRN_123456789"}'
```

### Processing Flow
1. Message received by Lambda handler
2. URL `https://example.com/index.php/journal` detected as OJS OAI
3. System calls `/oai?verb=Identify` and `/oai?verb=ListRecords`
4. Creates IndexImportQueue entries with XML responses
5. Processes data to create IndexJournal and settings
6. Returns success response

## Monitoring and Logging

### Log Levels
- **INFO**: Normal processing flow
- **WARN**: Non-critical issues (missing data, unknown systems)
- **ERROR**: Critical failures (API errors, database issues)

### Key Metrics
- **Processing Time**: Time to process each SQS message
- **Success Rate**: Percentage of successful processing
- **System Distribution**: Breakdown by detected system type
- **Data Volume**: Amount of data harvested per journal

## Future Enhancements

### Additional System Support
- **Crossref**: Support for Crossref API
- **PubMed**: Support for PubMed/PMC
- **Custom APIs**: Configurable API endpoints

### Performance Optimizations
- **Batch Processing**: Process multiple records in batches
- **Caching**: Cache frequently accessed data
- **Parallel Processing**: Concurrent processing of multiple journals

### Monitoring Improvements
- **Metrics Dashboard**: Real-time processing metrics
- **Alerting**: Automated error notifications
- **Health Checks**: System health monitoring
