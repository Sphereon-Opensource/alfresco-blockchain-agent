package com.sphereon.alfresco.blockchain.agent.model;

public enum AlfrescoBlockchainRegistrationState {
    REGISTERED("REGISTERED"),
    PENDING_REGISTRATION("PENDING_REGISTRATION"),
    PENDING_VERIFICATION("PENDING_VERIFICATION"),
    NOT_REGISTERED("NOT_REGISTERED");

    private final String key;

    AlfrescoBlockchainRegistrationState(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
