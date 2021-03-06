package com.sphereon.alfresco.blockchain.agent.sphereon.proof.tasks;

import com.google.common.base.Charsets;
import com.sphereon.alfresco.blockchain.agent.tasks.RegisterTask;
import com.sphereon.alfresco.blockchain.agent.utils.Signer;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.RegistrationApi;
import com.sphereon.sdk.blockchain.proof.model.ContentRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProofRegisterTask implements RegisterTask {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProofRegisterTask.class);

    private static final String EXCEPTION_MESSAGE_ERROR_REGISTER = "An error occurred whilst registering content: %d\n%s";

    private final RegistrationApi bcProofRegistrationApi;
    private final TokenRequest tokenRequester;
    private final Signer signer;
    private final String proofApiConfigName;

    public ProofRegisterTask(final RegistrationApi bcProofRegistrationApi,
                             final TokenRequest tokenRequester,
                             final Signer signer,
                             @Value("${blockchain.config-name:#{null}}") final String proofApiConfigName) {
        this.bcProofRegistrationApi = bcProofRegistrationApi;
        this.tokenRequester = tokenRequester;
        this.signer = signer;
        this.proofApiConfigName = proofApiConfigName;
    }

    @Override
    public void registerHash(byte[] contentHash) {
        final var contentRequest = new ContentRequest();
        contentRequest.setContent(contentHash);
        contentRequest.setHashProvider(ContentRequest.HashProviderEnum.CLIENT);
        try {
            final var signature = signer.sign(contentHash);
            logger.info("Registering content " + new String(contentHash, Charsets.UTF_8));
            tokenRequester.execute();
            final var registerContentResponse = bcProofRegistrationApi.registerUsingContent(proofApiConfigName, contentRequest, null, null, signature, null);
            logger.info("registerContentResponse: " + registerContentResponse);
        } catch (com.sphereon.sdk.blockchain.proof.handler.ApiException e) {
            logger.info("The apiClient base path is " + bcProofRegistrationApi.getApiClient().getBasePath());
            throw new RuntimeException(String.format(EXCEPTION_MESSAGE_ERROR_REGISTER, e.getCode(), e.getResponseBody()), e);
        } catch (Exception exception) {
            throw new RuntimeException("An exception occurred whilst verifying content: " + exception.getMessage(), exception);
        }
    }
}
