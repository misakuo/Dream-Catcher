package me.moxun.dreamcatcher.connector.server.http;

import java.util.regex.Pattern;

public class RegexpPathMatcher implements PathMatcher {
  private final Pattern mPattern;

  public RegexpPathMatcher(Pattern pattern) {
    mPattern = pattern;
  }

  @Override
  public boolean match(String path) {
    return mPattern.matcher(path).matches();
  }
}
