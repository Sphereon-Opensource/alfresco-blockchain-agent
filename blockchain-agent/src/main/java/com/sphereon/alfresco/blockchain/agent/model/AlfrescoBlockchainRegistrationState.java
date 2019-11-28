package com.sphereon.alfresco.blockchain.agent.model;

public enum AlfrescoBlockchainRegistrationState {
    ALF_REGISTERED("REGISTERED"),
    ALF_PENDING_REGISTRATION("PENDING_REGISTRATION"),
    ALF_PENDING_VERIFICATION("PENDING_VERIFICATION"),
    ALF_NOT_REGISTERED("NOT_REGISTERED");

    private final String key;

    AlfrescoBlockchainRegistrationState(final String key) {
        this.key = key;
    }

    public static AlfrescoBlockchainRegistrationState from(final BlockchainRegistrationState registrationState,
                                                           final AlfrescoBlockchainRegistrationState defaultPendingState) {
        switch (registrationState) {
            case BC_REGISTERED:
                return ALF_REGISTERED;
            case BC_NOT_REGISTERED:
                return ALF_NOT_REGISTERED;
            case BC_PENDING:
                return defaultPendingState;
            default:
                throw new IllegalArgumentException("Unknown blockchain state provided");
        }
    }

    public String getKey() {
        return key;
    }
}
