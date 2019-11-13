package com.sphereon.alfresco.blockchain.agent.tasks.scheduled;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;

public interface VerifyPendingRegistrationsTask {
    VerifyContentAlfrescoResponse verifyHash(byte[] contentHash);
}
