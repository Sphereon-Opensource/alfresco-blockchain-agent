package com.sphereon.alfresco.blockchain.agent.config;

import com.sphereon.libs.authentication.api.AuthenticationApi;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.libs.authentication.api.TokenResponse;
import com.sphereon.libs.authentication.api.config.ApiConfiguration;
import com.sphereon.libs.authentication.api.config.PersistenceType;
import com.sphereon.sdk.blockchain.proof.api.ConfigurationApi;
import com.sphereon.sdk.blockchain.proof.api.RegistrationApi;
import com.sphereon.sdk.blockchain.proof.api.VerificationApi;
import com.sphereon.sdk.blockchain.proof.handler.ApiClient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class BlockchainApiConfig {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BlockchainApiConfig.class);

    @Value("${sphereon.blockchain-proof-api.base-path:https://gw.api.cloud.sphereon.com/blockchain/proof/0.10}")
    String bcProofBasePath;

    @Value("${sphereon.blockchain-proof-api.base-path:https://gw.api.cloud.sphereon.com/crypto/keys/0.9}")
    String cryptoBasePath;

    @Value("${sphereon.api-client.timeout:120000}")
    int connectionTimeout;

    @Bean
    AuthenticationApi authenticationApi() {
        ApiConfiguration.Builder configBuilder = new ApiConfiguration.Builder()
                .withApplication("alfresco-blockchain")
                .withPersistenceType(PersistenceType.SYSTEM_ENVIRONMENT)
                .withEnvVarPrefix("BLOCKCHAIN");
        return new AuthenticationApi.Builder()
                .withConfiguration(configBuilder.build())
                .build();
    }

    @Bean
    TokenRequest tokenRequester(@Autowired AuthenticationApi authenticationApi) {
        return authenticationApi.requestToken()
                .build();
    }

    @Bean
    public VerificationApi bcProofVerificationApi(@Qualifier("tokenRequester") TokenRequest tokenRequester) {
        VerificationApi api = new VerificationApi();
        final ApiClient apiClient = api.getApiClient();
        apiClient.setBasePath(bcProofBasePath);
        apiClient.setConnectTimeout(connectionTimeout);
        apiClient.getHttpClient().setWriteTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
        apiClient.getHttpClient().setReadTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
        apiClient.setAccessToken(tokenRequester.execute().getAccessToken());
        tokenRequester.addTokenResponseListener(new TokenRequest.TokenResponseListener() {
            @Override
            public void tokenResponse(TokenResponse tokenResponse) {
                apiClient.setAccessToken(tokenResponse.getAccessToken());
            }


            @Override
            public void exception(Throwable throwable) {
                logger.error("An error occurred whilst renewing token for the blockchain verification API", throwable);
            }
        });
        return api;
    }

    @Bean
    public RegistrationApi bcProofRegistrationApi(@Qualifier("tokenRequester") TokenRequest tokenRequester) {
        RegistrationApi api = new RegistrationApi();
        final ApiClient apiClient = api.getApiClient();
        apiClient.setBasePath(bcProofBasePath);
        apiClient.setConnectTimeout(connectionTimeout);
        apiClient.getHttpClient().setWriteTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
        apiClient.getHttpClient().setReadTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
        apiClient.setAccessToken(tokenRequester.execute().getAccessToken());
        tokenRequester.addTokenResponseListener(new TokenRequest.TokenResponseListener() {
            @Override
            public void tokenResponse(TokenResponse tokenResponse) {
                apiClient.setAccessToken(tokenResponse.getAccessToken());
            }


            @Override
            public void exception(Throwable throwable) {
                logger.error("An error occurred whilst renewing token for the blockchain verification API", throwable);
            }
        });
        return api;
    }

    @Bean
    public ConfigurationApi configurationApi(@Qualifier("tokenRequester") TokenRequest tokenRequester) {
        ConfigurationApi api = new ConfigurationApi();
        final ApiClient apiClient = api.getApiClient();
        apiClient.setBasePath(bcProofBasePath);
        apiClient.setConnectTimeout(connectionTimeout);
        apiClient.getHttpClient().setWriteTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
        apiClient.getHttpClient().setReadTimeout(connectionTimeout, TimeUnit.MILLISECONDS);
        apiClient.setAccessToken(tokenRequester.execute().getAccessToken());
        tokenRequester.addTokenResponseListener(new TokenRequest.TokenResponseListener() {
            @Override
            public void tokenResponse(TokenResponse tokenResponse) {
                apiClient.setAccessToken(tokenResponse.getAccessToken());
            }

            @Override
            public void exception(Throwable throwable) {
                logger.error("An error occurred whilst renewing token for the blockchain verification API", throwable);
            }
        });
        return api;
    }
}
