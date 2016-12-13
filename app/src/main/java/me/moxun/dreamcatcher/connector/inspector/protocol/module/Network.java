package me.moxun.dreamcatcher.connector.inspector.protocol.module;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsDomain;
import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsMethod;
import me.moxun.dreamcatcher.connector.json.annotation.JsonProperty;
import me.moxun.dreamcatcher.connector.json.annotation.JsonValue;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcException;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcPeer;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcResult;
import me.moxun.dreamcatcher.connector.jsonrpc.protocol.JsonRpcError;
import me.moxun.dreamcatcher.connector.reporter.AsyncPrettyPrinterInitializer;
import me.moxun.dreamcatcher.connector.reporter.NetworkPeerManager;
import me.moxun.dreamcatcher.connector.reporter.ResponseBodyData;
import me.moxun.dreamcatcher.connector.reporter.ResponseBodyFileManager;
import me.moxun.dreamcatcher.connector.util.Util;

public class Network implements ChromeDevtoolsDomain {
  private final NetworkPeerManager mNetworkPeerManager;
  private final ResponseBodyFileManager mResponseBodyFileManager;

  public Network(Context context) {
    mNetworkPeerManager = NetworkPeerManager.getOrCreateInstance(context);
    mResponseBodyFileManager = mNetworkPeerManager.getResponseBodyFileManager();
  }

  @ChromeDevtoolsMethod
  public void enable(JsonRpcPeer peer, JSONObject params) {
    mNetworkPeerManager.addPeer(peer);
  }

  @ChromeDevtoolsMethod
  public void disable(JsonRpcPeer peer, JSONObject params) {
    mNetworkPeerManager.removePeer(peer);
  }

  @ChromeDevtoolsMethod
  public void setUserAgentOverride(JsonRpcPeer peer, JSONObject params) {
    // Not implemented...
  }

  @ChromeDevtoolsMethod
  public JsonRpcResult getResponseBody(JsonRpcPeer peer, JSONObject params)
      throws JsonRpcException {
    try {
      String requestId = params.getString("requestId");
      return readResponseBody(requestId);
    } catch (IOException e) {
      throw new JsonRpcException(new JsonRpcError(JsonRpcError.ErrorCode.INTERNAL_ERROR,
          e.toString(),
          null /* data */));
    } catch (JSONException e) {
      throw new JsonRpcException(new JsonRpcError(JsonRpcError.ErrorCode.INTERNAL_ERROR,
          e.toString(),
          null /* data */));
    }
  }

  private GetResponseBodyResponse readResponseBody(String requestId)
      throws IOException, JsonRpcException {
    GetResponseBodyResponse response = new GetResponseBodyResponse();
    ResponseBodyData bodyData;
    try {
      bodyData = mResponseBodyFileManager.readFile(requestId);
    } catch (OutOfMemoryError e) {
      throw new JsonRpcException(new JsonRpcError(JsonRpcError.ErrorCode.INTERNAL_ERROR,
          e.toString(),
          null /* data */));
    }
    response.body = bodyData.data;
    response.base64Encoded = bodyData.base64Encoded;
    return response;
  }

  /**
   * Method that allows callers to provide an {@link AsyncPrettyPrinterInitializer} that is
   * responsible for registering all
   * Note that AsyncPrettyPrinterInitializer cannot be null and can only be set once.
   * @param initializer
   */
  public void setPrettyPrinterInitializer(AsyncPrettyPrinterInitializer initializer) {
    Util.throwIfNull(initializer);
    mNetworkPeerManager.setPrettyPrinterInitializer(initializer);
  }

  private static class GetResponseBodyResponse implements JsonRpcResult {
    @JsonProperty(required = true)
    public String body;

    @JsonProperty(required = true)
    public boolean base64Encoded;
  }

  public static class RequestWillBeSentParams {
    @JsonProperty(required = true)
    public String requestId;

    @JsonProperty(required = true)
    public String frameId;

    @JsonProperty(required = true)
    public String loaderId;

    @JsonProperty(required = true)
    public String documentURL;

    @JsonProperty(required = true)
    public Request request;

    @JsonProperty(required = true)
    public double timestamp;

    @JsonProperty(required = true)
    public Initiator initiator;

    @JsonProperty
    public Response redirectResponse;

    @JsonProperty
    public Page.ResourceType type;
  }

  public static class ResponseReceivedParams {
    @JsonProperty(required = true)
    public String requestId;

    @JsonProperty(required = true)
    public String frameId;

    @JsonProperty(required = true)
    public String loaderId;

    @JsonProperty(required = true)
    public double timestamp;

    @JsonProperty(required = true)
    public Page.ResourceType type;

    @JsonProperty(required = true)
    public Response response;
  }

  public static class LoadingFinishedParams {
    @JsonProperty(required = true)
    public String requestId;

    @JsonProperty(required = true)
    public double timestamp;
  }

  public static class LoadingFailedParams {
    @JsonProperty(required = true)
    public String requestId;

    @JsonProperty(required = true)
    public double timestamp;

    @JsonProperty(required = true)
    public String errorText;

    // Chrome introduced this undocumented new addition that, if not sent, will cause the row
    // to be removed from the UI and raise a JavaScript exception in the console.  This is
    // clearly an upstream bug that needs to be fixed, though we can work around it by
    // providing this new undocumented field.
    @JsonProperty
    public Page.ResourceType type;
  }

  public static class DataReceivedParams {
    @JsonProperty(required = true)
    public String requestId;

    @JsonProperty(required = true)
    public double timestamp;

    @JsonProperty(required = true)
    public int dataLength;

    @JsonProperty(required = true)
    public int encodedDataLength;
  }

  public static class Request {
    @JsonProperty(required = true)
    public String url;

    @JsonProperty(required = true)
    public String method;

    @JsonProperty(required = true)
    public JSONObject headers;

    @JsonProperty
    public String postData;
  }

  public static class Initiator {
    @JsonProperty(required = true)
    public InitiatorType type;

    @JsonProperty
    public List<Console.CallFrame> stackTrace;
  }

  public enum InitiatorType {
    PARSER("parser"),
    SCRIPT("script"),
    OTHER("other");

    private final String mProtocolValue;

    private InitiatorType(String protocolValue) {
      mProtocolValue = protocolValue;
    }

    @JsonValue
    public String getProtocolValue() {
      return mProtocolValue;
    }
  }

  public static class Response {
    @JsonProperty(required = true)
    public String url;

    @JsonProperty(required = true)
    public int status;

    @JsonProperty(required = true)
    public String statusText;

    @JsonProperty(required = true)
    public JSONObject headers;

    @JsonProperty
    public String headersText;

    @JsonProperty(required = true)
    public String mimeType;

    @JsonProperty
    public JSONObject requestHeaders;

    @JsonProperty
    public String requestHeadersTest;

    @JsonProperty(required = true)
    public boolean connectionReused;

    @JsonProperty(required = true)
    public int connectionId;

    @JsonProperty(required = true)
    public Boolean fromDiskCache;

    @JsonProperty
    public ResourceTiming timing;
  }

  public static class ResourceTiming {
    @JsonProperty(required = true)
    public double requestTime;

    @JsonProperty(required = true)
    public double proxyStart;

    @JsonProperty(required = true)
    public double proxyEnd;

    @JsonProperty(required = true)
    public double dnsStart;

    @JsonProperty(required = true)
    public double dnsEnd;

    @JsonProperty(required = true)
    public double connectionStart;

    @JsonProperty(required = true)
    public double connectionEnd;

    @JsonProperty(required = true)
    public double sslStart;

    @JsonProperty(required = true)
    public double sslEnd;

    @JsonProperty(required = true)
    public double sendStart;

    @JsonProperty(required = true)
    public double sendEnd;

    @JsonProperty(required = true)
    public double receivedHeadersEnd;
  }
}
