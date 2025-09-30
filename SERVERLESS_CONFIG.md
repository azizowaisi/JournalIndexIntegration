# Serverless Configuration Guide

This document provides detailed information about the serverless configuration for the Journal Index Integration project.

## Overview

The project uses the Serverless Framework to deploy and manage AWS Lambda functions, SQS queues, CloudWatch alarms, and other AWS resources.

## Configuration Files

### 1. `serverless.yml`
Main serverless configuration file containing:
- Function definitions (CreatorCommand, ImportCommand, HealthCheck)
- AWS resource definitions (SQS, CloudWatch, IAM roles)
- Environment variables
- Deployment settings

### 2. `env.example`
Template for environment variables with comprehensive configuration options.

### 3. `package.json`
Node.js dependencies and npm scripts for serverless operations.

## Functions

### CreatorCommand Function
- **Handler**: `com.teckiz.journalindex.LambdaHandler`
- **Trigger**: SQS Queue
- **Timeout**: 300 seconds
- **Memory**: 512 MB
- **Purpose**: Processes SQS messages and creates IndexImportQueue entries

### ImportCommand Function
- **Handler**: `com.teckiz.journalindex.ImportCommandLambdaHandler`
- **Trigger**: CloudWatch Events (scheduled every 5 minutes)
- **Timeout**: 180 seconds
- **Memory**: 256 MB
- **Purpose**: Processes IndexImportQueue entries and maps data to database

### HealthCheck Function
- **Handler**: `com.teckiz.journalindex.LambdaHandler`
- **Trigger**: HTTP API Gateway
- **Timeout**: 30 seconds
- **Memory**: 128 MB
- **Purpose**: Health monitoring endpoint

## AWS Resources

### SQS Queue
- **Name**: `journal-index-queue`
- **Features**:
  - Dead Letter Queue for failed messages
  - Long polling (20 seconds)
  - Message retention: 14 days
  - Visibility timeout: 300 seconds

### CloudWatch
- **Log Groups**: Separate for each function
- **Alarms**: Error monitoring for both main functions
- **Retention**: 14 days for main functions, 7 days for health check

### IAM Roles
- SQS permissions (Receive, Delete, GetQueueAttributes)
- RDS permissions (DescribeDBInstances, DescribeDBClusters)
- CloudWatch Logs permissions
- CloudWatch Events permissions

## Environment Variables

### Required Variables
```bash
DB_URL=jdbc:mysql://localhost:3306/journal_index
DB_USERNAME=your_username
DB_PASSWORD=your_password
SQS_QUEUE_NAME=journal-index-queue
AWS_REGION=us-east-1
```

### Optional Variables
```bash
BATCH_SIZE=10
MAX_RETRIES=3
LOG_LEVEL=INFO
ENABLE_ALARMS=true
```

## Deployment Commands

### Full Deployment
```bash
# Deploy to development
serverless deploy --stage dev

# Deploy to production
serverless deploy --stage prod
```

### Function-Specific Deployment
```bash
# Deploy only CreatorCommand
serverless deploy function -f creatorCommand

# Deploy only ImportCommand
serverless deploy function -f importCommand
```

### Testing
```bash
# Test CreatorCommand
serverless invoke -f creatorCommand

# Test ImportCommand
serverless invoke -f importCommand

# View logs
serverless logs -f creatorCommand --tail
```

## Local Development

### Serverless Offline
```bash
# Install serverless-offline
npm install serverless-offline --save-dev

# Start offline development
serverless offline
```

### Environment Setup
```bash
# Copy environment template
cp env.example .env

# Edit with your values
nano .env
```

## Monitoring and Logging

### CloudWatch Logs
- **CreatorCommand**: `/aws/lambda/journal-index-integration-dev-creator`
- **ImportCommand**: `/aws/lambda/journal-index-integration-dev-importer`
- **HealthCheck**: `/aws/lambda/journal-index-integration-dev-health`

### CloudWatch Alarms
- **CreatorCommand Errors**: Triggers when error count > 5 in 10 minutes
- **ImportCommand Errors**: Triggers when error count > 5 in 10 minutes

### Metrics to Monitor
- Function invocations
- Function duration
- Function errors
- SQS queue depth
- Dead letter queue messages

## Security Considerations

### IAM Permissions
- Minimal required permissions for each function
- Separate roles for different functions
- No cross-account access unless necessary

### Environment Variables
- Sensitive data (passwords, API keys) should be stored in AWS Systems Manager Parameter Store
- Use encrypted environment variables for production

### Network Security
- VPC configuration for database access
- Security groups for Lambda functions
- Private subnets for database resources

## Troubleshooting

### Common Issues

#### 1. Deployment Failures
```bash
# Check AWS credentials
aws sts get-caller-identity

# Validate serverless configuration
serverless validate

# Check for syntax errors
serverless package --verbose
```

#### 2. Function Timeouts
- Increase timeout in `serverless.yml`
- Optimize function code
- Check database connection performance

#### 3. SQS Message Processing Issues
- Check SQS queue permissions
- Verify event source mapping
- Monitor dead letter queue

#### 4. Database Connection Issues
- Verify RDS security groups
- Check VPC configuration
- Validate connection string

### Debug Commands
```bash
# View function configuration
serverless info

# Check function logs
serverless logs -f creatorCommand --startTime 1h

# Test function locally
serverless invoke local -f creatorCommand

# View stack resources
aws cloudformation describe-stacks --stack-name journal-index-integration-dev
```

## Cost Optimization

### Lambda Configuration
- Right-size memory allocation
- Optimize timeout settings
- Use provisioned concurrency for consistent workloads

### SQS Configuration
- Use long polling to reduce API calls
- Set appropriate visibility timeout
- Monitor queue depth

### CloudWatch
- Set appropriate log retention periods
- Use custom metrics instead of high-cardinality logs
- Enable log compression

## Best Practices

### 1. Environment Management
- Use separate stages for dev/staging/prod
- Never commit sensitive environment variables
- Use AWS Parameter Store for secrets

### 2. Function Design
- Keep functions focused and single-purpose
- Use appropriate timeout and memory settings
- Implement proper error handling

### 3. Monitoring
- Set up comprehensive CloudWatch alarms
- Monitor function performance metrics
- Track business metrics (processing rates, success rates)

### 4. Security
- Follow principle of least privilege
- Use VPC for database access
- Encrypt sensitive data at rest and in transit

### 5. Deployment
- Use CI/CD pipelines
- Test functions before deployment
- Implement blue-green deployments for critical functions

## Migration from Manual Deployment

If you're migrating from the manual `deploy.sh` script:

1. **Backup Current Configuration**:
   ```bash
   # Export current function configuration
   aws lambda get-function-configuration --function-name journal-index-integration
   ```

2. **Deploy with Serverless**:
   ```bash
   # Deploy new serverless stack
   serverless deploy --stage prod
   ```

3. **Update SQS Configuration**:
   - Update queue name if needed
   - Verify event source mappings

4. **Test Functions**:
   ```bash
   # Test all functions
   serverless invoke -f creatorCommand
   serverless invoke -f importCommand
   ```

5. **Clean Up Old Resources**:
   ```bash
   # Remove old manual deployment
   aws lambda delete-function --function-name journal-index-integration
   ```

## Support

For issues related to serverless configuration:
1. Check the [Serverless Framework documentation](https://www.serverless.com/framework/docs/)
2. Review AWS CloudFormation logs
3. Check function logs in CloudWatch
4. Validate configuration with `serverless validate`
