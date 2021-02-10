package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.item.assets.Pair;
import org.erachain.database.DBASet;
import org.erachain.datachain.PairSuit;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableTrade;
import org.erachain.dbs.rocksDB.transformation.tuples.ByteableTuple2LongLong;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.util.ArrayList;

/**
 * Хранит сделки на бирже
 * Ключ: ссылка на иницатора + ссылка на цель
 * Значение - Сделка
 * Initiator DBRef (Long) + Target DBRef (Long) -> Trade
 */

@Slf4j
public class PairSuitRocksDB extends DBMapSuit<Tuple2<Long, Long>, Pair> implements PairSuit {

    private final String NAME_TABLE = "PAIRS_TABLE";

    public PairSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, false);
    }

    @Override
    public void openMap() {

        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableTuple2LongLong(), new ByteableTrade(),
                NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(),
                databaseSet, sizeEnable);
    }

    @Override
    protected void createIndexes() {
        // SIZE need count - make not empty LIST
        indexes = new ArrayList<>();

    }

    static void makeKey(byte[] buffer, long have, long want) {

        if (have > want) {
            System.arraycopy(Ints.toByteArray((int) have), 0, buffer, 0, 8);
            System.arraycopy(Ints.toByteArray((int) want), 0, buffer, 8, 8);
        } else {
            System.arraycopy(Ints.toByteArray((int) want), 0, buffer, 0, 8);
            System.arraycopy(Ints.toByteArray((int) have), 0, buffer, 8, 8);
        }

    }

}
