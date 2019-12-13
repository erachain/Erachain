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

    public TransactionFinalSignsSuitRocksDB(DBASet databaseSet, DB database, boolean sizeEnable) {
        super(databaseSet, database, logger, sizeEnable);
    }

    @Override
    public void openMap() {

        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableTrivial(), new ByteableLong(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(2, 640, 8,
                        56, 30,
                        2, 256, 1, true),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(false, false).setReadaheadSize(100).setFillCache(false),
                databaseSet, sizeEnable);
    }

}
