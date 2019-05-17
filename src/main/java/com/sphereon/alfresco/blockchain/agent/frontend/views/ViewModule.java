package com.sphereon.alfresco.blockchain.agent.frontend.views;

import com.sphereon.alfresco.blockchain.agent.frontend.components.SessionController;
import com.sphereon.alfresco.blockchain.agent.frontend.presenters.WelcomePresenter;
import com.vaadin.icons.VaadinIcons;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;

import java.util.*;

public enum ViewModule {

    WELCOME(new UIViewModule("welcome", VaadinIcons.HOME, false, true, false));


    private static final Map<String, UIViewModule> moduleMap = new HashMap<>();
    private final UIViewModule module;


    ViewModule(UIViewModule viewModule) {
        this.module = viewModule;
    }


    public UIViewModule getModule() {
        return module;
    }


    public static Collection<UIViewModule> getMenuModules(SessionController session) {
        final List<UIViewModule> allowedMenuModules = new ArrayList<>();
        for (var view : ViewModule.values()) {
            final var viewModule = view.getModule();
            if (viewModule.isMenuView()) {
                if (session.isSignedIn() || !viewModule.isSignedInOnly()) {
                    allowedMenuModules.add(viewModule);
                }
            }
        }
        return allowedMenuModules;
    }


    public static boolean isModuleAllowed(String viewName, SessionController session) {
        for (var view : ViewModule.values()) {
            final var viewModule = view.getModule();
            if (viewModule.getModuleName().equals(viewName)
                && (session.isSignedIn() || !viewModule.isSignedInOnly())) {
                return true;
            }
        }
        return false;
    }


    public static UIViewModule getModuleByName(String viewName) {
        for (var view : ViewModule.values()) {
            final var viewModule = view.getModule();
            if (viewModule.getModuleName().equals(viewName)) {
                return viewModule;
            }
        }
        return null;
    }


    @Component
    public static class ObjectFactoryInjector {
        public ObjectFactoryInjector(ObjectFactory<WelcomePresenter> welcomeViewObjectFactory) {
            for (ViewModule uiViewModule : ViewModule.values()) {
                switch (uiViewModule) {
                    case WELCOME:
                        uiViewModule.getModule().setPresenterFactory(welcomeViewObjectFactory);
                        break;
                }
                moduleMap.put(uiViewModule.getModule().getModuleName(), uiViewModule.getModule());
            }
        }
    }
}
