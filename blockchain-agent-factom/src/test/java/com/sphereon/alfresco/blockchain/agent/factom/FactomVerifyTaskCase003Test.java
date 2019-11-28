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
import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.NOT_REGISTERED;
import static org.junit.Assert.assertEquals;

@SpringBootTest(properties = {"sphereon.blockchain.agent.factom.chain.id=2fb913d6920590df41a11767266c9081ee930fa2e825a5b0fb0f2e98e30b5c58"})
@RunWith(SpringJUnit4ClassRunner.class)
public class FactomVerifyTaskCase003Test {
    @Autowired
    private FactomVerifyTask factomVerifyTask;

    @ClassRule
    public static WireMockClassRule factomDWiremock = new WireMockClassRule(wireMockConfig()
            .port(9088)
            .usingFilesUnderDirectory("src/test/resources/factomd-wiremock/verify/case-003"));

    @Test
    public void shouldReturnNegativeWhenNotfound() {
        final var dummyHash = "Dummy-hash-003";
        final var response = this.factomVerifyTask.verifyHash(dummyHash.getBytes());
        assertEquals(Encoding.BASE64.encode(dummyHash.getBytes()), response.getHash());
        assertEquals(NOT_REGISTERED, response.getRegistrationState());
    }
}
