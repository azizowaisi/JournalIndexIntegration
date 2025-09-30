#!/bin/bash

# Deployment script for Journal Index Integration Lambda function

set -e

# Configuration
FUNCTION_NAME="journal-index-integration"
RUNTIME="java11"
HANDLER="com.teckiz.journalindex.LambdaHandler"
ROLE_ARN="arn:aws:iam::123456789012:role/lambda-execution-role"
SQS_QUEUE_ARN="arn:aws:sqs:us-east-1:123456789012:journal-index-queue"
REGION="us-east-1"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting deployment of Journal Index Integration...${NC}"

# Check if AWS CLI is configured
if ! aws sts get-caller-identity > /dev/null 2>&1; then
    echo -e "${RED}Error: AWS CLI is not configured. Please run 'aws configure' first.${NC}"
    exit 1
fi

# Build the project
echo -e "${YELLOW}Building the project...${NC}"
mvn clean package -DskipTests

if [ ! -f "target/journal-index-integration-1.0.0.jar" ]; then
    echo -e "${RED}Error: JAR file not found. Build failed.${NC}"
    exit 1
fi

# Check if function exists
if aws lambda get-function --function-name $FUNCTION_NAME --region $REGION > /dev/null 2>&1; then
    echo -e "${YELLOW}Function exists. Updating...${NC}"
    aws lambda update-function-code \
        --function-name $FUNCTION_NAME \
        --zip-file fileb://target/journal-index-integration-1.0.0.jar \
        --region $REGION
else
    echo -e "${YELLOW}Function doesn't exist. Creating...${NC}"
    aws lambda create-function \
        --function-name $FUNCTION_NAME \
        --runtime $RUNTIME \
        --role $ROLE_ARN \
        --handler $HANDLER \
        --zip-file fileb://target/journal-index-integration-1.0.0.jar \
        --timeout 300 \
        --memory-size 512 \
        --region $REGION
fi

# Update function configuration
echo -e "${YELLOW}Updating function configuration...${NC}"
aws lambda update-function-configuration \
    --function-name $FUNCTION_NAME \
    --timeout 300 \
    --memory-size 512 \
    --environment Variables='{
        "DB_URL":"jdbc:mysql://your-rds-endpoint:3306/journal_index",
        "DB_USERNAME":"your_username",
        "DB_PASSWORD":"your_password",
        "AWS_REGION":"'$REGION'"
    }' \
    --region $REGION

# Check if SQS trigger exists
if ! aws lambda list-event-source-mappings --function-name $FUNCTION_NAME --region $REGION | grep -q $SQS_QUEUE_ARN; then
    echo -e "${YELLOW}Creating SQS trigger...${NC}"
    aws lambda create-event-source-mapping \
        --event-source-arn $SQS_QUEUE_ARN \
        --function-name $FUNCTION_NAME \
        --batch-size 10 \
        --maximum-batching-window-in-seconds 5 \
        --region $REGION
else
    echo -e "${GREEN}SQS trigger already exists.${NC}"
fi

# Test the function
echo -e "${YELLOW}Testing the function...${NC}"
aws lambda invoke \
    --function-name $FUNCTION_NAME \
    --payload '{"Records":[{"body":"https://example.com"}]}' \
    --region $REGION \
    response.json

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Deployment completed successfully!${NC}"
    echo -e "${GREEN}Function ARN: $(aws lambda get-function --function-name $FUNCTION_NAME --region $REGION --query 'Configuration.FunctionArn' --output text)${NC}"
else
    echo -e "${RED}Deployment failed!${NC}"
    exit 1
fi

# Clean up
rm -f response.json

echo -e "${GREEN}Deployment script completed.${NC}"
