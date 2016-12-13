package me.moxun.dreamcatcher.connector.jsonrpc.protocol;

import android.annotation.SuppressLint;

import org.json.JSONObject;

import me.moxun.dreamcatcher.connector.json.annotation.JsonProperty;

@SuppressLint({"UsingDefaultJsonDeserializer", "EmptyJsonPropertyUse"})
public class JsonRpcRequest {
    /**
     * This field is not required so that we can support JSON-RPC "notification" requests.
     */
    @JsonProperty
    public Long id;

    @JsonProperty(required = true)
    public String method;

    @JsonProperty
    public JSONObject params;

    public JsonRpcRequest() {
    }

    @Override
    public String toString() {
        return "Id:" + id + ",method:" + method + ",params:" + params;
    }

    public JsonRpcRequest(Long id, String method, JSONObject params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }
}
