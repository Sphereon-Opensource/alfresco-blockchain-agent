package com.sphereon.alfresco.blockchain.agent.rest;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.ondemand.VerifyRegistrations;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlockchainControllerDelegate {
    private final VerifyRegistrations verifyRegistrations;

    public BlockchainControllerDelegate(final VerifyRegistrations verifyRegistrations) {
        this.verifyRegistrations = verifyRegistrations;
    }

    public List<VerifyContentAlfrescoResponse> verifyEntries(final List<String> nodeIds, final String credentials) {
        if (StringUtils.isNotEmpty(credentials)) {
            verifyRegistrations.updateCredentials(credentials);
        }
        return verifyRegistrations.execute(nodeIds);
    }
}
