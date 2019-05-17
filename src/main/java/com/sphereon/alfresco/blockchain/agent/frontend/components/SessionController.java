package com.sphereon.alfresco.blockchain.agent.frontend.components;

import com.alfresco.apis.api.AuthenticationApi;
import com.alfresco.apis.handler.ApiException;
import com.alfresco.apis.model.TicketBody;
import com.alfresco.apis.model.TicketEntry;
import com.google.common.eventbus.Subscribe;
import com.sphereon.alfresco.blockchain.agent.frontend.events.SessionEventBus;
import com.sphereon.alfresco.blockchain.agent.frontend.events.UIEvent;
import com.sphereon.alfresco.blockchain.agent.frontend.windows.LoginWindow;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.logging.Logger;

/**
 * @author Created by Sander
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SessionController {
    private static final Logger logger = Logger.getLogger(SessionController.class.getName());

    public static final String TICKET_KEY = "alfTicket";
    
    public static final String SESSION_ACCOUNT_KEY = "alfSessionAccount";

    private final SessionRegistry sessionMapper;

    private final LoginWindow loginWindow;

    private final SessionEventBus eventBus;

    private AuthenticationApi authenticationApi;


    @Autowired
    public SessionController(SessionRegistry sessionMapper, LoginWindow loginWindow, SessionEventBus eventBus, AuthenticationApi authenticationApi) {
        this.sessionMapper = sessionMapper;
        this.loginWindow = loginWindow;
        this.eventBus = eventBus;
        this.authenticationApi = authenticationApi;
        this.eventBus.register(this);
        this.sessionMapper.registerSession(this);
    }


    public SessionEventBus getEventBus() {
        return eventBus;
    }


    @Subscribe
    public void userSignInRequestEvent(final UIEvent.UserSignInRequestEvent event) {
        loginWindow.show(new LoginWindow.LoginResponse() {
            @Override
            public void login(String userName, String password) {
                try {
                    if (signIn(userName, password)) {
                        loginWindow.close();
                    } else {
                        Notification.show("Invalid user name or password.", Notification.Type.ERROR_MESSAGE);
                    }
                } catch (Throwable throwable) {
                    Notification.show("Login failed: " + throwable, Notification.Type.ERROR_MESSAGE);
                }
            }


            @Override
            public void close() {

            }
        });
    }


    @Subscribe
    public void signOut(final UIEvent.UserSignOutRequestEvent event) {
        SessionController userSession = sessionMapper.getSession();
        setAttribute(SESSION_ACCOUNT_KEY, null);
        getEventBus().post(new UIEvent.SessionStateChangedEvent(false));
    }


    @PreDestroy
    private void cleanup() {
        eventBus.unregister(this);
    }


    public boolean isSignedIn() {
        return getAttribute(SESSION_ACCOUNT_KEY) != null;
    }


    public void setAttribute(String key, Object value) {
        UI.getCurrent().getSession().setAttribute(key, value);
    }


    public Object getAttribute(String key) {
        final VaadinSession session = UI.getCurrent().getSession();
        return session.getAttribute(key);
    }


    public String getAttributeString(String key) {
        Object o = getAttribute(key);
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            return (String) o;
        } else {
            return o.toString();
        }
    }


    public Integer getAttributeInteger(String key) {
        Object o = getAttribute(key);
        if (o == null) {
            return null;
        } else if (o instanceof Integer) {
            return (Integer) o;
        } else {
            return Integer.parseInt(o.toString());
        }
    }


    public Boolean getAttributeBoolean(String key) {
        Object o = getAttribute(key);
        if (o == null) {
            return null;
        } else if (o instanceof Boolean) {
            return (Boolean) o;
        } else {
            return Boolean.parseBoolean(o.toString());
        }
    }


    public boolean signIn(String userId, String password) {

        try {
            TicketBody ticketBody = new TicketBody();
            ticketBody.setUserId(userId);
            ticketBody.setPassword(password);
            TicketEntry ticketEntry = authenticationApi.createTicket(ticketBody);
            setAttribute(TICKET_KEY, ticketEntry.getEntry());
        } catch (ApiException e) {
            throw new RuntimeException(e.getResponseBody(), e);
        }
        return true;
    }


/*
    public UserAccount getUserAccount() {
        return (UserAccount) getAttribute(SESSION_ACCOUNT_KEY);
    }
*/

}
