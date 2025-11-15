#!/bin/bash
# Production Deployment Script
# Usage: ./scripts/deploy-production.sh

set -e  # Exit on error

echo "🚀 Starting production deployment..."

# Load production environment variables
echo "📋 Loading production environment variables..."
if [ -f "environments/env.production" ]; then
    set -a  # Automatically export all variables
    source environments/env.production
    set +a  # Disable automatic export
    echo "✅ Environment variables loaded"
else
    echo "❌ Error: environments/env.production file not found!"
    exit 1
fi

# Verify required variables are set
if [ -z "$VPC_SECURITY_GROUP_ID" ] || [ -z "$DB_URL" ] || [ -z "$SERVERLESS_DEPLOYMENT_BUCKET" ]; then
    echo "❌ Error: Required environment variables are not set!"
    echo "Please check environments/env.production file"
    exit 1
fi

# Build the application
echo "🔨 Building application..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful"

# Deploy to production
echo "🚀 Deploying to AWS Lambda (production)..."
sls deploy --stage=production --region=ap-south-1 --verbose

if [ $? -eq 0 ]; then
    echo "✅ Deployment successful!"
else
    echo "❌ Deployment failed!"
    exit 1
fi

