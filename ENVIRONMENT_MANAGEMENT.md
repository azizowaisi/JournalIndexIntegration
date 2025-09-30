# Environment Management Guide

This guide explains how to manage different environments (development, staging, production) for the Journal Index Integration project using both Java Spring profiles and environment-specific configuration files.

## Overview

The project supports three environments:
- **Development (dev)**: Local development with verbose logging and relaxed settings
- **Staging (staging)**: Pre-production testing with balanced configuration
- **Production (prod)**: Production environment with optimized settings and security

## Configuration Files

### Environment Files
- `env.development` - Development environment variables
- `env.staging` - Staging environment variables  
- `env.production` - Production environment variables
- `env.example` - Template with all available variables

### Java Spring Profiles
- `application.properties` - Base configuration with defaults
- `application-dev.properties` - Development-specific configuration
- `application-staging.properties` - Staging-specific configuration
- `application-prod.properties` - Production-specific configuration

## Quick Start

### 1. Select Environment Profile
```bash
# Interactive selection
./select-profile.sh

# Direct selection
./select-profile.sh dev
./select-profile.sh staging
./select-profile.sh prod

# Using npm scripts
npm run profile:dev
npm run profile:staging
npm run profile:prod
```

### 2. Deploy with Selected Profile
```bash
# Development
npm run deploy:dev

# Staging
npm run deploy:staging

# Production
npm run deploy:prod
```

## Environment-Specific Configurations

### Development Environment
**File**: `env.development` + `application-dev.properties`

**Key Features**:
- Local MySQL database
- Verbose logging (DEBUG level)
- Development tools enabled
- Relaxed security
- Smaller resource allocation
- Mock services enabled

**Configuration Highlights**:
```properties
# Database
DB_URL=jdbc:mysql://localhost:3306/journal_index_dev
LOG_LEVEL=DEBUG
ENABLE_DEBUG_ENDPOINTS=true
LAMBDA_MEMORY_CREATOR=256
```

### Staging Environment
**File**: `env.staging` + `application-staging.properties`

**Key Features**:
- RDS MySQL database
- Balanced logging (INFO level)
- Limited development tools
- Basic security
- Medium resource allocation
- Real external services

**Configuration Highlights**:
```properties
# Database
DB_URL=jdbc:mysql://staging-cluster.region.rds.amazonaws.com:3306/journal_index_staging
LOG_LEVEL=INFO
ENABLE_ALARMS=true
LAMBDA_MEMORY_CREATOR=384
```

### Production Environment
**File**: `env.production` + `application-prod.properties`

**Key Features**:
- Secure RDS MySQL database
- Minimal logging (WARN level)
- No development tools
- Strict security
- Optimized resource allocation
- Full monitoring and alerting

**Configuration Highlights**:
```properties
# Database
DB_URL=jdbc:mysql://prod-cluster.region.rds.amazonaws.com:3306/journal_index_prod?useSSL=true&requireSSL=true
LOG_LEVEL=WARN
ENABLE_ALARMS=true
LAMBDA_MEMORY_CREATOR=512
```

## Environment Variables

### Required Variables
All environments require these variables:
```bash
DB_URL=jdbc:mysql://...
DB_USERNAME=username
DB_PASSWORD=password
AWS_REGION=us-east-1
SQS_QUEUE_NAME=queue-name
```

### Environment-Specific Variables

#### Development
```bash
ENABLE_DEBUG_ENDPOINTS=true
ENABLE_MOCK_SERVICES=true
LOG_RETENTION_DAYS=3
BATCH_SIZE=5
```

#### Staging
```bash
ENABLE_ALARMS=true
LOG_RETENTION_DAYS=7
BATCH_SIZE=8
ENABLE_CACHING=false
```

#### Production
```bash
ENABLE_ALARMS=true
LOG_RETENTION_DAYS=30
BATCH_SIZE=10
ENABLE_CACHING=true
ENABLE_AUTOMATED_BACKUPS=true
```

## Java Spring Profiles

### Profile Activation

#### Method 1: Environment Variable
```bash
export SPRING_PROFILES_ACTIVE=dev
```

#### Method 2: JVM Parameter
```bash
java -jar app.jar -Dspring.profiles.active=prod
```

#### Method 3: Application Properties
```properties
spring.profiles.active=staging
```

### Profile-Specific Properties

Each profile can override base properties:

**Base (`application.properties`)**:
```properties
logging.level.com.teckiz.journalindex=INFO
spring.jpa.show-sql=false
```

**Development (`application-dev.properties`)**:
```properties
logging.level.com.teckiz.journalindex=DEBUG
spring.jpa.show-sql=true
```

**Production (`application-prod.properties`)**:
```properties
logging.level.com.teckiz.journalindex=WARN
spring.jpa.show-sql=false
```

## Deployment Commands

### Serverless Framework

#### Development
```bash
npm run deploy:dev
serverless deploy --stage dev
```

#### Staging
```bash
npm run deploy:staging
serverless deploy --stage staging
```

#### Production
```bash
npm run deploy:prod
serverless deploy --stage prod
```

### Local Development

#### Run with Profile
```bash
npm run run:dev
npm run run:staging
npm run run:prod

# Or directly with Maven
mvn spring-boot:run -Dspring.profiles.active=dev
```

#### Test with Profile
```bash
npm run test:dev
npm run test:staging
npm run test:prod

# Or directly with Maven
mvn test -Dspring.profiles.active=staging
```

### Build with Profile
```bash
npm run build:dev
npm run build:staging
npm run build:prod

# Or directly with Maven
mvn clean package -Dspring.profiles.active=prod
```

## Environment Management Scripts

### Profile Selector Script
The `select-profile.sh` script helps manage environment profiles:

```bash
# Interactive selection
./select-profile.sh

# Direct selection
./select-profile.sh dev

# Show help
./select-profile.sh --help
```

**What it does**:
1. Copies the appropriate environment file to `.env`
2. Sets the `SPRING_PROFILES_ACTIVE` environment variable
3. Validates the configuration
4. Shows current configuration
5. Displays deployment commands

### NPM Scripts
```bash
# Profile management
npm run profile:dev
npm run profile:staging
npm run profile:prod
npm run profile:select

# Environment-specific deployment
npm run deploy:dev
npm run deploy:staging
npm run deploy:prod

# Environment-specific testing
npm run test:dev
npm run test:staging
npm run test:prod

# Environment-specific logs
npm run logs:dev
npm run logs:staging
npm run logs:prod
```

## Database Configuration

### Development Database
```bash
# Local MySQL
DB_URL=jdbc:mysql://localhost:3306/journal_index_dev
DB_USERNAME=root
DB_PASSWORD=dev_password_123

# Docker MySQL
docker run --name mysql-dev -e MYSQL_ROOT_PASSWORD=dev_password_123 -e MYSQL_DATABASE=journal_index_dev -p 3306:3306 -d mysql:8.0
```

### Staging Database
```bash
# RDS MySQL
DB_URL=jdbc:mysql://staging-cluster.region.rds.amazonaws.com:3306/journal_index_staging
DB_USERNAME=staging_user
DB_PASSWORD=staging_secure_password_456
```

### Production Database
```bash
# RDS MySQL with SSL
DB_URL=jdbc:mysql://prod-cluster.region.rds.amazonaws.com:3306/journal_index_prod?useSSL=true&requireSSL=true
DB_USERNAME=prod_user
DB_PASSWORD=prod_ultra_secure_password_789
```

## Security Considerations

### Development
- Relaxed security settings
- Debug endpoints enabled
- Local database access
- Development API keys

### Staging
- Basic security configuration
- Limited debug access
- RDS database with basic security
- Staging API keys

### Production
- Strict security configuration
- No debug endpoints
- RDS database with SSL and strict access
- Production API keys (stored in AWS Parameter Store)

## Monitoring and Logging

### Development
- Verbose logging (DEBUG level)
- 3-day log retention
- Local log files
- Basic monitoring

### Staging
- Balanced logging (INFO level)
- 7-day log retention
- CloudWatch integration
- Enhanced monitoring

### Production
- Minimal logging (WARN level)
- 30-day log retention
- Full CloudWatch integration
- Comprehensive monitoring and alerting

## Best Practices

### 1. Environment Separation
- Use separate databases for each environment
- Use separate SQS queues for each environment
- Use separate AWS resources (Lambda functions, CloudWatch, etc.)

### 2. Configuration Management
- Never commit sensitive data (passwords, API keys)
- Use environment variables for configuration
- Store production secrets in AWS Parameter Store
- Use different API keys for each environment

### 3. Deployment
- Test in development first
- Deploy to staging for integration testing
- Deploy to production only after staging validation
- Use infrastructure as code (serverless.yml)

### 4. Monitoring
- Set up appropriate monitoring for each environment
- Use different log retention periods
- Configure alerts based on environment needs
- Monitor resource usage and costs

## Troubleshooting

### Common Issues

#### 1. Profile Not Activated
```bash
# Check current profile
echo $SPRING_PROFILES_ACTIVE

# Set profile
export SPRING_PROFILES_ACTIVE=dev
```

#### 2. Environment File Not Found
```bash
# Check if environment file exists
ls -la env.*

# Copy template
cp env.example .env
```

#### 3. Database Connection Issues
```bash
# Check database configuration
grep DB_URL .env

# Test database connection
mysql -h hostname -u username -p
```

#### 4. AWS Configuration Issues
```bash
# Check AWS credentials
aws sts get-caller-identity

# Check AWS region
echo $AWS_REGION
```

### Debug Commands
```bash
# Check current configuration
./select-profile.sh

# Validate serverless configuration
npm run validate

# Check logs
npm run logs:dev
```

## Migration Between Environments

### From Development to Staging
1. Update `env.staging` with staging-specific values
2. Deploy to staging: `npm run deploy:staging`
3. Run integration tests: `npm run test:staging`
4. Validate functionality

### From Staging to Production
1. Update `env.production` with production-specific values
2. Deploy to production: `npm run deploy:prod`
3. Run smoke tests
4. Monitor logs and metrics

## Support

For issues related to environment management:
1. Check the profile selector script: `./select-profile.sh --help`
2. Validate configuration: `npm run validate`
3. Check logs: `npm run logs:dev`
4. Review this documentation
5. Contact the development team
