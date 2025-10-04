#!/bin/bash

echo "=== Journal Index Integration - Serverless Offline ==="
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "Error: Node.js is not installed or not in PATH"
    echo "Please install Node.js and try again"
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "Error: npm is not installed or not in PATH"
    echo "Please install npm and try again"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven and try again"
    exit 1
fi

echo "Installing dependencies..."
npm install

if [ $? -ne 0 ]; then
    echo "Error: npm install failed"
    exit 1
fi

echo ""
echo "Building Java project..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Error: Maven build failed"
    exit 1
fi

echo ""
echo "Starting Serverless Offline..."
echo "=============================="
echo "Lambda functions will be available at: http://localhost:3002"
echo "SQS will be available at: http://localhost:9324"
echo "Local SQS Queue URL: http://localhost:9324/000000000000/journal-index-queue"
echo ""
echo "To send a test message to SQS:"
echo "aws --endpoint-url=http://localhost:9324 sqs send-message --queue-url http://localhost:9324/000000000000/journal-index-queue --message-body '{\"journalKey\":\"TEST_001\",\"oaiUrl\":\"https://example.com/oai\"}'"
echo ""
echo "Press Ctrl+C to stop the server"
echo "=============================="

# Set local profile for Spring Boot
export SPRING_PROFILES_ACTIVE=local

# Start serverless offline
npx serverless offline --stage local --env local
