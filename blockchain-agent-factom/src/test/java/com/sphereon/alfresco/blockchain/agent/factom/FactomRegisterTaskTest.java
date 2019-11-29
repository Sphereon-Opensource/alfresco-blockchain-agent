package com.sphereon.alfresco.blockchain.agent.factom;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.blockchain_innovation.factom.client.api.errors.FactomRuntimeException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FactomRegisterTaskTest {
    @Autowired
    private FactomRegisterTask factomRegisterTask;

    @ClassRule
    public static WireMockClassRule factomdWiremock = new WireMockClassRule(wireMockConfig()
            .port(9088)
            .withRootDirectory("src/test/resources/factomd-wiremock/register")
            .usingFilesUnderDirectory("src/test/resources/factomd-wiremock/register"));

    @ClassRule
    public static WireMockClassRule factomWalletDWiremock = new WireMockClassRule(wireMockConfig()
            .port(9089)
            .usingFilesUnderDirectory("src/test/resources/factom-walletd-wiremock/register"));

    @Test
    public void taskShouldRegisterHash() {
        this.factomRegisterTask.registerHash("Dummy-hash-001".getBytes());
    }

    @Test(expected = FactomRuntimeException.class)
    public void taskShouldFailOn400FactomResponse() {
        this.factomRegisterTask.registerHash("Dummy-hash-002".getBytes());
    }
}
