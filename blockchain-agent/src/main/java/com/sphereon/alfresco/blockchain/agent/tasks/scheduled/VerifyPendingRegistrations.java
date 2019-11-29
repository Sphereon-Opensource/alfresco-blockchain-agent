package com.sphereon.alfresco.blockchain.agent.tasks.scheduled;

import com.alfresco.apis.handler.ApiException;
import com.alfresco.apis.model.ResultNode;
import com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyBlockchainEntryChainType;
import com.sphereon.alfresco.blockchain.agent.tasks.AlfrescoRepository;
import com.sphereon.alfresco.blockchain.agent.tasks.VerifyTask;
import com.sphereon.alfresco.blockchain.agent.utils.Hasher;
import com.sphereon.libs.blockchain.commons.Digest;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.ALF_PENDING_VERIFICATION;
import static com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState.BC_REGISTERED;

@Component
public class VerifyPendingRegistrations {
    private static final int EXECUTION_RATE = 300_000;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VerifyPendingRegistrations.class);

    private final AlfrescoRepository alfrescoRepository;
    private final VerifyTask verifyTask;
    private final Digest.Algorithm hashAlgorithm;

    public VerifyPendingRegistrations(final AlfrescoRepository alfrescoRepository,
                                      final VerifyTask verifyTask,
                                      final Digest.Algorithm hashAlgorithm) {
        this.alfrescoRepository = alfrescoRepository;
        this.verifyTask = verifyTask;
        this.hashAlgorithm = hashAlgorithm;
    }

    @Scheduled(fixedRate = EXECUTION_RATE, initialDelay = 1000)
    public synchronized void execute() {
        try {
            logger.info("Searching for documents with registration state " + ALF_PENDING_VERIFICATION);
            this.alfrescoRepository.selectAlfrescoNodes(ALF_PENDING_VERIFICATION)
                    .forEach(rowEntry -> {
                        final ResultNode entry = rowEntry.getEntry();
                        logger.info("Found document " + entry.getName() + " / " + entry.getId());
                        final var content = this.alfrescoRepository.getEntry(entry.getId());
                        final var contentHash = Hasher.hash(content, hashAlgorithm);
                        final var result = this.verifyTask.verifyHash(contentHash);
                        if (result.getRegistrationState() != BC_REGISTERED) {
                            logger.info("Document " + entry.getName() + " / " + entry.getId() + " was not registered yet: " + result.getRegistrationState());
                            return;
                        }

                        final var registrationState = AlfrescoBlockchainRegistrationState.from(result.getRegistrationState(), ALF_PENDING_VERIFICATION);
                        final var registrationTime = result.getRegistrationTime().orElse(null);
                        final var singleProofChainChainId = result.getChainId(VerifyBlockchainEntryChainType.SINGLE_CHAIN);
                        final var perHashProofChainChainId = result.getChainId(VerifyBlockchainEntryChainType.PER_HASH_CHAIN);
                        logger.info("Updating state to {} for document {} / {}", registrationState, entry.getName(), entry.getId());
                        this.alfrescoRepository.updateAlfrescoNodeWith(entry.getId(), registrationState, registrationTime, singleProofChainChainId, perHashProofChainChainId);
                    });
        } catch (ApiException e) {
            logger.error("An error occurred whilst listing sites: " + e.getResponseBody(), e);
        } catch (Exception exception) {
            logger.error("An error occurred whilst listing sites", exception);
        }
    }
}
