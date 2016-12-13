package me.moxun.dreamcatcher.connector.inspector.helper;

import javax.annotation.Nullable;

/**
 * Created by moxun on 16/4/26.
 */
public class TracingPeerManager extends ChromePeerManager {
    private static TracingPeerManager sInstance;

    @Nullable
    public static synchronized TracingPeerManager getInstanceOrNull() {
        return sInstance;
    }

    public static synchronized TracingPeerManager getOrCreateInstance() {
        if (sInstance == null) {
            sInstance = new TracingPeerManager();
        }
        return sInstance;
    }
}
