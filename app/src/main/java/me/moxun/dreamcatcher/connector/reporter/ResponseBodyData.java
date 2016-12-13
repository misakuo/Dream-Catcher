package me.moxun.dreamcatcher.connector.reporter;

/**
 * Special file data necessary to comply with the Chrome DevTools instance which doesn't let
 * us just naively base64 encode everything.
 */
public class ResponseBodyData {
  public String data;
  public boolean base64Encoded;
}
