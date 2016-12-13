package me.moxun.dreamcatcher.connector.log;

/**
 * 输出日志到Chrome控制台
 * Created by moxun on 16/4/25.
 */
public class AELog {
    private static final IAELog log;
    private static boolean isLoggable = true;

    static {
        log = new AELogImpl();
    }

    public static void setLoggable(boolean isLoggable) {
        AELog.isLoggable = isLoggable;
    }

    public static void l(String msg) {
        if (isLoggable) {
            log.l(msg);
        }
    }

    public static void i(String msg) {
        if (isLoggable) {
            log.i(msg);
        }
    }

    public static void d(String msg) {
        if (isLoggable) {
            log.d(msg);
        }
    }

    public static void w(String msg) {
        if (isLoggable) {
            log.w(msg);
        }
    }

    public static void e(String msg) {
        if (isLoggable) {
            log.e(msg);
        }
    }
}
