package me.moxun.dreamcatcher.connector.console;

public interface RuntimeRepl {
  Object evaluate(String expression) throws Throwable;
}
