package com.sphereon.alfresco.blockchain.agent.config;

import com.sphereon.libs.blockchain.commons.Digest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlockchainConfig {
    @Bean
    public Digest.Algorithm getAlgorithm(@Value("${sphereon.blockchain.agent.alfresco.hash.algorithm:SHA-265}") final String hashAlgorithm) {
        return Digest.Algorithm.from(hashAlgorithm);
    }
}
