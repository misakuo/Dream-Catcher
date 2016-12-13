package me.moxun.dreamcatcher.connector.inspector.protocol.module;

import android.annotation.SuppressLint;

import org.json.JSONObject;

import java.util.List;

import me.moxun.dreamcatcher.connector.console.ConsolePeerManager;
import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsDomain;
import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsMethod;
import me.moxun.dreamcatcher.connector.json.annotation.JsonProperty;
import me.moxun.dreamcatcher.connector.json.annotation.JsonValue;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcPeer;

public class Console implements ChromeDevtoolsDomain {
    public Console() {
    }

    @ChromeDevtoolsMethod
    public void enable(JsonRpcPeer peer, JSONObject params) {
        ConsolePeerManager.getOrCreateInstance().addPeer(peer);
    }

    @ChromeDevtoolsMethod
    public void disable(JsonRpcPeer peer, JSONObject params) {
        ConsolePeerManager.getOrCreateInstance().removePeer(peer);
    }

    @SuppressLint({"UsingDefaultJsonDeserializer", "EmptyJsonPropertyUse"})
    public static class MessageAddedRequest {
        @JsonProperty(required = true)
        public ConsoleMessage message;
    }

    @SuppressLint({"UsingDefaultJsonDeserializer", "EmptyJsonPropertyUse"})
    public static class ConsoleMessage {
        @JsonProperty(required = true)
        public MessageSource source;

        @JsonProperty(required = true)
        public MessageLevel level;

        @JsonProperty(required = true)
        public String text;

        @JsonProperty(required = false)
        public MessageType type;

        @JsonProperty(required = false)
        public List<Runtime.RemoteObject> parameters;
    }

    public enum MessageSource {
        XML("xml"),
        JAVASCRIPT("javascript"),
        NETWORK("network"),
        CONSOLE_API("console-api"),
        STORAGE("storage"),
        APPCACHE("appcache"),
        RENDERING("rendering"),
        CSS("css"),
        SECURITY("security"),
        OTHER("other");

        private final String mProtocolValue;

        private MessageSource(String protocolValue) {
            mProtocolValue = protocolValue;
        }

        @JsonValue
        public String getProtocolValue() {
            return mProtocolValue;
        }
    }

    public enum MessageLevel {
        LOG("log"),
        INFO("info"),
        WARNING("warning"),
        ERROR("error"),
        DEBUG("debug");

        private final String mProtocolValue;

        private MessageLevel(String protocolValue) {
            mProtocolValue = protocolValue;
        }

        @JsonValue
        public String getProtocolValue() {
            return mProtocolValue;
        }
    }

    public enum MessageType {
        LOG("log"),
        TABLE("table"),
        GROUP_START("startGroup"),
        GROUP_END("endGroup"),
        DIR("dir");

        private final String mProtocolValue;

        private MessageType(String protocolValue) {
            mProtocolValue = protocolValue;
        }

        @JsonValue
        public String getProtocolValue() {
            return mProtocolValue;
        }
    }

    @SuppressLint({"UsingDefaultJsonDeserializer", "EmptyJsonPropertyUse"})
    public static class CallFrame {
        @JsonProperty(required = true)
        public String functionName;

        @JsonProperty(required = true)
        public String url;

        @JsonProperty(required = true)
        public int lineNumber;

        @JsonProperty(required = true)
        public int columnNumber;

        public CallFrame() {
        }

        public CallFrame(String functionName, String url, int lineNumber, int columnNumber) {
            this.functionName = functionName;
            this.url = url;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }
    }
}
