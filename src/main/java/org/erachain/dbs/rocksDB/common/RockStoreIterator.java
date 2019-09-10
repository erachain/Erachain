package org.erachain.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksIterator;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

@Slf4j
public final class RockStoreIterator implements DBIterator {

  private boolean jumpToLast;
  private boolean isIndex;
  private RocksIterator dbIterator;
  private boolean descending;
  private boolean first = true;

  public RockStoreIterator(RocksIterator dbIterator, boolean descending, boolean isIndex) {
    this.dbIterator = dbIterator;
    this.descending = descending;
    jumpToLast = descending;
    this.isIndex = isIndex;
  }

  @Override
  public void close() {
    dbIterator.close();
  }

  @Override
  public boolean hasNext() {
    boolean hasNext = false;
    try {
      if (jumpToLast) {
        dbIterator.seekToLast();
        jumpToLast = false;
        first = false;
      }
      if (first) {
        dbIterator.seekToFirst();
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
    return hasNext;
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