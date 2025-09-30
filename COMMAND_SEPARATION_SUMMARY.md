# Command Separation Summary

## Overview
The JournalIndexIntegration project has been updated to separate the two-step process that exists in your PHP system:

1. **CreatorCommand** (Lambda): Fetches data from OAI and saves to IndexImportQueue
2. **ImportCommand** (Separate Lambda): Processes IndexImportQueue entries and maps data to database

## Architecture Separation

### Step 1: CreatorCommand (Data Fetching)
**File**: `LambdaHandler.java` + `JournalIndexRoute.java` + `OaiHarvestService.java`

**Responsibilities**:
- Receive SQS messages with `url` and `journal_key`
- Detect system type (OJS, DOAJ, Teckiz)
- Fetch data from appropriate APIs
- Save raw data to IndexImportQueue
- Return success/failure status

**Process Flow**:
```
SQS Message → URL Detection → API Fetching → IndexImportQueue Creation
```

### Step 2: ImportCommand (Data Processing)
**File**: `ImportCommandLambdaHandler.java` + `ImportCommandService.java` + `IndexQueueImporter.java`

**Responsibilities**:
- Process IndexImportQueue entries
- Parse XML/JSON data
- Map data to database entities
- Update import queue status
- Handle errors and retries

**Process Flow**:
```
IndexImportQueue → Data Parsing → Database Mapping → Status Update
```

## Service Architecture

### CreatorCommand Services

#### LambdaHandler
- **Purpose**: Main entry point for SQS messages
- **Function**: Parse SQS message, validate inputs, route to processing
- **Input**: SQS event with `url` and `journal_key`
- **Output**: Success/failure response

#### JournalIndexRoute
- **Purpose**: Camel routes for data fetching
- **Function**: System detection, API calls, data harvesting
- **Routes**:
  - `direct:processWebsite` - Main processing route
  - `direct:detectSystemType` - URL analysis
  - `direct:harvestOjsOai` - OJS OAI data fetching
  - `direct:harvestDoaj` - DOAJ data fetching
  - `direct:harvestTeckiz` - Teckiz data fetching

#### OaiHarvestService
- **Purpose**: Create IndexImportQueue entries
- **Function**: Save harvested data to queue
- **Methods**:
  - `process()` - Main processing method
  - `createImportQueueEntries()` - Queue creation
  - `findOrCreateDefaultCompany()` - Company management

### ImportCommand Services

#### ImportCommandLambdaHandler
- **Purpose**: Main entry point for import processing
- **Function**: Process one or all pending queue entries
- **Methods**:
  - `handleRequest()` - Process one entry
  - `processAllPending()` - Batch processing
  - `getStats()` - Statistics

#### ImportCommandService
- **Purpose**: Core import processing logic
- **Function**: Queue management, progress tracking, error handling
- **Methods**:
  - `processOneImportQueue()` - Process single entry
  - `processAllPendingQueues()` - Batch processing
  - `getImportQueueStats()` - Statistics
  - `resetFailedQueues()` - Retry failed entries

#### IndexQueueImporter
- **Purpose**: Route to appropriate importer based on system type
- **Function**: System-specific processing dispatch
- **Handlers**:
  - `DoajXmlImporter` - DOAJ XML processing
  - `OjsOaiXmlImporter` - OJS OAI XML processing
  - `JsonImporter` - Teckiz JSON processing

## Data Flow

### CreatorCommand Flow
```
SQS Message
    ↓
LambdaHandler (parse message)
    ↓
JournalIndexRoute (detect system, fetch data)
    ↓
OaiHarvestService (create IndexImportQueue entries)
    ↓
IndexImportQueue (raw data stored)
```

### ImportCommand Flow
```
IndexImportQueue (pending entries)
    ↓
ImportCommandLambdaHandler (trigger processing)
    ↓
ImportCommandService (get next entry)
    ↓
IndexQueueImporter (route by system type)
    ↓
Specific Importer (parse and map data)
    ↓
Database Entities (IndexJournal, etc.)
```

## IndexImportQueue Structure

### Data Storage
The `data` column stores complete API responses:

**OJS OAI Identify**:
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

**OJS OAI Records**:
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

**DOAJ Response**:
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

## Deployment Strategy

### CreatorCommand Lambda
- **Trigger**: SQS events
- **Function**: Data fetching and queue creation
- **Timeout**: 5 minutes (for large datasets)
- **Memory**: 512MB (for XML processing)

### ImportCommand Lambda
- **Trigger**: CloudWatch Events (scheduled) or SQS
- **Function**: Queue processing and database mapping
- **Timeout**: 3 minutes (per queue entry)
- **Memory**: 256MB (for database operations)

## Monitoring and Logging

### CreatorCommand Metrics
- **SQS Messages Processed**: Count of messages handled
- **System Type Distribution**: Breakdown by detected system
- **API Response Times**: Performance of external APIs
- **Queue Creation Rate**: IndexImportQueue entries created

### ImportCommand Metrics
- **Queue Processing Rate**: Entries processed per minute
- **Success/Failure Rate**: Processing success percentage
- **Processing Time**: Time per queue entry
- **Database Operations**: Entity creation/updates

## Benefits of Separation

### Scalability
- **Independent Scaling**: Each Lambda can scale based on its workload
- **Resource Optimization**: Different memory/timeout requirements
- **Parallel Processing**: Multiple import processes can run simultaneously

### Reliability
- **Fault Isolation**: Failures in one step don't affect the other
- **Retry Logic**: Failed imports can be retried without re-fetching data
- **Data Persistence**: Raw data is preserved in IndexImportQueue

### Maintainability
- **Clear Separation**: Each service has a single responsibility
- **Independent Deployment**: Services can be updated separately
- **Easier Testing**: Each component can be tested in isolation

## Configuration

### CreatorCommand Configuration
```properties
# SQS Configuration
SQS_QUEUE_URL=https://sqs.region.amazonaws.com/account/creator-queue

# API Timeouts
OAI_TIMEOUT=120
DOAJ_TIMEOUT=30
TECKIZ_TIMEOUT=60
```

### ImportCommand Configuration
```properties
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/journal_index
DB_USERNAME=root
DB_PASSWORD=password

# Processing Configuration
BATCH_SIZE=10
MAX_RETRIES=3
RETRY_DELAY=300
```

## Future Enhancements

### CreatorCommand Improvements
- **Caching**: Cache frequently accessed OAI endpoints
- **Rate Limiting**: Respect API rate limits
- **Data Validation**: Validate data before saving to queue

### ImportCommand Improvements
- **Batch Processing**: Process multiple entries in one Lambda invocation
- **Data Transformation**: More sophisticated data mapping
- **Error Recovery**: Advanced retry and error handling strategies
