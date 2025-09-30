# Project Structure

## Overview
This is a standalone JournalIndexIntegration project that has been separated from the main teckiz6 project.

## Directory Structure
```
/Users/aziz.clipsource/Sites/JournalIndexIntegration/
├── src/
│   ├── main/
│   │   ├── java/com/teckiz/journalindex/
│   │   │   ├── config/           # Spring configuration classes
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── repository/       # Spring Data JPA repositories
│   │   │   ├── service/          # Business logic services
│   │   │   ├── parser/           # OAI data parsing utilities
│   │   │   ├── LambdaHandler.java           # CreatorCommand Lambda
│   │   │   ├── ImportCommandLambdaHandler.java  # ImportCommand Lambda
│   │   │   └── JournalIndexRoute.java       # Camel routes
│   │   └── resources/
│   │       ├── application.properties  # Configuration
│   │       └── schema.sql         # Database schema
│   └── test/
│       ├── java/                  # Test classes
│       └── resources/             # Test resources
├── pom.xml                       # Maven configuration
├── README.md                     # Project documentation
├── deploy.sh                     # Deployment script
├── docker-compose.yml            # Docker configuration
├── Dockerfile                    # Docker image definition
└── *.md                          # Documentation files
```

## Key Components

### Lambda Handlers
- **LambdaHandler.java**: CreatorCommand functionality - fetches data and saves to IndexImportQueue
- **ImportCommandLambdaHandler.java**: ImportCommand functionality - processes IndexImportQueue entries

### Services
- **OaiHarvestService**: Handles data fetching and queue creation
- **ImportCommandService**: Manages queue processing and database mapping
- **IndexQueueImporter**: Routes to appropriate system-specific importers
- **ImportQueueService**: Manages IndexImportQueue operations

### Entities
- **Company**: Main organization entity
- **IndexJournal**: Journal metadata
- **IndexJournalArticle**: Individual articles
- **IndexJournalAuthor**: Article authors
- **IndexJournalVolume**: Journal volumes/issues
- **IndexImportQueue**: Queue for processing data
- **IndexJournalSetting**: OAI configuration settings
- **IndexJournalSubject**: Subject categories
- **IndexJournalLanguage**: Language support
- **IndexCountry**: Country information
- **IndexLanguage**: Language definitions
- **IndexRelatedMedia**: Related media files
- **IndexJournalPage**: Journal-specific pages

### Repositories
All entities have corresponding Spring Data JPA repositories for database operations.

## Build and Deployment

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- AWS CLI configured

### Building
```bash
cd /Users/aziz.clipsource/Sites/JournalIndexIntegration
mvn clean package
```

### Testing
```bash
mvn test
```

### Deployment
```bash
./deploy.sh
```

## Configuration

### Database Configuration
Update `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/journal_index
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### AWS Configuration
Set environment variables or update deployment script:
```bash
export AWS_REGION=us-east-1
export SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/account/queue-name
```

## Documentation
- **README.md**: Main project documentation
- **COMMAND_SEPARATION_SUMMARY.md**: Detailed explanation of the two-command architecture
- **SQS_INTEGRATION_SUMMARY.md**: SQS message processing details
- **ENTITIES_SUMMARY.md**: Database entity documentation

## Migration from teckiz6
This project was originally part of the teckiz6 project but has been separated for:
- **Independence**: Standalone deployment and maintenance
- **Scalability**: Independent scaling and resource allocation
- **Clarity**: Clear separation of concerns
- **Reusability**: Can be used with other projects

## Support
For issues or questions related to this project, refer to the documentation files or contact the development team.
