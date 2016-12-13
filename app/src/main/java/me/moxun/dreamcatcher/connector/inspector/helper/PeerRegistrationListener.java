package me.moxun.dreamcatcher.connector.inspector.helper;


import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcPeer;

public interface PeerRegistrationListener {
  void onPeerRegistered(JsonRpcPeer peer);
  void onPeerUnregistered(JsonRpcPeer peer);
}
