package me.moxun.dreamcatcher.connector.inspector.protocol;


import me.moxun.dreamcatcher.connector.json.annotation.JsonProperty;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcResult;

/**
 * Created by moxun on 16/12/1.
 */

public class SimpleStringResult implements JsonRpcResult {
    @JsonProperty(required = true)
    public String data;

    public SimpleStringResult() {
    }

    public SimpleStringResult(String data) {
        this.data = data;
    }
}
