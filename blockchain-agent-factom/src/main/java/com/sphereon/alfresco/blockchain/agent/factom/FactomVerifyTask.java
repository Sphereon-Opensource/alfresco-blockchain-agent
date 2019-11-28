package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.VerifyTask;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.blockchain_innovation.factom.client.api.ops.Encoding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

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
        final var response = new VerifyContentAlfrescoResponse();
        response.setHash(Encoding.BASE64.encode(contentHash));

        final Consumer<Entry> ifPresent = entry -> response.setRegistrationState(AlfrescoBlockchainRegistrationState.REGISTERED);
        final Runnable ifNotPresent = () -> response.setRegistrationState(AlfrescoBlockchainRegistrationState.NOT_REGISTERED);

        this.factomClient.verifyEntry(chainId, contentHash)
                .ifPresentOrElse(ifPresent, ifNotPresent);

        return response;
    }
}
