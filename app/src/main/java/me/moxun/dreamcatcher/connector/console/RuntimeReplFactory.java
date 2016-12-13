package me.moxun.dreamcatcher.connector.console;

/**
 * Allows callers to specify their own Console tab REPL for the DevTools UI.  This is part of
 * early support for a possible optionally included default implementation for Android.
 * <p />
 * A new {@link RuntimeRepl} instances is created for each unique peer such that memory
 * can be garbage collected when the peer disconnects.
 * <p />
 * This is provided as part of an experimental API.  Depend on it at your own risk...
 */
public interface RuntimeReplFactory {
  RuntimeRepl newInstance();
}
