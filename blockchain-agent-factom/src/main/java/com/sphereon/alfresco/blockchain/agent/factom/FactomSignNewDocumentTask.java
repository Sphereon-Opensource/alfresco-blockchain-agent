package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.factom.config.ExternalIds;
import com.sphereon.alfresco.blockchain.agent.tasks.scheduled.SignNewDocumentsTask;
import com.sphereon.libs.blockchain.commons.Digest;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.blockchain_innovation.factom.client.api.ops.Encoding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FactomSignNewDocumentTask implements SignNewDocumentsTask {
    private final FactomClient factomClient;
    private final String chainId;
    private final Digest.Algorithm hashAlgorithm;

    public FactomSignNewDocumentTask(final FactomClient factomClient,
                                     @Qualifier("factomChainId") final String chainId,
                                     final Digest.Algorithm hashAlgorithm) {
        this.factomClient = factomClient;
        this.chainId = chainId;
        this.hashAlgorithm = hashAlgorithm;
    }

    @Override
    public void registerHash(final byte[] contentHash) {
        final var entry = new Entry();
        entry.setContent(Encoding.UTF_8.encode(contentHash));
        entry.setExternalIds(ExternalIds.getExternalIds(contentHash));
        entry.setChainId(chainId);

        this.factomClient.postEntryToChain(entry);
    }
}
