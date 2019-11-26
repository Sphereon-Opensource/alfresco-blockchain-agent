package com.sphereon.alfresco.blockchain.agent.tasks.scheduled;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;

public interface VerifyPendingRegistrationsTask {
    // TODO: Refactor model
    VerifyContentAlfrescoResponse verifyHash(byte[] contentHash);
}
