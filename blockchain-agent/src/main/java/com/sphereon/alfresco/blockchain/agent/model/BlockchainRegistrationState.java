package com.sphereon.alfresco.blockchain.agent.model;

public enum BlockchainRegistrationState {
    BC_REGISTERED("REGISTERED"),
    BC_PENDING("PENDING"),
    BC_NOT_REGISTERED("NOT_REGISTERED");

    private final String key;

    BlockchainRegistrationState(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
