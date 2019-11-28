package com.sphereon.alfresco.blockchain.agent.sphereon.proof;

import com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProofApiUtilsTest {
    private ProofApiUtils utils;

    public ProofApiUtilsTest() {
        this.utils = new ProofApiUtils();
    }

    @Test
    public void RegistrationStateEnum_shouldBeMappableToAlfrescoBlockchainRegistrationState() {
        assertEquals(AlfrescoBlockchainRegistrationState.NOT_REGISTERED, utils.alfrescoRegistrationStateFrom(VerifyContentResponse.RegistrationStateEnum.NOT_REGISTERED));
        assertEquals(AlfrescoBlockchainRegistrationState.NOT_REGISTERED, utils.alfrescoRegistrationStateFrom(VerifyContentResponse.RegistrationStateEnum.NOT_REGISTERED));
        assertEquals(AlfrescoBlockchainRegistrationState.NOT_REGISTERED, utils.alfrescoRegistrationStateFrom(VerifyContentResponse.RegistrationStateEnum.NOT_REGISTERED));
    }
}
