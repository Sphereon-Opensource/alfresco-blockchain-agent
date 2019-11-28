package com.sphereon.alfresco.blockchain.agent.tasks;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentBlockchainResponse;

public interface VerifyTask {
    VerifyContentBlockchainResponse verifyHash(byte[] contentHash);
}
