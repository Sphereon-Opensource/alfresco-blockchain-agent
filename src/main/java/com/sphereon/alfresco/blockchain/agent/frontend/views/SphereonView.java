package com.sphereon.alfresco.blockchain.agent.frontend.views;

import com.sphereon.alfresco.blockchain.agent.frontend.presenters.AbstractPresenter;
import com.vaadin.ui.VerticalLayout;

public abstract class SphereonView extends VerticalLayout {

    private AbstractPresenter presenter;


    public void setPresenter(AbstractPresenter presenter) {

        this.presenter = presenter;
    }


    public AbstractPresenter getPresenter() {
        return presenter;
    }
}
