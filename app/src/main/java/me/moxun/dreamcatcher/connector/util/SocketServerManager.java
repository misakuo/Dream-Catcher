package me.moxun.dreamcatcher.connector.util;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by moxun on 16/7/1.
 */
public class SocketServerManager {
    public static final String KEY_LOCAL_SERVER_MANAGER = "LOCAL_SERVER_MANAGER";
    public static final String KEY_REMOTE_SERVER_MANAGER = "REMOTE_SERVER_MANAGER";
    private static Map<String, IServerManager> managers = new HashMap<>();
    private static IServerManager currentManager;

    public enum Type {
        REMOTE(KEY_REMOTE_SERVER_MANAGER),
        LOCAL(KEY_LOCAL_SERVER_MANAGER),
        INVALID("INVALID");
        private final String key;

        Type(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public static void register(String key, IServerManager manager) {
        SocketServerManager.managers.put(key, manager);
    }

    public static void startServer(Type type) {
        if (type == Type.REMOTE) {
            IServerManager manager = getManager(KEY_REMOTE_SERVER_MANAGER);
            doStart(manager);
        } else if (type == Type.LOCAL) {
            IServerManager manager = getManager(KEY_LOCAL_SERVER_MANAGER);
            doStart(manager);
        }
    }

    public static void stopServer() {
        if (currentManager != null) {
            currentManager.stop();
        }
    }

    private static void doStart(IServerManager manager) {
        if (currentManager != manager) {
            if (currentManager != null) {
                currentManager.stop();
            }
            if (manager != null) {
                manager.start();
                currentManager = manager;
            }
        }
    }

    public static IServerManager getManager(String key) {
        return managers.get(key);
    }
}
