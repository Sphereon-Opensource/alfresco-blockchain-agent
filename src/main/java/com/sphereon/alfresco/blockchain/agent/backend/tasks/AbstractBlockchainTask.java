package com.sphereon.alfresco.blockchain.agent.backend.tasks;

import com.alfresco.apis.api.NodesApi;
import com.alfresco.apis.api.SearchApi;
import com.alfresco.apis.handler.ApiClient;
import com.alfresco.apis.handler.ApiException;
import com.alfresco.apis.handler.Pair;
import com.alfresco.apis.handler.auth.HttpBasicAuth;
import com.alfresco.apis.model.NodeBodyUpdate;
import com.alfresco.apis.model.NodeEntry;
import com.alfresco.apis.model.RequestInclude;
import com.alfresco.apis.model.RequestQuery;
import com.alfresco.apis.model.ResultSetPaging;
import com.alfresco.apis.model.ResultSetRowEntry;
import com.alfresco.apis.model.SearchRequest;
import com.google.common.collect.ImmutableMap;
import com.sphereon.alfresco.blockchain.agent.backend.AlfrescoBlockchainRegistrationState;
import com.sphereon.alfresco.blockchain.agent.backend.commands.certficate.Signer;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.libs.blockchain.commons.Digest;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import com.squareup.okhttp.Response;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public abstract class AbstractBlockchainTask {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final String EXCEPTION_MESSAGE_FETCH_CONTENT = "An error occurred whilst fetching content: "; // TODO: move to constants static class

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractBlockchainTask.class);
    public static final String EXCEPTION_MESSAGE_GET_NODE = "An error occurred whilst getting node %s: %s";

    @Autowired
    protected TokenRequest tokenRequester;

    @Autowired
    private SearchApi alfrescoSearchApi;

    @Autowired
    private ApiClient cmisApiClient;

    @Autowired
    private NodesApi alfrescoNodesApi;

    @Autowired
    protected Signer signer;

    @Value("${blockchain.config-name:#{null}}")
    protected String configName;

    @Value("${alfresco.query.model}")
    private String model;

    @Value("${alfresco.query.registration-state:RegistrationState}")
    private String registrationStateProperty;

    protected List<ResultSetRowEntry> selectEntries(final AlfrescoBlockchainRegistrationState state) throws ApiException {
        SearchRequest request = new SearchRequest();
        RequestQuery query = new RequestQuery();
        query.setLanguage(RequestQuery.LanguageEnum.AFTS);
        query.setQuery('{' + model + '}' + registrationStateProperty + ':' + state.getKey());
        request.setQuery(query);
        RequestInclude include = new RequestInclude();
        include.add("properties");
        request.include(include);

        ResultSetPaging pagedResult = alfrescoSearchApi.search(request);
        return pagedResult.getList().getEntries();
    }

    protected List<NodeEntry> selectEntries(final List<String> nodeIds) {
        final List<NodeEntry> nodeEntries = new ArrayList<>();
        final List<String> include = new ArrayList<>();
        include.add("properties");

        nodeIds.forEach(nodeId -> {
            try {
                nodeEntries.add(alfrescoNodesApi.getNode(nodeId, include, null, null));
            } catch (ApiException e) {
                String errorMessage = "http status: " + e.getCode() + "\r\n" + e.getResponseBody();
                throw new RuntimeException(String.format(EXCEPTION_MESSAGE_GET_NODE, nodeId, errorMessage), e);

            } catch (Throwable throwable) {
                throw new RuntimeException(String.format(EXCEPTION_MESSAGE_GET_NODE, nodeId, throwable.getMessage()), throwable);
            }
        });
        return nodeEntries;
    }

    protected byte[] hashEntry(String nodeId) {
        final String[] localVarAuthNames = new String[]{"basicAuth"};
        try {
            var call = cmisApiClient.buildCall("/content", "GET", List.of(new Pair("id", nodeId)), null,
                    new HashMap<>(), null, localVarAuthNames, null);
            Response response = call.execute();
            if (response.code() == 200) {
                return Digest.getInstance().getHashAsHex(Digest.Algorithm.SHA_256, response.body().byteStream()); // TODO: make configurable
            } else {
                throw new RuntimeException("Content request returned http code " + response.code());
            }
        } catch (ApiException e) {
            throw new RuntimeException(EXCEPTION_MESSAGE_FETCH_CONTENT + e.getCode(), e);

        } catch (Throwable throwable) {
            throw new RuntimeException(EXCEPTION_MESSAGE_FETCH_CONTENT + throwable.getMessage(), throwable);
        }
    }

    protected void updateMetadata(String id, VerifyContentResponse.RegistrationStateEnum state) {
        updateMetadata(id, state, null);
    }

    protected void updateMetadata(String id, VerifyContentResponse.RegistrationStateEnum state, VerifyContentResponse verifyContentResponse) {
        String now = TIME_FORMATTER.format(ZonedDateTime.now());
        var properties = ImmutableMap.<String, String>builder()
                .put("bc:LastVerificationTime", now);

        switch (state) {
            case NOT_REGISTERED:
                properties.put("bc:RegistrationState", AlfrescoBlockchainRegistrationState.NOT_REGISTERED.getKey());
                break;
            case PENDING:
                properties.put("bc:RegistrationState", AlfrescoBlockchainRegistrationState.PENDING_VERIFICATION.getKey());
                break;
            case REGISTERED:
                properties.put("bc:RegistrationState", AlfrescoBlockchainRegistrationState.REGISTERED.getKey());
                if (verifyContentResponse != null) {
                    if (verifyContentResponse.getRegistrationTime() != null) {
                        properties.put("bc:RegistrationTime", TIME_FORMATTER.format(verifyContentResponse.getRegistrationTime()));
                    }
                    if (verifyContentResponse.getPerHashProofChain() != null) {
                        properties.put("bc:ExternalLinks", createLink(verifyContentResponse.getPerHashProofChain().getChainId()));
                    } else if (verifyContentResponse.getSingleProofChain() != null) {
                        properties.put("bc:ExternalLinks", createLink(verifyContentResponse.getSingleProofChain().getChainId()));
                    }
                }
                break;
        }
        updateNode(id, properties.build());
    }

    private String createLink(String chainId) {
        return "https://explorer.factoid.org/data?type=chain&key=" + chainId;
    }

    private void updateNode(String id, Map<String, String> map) {
        try {
            NodeBodyUpdate update = new NodeBodyUpdate();
            update.setProperties(map);
            RequestInclude include = new RequestInclude();
            include.add("properties");
            logger.info("Updating node " + id);
            map.forEach((key, value2) -> {
                        logger.info("  - " + key + '=' + value2);
                    }
            );
            alfrescoNodesApi.updateNode(id, update, include, null);

            logger.info("Node " + id + " updated");
        } catch (ApiException e) {
            throw new RuntimeException("An error occurred whilst updating state in node " + id, e);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred whilst updating state in node " + id, e);
        }
    }

    public void updateCredentials(String userName, String password) {
        HttpBasicAuth authentication = (HttpBasicAuth) alfrescoSearchApi.getApiClient().getAuthentication("basicAuth");
        authentication.setUsername(userName);
        authentication.setPassword(password);
        authentication = (HttpBasicAuth) cmisApiClient.getAuthentication("basicAuth");
        authentication.setUsername(userName);
        authentication.setPassword(password);
    }
}
