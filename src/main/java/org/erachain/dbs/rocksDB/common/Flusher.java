package org.erachain.dbs.rocksDB.common;

import java.util.Map;

public interface Flusher {

  void flush(Map<byte[], byte[]> rows);

  void flush();

  void close();

  void reset();
}
