package me.moxun.dreamcatcher.connector.server;

import android.net.LocalSocket;

import java.io.IOException;

/**
 * @see SecureSocketHandler
 */
public interface SocketHandler {
  /**
   * Server socket has been accepted and a dedicated thread has been allocated to process this
   * callback.  Returning from this method or throwing an exception will attempt an orderly
   * shutdown of the socket, however it will not be treated as an error if returning normally.
   */
  void onAccepted(LocalSocket socket) throws IOException;
}
