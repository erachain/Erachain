package org.erachain.dbs.rocksDB.common;

import java.util.Map;

/**
 * TODO зачем выделен этот файл, какой функционал он несет, почему нельзя было его встрогить в супер
 * Почему его в DB не вставить?
 */
public interface Flusher {

  void flush(Map<byte[], byte[]> rows);

  void flush();

}
