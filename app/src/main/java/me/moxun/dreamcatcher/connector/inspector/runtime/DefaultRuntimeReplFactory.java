package me.moxun.dreamcatcher.connector.inspector.runtime;


import java.util.HashMap;
import java.util.Map;

import me.moxun.dreamcatcher.connector.console.RuntimeRepl;
import me.moxun.dreamcatcher.connector.console.RuntimeReplFactory;
import me.moxun.dreamcatcher.connector.console.command.CommandHandler;

/**
 * Created by moxun on 16/3/31.
 */
public class DefaultRuntimeReplFactory implements RuntimeReplFactory {
    private static Map<String, CommandHandler> handlerMap;

    @Override
    public RuntimeRepl newInstance() {
        if (handlerMap == null) {
            handlerMap = new HashMap<>();
            handlerMap.clear();
        }

        return new RuntimeRepl() {
            @Override
            public Object evaluate(String expression) throws Throwable {
                Object ret = null;
                try {
                    ret = processCommand(expression);
                } catch (Exception ex) {
                    //don't throw out
                    ret = ex.getMessage();
                    ex.printStackTrace();
                }
                return ret;
            }
        };
    }

    public static void register(String command, CommandHandler handler) {
        if (handlerMap == null) {
            handlerMap = new HashMap<>();
            handlerMap.clear();
        }
        handlerMap.put(command, handler);
    }

    private Command getCommand(String command) {
        Command c = new Command();
        if (command.contains(" ")) {
            int i = command.indexOf(" ");
            c.command = command.substring(0, i);
            c.param = command.substring(i + 1);
        } else {
            c.command = command;
            c.param = null;
        }
        return c;
    }

    private Object processCommand(String input) {
        Command command = getCommand(input);
        CommandHandler handler = handlerMap.get(command.command);
        if (handler != null) {
            return handler.onCommand(command.param);
        } else {
            return "command '" + input + "' not support now.";
        }
    }

    private class Command {
        String command;
        String param;
    }
}
