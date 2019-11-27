package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.VerifyTask;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FactomVerifyTask implements VerifyTask {
    private final FactomClient factomClient;
    private final String chainId;

    public FactomVerifyTask(final FactomClient factomClient,
                            @Qualifier("factomChainId") final String chainId) {
        this.factomClient = factomClient;
        this.chainId = chainId;
    }

    @Override
    public VerifyContentAlfrescoResponse verifyHash(final byte[] contentHash) {
        final Optional<Entry> entry = this.factomClient.verifyEntry(chainId, contentHash);

        final VerifyContentAlfrescoResponse verifyContentResponse = new VerifyContentAlfrescoResponse();

        entry.ifPresent(entryMatch -> verifyContentResponse.setHash(entryMatch.getContent()));

        return verifyContentResponse;
    }
}
