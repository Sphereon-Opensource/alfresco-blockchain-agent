package com.sphereon.alfresco.blockchain.agent.sphereon.proof;

import com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import org.springframework.stereotype.Component;

@Component
public class ProofApiUtils {
    public VerifyContentAlfrescoResponse toAlfrescoResponse(final VerifyContentResponse verifyResponse) {
        final var response = new VerifyContentAlfrescoResponse();
        response.setRequestId(verifyResponse.getRequestId());
        response.setBase64Signature(verifyResponse.getBase64Signature());
        response.setHash(verifyResponse.getHash());
        response.setHexSignature(verifyResponse.getHexSignature());
        response.setRegistrationTime(verifyResponse.getRegistrationTime());
        response.setRegistrationState(this.alfrescoRegistrationStateFrom(verifyResponse.getRegistrationState()));
        return response;
    }

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
