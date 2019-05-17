package com.sphereon.alfresco.blockchain.agent.backend.jobs;

import com.sphereon.alfresco.blockchain.agent.backend.tasks.ondemand.CheckBlockchainConfigTask;
import com.sphereon.alfresco.blockchain.agent.backend.tasks.ondemand.CheckCertificateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class Bootstrap implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private CheckBlockchainConfigTask checkBlockchainConfigTask;

    @Autowired
    private CheckCertificateTask checkCertificateTask;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent applicationReadyEvent) {
        checkBlockchainConfigTask.execute();
    }
}
