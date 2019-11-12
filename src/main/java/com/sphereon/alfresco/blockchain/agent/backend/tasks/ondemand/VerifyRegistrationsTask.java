package com.sphereon.alfresco.blockchain.agent.backend.tasks.ondemand;

import com.alfresco.apis.model.Node;
import com.google.common.base.Charsets;
import com.sphereon.alfresco.blockchain.agent.backend.commands.certficate.Signer;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.BlockchainTask;
import com.sphereon.alfresco.blockchain.agent.sphereon.proof.ProofApiUtils;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.VerificationApi;
import com.sphereon.sdk.blockchain.proof.model.ContentRequest;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse.RegistrationStateEnum;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static com.sphereon.alfresco.blockchain.agent.backend.AlfrescoBlockchainRegistrationState.PENDING_REGISTRATION;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VerifyRegistrationsTask {
    private static final Logger logger = LoggerFactory.getLogger(VerifyRegistrationsTask.class);

    private final VerificationApi bcProofVerificationApi;
    private final TokenRequest tokenRequester;
    private final ProofApiUtils utils;
    private final BlockchainTask blockchainTask;
    private final Signer signer;
    private final String proofApiConfigName;

    public VerifyRegistrationsTask(final VerificationApi bcProofVerificationApi,
                                   final TokenRequest tokenRequester,
                                   final ProofApiUtils utils,
                                   final BlockchainTask blockchainTask,
                                   final Signer signer,
                                   @Value("${blockchain.config-name:#{null}}") final String proofApiConfigName) {
        this.bcProofVerificationApi = bcProofVerificationApi;
        this.tokenRequester = tokenRequester;
        this.utils = utils;
        this.blockchainTask = blockchainTask;
        this.signer = signer;
        this.proofApiConfigName = proofApiConfigName;
    }

    public void updateCredentials(String credentials) {
        final var base64Decoded = new String(Base64.decodeBase64(credentials), Charsets.UTF_8);
        final var tokenizer = new StringTokenizer(base64Decoded, ":", false);
        final var userName = tokenizer.nextToken();
        final var password = tokenizer.nextToken();
        this.blockchainTask.updateCredentials(userName, password);
    }

    public List<VerifyContentResponse> execute(final List<String> selectedNodeIds) {
        final List<VerifyContentResponse> contentResponses = new ArrayList<>();
        try {
            this.blockchainTask.selectEntries(selectedNodeIds).forEach(nodeEntry -> {
                final var entry = nodeEntry.getEntry();
                final String entryCurrentState = getProperty(entry, "bc:RegistrationState");
                if (PENDING_REGISTRATION.getKey().equals(entryCurrentState)) {
                    /* Don't check the state as we have not yet started registration with the Blockchain Proof service. */
                    return;
                }
                logger.info("Found document " + entry.getName() + " / " + entry.getId());
                var contentHash = this.blockchainTask.hashEntry(entry.getId());
                VerifyContentResponse verifyResponse = verifyHash(contentHash);
                verifyResponse.setRequestId(entry.getId());
                contentResponses.add(verifyResponse);
                final var registrationState = utils.alfrescoRegistrationStateFrom(verifyResponse.getRegistrationState());
                final var registrationTime = verifyResponse.getRegistrationTime();
                final var singleProofChainChainId = verifyResponse.getSingleProofChain().getChainId();
                final var perHashProofChainChainId = verifyResponse.getPerHashProofChain().getChainId();
                this.blockchainTask.updateAlfrescoNodeWith(entry.getId(), registrationState, registrationTime, singleProofChainChainId, perHashProofChainChainId);
                if (verifyResponse.getRegistrationState() != RegistrationStateEnum.REGISTERED) {
                    logger.info("Document " + entry.getName() + " / " + entry.getId() + " was not registered yet: " + verifyResponse.getRegistrationState());
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
            return bcProofVerificationApi.verifyUsingContent(proofApiConfigName, contentRequest, null, null, signature, null);
        } catch (com.sphereon.sdk.blockchain.proof.handler.ApiException e) {
            throw new RuntimeException("An error occurred whilst verifying content: " + e.getCode(), e);
        } catch (Throwable throwable) {
            throw new RuntimeException("An error occurred whilst verifying content: " + throwable.getMessage(), throwable);
        }
    }
}
