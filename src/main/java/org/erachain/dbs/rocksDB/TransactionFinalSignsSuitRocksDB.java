package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.datachain.TransactionFinalMapSignsSuit;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.mapdb.DB;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

@Slf4j
public class TransactionFinalSignsSuitRocksDB extends DBMapSuit<byte[], Long> implements TransactionFinalMapSignsSuit {

    private final String NAME_TABLE = "TRANSACTION_FINAL_SIGNS_TABLE";

    public TransactionFinalSignsSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, true);
    }

    @Override
    public void openMap() {

        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableTrivial(), new ByteableLong(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(false, false),
                databaseSet, true);
    }

}
