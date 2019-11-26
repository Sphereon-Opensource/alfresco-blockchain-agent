package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.ondemand.VerifyRegistrationTask;
import com.sphereon.libs.blockchain.commons.Digest;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FactomVerificationTask implements VerifyRegistrationTask {
    private final FactomClient factomClient;
    private final String chainId;
    private final Digest.Algorithm hashAlgorithm;

    public FactomVerificationTask(final FactomClient factomClient,
                                  @Qualifier("factomChainId") final String chainId,
                                  final Digest.Algorithm hashAlgorithm) {
        this.factomClient = factomClient;
        this.chainId = chainId;
        this.hashAlgorithm = hashAlgorithm;
    }

    @Override
    public VerifyContentAlfrescoResponse verifyHash(final byte[] contentHash) {
        final Optional<Entry> entry = this.factomClient.verifyEntry(chainId, contentHash);

        final VerifyContentAlfrescoResponse verifyContentResponse = new VerifyContentAlfrescoResponse();

        entry.ifPresent(entryMatch -> verifyContentResponse.setHash(entryMatch.getChainId()));

        return verifyContentResponse;
    }
}
