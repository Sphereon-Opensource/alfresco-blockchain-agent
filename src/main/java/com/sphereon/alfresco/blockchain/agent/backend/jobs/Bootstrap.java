package com.sphereon.alfresco.blockchain.agent.backend.jobs;

import com.sphereon.alfresco.blockchain.agent.backend.tasks.ondemand.CheckBlockchainConfigTask;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.ondemand.CheckCertificateTask;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class Bootstrap implements ApplicationListener<ApplicationReadyEvent> {
    private final CheckBlockchainConfigTask checkBlockchainConfigTask;
    private final CheckCertificateTask checkCertificateTask;

    public Bootstrap(CheckBlockchainConfigTask checkBlockchainConfigTask, CheckCertificateTask checkCertificateTask) {
        this.checkBlockchainConfigTask = checkBlockchainConfigTask;
        this.checkCertificateTask = checkCertificateTask;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent applicationReadyEvent) {
        checkBlockchainConfigTask.execute();
    }
}
