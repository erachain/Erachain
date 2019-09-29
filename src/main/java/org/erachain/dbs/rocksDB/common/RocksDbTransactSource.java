package org.erachain.dbs.rocksDB.common;

public interface RocksDbTransactSource extends RocksDbDataSource {

    void commit();

    void rollback();
}
