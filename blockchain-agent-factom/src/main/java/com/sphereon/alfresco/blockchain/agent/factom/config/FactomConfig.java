package com.sphereon.alfresco.blockchain.agent.factom.config;

import com.sphereon.alfresco.blockchain.agent.factom.FactomClient;
import org.blockchain_innovation.factom.client.api.FactomdClient;
import org.blockchain_innovation.factom.client.api.WalletdClient;
import org.blockchain_innovation.factom.client.api.model.Address;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.blockchain_innovation.factom.client.api.settings.RpcSettings;
import org.blockchain_innovation.factom.client.api.settings.RpcSettings.SubSystem;
import org.blockchain_innovation.factom.client.impl.FactomdClientImpl;
import org.blockchain_innovation.factom.client.impl.WalletdClientImpl;
import org.blockchain_innovation.factom.client.impl.settings.RpcSettingsImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Properties;
import java.util.UUID;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;

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
        factomdClient.setSettings(factomDSettingsFrom(url, timeout, username, password));
        return factomdClient;
    }

    @Bean
    @Scope("prototype")
    public WalletdClient walletdClient(@Value("${sphereon.blockchain.agent.factom.walletd.url:#{null}}") final String url,
                                       @Value("${sphereon.blockchain.agent.factom.walletd.timeout:#{null}}") final String timeout,
                                       @Value("${sphereon.blockchain.agent.factom.walletd.username:#{null}}") final String username,
                                       @Value("${sphereon.blockchain.agent.factom.walletd.password:#{null}}") final String password) {
        final var walletdClient = new WalletdClientImpl();
        walletdClient.setSettings(walletDSettingsFrom(url, timeout, username, password));
        return walletdClient;
    }

    @Bean
    public Address entryCreditsAddress(@Value("${sphereon.blockchain.agent.factom.entry-credits.address}") final String ecAddress) {
        return new Address(ecAddress);
    }

    @Bean
    public String factomChainId(final FactomClient factomClient,
                                @Value("${sphereon.blockchain.agent.factom.chain.id:#{null}}") final String configuredChainId,
                                @Value("${sphereon.blockchain.agent.factom.chain.create:false}") final boolean shouldCreateChain) {
        if (configuredChainId != null && !configuredChainId.isEmpty()) {
            return configuredChainId;
        }

        if (!shouldCreateChain) {
            throw new IllegalStateException("Configuration did not specify a chain ID or to create a new chain");
        }

        final var entry = new Entry();
        entry.setContent(UUID.randomUUID().toString());
        entry.setExternalIds(emptyList());
        final var chainId = factomClient.createChainFromEntry(entry)
                .getChainId();
        logger.info("Created chain " + chainId);
        return chainId;
    }

    private RpcSettings factomDSettingsFrom(final String url,
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
}
