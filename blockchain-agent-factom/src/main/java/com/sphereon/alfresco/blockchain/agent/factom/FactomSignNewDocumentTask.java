package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.tasks.scheduled.SignNewDocumentsTask;
import com.sphereon.libs.blockchain.commons.Digest;
import org.blockchain_innovation.factom.client.api.FactomResponse;
import org.blockchain_innovation.factom.client.api.FactomdClient;
import org.blockchain_innovation.factom.client.api.WalletdClient;
import org.blockchain_innovation.factom.client.api.errors.FactomException;
import org.blockchain_innovation.factom.client.api.model.Address;
import org.blockchain_innovation.factom.client.api.model.Chain;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.blockchain_innovation.factom.client.api.model.response.factomd.CommitChainResponse;
import org.blockchain_innovation.factom.client.api.model.response.factomd.RevealResponse;
import org.blockchain_innovation.factom.client.api.model.response.walletd.ComposeResponse;
import org.blockchain_innovation.factom.client.api.ops.Encoding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;

@Component
public class FactomSignNewDocumentTask implements SignNewDocumentsTask {
    public static final String EXTERNAL_ID_HASH_KEY = "Hash";
    private FactomdClient factomdClient;
    private WalletdClient walletdClient;
    private Address entryCreditsAddress;
    private String chainId;

    public FactomSignNewDocumentTask(final FactomdClient factomdClient,
                                     final WalletdClient walletdClient,
                                     @Qualifier("entryCreditsAddress") final Address entryCreditsAddress,
                                     @Value("${sphereon.blockchain.agent.factom.chain-id}") final String chainId) {
        this.factomdClient = factomdClient;
        this.walletdClient = walletdClient;
        this.entryCreditsAddress = entryCreditsAddress;
        this.chainId = chainId;
    }

    @Override
    public void registerEntry(byte[] contentHash, Digest.Algorithm algorithm) {
        final var entry = new Entry();
        entry.setContent(Encoding.UTF_8.encode(contentHash));
        entry.setExternalIds(getExternalIds(algorithm));

//        createChainFromEntry(entry);
        postEntryToChain(entry, chainId);
    }

    private void postEntryToChain(final Entry entry, final String chainId) {
        entry.setChainId(chainId);
        final FactomResponse<ComposeResponse> composedEntry;
        try {
            composedEntry = this.walletdClient.composeEntry(entry, this.entryCreditsAddress).get();
        } catch (InterruptedException | ExecutionException e) {
            final FactomException.RpcErrorException cause = (FactomException.RpcErrorException) e.getCause();
            System.out.println(cause.getFactomResponse().getRpcErrorResponse().toString());
            throw new RuntimeException(e);
        }
        final var commitResult = composedEntry.getResult();

        try {
            this.factomdClient.commitEntry(commitResult.getCommit().getParams().getMessage()).get();
        } catch (InterruptedException | ExecutionException e) {
            final FactomException.RpcErrorException cause = (FactomException.RpcErrorException) e.getCause();
            System.out.println(cause.getFactomResponse().getRpcErrorResponse().toString());
        }

        try {
            this.factomdClient.revealEntry(commitResult.getReveal().getParams().getEntry()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void createChainFromEntry(Entry entry) {
        final var chain = new Chain();
        chain.setFirstEntry(entry);
        final FactomResponse<ComposeResponse> chainResult;
        try {
            chainResult = this.walletdClient.composeChain(chain, this.entryCreditsAddress).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            final var commitMessage = chainResult.getResult().getCommit().getParams().getMessage();
            final FactomResponse<CommitChainResponse> commitResult = this.factomdClient.commitChain(commitMessage).get();
            System.out.println(commitResult.getHTTPResponseCode());
        } catch (ExecutionException | InterruptedException e) {
            final FactomException.RpcErrorException cause = (FactomException.RpcErrorException) e.getCause();
            System.out.println(cause.getFactomResponse().getRpcErrorResponse().toString());
        }

        final FactomResponse<RevealResponse> revealResult;
        try {
            revealResult = this.factomdClient.revealChain(chainResult.getResult().getReveal().getParams().getEntry()).get();
            System.out.println(revealResult.getHTTPResponseCode());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private List<String> getExternalIds(Digest.Algorithm algorithm) {
        return asList(EXTERNAL_ID_HASH_KEY, algorithm.getImplementation());
    }
}
