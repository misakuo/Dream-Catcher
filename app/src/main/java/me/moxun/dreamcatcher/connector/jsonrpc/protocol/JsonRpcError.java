package me.moxun.dreamcatcher.connector.jsonrpc.protocol;

import android.annotation.SuppressLint;

import org.json.JSONObject;

import javax.annotation.Nullable;

import me.moxun.dreamcatcher.connector.json.annotation.JsonProperty;
import me.moxun.dreamcatcher.connector.json.annotation.JsonValue;

@SuppressLint({ "UsingDefaultJsonDeserializer", "EmptyJsonPropertyUse" })
public class JsonRpcError {
  @JsonProperty(required = true)
  public ErrorCode code;

  @JsonProperty(required = true)
  public String message;

  @JsonProperty
  public JSONObject data;

  public JsonRpcError() {
  }

  public JsonRpcError(ErrorCode code, String message, @Nullable JSONObject data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  public enum ErrorCode {
    PARSER_ERROR(-32700),
    INVALID_REQUEST(-32600),
    METHOD_NOT_FOUND(-32601),
    INVALID_PARAMS(-32602),
    INTERNAL_ERROR(-32603);

    private final int mProtocolValue;

    private ErrorCode(int protocolValue) {
      mProtocolValue = protocolValue;
    }

    @JsonValue
    public int getProtocolValue() {
      return mProtocolValue;
    }
  }
}
