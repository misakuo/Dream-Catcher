package me.moxun.dreamcatcher.connector.util;

public interface Accumulator<E> {
  void store(E object);
}
