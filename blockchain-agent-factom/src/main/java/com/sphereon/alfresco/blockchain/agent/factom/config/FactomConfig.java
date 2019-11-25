package com.sphereon.alfresco.blockchain.agent.factom.config;

import org.blockchain_innovation.factom.client.api.FactomdClient;
import org.blockchain_innovation.factom.client.api.WalletdClient;
import org.blockchain_innovation.factom.client.api.model.Address;
import org.blockchain_innovation.factom.client.impl.FactomdClientImpl;
import org.blockchain_innovation.factom.client.impl.WalletdClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FactomConfig {
    @Bean
    public FactomdClient factomdClient() {
        return new FactomdClientImpl();
    }

    @Bean
    public WalletdClient walletdClient() {
        return new WalletdClientImpl();
    }

    @Bean
    public Address entryCreditsAddress(@Value("${sphereon.blockchain.agent.factom.entry-credits.address}") final String ecAddress) {
        return new Address(ecAddress);
    }
}
