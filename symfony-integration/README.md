# Symfony Integration for Journal Index Lambda Service

This directory contains the Symfony integration files to replace direct OAI harvesting with SQS messaging to the Java Lambda service.

## Files

### 1. `SqsMessageSender.php`
- **Purpose**: Sends SQS messages to the Java Lambda service
- **Replaces**: Direct OAI API calls in `ImportQueueCreator`
- **Usage**: Injected as a service in Symfony

### 2. `ImportQueueCreatorUpdated.php`
- **Purpose**: Updated version of your `ImportQueueCreator` that uses SQS
- **Changes**: 
  - Removes direct OAI harvesting code
  - Sends SQS messages instead
  - Much simpler and faster

### 3. `services.yaml`
- **Purpose**: Symfony service configuration
- **Usage**: Add to your `config/services.yaml`

### 4. `env-example.txt`
- **Purpose**: Environment variables needed for AWS SQS
- **Usage**: Add these to your `.env` file

## Integration Steps

### 1. Install AWS SDK for PHP
```bash
composer require aws/aws-sdk-php
```

### 2. Add Environment Variables
Add these to your Symfony `.env` file:
```env
AWS_REGION=ap-south-1
SQS_QUEUE_URL=https://sqs.ap-south-1.amazonaws.com/518624980012/journal-index-integration-queue
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
```

### 3. Add Service Configuration
Add the contents of `services.yaml` to your `config/services.yaml`

### 4. Update Your Controller
Replace your current `ImportQueueCreator` with `ImportQueueCreatorUpdated`:

```php
// Before
$importQueueCreator = new ImportQueueCreator($importQueueHelper, ...);

// After  
$importQueueCreator = $this->get('app.import_queue_creator');
```

## How It Works

### Before (Direct OAI Harvesting):
```
Symfony Controller → ImportQueueCreator → OAI API Calls → IndexImportQueue
```

### After (SQS + Lambda):
```
Symfony Controller → SqsMessageSender → SQS Queue → Java Lambda → OAI API Calls → IndexImportQueue
```

## Benefits

1. **Faster Response**: Symfony doesn't wait for OAI harvesting
2. **Scalable**: Lambda can handle multiple requests in parallel
3. **Reliable**: SQS provides message durability and retry logic
4. **Maintainable**: Separation of concerns between Symfony and OAI harvesting

## Testing

1. **Deploy the Java Lambda** (already done)
2. **Update your Symfony app** with these files
3. **Send a test message** from your Symfony controller
4. **Check the Lambda logs** to see the processing
5. **Check the IndexImportQueue table** to see the harvested data

## Rollback

If you need to rollback, simply use the original `ImportQueueCreator` instead of `ImportQueueCreatorUpdated`.
