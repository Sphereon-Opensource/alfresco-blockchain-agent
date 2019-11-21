package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.ondemand.VerifyRegistrationTask;
import org.springframework.stereotype.Component;

@Component
public class FactomVerificationTask implements VerifyRegistrationTask {
    @Override
    public VerifyContentAlfrescoResponse verify(byte[] contentHash) {
        return null;
    }
}
