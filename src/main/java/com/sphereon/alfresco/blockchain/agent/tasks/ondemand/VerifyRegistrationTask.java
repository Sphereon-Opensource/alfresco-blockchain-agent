package com.sphereon.alfresco.blockchain.agent.tasks.ondemand;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;

public interface VerifyRegistrationTask {
    VerifyContentAlfrescoResponse verify(byte[] contentHash);
}
