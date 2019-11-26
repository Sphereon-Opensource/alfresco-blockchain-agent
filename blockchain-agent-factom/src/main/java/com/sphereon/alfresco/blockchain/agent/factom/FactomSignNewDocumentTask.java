package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.tasks.scheduled.SignNewDocumentsTask;
import com.sphereon.libs.blockchain.commons.Digest;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.blockchain_innovation.factom.client.api.ops.Encoding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Arrays.asList;

@Component
public class FactomSignNewDocumentTask implements SignNewDocumentsTask {
    private static final String EXTERNAL_ID_HASH_KEY = "Hash";

    private FactomClient factomClient;
    private String chainId;

    public FactomSignNewDocumentTask(final FactomClient factomClient,
                                     @Qualifier("factomChainId") final String chainId) {
        this.factomClient = factomClient;
        this.chainId = chainId;
    }

    @Override
    public void registerEntry(byte[] contentHash, Digest.Algorithm algorithm) {
        final var entry = new Entry();
        entry.setContent(Encoding.UTF_8.encode(contentHash));
        entry.setExternalIds(getExternalIds(algorithm));
        entry.setChainId(chainId);

        this.factomClient.postEntryToChain(entry);
    }

    private List<String> getExternalIds(Digest.Algorithm algorithm) {
        return asList(EXTERNAL_ID_HASH_KEY, algorithm.getImplementation());
    }
}
