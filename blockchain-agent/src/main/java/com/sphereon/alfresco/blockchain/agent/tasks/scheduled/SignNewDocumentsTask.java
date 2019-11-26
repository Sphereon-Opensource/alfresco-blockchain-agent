package com.sphereon.alfresco.blockchain.agent.tasks.scheduled;

public interface SignNewDocumentsTask {
    void registerHash(byte[] contentHash);
}
