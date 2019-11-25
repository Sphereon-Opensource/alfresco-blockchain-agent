package com.sphereon.alfresco.blockchain.agent.factom;

import com.sphereon.alfresco.blockchain.agent.tasks.scheduled.SignNewDocumentsTask;
import org.blockchain_innovation.factom.client.api.FactomdClient;
import org.blockchain_innovation.factom.client.api.WalletdClient;
import org.blockchain_innovation.factom.client.api.model.Address;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;

@Component
public class FactomSignNewDocumentTask implements SignNewDocumentsTask {
    private FactomdClient factomdClient;
    private WalletdClient walletdClient;
    private Address entryCreditsAddress;

    public FactomSignNewDocumentTask(final FactomdClient factomdClient,
                                     final WalletdClient walletdClient,
                                     @Qualifier("entryCreditsAddress") final Address entryCreditsAddress) {
        this.factomdClient = factomdClient;
        this.walletdClient = walletdClient;
        this.entryCreditsAddress = entryCreditsAddress;
    }

    @Override
    public void registerEntry(byte[] contentHash) {
        final var entry = new Entry();
        entry.setContent(new String(contentHash));
        entry.setExternalIds(asList("a", "b", "c"));

        final var composedEntry = this.walletdClient.composeEntry(entry, this.entryCreditsAddress).join();
        final var result = composedEntry.getResult();

        this.factomdClient.commitEntry(result.getCommit().getJsonRpc());
        this.factomdClient.revealEntry(result.getReveal().getJsonRpc());
    }
}
