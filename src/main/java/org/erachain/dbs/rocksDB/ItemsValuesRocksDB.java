package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableByte;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.erachain.dbs.rocksDB.transformation.tuples.ByteableTuple3;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

@Slf4j
public class ItemsValuesRocksDB extends DBMapSuit<Fun.Tuple3<Long, Byte, Byte>, byte[]> {

    private final String NAME_TABLE = "ITEMS_VALUES_TABLE";

    public ItemsValuesRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, false);
    }

    @Override
    public void openMap() {

        ByteableTuple3<Long, Byte, Byte> byteableElement = new ByteableTuple3<Long, Byte, Byte>() {
            @Override
            public int[] sizeElements() {
                return new int[]{Integer.BYTES, Byte.BYTES, Byte.BYTES};
            }
        };
        byteableElement.setByteables(new Byteable[]{new ByteableInteger(), new ByteableByte(), new ByteableByte()})

        map = new DBRocksDBTableDBCommitedAsBath<>(byteableElement,
                new ByteableTrivial(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(),
                databaseSet, sizeEnable);
    }

}
