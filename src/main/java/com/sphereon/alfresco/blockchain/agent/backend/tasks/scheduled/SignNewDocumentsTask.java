package com.sphereon.alfresco.blockchain.agent.backend.tasks.scheduled;

import com.google.common.base.Charsets;
import com.sphereon.alfresco.blockchain.agent.backend.commands.certficate.Signer;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.BlockchainTask;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.Task;
import com.sphereon.alfresco.blockchain.agent.sphereon.proof.ProofApiUtils;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.RegistrationApi;
import com.sphereon.sdk.blockchain.proof.model.ContentRequest;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse.RegistrationStateEnum;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.sphereon.alfresco.blockchain.agent.backend.AlfrescoBlockchainRegistrationState.PENDING_REGISTRATION;

@Component
public class SignNewDocumentsTask implements Task<Void> {
    private static final int EXECUTION_RATE = 30000;
    private static final String EXCEPTION_MESSAGE_ERROR_REGISTER = "An error occurred whilst registering content: %d\n%s";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SignNewDocumentsTask.class);

    private final TokenRequest tokenRequester;
    private final RegistrationApi bcProofRegistrationApi;
    private final ProofApiUtils utils;
    private final BlockchainTask blockchainTask;
    private final Signer signer;
    private final String proofApiConfigName;

    public SignNewDocumentsTask(final TokenRequest tokenRequester,
                                final RegistrationApi bcProofRegistrationApi,
                                final ProofApiUtils utils,
                                final BlockchainTask blockchainTask,
                                final Signer signer,
                                @Value("${blockchain.config-name:#{null}}") final String proofApiConfigName) {
        this.tokenRequester = tokenRequester;
        this.bcProofRegistrationApi = bcProofRegistrationApi;
        this.utils = utils;
        this.blockchainTask = blockchainTask;
        this.signer = signer;
        this.proofApiConfigName = proofApiConfigName;
    }

    @Override
    @Scheduled(fixedRate = EXECUTION_RATE)
    public synchronized Void execute() {
        try {
            logger.info("Searching for documents with registration state " + PENDING_REGISTRATION);
            this.blockchainTask.selectEntries(PENDING_REGISTRATION)
                    .forEach(rowEntry -> {
                        final var entry = rowEntry.getEntry();
                        try {
                            logger.info("Found document " + entry.getName() + " / " + entry.getId());
                            var contentHash = this.blockchainTask.hashEntry(entry.getId());
                            registerEntry(contentHash);
                            logger.info("Updating state to pending for document " + entry.getName() + " / " + entry.getId());
                            final var registrationState = utils.alfrescoRegistrationStateFrom(RegistrationStateEnum.PENDING);
                            this.blockchainTask.updateAlfrescoNodeWith(entry.getId(), registrationState);
                        } catch (Throwable throwable) {
                            logger.error(String.format("An error occurred whilst signing entry %s: %s", entry.getName(), throwable.getMessage()), throwable);
                            final var registrationState = utils.alfrescoRegistrationStateFrom(RegistrationStateEnum.NOT_REGISTERED);
                            this.blockchainTask.updateAlfrescoNodeWith(entry.getId(), registrationState);
                        }
                    });
        } catch (final Exception exception) {
            logger.error("An error occurred whilst executing SignNewDocuments task: " + exception.getMessage(), exception);
        }
        return null;
    }

    private void registerEntry(byte[] contentHash) {
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
