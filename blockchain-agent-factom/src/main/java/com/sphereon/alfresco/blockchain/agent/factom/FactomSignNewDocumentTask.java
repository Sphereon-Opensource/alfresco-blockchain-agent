package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.tasks.scheduled.SignNewDocumentsTask;
import org.springframework.stereotype.Component;

@Component
public class FactomSignNewDocumentTask implements SignNewDocumentsTask {
    @Override
    public void registerEntry(byte[] contentHash) {

    }
}
