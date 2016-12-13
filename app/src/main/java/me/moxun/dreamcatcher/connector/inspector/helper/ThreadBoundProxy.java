package me.moxun.dreamcatcher.connector.inspector.helper;


import me.moxun.dreamcatcher.connector.util.ThreadBound;
import me.moxun.dreamcatcher.connector.util.UncheckedCallable;
import me.moxun.dreamcatcher.connector.util.Util;

/**
 * This class is for those cases when a class' threading
 * policy is determined by one of its member variables.
 */
public abstract class ThreadBoundProxy implements ThreadBound {
  private final ThreadBound mEnforcer;

  public ThreadBoundProxy(ThreadBound enforcer) {
    mEnforcer = Util.throwIfNull(enforcer);
  }

  @Override
  public final boolean checkThreadAccess() {
    return mEnforcer.checkThreadAccess();
  }

  @Override
  public final void verifyThreadAccess() {
    mEnforcer.verifyThreadAccess();
  }

  @Override
  public final <V> V postAndWait(UncheckedCallable<V> c) {
    return mEnforcer.postAndWait(c);
  }

  @Override
  public final void postAndWait(Runnable r) {
    mEnforcer.postAndWait(r);
  }

  @Override
  public final void postDelayed(Runnable r, long delayMillis) {
    mEnforcer.postDelayed(r, delayMillis);
  }

  @Override
  public final void removeCallbacks(Runnable r) {
    mEnforcer.removeCallbacks(r);
  }
}
