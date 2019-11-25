package com.sphereon.alfresco.blockchain.agent.factom.config;

import org.blockchain_innovation.factom.client.api.FactomdClient;
import org.blockchain_innovation.factom.client.api.WalletdClient;
import org.blockchain_innovation.factom.client.api.model.Address;
import org.blockchain_innovation.factom.client.api.settings.RpcSettings;
import org.blockchain_innovation.factom.client.impl.FactomdClientImpl;
import org.blockchain_innovation.factom.client.impl.WalletdClientImpl;
import org.blockchain_innovation.factom.client.impl.settings.RpcSettingsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class FactomConfig {
    @Bean
    @Scope("prototype")
    public FactomdClient factomdClient() {
        final var factomdClient = new FactomdClientImpl();
        factomdClient.setSettings(settingsFor(RpcSettings.SubSystem.FACTOMD));
        return factomdClient;
    }

    @Bean
    @Scope("prototype")
    public WalletdClient walletdClient() {
        final var walletdClient = new WalletdClientImpl();
        walletdClient.setSettings(settingsFor(RpcSettings.SubSystem.WALLETD));
        return walletdClient;
    }

    @Bean
    public Address entryCreditsAddress(@Value("${sphereon.blockchain.agent.factom.entry-credits.address}") final String ecAddress) {
        return new Address(ecAddress);
    }

    private RpcSettingsImpl settingsFor(final RpcSettings.SubSystem subsystem) {
        return new RpcSettingsImpl(subsystem, new RpcSettingsImpl.ServerImpl(subsystem));
    }
}
