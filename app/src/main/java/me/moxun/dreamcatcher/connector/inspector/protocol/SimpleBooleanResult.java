package me.moxun.dreamcatcher.connector.inspector.protocol;


import me.moxun.dreamcatcher.connector.json.annotation.JsonProperty;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcResult;

public class SimpleBooleanResult implements JsonRpcResult {
  @JsonProperty(required = true)
  public boolean result;

  public SimpleBooleanResult() {
  }

  public SimpleBooleanResult(boolean result) {
    this.result = result;
  }
}
