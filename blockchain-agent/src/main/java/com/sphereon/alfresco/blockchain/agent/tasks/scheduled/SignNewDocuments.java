package com.sphereon.alfresco.blockchain.agent.tasks.scheduled;

import com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState;
import com.sphereon.alfresco.blockchain.agent.tasks.AlfrescoRepository;
import com.sphereon.alfresco.blockchain.agent.utils.Hasher;
import com.sphereon.libs.blockchain.commons.Digest;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.PENDING_REGISTRATION;

@Component
public class SignNewDocuments {
    private static final int EXECUTION_RATE = 30000;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SignNewDocuments.class);

    private final AlfrescoRepository alfrescoRepository;
    private final SignNewDocumentsTask signNewDocumentsTask;
    private Digest.Algorithm hashAlgorithm;

    public SignNewDocuments(final AlfrescoRepository alfrescoRepository,
                            final SignNewDocumentsTask signNewDocumentsTask,
                            final Digest.Algorithm hashAlgorithm) {
        this.alfrescoRepository = alfrescoRepository;
        this.signNewDocumentsTask = signNewDocumentsTask;
        this.hashAlgorithm = hashAlgorithm;
    }

    @Scheduled(fixedRate = EXECUTION_RATE, initialDelay = 1000)
    public synchronized void execute() {
        try {
            logger.info("Searching for documents with registration state " + PENDING_REGISTRATION);
            this.alfrescoRepository.selectAlfrescoNodes(PENDING_REGISTRATION)
                    .forEach(rowEntry -> {
                        final var entry = rowEntry.getEntry();
                        try {
                            logger.info("Found document " + entry.getName() + " / " + entry.getId());
                            final var content = this.alfrescoRepository.getEntry(entry.getId());
                            final var contentHash = Hasher.hash(content, hashAlgorithm);
                            this.signNewDocumentsTask.registerHash(contentHash);
                            final var registrationState = AlfrescoBlockchainRegistrationState.PENDING_VERIFICATION;
                            logger.info("Updating state to {} for document {} / {}", registrationState, entry.getName(), entry.getId());
                            this.alfrescoRepository.updateAlfrescoNodeWith(entry.getId(), registrationState);
                        } catch (Exception exception) {
                            logger.error(String.format("An error occurred whilst signing entry %s: %s", entry.getName(), exception.getMessage()), exception);
                            final var registrationState = AlfrescoBlockchainRegistrationState.NOT_REGISTERED;
                            this.alfrescoRepository.updateAlfrescoNodeWith(entry.getId(), registrationState);
                        }
                    });
        } catch (final Exception exception) {
            logger.error("An error occurred whilst executing SignNewDocuments task: " + exception.getMessage(), exception);
        }
    }
}
