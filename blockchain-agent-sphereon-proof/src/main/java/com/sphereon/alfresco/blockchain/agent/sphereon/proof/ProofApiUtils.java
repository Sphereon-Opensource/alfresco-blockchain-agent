package com.sphereon.alfresco.blockchain.agent.sphereon.proof;

import com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyBlockchainEntry;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyBlockchainEntryChainType;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentBlockchainResponse;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState.BC_NOT_REGISTERED;
import static com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState.BC_PENDING;
import static com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState.BC_REGISTERED;
import static java.util.stream.Collectors.toList;

@Component
public class ProofApiUtils {
    public VerifyContentBlockchainResponse toBlockchainResponse(final VerifyContentResponse verifyResponse) {
        final var hash = verifyResponse.getHash();
        final var sigHex = verifyResponse.getHexSignature();
        final var sigBase64 = verifyResponse.getBase64Signature();
        final var time = verifyResponse.getRegistrationTime();
        final var entries = entriesFrom(verifyResponse);
        final var state = this.blockchainStateFrom(verifyResponse.getRegistrationState());
        return new VerifyContentBlockchainResponse(hash, sigHex, sigBase64, time, entries, state);
    }

    private List<VerifyBlockchainEntry> entriesFrom(final VerifyContentResponse verification) {
        final var singleChain = Optional.of(verification.getSingleProofChain())
                .map(entry -> new VerifyBlockchainEntry(VerifyBlockchainEntryChainType.SINGLE_CHAIN, entry.getChainId()));

        final var perHashChain = Optional.of(verification.getPerHashProofChain())
                .map(entry -> new VerifyBlockchainEntry(VerifyBlockchainEntryChainType.PER_HASH_CHAIN, entry.getChainId()));

        return Stream.concat(singleChain.stream(), perHashChain.stream())
                .collect(toList());
    }

    public BlockchainRegistrationState blockchainStateFrom(final VerifyContentResponse.RegistrationStateEnum state) {
        switch (state) {
            case NOT_REGISTERED:
                return BC_NOT_REGISTERED;
            case PENDING:
                return BC_PENDING;
            case REGISTERED:
                return BC_REGISTERED;
        }
        throw new IllegalStateException("Cannot map " + state + " to " + BlockchainRegistrationState.class.toString());
    }
}
