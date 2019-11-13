package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.TradeSuit;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableTrade;
import org.erachain.dbs.rocksDB.transformation.tuples.ByteableTuple2LongLong;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
public class TradeSuitRocksDB extends DBMapSuit<Tuple2<Long, Long>, Trade> implements TradeSuit {

    private final String NAME_TABLE = "TRADES_TABLE";
    private final String tradesKeyPairIndexName = "tradesKeyPair";
    private final String tradesKeyWantIndexName = "tradesKeyWant";
    private final String tradesKeyHaveIndexName = "tradesKeyHave";
    private final String tradesKeyReverseIndexName = "tradesKeyReverse";

    SimpleIndexDB<Tuple2<Long, Long>, Trade, byte[]> pairIndex;
    SimpleIndexDB<Tuple2<Long, Long>, Trade, byte[]> wantIndex;
    SimpleIndexDB<Tuple2<Long, Long>, Trade, byte[]> haveIndex;
    SimpleIndexDB<Tuple2<Long, Long>, Trade, byte[]> reverseIndex;

    public TradeSuitRocksDB(DBASet databaseSet, DB database) {
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

                    /// нельзя! иначе строки длиньше тоже будет воспринимать как подходящие!
                    // byte[] buffer1 = TradeSuit.makeKey(have, want).getBytes(StandardCharsets.UTF_8);
                    ///byte[] buffer1 = TradeSuit.makeKey(have, want).getBytes(StandardCharsets.UTF_8);
                    //System.arraycopy(buffer1, 0, buffer, 0, buffer1.length);
                    //System.arraycopy(Longs.toByteArray(Long.MAX_VALUE - value.getInitiator()),
                    //        0, buffer, buffer1.length, 8);
                    //System.arraycopy(Ints.toByteArray(Integer.MAX_VALUE - value.getSequence()),
                    //        0, buffer, buffer1.length + 8, 4);

                    byte[] filter = new byte[28];
                    makeKey(filter, have, want);
                    System.arraycopy(Longs.toByteArray(Long.MAX_VALUE - value.getInitiator()),
                            0, filter, 16, 8);
                    System.arraycopy(Ints.toByteArray(Integer.MAX_VALUE - value.getSequence()),
                            0, filter, 24, 4);

                    return filter;
                }, (result) -> result);

        haveIndex = new SimpleIndexDB<>(
                tradesKeyHaveIndexName,
                (key, value) -> {
                    byte[] buffer = new byte[20];
                    System.arraycopy(Longs.toByteArray(value.getHaveKey()),
                            0, buffer, 0, 8);
                    System.arraycopy(Longs.toByteArray(Long.MAX_VALUE - value.getInitiator()),
                            0, buffer, 8, 8);
                    System.arraycopy(Ints.toByteArray(Integer.MAX_VALUE - value.getSequence()),
                            0, buffer, 16, 4);
                    return buffer;
                }, (result) -> result);

        wantIndex = new SimpleIndexDB<>(
                tradesKeyWantIndexName,
                (key, value) -> {
                    byte[] buffer = new byte[20];
                    System.arraycopy(Longs.toByteArray(value.getWantKey()),
                            0, buffer, 0, 8);
                    System.arraycopy(Longs.toByteArray(Long.MAX_VALUE - value.getInitiator()),
                            0, buffer, 8, 8);
                    System.arraycopy(Ints.toByteArray(Integer.MAX_VALUE - value.getSequence()),
                            0, buffer, 16, 4);
                    return buffer;
                }, (result) -> result);

        reverseIndex = new SimpleIndexDB<>(
                tradesKeyReverseIndexName,
                (key, value) -> {
                    byte[] buffer = new byte[16];
                    System.arraycopy(Longs.toByteArray(key.b), 0, buffer, 0, 8);
                    System.arraycopy(Longs.toByteArray(key.a), 0, buffer, 8, 8);
                    return buffer;
                }, (result) -> result);

        indexes.add(pairIndex);
        indexes.add(haveIndex);
        indexes.add(wantIndex);
        indexes.add(reverseIndex);
    }

    static void makeKey(byte[] buffer, long have, long want) {

        if (have > want) {
            System.arraycopy(Longs.toByteArray(have), 0, buffer, 0, 8);
            System.arraycopy(Longs.toByteArray(want), 0, buffer, 8, 8);
        } else {
            System.arraycopy(Longs.toByteArray(want), 0, buffer, 0, 8);
            System.arraycopy(Longs.toByteArray(have), 0, buffer, 8, 8);
        }

    }

    public void add(Trade trade) {
        this.put(new Tuple2<>(trade.getInitiator(), trade.getTarget()), trade);
    }

    public void delete(Trade trade) {
        delete(new Tuple2<>(trade.getInitiator(), trade.getTarget()));
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getIterator(Order order) {
        return map.getIndexIteratorFilter(Longs.toByteArray(order.getId()), false, false);
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getIteratorByKeys(Long orderID) {
        // тут нужно не Индекс включать
        return map.getIndexIteratorFilter(Longs.toByteArray(orderID), false, false);
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getTargetsIterator(Long orderID) {
        return map.getIndexIteratorFilter(reverseIndex.getColumnFamilyHandle(), Longs.toByteArray(orderID), false, true);
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getHaveIterator(long have) {
        return map.getIndexIteratorFilter(haveIndex.getColumnFamilyHandle(), Longs.toByteArray(have), false, true);
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getWantIterator(long want) {
        return map.getIndexIteratorFilter(wantIndex.getColumnFamilyHandle(), Longs.toByteArray(want), false, true);
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getPairIterator(long have, long want) {
        //byte[] filter = TradeSuit.makeKey(have, want).getBytes(StandardCharsets.UTF_8);
        byte[] filter = new byte[16];
        makeKey(filter, have, want);
        return map.getIndexIteratorFilter(pairIndex.getColumnFamilyHandle(), filter, false, true);
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getPairTimestampIterator(long have, long want, long timestamp) {
        return null;
    }

    @Override
    public Iterator<Tuple2<Long, Long>> getPairHeightIterator(long have, long want, int heightStart) {

        // тут индекс не по времени а по номерам блоков как лонг
        ///int heightStart = Controller.getInstance().getMyHeight();
        //// с последнего -- long refDBstart = Transaction.makeDBRef(heightStart, 0);
        int heightEnd = heightStart - BlockChain.BLOCKS_PER_DAY(heightStart);
        long refDBend = Transaction.makeDBRef(heightEnd, 0);

        ///byte[] filter = TradeSuit.makeKey(have, want).getBytes(StandardCharsets.UTF_8);
        byte[] filter = new byte[16];
        makeKey(filter, have, want);
        Iterator<Tuple2<Long, Long>> iterator = map.getIndexIteratorFilter(pairIndex.getColumnFamilyHandle(), filter, false, true);

        Set<Tuple2<Long, Long>> keys = new TreeSet<Tuple2<Long, Long>>();
        while (iterator.hasNext()) {
            Tuple2<Long, Long> key = iterator.next();
            if (key.a > refDBend)
                break;
            keys.add(key);
        }

        return keys.iterator();

    }
}
