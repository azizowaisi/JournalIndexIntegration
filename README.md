# Journal Index Integration

A Java-based AWS Lambda service for processing journal data from various sources including OAI harvesting and S3 file processing.

## Project Structure

```
├── src/                          # Java source code
├── scripts/                      # All deployment and utility scripts
│   ├── deploy.sh                # Unified deployment script
│   └── run-offline.sh           # Local development server
├── environments/                 # Environment configuration files
│   ├── env.local               # Local development
│   └── env.production          # Production environment
└── serverless.yml              # Serverless Framework configuration
```

## Quick Start

### 1. Local Development

```bash
# Deploy locally with offline mode
./scripts/deploy.sh

# Run tests only
./scripts/deploy.sh -a test

# Run local development server
./scripts/run-offline.sh
```

### 2. Production Environment

```bash
# Deploy to production (requires VPC configuration)
./scripts/deploy.sh -e production

# Validate VPC configuration
./scripts/deploy.sh -e production --validate-only
```

## Unified Deployment Script

The `scripts/deploy.sh` script handles all deployment scenarios:

### Usage

```bash
./scripts/deploy.sh [OPTIONS]
```

### Options

- `-e, --environment ENV` - Environment (local|production) [default: local]
- `-a, --action ACTION` - Action (deploy|test|validate|build|local-test) [default: deploy]
- `-s, --stage STAGE` - Serverless stage [default: local]
- `--skip-tests` - Skip running tests
- `--validate-only` - Only validate configuration, don't deploy
- `-h, --help` - Show help message

### Examples

```bash
# Deploy to local environment
./scripts/deploy.sh

# Deploy to production
./scripts/deploy.sh -e production

# Run tests only
./scripts/deploy.sh -a test

# Validate production configuration
./scripts/deploy.sh -e production --validate-only

# Build only
./scripts/deploy.sh -a build

# Run local development server
./scripts/run-offline.sh
```

## Environments

### Local Environment
- Uses H2 in-memory database
- Mock AWS services with serverless-offline
- No VPC configuration required
- Perfect for development and testing

### Production Environment
- Uses MySQL RDS in VPC
- Real AWS services
- VPC configuration required
- Full production deployment

## VPC Configuration

For production environment, you need to configure:

1. **VPC Settings**:
   - `VPC_ID` - Your VPC ID
   - `VPC_CIDR` - VPC CIDR block
   - `VPC_SECURITY_GROUP_ID` - Security group for Lambda
   - `VPC_SUBNET_ID_1` - First subnet
   - `VPC_SUBNET_ID_2` - Second subnet

2. **MySQL RDS Settings**:
   - `MYSQL_HOST` - RDS endpoint
   - `MYSQL_DATABASE` - Database name
   - `DB_USERNAME` - Database username
   - `DB_PASSWORD` - Database password

3. **AWS Services**:
   - `SQS_QUEUE_ARN` - SQS queue ARN
   - `S3_FILE_QUEUE_ARN` - S3 file processing queue ARN
   - `S3_SCRAPING_BUCKET` - S3 bucket for scraped data


## Features

### 1. OAI Harvesting
- Processes OJS OAI endpoints
- Handles DOAJ data
- Supports Teckiz journal systems
- Automatic system type detection

### 2. S3 File Processing
- Reads files from S3 buckets
- Processes data from S3 files
- Supports JSON and XML formats
- Automatic content type detection

### 3. Database Storage
- Stores processed data in MySQL
- Queue-based processing
- Error handling and retry logic
- Comprehensive logging

### 4. VPC Support
- Runs in private subnets
- Secure database connectivity
- Configurable security groups
- Optimized connection pooling

## Monitoring

- CloudWatch logs for all Lambda functions
- CloudWatch alarms for error monitoring
- Detailed logging for debugging
- Performance metrics

## Troubleshooting

### Common Issues

1. **VPC Configuration**: Ensure all VPC settings are correct
2. **Database Connectivity**: Check security groups and RDS endpoint
3. **SQS Permissions**: Verify IAM permissions for SQS access
4. **S3 Access**: Ensure Lambda has S3 read permissions

### Debugging

1. Check CloudWatch logs
2. Validate configuration with `--validate-only`
3. Test locally with `-e local`
4. Review security group rules

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test with `./scripts/deploy.sh -a test`
5. Submit a pull request

## License

This project is licensed under the MIT License.