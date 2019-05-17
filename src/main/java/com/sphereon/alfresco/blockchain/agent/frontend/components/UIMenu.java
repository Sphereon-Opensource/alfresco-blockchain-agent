package com.sphereon.alfresco.blockchain.agent.frontend.components;

import com.google.common.eventbus.Subscribe;
import com.sphereon.alfresco.blockchain.agent.frontend.events.SessionEventBus;
import com.sphereon.alfresco.blockchain.agent.frontend.events.UIEvent;
import com.sphereon.alfresco.blockchain.agent.frontend.views.UIViewModule;
import com.sphereon.alfresco.blockchain.agent.frontend.views.ViewModule;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PreDestroy;

/**
 * Created by Sander
 */
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UIMenu extends CustomComponent {
    public static final String ID = "keygen-menu";
    public static final String REPORTS_BADGE_ID = "keygen-menu-reports-badge";
    public static final String NOTIFICATIONS_BADGE_ID = "keygen-menu-notifications-badge";
    private static final String STYLE_VISIBLE = "valo-menu-visible";


    private final SessionRegistry sessionRegistry;

    private SessionEventBus eventBus;

    private Label notificationsBadge;
    private Label reportsBadge;
    private MenuBar.MenuItem settingsItem;
    private CssLayout menuItemsLayout;
    private MenuBar userMenu;

    private String lastViewName;


    @Autowired
    public UIMenu(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
        setPrimaryStyleName("valo-menu");
        setId(ID);
        setSizeUndefined();
    }


    public void init() {

        // There's only one DashboardMenu per UI so this doesn't need to be
        // unregistered from the UI-scoped DashboardEventBus.
        setCompositionRoot(buildContent());
    }


    private Component buildContent() {
        final CssLayout menuContent = new CssLayout();
        menuContent.addStyleName("sidebar");
        menuContent.addStyleName(ValoTheme.MENU_PART);
        menuContent.addStyleName("no-vertical-drag-hints");
        menuContent.addStyleName("no-horizontal-drag-hints");
        menuContent.setWidth(null);
        menuContent.setHeight("100%");

        menuContent.addComponent(buildTitle());
        menuContent.addComponent(buildUserMenu());
        menuContent.addComponent(buildToggleButton());
        menuContent.addComponent(buildMenuItems());

        return menuContent;
    }


    private Component buildTitle() {
        Label logo = new Label("Sphereon.com <strong>Accounts portal</strong>", ContentMode.HTML);
        logo.setSizeUndefined();
        HorizontalLayout logoWrapper = new HorizontalLayout(logo);
        logoWrapper.setComponentAlignment(logo, Alignment.MIDDLE_CENTER);
        logoWrapper.addStyleName("valo-menu-title");
        return logoWrapper;
    }


    private Component buildUserMenu() {
        if (userMenu == null) {
            userMenu = new MenuBar();
            userMenu.addStyleName("user-menu");
            settingsItem = userMenu.addItem("", new ThemeResource("img/profile4.png"), null);
            settingsItem.addItem("Sign in", (Command) selectedItem -> triggerSignIn());
            settingsItem.addSeparator();
            settingsItem.addItem("Sign out", (Command) selectedItem -> eventBus.post(new UIEvent.UserSignOutRequestEvent()));
        }

/* TODO
        UserAccount sessionAccount = (UserAccount) sessionRegistry.getSession().getAttribute("sessionAccount");
        if (sessionAccount != null) {
            userMenu.setCaption(sessionAccount.getUserName());
        } else {
*/
            userMenu.setCaption("Not signed in");
            triggerSignIn();
//        }
        return userMenu;
    }


    private void triggerSignIn() {
        eventBus = sessionRegistry.getSession().getEventBus();
        eventBus.register(this);
        eventBus.post(new UIEvent.UserSignInRequestEvent());
    }


    private Component buildToggleButton() {
        Button valoMenuToggleButton = new Button("Menu", (ClickListener) event ->
        {
            if (getCompositionRoot().getStyleName().contains(STYLE_VISIBLE)) {
                getCompositionRoot().removeStyleName(STYLE_VISIBLE);
            } else {
                getCompositionRoot().addStyleName(STYLE_VISIBLE);
            }
        });
        valoMenuToggleButton.setIcon(VaadinIcons.LIST);
        valoMenuToggleButton.addStyleName("valo-menu-toggle");
        valoMenuToggleButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        valoMenuToggleButton.addStyleName(ValoTheme.BUTTON_SMALL);
        return valoMenuToggleButton;
    }


    private Component buildMenuItems() {
        if (menuItemsLayout == null) {
            menuItemsLayout = new CssLayout();
        } else {
            menuItemsLayout.removeAllComponents();
        }
        menuItemsLayout.addStyleName("valo-menuitems");

        final var menuModules = ViewModule.getMenuModules(sessionRegistry.getSession());
        for (final UIViewModule viewModule : menuModules) {
            Component menuItemComponent = new ValoMenuItemButton(viewModule);
            if (viewModule.getTitle() != null) {
                menuItemComponent.setCaption(viewModule.getTitle());
            }
            if (eventBus != null) {
                eventBus.register(menuItemComponent);
            }
            menuItemsLayout.addComponent(menuItemComponent);
        }

        return menuItemsLayout;

    }


    private Component buildBadgeWrapper(final Component menuItemButton,
                                        final Component badgeLabel) {
        CssLayout dashboardWrapper = new CssLayout(menuItemButton);
        dashboardWrapper.addStyleName("badgewrapper");
        dashboardWrapper.addStyleName(ValoTheme.MENU_ITEM);
        badgeLabel.addStyleName(ValoTheme.MENU_BADGE);
        badgeLabel.setWidthUndefined();
        badgeLabel.setVisible(false);
        dashboardWrapper.addComponent(badgeLabel);
        return dashboardWrapper;
    }


    @Override
    public void attach() {
        super.attach();
    }


    @Subscribe
    public void sessionStateChangedEvent(final UIEvent.SessionStateChangedEvent event) {
        // Rebuild menu items after sign in or sign out
        buildUserMenu();
        buildMenuItems();
        if (!event.isLoggedIn()) {
            lastViewName = null;
        }
        activateView(lastViewName);
    }


    @Subscribe
    public void postViewChange(final UIEvent.PostViewChangeEvent event) {
        // After a successful view change the menu can be hidden in mobile view.
        getCompositionRoot().removeStyleName(STYLE_VISIBLE);
    }


    public final class ValoMenuItemButton extends Button {

        private static final String STYLE_SELECTED = "selected";

        private final UIViewModule view;


        public ValoMenuItemButton(final UIViewModule view) {
            this.view = view;
            setPrimaryStyleName("valo-menu-item");
            setIcon(view.getIcon());
            setCaption(view.getModuleName().substring(0, 1).toUpperCase() + view.getModuleName().substring(1));
            addClickListener((ClickListener) event -> {
                activateView(view.getModuleName());
            });
        }


        @Subscribe
        public void postViewChange(final UIEvent.PostViewChangeEvent event) {
            removeStyleName(STYLE_SELECTED);
            if (event.getView() == view) {
                addStyleName(STYLE_SELECTED);
            }
        }


        @PreDestroy
        private void cleanup() {
            eventBus.unregister(this);
        }
    }


    private void activateView(String viewName) {
        if (viewName == null) {
            final var menuModules = ViewModule.getMenuModules(sessionRegistry.getSession());
            if (menuModules.size() > 0) {
                viewName = menuModules.iterator().next().getModuleName();
            } else {
                viewName = "welcome";
            }
        }
        UI.getCurrent().getNavigator().navigateTo(viewName);
        lastViewName = viewName;
    }


    @PreDestroy
    private void cleanup() {
        eventBus.unregister(this);
    }
}
