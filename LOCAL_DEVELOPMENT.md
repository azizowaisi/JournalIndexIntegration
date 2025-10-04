# Local Development with Serverless Offline

This guide explains how to run the Journal Index Integration Lambda function locally using Serverless Offline with local SQS simulation.

## Prerequisites

- **Node.js** (v18 or higher)
- **npm** (comes with Node.js)
- **Maven** (for Java compilation)
- **AWS CLI** (for testing SQS messages)

## Quick Start

### 1. Install Dependencies

```bash
npm install
```

### 2. Run Serverless Offline

**Linux/Mac:**
```bash
./run-offline.sh
```

**Windows:**
```bash
run-offline.bat
```

**Or using npm:**
```bash
npm run offline
```

### 3. Test with SQS Messages

**Linux/Mac:**
```bash
./test-sqs-local.sh
```

**Windows:**
```bash
test-sqs-local.bat
```

## What's Running

When you start serverless offline, you'll have:

- **Lambda Function**: Available at `http://localhost:3002`
- **Local SQS**: Available at `http://localhost:9324`
- **Queue URL**: `http://localhost:9324/000000000000/journal-index-queue`

## Configuration

### Environment Variables

The local environment uses `env.local` file with these settings:

```properties
# Database (H2 in-memory)
DB_URL=jdbc:h2:mem:localdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
DB_USERNAME=sa
DB_PASSWORD=

# AWS (Local mock)
AWS_REGION=us-east-1
SQS_QUEUE_URL=http://localhost:9324/000000000000/journal-index-queue
SQS_QUEUE_ARN=arn:aws:sqs:us-east-1:000000000000:journal-index-queue
S3_BUCKET_NAME=local-test-bucket
```

### Spring Profile

The application uses `local` Spring profile with:
- H2 in-memory database
- Verbose logging for debugging
- Mock AWS services

## Testing

### Manual SQS Testing

Send a message to the local SQS queue:

```bash
aws --endpoint-url=http://localhost:9324 sqs send-message \
    --queue-url http://localhost:9324/000000000000/journal-index-queue \
    --message-body '{
        "journalKey": "TEST_JOURNAL_001",
        "companyKey": "TEST_COMPANY_001",
        "oaiUrl": "https://example.com/oai",
        "metadataPrefix": "oai_dc"
    }'
```

### Test Scripts

The project includes test scripts that send multiple test messages:

- `test-sqs-local.sh` (Linux/Mac)
- `test-sqs-local.bat` (Windows)

## Monitoring

### Logs

All Lambda function logs will appear in the terminal where you started serverless offline.

### Database

The H2 database is in-memory and will be recreated each time you restart the server.

### SQS Queue

You can monitor the SQS queue using AWS CLI:

```bash
# List queues
aws --endpoint-url=http://localhost:9324 sqs list-queues

# Get queue attributes
aws --endpoint-url=http://localhost:9324 sqs get-queue-attributes \
    --queue-url http://localhost:9324/000000000000/journal-index-queue \
    --attribute-names All

# Receive messages
aws --endpoint-url=http://localhost:9324 sqs receive-message \
    --queue-url http://localhost:9324/000000000000/journal-index-queue
```

## Troubleshooting

### Common Issues

1. **Port already in use**: Change ports in `serverless.yml`
2. **Maven build fails**: Check Java version and Maven installation
3. **SQS not working**: Ensure AWS CLI is installed and configured
4. **Database connection issues**: Check H2 configuration in `application-local.properties`

### Debug Mode

Enable debug logging by setting `LOG_LEVEL=DEBUG` in `env.local`.

### Reset Everything

```bash
# Stop serverless offline (Ctrl+C)
# Clean Maven
mvn clean

# Reinstall npm dependencies
rm -rf node_modules
npm install

# Restart
./run-offline.sh
```

## Development Workflow

1. **Start offline**: `./run-offline.sh`
2. **Make changes**: Edit Java code
3. **Rebuild**: Maven will auto-rebuild on changes
4. **Test**: Send SQS messages or use test scripts
5. **Debug**: Check logs in terminal
6. **Deploy**: When ready, deploy to AWS

## Benefits

- ✅ **No AWS costs** during development
- ✅ **Fast iteration** with local testing
- ✅ **Real SQS simulation** with serverless-offline-sqs
- ✅ **In-memory database** for quick testing
- ✅ **Verbose logging** for debugging
- ✅ **Easy message testing** with AWS CLI

## Next Steps

Once local development is working:
1. Test with real OAI endpoints
2. Deploy to AWS dev environment
3. Test with real SQS queue
4. Deploy to production
