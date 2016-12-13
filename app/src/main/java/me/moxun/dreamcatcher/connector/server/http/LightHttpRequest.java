package me.moxun.dreamcatcher.connector.server.http;

import android.net.Uri;

public class LightHttpRequest extends LightHttpMessage {
  public String method;
  public Uri uri;
  public String protocol;

  @Override
  public String toString() {
    return "LightHttpRequest{" +
        "method='" + method + '\'' +
        ", uri=" + uri +
        ", protocol='" + protocol + '\'' +
        '}';
  }

  @Override
  public void reset() {
    super.reset();
    this.method = null;
    this.uri = null;
    this.protocol = null;
  }
}
