package com.sphereon.alfresco.blockchain.agent.backend.tasks.ondemand;

import com.alfresco.apis.model.Node;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.AbstractBlockchainTask;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.Task;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.VerificationApi;
import com.sphereon.sdk.blockchain.proof.model.ContentRequest;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse.RegistrationStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sphereon.alfresco.blockchain.agent.backend.AlfrescoBlockchainRegistrationState.PENDING_REGISTRATION;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VerifyRegistrationsTask extends AbstractBlockchainTask implements Task<List<VerifyContentResponse>> {

    private static final Logger logger = LoggerFactory.getLogger(VerifyRegistrationsTask.class);

    private final VerificationApi bcProofVerificationApi;

    private List<String> selectedNodeIds;

    @Autowired
    protected TokenRequest tokenRequester;

    public VerifyRegistrationsTask(final VerificationApi bcProofVerificationApi) {
        this.bcProofVerificationApi = bcProofVerificationApi;
    }

    public List<VerifyContentResponse> execute() {
        final List<VerifyContentResponse> contentResponses = new ArrayList<>();
        try {
            selectEntries(selectedNodeIds).forEach(nodeEntry -> {
                final var entry = nodeEntry.getEntry();
                final String entryCurrentState = getProperty(entry, "bc:RegistrationState");
                if (PENDING_REGISTRATION.getKey().equals(entryCurrentState)) {
                    /* Don't check the state as we have not yet started registration with the Blockchain Proof service. */
                    return;
                }
                logger.info("Found document " + entry.getName() + " / " + entry.getId());
                var contentHash = hashEntry(entry.getId());
                VerifyContentResponse verifyContentResponse = verifyHash(contentHash);
                verifyContentResponse.setRequestId(entry.getId());
                contentResponses.add(verifyContentResponse);
                updateMetadata(entry.getId(), verifyContentResponse.getRegistrationState(), verifyContentResponse);
                if (verifyContentResponse.getRegistrationState() != RegistrationStateEnum.REGISTERED) {
                    logger.info("Document " + entry.getName() + " / " + entry.getId() + " was not registered yet: " + verifyContentResponse.getRegistrationState());
                }
            });
        } catch (Throwable throwable) {
            logger.error("An error occurred whilst executing VerifyRegistrationsTask", throwable);
        }
        return contentResponses;
    }

    private <T> T getProperty(final Node entry, final String key) {
        return (T) ((Map<String, Object>) entry.getProperties()).get(key);
    }

    private VerifyContentResponse verifyHash(byte[] contentHash) {
        tokenRequester.execute();
        var contentRequest = new ContentRequest();
        contentRequest.setContent(contentHash);
        contentRequest.setHashProvider(ContentRequest.HashProviderEnum.CLIENT);
        try {
            final var signature = signer.sign(contentHash);
            return bcProofVerificationApi.verifyUsingContent(configName, contentRequest, null, null, signature, null);
        } catch (com.sphereon.sdk.blockchain.proof.handler.ApiException e) {
            throw new RuntimeException("An error occurred whilst verifying content: " + e.getCode(), e);
        } catch (Throwable throwable) {
            throw new RuntimeException("An error occurred whilst verifying content: " + throwable.getMessage(), throwable);
        }
    }

    public void setSelectedNodeIds(List<String> selectedNodeIds) {
        this.selectedNodeIds = selectedNodeIds;
    }
}
