package me.moxun.dreamcatcher.connector.inspector.protocol.module;

import com.pandora.appex.inspector.protocol.ChromeDevtoolsDomain;
import com.pandora.appex.inspector.protocol.ChromeDevtoolsMethod;
import com.pandora.appex.json.annotation.JsonProperty;
import com.pandora.appex.jsonrpc.JsonRpcPeer;
import com.pandora.appex.jsonrpc.JsonRpcResult;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

public class Profiler implements ChromeDevtoolsDomain {
  public Profiler() {
  }

  @ChromeDevtoolsMethod
  public void enable(JsonRpcPeer peer, JSONObject params) {
  }

  @ChromeDevtoolsMethod
  public void disable(JsonRpcPeer peer, JSONObject params) {
  }

  @ChromeDevtoolsMethod
  public void stop(JsonRpcPeer peer, JSONObject params) {
  }

  @ChromeDevtoolsMethod
  public void setSamplingInterval(JsonRpcPeer peer, JSONObject params) {
  }

  @ChromeDevtoolsMethod
  public JsonRpcResult getProfileHeaders(JsonRpcPeer peer, JSONObject params) {
    ProfileHeaderResponse response = new ProfileHeaderResponse();
    response.headers = Collections.emptyList();
    return response;
  }

  private static class ProfileHeaderResponse implements JsonRpcResult {
    @JsonProperty(required = true)
    public List<ProfileHeader> headers;
  }

  private static class ProfileHeader {
    @JsonProperty(required = true)
    String typeId;

    @JsonProperty(required = true)
    String title;

    @JsonProperty(required = true)
    int uid;
  }
}
