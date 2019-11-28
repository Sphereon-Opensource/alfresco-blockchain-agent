package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.factom.config.ExternalIds;
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
import org.blockchain_innovation.factom.client.impl.OfflineWalletdClientImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static java.util.Optional.empty;

@Component
public class FactomClient {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FactomClient.class);
    public static final String NO_PREVIOUS_MERKLE_ROOT = "0000000000000000000000000000000000000000000000000000000000000000";

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
                        .handle(this::handleCommitEntry)
                        .thenCompose(commitEntry -> revealEntry(composedEntry)));

        try {
            postEntryToChain.get();
        } catch (CompletionException | InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon", e);
        }
    }

    private <T> T handleCommitEntry(final T commitResponse, final Throwable throwable) {
        if (throwable != null) {
            logger.error("Commit entry failed. Will try reveal next.", throwable);
        }
        return commitResponse;
    }

    public RevealResponse createChainFromEntry(final Entry entry) {
        final var chain = new Chain();
        chain.setFirstEntry(entry);

        final CompletableFuture<RevealResponse> createChain = composeChain(chain)
                .thenCompose(composeResponse -> commitChain(composeResponse.getCommit())
                        .handle(this::handleCommitEntry)
                        .thenCompose(commitResult -> revealChain(composeResponse.getReveal())));

        try {
            return createChain.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon on creating chain", e);
        }
    }

    public Optional<Entry> verifyEntry(final String chainId, final byte[] contentHash) {
        final var entryVerification = getChainHead(chainId)
                .thenCompose(chainHeadResponse -> {
                    final var keyMerkleRoot = chainHeadResponse.getChainHead();
                    return findEntryStartingAtMerkleRoot(keyMerkleRoot, contentHash);
                });

        try {
            return entryVerification.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon on verifying entry", e);
        }
    }

    private CompletableFuture<ChainHeadResponse> getChainHead(final String chainId) {
        return this.factomdClient.chainHead(chainId)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<ComposeResponse> composeEntry(final Entry entry) {
        if (this.walletdClient instanceof OfflineWalletdClientImpl) {
            return this.walletdClient.composeEntry(entry, this.entryCreditsAddress)
                    .thenApply(FactomResponse::getResult);
        }

        return this.walletdClient.composeEntry(entry, this.entryCreditsAddress)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<CommitEntryResponse> commitEntry(final ComposeResponse composeResult) {
        return this.factomdClient.commitEntry(composeResult.getCommit().getParams().getMessage())
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<RevealResponse> revealEntry(final ComposeResponse composeResult) {
        return this.factomdClient.revealEntry(composeResult.getReveal().getParams().getEntry())
                .thenApply(this::validateFactomResponse);
    }

    private <T> T validateFactomResponse(final FactomResponse<T> factomResponse) {
        if (factomResponse == null) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon");
        }
        if (200 != factomResponse.getHTTPResponseCode()) {
            final var message = String.format("Error retrieving response from Factom daemon. Responsecode: %s", factomResponse.getHTTPResponseCode());
            throw new FactomRuntimeException(message);
        }
        return factomResponse.getResult();
    }

    private CompletableFuture<RevealResponse> revealChain(final ComposeResponse.Reveal reveal) {
        final String chainId = reveal.getParams().getEntry();
        return this.factomdClient.revealChain(chainId)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<CommitChainResponse> commitChain(final ComposeResponse.Commit commit) {
        final var commitMessage = commit.getParams().getMessage();
        return this.factomdClient.commitChain(commitMessage)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<ComposeResponse> composeChain(final Chain chain) {
        return this.walletdClient.composeChain(chain, this.entryCreditsAddress)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<Optional<Entry>> findEntryStartingAtMerkleRoot(final String merkleRoot, final byte[] contentHash) {
        if (merkleRoot.equals(NO_PREVIOUS_MERKLE_ROOT)) {
            return CompletableFuture.completedFuture(empty());
        }

        return entryBlockByKeyMerkleRoot(merkleRoot)
                .thenCompose(entryBlockResponse -> findEntryInList(contentHash, entryBlockResponse.getEntryList())
                        .thenCompose(entry -> {
                            if (entry.isPresent()) {
                                return CompletableFuture.completedFuture(entry);
                            }

                            final var previousKeyMR = entryBlockResponse.getHeader().getPreviousKeyMR();
                            return findEntryStartingAtMerkleRoot(previousKeyMR, contentHash);
                        }));
    }

    private CompletableFuture<EntryBlockResponse> entryBlockByKeyMerkleRoot(final String merkleRoot) {
        return this.factomdClient.entryBlockByKeyMerkleRoot(merkleRoot)
                .thenApply(this::validateFactomResponse);
    }

    private CompletableFuture<Optional<Entry>> findEntryInList(final byte[] contentHash, final List<EntryBlockResponse.Entry> entryList) {
        final String contentHashInHex = Encoding.HEX.encode(contentHash);

        final var findEntries = new CompletableFuture<Optional<Entry>>();
        entryList.stream()
                .map(entry -> {
                    final var entryHash = entry.getEntryHash();
                    return getEntry(entryHash);
                })
                .map(CompletableFuture::join)
                .filter(entry -> entryMatches(entry, contentHashInHex))
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

    private boolean entryMatches(final EntryResponse entry, final String contentHashInHex) {
        final List<String> extIds = entry.getExtIds();
        if (extIds == null || extIds.size() < 2) {
            return false;
        }

        if (!hashMatches(contentHashInHex, extIds)) {
            return false;
        }

        return isSignatureValid(entry.getContent(), contentHashInHex);
    }

    private boolean isSignatureValid(final String content, final String contentHashInHex) {
        // TODO: Verify signature
        return true;
    }

    private boolean hashMatches(String contentHashInHex, List<String> extIds) {
        final var firstExtIdPairKey = Encoding.HEX.encode(ExternalIds.HASH.getBytes());
        return extIds.get(0).equals(firstExtIdPairKey) && extIds.get(1).equals(contentHashInHex);
    }

    private CompletableFuture<EntryResponse> getEntry(final String entryHash) {
        return this.factomdClient.entry(entryHash)
                .thenApply(this::validateFactomResponse);
    }
}
