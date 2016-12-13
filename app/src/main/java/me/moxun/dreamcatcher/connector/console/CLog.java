
package me.moxun.dreamcatcher.connector.console;


import me.moxun.dreamcatcher.connector.inspector.helper.ChromePeerManager;
import me.moxun.dreamcatcher.connector.inspector.protocol.module.Console;

/**
 * Utility for reporting an event to the console
 * WARN:请勿在此类或其子类中以任何方式输出LogCat
 */
public class CLog {
    private static final String TAG = "CLog";

    public static void writeToConsole(
            ChromePeerManager chromePeerManager,
            Console.MessageLevel logLevel,
            Console.MessageSource messageSource,
            String messageText) {

        Console.ConsoleMessage message = new Console.ConsoleMessage();
        message.source = messageSource;
        message.level = logLevel;
        message.text = messageText;
        message.type = Console.MessageType.LOG;
        Console.MessageAddedRequest messageAddedRequest = new Console.MessageAddedRequest();
        messageAddedRequest.message = message;
        chromePeerManager.sendNotificationToPeers("Console.messageAdded", messageAddedRequest);
    }

    public static void writeToConsole(
            Console.MessageLevel logLevel,
            Console.MessageSource messageSource,
            String messageText
    ) {
        ConsolePeerManager peerManager = ConsolePeerManager.getInstanceOrNull();
        if (peerManager == null) {
            return;
        }

        writeToConsole(peerManager, logLevel, messageSource, messageText);
    }

    public static void log(String note) {
        writeToConsole(Console.MessageLevel.LOG, Console.MessageSource.CONSOLE_API, note);
    }
}
