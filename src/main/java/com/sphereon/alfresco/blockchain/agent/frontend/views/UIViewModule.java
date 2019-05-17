package com.sphereon.alfresco.blockchain.agent.frontend.views;

import com.sphereon.alfresco.blockchain.agent.frontend.presenters.AbstractPresenter;
import com.vaadin.server.Resource;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.ObjectFactory;

import java.io.Serializable;

/**
 * Created by Sander on 21-8-2015.
 */
public class UIViewModule implements Serializable {

    private final String moduleName;
    private String title;
    private final Resource icon;
    private final boolean stateful;
    private final boolean menuView;
    private final boolean signedInOnly;

    private ObjectFactory<? extends AbstractPresenter> presenterFactory;


    public UIViewModule(String moduleName, Resource icon, boolean stateful, boolean menuView, boolean signedInOnly) {
        this.moduleName = moduleName;
        this.icon = icon;
        this.stateful = stateful;
        this.menuView = menuView;
        this.signedInOnly = signedInOnly;
    }


    public Resource getIcon() {
        return icon;
    }


    public String getModuleName() {
        return moduleName;
    }


    public ObjectFactory<? extends AbstractPresenter> getPresenterFactory() {
        return presenterFactory;
    }


    void setPresenterFactory(ObjectFactory<? extends AbstractPresenter> presenterFactory) {
        this.presenterFactory = presenterFactory;
    }


    public boolean isStateful() {
        return stateful;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public AbstractPresenter getPresenter(UI parentUI) {
        final AbstractPresenter presenter = presenterFactory.getObject();
        presenter.init(parentUI);
        return presenter;
    }


    public boolean isMenuView() {
        return menuView;
    }


    public boolean isSignedInOnly() {
        return signedInOnly;
    }
}
