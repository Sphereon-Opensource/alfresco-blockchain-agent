package com.sphereon.alfresco.blockchain.agent.factom;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public RevealResponse createChainFromEntry(Entry entry) {
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

        try {
            final var revealResult = this.factomdClient.revealChain(chainResult.getResult().getReveal().getParams().getEntry()).get();
            System.out.println(revealResult.getHTTPResponseCode());
            return revealResult.getResult();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
