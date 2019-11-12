package com.sphereon.alfresco.blockchain.agent.sphereon.proof;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class BlockchainProofCheck implements ApplicationListener<ApplicationReadyEvent> {
    private final CheckBlockchainProofConfigTask checkBlockchainConfigTask;

    public BlockchainProofCheck(final CheckBlockchainProofConfigTask checkBlockchainConfigTask) {
        this.checkBlockchainConfigTask = checkBlockchainConfigTask;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent applicationReadyEvent) {
        checkBlockchainConfigTask.execute();
    }
}
