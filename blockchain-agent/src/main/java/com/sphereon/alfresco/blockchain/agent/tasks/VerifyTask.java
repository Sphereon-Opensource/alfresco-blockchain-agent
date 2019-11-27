package com.sphereon.alfresco.blockchain.agent.tasks;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;

public interface VerifyTask {
    // TODO: Refactor model
    VerifyContentAlfrescoResponse verifyHash(byte[] contentHash);
}
