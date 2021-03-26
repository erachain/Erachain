package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class IteratorCloseableImpl<T> implements IteratorCloseable<T> {
  protected Iterator<? extends T> iterator;
  protected boolean isClosed;

  protected IteratorCloseableImpl() {
  }

  public IteratorCloseableImpl(Iterator<? extends T> iterator) {
    this.iterator = iterator;
  }

  /**
   * Ii already IteratorCloseable - return it else make new class IteratorCloseable(iterator)
   *
   * @param iterator
   * @param <T>
   * @return
   */
  // делаем или без изменений вернем если там уже нужный класс
  public static <T> IteratorCloseableImpl<T> make(
          Iterator<? extends T> iterator) {
    if (iterator instanceof IteratorCloseableImpl) {
      // Safe to cast <? extends T> to <T> because Impl only uses T
      // covariantly (and cannot be subclassed to add non-covariant uses).
      @SuppressWarnings("unchecked")
      IteratorCloseableImpl<T> closable = (IteratorCloseableImpl<T>) iterator;
      return closable;
    }
    return new IteratorCloseableImpl<T>(iterator);
  }

  /// нужно обязательно освобождать память, см https://github.com/facebook/rocksdb/wiki/RocksJava-Basics
  @Override
  public void close() {
    if (iterator instanceof IteratorCloseable) {
      try {
        ((IteratorCloseable) iterator).close();
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      }
    }
    isClosed = true;
  }

  @Override
  public void finalize() throws Throwable {
    if (!isClosed) {
      close();
      logger.warn("FINALIZE used - " + iterator.getClass().getName());
    }

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


  public static <T> IteratorCloseable<T> limit(
          final IteratorCloseable<T> iterator, final int limitSize) {
    checkNotNull(iterator);
    checkArgument(limitSize >= 0, "limit is negative");
    return new IteratorCloseableImpl<T>(iterator) {
      private int count;

      @Override
      public boolean hasNext() {
        return count < limitSize && iterator.hasNext();
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        count++;
        return iterator.next();
      }

      @Override
      public void remove() {
        iterator.remove();
      }

      @Override
      public void close() {
        try {
          ((IteratorCloseable) iterator).close();
        } catch (IOException e) {
          logger.error(e.getMessage(), e);
        }
        isClosed = true;
      }

      @Override
      public void finalize() throws Throwable {
        if (!isClosed) {
          close();
          logger.warn("FINALIZE used - " + iterator.getClass().getName());
        }

        super.finalize();
      }

    };

  }

}