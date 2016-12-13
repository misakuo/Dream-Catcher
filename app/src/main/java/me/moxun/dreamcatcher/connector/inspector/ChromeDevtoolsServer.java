package me.moxun.dreamcatcher.connector.inspector;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsDomain;
import me.moxun.dreamcatcher.connector.json.ObjectMapper;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcException;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcPeer;
import me.moxun.dreamcatcher.connector.jsonrpc.PendingRequest;
import me.moxun.dreamcatcher.connector.jsonrpc.protocol.JsonRpcError;
import me.moxun.dreamcatcher.connector.jsonrpc.protocol.JsonRpcRequest;
import me.moxun.dreamcatcher.connector.jsonrpc.protocol.JsonRpcResponse;
import me.moxun.dreamcatcher.connector.manager.Lifecycle;
import me.moxun.dreamcatcher.connector.manager.SimpleConnectorLifecycleManager;
import me.moxun.dreamcatcher.connector.util.LogFilter;
import me.moxun.dreamcatcher.connector.util.LogUtil;
import me.moxun.dreamcatcher.connector.util.Util;
import me.moxun.dreamcatcher.connector.websocket.CloseCodes;
import me.moxun.dreamcatcher.connector.websocket.SimpleEndpoint;
import me.moxun.dreamcatcher.connector.websocket.SimpleSession;
import me.moxun.dreamcatcher.event.CaptureEvent;
import me.moxun.dreamcatcher.event.OperateEvent;

/**
 * Implements a limited version of the Chrome Debugger WebSocket protocol (using JSON-RPC 2.0).
 * The most up-to-date documentation can be found in the Blink source code:
 * <a href="https://code.google.com/p/chromium/codesearch#chromium/src/third_party/WebKit/Source/devtools/protocol.json&q=protocol.json&sq=package:chromium&type=cs">protocol.json</a>
 */
public class ChromeDevtoolsServer implements SimpleEndpoint {
    private static final String TAG = "ChromeDevtoolsServer";

    public static final String PATH = "/inspector";

    private final ObjectMapper mObjectMapper;
    private final MethodDispatcher mMethodDispatcher;
    private final Map<SimpleSession, JsonRpcPeer> mPeers =
            Collections.synchronizedMap(
                    new HashMap<SimpleSession, JsonRpcPeer>());

    public ChromeDevtoolsServer(Iterable<ChromeDevtoolsDomain> domainModules) {
        mObjectMapper = new ObjectMapper();
        mMethodDispatcher = new MethodDispatcher(mObjectMapper, domainModules);
    }

    @Override
    public void onOpen(SimpleSession session) {
        LogUtil.e(TAG, "Open session: " + session.toString());
        mPeers.put(session, new JsonRpcPeer(mObjectMapper, session));
        SimpleConnectorLifecycleManager.setCurrentState(Lifecycle.WAITING_FOR_DISCOVERY);
        CaptureEvent.send("Waiting for chrome discovery");
    }

    @Override
    public void onClose(SimpleSession session, int code, String reasonPhrase) {
        LogUtil.e(TAG, "Close session: " + session.toString() + ", cause: " + reasonPhrase + "(" + code + ")");

        JsonRpcPeer peer = mPeers.remove(session);
        if (peer != null) {
            peer.invokeDisconnectReceivers();
        }
    }

    @Override
    public void onMessage(SimpleSession session, byte[] message, int messageLen) {
        LogUtil.d(TAG, "Ignoring binary message of length " + messageLen);
    }

    @Override
    public void onMessage(SimpleSession session, String message) {
        if (LogUtil.isLoggable(TAG, Log.VERBOSE)) {
            LogUtil.v(TAG, "onMessage: message=" + message);
        }
        try {
            JsonRpcPeer peer = mPeers.get(session);
            Util.throwIfNull(peer);

            handleRemoteMessage(peer, message);
        } catch (IOException e) {
            if (LogUtil.isLoggable(TAG, Log.VERBOSE)) {
                LogUtil.v(TAG, "Unexpected I/O exception processing message: " + e);
            }
            closeSafely(session, CloseCodes.UNEXPECTED_CONDITION, e.getClass().getSimpleName());
        } catch (MessageHandlingException e) {
            LogUtil.i(TAG, "Message could not be processed by implementation: " + e);
            closeSafely(session, CloseCodes.UNEXPECTED_CONDITION, e.getClass().getSimpleName());
        } catch (JSONException e) {
            LogUtil.v(TAG, "Unexpected JSON exception processing message", e);
            closeSafely(session, CloseCodes.UNEXPECTED_CONDITION, e.getClass().getSimpleName());
        }
    }

    private void closeSafely(SimpleSession session, int code, String reasonPhrase) {
        session.close(code, reasonPhrase);
    }

    private void handleRemoteMessage(JsonRpcPeer peer, String message)
            throws IOException, MessageHandlingException, JSONException {
        // Parse as a generic JSONObject first since we don't know if this is a request or response.
        JSONObject messageNode = new JSONObject(message);
        if (messageNode.has("method")) {
            handleRemoteRequest(peer, messageNode);
        } else if (messageNode.has("result")) {
            handleRemoteResponse(peer, messageNode);
        } else {
            throw new MessageHandlingException("Improper JSON-RPC message: " + message);
        }
    }

    private void handleRemoteRequest(JsonRpcPeer peer, JSONObject requestNode)
            throws MessageHandlingException {
        JsonRpcRequest request;
        request = mObjectMapper.convertValue(
                requestNode,
                JsonRpcRequest.class);

        if (!request.method.equals("Runtime.evaluate")) {
            //LogUtil.w(TAG, "JsonRpcRequest received: " + request.toString());
        }

        JSONObject result = null;
        JSONObject error = null;

        try {
            result = mMethodDispatcher.dispatch(peer,
                    request.method,
                    request.params);
        } catch (JsonRpcException e) {
            logDispatchException(e);
            error = mObjectMapper.convertValue(e.getErrorMessage(), JSONObject.class);
        }
        if (request.id != null) {
            JsonRpcResponse response = new JsonRpcResponse();
            response.id = request.id;
            response.result = result;
            response.error = error;
            JSONObject jsonObject = mObjectMapper.convertValue(response, JSONObject.class);
            String responseString;
            try {
                responseString = jsonObject.toString();
            } catch (OutOfMemoryError e) {
                // JSONStringer can cause an OOM when the Json to handle is too big.
                response.result = null;
                response.error = mObjectMapper.convertValue(e.getMessage(), JSONObject.class);
                jsonObject = mObjectMapper.convertValue(response, JSONObject.class);
                responseString = jsonObject.toString();
            }
            JSONObject log = LogFilter.filter(requestNode, jsonObject);
            if (log != null) {
                LogUtil.w(log.toString());
            }
            peer.getWebSocket().sendText(responseString);
        }
    }

    private static void logDispatchException(JsonRpcException e) {
        JsonRpcError errorMessage = e.getErrorMessage();
        switch (errorMessage.code) {
            case METHOD_NOT_FOUND:
                LogUtil.e(TAG, "Method not implemented: " + errorMessage.message);
                break;
            default:
                LogUtil.w(TAG, "Error processing remote message", e);
        }
    }

    private void handleRemoteResponse(JsonRpcPeer peer, JSONObject responseNode)
            throws MismatchedResponseException {
        LogUtil.w(responseNode.toString());
        JsonRpcResponse response = mObjectMapper.convertValue(
                responseNode,
                JsonRpcResponse.class);
        PendingRequest pendingRequest = peer.getAndRemovePendingRequest(response.id);
        if (pendingRequest == null) {
            throw new MismatchedResponseException(response.id);
        }
        if (pendingRequest.callback != null) {
            pendingRequest.callback.onResponse(peer, response);
        }
    }

    @Override
    public void onError(SimpleSession session, Throwable ex) {
        LogUtil.w(TAG, "Session error: " + ex.toString());
        SimpleConnectorLifecycleManager.setCurrentState(Lifecycle.WEBSOCKET_SESSION_CLOSED);
        CaptureEvent.send("Websocket session closes unexpectedly");
        EventBus.getDefault().post(new OperateEvent(OperateEvent.TARGET_CONNECTOR, false, true, "Websocket session closed"));
    }
}
