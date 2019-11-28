package com.sphereon.alfresco.blockchain.agent.tasks.scheduled;

import com.sphereon.alfresco.blockchain.agent.tasks.AlfrescoRepository;
import com.sphereon.alfresco.blockchain.agent.tasks.RegisterTask;
import com.sphereon.alfresco.blockchain.agent.utils.Hasher;
import com.sphereon.libs.blockchain.commons.Digest;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.ALF_NOT_REGISTERED;
import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.ALF_PENDING_REGISTRATION;
import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.ALF_PENDING_VERIFICATION;

@Component
public class SignNewDocuments {
    private static final int EXECUTION_RATE = 30000;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SignNewDocuments.class);

    private final AlfrescoRepository alfrescoRepository;
    private final RegisterTask registerTask;
    private Digest.Algorithm hashAlgorithm;

    public SignNewDocuments(final AlfrescoRepository alfrescoRepository,
                            final RegisterTask registerTask,
                            final Digest.Algorithm hashAlgorithm) {
        this.alfrescoRepository = alfrescoRepository;
        this.registerTask = registerTask;
        this.hashAlgorithm = hashAlgorithm;
    }

    @Scheduled(fixedRate = EXECUTION_RATE, initialDelay = 60 * 1000)
    public synchronized void execute() {
        try {
            logger.info("Searching for documents with registration state " + ALF_PENDING_REGISTRATION);
            this.alfrescoRepository.selectAlfrescoNodes(ALF_PENDING_REGISTRATION)
                    .forEach(rowEntry -> {
                        final var entry = rowEntry.getEntry();
                        try {
                            logger.info("Found document " + entry.getName() + " / " + entry.getId());
                            final var content = this.alfrescoRepository.getEntry(entry.getId());
                            final var contentHash = Hasher.hash(content, hashAlgorithm);
                            this.registerTask.registerHash(contentHash);
                            final var registrationState = ALF_PENDING_VERIFICATION;
                            logger.info("Updating state to {} for document {} / {}", registrationState, entry.getName(), entry.getId());
                            this.alfrescoRepository.updateAlfrescoNodeWith(entry.getId(), registrationState);
                        } catch (final Exception exception) {
                            logger.error(String.format("An error occurred whilst signing entry %s: %s", entry.getName(), exception.getMessage()), exception);
                            this.alfrescoRepository.updateAlfrescoNodeWith(entry.getId(), ALF_NOT_REGISTERED);
                        }
                    });
        } catch (final Exception exception) {
            logger.error("An error occurred whilst executing SignNewDocuments task: " + exception.getMessage(), exception);
        }
    }
}
