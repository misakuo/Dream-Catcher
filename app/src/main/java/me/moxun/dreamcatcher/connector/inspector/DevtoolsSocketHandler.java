package me.moxun.dreamcatcher.connector.inspector;

import android.content.Context;

import java.io.IOException;

import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsDomain;
import me.moxun.dreamcatcher.connector.server.SocketLike;
import me.moxun.dreamcatcher.connector.server.SocketLikeHandler;
import me.moxun.dreamcatcher.connector.server.http.ExactPathMatcher;
import me.moxun.dreamcatcher.connector.server.http.HandlerRegistry;
import me.moxun.dreamcatcher.connector.server.http.LightHttpServer;
import me.moxun.dreamcatcher.connector.websocket.WebSocketHandler;

public class DevtoolsSocketHandler implements SocketLikeHandler {
  private final Context mContext;
  private final Iterable<ChromeDevtoolsDomain> mModules;
  private final LightHttpServer mServer;

  public DevtoolsSocketHandler(Context context, Iterable<ChromeDevtoolsDomain> modules) {
    mContext = context;
    mModules = modules;
    mServer = createServer();
  }

  private LightHttpServer createServer() {
    HandlerRegistry registry = new HandlerRegistry();
    ChromeDiscoveryHandler discoveryHandler =
        new ChromeDiscoveryHandler(
            mContext,
            ChromeDevtoolsServer.PATH);
    discoveryHandler.register(registry);
    registry.register(
        new ExactPathMatcher(ChromeDevtoolsServer.PATH),
        new WebSocketHandler(new ChromeDevtoolsServer(mModules)));

    return new LightHttpServer(registry);
  }

  @Override
  public void onAccepted(SocketLike socket) throws IOException {
    mServer.serve(socket);
  }
}
