package com.sphereon.alfresco.blockchain.agent.sphereon.proof;

import com.sphereon.alfresco.blockchain.agent.AlfrescoBlockchainAgentApp;
import com.sphereon.alfresco.blockchain.agent.utils.Signer;
import com.sphereon.sdk.blockchain.proof.api.ConfigurationApi;
import com.sphereon.sdk.blockchain.proof.api.RegistrationApi;
import com.sphereon.sdk.blockchain.proof.api.VerificationApi;
import com.sphereon.sdk.blockchain.proof.handler.ApiException;
import com.sphereon.sdk.blockchain.proof.model.ConfigurationResponse;
import com.sphereon.sdk.blockchain.proof.model.ModelConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = AlfrescoBlockchainAgentApp.class)
@MockBeans({
        @MockBean(ConfigurationApi.class),
        @MockBean(VerificationApi.class),
        @MockBean(RegistrationApi.class),
        @MockBean(Signer.class)
})
public class BlockchainProofConfigVerifierSpringTest {
    /**
     * TestConfiguration class to setup methods on configurationApi mock before it will be used in {@code ApplicationListener<ApplicationReadyEvent>}
     * {@code @Before} is called too late for this and {@code @BeforeClass} will not have access to the bean.
     */
    @Configuration
    public static class VerifierTestConfiguration {
        @Autowired
        private ConfigurationApi configurationApiMock;

        @PostConstruct
        public void setup() throws ApiException {
            when(configurationApiMock.getConfiguration(eq("dummy-proof-config")))
                    .thenReturn(new ConfigurationResponse()._configuration(new ModelConfiguration()));
        }
    }

    @Autowired
    private ConfigurationApi configurationApiMock;

    @Test
    @Ignore("This test needs environment variables which are difficult to mock. Should be fixed in authentication-lib. See README")
    public void shouldBeCalledOnApplicationStart() throws ApiException {
        verify(configurationApiMock).getConfiguration(eq("dummy-proof-config"));
        verifyNoMoreInteractions(configurationApiMock);
    }
}
