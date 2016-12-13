package me.moxun.dreamcatcher.connector.inspector.protocol.module;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsDomain;
import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsMethod;
import me.moxun.dreamcatcher.connector.inspector.protocol.SimpleBooleanResult;
import me.moxun.dreamcatcher.connector.json.ObjectMapper;
import me.moxun.dreamcatcher.connector.json.annotation.JsonProperty;
import me.moxun.dreamcatcher.connector.json.annotation.JsonValue;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcPeer;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcResult;
import me.moxun.dreamcatcher.connector.util.LogUtil;
import me.moxun.dreamcatcher.connector.util.ProcessUtil;

public class Page implements ChromeDevtoolsDomain {
    private final Context mContext;
    private final ObjectMapper mObjectMapper = new ObjectMapper();
    private int lastFrame = 0;

    public Page(Context context) {
        mContext = context;
    }

    @ChromeDevtoolsMethod
    public void enable(JsonRpcPeer peer, JSONObject params) {
        notifyExecutionContexts(peer);
        sendWelcomeMessage(peer);
    }

    @ChromeDevtoolsMethod
    public void disable(JsonRpcPeer peer, JSONObject params) {
    }

    private void notifyExecutionContexts(JsonRpcPeer peer) {
        ExecutionContextDescription context = new ExecutionContextDescription();
        context.frameId = "1";
        context.id = 1;
        ExecutionContextCreatedParams params = new ExecutionContextCreatedParams();
        params.context = context;
        peer.invokeMethod("Runtime.executionContextCreated", params, null /* callback */);
    }

    private void sendWelcomeMessage(JsonRpcPeer peer) {
        Console.ConsoleMessage message = new Console.ConsoleMessage();
        message.source = Console.MessageSource.JAVASCRIPT;
        message.level = Console.MessageLevel.LOG;
        String art =
                "      ___                                       ___           __               \n" +
                        "     /  /\\          ___           ___          /  /\\         |  |\\             \n" +
                        "    /  /::\\        /  /\\         /  /\\        /  /::\\        |  |:|            \n" +
                        "   /  /:/\\:\\      /  /::\\       /  /::\\      /  /:/\\:\\       |  |:|            \n" +
                        "  /  /::\\ \\:\\    /  /:/\\:\\     /  /:/\\:\\    /  /::\\ \\:\\      |__|:|__          \n" +
                        " /__/:/\\:\\_\\:\\  /  /::\\ \\:\\   /  /::\\ \\:\\  /__/:/\\:\\ \\:\\ ____/__/::::\\         \n" +
                        " \\__\\/  \\:\\/:/ /__/:/\\:\\_\\:\\ /__/:/\\:\\_\\:\\ \\  \\:\\ \\:\\_\\/ \\__\\::::/~~~~         \n" +
                        "      \\__\\::/  \\__\\/  \\:\\/:/ \\__\\/  \\:\\/:/  \\  \\:\\ \\:\\      |~~|:|             \n" +
                        "      /  /:/        \\  \\::/       \\  \\::/    \\  \\:\\_\\/      |  |:|             \n" +
                        "     /__/:/          \\__\\/         \\__\\/      \\  \\:\\        |__|:|             \n" +
                        "     \\__\\/                                     \\__\\/         \\__\\|             \n" +
                        " Welcome to DreamCatcher\n Attached to process " + ProcessUtil.getProcessName() + "\n\n";
        message.text = art;
        // Note: not using Android resources so we can maintain .jar distribution for now.
        Console.MessageAddedRequest messageAddedRequest = new Console.MessageAddedRequest();
        messageAddedRequest.message = message;
        peer.invokeMethod("Console.messageAdded", messageAddedRequest, null /* callback */);
    }

    private static FrameResourceTree createSimpleFrameResourceTree(
            String id,
            String parentId,
            String name,
            String securityOrigin) {
        Frame frame = new Frame();
        frame.id = id;
        frame.parentId = parentId;
        frame.loaderId = "1";
        frame.name = name;
        frame.url = "";
        frame.securityOrigin = securityOrigin;
        frame.mimeType = "text/plain";
        FrameResourceTree tree = new FrameResourceTree();
        tree.frame = frame;
        tree.resources = Collections.emptyList();
        tree.childFrames = null;
        return tree;
    }

    @ChromeDevtoolsMethod
    public JsonRpcResult canScreencast(JsonRpcPeer peer, JSONObject params) {
        return new SimpleBooleanResult(false);
    }

    @ChromeDevtoolsMethod
    public JsonRpcResult hasTouchInputs(JsonRpcPeer peer, JSONObject params) {
        return new SimpleBooleanResult(false);
    }

    @ChromeDevtoolsMethod
    public void setDeviceMetricsOverride(JsonRpcPeer peer, JSONObject params) {
    }

    @ChromeDevtoolsMethod
    public void clearDeviceOrientationOverride(JsonRpcPeer peer, JSONObject params) {
    }

    @ChromeDevtoolsMethod
    public void startScreencast(final JsonRpcPeer peer, JSONObject params) {

    }

    @ChromeDevtoolsMethod
    public void stopScreencast(JsonRpcPeer peer, JSONObject params) {

    }

    @ChromeDevtoolsMethod
    public void screencastFrameAck(JsonRpcPeer peer, JSONObject params) {
        // Nothing to do here, just need to make sure Chrome doesn't get an error that this method
        // isn't implemented
        int ackFrame = 0;
        try {
            ackFrame = params.getInt("sessionId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if ((ackFrame - lastFrame) != 1 ) {
            LogUtil.w("Screencast", "Lost " + (ackFrame - lastFrame) + " frame(s)! current frame is " + ackFrame);
        }
        lastFrame = ackFrame;
    }

    @ChromeDevtoolsMethod
    public void clearGeolocationOverride(JsonRpcPeer peer, JSONObject params) {
    }

    @ChromeDevtoolsMethod
    public void setTouchEmulationEnabled(JsonRpcPeer peer, JSONObject params) {
    }

    @ChromeDevtoolsMethod
    public void setEmulatedMedia(JsonRpcPeer peer, JSONObject params) {
    }

    @ChromeDevtoolsMethod
    public void setShowViewportSizeOnResize(JsonRpcPeer peer, JSONObject params) {
    }

    private static class GetResourceTreeParams implements JsonRpcResult {
        @JsonProperty(required = true)
        public FrameResourceTree frameTree;
    }

    private static class FrameResourceTree {
        @JsonProperty(required = true)
        public Frame frame;

        @JsonProperty
        public List<FrameResourceTree> childFrames;

        @JsonProperty(required = true)
        public List<Resource> resources;
    }

    private static class Frame {
        @JsonProperty(required = true)
        public String id;

        @JsonProperty
        public String parentId;

        @JsonProperty(required = true)
        public String loaderId;

        @JsonProperty
        public String name;

        @JsonProperty(required = true)
        public String url;

        @JsonProperty(required = true)
        public String securityOrigin;

        @JsonProperty(required = true)
        public String mimeType;
    }

    private static class Resource {
        // Incomplete...
    }

    public enum ResourceType {
        DOCUMENT("Document"),
        STYLESHEET("Stylesheet"),
        IMAGE("Image"),
        FONT("Font"),
        SCRIPT("Script"),
        XHR("XHR"),
        WEBSOCKET("WebSocket"),
        OTHER("Other");

        private final String mProtocolValue;

        private ResourceType(String protocolValue) {
            mProtocolValue = protocolValue;
        }

        @JsonValue
        public String getProtocolValue() {
            return mProtocolValue;
        }
    }

    private static class ExecutionContextCreatedParams {
        @JsonProperty(required = true)
        public ExecutionContextDescription context;
    }

    private static class ExecutionContextDescription {
        @JsonProperty(required = true)
        public String frameId;

        @JsonProperty(required = true)
        public int id;
    }

    public static class ScreencastFrameEvent {
        @JsonProperty(required = true)
        public String data;

        @JsonProperty(required = true)
        public ScreencastFrameEventMetadata metadata;

        @JsonProperty(required = true)
        public int sessionId;

        private static AtomicInteger generator = new AtomicInteger();
        public void increment() {
            sessionId = generator.incrementAndGet();
        }
    }

    public static class ScreencastFrameEventMetadata {
        @JsonProperty(required = true)
        public float pageScaleFactor;
        @JsonProperty(required = true)
        public int offsetTop;
        @JsonProperty(required = true)
        public int deviceWidth;
        @JsonProperty(required = true)
        public int deviceHeight;
        @JsonProperty(required = true)
        public int scrollOffsetX;
        @JsonProperty(required = true)
        public int scrollOffsetY;
    }

    public static class StartScreencastRequest {
        @JsonProperty
        public String format;
        @JsonProperty
        public int quality;
        @JsonProperty
        public int maxWidth;
        @JsonProperty
        public int maxHeight;
    }

    public static class NavigationHistory implements JsonRpcResult {
        @JsonProperty(required = true)
        public int currentIndex;
        @JsonProperty(required = true)
        public List<NavigationEntry> entries;
    }

    public static class NavigationEntry {
        @JsonProperty(required = true)
        public int id;
        @JsonProperty(required = true)
        public String url;
        @JsonProperty(required = true)
        public String title;
    }
}
