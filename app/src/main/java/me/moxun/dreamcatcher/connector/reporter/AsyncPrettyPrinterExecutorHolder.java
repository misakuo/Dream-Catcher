package me.moxun.dreamcatcher.connector.reporter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

/**
 * A holder class for the executor service used for pretty printing related tasks
 */
final class AsyncPrettyPrinterExecutorHolder {

  private static ExecutorService sExecutorService;

  private AsyncPrettyPrinterExecutorHolder() {
  }
  
  public static void ensureInitialized() {
    if (sExecutorService == null) {
      sExecutorService = Executors.newCachedThreadPool();
    }
  }

  @Nullable
  public static ExecutorService getExecutorService() {
    return sExecutorService;
  }

  public static void shutdown() {
    sExecutorService.shutdown();
    sExecutorService = null;
  }
}
