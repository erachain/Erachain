package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.datachain.ReferenceSuit;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableTransact;
import org.erachain.dbs.rocksDB.transformation.ByteableLongArray;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.mapdb.DB;

@Slf4j
public class ReferenceSuitRocksDB extends DBMapSuit<byte[], long[]> implements ReferenceSuit {

    private final String NAME_TABLE = "REFERENCE_TABLE";

    public ReferenceSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    protected void getMap() {

        map = new DBRocksDBTableTransact<>(new ByteableTrivial(), new ByteableLongArray(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                databaseSet);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
    }
}
