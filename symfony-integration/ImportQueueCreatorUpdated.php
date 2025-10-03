<?php

namespace AppBundle\Helper\RJIndex;

use AppBundle\Entity\IndexJournal;
use AppBundle\Entity\IndexJournalSetting;
use AppBundle\Helper\UtilHelper;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;

/**
 * Updated ImportQueueCreator that uses SQS messaging instead of direct OAI calls
 * This replaces the direct OAI harvesting with Lambda service integration
 */
class ImportQueueCreatorUpdated
{
    public function __construct(
        private readonly SqsMessageSender $sqsMessageSender
    )
    {
    }

    /**
     * Main entry point - now sends SQS messages instead of direct OAI calls
     */
    public function createQueue(IndexJournal $indexJournal): void
    {
        $indexJournalSetting = $indexJournal->getSetting();
        if (!$indexJournalSetting instanceof IndexJournalSetting) {
            throw new \Exception('journal setting not found');
        }

        $system = $indexJournalSetting->getSystem();
        if (UtilHelper::isEmpty($system)) {
            throw new \Exception('system not found');
        }

        switch ($indexJournalSetting->getSystem()) {
            case IndexJournalSetting::SYSTEM_OJS_OAI:
                var_dump('SYSTEM_OJS_OAI - Sending SQS message to Lambda');
                $this->sqsMessageSender->sendOjsOaiMessage($indexJournal);
                return;

            case IndexJournalSetting::SYSTEM_TECKIZ:
                var_dump('SYSTEM_TECKIZ - Sending SQS message to Lambda');
                $this->sqsMessageSender->sendTeckizMessage($indexJournal);
                return;
                
            case IndexJournalSetting::SYSTEM_DOAJ:
                var_dump('SYSTEM_DOAJ - Sending SQS message to Lambda');
                $this->sqsMessageSender->sendDoajMessage($indexJournal);
                return;
        }
    }
}
