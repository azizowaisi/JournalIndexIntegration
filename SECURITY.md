# Security Guidelines

This document outlines security best practices for the Journal Index Integration project.

## 🔒 Secret Management

### ❌ Never Commit These Files
The following files contain sensitive information and are automatically ignored by git:

- `.env` - Local environment variables
- `.env.*` - Environment-specific variables
- `env.development` - Development environment secrets
- `env.staging` - Staging environment secrets  
- `env.production` - Production environment secrets
- `*.pem`, `*.key`, `*.crt` - Certificate files
- `secrets/` - Directory for secret files
- `credentials/` - Directory for credential files

### ✅ Safe to Commit
- `env.example` - Template with placeholder values
- `application.properties` - Base configuration with `CHANGE_ME` defaults
- `application-*.properties` - Profile-specific configs with `CHANGE_ME` defaults

## 🔐 Environment Variables

### Required Environment Variables
Set these in your `.env` file or environment:

```bash
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/journal_index_dev
DB_USERNAME=your_username
DB_PASSWORD=your_secure_password

# AWS Configuration
AWS_REGION=us-east-1
SQS_QUEUE_NAME=journal-index-queue-dev

# Application Security
ADMIN_PASSWORD=your_admin_password
ENCRYPTION_KEY=your_encryption_key

# API Keys (if needed)
DOAJ_API_KEY=your_doaj_api_key
CROSSREF_API_KEY=your_crossref_api_key
```

### Environment-Specific Variables

#### Development
```bash
# Copy template and update
cp env.example .env
# Edit .env with your development values
```

#### Staging
```bash
# Copy staging template
cp env.staging .env
# Edit .env with your staging values
```

#### Production
```bash
# Copy production template
cp env.production .env
# Edit .env with your production values
```

## 🛡️ Security Best Practices

### 1. Password Requirements
- **Minimum 12 characters**
- **Mix of uppercase, lowercase, numbers, symbols**
- **No dictionary words**
- **Unique per environment**

### 2. API Key Management
- **Store in AWS Parameter Store** (recommended for production)
- **Use environment variables** for development
- **Rotate keys regularly**
- **Use different keys per environment**

### 3. Database Security
- **Use SSL connections** in staging/production
- **Limit database access** by IP/security groups
- **Use strong passwords**
- **Regular security updates**

### 4. AWS Security
- **Use IAM roles** with minimal permissions
- **Enable CloudTrail logging**
- **Use VPC** for database access
- **Encrypt data at rest and in transit**

## 🔧 Configuration Management

### Development Setup
```bash
# 1. Copy environment template
cp env.example .env

# 2. Update with your values
nano .env

# 3. Verify no secrets in git
git status
```

### Production Setup
```bash
# 1. Use AWS Parameter Store
aws ssm put-parameter \
  --name "/journal-index/prod/db-password" \
  --value "your-secure-password" \
  --type "SecureString"

# 2. Reference in environment
DB_PASSWORD=$(aws ssm get-parameter --name "/journal-index/prod/db-password" --with-decryption --query 'Parameter.Value' --output text)
```

## 🚨 Security Checklist

### Before Committing Code
- [ ] No hardcoded passwords in code
- [ ] No API keys in configuration files
- [ ] No database credentials in properties
- [ ] All secrets use environment variables
- [ ] `.env` files are in `.gitignore`
- [ ] No sensitive data in logs

### Before Deployment
- [ ] Environment variables are set
- [ ] Database passwords are secure
- [ ] API keys are rotated
- [ ] SSL certificates are valid
- [ ] Access controls are configured
- [ ] Monitoring is enabled

### Regular Maintenance
- [ ] Rotate passwords quarterly
- [ ] Update API keys annually
- [ ] Review access logs monthly
- [ ] Update dependencies regularly
- [ ] Audit security configurations

## 🔍 Security Monitoring

### Log Monitoring
Monitor these log patterns for security issues:
- Failed authentication attempts
- Database connection failures
- Unusual API access patterns
- Error rate spikes

### CloudWatch Alarms
Set up alarms for:
- High error rates
- Unusual traffic patterns
- Database connection failures
- Lambda execution errors

## 🆘 Incident Response

### If Secrets Are Compromised
1. **Immediately rotate** all affected credentials
2. **Review access logs** for unauthorized usage
3. **Update all environments** with new credentials
4. **Notify security team** if applicable
5. **Document incident** and lessons learned

### If Code Contains Secrets
1. **Remove secrets** from code immediately
2. **Force push** to remove from git history
3. **Rotate affected credentials**
4. **Review git history** for other instances

## 📚 Additional Resources

- [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/)
- [Spring Boot Security](https://spring.io/guides/gs/securing-web/)
- [OWASP Security Guidelines](https://owasp.org/www-project-top-ten/)
- [Git Security Best Practices](https://git-scm.com/docs/gitignore)

## 🆘 Support

For security-related questions or incidents:
1. Check this documentation
2. Review environment configuration
3. Contact the development team
4. Escalate to security team if needed
