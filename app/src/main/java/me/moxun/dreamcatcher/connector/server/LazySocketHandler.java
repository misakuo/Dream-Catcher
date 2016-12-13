package me.moxun.dreamcatcher.connector.server;

import android.net.LocalSocket;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Optimization designed to allow us to lazily construct/configure the true DreamCatcher server
 * only after the first caller connects.  This gives us much more wiggle room to have performance
 * impact in the set up path that only applies when DreamCatcher is _used_, not simply enabled.
 */
public class LazySocketHandler implements SocketHandler {
  private final SocketHandlerFactory mSocketHandlerFactory;

  @Nullable
  private SocketHandler mSocketHandler;

  public LazySocketHandler(SocketHandlerFactory socketHandlerFactory) {
    mSocketHandlerFactory = socketHandlerFactory;
  }

  @Override
  public void onAccepted(LocalSocket socket) throws IOException {
    getSocketHandler().onAccepted(socket);
  }

  @Nonnull
  private synchronized SocketHandler getSocketHandler() {
    if (mSocketHandler == null) {
      mSocketHandler = mSocketHandlerFactory.create();
    }
    return mSocketHandler;
  }
}
