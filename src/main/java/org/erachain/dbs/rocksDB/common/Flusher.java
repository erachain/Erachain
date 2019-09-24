package org.erachain.dbs.rocksDB.common;

import java.util.Map;

/**
 * TODO ??
 */
public interface Flusher {

  void flush(Map<byte[], byte[]> rows);

  void flush();

}
