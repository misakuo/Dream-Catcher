package me.moxun.dreamcatcher.connector.console;


import javax.annotation.Nullable;

import me.moxun.dreamcatcher.connector.inspector.helper.ChromePeerManager;

public class ConsolePeerManager extends ChromePeerManager {

  private static ConsolePeerManager sInstance;

  private ConsolePeerManager() {
    super();
  }

  @Nullable
  public static synchronized ConsolePeerManager getInstanceOrNull() {
    return sInstance;
  }

  public static synchronized ConsolePeerManager getOrCreateInstance() {
    if (sInstance == null) {
      sInstance = new ConsolePeerManager();
    }
    return sInstance;
  }
}
