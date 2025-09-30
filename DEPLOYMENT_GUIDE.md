# Deployment Guide

## Prerequisites

### Required Software
- Java 11 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- AWS CLI configured
- Docker (optional)

### AWS Requirements
- AWS Account with appropriate permissions
- SQS Queue created
- Lambda execution role
- RDS MySQL instance (or local MySQL)

## Setup Instructions

### 1. Clone/Download Project
```bash
cd /Users/aziz.clipsource/Sites
# Project is already located at JournalIndexIntegration/
```

### 2. Database Setup
```bash
# Create MySQL database
mysql -u root -p
CREATE DATABASE journal_index;
USE journal_index;

# Run schema
mysql -u root -p journal_index < src/main/resources/schema.sql
```

### 3. Configuration
Update `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/journal_index
spring.datasource.username=your_username
spring.datasource.password=your_password

# AWS Configuration
aws.region=us-east-1
sqs.queue.url=https://sqs.us-east-1.amazonaws.com/account/queue-name
```

### 4. Build Project
```bash
cd /Users/aziz.clipsource/Sites/JournalIndexIntegration
mvn clean package
```

### 5. Test Locally
```bash
# Run tests
mvn test

# Test with sample data
mvn exec:java -Dexec.mainClass="com.teckiz.journalindex.LambdaHandler"
```

## AWS Deployment

### 1. Create IAM Role
Create a Lambda execution role with permissions for:
- SQS (Read/Write)
- RDS (if using RDS)
- CloudWatch Logs

### 2. Deploy CreatorCommand Lambda
```bash
# Build JAR
mvn clean package

# Deploy using AWS CLI
aws lambda create-function \
  --function-name journal-index-creator \
  --runtime java11 \
  --role arn:aws:iam::account:role/lambda-execution-role \
  --handler com.teckiz.journalindex.LambdaHandler \
  --zip-file fileb://target/journal-index-integration-1.0.0.jar \
  --timeout 300 \
  --memory-size 512
```

### 3. Deploy ImportCommand Lambda
```bash
aws lambda create-function \
  --function-name journal-index-importer \
  --runtime java11 \
  --role arn:aws:iam::account:role/lambda-execution-role \
  --handler com.teckiz.journalindex.ImportCommandLambdaHandler \
  --zip-file fileb://target/journal-index-integration-1.0.0.jar \
  --timeout 180 \
  --memory-size 256
```

### 4. Configure SQS Trigger
```bash
# Create SQS trigger for CreatorCommand
aws lambda create-event-source-mapping \
  --event-source-arn arn:aws:sqs:region:account:queue-name \
  --function-name journal-index-creator \
  --batch-size 1
```

### 5. Schedule ImportCommand
```bash
# Create CloudWatch Events rule for ImportCommand
aws events put-rule \
  --name journal-index-importer-schedule \
  --schedule-expression "rate(5 minutes)"

# Add Lambda target
aws events put-targets \
  --rule journal-index-importer-schedule \
  --targets "Id"="1","Arn"="arn:aws:lambda:region:account:function:journal-index-importer"
```

## Docker Deployment

### 1. Build Docker Image
```bash
docker build -t journal-index-integration .
```

### 2. Run with Docker Compose
```bash
docker-compose up -d
```

### 3. Environment Variables
Update `docker-compose.yml` with your configuration:
```yaml
environment:
  - DB_URL=jdbc:mysql://mysql:3306/journal_index
  - DB_USERNAME=root
  - DB_PASSWORD=password
  - AWS_REGION=us-east-1
  - SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/account/queue-name
```

## Monitoring and Logging

### CloudWatch Logs
- CreatorCommand logs: `/aws/lambda/journal-index-creator`
- ImportCommand logs: `/aws/lambda/journal-index-importer`

### Metrics to Monitor
- Lambda invocations
- Lambda errors
- Lambda duration
- SQS message count
- Database connections

### Alerts
Set up CloudWatch alarms for:
- High error rate
- Long execution times
- SQS queue depth

## Testing

### 1. Send Test SQS Message
```bash
aws sqs send-message \
  --queue-url https://sqs.us-east-1.amazonaws.com/account/queue-name \
  --message-body '{"url":"https://example.com/index.php/journal","journal_key":"JRN_123456789"}'
```

### 2. Check Lambda Logs
```bash
aws logs tail /aws/lambda/journal-index-creator --follow
aws logs tail /aws/lambda/journal-index-importer --follow
```

### 3. Verify Database
```sql
-- Check import queue entries
SELECT * FROM index_import_queues ORDER BY created_at DESC LIMIT 10;

-- Check journal data
SELECT * FROM index_journals ORDER BY created_at DESC LIMIT 10;
```

## Troubleshooting

### Common Issues

#### 1. Database Connection
- Verify MySQL is running
- Check connection string
- Ensure database exists
- Verify user permissions

#### 2. AWS Permissions
- Check Lambda execution role
- Verify SQS permissions
- Ensure CloudWatch Logs access

#### 3. Memory Issues
- Increase Lambda memory allocation
- Check for memory leaks in code
- Monitor CloudWatch metrics

#### 4. Timeout Issues
- Increase Lambda timeout
- Optimize database queries
- Check external API response times

### Debug Commands
```bash
# Check Lambda function status
aws lambda get-function --function-name journal-index-creator

# Check SQS queue attributes
aws sqs get-queue-attributes --queue-url https://sqs.us-east-1.amazonaws.com/account/queue-name --attribute-names All

# Check CloudWatch metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/Lambda \
  --metric-name Invocations \
  --dimensions Name=FunctionName,Value=journal-index-creator \
  --start-time 2024-01-01T00:00:00Z \
  --end-time 2024-01-01T23:59:59Z \
  --period 3600 \
  --statistics Sum
```

## Maintenance

### Regular Tasks
1. Monitor CloudWatch logs for errors
2. Check database performance
3. Review Lambda metrics
4. Update dependencies
5. Backup database

### Updates
1. Update code
2. Build new JAR
3. Deploy to Lambda
4. Test functionality
5. Monitor for issues

## Support
For issues or questions:
1. Check CloudWatch logs
2. Review this documentation
3. Check AWS service status
4. Contact development team
