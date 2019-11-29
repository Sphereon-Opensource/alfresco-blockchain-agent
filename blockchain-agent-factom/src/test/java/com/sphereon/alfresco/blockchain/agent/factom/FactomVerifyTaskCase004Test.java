package com.sphereon.alfresco.blockchain.agent.factom;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(properties = {"sphereon.blockchain.agent.factom.chain.id=fe07dab8c2917366f376b454736ac07865626074691b30ffddddda4ff02a9451"})
@RunWith(SpringJUnit4ClassRunner.class)
public class FactomVerifyTaskCase004Test {
    @Autowired
    private FactomVerifyTask factomVerifyTask;

    @ClassRule
    public static WireMockClassRule factomdWiremock = new WireMockClassRule(wireMockConfig()
            .port(9088)
            .usingFilesUnderDirectory("src/test/resources/factomd-wiremock/verify"));

    @Test
    @Ignore
    public void shouldDoXOnError() {
        // TODO
    }
}
