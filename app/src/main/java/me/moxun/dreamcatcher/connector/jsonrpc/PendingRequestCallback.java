package me.moxun.dreamcatcher.connector.jsonrpc;


import me.moxun.dreamcatcher.connector.jsonrpc.protocol.JsonRpcResponse;

public interface PendingRequestCallback {
  void onResponse(JsonRpcPeer peer, JsonRpcResponse response);
}
