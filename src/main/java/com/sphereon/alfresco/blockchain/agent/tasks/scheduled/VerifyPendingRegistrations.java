package com.sphereon.alfresco.blockchain.agent.tasks.scheduled;

import com.alfresco.apis.handler.ApiException;
import com.alfresco.apis.model.ResultNode;
import com.sphereon.alfresco.blockchain.agent.tasks.BlockchainTask;
import com.sphereon.alfresco.blockchain.agent.tasks.Task;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.PENDING_VERIFICATION;
import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.REGISTERED;

@Component
public class VerifyPendingRegistrations implements Task<Void> {
    private static final int EXECUTION_RATE = 300000;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VerifyPendingRegistrations.class);

    private final BlockchainTask blockchainTask;
    private final VerifyPendingRegistrationsTask pendingRegistrationsTask;

    public VerifyPendingRegistrations(final BlockchainTask blockchainTask,
                                      final VerifyPendingRegistrationsTask pendingRegistrationsTask) {
        this.blockchainTask = blockchainTask;
        this.pendingRegistrationsTask = pendingRegistrationsTask;
    }

    @Scheduled(fixedRate = EXECUTION_RATE)
    public synchronized Void execute() {
        try {
            logger.info("Searching for documents with registration state " + PENDING_VERIFICATION);
            this.blockchainTask.selectEntries(PENDING_VERIFICATION).forEach(rowEntry -> {
                ResultNode entry = rowEntry.getEntry();
                logger.info("Found document " + entry.getName() + " / " + entry.getId());
                var contentHash = this.blockchainTask.hashEntry(entry.getId());
                final var verifyResponse = this.pendingRegistrationsTask.verifyHash(contentHash);
                if (verifyResponse.getRegistrationState() == REGISTERED) {
                    logger.info("Updating state to pending for document " + entry.getName() + " / " + entry.getId());
                    final var registrationState = verifyResponse.getRegistrationState();
                    final var singleProofChainChainId = verifyResponse.getSingleProofChainId();
                    final var perHashProofChainChainId = verifyResponse.getPerHashProofChainId();
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
}
