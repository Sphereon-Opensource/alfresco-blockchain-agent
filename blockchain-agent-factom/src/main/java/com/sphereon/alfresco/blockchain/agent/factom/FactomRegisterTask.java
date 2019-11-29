package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.factom.config.ExternalIds;
import com.sphereon.alfresco.blockchain.agent.tasks.RegisterTask;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FactomRegisterTask implements RegisterTask {
    private final FactomClient factomClient;
    private final String chainId;

    public FactomRegisterTask(final FactomClient factomClient,
                              @Qualifier("factomChainId") final String chainId) {
        this.factomClient = factomClient;
        this.chainId = chainId;
    }

    @Override
    public void registerHash(final byte[] contentHash) {
        final var entry = new Entry();
        entry.setContent(sign(contentHash));
        entry.setExternalIds(ExternalIds.getExternalIds(contentHash));
        entry.setChainId(chainId);

        this.factomClient.postEntryToChain(entry);
    }

    private String sign(byte[] contentHash) {
        // TODO: Sign and put signed entry into content
        return "-";
    }
}
