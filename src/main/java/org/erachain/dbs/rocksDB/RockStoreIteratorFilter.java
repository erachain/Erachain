package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksIterator;

import java.util.NoSuchElementException;

import static org.erachain.utils.ByteArrayUtils.areEqualMask;

@Slf4j
public final class RockStoreIteratorFilter extends RockStoreIterator {

  byte[] filter;

  public RockStoreIteratorFilter(RocksIterator dbIterator, boolean descending, boolean isIndex, byte[] filter) {
    super(dbIterator, descending, isIndex);
    this.filter = filter;
  }

  @Override
  public boolean hasNext() {
    boolean hasNext = false;
    try {
      if (jumpToLast) {
        if (filter != null) {

          // тут нужно взять кранее верхнее значени и найти нижнее первое
          // см. https://github.com/facebook/rocksdb/wiki/SeekForPrev
          int length = filter.length;
          byte[] prevFilter = new byte[length + 1];
          System.arraycopy(filter, 0, prevFilter, 0, length);
          prevFilter[length] = (byte) 255;
          dbIterator.seekForPrev(prevFilter);

        } else {
          dbIterator.seekToLast();
        }
        jumpToLast = false;
        first = false;
      }
      if (first) {
        if (filter != null) {
          dbIterator.seek(filter);
        } else {
          dbIterator.seekToFirst();
        }
        first = false;
      }
      if (!(hasNext = dbIterator.isValid())) {
        dbIterator.close();
      }
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
      try {
        dbIterator.close();
      } catch (Exception e1) {
        logger.error(e1.getMessage(),e1);
      }
    }

    return hasNext && (filter == null || areEqualMask(dbIterator.key(), filter));

  }

  @Override
  public byte[] next() {
    if (!dbIterator.isValid()) {
      throw new NoSuchElementException();
    }
    byte[] key;
    if (isIndex) {
      key = dbIterator.value();
    } else {
      key = dbIterator.key();
    }
    if (descending) {
      dbIterator.prev();
    } else {
      dbIterator.next();
    }
    return key;
  }
}