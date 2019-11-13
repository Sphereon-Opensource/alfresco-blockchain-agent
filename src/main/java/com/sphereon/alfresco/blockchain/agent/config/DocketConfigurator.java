package com.sphereon.alfresco.blockchain.agent.config;

import com.sphereon.alfresco.blockchain.agent.rest.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.Contact;

import static com.sphereon.alfresco.blockchain.agent.config.RestControllerConfigTemplate.Mode;
import static com.sphereon.alfresco.blockchain.agent.config.RestControllerConfigTemplate.SimplifiedDocketConfigurator;

@Configuration
public class DocketConfigurator implements SimplifiedDocketConfigurator {
    @Value(value = "${alfresco-blockchain.api.version}")
    private String apiVersion;

    @Value(value = "${alfresco.dns-name}")
    private String dnsName;

    @Override
    public void configureDocket(Builder docketBuilder, Mode mode) {
        docketBuilder
                .withPathMapping(Mode.DEFAULT, "/alfresco-blockchain/")
                .withPathSelector("^/(alfresco-blockchain.*)")
                .withGatewayHostname(dnsName + "/agent")
                .withTags(Constants.TagEnum.ALFRESCO_BLOCKCHAIN_SERVICE.asTag());
    }

    @Override
    public void configureApiInfo(ApiInfoBuilder apiInfoBuilder, Mode mode) {
        apiInfoBuilder.title("Alfresco Blockchain Agent for Alfresco API's")
                .description(description(mode))
                .contact(new Contact("Sphereon DevOps Team", "https://sphereon.com", "dev@sphereon.com"))
                .license("Apache License Version 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0")
                .version(apiVersion);
    }

    private String description(Mode mode) {
        final String desc = "This is an API containing functions for blockchain integration with Alfresco.\r\n";
        if (mode == Mode.DEFAULT) {
            return desc.replaceAll("<[^>]+>", "");
        }
        return desc;
    }
}
