#!/bin/bash

# Profile Selector Script for Journal Index Integration
# This script helps you select and configure the appropriate environment profile

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Journal Index Integration Profile Selector${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to display available profiles
show_profiles() {
    echo -e "${YELLOW}Available Profiles:${NC}"
    echo -e "${GREEN}1. Development (dev)${NC}     - Local development with verbose logging"
    echo -e "${GREEN}2. Staging (staging)${NC}    - Pre-production testing environment"
    echo -e "${GREEN}3. Production (prod)${NC}    - Production environment with optimized settings"
    echo ""
}

# Function to select profile
select_profile() {
    local profile=$1
    
    case $profile in
        "dev"|"development"|"1")
            PROFILE="dev"
            ENV_FILE="env.development"
            ;;
        "staging"|"stage"|"2")
            PROFILE="staging"
            ENV_FILE="env.staging"
            ;;
        "prod"|"production"|"3")
            PROFILE="prod"
            ENV_FILE="env.production"
            ;;
        *)
            echo -e "${RED}Invalid profile selected: $profile${NC}"
            echo -e "${YELLOW}Please select from: dev, staging, prod${NC}"
            exit 1
            ;;
    esac
}

# Function to copy environment file
copy_env_file() {
    if [ -f "$ENV_FILE" ]; then
        echo -e "${YELLOW}Copying $ENV_FILE to .env...${NC}"
        cp "$ENV_FILE" .env
        echo -e "${GREEN}✓ Environment file copied successfully${NC}"
    else
        echo -e "${RED}✗ Environment file $ENV_FILE not found${NC}"
        exit 1
    fi
}

# Function to set Spring profile
set_spring_profile() {
    echo -e "${YELLOW}Setting Spring profile to: $PROFILE${NC}"
    export SPRING_PROFILES_ACTIVE=$PROFILE
    echo "SPRING_PROFILES_ACTIVE=$PROFILE" >> .env
    echo -e "${GREEN}✓ Spring profile set to: $PROFILE${NC}"
}

# Function to validate environment
validate_environment() {
    echo -e "${YELLOW}Validating environment configuration...${NC}"
    
    # Check if .env file exists
    if [ ! -f ".env" ]; then
        echo -e "${RED}✗ .env file not found${NC}"
        exit 1
    fi
    
    # Check if required environment variables are set
    source .env
    
    if [ -z "$DB_URL" ]; then
        echo -e "${RED}✗ DB_URL not set${NC}"
        exit 1
    fi
    
    if [ -z "$DB_USERNAME" ]; then
        echo -e "${RED}✗ DB_USERNAME not set${NC}"
        exit 1
    fi
    
    if [ -z "$DB_PASSWORD" ]; then
        echo -e "${RED}✗ DB_PASSWORD not set${NC}"
        exit 1
    fi
    
    if [ -z "$AWS_REGION" ]; then
        echo -e "${RED}✗ AWS_REGION not set${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Environment validation passed${NC}"
}

# Function to display current configuration
show_configuration() {
    echo ""
    echo -e "${BLUE}Current Configuration:${NC}"
    echo -e "${YELLOW}Profile:${NC} $PROFILE"
    echo -e "${YELLOW}Environment File:${NC} $ENV_FILE"
    echo -e "${YELLOW}Spring Profile:${NC} $SPRING_PROFILES_ACTIVE"
    echo ""
    
    if [ -f ".env" ]; then
        echo -e "${BLUE}Environment Variables:${NC}"
        echo -e "${YELLOW}DB_URL:${NC} $(grep '^DB_URL=' .env | cut -d'=' -f2- | head -c 50)..."
        echo -e "${YELLOW}DB_USERNAME:${NC} $(grep '^DB_USERNAME=' .env | cut -d'=' -f2-)"
        echo -e "${YELLOW}AWS_REGION:${NC} $(grep '^AWS_REGION=' .env | cut -d'=' -f2-)"
        echo -e "${YELLOW}SQS_QUEUE_NAME:${NC} $(grep '^SQS_QUEUE_NAME=' .env | cut -d'=' -f2-)"
        echo ""
    fi
}

# Function to show deployment commands
show_deployment_commands() {
    echo -e "${BLUE}Deployment Commands for $PROFILE:${NC}"
    echo ""
    
    case $PROFILE in
        "dev")
            echo -e "${GREEN}# Development Commands:${NC}"
            echo "npm run deploy:dev"
            echo "serverless deploy --stage dev"
            echo "mvn spring-boot:run -Dspring.profiles.active=dev"
            ;;
        "staging")
            echo -e "${GREEN}# Staging Commands:${NC}"
            echo "npm run deploy:staging"
            echo "serverless deploy --stage staging"
            echo "mvn spring-boot:run -Dspring.profiles.active=staging"
            ;;
        "prod")
            echo -e "${GREEN}# Production Commands:${NC}"
            echo "npm run deploy:prod"
            echo "serverless deploy --stage prod"
            echo "mvn spring-boot:run -Dspring.profiles.active=prod"
            ;;
    esac
    echo ""
}

# Function to show usage
show_usage() {
    echo -e "${YELLOW}Usage:${NC}"
    echo "  $0 [profile]"
    echo ""
    echo -e "${YELLOW}Examples:${NC}"
    echo "  $0 dev          # Select development profile"
    echo "  $0 staging      # Select staging profile"
    echo "  $0 prod         # Select production profile"
    echo "  $0              # Interactive selection"
    echo ""
}

# Main execution
main() {
    # Check if profile is provided as argument
    if [ $# -eq 1 ]; then
        select_profile "$1"
    else
        # Interactive selection
        show_profiles
        echo -e "${YELLOW}Please select a profile (1-3):${NC}"
        read -r choice
        select_profile "$choice"
    fi
    
    # Execute profile setup
    copy_env_file
    set_spring_profile
    validate_environment
    show_configuration
    show_deployment_commands
    
    echo -e "${GREEN}✓ Profile setup completed successfully!${NC}"
    echo -e "${BLUE}You can now run your application with the selected profile.${NC}"
}

# Check if help is requested
if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    show_usage
    exit 0
fi

# Run main function
main "$@"
