package com.sphereon.alfresco.blockchain.agent.config;

import com.sphereon.libs.authentication.api.AuthenticationApi;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class TokenUpdater {
    private final AuthenticationApi storeElevatedAuthenticationApi;
    private String currentAccessToken;
    private Set<UpdateEvent> updateEvents;

    public TokenUpdater(AuthenticationApi storeElevatedAuthenticationApi) {
        this.storeElevatedAuthenticationApi = storeElevatedAuthenticationApi;
        this.updateEvents = new HashSet<>();
    }

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
