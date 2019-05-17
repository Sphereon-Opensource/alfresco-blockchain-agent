package com.sphereon.alfresco.blockchain.agent.frontend.events;

import com.sphereon.alfresco.blockchain.agent.frontend.views.UIViewModule;

/**
 * Created by Sander on 21-8-2015.
 */
public abstract class UIEvent {

    public static class BrowserResizeEvent {

    }

    public static class UserSignInRequestEvent {
    }

    public static class UserSignOutRequestEvent {
    }

    public static class SessionStateChangedEvent {
        private boolean loggedIn;


        public SessionStateChangedEvent(boolean loggedIn) {
            this.loggedIn = loggedIn;
        }


        public boolean isLoggedIn() {
            return loggedIn;
        }
    }

    public static class NotificationsCountUpdatedEvent {
    }


    public static final class PostViewChangeEvent {
        private final UIViewModule viewModule;


        public PostViewChangeEvent(final UIViewModule viewModule) {
            this.viewModule = viewModule;
        }


        public UIViewModule getView() {
            return viewModule;
        }
    }

    public static class CloseOpenWindowsEvent {
    }

    public static class ProfileUpdatedEvent {
    }

}
