package com.sphereon.alfresco.blockchain.agent.frontend.presenters;

import com.sphereon.alfresco.blockchain.agent.frontend.components.SessionRegistry;
import com.sphereon.alfresco.blockchain.agent.frontend.views.SphereonView;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

public abstract class AbstractPresenter<V extends SphereonView> implements View {

    protected final SessionRegistry sessionRegistry;
    protected UI parentUI;

    private boolean loadingForm = false;


    public AbstractPresenter(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }


    public void init(UI parentUI) {
        this.parentUI = parentUI;
        init();
    }


    protected abstract void init();

    public abstract V getView();


    @Override
    public Component getViewComponent() {
        return (Component) getView();
    }


    public abstract void enter(ViewChangeListener.ViewChangeEvent event);


    protected boolean isLoadingForm() {
        return loadingForm;
    }


    protected void enableLoadingFormMode() {
        this.loadingForm = true;
    }


    protected void disableLoadingFormMode() {
        this.loadingForm = false;
    }


    protected StringLengthValidator stringLengthValidator(int minLength, int maxLength) {
        return new StringLengthValidator(String.format("The length of this field should be between %d and %d", minLength, maxLength), minLength, maxLength);
    }

}
