package me.moxun.dreamcatcher.connector.util;

import java.util.ArrayList;

public final class ArrayListAccumulator<E> extends ArrayList<E> implements Accumulator<E> {
  @Override
  public void store(E object) {
    add(object);
  }
}
