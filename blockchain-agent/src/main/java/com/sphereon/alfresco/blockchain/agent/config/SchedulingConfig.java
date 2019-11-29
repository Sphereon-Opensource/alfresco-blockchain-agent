package com.sphereon.alfresco.blockchain.agent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Profile("!disable-scheduling")
@EnableScheduling
public class SchedulingConfig {
}
