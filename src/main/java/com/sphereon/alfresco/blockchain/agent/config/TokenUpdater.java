package com.sphereon.alfresco.blockchain.agent.config;

import com.sphereon.libs.authentication.api.AuthenticationApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class TokenUpdater {

    @Autowired
    private AuthenticationApi storeElevatedAuthenticationApi;

    private Set<UpdateEvent> updateEvents = new HashSet<>();

    private String currentAccessToken;


    public void addUpdateListener(UpdateEvent updateEvent) {
        updateEvents.add(updateEvent);
    }


    public void updateAccessToken(String accessToken) {
        this.currentAccessToken = accessToken;
        for (var event : updateEvents) {
            event.setAccessToken(accessToken);
        }
    }


    public String getCurrentAccessToken() {
        return currentAccessToken;
    }


    public void revoke() {
        storeElevatedAuthenticationApi.revokeToken();
    }


    public interface UpdateEvent {
        void setAccessToken(String accessToken);
    }

}
