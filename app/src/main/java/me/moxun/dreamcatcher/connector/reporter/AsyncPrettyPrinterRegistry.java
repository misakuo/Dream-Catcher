package me.moxun.dreamcatcher.connector.reporter;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class AsyncPrettyPrinterRegistry {

  private final Map<String, AsyncPrettyPrinterFactory> mRegistry = new HashMap<>();

  public synchronized void register(String headerName, AsyncPrettyPrinterFactory factory) {
    mRegistry.put(headerName, factory);
  }

  @Nullable
  public synchronized AsyncPrettyPrinterFactory lookup(String headerName) {
    return mRegistry.get(headerName);
  }

  public synchronized boolean unregister(String headerName) {
    return mRegistry.remove(headerName) != null;
  }
}
