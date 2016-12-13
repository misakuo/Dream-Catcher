package me.moxun.dreamcatcher.connector.jsonrpc.protocol;

import android.annotation.SuppressLint;

import org.json.JSONObject;

import javax.annotation.Nullable;

import me.moxun.dreamcatcher.connector.json.annotation.JsonProperty;

@SuppressLint({ "UsingDefaultJsonDeserializer", "EmptyJsonPropertyUse" })
public class JsonRpcEvent {
  @JsonProperty(required = true)
  public String method;

  @JsonProperty
  public JSONObject params;

  public JsonRpcEvent() {
  }

  public JsonRpcEvent(String method, @Nullable JSONObject params) {
    this.method = method;
    this.params = params;
  }
}
