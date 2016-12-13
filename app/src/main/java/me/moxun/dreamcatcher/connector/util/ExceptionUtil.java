package me.moxun.dreamcatcher.connector.util;

public class ExceptionUtil {
  @SuppressWarnings("unchecked")
  public static <T extends Throwable> void propagateIfInstanceOf(Throwable t, Class<T> type)
      throws T {
    if (type.isInstance(t)) {
      throw (T)t;
    }
  }

  public static RuntimeException propagate(Throwable t) {
    propagateIfInstanceOf(t, Error.class);
    propagateIfInstanceOf(t, RuntimeException.class);
    throw new RuntimeException(t);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
    throw (T)t;
  }
}
