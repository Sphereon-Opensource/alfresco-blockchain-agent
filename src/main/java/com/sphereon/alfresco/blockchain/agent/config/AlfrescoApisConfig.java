package com.sphereon.alfresco.blockchain.agent.config;

import com.alfresco.apis.api.AuditApi;
import com.alfresco.apis.api.AuthenticationApi;
import com.alfresco.apis.api.NodesApi;
import com.alfresco.apis.api.PeopleApi;
import com.alfresco.apis.api.QueriesApi;
import com.alfresco.apis.api.SearchApi;
import com.alfresco.apis.api.SitesApi;
import com.alfresco.apis.handler.ApiClient;
import com.alfresco.apis.handler.auth.HttpBasicAuth;
import com.squareup.okhttp.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Configuration
class AlfrescoApisConfig {
    private static final String AUTHENTICATION_API_PATH = "/alfresco/api/-default-/public/authentication/versions/1";
    private static final String CORE_API_PATH = "/alfresco/api/-default-/public/alfresco/versions/1";
    private static final String SEARCH_API_PATH = "/alfresco/api/-default-/public/search/versions/1";
    private static final String CMIS_API_PATH = "/alfresco/api/-default-/public/cmis/versions/1.1/atom";

    private final ApiClient authenticationApiClient;
    private final ApiClient coreApiClient;
    private final ApiClient searchApiClient;

    private final int connectionTimeout;
    private final String alfrescoDnsName;
    private final String userName;
    private final String password;

    AlfrescoApisConfig(@Value("${sphereon.blockchain.agent.alfresco.api-client.timeout:40000}") final int connectionTimeout,
                       @Value("${sphereon.blockchain.agent.alfresco.dns-name}") final String alfrescoDnsName,
                       @Value("${sphereon.blockchain.agent.alfresco.username}") final String userName,
                       @Value("${sphereon.blockchain.agent.alfresco.password}") final String password) {
        this.authenticationApiClient = new ApiClient();
        this.coreApiClient = new ApiClient();
        this.searchApiClient = new ApiClient();
        this.connectionTimeout = connectionTimeout;
        this.alfrescoDnsName = alfrescoDnsName;
        this.userName = userName; // from system env
        this.password = password; // from system env
    }

    @PostConstruct
    void init() {
        configureClient(authenticationApiClient, AUTHENTICATION_API_PATH);
        configureClient(coreApiClient, CORE_API_PATH);
        configureClient(searchApiClient, SEARCH_API_PATH);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    AuthenticationApi alfrescoAuthenticationApi() {
        return new AuthenticationApi(authenticationApiClient);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    NodesApi alfrescoNodesApi() {
        return new NodesApi(coreApiClient);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    QueriesApi alfrescoQueriesApi() {
        return new QueriesApi(coreApiClient);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    SitesApi alfrescoSitesApi() {
        return new SitesApi(coreApiClient);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    AuditApi alfrescoAuditApi() {
        return new AuditApi(coreApiClient);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    PeopleApi alfrescoPeopleApi() {
        return new PeopleApi(coreApiClient);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    SearchApi alfrescoSearchApi() {
        return new SearchApi(searchApiClient);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    ApiClient cmisApiClient() {
        final var cmisApiClient = new ApiClient();
        configureClient(cmisApiClient, CMIS_API_PATH);
        return cmisApiClient;
    }

    private void configureClient(final ApiClient apiClient, final String apiPath) {
        configureClientAuthentication(apiClient);
        apiClient.setBasePath("https://" + alfrescoDnsName + apiPath);
        configureClientTimeouts(apiClient);
    }

    private void configureClientTimeouts(final ApiClient apiClient) {
        apiClient.setConnectTimeout(connectionTimeout);
        final OkHttpClient httpClient = apiClient.getHttpClient();
        httpClient.setConnectTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
        httpClient.setReadTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
        httpClient.setWriteTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
    }

    private void configureClientAuthentication(final ApiClient apiClient) {
        final var authentication = (HttpBasicAuth) apiClient.getAuthentication("basicAuth");
        authentication.setUsername(userName);
        authentication.setPassword(password);
    }
}
