package org.erachain.dbs.rocksDB.common;

import lombok.Getter;
import org.rocksdb.WriteOptions;

/**
 *  TODO ??
 */
public class WriteOptionsWrapper {

  @Getter
  private org.rocksdb.WriteOptions rocks = null;

  public static WriteOptionsWrapper getInstance() {
    WriteOptionsWrapper wrapper = new WriteOptionsWrapper();
    wrapper.rocks = new WriteOptions();
    return wrapper;
  }

  // TODO ??
  public WriteOptionsWrapper sync(boolean bool) {
    rocks.setSync(bool);
    return this;
  }
}