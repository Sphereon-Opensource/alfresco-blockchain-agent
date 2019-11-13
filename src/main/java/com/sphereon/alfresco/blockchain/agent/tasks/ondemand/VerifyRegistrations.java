package com.sphereon.alfresco.blockchain.agent.tasks.ondemand;

import com.alfresco.apis.model.Node;
import com.google.common.base.Charsets;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.BlockchainTask;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.PENDING_REGISTRATION;
import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.REGISTERED;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VerifyRegistrations {
    private static final Logger logger = LoggerFactory.getLogger(VerifyRegistrations.class);

    private final BlockchainTask blockchainTask;
    private VerifyRegistrationTask verificationTask;

    public VerifyRegistrations(final BlockchainTask blockchainTask,
                               final VerifyRegistrationTask verificationTask) {
        this.blockchainTask = blockchainTask;
        this.verificationTask = verificationTask;
    }

    public void updateCredentials(String credentials) {
        final var base64Decoded = new String(Base64.decodeBase64(credentials), Charsets.UTF_8);
        final var tokenizer = new StringTokenizer(base64Decoded, ":", false);
        final var userName = tokenizer.nextToken();
        final var password = tokenizer.nextToken();
        this.blockchainTask.updateCredentials(userName, password);
    }

    public List<VerifyContentAlfrescoResponse> execute(final List<String> selectedNodeIds) {
        final List<VerifyContentAlfrescoResponse> contentResponses = new ArrayList<>();
        try {
            this.blockchainTask.selectEntries(selectedNodeIds)
                    .forEach(nodeEntry -> {
                        final var entry = nodeEntry.getEntry();
                        final String entryCurrentState = getProperty(entry, "bc:RegistrationState");
                        if (PENDING_REGISTRATION.getKey().equals(entryCurrentState)) {
                            /* Don't check the state as we have not yet started registration. */
                            return;
                        }

                        logger.info("Found document " + entry.getName() + " / " + entry.getId());
                        final var contentHash = this.blockchainTask.hashEntry(entry.getId());
                        final var response = verificationTask.verify(contentHash);
                        contentResponses.add(response);
                        final var registrationState = response.getRegistrationState();
                        final var registrationTime = response.getRegistrationTime();
                        final var singleProofChainChainId = response.getSingleProofChainId();
                        final var perHashProofChainChainId = response.getPerHashProofChainId();
                        this.blockchainTask.updateAlfrescoNodeWith(entry.getId(), registrationState, registrationTime, singleProofChainChainId, perHashProofChainChainId);
                        if (response.getRegistrationState() != REGISTERED) {
                            logger.info("Document " + entry.getName() + " / " + entry.getId() + " was not registered yet: " + response.getRegistrationState());
                        }
                    });
        } catch (Exception exception) {
            logger.error("An error occurred whilst executing VerifyRegistrationsTask", exception);
        }
        return contentResponses;
    }

    private <T> T getProperty(final Node entry, final String key) {
        return (T) ((Map<String, Object>) entry.getProperties()).get(key);
    }
}
