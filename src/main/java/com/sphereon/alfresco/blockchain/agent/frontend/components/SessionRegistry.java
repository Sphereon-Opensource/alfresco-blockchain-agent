package com.sphereon.alfresco.blockchain.agent.frontend.components;

import com.sphereon.commons.assertions.Assert;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SessionRegistry {

    private final Map<VaadinSession, SessionController> sessionMap = new HashMap<>();

    private final ObjectFactory<SessionController> sessionControllerObjectFactory;


    @Autowired
    public SessionRegistry(ObjectFactory<SessionController> sessionControllerObjectFactory) {
        this.sessionControllerObjectFactory = sessionControllerObjectFactory;
    }


    public SessionController getSession() {
        final UI current = UI.getCurrent();
        Assert.notNull(current, "The current thread is not in a UI scope");
        final var session = current.getSession();
        Assert.notNull(session, "The current thread does not have a valid session in thr current UI scope");

        var sessionController = sessionMap.get(session);
        if (sessionController == null) {
            sessionController = sessionControllerObjectFactory.getObject();
        }
        return sessionController;
    }


    void registerSession(SessionController sessionController) {
        sessionMap.put(UI.getCurrent().getSession(), sessionController);
    }

}
