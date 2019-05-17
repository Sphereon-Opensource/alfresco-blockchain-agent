package com.sphereon.alfresco.blockchain.agent.frontend.windows;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Created by Sander on 14-4-2016.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LoginWindow extends LoginWindowDesign {
    private final static Logger logger = Logger.getLogger(LoginWindow.class.getName());

    private Window loginWindow;

    private LoginResponse loginResponseCallback;


    public interface LoginResponse {
        void login(String userName, String password);

        void close();
    }


    public LoginWindow() {
        loginWindow = new Window();
        loginWindow.setContent(this);
        loginWindow.setClosable(true);
        loginWindow.setCaption("Please enter your user name and password");
        loginWindow.center();
        createButtonHandler();
    }


    public void show(LoginResponse loginResponseCallback) {
        this.loginResponseCallback = loginResponseCallback;
        textUserName.clear();
        textPassword.clear();
        textUserName.focus();

        if (loginWindow.getParent() != null) {
            close();
        }

        UI.getCurrent().addWindow(loginWindow);
        addDetachListener(detachEvent -> close());
    }


    public void close() { // FIXME please
        if (UI.getCurrent() != null) {
            try {
                UI.getCurrent().removeWindow(loginWindow);
            } catch (Throwable ignored) {
            }
        }
        try {
            if (loginWindow.getParent() != null) {
                ((UI) loginWindow.getParent()).removeWindow(loginWindow);
                loginWindow.setParent(null);
            }
        } catch (Throwable ignored) {
        }
        try {
            loginWindow.setParent(null);
        } catch (Throwable ignored) {
        }

    }


    private void createButtonHandler() {
        buttonSignIn.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        buttonSignIn.addClickListener(clickEvent -> {
            if (!textUserName.isEmpty() && !textPassword.isEmpty()) {
                try {
                    loginResponseCallback.login(textUserName.getValue(), textPassword.getValue());
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Login failed", e);
                    Notification.show("Login failed: " + e.getMessage());
                }
            }
        });

        loginWindow.addCloseListener((Window.CloseListener) closeEvent -> {
            if (loginWindow != null) {
                loginResponseCallback.close();
                UI.getCurrent().removeWindow(loginWindow);
            }
        });
    }

}
