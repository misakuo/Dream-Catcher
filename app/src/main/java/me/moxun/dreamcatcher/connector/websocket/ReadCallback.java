package me.moxun.dreamcatcher.connector.websocket;

interface ReadCallback {
  void onCompleteFrame(byte opcode, byte[] payload, int payloadLen);
}
