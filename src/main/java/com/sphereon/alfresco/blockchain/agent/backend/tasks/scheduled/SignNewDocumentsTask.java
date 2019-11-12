package com.sphereon.alfresco.blockchain.agent.backend.tasks.scheduled;

import com.alfresco.apis.model.ResultNode;
import com.google.common.base.Charsets;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.AbstractBlockchainTask;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.Task;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.RegistrationApi;
import com.sphereon.sdk.blockchain.proof.model.ContentRequest;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse.RegistrationStateEnum;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.sphereon.alfresco.blockchain.agent.backend.AlfrescoBlockchainRegistrationState.PENDING_REGISTRATION;

@Component
public class SignNewDocumentsTask extends AbstractBlockchainTask implements Task<Void> {

    private static final int EXECUTION_RATE = 30000;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SignNewDocumentsTask.class);
    public static final String EXCEPTION_MESSAGE_ERROR_REGISTER = "An error occurred whilst registering content: %d\n%s";

    @Autowired
    protected TokenRequest tokenRequester;

    @Autowired
    private RegistrationApi bcProofRegistrationApi;

    @Override
    @Scheduled(fixedRate = EXECUTION_RATE)
    public synchronized Void execute() {
        try {
            logger.info("Searching for documents with registration state " + PENDING_REGISTRATION);
            selectEntries(PENDING_REGISTRATION).forEach(rowEntry -> {
                ResultNode entry = rowEntry.getEntry();
                try {
                    logger.info("Found document " + entry.getName() + " / " + entry.getId());
                    var contentHash = hashEntry(entry.getId());
                    registerEntry(contentHash, entry.getVersionLabel());
                    logger.info("Updating state to pending for document " + entry.getName() + " / " + entry.getId());
                    updateMetadata(entry.getId(), RegistrationStateEnum.PENDING);
                } catch (Throwable throwable) {
                    logger.error(String.format("An error occurred whilst signing entry %s: %s", entry.getName(), throwable.getMessage()), throwable);
                    updateMetadata(entry.getId(), RegistrationStateEnum.NOT_REGISTERED);
                }
            });
        } catch (Throwable throwable) {
            logger.error("An error occurred whilst executing SignNewDocuments task: " + throwable.getMessage(), throwable);
        }
        return null;
    }


    private void registerEntry(byte[] contentHash, String versionLabel) {
        tokenRequester.execute();
        var contentRequest = new ContentRequest();
        contentRequest.setContent(contentHash);
        contentRequest.setHashProvider(ContentRequest.HashProviderEnum.CLIENT);
        try {
            final var signature = signer.sign(contentHash);
            logger.info("Registering content " + new String(contentHash, Charsets.UTF_8));
            final var registerContentResponse = bcProofRegistrationApi.registerUsingContent(configName, contentRequest, null, null, signature, null);
            logger.info("registerContentResponse: " + registerContentResponse);
        } catch (com.sphereon.sdk.blockchain.proof.handler.ApiException e) {
            logger.info("The apiClient base path is " + bcProofRegistrationApi.getApiClient().getBasePath());
            throw new RuntimeException(String.format(EXCEPTION_MESSAGE_ERROR_REGISTER, e.getCode(), e.getResponseBody()), e);
        } catch (Exception throwable) {
            throw new RuntimeException("An exception occurred whilst verifying content: " + throwable.getMessage(), throwable);
        }
    }
}
