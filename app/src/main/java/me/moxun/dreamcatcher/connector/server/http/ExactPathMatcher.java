package me.moxun.dreamcatcher.connector.server.http;

public class ExactPathMatcher implements PathMatcher {
  private final String mPath;

  public ExactPathMatcher(String path) {
    mPath = path;
  }

  @Override
  public String toString() {
    return "ExactPathMatcher{" +
        "mPath='" + mPath + '\'' +
        '}';
  }

  @Override
  public boolean match(String path) {
    return mPath.equals(path);
  }
}
