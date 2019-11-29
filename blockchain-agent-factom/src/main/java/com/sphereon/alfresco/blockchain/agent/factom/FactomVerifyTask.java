package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyBlockchainEntry;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyBlockchainEntryChainType;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentBlockchainResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.VerifyTask;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.blockchain_innovation.factom.client.api.ops.Encoding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState.BC_NOT_REGISTERED;
import static com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState.BC_REGISTERED;
import static java.util.stream.Collectors.toList;

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
    public VerifyContentBlockchainResponse verifyHash(final byte[] contentHash) {
        final var hash = Encoding.BASE64.encode(contentHash);

        final Optional<Entry> entry = this.factomClient.verifyEntry(chainId, contentHash);
        final var registrationState = entry.isPresent() ? BC_REGISTERED : BC_NOT_REGISTERED;
        final List<VerifyBlockchainEntry> entries = entry.stream()
                .map(e -> new VerifyBlockchainEntry(VerifyBlockchainEntryChainType.SINGLE_CHAIN, e.getChainId()))
                .collect(toList());

        return new VerifyContentBlockchainResponse(hash, entries, registrationState);
    }
}
