package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

@Slf4j
public class IteratorCloseableImpl<T> implements IteratorCloseable<T> {
  protected Iterator<T> iterator;

  public IteratorCloseableImpl(Iterator<T> iterator) {
    this.iterator = iterator;
  }

  @Override
  public void close() {
  }

  @Override
  public void finalize() throws Throwable {
    super.finalize();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public T next() {
    return iterator.next();
  }
}