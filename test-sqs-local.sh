#!/bin/bash

echo "=== Testing Local SQS ==="
echo ""

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo "Error: AWS CLI is not installed or not in PATH"
    echo "Please install AWS CLI and try again"
    exit 1
fi

# SQS Configuration
SQS_ENDPOINT="http://localhost:9324"
QUEUE_URL="http://localhost:9324/000000000000/journal-index-queue"

echo "Sending test message to local SQS..."
echo "Queue URL: $QUEUE_URL"
echo ""

# Test message 1: Basic journal processing
echo "Test 1: Basic journal processing"
aws --endpoint-url=$SQS_ENDPOINT sqs send-message \
    --queue-url $QUEUE_URL \
    --message-body '{
        "journalKey": "TEST_JOURNAL_001",
        "companyKey": "TEST_COMPANY_001",
        "oaiUrl": "https://example.com/oai",
        "metadataPrefix": "oai_dc"
    }'

echo ""
echo "Test 1 sent successfully!"
echo ""

# Test message 2: Different journal
echo "Test 2: Different journal"
aws --endpoint-url=$SQS_ENDPOINT sqs send-message \
    --queue-url $QUEUE_URL \
    --message-body '{
        "journalKey": "TEST_JOURNAL_002",
        "companyKey": "TEST_COMPANY_002",
        "oaiUrl": "https://test-journal.com/oai",
        "metadataPrefix": "oai_dc"
    }'

echo ""
echo "Test 2 sent successfully!"
echo ""

# Test message 3: Error case
echo "Test 3: Error case (invalid URL)"
aws --endpoint-url=$SQS_ENDPOINT sqs send-message \
    --queue-url $QUEUE_URL \
    --message-body '{
        "journalKey": "TEST_JOURNAL_ERROR",
        "companyKey": "TEST_COMPANY_ERROR",
        "oaiUrl": "https://invalid-url-that-does-not-exist.com/oai",
        "metadataPrefix": "oai_dc"
    }'

echo ""
echo "Test 3 sent successfully!"
echo ""

echo "=== All test messages sent ==="
echo "Check the serverless offline logs to see the Lambda function processing these messages."
echo ""
