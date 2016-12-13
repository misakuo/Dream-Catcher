package me.moxun.dreamcatcher.connector.log;

/**
 * Created by moxun on 16/4/25.
 */
public interface IAELog {
    boolean isLoggable();
    void l(String msg);
    void i(String msg);
    void d(String msg);
    void w(String msg);
    void e(String msg);
}
