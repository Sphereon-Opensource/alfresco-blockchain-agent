package com.sphereon.alfresco.blockchain.agent.factom;

import org.blockchain_innovation.factom.client.api.FactomResponse;
import org.blockchain_innovation.factom.client.api.FactomdClient;
import org.blockchain_innovation.factom.client.api.WalletdClient;
import org.blockchain_innovation.factom.client.api.errors.FactomRuntimeException;
import org.blockchain_innovation.factom.client.api.model.Address;
import org.blockchain_innovation.factom.client.api.model.Chain;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.blockchain_innovation.factom.client.api.model.response.factomd.ChainHeadResponse;
import org.blockchain_innovation.factom.client.api.model.response.factomd.CommitChainResponse;
import org.blockchain_innovation.factom.client.api.model.response.factomd.CommitEntryResponse;
import org.blockchain_innovation.factom.client.api.model.response.factomd.EntryBlockResponse;
import org.blockchain_innovation.factom.client.api.model.response.factomd.EntryResponse;
import org.blockchain_innovation.factom.client.api.model.response.factomd.RevealResponse;
import org.blockchain_innovation.factom.client.api.model.response.walletd.ComposeResponse;
import org.blockchain_innovation.factom.client.api.ops.Encoding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.Optional.empty;

@Component
public class FactomClient {
    private FactomdClient factomdClient;
    private WalletdClient walletdClient;
    private Address entryCreditsAddress;

    public FactomClient(final FactomdClient factomdClient,
                        final WalletdClient walletdClient,
                        @Qualifier("entryCreditsAddress") final Address entryCreditsAddress) {
        this.factomdClient = factomdClient;
        this.walletdClient = walletdClient;
        this.entryCreditsAddress = entryCreditsAddress;
    }

    public void postEntryToChain(final Entry entry) {
        final var postEntryToChain = composeEntry(entry)
                .thenCompose(composedEntry -> commitEntry(composedEntry)
                        .thenCompose(commitEntry -> revealEntry(composedEntry)));

        try {
            postEntryToChain.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon");
        }
    }

    public RevealResponse createChainFromEntry(Entry entry) {
        final var chain = new Chain();
        chain.setFirstEntry(entry);

        final CompletableFuture<RevealResponse> createChain = composeChain(chain)
                .thenCompose(composeResponse -> commitChain(composeResponse.getCommit())
                        .thenCompose(commitResult -> revealChain(composeResponse.getReveal())));

        try {
            return createChain.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon on creating chain");
        }
    }

    public Optional<Entry> verifyEntry(final String chainId, final byte[] contentHash) {
        final var entryVerification = getChainHead(chainId)
                .thenCompose(chainHeadResponse -> {
                    final var keyMerkleRoot = chainHeadResponse.getChainHead();
                    return getEntryFromMerkleRoot(keyMerkleRoot, contentHash);
                });

        try {
            return entryVerification.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon on creating chain");
        }
    }

    private CompletableFuture<ChainHeadResponse> getChainHead(String chainId) {
        return this.factomdClient.chainHead(chainId)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<ComposeResponse> composeEntry(final Entry entry) {
        return this.walletdClient.composeEntry(entry, this.entryCreditsAddress)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<CommitEntryResponse> commitEntry(ComposeResponse commitResult) {
        return this.factomdClient.commitEntry(commitResult.getCommit().getParams().getMessage())
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<RevealResponse> revealEntry(ComposeResponse commitResult) {
        return this.factomdClient.revealEntry(commitResult.getReveal().getParams().getEntry())
                .thenApply(this::validateFactomResponse);
    }

    private <T> T validateFactomResponse(FactomResponse<T> factomResponse) {
        if (factomResponse == null || 200 != factomResponse.getHTTPResponseCode()) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon");
        }
        return factomResponse.getResult();
    }

    private CompletableFuture<RevealResponse> revealChain(ComposeResponse.Reveal reveal) {
        final String chainId = reveal.getParams().getEntry();
        return this.factomdClient.revealChain(chainId)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<CommitChainResponse> commitChain(ComposeResponse.Commit commit) {
        final var commitMessage = commit.getParams().getMessage();
        return this.factomdClient.commitChain(commitMessage)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<ComposeResponse> composeChain(Chain chain) {
        return this.walletdClient.composeChain(chain, this.entryCreditsAddress)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<Optional<Entry>> getEntryFromMerkleRoot(final String merkleRoot, final byte[] contentHash) {
        if (merkleRoot.equals("0000000000000000000000000000000000000000000000000000000000000000")) {
            return CompletableFuture.completedFuture(empty());
        }

        return entryBlockByKeyMerkleRoot(merkleRoot)
                .thenCompose(entryBlockResponse -> findEntryInList(contentHash, entryBlockResponse.getEntryList())
                        .thenCompose(entry -> {
                            if (entry.isPresent()) {
                                return CompletableFuture.completedFuture(entry);
                            }

                            final var previousKeyMR = entryBlockResponse.getHeader().getPreviousKeyMR();
                            return getEntryFromMerkleRoot(previousKeyMR, contentHash);
                        }));
    }

    private CompletableFuture<EntryBlockResponse> entryBlockByKeyMerkleRoot(final String merkleRoot) {
        return this.factomdClient.entryBlockByKeyMerkleRoot(merkleRoot)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<Optional<Entry>> findEntryInList(final byte[] contentHash, final List<EntryBlockResponse.Entry> entryList) {
        final String contentInHex = Encoding.HEX.encode(contentHash);

        final var findEntries = new CompletableFuture<Optional<Entry>>();
        entryList.stream()
                .map(entry -> {
                    final var entryHash = entry.getEntryHash();
                    return getEntry(entryHash);
                })
                .map(CompletableFuture::join)
                .filter(entry -> entry.getContent().equals(contentInHex))
                .findFirst()
                .map(entry -> {
                    final var bifEntry = new Entry();
                    bifEntry.setChainId(entry.getChainId());
                    bifEntry.setContent(entry.getContent());
                    bifEntry.setExternalIds(entry.getExtIds());
                    return bifEntry;
                })
                .map(Optional::of)
                .ifPresentOrElse(findEntries::complete, () -> findEntries.complete(empty()));

        return findEntries;
    }

    private CompletableFuture<EntryResponse> getEntry(final String entryHash) {
        return this.factomdClient.entry(entryHash)
                .thenApply(this::validateFactomResponse);
    }
}
