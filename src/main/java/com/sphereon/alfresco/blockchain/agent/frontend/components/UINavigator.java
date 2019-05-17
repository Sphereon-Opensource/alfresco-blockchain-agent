package com.sphereon.alfresco.blockchain.agent.frontend.components;

import com.sphereon.alfresco.blockchain.agent.frontend.events.UIEvent;
import com.sphereon.alfresco.blockchain.agent.frontend.views.UIViewModule;
import com.sphereon.alfresco.blockchain.agent.frontend.views.ViewModule;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * Created by Sander on 21-8-2015.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UINavigator extends Navigator {


    private final SessionRegistry sessionRegistry;

    private final ViewModule fallbackModule;

    private ViewProvider fallbackViewProvider;


    @Autowired
    public UINavigator(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
        fallbackModule = ViewModule.WELCOME;
    }


    public void init(ComponentContainer container) {
        init(UI.getCurrent(), null, new ComponentContainerViewDisplay(container));
        initViewChangeListener();
        initViewProviders();
    }


    private void initViewProviders() {
        // A dedicated view provider is added for each separate view type
        for (final ViewModule viewModule : ViewModule.values()) {
            var module = viewModule.getModule();
            ViewProvider viewProvider = new StaticViewProvider(module.getModuleName(), module.getPresenter(UI.getCurrent())) {

                // This field caches an already initialized view instance if the
                // view should be cached (stateful views).
                private View cachedInstance;


                @Override
                public View getView(final String viewName) {
                    if (!ViewModule.isModuleAllowed(viewName, sessionRegistry.getSession())) {
                        return fallbackModule.getModule().getPresenter(UI.getCurrent());
                    }

                    View result = null;
                    if (module.getModuleName().equals(viewName)) {
                        if (module.isStateful()) {
                            // Stateful views get lazily instantiated
                            if (cachedInstance == null) {
                                cachedInstance = super.getView(module.getModuleName());
                            }
                            result = cachedInstance;
                        } else {
                            // Non-stateful views get instantiated every time they're navigated to
                            result = super.getView(module.getModuleName());
                        }
                    }
                    return result;
                }
            };
            if (module == fallbackModule.getModule()) {
                fallbackViewProvider = viewProvider;
            }
            addProvider(viewProvider);
        }

        setErrorProvider(new ViewProvider() {
            @Override
            public String getViewName(String s) {
                return fallbackModule.getModule().getModuleName();
            }


            @Override
            public View getView(String s) {
                return fallbackViewProvider.getView(fallbackModule.getModule().getModuleName());
            }
        });
    }


    private void initViewChangeListener() {
        addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(final ViewChangeEvent event) {
                // Since there's no conditions in switching between the views
                // we can always return true.
                return true;
            }


            @Override
            public void afterViewChange(final ViewChangeEvent event) {
                UIViewModule view = ViewModule.getModuleByName(event.getViewName());
                final var session = sessionRegistry.getSession();
                if (session != null) {
                    // Appropriate events get fired after the view is changed.
                    final var eventBus = session.getEventBus();
                    eventBus.post(new UIEvent.PostViewChangeEvent(view));
                    eventBus.post(new UIEvent.BrowserResizeEvent());
                    eventBus.post(new UIEvent.CloseOpenWindowsEvent());
                }
            }
        });
    }
}
