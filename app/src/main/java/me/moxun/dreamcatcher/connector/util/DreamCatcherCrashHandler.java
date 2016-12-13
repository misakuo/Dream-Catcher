package me.moxun.dreamcatcher.connector.util;


import me.moxun.dreamcatcher.connector.log.AELog;

/**
 * Created by moxun on 16/7/12.
 */
public class DreamCatcherCrashHandler implements Thread.UncaughtExceptionHandler {

    private final static DreamCatcherCrashHandler INSTANCE = new DreamCatcherCrashHandler();
    private Thread.UncaughtExceptionHandler oldHandler;

    private DreamCatcherCrashHandler() {

    }

    public static DreamCatcherCrashHandler getInstance() {
        return INSTANCE;
    }

    public void attach() {
        oldHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        AELog.e("发生致命错误，DreamCatcher即将断开连接。");
        AELog.e("Fatal exception on thread " + thread.toString() + ", caused by " + ex.toString() + "\r\n" + dumpException(ex));
        if (oldHandler != null) {
            oldHandler.uncaughtException(thread, ex);
        }
    }

    private String dumpException(Throwable ex) {
        StackTraceElement[] elements = ex.getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : elements) {
            sb.append("\tat ").append(element.toString()).append("\r\n");
        }
        return sb.toString();
    }
}
