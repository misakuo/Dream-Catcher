package me.moxun.dreamcatcher.connector.jsonrpc;

import android.database.Observable;

import org.json.JSONObject;

import java.nio.channels.NotYetConnectedException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import me.moxun.dreamcatcher.connector.json.ObjectMapper;
import me.moxun.dreamcatcher.connector.jsonrpc.protocol.JsonRpcRequest;
import me.moxun.dreamcatcher.connector.util.Util;
import me.moxun.dreamcatcher.connector.websocket.SimpleSession;

@ThreadSafe
public class JsonRpcPeer {
  private final SimpleSession mPeer;
  private final ObjectMapper mObjectMapper;

  @GuardedBy("this")
  private long mNextRequestId;

  @GuardedBy("this")
  private final Map<Long, PendingRequest> mPendingRequests = new HashMap<>();

  private final DisconnectObservable mDisconnectObservable = new DisconnectObservable();

  public JsonRpcPeer(ObjectMapper objectMapper, SimpleSession peer) {
    mObjectMapper = objectMapper;
    mPeer = Util.throwIfNull(peer);
  }

  public SimpleSession getWebSocket() {
    return mPeer;
  }

  public void invokeMethod(String method, Object paramsObject,
                           @Nullable PendingRequestCallback callback)
      throws NotYetConnectedException {
    Util.throwIfNull(method);

    Long requestId = (callback != null) ? preparePendingRequest(callback) : null;

    // magic, can basically convert anything for some amount of runtime overhead...
    JSONObject params = mObjectMapper.convertValue(paramsObject, JSONObject.class);

    JsonRpcRequest message = new JsonRpcRequest(requestId, method, params);
    String requestString;
    JSONObject jsonObject = mObjectMapper.convertValue(message, JSONObject.class);
    requestString = jsonObject.toString();
    mPeer.sendText(requestString);
  }

  public void registerDisconnectReceiver(DisconnectReceiver callback) {
    mDisconnectObservable.registerObserver(callback);
  }

  public void unregisterDisconnectReceiver(DisconnectReceiver callback) {
    mDisconnectObservable.unregisterObserver(callback);
  }

  public void invokeDisconnectReceivers() {
    mDisconnectObservable.onDisconnect();
  }

  private synchronized long preparePendingRequest(PendingRequestCallback callback) {
    long requestId = mNextRequestId++;
    mPendingRequests.put(requestId, new PendingRequest(requestId, callback));
    return requestId;
  }

  public synchronized PendingRequest getAndRemovePendingRequest(long requestId) {
    return mPendingRequests.remove(requestId);
  }

  private static class DisconnectObservable extends Observable<DisconnectReceiver> {
    public void onDisconnect() {
      for (int i = 0, N = mObservers.size(); i < N; ++i) {
        final DisconnectReceiver observer = mObservers.get(i);
        observer.onDisconnect();
      }
    }
  }
}
