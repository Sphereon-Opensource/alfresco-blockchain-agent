package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import org.blockchain_innovation.factom.client.api.ops.Encoding;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class FactomVerifyTaskTest {
    @Autowired
    private FactomVerifyTask factomVerifyTask;

    @Test
    public void verifyHashAtChainHead() {
        final var dummyHash = "Dummy-hash-4";
        final VerifyContentAlfrescoResponse verifyContentAlfrescoResponse = this.factomVerifyTask.verifyHash(dummyHash.getBytes());
        assertEquals(Encoding.HEX.encode("Dummy-hash-4".getBytes()), verifyContentAlfrescoResponse.getHash());
    }

    @Test
    public void verifyHashInPreviousBlock() {
        final var dummyHash = "Foo";
        final VerifyContentAlfrescoResponse verifyContentAlfrescoResponse = this.factomVerifyTask.verifyHash(dummyHash.getBytes());
        assertEquals(Encoding.HEX.encode("Foo".getBytes()), verifyContentAlfrescoResponse.getHash());
    }
}
