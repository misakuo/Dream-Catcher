package me.moxun.dreamcatcher.connector.util;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.moxun.dreamcatcher.connector.server.http.LightHttpRequest;
import me.moxun.dreamcatcher.connector.server.http.LightHttpResponse;

/**
 * Logging helper specifically for use by DreamCatcher internals.
 */
public class LogUtil {
    private static List<String> requestBlackList = new ArrayList<>();
    private static List<String> responseBlackList = new ArrayList<>();
    private static boolean isLoggable = true;
    private static LogInterface logImpl;

    static {
        logImpl = new KLogImpl();
        logImpl.setLoggable(isLoggable);

        requestBlackList.add("Runtime.evaluate");

        responseBlackList.add("Target activation ignored\n");
    }

    public static void setLoggable(boolean loggable) {
        isLoggable = loggable;
        logImpl.setLoggable(isLoggable);
    }

    public static String filter(@NonNull LightHttpRequest request, @NonNull LightHttpResponse response) {
        if (requestBlackList.contains(request.method)) {
            return null;
        }

        if (responseBlackList.contains(response.body.toString())) {
            return null;
        }

        return "Request:" + request.toString() + ", Response:" + response.toString();
    }

    public static String filter(String title, @NonNull LightHttpResponse response) {

        if (responseBlackList.contains(response.body.toString())) {
            return null;
        }

        return title + ":" + response.toString();
    }

    public static String filter(String title, @NonNull LightHttpRequest request) {
        if (requestBlackList.contains(request.method)) {
            return null;
        }

        return title + ":" + request.toString();
    }

    public static void e(String format, Object... args) {
        logImpl.e(format(format, args));
    }

    public static void e(Throwable t, String format, Object... args) {
        logImpl.e(format(format, args), t);
    }


    public static void e(Throwable t, String message) {
        if (isLoggable(Log.ERROR)) {
            logImpl.e(message, t);
        }
    }

    public static void w(String format, Object... args) {
        logImpl.w(format(format, args));
    }

    public static void w(Throwable t, String format, Object... args) {
        logImpl.w(format(format, args), t);
    }


    public static void w(Throwable t, String message) {
        if (isLoggable(Log.WARN)) {
            logImpl.w(message, t);
        }
    }

    public static void i(String format, Object... args) {
        logImpl.i(format(format, args));
    }

    public static void i(Throwable t, String format, Object... args) {
        logImpl.i(format(format, args), t);
    }

    public static void i(Throwable t, String message) {
        if (isLoggable(Log.INFO)) {
            logImpl.i(message, t);
        }
    }

    public static void d(String format, Object... args) {
        logImpl.d(format(format, args));
    }

    public static void d(Throwable t, String format, Object... args) {
        logImpl.d(format(format, args), t);
    }

    public static void d(Throwable t, String message) {
        if (isLoggable(Log.DEBUG)) {
            logImpl.d(message, t);
        }
    }

    public static void v(String format, Object... args) {
        logImpl.v(format(format, args));
    }

    public static void v(Throwable t, String format, Object... args) {
        logImpl.v(format(format, args), t);
    }

    public static void v(Throwable t, String message) {
        if (isLoggable(Log.VERBOSE)) {
            logImpl.v(message, t);
        }
    }

    private static String format(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }

    public static boolean isLoggable(int priority) {
        switch (priority) {
            case Log.ERROR:
            case Log.WARN:
                return true;
            default:
                return true;
        }
    }

    public static boolean isLoggable(String tag, int priority) {
        return isLoggable(priority);
    }

    public static void v(String msg) {
        logImpl.v(msg);
    }

    public static void v(String tag, String msg) {
        logImpl.v(tag, msg);
    }

    public static void v(String tag, Throwable tr) {
        logImpl.v(tag, tr);
    }

    public static void v(String tag, String msg, Throwable tr) {
        logImpl.v(tag, msg, tr);
    }


    public static void d(String msg) {
        logImpl.d(msg);
    }

    public static void d(String tag, String msg) {
        logImpl.d(tag, msg);
    }

    public static void d(String tag, Throwable tr) {
        logImpl.d(tag, tr);
    }

    public static void d(String tag, String msg, Throwable tr) {
        logImpl.d(tag, msg, tr);
    }


    public static void i(String msg) {
        logImpl.i(msg);
    }

    public static void i(String tag, String msg) {
        logImpl.i(tag, msg);
    }

    public static void i(String tag, Throwable tr) {
        logImpl.i(tag, tr);
    }

    public static void i(String tag, String msg, Throwable tr) {
        logImpl.i(tag, msg, tr);
    }


    public static void w(String msg) {
        logImpl.w(msg);
    }

    public static void w(String tag, String msg) {
        logImpl.w(tag, msg);
    }

    public static void w(String tag, Throwable tr) {
        logImpl.w(tag, tr);
    }

    public static void w(String tag, String msg, Throwable tr) {
        logImpl.w(tag, msg, tr);
    }


    public static void e(String msg) {
        logImpl.e(msg);
    }

    public static void e(String tag, String msg) {
        logImpl.e(tag, msg);
    }

    public static void e(String tag, Throwable tr) {
        logImpl.e(tag, tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        logImpl.e(tag, msg, tr);
    }
}
