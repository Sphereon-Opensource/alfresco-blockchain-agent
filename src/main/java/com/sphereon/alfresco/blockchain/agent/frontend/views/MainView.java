package com.sphereon.alfresco.blockchain.agent.frontend.views;

import com.sphereon.alfresco.blockchain.agent.frontend.components.UIMenu;
import com.sphereon.alfresco.blockchain.agent.frontend.components.UINavigator;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by Sander on 21-8-2015.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MainView extends HorizontalLayout {

    private final UIMenu uiMenu;

    private final UINavigator uiNavigator;
    private final CssLayout content;


    @Autowired
    public MainView(UIMenu uiMenu, UINavigator uiNavigator) {
        this.uiMenu = uiMenu;
        this.uiNavigator = uiNavigator;
        setSizeFull();
        addStyleName("mainview");

        addComponent(this.uiMenu);

        content = new CssLayout();
        content.addStyleName("view-content");
        content.setSizeFull();
        addComponent(content);
        setExpandRatio(content, 1.0f);
    }


    public void init() {
        uiMenu.init();
        this.uiNavigator.init(content);
    }
}
