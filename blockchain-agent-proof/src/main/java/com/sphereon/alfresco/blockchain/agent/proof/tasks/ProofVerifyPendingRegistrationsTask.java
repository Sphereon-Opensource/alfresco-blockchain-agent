package com.sphereon.alfresco.blockchain.agent.proof.tasks;

import com.sphereon.alfresco.blockchain.agent.proof.ProofApiUtils;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.scheduled.VerifyPendingRegistrationsTask;
import com.sphereon.alfresco.blockchain.agent.utils.Signer;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.VerificationApi;
import com.sphereon.sdk.blockchain.proof.model.ContentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sphereon-proof-api")
public class ProofVerifyPendingRegistrationsTask implements VerifyPendingRegistrationsTask {
    private final VerificationApi bcProofVerificationApi;
    private final TokenRequest tokenRequester;
    private final ProofApiUtils utils;
    private Signer signer;
    private final String proofApiConfigName;

    public ProofVerifyPendingRegistrationsTask(final VerificationApi bcProofVerificationApi,
                                               final TokenRequest tokenRequester,
                                               final ProofApiUtils utils,
                                               final Signer signer,
                                               @Value("${blockchain.config-name:#{null}}") final String proofApiConfigName) {
        this.bcProofVerificationApi = bcProofVerificationApi;
        this.tokenRequester = tokenRequester;
        this.utils = utils;
        this.signer = signer;
        this.proofApiConfigName = proofApiConfigName;
    }

    @Override
    public VerifyContentAlfrescoResponse verifyHash(byte[] contentHash) {
        tokenRequester.execute();
        var contentRequest = new ContentRequest();
        contentRequest.setContent(contentHash);
        contentRequest.setHashProvider(ContentRequest.HashProviderEnum.CLIENT);
        try {
            final var signature = signer.sign(contentHash);
            final var verifyResponse = bcProofVerificationApi.verifyUsingContent(proofApiConfigName, contentRequest, null, null, signature, null);
            return utils.toAlfrescoResponse(verifyResponse);
        } catch (com.sphereon.sdk.blockchain.proof.handler.ApiException e) {
            throw new RuntimeException("An error occurred whilst verifying content: " + e.getCode(), e);
        } catch (Throwable throwable) {
            throw new RuntimeException("An error occurred whilst verifying content: " + throwable.getMessage(), throwable);
        }
    }
}
