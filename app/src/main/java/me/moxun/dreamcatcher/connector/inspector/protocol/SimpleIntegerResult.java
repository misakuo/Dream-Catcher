package me.moxun.dreamcatcher.connector.inspector.protocol;


import me.moxun.dreamcatcher.connector.json.annotation.JsonProperty;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcResult;

/**
 * Created by moxun on 16/11/17.
 */

public class SimpleIntegerResult implements JsonRpcResult {
    @JsonProperty(required = true)
    public int result;

    public SimpleIntegerResult() {
    }

    public SimpleIntegerResult(int result) {
        this.result = result;
    }
}
