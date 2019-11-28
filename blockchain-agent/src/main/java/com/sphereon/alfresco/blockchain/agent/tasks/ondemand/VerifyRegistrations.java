package com.sphereon.alfresco.blockchain.agent.tasks.ondemand;

import com.alfresco.apis.model.Node;
import com.alfresco.apis.model.NodeEntry;
import com.google.common.base.Charsets;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyBlockchainEntryChainType;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.AlfrescoRepository;
import com.sphereon.alfresco.blockchain.agent.tasks.VerifyTask;
import com.sphereon.alfresco.blockchain.agent.utils.Hasher;
import com.sphereon.libs.blockchain.commons.Digest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
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

    private final ObjectFactory<AlfrescoRepository> alfrescoRepositoryFactory;
    private final VerifyTask verificationTask;
    private final Digest.Algorithm hashAlgorithm;

    public VerifyRegistrations(final ObjectFactory<AlfrescoRepository> alfrescoRepositoryFactory,
                               final VerifyTask verificationTask,
                               final Digest.Algorithm hashAlgorithm) {
        this.alfrescoRepositoryFactory = alfrescoRepositoryFactory;
        this.verificationTask = verificationTask;
        this.hashAlgorithm = hashAlgorithm;
    }

    public List<VerifyContentAlfrescoResponse> execute(final List<String> selectedNodeIds,
                                                       final String credentials) {
        final List<VerifyContentAlfrescoResponse> contentResponses = new ArrayList<>();
        try {
            final var alfrescoRepository = this.alfrescoRepositoryFactory.getObject();
            if (StringUtils.isNotEmpty(credentials)) {
                this.updateCredentials(alfrescoRepository, credentials);
            }
            alfrescoRepository.selectAlfrescoNodes(selectedNodeIds).stream()
                    .map(NodeEntry::getEntry)
                    .filter(entry -> {
                        final String entryCurrentState = getProperty(entry, "bc:RegistrationState");
                        return !PENDING_REGISTRATION.getKey().equals(entryCurrentState);
                    })
                    .forEach(entry -> {
                        logger.info("Found document " + entry.getName() + " / " + entry.getId());
                        final var content = alfrescoRepository.getEntry(entry.getId());
                        final var contentHash = Hasher.hash(content, hashAlgorithm);
                        final var response = verificationTask.verifyHash(contentHash);
                        final var registrationState = response.getRegistrationState();
                        final var registrationTime = response.getRegistrationTime();
                        final var singleProofChainChainId = response.getChainId(VerifyBlockchainEntryChainType.SINGLE_CHAIN);
                        final var perHashProofChainChainId = response.getChainId(VerifyBlockchainEntryChainType.PER_HASH_CHAIN);
                        alfrescoRepository.updateAlfrescoNodeWith(entry.getId(), registrationState, registrationTime, singleProofChainChainId, perHashProofChainChainId);

                        response.setNodeId(entry.getId());

                        contentResponses.add(response);
                        if (response.getRegistrationState() != REGISTERED) {
                            logger.info("Document " + entry.getName() + " / " + entry.getId() + " was not registered yet: " + response.getRegistrationState());
                        }
                    });
        } catch (Exception exception) {
            logger.error("An error occurred whilst executing VerifyRegistrationsTask", exception);
        }
        return contentResponses;
    }

    private void updateCredentials(AlfrescoRepository alfrescoRepository, final String credentials) {
        final var base64Decoded = new String(Base64.decodeBase64(credentials), Charsets.UTF_8);
        final var tokenizer = new StringTokenizer(base64Decoded, ":", false);
        final var userName = tokenizer.nextToken();
        final var password = tokenizer.nextToken();
        alfrescoRepository.updateCredentials(userName, password);
    }

    private <T> T getProperty(final Node entry, final String key) {
        return (T) ((Map<String, Object>) entry.getProperties()).get(key);
    }
}
