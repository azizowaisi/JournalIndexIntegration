<?php

namespace AppBundle\Helper\RJIndex;

use Aws\Sqs\SqsClient;
use Exception;

/**
 * Helper class to send SQS messages to Java Lambda service
 * Replaces direct OAI harvesting in Symfony with SQS messaging
 */
class SqsMessageSender
{
    private SqsClient $sqsClient;
    private string $queueUrl;
    
    public function __construct(string $awsRegion, string $queueUrl, string $accessKey, string $secretKey)
    {
        $this->queueUrl = $queueUrl;
        
        $this->sqsClient = new SqsClient([
            'region' => $awsRegion,
            'version' => 'latest',
            'credentials' => [
                'key' => $accessKey,
                'secret' => $secretKey,
            ],
        ]);
    }
    
    /**
     * Send OJS OAI harvesting message to Lambda
     * Replaces createOjsOaiQueue() method
     */
    public function sendOjsOaiMessage(IndexJournal $indexJournal): void
    {
        $message = [
            'url' => $indexJournal->getWebsite(),
            'journal_key' => (string) $indexJournal->getId(),
            'system_type' => 'OJS_OAI',
            'action' => 'harvest_oai'
        ];
        
        $this->sendMessage($message);
    }
    
    /**
     * Send Teckiz harvesting message to Lambda
     * Replaces createTeckizQueue() method
     */
    public function sendTeckizMessage(IndexJournal $indexJournal): void
    {
        $message = [
            'url' => $indexJournal->getWebsite(),
            'journal_key' => (string) $indexJournal->getId(),
            'system_type' => 'TECKIZ',
            'action' => 'harvest_teckiz'
        ];
        
        $this->sendMessage($message);
    }
    
    /**
     * Send DOAJ harvesting message to Lambda
     */
    public function sendDoajMessage(IndexJournal $indexJournal): void
    {
        $message = [
            'url' => $indexJournal->getWebsite(),
            'journal_key' => (string) $indexJournal->getId(),
            'system_type' => 'DOAJ',
            'action' => 'harvest_doaj'
        ];
        
        $this->sendMessage($message);
    }
    
    /**
     * Send generic message to Lambda
     */
    private function sendMessage(array $message): void
    {
        try {
            $result = $this->sqsClient->sendMessage([
                'QueueUrl' => $this->queueUrl,
                'MessageBody' => json_encode($message),
                'MessageAttributes' => [
                    'system_type' => [
                        'DataType' => 'String',
                        'StringValue' => $message['system_type']
                    ],
                    'action' => [
                        'DataType' => 'String',
                        'StringValue' => $message['action']
                    ]
                ]
            ]);
            
            var_dump('SQS message sent successfully. MessageId: ' . $result['MessageId']);
            
        } catch (Exception $e) {
            var_dump('Failed to send SQS message: ' . $e->getMessage());
            throw $e;
        }
    }
}
