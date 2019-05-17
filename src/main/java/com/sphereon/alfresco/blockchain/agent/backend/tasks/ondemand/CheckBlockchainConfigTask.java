package com.sphereon.alfresco.blockchain.agent.backend.tasks.ondemand;

import com.sphereon.sdk.blockchain.proof.api.ConfigurationApi;
import com.sphereon.sdk.blockchain.proof.handler.ApiException;
import com.sphereon.sdk.blockchain.proof.model.ChainSettings;
import com.sphereon.sdk.blockchain.proof.model.CreateConfigurationRequest;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

import static com.sphereon.sdk.blockchain.proof.model.ChainSettings.ContentRegistrationChainTypesEnum;
import static com.sphereon.sdk.blockchain.proof.model.ChainSettings.HashAlgorithmEnum;
import static com.sphereon.sdk.blockchain.proof.model.CreateConfigurationRequest.AccessModeEnum;

@Component
public class CheckBlockchainConfigTask implements Task<Void> {

    private static final String EXCEPTION_MESSAGE_GET_CONFIG = "An error occurred whilst retrieving blockchain configuration %s: %s";
    private static final String ASSERT_MESSAGE_GET_CONFIG = "An error occurred whilst retrieving blockchain configuration %s. The response was empty.";
    private static final String EXCEPTION_MESSAGE_CREATE_CONFIG = "An error occurred whilst creating blockchain configuration %s: %s";
    private static final String EXCEPTION_MESSAGE_CREATE_CONFIG_API = "An error occurred whilst creating blockchain configuration %s, HTTP status: %d\n Response body:\n%s";

    @Autowired
    private ConfigurationApi configurationApi;

    @Value("${blockchain.config-name:#{null}}")
    protected String configName;

    @Value("${blockchain.context:factom}")
    protected String context;


    @Override
    public Void execute() {
        try {
            final var configResponse = configurationApi.getConfiguration(configName);
            Assert.notNull(configResponse, String.format(ASSERT_MESSAGE_GET_CONFIG, configName));
            Assert.notNull(configResponse.getConfiguration(), String.format(ASSERT_MESSAGE_GET_CONFIG, configName));
        } catch (ApiException e) {
            if (e.getCode() == HttpStatus.NOT_FOUND.value()) {
                createConfiguration();
            } else {
                throw new RuntimeException(String.format(EXCEPTION_MESSAGE_GET_CONFIG, configName, "" + e.getCode()), e);
            }

        } catch (Throwable throwable) {
            throw new RuntimeException(String.format(EXCEPTION_MESSAGE_GET_CONFIG, configName, throwable.getMessage()), throwable);
        }
        return null;
    }


    private void createConfiguration() {
        try {
            final var chainSettings = new ChainSettings()
                .hashAlgorithm(HashAlgorithmEnum._256)
                .contentRegistrationChainTypes(List.of(ContentRegistrationChainTypesEnum.PER_HASH_PROOF_CHAIN, ContentRegistrationChainTypesEnum.SINGLE_PROOF_CHAIN));

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
        } catch (Throwable throwable) {
            throw new RuntimeException(String.format(EXCEPTION_MESSAGE_CREATE_CONFIG, configName, throwable.getMessage()), throwable);
        }
    }
}
