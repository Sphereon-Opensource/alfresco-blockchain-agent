package com.sphereon.alfresco.blockchain.agent.factom;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {"sphereon.blockchain.agent.factom.entry-credits.address=Es2uwrYfDUFc1bxe96CmMKGyD6sJZCAXfvvZFx5Ru7KFHfpP1w9v"})
public class FactomRegisterWithoutWalletDTaskTest {
    @Autowired
    private FactomRegisterTask factomRegisterTask;

    @ClassRule
    public static WireMockClassRule factomDWiremock = new WireMockClassRule(wireMockConfig()
            .port(9088)
            .withRootDirectory("src/test/resources/factomd-wiremock/register-offline")
            .usingFilesUnderDirectory("src/test/resources/factomd-wiremock/register-offline"));

    @Test
    public void taskShouldRegisterHash() {
        this.factomRegisterTask.registerHash("Dummy-hash-003".getBytes());
    }
}
