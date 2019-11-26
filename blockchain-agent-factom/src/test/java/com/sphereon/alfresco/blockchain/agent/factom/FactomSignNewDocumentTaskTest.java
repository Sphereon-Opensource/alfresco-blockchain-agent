package com.sphereon.alfresco.blockchain.agent.factom;

import org.blockchain_innovation.factom.client.api.FactomResponse;
import org.blockchain_innovation.factom.client.api.FactomdClient;
import org.blockchain_innovation.factom.client.api.WalletdClient;
import org.blockchain_innovation.factom.client.api.model.Address;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.blockchain_innovation.factom.client.api.model.response.walletd.ComposeResponse;
import org.blockchain_innovation.factom.client.impl.FactomResponseImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class FactomSignNewDocumentTaskTest {
    @MockBean
    private FactomdClient factomdClient;

    @MockBean
    private WalletdClient walletdClient;

    @Autowired
    private FactomSignNewDocumentTask factomSignNewDocumentTask;

    private String dummyHash;
    private String dummyChainId;
    private String ecAddress;

    @Before
    public void setup() {
        this.dummyChainId = "fe07dab8c2917366f376b454736ac07865626074691b30ffddddda4ff02a9451";
        this.dummyHash = "Dummy-hash";
        this.ecAddress = "EC2uddT5TUToHGU34tp7fdhZagGwH5w2fFnpQ3GNNfUjeb7X18kF";
    }

    @Test
    public void testRegisterEntry() throws ExecutionException, InterruptedException {
        /* Mock composeEntry. */
        final CompletableFuture<FactomResponse<ComposeResponse>> composeResponseFuture = mock(CompletableFuture.class);
        final FactomResponseImpl<ComposeResponse> composeFactomResponse = mock(FactomResponseImpl.class);

        final ArgumentMatcher<Entry> composeEntryMatcher = (entry) ->
                entry.getChainId().equals(dummyChainId)
                        && entry.getContent().equals(dummyHash)
                        && entry.getExternalIds().equals(asList("Hash", dummyHash));
        final ArgumentMatcher<Address> ecAddressMatcher = (address) -> address.getValue().equals(this.ecAddress);

        when(walletdClient.composeEntry(argThat(composeEntryMatcher), argThat(ecAddressMatcher)))
                .thenReturn(composeResponseFuture);
        when(composeResponseFuture.get())
                .thenReturn(composeFactomResponse);

        /* Mock revealEntry. */
        final var reveal = mock(ComposeResponse.Reveal.class);
        when(reveal.getParams()).thenReturn(new ComposeResponse.Reveal.Params(dummyChainId));

        /* Mock commitEntry. */
        final var commitMessage = "Dummy-commit-message";
        final var commit = mock(ComposeResponse.Commit.class);
        when(commit.getParams()).thenReturn(new ComposeResponse.Commit.Params(commitMessage));

        /* Mock ComposeResponse. */
        final var composeResult = new ComposeResponse(commit, reveal);
        when(composeFactomResponse.getResult()).thenReturn(composeResult);

        /* Mock calls to FactomD. */
        when(factomdClient.commitEntry(eq(commitMessage)))
                .thenReturn(mock(CompletableFuture.class));
        when(factomdClient.revealEntry(eq(dummyChainId)))
                .thenReturn(mock(CompletableFuture.class));

        /* Act. */
        this.factomSignNewDocumentTask.registerHash(dummyHash.getBytes());

        /* Verify. */
        verify(walletdClient, times(1)).composeEntry(argThat(composeEntryMatcher), argThat(ecAddressMatcher));
        verify(factomdClient, times(1)).commitEntry(eq(commitMessage));
        verify(factomdClient, times(1)).revealEntry(eq(dummyChainId));
    }
}
