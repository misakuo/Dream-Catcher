package me.moxun.dreamcatcher.connector.manager;

/**
 * Created by moxun on 16/12/9.
 */

public interface Lifecycle {
    int SHUTDOWN = 1;
    int LOCAL_SERVER_SOCKET_OPENING = 2;
    int WAITING_FOR_DISCOVERY = 3;
    int CHROME_DISCOVERY_CONNECTED = 4;
    int WAITING_FOR_WEBSOCKET = 5;
    int WEBSOCKET_SESSION_OPENING = 6;
    int WEBSOCKET_SESSION_CLOSED = 7;
}
