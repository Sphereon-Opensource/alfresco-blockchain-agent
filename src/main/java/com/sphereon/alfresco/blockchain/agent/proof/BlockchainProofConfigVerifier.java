package com.sphereon.alfresco.blockchain.agent.proof;

import com.sphereon.sdk.blockchain.proof.api.ConfigurationApi;
import com.sphereon.sdk.blockchain.proof.handler.ApiException;
import com.sphereon.sdk.blockchain.proof.model.ChainSettings;
import com.sphereon.sdk.blockchain.proof.model.CreateConfigurationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.List;

import static com.sphereon.sdk.blockchain.proof.model.ChainSettings.ContentRegistrationChainTypesEnum;
import static com.sphereon.sdk.blockchain.proof.model.ChainSettings.HashAlgorithmEnum;
import static com.sphereon.sdk.blockchain.proof.model.CreateConfigurationRequest.AccessModeEnum;

/**
 * Task that verifies that the Blockchain Proof config is valid. Annotated with {@code @ApplicationListener} to run as startup-time check.
 */
@Component
public class BlockchainProofConfigVerifier implements ApplicationListener<ApplicationReadyEvent> {
    private static final String EXCEPTION_MESSAGE_GET_CONFIG = "An error occurred whilst retrieving blockchain configuration %s: %s";
    private static final String ASSERT_MESSAGE_GET_CONFIG = "An error occurred whilst retrieving blockchain configuration %s. The response was empty.";
    private static final String EXCEPTION_MESSAGE_CREATE_CONFIG = "An error occurred whilst creating blockchain configuration %s: %s";
    private static final String EXCEPTION_MESSAGE_CREATE_CONFIG_API = "An error occurred whilst creating blockchain configuration %s, HTTP status: %d\n Response body:\n%s";

    private final ConfigurationApi configurationApi;
    private final String configName;
    private final String context;

    public BlockchainProofConfigVerifier(final ConfigurationApi configurationApi,
                                         @Value("${sphereon.blockchain.agent.blockchain-proof.config-name}") final String configName,
                                         @Value("${sphereon.blockchain.agent.blockchain-proof.context:factom}") final String context) {
        this.configurationApi = configurationApi;
        this.configName = configName;
        this.context = context;
    }

    @Override
    public void onApplicationEvent(@Nonnull final ApplicationReadyEvent applicationReadyEvent) {
        verifyValidBlockchainProofApiConfig();
    }

    public void verifyValidBlockchainProofApiConfig() {
        try {
            final var configResponse = configurationApi.getConfiguration(configName);
            Assert.notNull(configResponse, String.format(ASSERT_MESSAGE_GET_CONFIG, configName));
            Assert.notNull(configResponse.getConfiguration(), String.format(ASSERT_MESSAGE_GET_CONFIG, configName));
        } catch (final ApiException e) {
            if (e.getCode() != HttpStatus.NOT_FOUND.value()) {
                throw new RuntimeException(String.format(EXCEPTION_MESSAGE_GET_CONFIG, configName, "" + e.getCode()), e);
            }
            createConfiguration();
        } catch (final Exception exception) {
            throw new RuntimeException(String.format(EXCEPTION_MESSAGE_GET_CONFIG, configName, exception.getMessage()), exception);
        }
    }

    private void createConfiguration() {
        try {
            final var proofTypes = List.of(ContentRegistrationChainTypesEnum.PER_HASH_PROOF_CHAIN, ContentRegistrationChainTypesEnum.SINGLE_PROOF_CHAIN);
            final var chainSettings = new ChainSettings()
                    .hashAlgorithm(HashAlgorithmEnum._256)
                    .contentRegistrationChainTypes(proofTypes);

            final var createConfigRequest = new CreateConfigurationRequest()
                    .name(configName)
                    .context(context)
                    .accessMode(AccessModeEnum.PRIVATE)
                    .initialSettings(chainSettings);
            final var configResponse = configurationApi.createConfiguration(createConfigRequest);
            Assert.notNull(configResponse, String.format(ASSERT_MESSAGE_GET_CONFIG, configName));
            Assert.notNull(configResponse.getConfiguration(), String.format(ASSERT_MESSAGE_GET_CONFIG, configName));
        } catch (ApiException e) {
            throw new RuntimeException(String.format(EXCEPTION_MESSAGE_CREATE_CONFIG_API, configName, e.getCode(), e.getResponseBody()), e);
        } catch (Exception exception) {
            throw new RuntimeException(String.format(EXCEPTION_MESSAGE_CREATE_CONFIG, configName, exception.getMessage()), exception);
        }
    }
}
