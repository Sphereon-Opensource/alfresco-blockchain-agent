package com.sphereon.alfresco.blockchain.agent.backend.commands.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

public class CommandFactory {

    @Component
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public static class AlfrescoCommands {
        // TODO or remove
/*

        @Autowired
        private ObjectFactory<StoreLoginElevatedCommand> storeLoginElevatedCommandObjectFactory;
*/


/*

        public StoreLoginElevatedCommand createStoreLoginElevatedCommand() {
            return storeLoginElevatedCommandObjectFactory.getObject();
        }
*/

    }
}
