#!/bin/bash

# =============================================================================
# Journal Index Integration - Deployment Script
# =============================================================================
# Updated: October 2024
# Simplified deployment workflow for Lambda JSON processing
# =============================================================================

set -e

# Default values
ENVIRONMENT="production"
STAGE="production"
REGION="ap-south-1"
SKIP_TESTS=false
SKIP_BUILD=false
VERBOSE=false

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

print_header() {
    echo ""
    echo -e "${CYAN}=============================================================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}=============================================================================${NC}"
    echo ""
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓ SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[⚠ WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗ ERROR]${NC} $1"
}

# =============================================================================
# HELP / USAGE
# =============================================================================

show_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Deployment script for Journal Index Integration

Options:
  -e, --environment ENV    Environment to deploy (production) [default: production]
  -s, --stage STAGE        Serverless stage [default: production]
  -r, --region REGION      AWS region [default: ap-south-1]
  --skip-tests            Skip running tests
  --skip-build            Skip Maven build (use existing JAR)
  -v, --verbose           Verbose output
  -h, --help              Show this help message

Examples:
  $0                                      # Deploy to production
  $0 --skip-tests                         # Deploy without tests
  $0 -s prod -r us-east-1                # Custom region deployment
  $0 --skip-build                         # Deploy existing build

Deployment Steps:
  1. Load environment variables
  2. Validate configuration
  3. Maven clean & package
  4. Serverless deploy to AWS

EOF
}

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -e|--environment)
                ENVIRONMENT="$2"
                shift 2
                ;;
            -s|--stage)
                STAGE="$2"
                shift 2
                ;;
            -r|--region)
                REGION="$2"
                shift 2
                ;;
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            --skip-build)
                SKIP_BUILD=true
                shift
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
}

# =============================================================================
# VALIDATION FUNCTIONS
# =============================================================================

validate_tools() {
    print_step "Validating required tools..."
    
    local missing_tools=()
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        missing_tools+=("Maven")
    else
        print_status "Maven: $(mvn -version | head -n 1 | cut -d' ' -f3)"
    fi
    
    # Check Serverless Framework
    if ! command -v serverless &> /dev/null && ! command -v sls &> /dev/null; then
        missing_tools+=("Serverless Framework")
    else
        print_status "Serverless: $(serverless -v 2>/dev/null || sls -v)"
    fi
    
    # Check AWS CLI (optional)
    if ! command -v aws &> /dev/null; then
        print_warning "AWS CLI not found (optional)"
    else
        print_status "AWS CLI: $(aws --version 2>&1 | cut -d' ' -f1 | cut -d'/' -f2)"
    fi
    
    if [ ${#missing_tools[@]} -gt 0 ]; then
        print_error "Missing required tools: ${missing_tools[*]}"
        exit 1
    fi
    
    print_success "All required tools are installed"
}

load_environment_variables() {
    print_step "Loading environment variables..."
    
    local env_file="environments/env.${ENVIRONMENT}"
    
    if [ ! -f "$env_file" ]; then
        print_error "Environment file not found: $env_file"
        exit 1
    fi
    
    print_status "Loading from: $env_file"
    set -a  # Enable automatic export of variables
    source "$env_file"
    set +a  # Disable automatic export
    
    # Validate critical variables
    local missing_vars=()
    
    if [ -z "$DB_URL" ]; then missing_vars+=("DB_URL"); fi
    if [ -z "$DB_USERNAME" ]; then missing_vars+=("DB_USERNAME"); fi
    if [ -z "$DB_PASSWORD" ]; then missing_vars+=("DB_PASSWORD"); fi
    if [ -z "$SQS_QUEUE_URL" ]; then missing_vars+=("SQS_QUEUE_URL"); fi
    if [ -z "$SQS_QUEUE_ARN" ]; then missing_vars+=("SQS_QUEUE_ARN"); fi
    
    if [ $ENVIRONMENT = "production" ]; then
        if [ -z "$VPC_ID" ]; then missing_vars+=("VPC_ID"); fi
        if [ -z "$VPC_SECURITY_GROUP_ID" ]; then missing_vars+=("VPC_SECURITY_GROUP_ID"); fi
        if [ -z "$VPC_SUBNET_ID_1" ]; then missing_vars+=("VPC_SUBNET_ID_1"); fi
        if [ -z "$VPC_SUBNET_ID_2" ]; then missing_vars+=("VPC_SUBNET_ID_2"); fi
    fi
    
    if [ ${#missing_vars[@]} -gt 0 ]; then
        print_error "Missing required environment variables: ${missing_vars[*]}"
        exit 1
    fi
    
    print_success "Environment variables loaded successfully"
    print_status "DB Host: ${MYSQL_HOST}"
    print_status "SQS Queue: ${SQS_QUEUE_URL}"
    print_status "Region: ${AWS_REGION:-$REGION}"
}

# =============================================================================
# BUILD FUNCTIONS
# =============================================================================

maven_build() {
    print_step "Building application with Maven..."
    
    local mvn_cmd="mvn clean package"
    
    if [ "$SKIP_TESTS" = true ]; then
        mvn_cmd="$mvn_cmd -DskipTests"
    fi
    
    if [ "$VERBOSE" = false ]; then
        mvn_cmd="$mvn_cmd -q"
    fi
    
    if $mvn_cmd; then
        print_success "Maven build completed"
        
        # Show JAR file info
        if [ -f "target/journal-index-integration-1.0.0.jar" ]; then
            local jar_size=$(du -h target/journal-index-integration-1.0.0.jar | cut -f1)
            print_status "JAR file size: $jar_size"
        fi
    else
        print_error "Maven build failed"
        exit 1
    fi
}

# =============================================================================
# DEPLOYMENT FUNCTIONS
# =============================================================================

deploy_to_aws() {
    print_step "Deploying to AWS..."
    print_status "Stage: $STAGE"
    print_status "Region: $REGION"
    
    local deploy_cmd="sls deploy --stage $STAGE --region $REGION"
    
    if [ "$VERBOSE" = true ]; then
        deploy_cmd="$deploy_cmd --verbose"
    fi
    
    print_status "Running: $deploy_cmd"
    
    if $deploy_cmd; then
        print_success "Deployment completed successfully!"
    else
        print_error "Deployment failed"
        exit 1
    fi
}

show_deployment_info() {
    print_header "DEPLOYMENT INFORMATION"
    
    echo -e "${GREEN}Deployment completed successfully!${NC}"
    echo ""
    echo "Environment Details:"
    echo "  Environment:  $ENVIRONMENT"
    echo "  Stage:        $STAGE"
    echo "  Region:       $REGION"
    echo ""
    echo "Lambda Function:"
    echo "  - journal-index-integration-$STAGE-processor"
    echo ""
    echo "Resources:"
    echo "  - SQS Queue:  $SQS_QUEUE_URL"
    echo "  - Database:   ${MYSQL_HOST}"
    echo ""
    echo "Processing:"
    echo "  - Batch Size: 10 messages per invocation"
    echo "  - Timeout:    900 seconds"
    echo "  - Memory:     2048 MB"
    echo "  - Runtime:    Java 17 with SnapStart"
    echo ""
    echo "Useful Commands:"
    echo "  sls info --stage $STAGE --region $REGION"
    echo "  sls logs -f journalProcessor --stage $STAGE --region $REGION --tail"
    echo "  aws logs tail /aws/lambda/journal-index-integration-$STAGE-processor --follow"
    echo ""
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

main() {
    # Clear screen for clean output
    clear
    
    print_header "JOURNAL INDEX INTEGRATION - DEPLOYMENT"
    
    # Parse command line arguments
    parse_arguments "$@"
    
    print_status "Deployment Configuration:"
    print_status "  Environment:  $ENVIRONMENT"
    print_status "  Stage:        $STAGE"
    print_status "  Region:       $REGION"
    print_status "  Skip Tests:   $SKIP_TESTS"
    print_status "  Skip Build:   $SKIP_BUILD"
    echo ""
    
    # Step 1: Validate tools
    validate_tools
    
    # Step 2: Load environment variables
    load_environment_variables
    
    if [ "$SKIP_BUILD" = false ]; then
        # Step 3: Maven build
        maven_build
    else
        print_warning "Skipping build - using existing JAR"
        if [ ! -f "target/journal-index-integration-1.0.0.jar" ]; then
            print_error "No existing JAR found in target directory"
            exit 1
        fi
    fi
    
    # Step 4: Deploy to AWS
    deploy_to_aws
    
    # Show deployment information
    show_deployment_info
    
    print_success "Deployment script completed successfully!"
}

# Run main function with all arguments
main "$@"

