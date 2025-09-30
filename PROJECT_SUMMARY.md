# Journal Index Integration - Project Summary

## Overview
This is a complete Apache Camel-based fanout Lambda service that processes SQS messages containing website URLs, discovers OAI (Open Archives Initiative) endpoints, harvests journal data, and stores it in MySQL.

## Project Structure

```
JournalIndexIntegration/
├── pom.xml                          # Maven configuration with all dependencies
├── README.md                        # Comprehensive documentation
├── deploy.sh                        # AWS Lambda deployment script
├── Dockerfile                       # Docker configuration
├── docker-compose.yml               # Local development setup
├── src/
│   ├── main/
│   │   ├── java/com/teckiz/journalindex/
│   │   │   ├── LambdaHandler.java           # Main Lambda entry point
│   │   │   ├── JournalIndexRoute.java       # Camel route configuration
│   │   │   ├── config/
│   │   │   │   └── DatabaseConfig.java      # MySQL data source configuration
│   │   │   ├── parser/
│   │   │   │   └── OaiDataParser.java       # OAI-PMH XML parser
│   │   │   └── service/
│   │   │       └── OaiHarvestService.java   # OAI harvesting service
│   │   └── resources/
│   │       ├── application.properties       # Application configuration
│   │       └── schema.sql                   # MySQL database schema
│   └── test/
│       ├── java/com/teckiz/journalindex/
│       │   ├── JournalIndexRouteTest.java   # Route testing
│       │   └── parser/
│       │       └── OaiDataParserTest.java   # Parser testing
│       └── resources/
│           └── sample-oai-response.xml      # Test data
```

## Key Features

### 1. **SQS Integration**
- Processes messages from AWS SQS containing website URLs
- Handles batch processing with configurable batch size
- Includes error handling and retry mechanisms

### 2. **OAI Discovery & Harvesting**
- Automatically discovers OAI endpoints from website URLs
- Supports OAI-PMH protocol for metadata harvesting
- Parses XML responses to extract journal information
- Handles multiple metadata formats (primarily oai_dc)

### 3. **Database Storage**
- MySQL integration with connection pooling
- Comprehensive schema for journals, records, and logs
- Supports both journal metadata and individual records
- Tracks harvesting status and error information

### 4. **Error Handling**
- Comprehensive error handling throughout the pipeline
- Retry mechanisms for failed operations
- Detailed logging for monitoring and debugging
- Graceful handling of malformed data

## Technical Stack

- **Java 11**: Core runtime
- **Apache Camel 3.20.2**: Integration framework
- **AWS Lambda**: Serverless execution
- **AWS SQS**: Message queue
- **MySQL 8.0**: Database storage
- **Maven**: Build and dependency management
- **Docker**: Containerization and local development

## Dependencies

### Core Dependencies
- `camel-core`: Apache Camel core functionality
- `camel-aws2-sqs`: SQS integration
- `camel-http`: HTTP client for OAI requests
- `camel-sql`: Database integration
- `camel-jackson`: JSON processing

### AWS Dependencies
- `aws-lambda-java-core`: Lambda runtime
- `aws-lambda-java-events`: SQS event handling

### Database Dependencies
- `mysql-connector-java`: MySQL driver
- `commons-dbcp2`: Connection pooling

## Configuration

### Environment Variables
- `DB_URL`: MySQL connection string
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `AWS_REGION`: AWS region
- `SQS_QUEUE_URL`: SQS queue URL

### Application Properties
- Database connection settings
- OAI configuration (metadata prefix, batch size, timeout)
- Logging configuration
- Error handling settings

## Database Schema

### Tables
1. **journals**: Main journal metadata
2. **journal_records**: Individual OAI records
3. **harvest_logs**: Processing activity logs
4. **oai_endpoints**: Discovered OAI endpoints

### Key Fields
- Journal identification (URL, ISSN, title)
- Metadata storage (JSON format)
- Processing status tracking
- Error logging and retry information

## Deployment

### AWS Lambda
1. Build the project: `mvn clean package`
2. Deploy using the provided script: `./deploy.sh`
3. Configure environment variables
4. Set up SQS trigger

### Local Development
1. Start MySQL: `docker-compose up mysql`
2. Run tests: `mvn test`
3. Start application: `docker-compose up`

## Testing

### Unit Tests
- Route testing with CamelTestSupport
- Parser testing with sample OAI data
- Service testing with mock data

### Integration Tests
- End-to-end testing with real OAI endpoints
- Database integration testing
- SQS message processing testing

## Monitoring

### Logging
- Structured logging with Log4j2
- CloudWatch integration for AWS Lambda
- Different log levels for debugging

### Metrics
- Processing time tracking
- Success/failure rates
- Database operation metrics

## Future Enhancements

### Potential Improvements
1. **Additional Metadata Formats**: Support for more OAI metadata formats
2. **Incremental Harvesting**: Only harvest new/updated records
3. **Data Validation**: Enhanced data validation and cleaning
4. **Caching**: Redis integration for performance
5. **Monitoring**: CloudWatch metrics and alarms
6. **Dead Letter Queue**: Enhanced error handling with DLQ

### Scalability Considerations
1. **Batch Processing**: Optimize batch sizes for different workloads
2. **Connection Pooling**: Tune database connection settings
3. **Memory Management**: Optimize Lambda memory allocation
4. **Concurrent Processing**: Handle multiple OAI endpoints simultaneously

## Security Considerations

### Data Protection
- Encrypted database connections
- Secure credential management
- Input validation and sanitization

### AWS Security
- IAM roles with minimal permissions
- VPC configuration for database access
- CloudTrail logging for audit trails

## Maintenance

### Regular Tasks
1. Monitor CloudWatch logs for errors
2. Check database performance and storage
3. Update dependencies and security patches
4. Review and optimize processing performance

### Troubleshooting
1. Check Lambda function logs
2. Verify database connectivity
3. Test OAI endpoint accessibility
4. Review SQS message processing

This project provides a complete, production-ready solution for OAI harvesting with proper error handling, testing, and deployment automation.
