package com.sphereon.alfresco.blockchain.agent.backend.rest;

import com.google.common.base.Charsets;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.ondemand.VerifyRegistrationsTask;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringTokenizer;

@Component("oneshotDelegate")
public class BlockchainControllerDelegate {
    private final VerifyRegistrationsTask verifyRegistrationsTask;

    public BlockchainControllerDelegate(final VerifyRegistrationsTask verifyRegistrationsTask) {
        this.verifyRegistrationsTask = verifyRegistrationsTask;
    }

    public List<VerifyContentResponse> verifyEntries(final List<String> nodeIds, final String credentials) {
        if (StringUtils.isNotEmpty(credentials)) {
            updateCredentials(credentials);
        }
        verifyRegistrationsTask.setSelectedNodeIds(nodeIds);
        return verifyRegistrationsTask.execute();
    }

    private void updateCredentials(final String credentials) {
        final var base64Decoded = new String(Base64.decodeBase64(credentials), Charsets.UTF_8);
        final var tokenizer = new StringTokenizer(base64Decoded, ":", false);
        final var userName = tokenizer.nextToken();
        final var password = tokenizer.nextToken();
        verifyRegistrationsTask.updateCredentials(userName, password);
    }
}
