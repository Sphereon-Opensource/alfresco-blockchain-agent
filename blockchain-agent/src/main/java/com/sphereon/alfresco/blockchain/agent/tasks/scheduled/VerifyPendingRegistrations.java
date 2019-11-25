package com.sphereon.alfresco.blockchain.agent.tasks.scheduled;

import com.alfresco.apis.handler.ApiException;
import com.alfresco.apis.model.ResultNode;
import com.sphereon.alfresco.blockchain.agent.tasks.AlfrescoRepository;
import com.sphereon.libs.blockchain.commons.Digest;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.PENDING_VERIFICATION;
import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.REGISTERED;

@Component
public class VerifyPendingRegistrations {
    private static final int EXECUTION_RATE = 300000;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VerifyPendingRegistrations.class);

    private final AlfrescoRepository alfrescoRepository;
    private final VerifyPendingRegistrationsTask pendingRegistrationsTask;
    private final Digest.Algorithm hashAlgorithm;

    public VerifyPendingRegistrations(final AlfrescoRepository alfrescoRepository,
                                      final VerifyPendingRegistrationsTask pendingRegistrationsTask,
                                      final Digest.Algorithm hashAlgorithm) {
        this.alfrescoRepository = alfrescoRepository;
        this.pendingRegistrationsTask = pendingRegistrationsTask;
        this.hashAlgorithm = hashAlgorithm;
    }

    @Scheduled(fixedRate = EXECUTION_RATE, initialDelay = 1000)
    public synchronized void execute() {
        try {
            logger.info("Searching for documents with registration state " + PENDING_VERIFICATION);
            this.alfrescoRepository.selectAlfrescoNodes(PENDING_VERIFICATION).forEach(rowEntry -> {
                ResultNode entry = rowEntry.getEntry();
                logger.info("Found document " + entry.getName() + " / " + entry.getId());
                var contentHash = this.alfrescoRepository.hashEntry(entry.getId(), hashAlgorithm);
                final var verifyResponse = this.pendingRegistrationsTask.verifyHash(contentHash);
                if (verifyResponse.getRegistrationState() == REGISTERED) {
                    final var registrationState = verifyResponse.getRegistrationState();
                    final var singleProofChainChainId = verifyResponse.getSingleProofChainId();
                    final var perHashProofChainChainId = verifyResponse.getPerHashProofChainId();
                    logger.info("Updating state to {} for document {} / {}", registrationState, entry.getName(), entry.getId());
                    this.alfrescoRepository.updateAlfrescoNodeWith(entry.getId(), registrationState, verifyResponse.getRegistrationTime(), singleProofChainChainId, perHashProofChainChainId);
                } else {
                    logger.info("Document " + entry.getName() + " / " + entry.getId() + " was not registered yet: " + verifyResponse.getRegistrationState());
                }
            });
        } catch (ApiException e) {
            logger.error("An error occurred whilst listing sites: " + e.getResponseBody(), e);
        } catch (Exception exception) {
            logger.error("An error occurred whilst listing sites", exception);
        }
    }
}
