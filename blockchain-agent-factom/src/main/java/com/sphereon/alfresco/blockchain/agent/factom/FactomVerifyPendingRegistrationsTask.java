package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.scheduled.VerifyPendingRegistrationsTask;
import org.springframework.stereotype.Component;

@Component
public class FactomVerifyPendingRegistrationsTask implements VerifyPendingRegistrationsTask {
    @Override
    public VerifyContentAlfrescoResponse verifyHash(byte[] contentHash) {
        return null;
    }
}
