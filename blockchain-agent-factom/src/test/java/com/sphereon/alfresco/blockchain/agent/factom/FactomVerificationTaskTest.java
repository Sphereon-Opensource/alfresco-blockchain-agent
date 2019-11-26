package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class FactomVerificationTaskTest {
    @Autowired
    private FactomVerificationTask factomVerificationTask;

    @Test
    public void verifyHash() {
        final var dummyHash = "Dummy-hash";
        final VerifyContentAlfrescoResponse verifyContentAlfrescoResponse = this.factomVerificationTask.verifyHash(dummyHash.getBytes());
        assertEquals("fe07dab8c2917366f376b454736ac07865626074691b30ffddddda4ff02a9451", verifyContentAlfrescoResponse.getHash());
    }
}
