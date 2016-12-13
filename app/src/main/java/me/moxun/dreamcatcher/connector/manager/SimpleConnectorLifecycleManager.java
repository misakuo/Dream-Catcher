package me.moxun.dreamcatcher.connector.manager;

import me.moxun.dreamcatcher.connector.inspector.ChromeDiscoveryHandler;

/**
 * Created by moxun on 16/12/9.
 */

public class SimpleConnectorLifecycleManager {
    private static int CURRENT_STATE = Lifecycle.SHUTDOWN;
    private static boolean PROXY_ENABLED = false;

    public static void setCurrentState(int state) {
        CURRENT_STATE = state;
    }

    public static int getCurrentState() {
        return CURRENT_STATE;
    }

    public static boolean isSessionActive() {
        return CURRENT_STATE == Lifecycle.WEBSOCKET_SESSION_OPENING;
    }

    public static boolean isProxyEnabled() {
        return PROXY_ENABLED;
    }

    public static void setProxyEnabled(boolean proxyEnabled) {
        PROXY_ENABLED = proxyEnabled;
        ChromeDiscoveryHandler.setInvalid(true);
    }
}
