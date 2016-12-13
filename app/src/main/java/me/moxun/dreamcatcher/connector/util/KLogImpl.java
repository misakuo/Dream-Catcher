package me.moxun.dreamcatcher.connector.util;

public class KLogImpl implements LogInterface {
    @Override
    public void setLoggable(boolean loggable) {
        KLog.setLoggable(loggable);
    }

    @Override
    public void v(String msg) {
        KLog.v(msg);
    }
    @Override
    public void v(String tag, String msg) {
        KLog.v(tag, msg);
    }

    @Override
    public void v(String tag, Throwable tr) {
        KLog.v(tag, android.util.Log.getStackTraceString(tr));
    }

    @Override
    public void v(String tag, String msg, Throwable tr) {
        KLog.v(tag, msg + "\n" + android.util.Log.getStackTraceString(tr));
    }

    @Override
    public void d(String msg) {
        KLog.d(msg);
    }

    @Override
    public void d(String tag, String msg) {
        KLog.d(tag, msg);
    }

    @Override
    public void d(String tag, Throwable tr) {
        KLog.d(tag, android.util.Log.getStackTraceString(tr));
    }

    @Override
    public void d(String tag, String msg, Throwable tr) {
        KLog.d(tag, msg + "\n" + android.util.Log.getStackTraceString(tr));
    }

    @Override
    public void i(String msg) {
        KLog.i(msg);
    }

    @Override
    public void i(String tag, String msg) {
        KLog.i(tag, msg);
    }

    @Override
    public void i(String tag, Throwable tr) {
        KLog.i(tag, android.util.Log.getStackTraceString(tr));
    }

    @Override
    public void i(String tag, String msg, Throwable tr) {
        KLog.i(tag, msg + "\n" + android.util.Log.getStackTraceString(tr));
    }

    @Override
    public void w(String msg) {
        KLog.w(msg);
    }

    @Override
    public void w(String tag, String msg) {
        KLog.w(tag, msg);
    }

    @Override
    public void w(String tag, Throwable tr) {
        KLog.w(tag, android.util.Log.getStackTraceString(tr));
    }

    @Override
    public void w(String tag, String msg, Throwable tr) {
        KLog.w(tag, msg + "\n" + android.util.Log.getStackTraceString(tr));
    }

    @Override
    public void e(String msg) {
        KLog.e(msg);
    }

    @Override
    public void e(String tag, String msg) {
        KLog.e(tag, msg);
    }

    @Override
    public void e(String tag, Throwable tr) {
        KLog.e(tag, android.util.Log.getStackTraceString(tr));
    }

    @Override
    public void e(String tag, String msg, Throwable tr) {
        KLog.e(tag, msg + "\n" + android.util.Log.getStackTraceString(tr));
    }
}
