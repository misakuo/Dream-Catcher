package me.moxun.dreamcatcher.connector.manager;

/**
 * Created by moxun on 16/12/9.
 */

public class SimpleConnectorLifecycleManager {
    private static int CURRENT_STATE = Lifecycle.SHUTDOWN;

    public static void setCurrentState(int state) {
        CURRENT_STATE = state;
    }

    public static int getCurrentState() {
        return CURRENT_STATE;
    }

    public static boolean isSessionActive() {
        return CURRENT_STATE == Lifecycle.WEBSOCKET_SESSION_OPENING;
    }
}
