package me.moxun.dreamcatcher.connector.server;

/** @see LazySocketHandler */
public interface SocketHandlerFactory {
  SocketHandler create();
}
