package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.libs.blockchain.commons.Digest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class FactomSignNewDocumentTaskTest {
    @Autowired
    private FactomSignNewDocumentTask factomSignNewDocumentTask;

    @Test
    public void testRegisterEntry() {
        this.factomSignNewDocumentTask.registerEntry("Foo".getBytes(), Digest.Algorithm.SHA_256);
    }
}
