package com.sphereon.alfresco.blockchain.agent.tasks.scheduled;

import com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState;
import com.sphereon.alfresco.blockchain.agent.tasks.AlfrescoRepository;
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

    public SignNewDocuments(final AlfrescoRepository alfrescoRepository,
                            final SignNewDocumentsTask signNewDocumentsTask) {
        this.alfrescoRepository = alfrescoRepository;
        this.signNewDocumentsTask = signNewDocumentsTask;
    }

    @Scheduled(fixedRate = EXECUTION_RATE)
    public synchronized void execute() {
        try {
            logger.info("Searching for documents with registration state " + PENDING_REGISTRATION);
            this.alfrescoRepository.selectEntries(PENDING_REGISTRATION)
                    .forEach(rowEntry -> {
                        final var entry = rowEntry.getEntry();
                        try {
                            logger.info("Found document " + entry.getName() + " / " + entry.getId());
                            var contentHash = this.alfrescoRepository.hashEntry(entry.getId());
                            this.signNewDocumentsTask.registerEntry(contentHash);
                            logger.info("Updating state to pending for document " + entry.getName() + " / " + entry.getId());
                            final var registrationState = AlfrescoBlockchainRegistrationState.REGISTERED;
                            this.alfrescoRepository.updateAlfrescoNodeWith(entry.getId(), registrationState);
                        } catch (Throwable throwable) {
                            logger.error(String.format("An error occurred whilst signing entry %s: %s", entry.getName(), throwable.getMessage()), throwable);
                            final var registrationState = AlfrescoBlockchainRegistrationState.NOT_REGISTERED;
                            this.alfrescoRepository.updateAlfrescoNodeWith(entry.getId(), registrationState);
                        }
                    });
        } catch (final Exception exception) {
            logger.error("An error occurred whilst executing SignNewDocuments task: " + exception.getMessage(), exception);
        }
    }
}
