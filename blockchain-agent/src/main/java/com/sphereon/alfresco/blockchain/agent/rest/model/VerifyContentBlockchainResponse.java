package com.sphereon.alfresco.blockchain.agent.rest.model;

import com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VerifyContentBlockchainResponse {
    private final String hash;
    private final String hexSignature;
    private final String base64Signature;
    private final Optional<OffsetDateTime> registrationTime;
    private final List<VerifyBlockchainEntry> blockchainEntries;
    private final BlockchainRegistrationState registrationState;

    public VerifyContentBlockchainResponse(final String hash,
                                           final List<VerifyBlockchainEntry> blockchainEntries,
                                           final BlockchainRegistrationState state) {
        this(hash, null, null, null, blockchainEntries, state);
    }

    public VerifyContentBlockchainResponse(final BlockchainRegistrationState state,
                                           final OffsetDateTime time) {
        this(null, null, null, time, new ArrayList<>(), state);
    }

    public VerifyContentBlockchainResponse(final String hash,
                                           final String hexSignature,
                                           final String base64Signature,
                                           final OffsetDateTime registrationTime,
                                           final List<VerifyBlockchainEntry> blockchainEntries,
                                           final BlockchainRegistrationState registrationState) {
        this.hash = hash;
        this.hexSignature = hexSignature;
        this.base64Signature = base64Signature;
        this.registrationTime = Optional.ofNullable(registrationTime);
        this.blockchainEntries = new ArrayList<>(blockchainEntries);
        this.registrationState = registrationState;
    }

    public String getHash() {
        return hash;
    }

    public String getHexSignature() {
        return hexSignature;
    }

    public String getBase64Signature() {
        return base64Signature;
    }

    public Optional<OffsetDateTime> getRegistrationTime() {
        return registrationTime;
    }

    public List<VerifyBlockchainEntry> getBlockchainEntries() {
        return blockchainEntries;
    }

    public BlockchainRegistrationState getRegistrationState() {
        return registrationState;
    }

    public String getChainId(final VerifyBlockchainEntryChainType type) {
        return blockchainEntries.stream()
                .filter(entry -> entry.getChainType().equals(type))
                .findFirst()
                .map(VerifyBlockchainEntry::getChainId)
                .orElse(null);
    }
}
