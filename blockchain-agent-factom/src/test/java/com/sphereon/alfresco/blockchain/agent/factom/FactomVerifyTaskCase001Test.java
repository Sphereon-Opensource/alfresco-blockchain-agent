package com.sphereon.alfresco.blockchain.agent.factom;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.blockchain_innovation.factom.client.api.ops.Encoding;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.sphereon.alfresco.blockchain.agent.model.BlockchainRegistrationState.BC_REGISTERED;
import static org.junit.Assert.assertEquals;

@SpringBootTest(properties = {"sphereon.blockchain.agent.factom.chain.id=fe07dab8c2917366f376b454736ac07865626074691b30ffddddda4ff02a9451"})
@RunWith(SpringJUnit4ClassRunner.class)
public class FactomVerifyTaskCase001Test {
    @Autowired
    private FactomVerifyTask factomVerifyTask;

    @ClassRule
    public static WireMockClassRule factomDWiremock = new WireMockClassRule(wireMockConfig()
            .port(9088)
            .usingFilesUnderDirectory("src/test/resources/factomd-wiremock/verify/case-001"));

    @Test
    /**
     * Hex value of "Dummy-hash-001": 44756d6d792d686173682d303031
     */
    public void shouldVerifyHashAtChainHead() {
        final var dummyHash = "Dummy-hash-001";
        final var response = this.factomVerifyTask.verifyHash(dummyHash.getBytes());
        assertEquals(Encoding.BASE64.encode(dummyHash.getBytes()), response.getHash());
        assertEquals(BC_REGISTERED, response.getRegistrationState());
    }
}
