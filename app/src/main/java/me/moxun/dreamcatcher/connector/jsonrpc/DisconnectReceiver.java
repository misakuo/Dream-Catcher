package me.moxun.dreamcatcher.connector.jsonrpc;

/**
 * @see JsonRpcPeer#registerDisconnectReceiver(DisconnectReceiver)
 */
public interface DisconnectReceiver {
  /**
   * Invoked when a WebSocket peer disconnects.
   */
  void onDisconnect();
}
