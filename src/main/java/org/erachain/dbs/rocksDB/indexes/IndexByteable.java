package org.erachain.dbs.rocksDB.indexes;

public interface IndexByteable<R, K> {
    byte[] toBytes(R result, K key);
}
