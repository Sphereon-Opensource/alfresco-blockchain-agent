package com.sphereon.alfresco.blockchain.agent.frontend.events;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by Sander on 21-8-2015.
 */

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SessionEventBus implements SubscriberExceptionHandler {
    private final EventBus eventBus = new EventBus(this);


    public void post(final Object event) {
        eventBus.post(event);
    }


    public void register(final Object object) {
        eventBus.register(object);
    }


    public void unregister(final Object object) {
        eventBus.unregister(object);
    }


    @Override
    public final void handleException(final Throwable exception,
                                      final SubscriberExceptionContext context) {
        exception.printStackTrace();
    }
}
