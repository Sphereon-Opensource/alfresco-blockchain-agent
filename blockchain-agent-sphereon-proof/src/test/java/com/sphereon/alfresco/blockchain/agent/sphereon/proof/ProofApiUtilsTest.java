package com.sphereon.alfresco.blockchain.agent.sphereon.proof;

import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import org.junit.Test;

import static com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState.BC_NOT_REGISTERED;
import static com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState.BC_PENDING;
import static com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState.BC_REGISTERED;
import static org.junit.Assert.assertEquals;

public class ProofApiUtilsTest {
    private ProofApiUtils utils;

    public ProofApiUtilsTest() {
        this.utils = new ProofApiUtils();
    }

    @Test
    public void ProofRegistrationStateEnum_shouldBeMappableToBlockchainRegistrationState() {
        assertEquals(BC_NOT_REGISTERED, utils.blockchainStateFrom(VerifyContentResponse.RegistrationStateEnum.NOT_REGISTERED));
        assertEquals(BC_PENDING, utils.blockchainStateFrom(VerifyContentResponse.RegistrationStateEnum.PENDING));
        assertEquals(BC_REGISTERED, utils.blockchainStateFrom(VerifyContentResponse.RegistrationStateEnum.REGISTERED));
    }
}
