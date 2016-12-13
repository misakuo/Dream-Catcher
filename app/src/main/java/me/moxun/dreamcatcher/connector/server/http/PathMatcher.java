package me.moxun.dreamcatcher.connector.server.http;

public interface PathMatcher {
  boolean match(String path);
}
