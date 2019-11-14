package com.sphereon.alfresco.blockchain.agent.tasks.scheduled;

public interface SignNewDocumentsTask {
    void registerEntry(byte[] contentHash);
}
