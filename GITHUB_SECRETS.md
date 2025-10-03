# GitHub Secrets Configuration

This document lists all the GitHub secrets that need to be configured in your repository for the CI/CD pipeline to work properly.

## Required GitHub Secrets

### 1. Serverless Framework Authentication
```
SERVERLESS_ACCESS_KEY
```
- **Description**: Serverless Framework access key for authentication
- **How to get**: Sign up at https://www.serverless.com/ and get your access key
- **Example**: `sl_1234567890abcdef`

### 2. AWS Authentication
```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION
```
- **Description**: AWS credentials and region for deployment
- **How to get**: Create IAM user with appropriate permissions in AWS Console
- **Example**: 
  - `AWS_ACCESS_KEY_ID`: `AKIAIOSFODNN7EXAMPLE`
  - `AWS_SECRET_ACCESS_KEY`: `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`
  - `AWS_REGION`: `us-east-1`

### 3. Database Configuration (EXISTING)
```
DB_URL ✅ (Already configured)
DB_USERNAME ✅ (Already configured)
DB_PASSWORD ✅ (Already configured)
```
- **Description**: MySQL database connection details
- **Status**: These secrets are already configured in your GitHub repository
- **Examples**:
  - `DB_URL`: `jdbc:mysql://journal-index-prod.cluster-abc.us-east-1.rds.amazonaws.com:3306/journal_index_prod?useSSL=true&serverTimezone=UTC&requireSSL=true&verifyServerCertificate=true`
  - `DB_USERNAME`: `prod_user`
  - `DB_PASSWORD`: `your_secure_password`

### 4. AWS Services Configuration
```
SQS_QUEUE_URL
SQS_QUEUE_ARN
S3_BUCKET_NAME
```
- **Description**: AWS SQS queue and S3 bucket for the application
- **How to get**: From your AWS Console or create new resources
- **Examples**:
  - `SQS_QUEUE_URL`: `https://sqs.us-east-1.amazonaws.com/123456789012/journal-index-queue-prod`
  - `SQS_QUEUE_ARN`: `arn:aws:sqs:us-east-1:123456789012:journal-index-queue-prod`
  - `S3_BUCKET_NAME`: `journal-index-xml-files-prod`

## How to Add Secrets in GitHub

1. Go to your repository on GitHub
2. Click on **Settings** tab
3. In the left sidebar, click on **Secrets and variables** → **Actions**
4. Click **New repository secret**
5. Add each secret with the exact name and value
6. Click **Add secret**

## Environment-Specific Configuration

### Development Environment
```bash
DB_URL=jdbc:mysql://localhost:3306/journal_index_dev?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=dev_password
AWS_REGION=us-east-1
SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/journal-index-queue-dev
SQS_QUEUE_ARN=arn:aws:sqs:us-east-1:123456789012:journal-index-queue-dev
S3_BUCKET_NAME=journal-index-xml-files-dev
```

### Staging Environment
```bash
DB_URL=jdbc:mysql://journal-index-staging.cluster-abc.us-east-1.rds.amazonaws.com:3306/journal_index_staging?useSSL=true&serverTimezone=UTC&requireSSL=true&verifyServerCertificate=true
DB_USERNAME=staging_user
DB_PASSWORD=staging_password
AWS_REGION=us-east-1
SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/journal-index-queue-staging
SQS_QUEUE_ARN=arn:aws:sqs:us-east-1:123456789012:journal-index-queue-staging
S3_BUCKET_NAME=journal-index-xml-files-staging
```

### Production Environment
```bash
DB_URL=jdbc:mysql://journal-index-prod.cluster-abc.us-east-1.rds.amazonaws.com:3306/journal_index_prod?useSSL=true&serverTimezone=UTC&requireSSL=true&verifyServerCertificate=true
DB_USERNAME=prod_user
DB_PASSWORD=prod_secure_password
AWS_REGION=us-east-1
SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/journal-index-queue-prod
SQS_QUEUE_ARN=arn:aws:sqs:us-east-1:123456789012:journal-index-queue-prod
S3_BUCKET_NAME=journal-index-xml-files-prod
```

## Security Best Practices

1. **Use strong passwords** for database credentials
2. **Rotate secrets regularly** (every 90 days recommended)
3. **Use least privilege principle** for AWS IAM permissions
4. **Never commit secrets** to the repository
5. **Use different secrets** for different environments
6. **Monitor secret usage** in GitHub Actions logs

## Troubleshooting

### Common Issues

1. **"Secret not found" error**: Make sure the secret name matches exactly (case-sensitive)
2. **"Access denied" error**: Check AWS credentials and permissions
3. **"Database connection failed"**: Verify database credentials and network access
4. **"SQS queue not found"**: Ensure the SQS queue exists and ARN is correct

### Verification Commands

```bash
# Test database connection
mysql -h $DB_HOST -P $DB_PORT -u $DB_USERNAME -p$DB_PASSWORD $DB_NAME -e "SELECT 1;"

# Test AWS credentials
aws sts get-caller-identity

# Test SQS queue access
aws sqs get-queue-attributes --queue-url $SQS_QUEUE_URL --attribute-names All

# Test S3 bucket access
aws s3 ls s3://$S3_BUCKET_NAME
```

## Required AWS IAM Permissions

The AWS user/role needs the following permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "lambda:*",
                "iam:*",
                "cloudformation:*",
                "s3:*",
                "sqs:*",
                "logs:*",
                "events:*",
                "rds:DescribeDBInstances",
                "rds:DescribeDBClusters"
            ],
            "Resource": "*"
        }
    ]
}
```
