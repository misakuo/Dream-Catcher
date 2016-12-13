package me.moxun.dreamcatcher.connector.websocket;

import java.io.IOException;

interface WriteCallback {
  void onFailure(IOException e);
  void onSuccess();
}
