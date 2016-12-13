package me.moxun.dreamcatcher.connector.inspector.protocol.module;


import org.json.JSONObject;

import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsDomain;
import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsMethod;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcPeer;

public class Inspector implements ChromeDevtoolsDomain {
  public Inspector() {
  }

  @ChromeDevtoolsMethod
  public void enable(JsonRpcPeer peer, JSONObject params) {
  }

  @ChromeDevtoolsMethod
  public void disable(JsonRpcPeer peer, JSONObject params) {
  }
}
