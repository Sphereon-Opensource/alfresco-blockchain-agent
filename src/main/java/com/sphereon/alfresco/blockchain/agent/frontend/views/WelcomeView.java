package com.sphereon.alfresco.blockchain.agent.frontend.views;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WelcomeView extends SphereonView {

    @Autowired
    public WelcomeView() {
        final var headerLabel = new Label("<h1>Welcome to the Sphereon Accounts Portal</h1>", ContentMode.HTML);
        addComponent(headerLabel);
        setComponentAlignment(headerLabel, Alignment.TOP_CENTER);
    }
}
