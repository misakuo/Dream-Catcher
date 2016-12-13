package me.moxun.dreamcatcher.connector.jsonrpc;


import me.moxun.dreamcatcher.connector.jsonrpc.protocol.JsonRpcResponse;

/**
 * Marker interface used to denote a JSON-RPC result.  After conversion from Jackson,
 * this will be placed into {@link JsonRpcResponse#result}.
 */
public interface JsonRpcResult {
}
