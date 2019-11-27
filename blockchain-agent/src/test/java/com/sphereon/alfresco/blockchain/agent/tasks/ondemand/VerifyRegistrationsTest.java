package com.sphereon.alfresco.blockchain.agent.tasks.ondemand;

import com.alfresco.apis.model.Node;
import com.alfresco.apis.model.NodeEntry;
import com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyContentAlfrescoResponse;
import com.sphereon.alfresco.blockchain.agent.tasks.AlfrescoRepository;
import com.sphereon.alfresco.blockchain.agent.tasks.VerifyTask;
import com.sphereon.alfresco.blockchain.agent.utils.Hasher;
import com.sphereon.libs.blockchain.commons.Digest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.NOT_REGISTERED;
import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.PENDING_REGISTRATION;
import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.PENDING_VERIFICATION;
import static com.sphereon.alfresco.blockchain.agent.model.AlfrescoBlockchainRegistrationState.REGISTERED;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class VerifyRegistrationsTest {
    private VerifyRegistrations verification;
    private AlfrescoRepository alfrescoRepositoryMock;
    private VerifyTask verificationTask;
    private Clock dummyClock;
    private Digest.Algorithm hashAlgorithm;

    @Before
    public void setup() {
        this.dummyClock = Clock.fixed(Instant.ofEpochSecond(123456), ZoneOffset.ofHours(1));

        this.alfrescoRepositoryMock = mock(AlfrescoRepository.class);
        final ObjectFactory<AlfrescoRepository> factory = () -> alfrescoRepositoryMock;
        this.verificationTask = mock(VerifyTask.class);

        this.hashAlgorithm = Digest.Algorithm.SHA_256;
        this.verification = new VerifyRegistrations(factory, verificationTask, hashAlgorithm);
    }

    @Test
    public void shouldVerifyNodesWithCredentials() {
        final var nodeIds = asList("1", "2", "3", "4");
        final var credentials = "Bearer dummy-token";

    }

    @Test
    public void shouldVerifyNodes() {
        final var nodeIds = asList("1", "2", "3", "4");

        final var alfrescoNodes = asList(
                buildAlfrescoNode("1", PENDING_REGISTRATION),
                buildAlfrescoNode("2", PENDING_VERIFICATION),
                buildAlfrescoNode("3", NOT_REGISTERED),
                buildAlfrescoNode("4", REGISTERED)
        );

        when(alfrescoRepositoryMock.selectAlfrescoNodes(eq(nodeIds)))
                .thenReturn(alfrescoNodes);

        for (var id : asList("2", "3", "4")) {
            final byte[] content = ("dummy-content-" + id).getBytes();
            final byte[] contentHash = Hasher.hash(content, Digest.Algorithm.SHA_256);

            when(alfrescoRepositoryMock.getEntry(eq(id)))
                    .thenReturn(content);

            final var response = new VerifyContentAlfrescoResponse();
            response.setRegistrationState(REGISTERED);
            response.setRegistrationTime(OffsetDateTime.now(dummyClock));
            when(verificationTask.verifyHash(eq(contentHash)))
                    .thenReturn(response);
        }

        final var result = this.verification.execute(nodeIds, "");
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0).getRegistrationState()).isEqualTo(REGISTERED);
        assertThat(result.get(0).getRegistrationTime()).isEqualTo(OffsetDateTime.now(dummyClock));

        verify(alfrescoRepositoryMock).selectAlfrescoNodes(eq(nodeIds));
        verify(alfrescoRepositoryMock, times(3)).getEntry(anyString());

        verify(alfrescoRepositoryMock).updateAlfrescoNodeWith("2", REGISTERED, OffsetDateTime.now(dummyClock), null, null);
        verify(alfrescoRepositoryMock).updateAlfrescoNodeWith("3", REGISTERED, OffsetDateTime.now(dummyClock), null, null);
        verify(alfrescoRepositoryMock).updateAlfrescoNodeWith("4", REGISTERED, OffsetDateTime.now(dummyClock), null, null);

        verifyNoMoreInteractions(alfrescoRepositoryMock);

        verify(verificationTask, times(3)).verifyHash(any());
        verifyNoMoreInteractions(verificationTask);
    }

    @Test
    public void shouldReturnPartialResultsOnFailure() {
        final var nodeIds = asList("1", "2", "3", "4");

        final var alfrescoNodes = asList(
                buildAlfrescoNode("1", PENDING_REGISTRATION),
                buildAlfrescoNode("2", PENDING_VERIFICATION),
                buildAlfrescoNode("3", NOT_REGISTERED),
                buildAlfrescoNode("4", REGISTERED)
        );

        when(alfrescoRepositoryMock.selectAlfrescoNodes(eq(nodeIds)))
                .thenReturn(alfrescoNodes);

        for (var id : asList("2", "3")) {
            final byte[] content = ("dummy-content-" + id).getBytes();
            final byte[] contentHash = Hasher.hash(content, Digest.Algorithm.SHA_256);

            when(alfrescoRepositoryMock.getEntry(eq(id)))
                    .thenReturn(content);

            final var response = new VerifyContentAlfrescoResponse();
            response.setRegistrationState(REGISTERED);
            response.setRegistrationTime(OffsetDateTime.now(dummyClock));
            when(verificationTask.verifyHash(eq(contentHash)))
                    .thenReturn(response);
        }

        doThrow(new RuntimeException())
                .when(alfrescoRepositoryMock)
                .updateAlfrescoNodeWith(eq("3"), any(), any(), any(), any());

        final var result = this.verification.execute(nodeIds, "");
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getRegistrationState()).isEqualTo(REGISTERED);
        assertThat(result.get(0).getRegistrationTime()).isEqualTo(OffsetDateTime.now(dummyClock));

        verify(alfrescoRepositoryMock).selectAlfrescoNodes(eq(nodeIds));
        verify(alfrescoRepositoryMock, times(2)).getEntry(anyString());

        verify(alfrescoRepositoryMock).updateAlfrescoNodeWith("2", REGISTERED, OffsetDateTime.now(dummyClock), null, null);
        verify(alfrescoRepositoryMock).updateAlfrescoNodeWith("3", REGISTERED, OffsetDateTime.now(dummyClock), null, null);

        verifyNoMoreInteractions(alfrescoRepositoryMock);

        verify(verificationTask, times(2)).verifyHash(any());
        verifyNoMoreInteractions(verificationTask);
    }

    private NodeEntry buildAlfrescoNode(final String nodeId, final AlfrescoBlockchainRegistrationState registrationState) {
        final var node = new Node();
        node.setId(nodeId);
        node.setProperties(singletonMap("bc:RegistrationState", registrationState.getKey()));
        return new NodeEntry().entry(node);
    }
}
