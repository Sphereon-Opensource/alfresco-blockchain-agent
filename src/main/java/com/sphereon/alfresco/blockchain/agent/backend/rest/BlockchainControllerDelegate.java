package com.sphereon.alfresco.blockchain.agent.backend.rest;

import com.sphereon.alfresco.blockchain.agent.backend.tasks.ondemand.VerifyRegistrationsTask;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlockchainControllerDelegate {
    private final VerifyRegistrationsTask verifyRegistrationsTask;

    public BlockchainControllerDelegate(final VerifyRegistrationsTask verifyRegistrationsTask) {
        this.verifyRegistrationsTask = verifyRegistrationsTask;
    }

    public List<VerifyContentResponse> verifyEntries(final List<String> nodeIds, final String credentials) {
        if (StringUtils.isNotEmpty(credentials)) {
            verifyRegistrationsTask.updateCredentials(credentials);
        }
        return verifyRegistrationsTask.execute(nodeIds);
    }
}
