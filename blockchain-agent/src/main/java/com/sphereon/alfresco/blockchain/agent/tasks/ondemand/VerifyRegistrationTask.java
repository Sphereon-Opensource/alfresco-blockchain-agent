package com.sphereon.alfresco.blockchain.agent.tasks.ondemand;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;

public interface VerifyRegistrationTask {
    // TODO: Refactor model
    VerifyContentAlfrescoResponse verifyHash(byte[] contentHash);
}
