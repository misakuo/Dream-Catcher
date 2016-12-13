package me.moxun.dreamcatcher.connector.server.http;


import java.io.IOException;

import me.moxun.dreamcatcher.connector.server.SocketLike;

public interface HttpHandler {
  boolean handleRequest(
          SocketLike socket,
          LightHttpRequest request,
          LightHttpResponse response)
      throws IOException;
}
