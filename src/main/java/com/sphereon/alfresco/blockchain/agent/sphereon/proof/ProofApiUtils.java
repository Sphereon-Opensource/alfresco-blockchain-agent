package com.sphereon.alfresco.blockchain.agent.sphereon.proof;

import com.sphereon.alfresco.blockchain.agent.backend.AlfrescoBlockchainRegistrationState;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import org.springframework.stereotype.Component;

@Component
public class ProofApiUtils {
    public AlfrescoBlockchainRegistrationState alfrescoRegistrationStateFrom(final VerifyContentResponse.RegistrationStateEnum state) {
        switch (state) {
            case NOT_REGISTERED:
                return AlfrescoBlockchainRegistrationState.NOT_REGISTERED;
            case PENDING:
                return AlfrescoBlockchainRegistrationState.PENDING_VERIFICATION;
            case REGISTERED:
                return AlfrescoBlockchainRegistrationState.REGISTERED;
        }
        throw new IllegalStateException("Cannot map " + state + " to " + AlfrescoBlockchainRegistrationState.class.toString());
    }
}
