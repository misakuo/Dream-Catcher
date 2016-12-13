package me.moxun.dreamcatcher.connector.jsonrpc.protocol;

import android.annotation.SuppressLint;

import org.json.JSONObject;

import me.moxun.dreamcatcher.connector.json.annotation.JsonProperty;

@SuppressLint({ "UsingDefaultJsonDeserializer", "EmptyJsonPropertyUse" })
public class JsonRpcResponse {
  @JsonProperty(required = true)
  public long id;

  @JsonProperty
  public JSONObject result;

  @JsonProperty
  public JSONObject error;
}
