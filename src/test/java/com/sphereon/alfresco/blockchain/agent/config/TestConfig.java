package com.sphereon.alfresco.blockchain.agent.config;

/**
 * Setup the following keys in your system environment before running the tests:
 * - authentication-api.ExpiringTokens.consumer-key
 * - authentication-api.ExpiringTokens.consumer-secret
 */

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.SimpleThreadScope;

@Configuration
@ComponentScan(basePackages = {"com.sphereon.alfresco.blockchain.agent", "com.sphereon.libs"})
@Profile("test")
public class TestConfig {
    @Bean
    public CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
        customScopeConfigurer.addScope("session", new SimpleThreadScope());
        return customScopeConfigurer;
    }

}
