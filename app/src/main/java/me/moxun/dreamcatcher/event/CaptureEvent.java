package me.moxun.dreamcatcher.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by moxun on 16/12/9.
 */

public class CaptureEvent {
    private static boolean allowReport = false;
    public String cause;

    public CaptureEvent(String cause) {
        this.cause = cause;
    }

    public static void send(String msg) {
        if (allowReport) {
            EventBus.getDefault().post(new CaptureEvent(msg));
        }
    }
}
