package me.moxun.dreamcatcher.connector.jsonrpc;


import me.moxun.dreamcatcher.connector.jsonrpc.protocol.JsonRpcError;
import me.moxun.dreamcatcher.connector.util.Util;

public class JsonRpcException extends Exception {
  private final JsonRpcError mErrorMessage;

  public JsonRpcException(JsonRpcError errorMessage) {
    super(errorMessage.code + ": " + errorMessage.message);
    mErrorMessage = Util.throwIfNull(errorMessage);
  }

  public JsonRpcError getErrorMessage() {
    return mErrorMessage;
  }
}
