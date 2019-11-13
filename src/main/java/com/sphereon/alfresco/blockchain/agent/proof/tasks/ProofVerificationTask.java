package com.sphereon.alfresco.blockchain.agent.proof.tasks;

import com.sphereon.alfresco.blockchain.agent.proof.ProofApiUtils;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.ondemand.VerifyRegistrationTask;
import com.sphereon.alfresco.blockchain.agent.utils.Signer;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.VerificationApi;
import com.sphereon.sdk.blockchain.proof.handler.ApiException;
import com.sphereon.sdk.blockchain.proof.model.ContentRequest;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProofVerificationTask implements VerifyRegistrationTask {
    private final VerificationApi bcProofVerificationApi;
    private final TokenRequest tokenRequester;
    private final Signer signer;
    private String proofApiConfigName;
    private ProofApiUtils utils;

    public ProofVerificationTask(final VerificationApi bcProofVerificationApi,
                                 final TokenRequest tokenRequester,
                                 final Signer signer,
                                 final ProofApiUtils utils,
                                 @Value("${blockchain.config-name:#{null}}") final String proofApiConfigName) {
        this.bcProofVerificationApi = bcProofVerificationApi;
        this.tokenRequester = tokenRequester;
        this.signer = signer;
        this.utils = utils;
        this.proofApiConfigName = proofApiConfigName;
    }

    public VerifyContentAlfrescoResponse verify(final byte[] contentHash) {
        return toAlfrescoResponse(verifyHash(contentHash));
    }

    private VerifyContentAlfrescoResponse toAlfrescoResponse(final VerifyContentResponse verifyResponse) {
        final var response = new VerifyContentAlfrescoResponse();
        response.setRequestId(verifyResponse.getRequestId());
        response.setBase64Signature(verifyResponse.getBase64Signature());
        response.setHash(verifyResponse.getHash());
        response.setHexSignature(verifyResponse.getHexSignature());
        response.setRegistrationTime(verifyResponse.getRegistrationTime());
        response.setRegistrationState(utils.alfrescoRegistrationStateFrom(verifyResponse.getRegistrationState()));
        return response;
    }

    private VerifyContentResponse verifyHash(final byte[] contentHash) {
        final var contentRequest = new ContentRequest();
        contentRequest.setContent(contentHash);
        contentRequest.setHashProvider(ContentRequest.HashProviderEnum.CLIENT);
        try {
            final var signature = signer.sign(contentHash);
            tokenRequester.execute();
            return bcProofVerificationApi.verifyUsingContent(proofApiConfigName, contentRequest, null, null, signature, null);
        } catch (final ApiException e) {
            throw new RuntimeException("An error occurred whilst verifying content: " + e.getCode(), e);
        } catch (final Exception exception) {
            throw new RuntimeException("An error occurred whilst verifying content: " + exception.getMessage(), exception);
        }
    }
}
