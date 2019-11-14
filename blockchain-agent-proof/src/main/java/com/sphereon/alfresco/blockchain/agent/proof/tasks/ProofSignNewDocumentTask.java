package com.sphereon.alfresco.blockchain.agent.proof.tasks;

import com.google.common.base.Charsets;
import com.sphereon.alfresco.blockchain.agent.tasks.scheduled.SignNewDocumentsTask;
import com.sphereon.alfresco.blockchain.agent.utils.Signer;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.RegistrationApi;
import com.sphereon.sdk.blockchain.proof.model.ContentRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sphereon-proof-api")
public class ProofSignNewDocumentTask implements SignNewDocumentsTask {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProofSignNewDocumentTask.class);

    private static final String EXCEPTION_MESSAGE_ERROR_REGISTER = "An error occurred whilst registering content: %d\n%s";

    private final RegistrationApi bcProofRegistrationApi;
    private final TokenRequest tokenRequester;
    private final Signer signer;
    private final String proofApiConfigName;

    public ProofSignNewDocumentTask(final RegistrationApi bcProofRegistrationApi,
                                    final TokenRequest tokenRequester,
                                    final Signer signer,
                                    @Value("${blockchain.config-name:#{null}}") final String proofApiConfigName) {
        this.bcProofRegistrationApi = bcProofRegistrationApi;
        this.tokenRequester = tokenRequester;
        this.signer = signer;
        this.proofApiConfigName = proofApiConfigName;
    }

    @Override
    public void registerEntry(byte[] contentHash) {
        tokenRequester.execute();
        var contentRequest = new ContentRequest();
        contentRequest.setContent(contentHash);
        contentRequest.setHashProvider(ContentRequest.HashProviderEnum.CLIENT);
        try {
            final var signature = signer.sign(contentHash);
            logger.info("Registering content " + new String(contentHash, Charsets.UTF_8));
            final var registerContentResponse = bcProofRegistrationApi.registerUsingContent(proofApiConfigName, contentRequest, null, null, signature, null);
            logger.info("registerContentResponse: " + registerContentResponse);
        } catch (com.sphereon.sdk.blockchain.proof.handler.ApiException e) {
            logger.info("The apiClient base path is " + bcProofRegistrationApi.getApiClient().getBasePath());
            throw new RuntimeException(String.format(EXCEPTION_MESSAGE_ERROR_REGISTER, e.getCode(), e.getResponseBody()), e);
        } catch (Exception throwable) {
            throw new RuntimeException("An exception occurred whilst verifying content: " + throwable.getMessage(), throwable);
        }
    }
}
