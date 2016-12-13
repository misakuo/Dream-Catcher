package me.moxun.dreamcatcher.connector.reporter;

import android.content.Context;

import javax.annotation.Nullable;

import me.moxun.dreamcatcher.connector.inspector.helper.ChromePeerManager;
import me.moxun.dreamcatcher.connector.inspector.helper.PeersRegisteredListener;
import me.moxun.dreamcatcher.connector.util.Util;

public class NetworkPeerManager extends ChromePeerManager {
  private static NetworkPeerManager sInstance;

  private final ResponseBodyFileManager mResponseBodyFileManager;
  private AsyncPrettyPrinterInitializer mPrettyPrinterInitializer;
  private AsyncPrettyPrinterRegistry mAsyncPrettyPrinterRegistry;

  @Nullable
  public static synchronized NetworkPeerManager getInstanceOrNull() {
    return sInstance;
  }

  public static synchronized NetworkPeerManager getOrCreateInstance(Context context) {
    if (sInstance == null) {
      sInstance = new NetworkPeerManager(
          new ResponseBodyFileManager(
              context.getApplicationContext()));
    }
    return sInstance;
  }

  public NetworkPeerManager(
      ResponseBodyFileManager responseBodyFileManager) {
    mResponseBodyFileManager = responseBodyFileManager;
    setListener(mTempFileCleanup);
  }

  public ResponseBodyFileManager getResponseBodyFileManager() {
    return mResponseBodyFileManager;
  }

  @Nullable
  public AsyncPrettyPrinterRegistry getAsyncPrettyPrinterRegistry() {
    return mAsyncPrettyPrinterRegistry;
  }

  public void setPrettyPrinterInitializer(AsyncPrettyPrinterInitializer initializer) {
    Util.throwIfNotNull(mPrettyPrinterInitializer);
    mPrettyPrinterInitializer = Util.throwIfNull(initializer);
  }

  private final PeersRegisteredListener mTempFileCleanup = new PeersRegisteredListener() {
    @Override
    protected void onFirstPeerRegistered() {
      AsyncPrettyPrinterExecutorHolder.ensureInitialized();
      if (mAsyncPrettyPrinterRegistry == null && mPrettyPrinterInitializer != null) {
        mAsyncPrettyPrinterRegistry = new AsyncPrettyPrinterRegistry();
        mPrettyPrinterInitializer.populatePrettyPrinters(mAsyncPrettyPrinterRegistry);
      }
      mResponseBodyFileManager.cleanupFiles();
    }

    @Override
    protected void onLastPeerUnregistered() {
      mResponseBodyFileManager.cleanupFiles();
      AsyncPrettyPrinterExecutorHolder.shutdown();
    }
  };
}
