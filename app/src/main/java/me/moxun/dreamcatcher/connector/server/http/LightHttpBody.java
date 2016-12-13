package me.moxun.dreamcatcher.connector.server.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public abstract class LightHttpBody {
  private static String mBody;

  public static LightHttpBody create(String body, String contentType) {
     mBody = body;
    try {
      return create(body.getBytes("UTF-8"), contentType);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static LightHttpBody create(final byte[] body, final String contentType) {
    return new LightHttpBody() {
      @Override
      public String contentType() {
        return contentType;
      }

      @Override
      public int contentLength() {
        return body.length;
      }

      @Override
      public void writeTo(OutputStream output) throws IOException {
        output.write(body);
      }
    };
  }

  @Override
  public String toString() {
    return mBody;
  }

  public abstract String contentType();

  public abstract int contentLength();

  public abstract void writeTo(OutputStream output) throws IOException;
}
