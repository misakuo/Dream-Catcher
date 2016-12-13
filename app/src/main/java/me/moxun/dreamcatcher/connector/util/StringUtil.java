package me.moxun.dreamcatcher.connector.util;

public final class StringUtil {
  private StringUtil() {
  }

  @SuppressWarnings("StringEquality")
  public static String removePrefix(String string, String prefix, String previousAttempt) {
    if (string != previousAttempt) {
      return previousAttempt;
    } else {
      return removePrefix(string, prefix);
    }
  }

  public static String removePrefix(String string, String prefix) {
    if (string.startsWith(prefix)) {
      return string.substring(prefix.length());
    } else {
      return string;
    }
  }
}
