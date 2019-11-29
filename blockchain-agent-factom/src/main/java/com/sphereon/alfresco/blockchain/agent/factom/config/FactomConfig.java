package com.sphereon.alfresco.blockchain.agent.factom.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sphereon.alfresco.blockchain.agent.factom.FactomClient;
import com.sphereon.libs.blockchain.commons.Digest;
import org.blockchain_innovation.factom.client.api.FactomdClient;
import org.blockchain_innovation.factom.client.api.WalletdClient;
import org.blockchain_innovation.factom.client.api.model.Address;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.blockchain_innovation.factom.client.api.settings.RpcSettings;
import org.blockchain_innovation.factom.client.api.settings.RpcSettings.SubSystem;
import org.blockchain_innovation.factom.client.impl.FactomdClientImpl;
import org.blockchain_innovation.factom.client.impl.OfflineWalletdClientImpl;
import org.blockchain_innovation.factom.client.impl.WalletdClientImpl;
import org.blockchain_innovation.factom.client.impl.settings.RpcSettingsImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;

@Configuration
public class FactomConfig {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FactomConfig.class);

    @Bean
    @Scope("prototype")
    public FactomdClient factomdClient(@Value("${sphereon.blockchain.agent.factom.factomd.url:#{null}}") final String url,
                                       @Value("${sphereon.blockchain.agent.factom.factomd.timeout:#{null}}") final String timeout,
                                       @Value("${sphereon.blockchain.agent.factom.factomd.username:#{null}}") final String username,
                                       @Value("${sphereon.blockchain.agent.factom.factomd.password:#{null}}") final String password) {
        final var factomdClient = new FactomdClientImpl();
        factomdClient.setSettings(factomdSettingsFrom(url, timeout, username, password));
        return factomdClient;
    }

    @Bean
    @ConditionalOnExpression("#{T(org.blockchain_innovation.factom.client.api.model.types.AddressType).ENTRY_CREDIT_PUBLIC.isValid('${sphereon.blockchain.agent.factom.entry-credits.address}')}")
    @Scope("prototype")
    public WalletdClient walletDClient(@Value("${sphereon.blockchain.agent.factom.walletd.url:#{null}}") final String url,
                                       @Value("${sphereon.blockchain.agent.factom.walletd.timeout:#{null}}") final String timeout,
                                       @Value("${sphereon.blockchain.agent.factom.walletd.username:#{null}}") final String username,
                                       @Value("${sphereon.blockchain.agent.factom.walletd.password:#{null}}") final String password) {
        final var walletdClient = new WalletdClientImpl();
        walletdClient.setSettings(walletDSettingsFrom(url, timeout, username, password));
        return walletdClient;
    }

    @Bean
    @ConditionalOnExpression("#{T(org.blockchain_innovation.factom.client.api.model.types.AddressType).ENTRY_CREDIT_SECRET.isValid('${sphereon.blockchain.agent.factom.entry-credits.address}')}")
    @Scope("prototype")
    public WalletdClient offlineWalletDClient() {
        return new OfflineWalletdClientImpl();
    }

    @Bean
    public Address entryCreditsAddress(@Value("${sphereon.blockchain.agent.factom.entry-credits.address}") final String ecAddress) {
        return new Address(ecAddress);
    }

    @Bean
    public String factomChainId(final FactomClient factomClient,
                                @Value("${sphereon.blockchain.agent.factom.chain.id:#{null}}") final String configuredChainId,
                                @Value("${sphereon.blockchain.agent.factom.chain.names:#{T(java.util.Collections).emptyList()}}") final List<String> configuredChainNames,
                                final ObjectMapper objectMapper,
                                final Digest.Algorithm hashAlgorithm) throws JsonProcessingException {
        final boolean isChainIdConfigured = configuredChainId != null && !configuredChainId.isEmpty();
        final boolean isChainNamesConfigured = configuredChainNames != null && !configuredChainNames.isEmpty();

        if (isChainIdConfigured && isChainNamesConfigured) {
            throw new IllegalStateException("Both chain ID and chain names configured, expected only one property");
        }

        if (isChainIdConfigured) {
            return configuredChainId;
        }

        final var entry = new Entry();
        entry.setContent(contentFrom(hashAlgorithm, objectMapper));
        entry.setExternalIds(configuredChainNames);

        // TODO: Check whether chain already exists

        final var chainId = factomClient.createChainFromEntry(entry).getChainId();
        logger.info("Created chain " + chainId);
        return chainId;
    }

    private String contentFrom(final Digest.Algorithm hashAlgorithm, final ObjectMapper objectMapper) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new FirstChainEntryContent(hashAlgorithm));
    }

    private RpcSettings factomdSettingsFrom(final String url,
                                            final String timeout,
                                            final String username,
                                            final String password) {
        return settingsFrom(SubSystem.FACTOMD, url, timeout, username, password);
    }

    private RpcSettings walletDSettingsFrom(final String url,
                                            final String timeout,
                                            final String username,
                                            final String password) {
        return settingsFrom(SubSystem.WALLETD, url, timeout, username, password);
    }

    private RpcSettings settingsFrom(final SubSystem subSystem,
                                     final String url,
                                     final String timeout,
                                     final String username,
                                     final String password) {
        final var subSystemKey = subSystem.configKey();
        final var properties = new Properties();

        final BiConsumer<String, String> setProperty = (configKey, value) -> {
            if (value != null && !value.isEmpty()) {
                properties.setProperty(subSystemKey + "." + configKey, value);
            }
        };

        setProperty.accept("url", url);
        setProperty.accept("timeout", timeout);
        setProperty.accept("username", username);
        setProperty.accept("password", password);

        return new RpcSettingsImpl(subSystem, properties);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class FirstChainEntryContent {
        private final String hashAlgorithm;

        private FirstChainEntryContent(final Digest.Algorithm hashAlgorithm) {
            this.hashAlgorithm = hashAlgorithm.getImplementation();
        }

        @JsonProperty("hash_algorithm")
        public String getHashAlgorithm() {
            return hashAlgorithm;
        }
    }
}
