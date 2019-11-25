package com.sphereon.alfresco.blockchain.agent.tasks.scheduled;

import com.sphereon.libs.blockchain.commons.Digest;

public interface SignNewDocumentsTask {
    void registerEntry(byte[] contentHash, Digest.Algorithm algorithm);
}
