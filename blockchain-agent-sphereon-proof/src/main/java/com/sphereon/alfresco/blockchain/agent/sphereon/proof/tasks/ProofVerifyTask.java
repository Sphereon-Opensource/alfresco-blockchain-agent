package com.sphereon.alfresco.blockchain.agent.sphereon.proof.tasks;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentBlockchainResponse;
import com.sphereon.alfresco.blockchain.agent.sphereon.proof.ProofApiUtils;
import com.sphereon.alfresco.blockchain.agent.tasks.VerifyTask;
import com.sphereon.alfresco.blockchain.agent.utils.Signer;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.VerificationApi;
import com.sphereon.sdk.blockchain.proof.handler.ApiException;
import com.sphereon.sdk.blockchain.proof.model.ContentRequest;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProofVerifyTask implements VerifyTask {
    private final VerificationApi bcProofVerificationApi;
    private final TokenRequest tokenRequester;
    private final Signer signer;
    private final String proofApiConfigName;
    private final ProofApiUtils utils;

    public ProofVerifyTask(final VerificationApi bcProofVerificationApi,
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

    @Override
    public VerifyContentBlockchainResponse verifyHash(final byte[] contentHash) {
        return this.utils.toBlockchainResponse(verify(contentHash));
    }

    private VerifyContentResponse verify(final byte[] contentHash) {
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
