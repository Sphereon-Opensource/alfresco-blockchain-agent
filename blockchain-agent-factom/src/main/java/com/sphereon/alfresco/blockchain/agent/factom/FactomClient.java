package com.sphereon.alfresco.blockchain.agent.factom;

import org.blockchain_innovation.factom.client.api.FactomResponse;
import org.blockchain_innovation.factom.client.api.FactomdClient;
import org.blockchain_innovation.factom.client.api.WalletdClient;
import org.blockchain_innovation.factom.client.api.errors.FactomRuntimeException;
import org.blockchain_innovation.factom.client.api.model.Address;
import org.blockchain_innovation.factom.client.api.model.Chain;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.blockchain_innovation.factom.client.api.model.response.factomd.CommitChainResponse;
import org.blockchain_innovation.factom.client.api.model.response.factomd.EntryBlockResponse;
import org.blockchain_innovation.factom.client.api.model.response.factomd.RevealResponse;
import org.blockchain_innovation.factom.client.api.model.response.walletd.ComposeResponse;
import org.blockchain_innovation.factom.client.api.ops.Encoding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

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
        final var composedEntry = composeEntry(entry);
        commitEntry(composedEntry.getResult());
        revealEntry(composedEntry.getResult());
    }

    private void revealEntry(ComposeResponse commitResult) {
        try {
            final var revealResponse = this.factomdClient.revealEntry(commitResult.getReveal().getParams().getEntry()).get();
            if (revealResponse == null || 200 != revealResponse.getHTTPResponseCode()) {
                throw new FactomRuntimeException("Error retrieving response from Factom daemon on reveal-entry");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon on reveal-entry");
        }
    }

    private void commitEntry(ComposeResponse commitResult) {
        try {
            final var commitEntryResponse = this.factomdClient.commitEntry(commitResult.getCommit().getParams().getMessage()).get();
            if (commitEntryResponse == null || 200 != commitEntryResponse.getHTTPResponseCode()) {
                throw new FactomRuntimeException("Error retrieving response from Factom daemon on commit-entry");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon on commit-entry");
        }
    }

    private FactomResponse<ComposeResponse> composeEntry(final Entry entry) {
        try {
            final var composeResponse = this.walletdClient.composeEntry(entry, this.entryCreditsAddress).get();
            if (composeResponse == null || 200 != composeResponse.getHTTPResponseCode()) {
                throw new FactomRuntimeException("Error retrieving response from Factom daemon on compose-entry");
            }
            return composeResponse;
        } catch (InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon on compose-entry");
        }
    }

    public RevealResponse createChainFromEntry(Entry entry) {
        final var chain = new Chain();
        chain.setFirstEntry(entry);

        final var chainResult = composeChain(chain);

        commitChain(chainResult);

        return revealChain(chainResult);
    }

    public Optional<Entry> verifyEntry(final String chainId, final byte[] contentHash) {
        try {
            final var chainHeadResponse = this.factomdClient.chainHead(chainId).get();
            if (chainHeadResponse == null || 200 != chainHeadResponse.getHTTPResponseCode()) {
                throw new FactomRuntimeException("Error retrieving response from Factom daemon on getting chain-head");
            }
            final var keyMerkleRoot = chainHeadResponse.getResult().getChainHead();
            return getEntryFromMerkleRoot(keyMerkleRoot, contentHash);
        } catch (InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon on getting chain-head");
        }
    }

    private RevealResponse revealChain(FactomResponse<ComposeResponse> chainResult) {
        try {
            final var revealResponse = this.factomdClient.revealChain(chainResult.getResult().getReveal().getParams().getEntry())
                    .get();
            if (revealResponse == null || 200 != revealResponse.getHTTPResponseCode()) {
                throw new FactomRuntimeException("Error communicating with Factom daemon on reveal-chain");
            }
            return revealResponse.getResult();
        } catch (InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error communicating with Factom daemon on reveal-chain");
        }
    }

    private FactomResponse<CommitChainResponse> commitChain(FactomResponse<ComposeResponse> chainResult) {
        try {
            final var commitMessage = chainResult.getResult().getCommit().getParams().getMessage();
            final FactomResponse<CommitChainResponse> commitResult = this.factomdClient.commitChain(commitMessage).get();
            if (commitResult == null || 200 != commitResult.getHTTPResponseCode()) {
                throw new FactomRuntimeException("Error communicating with Factom daemon on commit-chain");
            }
            return commitResult;
        } catch (ExecutionException | InterruptedException e) {
            throw new FactomRuntimeException("Error communicating with Factom daemon on commit-chain");
        }
    }

    private FactomResponse<ComposeResponse> composeChain(Chain chain) {
        try {
            final FactomResponse<ComposeResponse> composeResponse = this.walletdClient.composeChain(chain, this.entryCreditsAddress).get();
            if (composeResponse == null || 200 != composeResponse.getHTTPResponseCode()) {
                throw new FactomRuntimeException("Error communicating with Factom-wallet daemon on compose-chain");
            }
            return composeResponse;
        } catch (ExecutionException | InterruptedException e) {
            throw new FactomRuntimeException("Error communicating with Factom-wallet daemon on compose-chain");
        }
    }

    private Optional<Entry> getEntryFromMerkleRoot(final String merkleRoot, final byte[] contentHash) {
        if (merkleRoot.equals("0000000000000000000000000000000000000000000000000000000000000000")) {
            return Optional.empty();
        }

        final var entryBlockResponseFactomResponse = entryBlockByKeyMerkleRoot(merkleRoot);

        final var entryList = entryBlockResponseFactomResponse.getResult().getEntryList();
        final var entryInList = findEntryInList(contentHash, entryList);

        return entryInList
                .or(() -> {
                    final var previousKeyMR = entryBlockResponseFactomResponse.getResult().getHeader().getPreviousKeyMR();
                    return getEntryFromMerkleRoot(previousKeyMR, contentHash);
                });
    }

    private FactomResponse<EntryBlockResponse> entryBlockByKeyMerkleRoot(final String merkleRoot) {
        final FactomResponse<EntryBlockResponse> entryBlockResponseFactomResponse;
        try {
            entryBlockResponseFactomResponse = this.factomdClient.entryBlockByKeyMerkleRoot(merkleRoot).get();

            if (entryBlockResponseFactomResponse == null || 200 != entryBlockResponseFactomResponse.getHTTPResponseCode()) {
                throw new FactomRuntimeException("Error retrieving response from Factom daemon on getting entry-block");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new FactomRuntimeException("Error retrieving response from Factom daemon on getting entry-block");
        }
        return entryBlockResponseFactomResponse;
    }

    private Optional<Entry> findEntryInList(final byte[] contentHash, final List<EntryBlockResponse.Entry> entryList) {
        final String contentInHex = Encoding.HEX.encode(contentHash);
        return entryList.stream()
                .map(entry -> {
                    final var entryHash = entry.getEntryHash();
                    try {
                        final var entryResponseFactomResponse = this.factomdClient.entry(entryHash).get();
                        return entryResponseFactomResponse.getResult();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new IllegalArgumentException();
                    }
                })
                .filter(entry -> entry.getContent().equals(contentInHex))
                .map(entry -> {
                    final var bifEntry = new Entry();
                    bifEntry.setChainId(entry.getChainId());
                    bifEntry.setContent(entry.getContent());
                    bifEntry.setExternalIds(entry.getExtIds());
                    return bifEntry;
                })
                .findFirst();
    }
}
