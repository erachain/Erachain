package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.database.DBASet;
import org.erachain.datachain.TradeMapSuit;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.erachain.dbs.rocksDB.transformation.ByteableTrade;
import org.erachain.dbs.rocksDB.transformation.tuples.ByteableTuple2LongLong;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

@Slf4j
public class TradeSuitRocksDB extends DBMapSuit<Tuple2<Long, Long>, Trade> implements TradeMapSuit {

    private final String NAME_TABLE = "TRADES_TABLE";
    private final String tradesKeyPairIndexName = "tradesKeyPair";
    private final String tradesKeyWantIndexName = "tradesKeyWant";
    private final String tradesKeyHaveIndexName = "tradesKeyHave";
    private final String tradesKeyReverseIndexName = "tradesKeyReverse";

    SimpleIndexDB<Tuple2<Long, Long>, Trade, byte[]> pairIndex;
    SimpleIndexDB<Tuple2<Long, Long>, Trade, byte[]> wantIndex;
    SimpleIndexDB<Tuple2<Long, Long>, Trade, Tuple3<String, Long, Integer>> haveIndex;
    ListIndexDB<Tuple2<Long, Long>, Trade, Tuple2<Long, Long>> reverseIndex;

    public TradeSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    protected void openMap() {

        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableTuple2LongLong(), new ByteableTrade(),
                    NAME_TABLE, indexes,
                    RocksDbSettings.initCustomSettings(7, 64, 32,
                            256, 10,
                            1, 256, 32, false),
                    new WriteOptions().setSync(true).setDisableWAL(false),
                    new ReadOptions(),
                    databaseSet);
    }

    @Override
    protected void createIndexes() {
        // SIZE need count - make not empty LIST
        indexes = new ArrayList<>();

        ///////////////////////////// HERE PROTOCOL INDEXES

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        //////////////// NOT PROTOCOL INDEXES

        pairIndex = new SimpleIndexDB<>(
                tradesKeyPairIndexName,
                (key, value) -> {
                    long have = value.getHaveKey();
                    long want = value.getWantKey();
                    byte[] buffer = receivePairKey(have, want).getBytes(StandardCharsets.UTF_8);
                    buffer = Longs.toByteArray(Long.MAX_VALUE - value.getInitiator());
                    buffer = Ints.toByteArray(Integer.MAX_VALUE - value.getSequence());
                    return buffer;
                }, (result, key) -> result);

        wantIndex = new SimpleIndexDB<>(
                tradesKeyWantIndexName,
                (key, value) -> {
                    byte[] buffer = Longs.toByteArray(value.getWantKey());
                    buffer = Longs.toByteArray(Long.MAX_VALUE - value.getInitiator());
                    buffer = Ints.toByteArray(Integer.MAX_VALUE - value.getSequence());
                    return buffer;
                }
                , (result, key) -> result);


        haveIndex = new SimpleIndexDB<>(
                tradesKeyHaveIndexName,
                (key, value) -> {
                    long have = value.getHaveKey();
                    String haveKey;
                    haveKey = String.valueOf(have);
                    return new Tuple3<>(
                            haveKey,
                            Long.MAX_VALUE - value.getInitiator(),
                            Integer.MAX_VALUE - value.getSequence());
                }
                , (result, key) -> org.bouncycastle.util.Arrays.concatenate(
                new ByteableString().toBytesObject(result.a),
                new ByteableLong().toBytesObject(result.b),
                new ByteableInteger().toBytesObject(result.c)));

        reverseIndex = new ListIndexDB<>(
                tradesKeyReverseIndexName,
                (key, value) -> new ArrayList<Tuple2<Long, Long>>() {{
                    add(new Tuple2<>(key.b, key.a));
                    add(key);
                }},
                (result, key) -> new ByteableTuple2LongLong().toBytesObject(result));
        indexes = new ArrayList<>();
        indexes.add(pairIndex);
        indexes.add(haveIndex);
        indexes.add(wantIndex);
        indexes.add(reverseIndex);
    }


    public void add(Trade trade) {
        this.put(new Tuple2<>(trade.getInitiator(), trade.getTarget()), trade);
    }

    private String receivePairKey(long have, long want) {
        String pairKey;
        if (have > want) {
            pairKey = have + "/" + want;
        } else {
            pairKey = want + "/" + have;
        }
        return pairKey;
    }


    public void delete(Trade trade) {
        delete(new Tuple2<>(trade.getInitiator(), trade.getTarget()));
    }


    @Override
    public Iterator<Tuple2> getIterator(Order order) {
        return null;
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getReverseIterator(Long orderID) {
        return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(reverseIndex.getColumnFamilyHandle(), Longs.toByteArray(orderID), false);
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getHaveIterator(long have) {
        return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(haveIndex.getColumnFamilyHandle(), Longs.toByteArray(have), false);
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getWantIterator(long want) {
        return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(wantIndex.getColumnFamilyHandle(), Longs.toByteArray(want), false);
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getPairIterator(long have, long want) {
        return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(pairIndex.getColumnFamilyHandle(), Longs.toByteArray(want), false);
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getPairTimestampIterator(long have, long want, long timestamp) {
        return null;
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getPairHeightIterator(long have, long want, int heightStart) {
        return null;
    }
}
