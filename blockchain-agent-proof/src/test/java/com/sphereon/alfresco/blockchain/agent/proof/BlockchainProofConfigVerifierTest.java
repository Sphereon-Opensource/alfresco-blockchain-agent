package com.sphereon.alfresco.blockchain.agent.proof;

import com.sphereon.sdk.blockchain.proof.api.ConfigurationApi;
import com.sphereon.sdk.blockchain.proof.handler.ApiException;
import com.sphereon.sdk.blockchain.proof.model.ConfigurationResponse;
import com.sphereon.sdk.blockchain.proof.model.CreateConfigurationRequest;
import com.sphereon.sdk.blockchain.proof.model.ModelConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class BlockchainProofConfigVerifierTest {
    private ConfigurationApi configurationApiMock;

    private BlockchainProofConfigVerifier verifier;

    @Before
    public void setup() {
        this.configurationApiMock = mock(ConfigurationApi.class);
        this.verifier = new BlockchainProofConfigVerifier(this.configurationApiMock, "dummy-proof-config", "");
    }

    @After
    public void breakdown() throws ApiException {
        verify(configurationApiMock).getConfiguration(eq("dummy-proof-config"));
        verifyNoMoreInteractions(configurationApiMock);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionOnMissingConfigurationHolder() throws ApiException {
        when(configurationApiMock.getConfiguration(eq("dummy-proof-config")))
                .thenReturn(null);
        this.verifier.onApplicationEvent(null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionOnMissingConfiguration() throws ApiException {
        when(configurationApiMock.getConfiguration(eq("dummy-proof-config")))
                .thenReturn(new ConfigurationResponse());
        this.verifier.onApplicationEvent(null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionOnInvalidHttpCall() throws ApiException {
        when(configurationApiMock.getConfiguration(eq("dummy-proof-config")))
                .thenThrow(new ApiException());
        this.verifier.onApplicationEvent(null);
    }

    @Test
    public void shouldNotThrowExceptionOnSuccessfulConfigurationRetrieval() throws ApiException {
        when(configurationApiMock.getConfiguration(eq("dummy-proof-config")))
                .thenReturn(new ConfigurationResponse()._configuration(new ModelConfiguration()));
        this.verifier.onApplicationEvent(null);
    }

    @Test
    public void shouldCreateConfigurationWhenItDoesntExist() throws ApiException {
        when(configurationApiMock.getConfiguration(eq("dummy-proof-config")))
                .thenThrow(new ApiException(404, "Configuration not found"));
        when(configurationApiMock.createConfiguration(any(CreateConfigurationRequest.class)))
                .thenReturn(new ConfigurationResponse()._configuration(new ModelConfiguration()));
        this.verifier.onApplicationEvent(null);

        verify(configurationApiMock).createConfiguration(any(CreateConfigurationRequest.class));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenCreateConfigurationFails() throws ApiException {
        when(configurationApiMock.getConfiguration(eq("dummy-proof-config")))
                .thenThrow(new ApiException(404, "Configuration not found"));
        when(configurationApiMock.createConfiguration(any(CreateConfigurationRequest.class)))
                .thenThrow(new ApiException());
        try {
            this.verifier.onApplicationEvent(null);
        } finally {
            verify(configurationApiMock).createConfiguration(any(CreateConfigurationRequest.class));
        }
    }
}
