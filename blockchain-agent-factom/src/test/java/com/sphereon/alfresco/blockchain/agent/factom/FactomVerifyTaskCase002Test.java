package com.sphereon.alfresco.blockchain.agent.factom;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import org.blockchain_innovation.factom.client.api.ops.Encoding;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

@SpringBootTest(properties = {"sphereon.blockchain.agent.factom.chain.id=e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"})
@RunWith(SpringJUnit4ClassRunner.class)
public class FactomVerifyTaskCase002Test {
    @Autowired
    private FactomVerifyTask factomVerifyTask;

    @ClassRule
    public static WireMockClassRule factomDWiremock = new WireMockClassRule(wireMockConfig()
            .port(9088)
            .usingFilesUnderDirectory("src/test/resources/factomd-wiremock/verify"));

    @Test
    /*
     * Test whether the Verification task can successfully verify an entry that is a few entry blocks before the chain-head. The task will need
     * to traverse the chain, retrieve its entries and check at least the external IDs.
     *
     * Hex value of "Dummy-hash-002" 44756d6d792d686173682d303032
     */
    public void shouldVerifyHashInPreviousBlock() {
        final var dummyHash = "Dummy-hash-002";
        final VerifyContentAlfrescoResponse verifyContentAlfrescoResponse = this.factomVerifyTask.verifyHash(dummyHash.getBytes());
        assertEquals(Encoding.HEX.encode(dummyHash.getBytes()), verifyContentAlfrescoResponse.getHash());
    }
}
