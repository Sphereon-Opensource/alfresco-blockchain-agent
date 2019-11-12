package com.sphereon.alfresco.blockchain.agent.backend.tasks.scheduled;

import com.alfresco.apis.handler.ApiException;
import com.alfresco.apis.model.ResultNode;
import com.sphereon.alfresco.blockchain.agent.sphereon.utils.Signer;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.BlockchainTask;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.Task;
import com.sphereon.alfresco.blockchain.agent.sphereon.proof.ProofApiUtils;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.VerificationApi;
import com.sphereon.sdk.blockchain.proof.model.ContentRequest;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse.RegistrationStateEnum;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.sphereon.alfresco.blockchain.agent.sphereon.alfresco.AlfrescoBlockchainRegistrationState.PENDING_VERIFICATION;

@Component
public class VerifyPendingRegistrationsTask implements Task<Void> {
    private static final int EXECUTION_RATE = 300000;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VerifyPendingRegistrationsTask.class);

    private final VerificationApi bcProofVerificationApi;
    private final TokenRequest tokenRequester;
    private final ProofApiUtils utils;
    private final BlockchainTask blockchainTask;
    private final Signer signer;
    private final String proofApiConfigName;

    public VerifyPendingRegistrationsTask(final VerificationApi verificationApi,
                                          final TokenRequest tokenRequester,
                                          final ProofApiUtils utils,
                                          final BlockchainTask blockchainTask,
                                          final Signer signer,
                                          @Value("${blockchain.config-name:#{null}}") final String proofApiConfigName) {
        this.bcProofVerificationApi = verificationApi;
        this.tokenRequester = tokenRequester;
        this.utils = utils;
        this.blockchainTask = blockchainTask;
        this.signer = signer;
        this.proofApiConfigName = proofApiConfigName;
    }

    @Scheduled(fixedRate = EXECUTION_RATE)
    public synchronized Void execute() {
        try {
            logger.info("Searching for documents with registration state " + PENDING_VERIFICATION);
            this.blockchainTask.selectEntries(PENDING_VERIFICATION).forEach(rowEntry -> {
                ResultNode entry = rowEntry.getEntry();
                logger.info("Found document " + entry.getName() + " / " + entry.getId());
                var contentHash = this.blockchainTask.hashEntry(entry.getId());
                VerifyContentResponse verifyResponse = verifyHash(contentHash);
                if (verifyResponse.getRegistrationState() == RegistrationStateEnum.REGISTERED) {
                    logger.info("Updating state to pending for document " + entry.getName() + " / " + entry.getId());
                    final var registrationState = utils.alfrescoRegistrationStateFrom(verifyResponse.getRegistrationState());
                    final var singleProofChainChainId = verifyResponse.getSingleProofChain().getChainId();
                    final var perHashProofChainChainId = verifyResponse.getPerHashProofChain().getChainId();
                    this.blockchainTask.updateAlfrescoNodeWith(entry.getId(), registrationState, verifyResponse.getRegistrationTime(), singleProofChainChainId, perHashProofChainChainId);
                } else {
                    logger.info("Document " + entry.getName() + " / " + entry.getId() + " was not registered yet: " + verifyResponse.getRegistrationState());
                }
            });
        } catch (ApiException e) {
            logger.error("An error occurred whilst listing sites: " + e.getResponseBody(), e);
        } catch (Throwable throwable) {
            logger.error("An error occurred whilst listing sites", throwable);
        }
        return null;
    }

    private VerifyContentResponse verifyHash(byte[] contentHash) {
        tokenRequester.execute();
        var contentRequest = new ContentRequest();
        contentRequest.setContent(contentHash);
        contentRequest.setHashProvider(ContentRequest.HashProviderEnum.CLIENT);
        try {
            final var signature = signer.sign(contentHash);
            return bcProofVerificationApi.verifyUsingContent(proofApiConfigName, contentRequest, null, null, signature, null);
        } catch (com.sphereon.sdk.blockchain.proof.handler.ApiException e) {
            throw new RuntimeException("An error occurred whilst verifying content: " + e.getCode(), e);
        } catch (Throwable throwable) {
            throw new RuntimeException("An error occurred whilst verifying content: " + throwable.getMessage(), throwable);
        }
    }
}