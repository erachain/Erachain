package org.erachain.dbs.rocksDB.common;

import lombok.Getter;
import org.rocksdb.WriteOptions;

/**
 * TODO зачем выделен этот файл, какой функционал он несет, почему нельзя было его встрогить в супер
 * Этот класс позаимствовани из проекта "tron". Скорее всего он использовался для разделения функционала.
 * Можно удалить.
 * Встроить можно все что угодно куда угодно
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