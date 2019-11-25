package com.sphereon.alfresco.blockchain.agent.tasks;

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
import com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState;
import com.sphereon.libs.blockchain.commons.Digest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AlfrescoRepository {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AlfrescoRepository.class);

    private static final String EXCEPTION_MESSAGE_FETCH_CONTENT = "An error occurred whilst fetching content: ";
    private static final String EXCEPTION_MESSAGE_GET_NODE = "An error occurred whilst getting node %s: %s";

    private final SearchApi alfrescoSearchApi;
    private final ApiClient cmisApiClient;
    private final NodesApi alfrescoNodesApi;

    private String model;
    private String registrationStateProperty;

    public AlfrescoRepository(final SearchApi alfrescoSearchApi,
                              final ApiClient cmisApiClient,
                              final NodesApi alfrescoNodesApi,
                              @Value("${sphereon.blockchain.agent.alfresco.query.model}") final String model,
                              @Value("${sphereon.blockchain.agent.alfresco.query.registration-state:RegistrationState}") final String registrationStateProperty) {
        this.alfrescoSearchApi = alfrescoSearchApi;
        this.cmisApiClient = cmisApiClient;
        this.alfrescoNodesApi = alfrescoNodesApi;
        this.model = model;
        this.registrationStateProperty = registrationStateProperty;
    }

    public List<ResultSetRowEntry> selectAlfrescoNodes(final AlfrescoBlockchainRegistrationState state) throws ApiException {
        final var query = new RequestQuery();
        query.setLanguage(RequestQuery.LanguageEnum.AFTS);
        query.setQuery('{' + model + '}' + registrationStateProperty + ':' + state.getKey());

        final var include = new RequestInclude();
        include.add("properties");

        final var request = new SearchRequest();
        request.setQuery(query);
        request.include(include);

        final ResultSetPaging pagedResult = alfrescoSearchApi.search(request);
        return pagedResult.getList().getEntries();
    }

    public List<NodeEntry> selectAlfrescoNodes(final List<String> alfrescoNodeIds) {
        final List<NodeEntry> nodeEntries = new ArrayList<>();
        final List<String> include = new ArrayList<>();
        include.add("properties");

        alfrescoNodeIds
                .forEach(nodeId -> {
                    try {
                        nodeEntries.add(alfrescoNodesApi.getNode(nodeId, include, null, null));
                    } catch (ApiException e) {
                        String errorMessage = "http status: " + e.getCode() + "\r\n" + e.getResponseBody();
                        throw new RuntimeException(String.format(EXCEPTION_MESSAGE_GET_NODE, nodeId, errorMessage), e);
                    } catch (Exception exception) {
                        throw new RuntimeException(String.format(EXCEPTION_MESSAGE_GET_NODE, nodeId, exception.getMessage()), exception);
                    }
                });
        return nodeEntries;
    }

    public byte[] hashEntry(final String alfrescoNodeId, Digest.Algorithm hashAlgorithm) {
        final String[] localVarAuthNames = new String[]{"basicAuth"};
        try {
            var call = cmisApiClient.buildCall("/content", "GET", List.of(new Pair("id", alfrescoNodeId)), null,
                    new HashMap<>(), null, localVarAuthNames, null);
            var response = call.execute();
            if (response.code() == 200) {
                return Digest.getInstance().getHashAsHex(hashAlgorithm, response.body().byteStream());
            }
            throw new RuntimeException("Content request returned http code " + response.code());
        } catch (ApiException e) {
            throw new RuntimeException(EXCEPTION_MESSAGE_FETCH_CONTENT + e.getCode(), e);
        } catch (Exception exception) {
            throw new RuntimeException(EXCEPTION_MESSAGE_FETCH_CONTENT + exception.getMessage(), exception);
        }
    }

    public void updateAlfrescoNodeWith(String id, AlfrescoBlockchainRegistrationState state) {
        updateAlfrescoNodeWith(id, state, null, null, null);
    }

    public void updateAlfrescoNodeWith(final String alfrescoNodeId,
                                       final AlfrescoBlockchainRegistrationState blockchainRegistrationState,
                                       final OffsetDateTime blockchainRegistrationTime,
                                       final String singleProofChainChainId,
                                       final String perHashProofChainChainId) {
        final String now = TIME_FORMATTER.format(ZonedDateTime.now());
        final var properties = ImmutableMap.<String, String>builder()
                .put("bc:LastVerificationTime", now)
                .put("bc:RegistrationState", blockchainRegistrationState.getKey());

        if (blockchainRegistrationState == AlfrescoBlockchainRegistrationState.REGISTERED) {
            if (blockchainRegistrationTime != null) {
                properties.put("bc:RegistrationTime", TIME_FORMATTER.format(blockchainRegistrationTime));
            }
            if (singleProofChainChainId != null) {
                properties.put("bc:ExternalLinks", explorerLinkFrom(singleProofChainChainId));
            } else if (perHashProofChainChainId != null) {
                properties.put("bc:ExternalLinks", explorerLinkFrom(perHashProofChainChainId));
            }
        }
        updateAlfrescoNodeWith(alfrescoNodeId, properties.build());
    }

    private String explorerLinkFrom(final String chainId) {
        // TODO: Make configurable
        return "https://explorer.factoid.org/data?type=chain&key=" + chainId;
    }

    private void updateAlfrescoNodeWith(final String alfrescoNodeId, final Map<String, String> properties) {
        try {
            NodeBodyUpdate update = new NodeBodyUpdate();
            update.setProperties(properties);
            RequestInclude include = new RequestInclude();
            include.add("properties");
            logger.info("Updating node " + alfrescoNodeId);
            properties.forEach((key, value) -> logger.info("  - " + key + '=' + value));
            alfrescoNodesApi.updateNode(alfrescoNodeId, update, include, null);

            logger.info("Node " + alfrescoNodeId + " updated");
        } catch (ApiException exception) {
            throw new RuntimeException("An error occurred whilst updating state in node " + alfrescoNodeId, exception);
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred whilst updating state in node " + alfrescoNodeId, exception);
        }
    }

    public void updateCredentials(final String userName, final String password) {
        final HttpBasicAuth alfrescoSearchAuthentication = (HttpBasicAuth) alfrescoSearchApi
                .getApiClient()
                .getAuthentication("basicAuth");
        alfrescoSearchAuthentication.setUsername(userName);
        alfrescoSearchAuthentication.setPassword(password);

        final HttpBasicAuth alfrescoCmisAuthentication = (HttpBasicAuth) cmisApiClient.getAuthentication("basicAuth");
        alfrescoCmisAuthentication.setUsername(userName);
        alfrescoCmisAuthentication.setPassword(password);
    }
}
