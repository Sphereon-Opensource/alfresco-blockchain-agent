package com.sphereon.alfresco.blockchain.agent.frontend.components;

import com.sphereon.alfresco.blockchain.agent.frontend.views.SphereonView;
import com.vaadin.ui.*;

public abstract class InputFormView extends SphereonView {


    protected void disableAutofill(AbstractComponent component) {
        final String styleName = "noAutoFill_" + component.hashCode();
        component.addStyleName(styleName);
        JavaScript.getCurrent().execute(
            "var elem = document.getElementsByClassName('" + styleName + "')[0];elem.setAttribute('data-lpignore', 'true');elem.setAttribute('autocomplete', 'off')");
    }


    protected void disableLoginManager(AbstractComponent component) {
        final String styleName = "noAutoFill_" + component.hashCode();
        component.addStyleName(styleName);
        JavaScript.getCurrent().execute(
            "var elem = document.getElementsByClassName('" + styleName + "')[0];elem.setAttribute('data-lpignore', 'true')");
    }


    public void setReadOnly() {
        HasComponents hasComponents = this;
        setReadOnly(hasComponents);
    }


    private void setReadOnly(HasComponents hasComponents) {
        hasComponents.iterator().forEachRemaining(component -> {
            if (component instanceof AbstractField) {
                ((AbstractField) component).setReadOnly(true);
            } else if (component instanceof AbstractSingleSelect) {
                ((AbstractSingleSelect) component).setReadOnly(true);
            } else if (component instanceof AbstractSingleComponentContainer) {
                var childComponent = ((AbstractSingleComponentContainer) component).getContent();
                if (HasComponents.class.isAssignableFrom(childComponent.getClass())) {
                    setReadOnly((HasComponents) childComponent);
                }
            } else if (HasComponents.class.isAssignableFrom(component.getClass())) {
                setReadOnly((HasComponents) component);
            }
        });
    }
}
