package com.sphereon.alfresco.blockchain.agent.frontend;

import com.sphereon.alfresco.blockchain.agent.frontend.views.MainView;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@SpringUI(path = "/")
@Theme("mytheme")
@Push(transport = Transport.LONG_POLLING)
public class AlfrescoBlockchainUI extends UI {
    @Autowired
    private MainView mainView;

    @Override
    protected void init(VaadinRequest vaadinRequest) {

        Responsive.makeResponsive(this);
        addStyleName(ValoTheme.UI_WITH_MENU);
        mainView.init();
        setContent(mainView);
    }

    @WebServlet(urlPatterns = "/*", name = "AlfrescoBlockchainPortal", asyncSupported = true)
    @VaadinServletConfiguration(ui = AlfrescoBlockchainUI.class, productionMode = false, closeIdleSessions = true)
    public static class KeyGensUIServlet extends VaadinServlet {
    }
}
