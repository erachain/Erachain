package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.erachain.dbs.rocksDB.transformation.lists.ByteableStackTuple4;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.util.Stack;

@Slf4j
public class AddressPersonRocksSuit extends DBMapSuit<byte[], Stack<Fun.Tuple4<
        Long, // person key
        Integer, // end_date day
        Integer, // block height
        Integer>>> // transaction
{

    private final String NAME_TABLE = "ADDRESS_PERSON_TABLE";

    public AddressPersonRocksSuit(DBASet databaseSet, DB database, DBTab cover) {
        super(databaseSet, database, logger, false, cover);
    }

    @Override
    public void openMap() {

        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableTrivial(), new ByteableStackTuple4(),
                NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(),
                databaseSet, sizeEnable);

    }

}
