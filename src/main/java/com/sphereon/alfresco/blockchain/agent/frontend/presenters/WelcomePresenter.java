package com.sphereon.alfresco.blockchain.agent.frontend.presenters;

import com.sphereon.alfresco.blockchain.agent.frontend.components.SessionRegistry;
import com.sphereon.alfresco.blockchain.agent.frontend.views.WelcomeView;
import com.vaadin.navigator.ViewChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WelcomePresenter extends AbstractPresenter<WelcomeView> {

    @Autowired
    private WelcomeView welcomeView;


    @Autowired
    public WelcomePresenter(SessionRegistry sessionRegistry) {
        super(sessionRegistry);
    }


    @Override
    protected void init() {
        welcomeView.setPresenter(this);
    }


    @Override
    public WelcomeView getView() {
        return welcomeView;
    }


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }
}