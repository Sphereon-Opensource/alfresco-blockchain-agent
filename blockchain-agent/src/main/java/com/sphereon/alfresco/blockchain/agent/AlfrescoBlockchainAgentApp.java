package com.sphereon.alfresco.blockchain.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackages = {"com.sphereon.alfresco.blockchain.agent", "com.sphereon.libs"})
public class AlfrescoBlockchainAgentApp {
    public static void main(String[] args) {
        SpringApplication.run(AlfrescoBlockchainAgentApp.class, args);
    }
}
