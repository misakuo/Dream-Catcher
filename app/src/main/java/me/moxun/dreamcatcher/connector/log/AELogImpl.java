package me.moxun.dreamcatcher.connector.log;


import me.moxun.dreamcatcher.connector.console.CLog;
import me.moxun.dreamcatcher.connector.inspector.protocol.module.Console;

/**
 * Created by moxun on 16/4/25.
 */
final class AELogImpl implements IAELog {

    @Override
    public boolean isLoggable() {
        return true;
    }

    @Override
    public void l(String msg) {
        if (isLoggable()) {
            log(Console.MessageLevel.LOG, msg);
        }
    }

    @Override
    public void i(String msg) {
        if (isLoggable()) {
            log(Console.MessageLevel.INFO, msg);
        }
    }

    @Override
    public void d(String msg) {
        if (isLoggable()) {
            log(Console.MessageLevel.DEBUG, msg);
        }
    }

    @Override
    public void w(String msg) {
        if (isLoggable()) {
            log(Console.MessageLevel.WARNING, msg);
        }
    }

    @Override
    public void e(String msg) {
        if (isLoggable()) {
            log(Console.MessageLevel.ERROR, msg);
        }
    }

    private static void log(Console.MessageLevel level, String note) {
        CLog.writeToConsole(level, Console.MessageSource.CONSOLE_API, note);
    }
}
