package org.erachain.dbs.rocksDB.common;

public interface RocksDbTransactSource {
    void commit();

    void rollback();
}
