package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.TradeSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
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
public class TradeSuitRocksDB extends DBMapSuit<Tuple2<Long, Long>, Trade> implements TradeSuit {

    SimpleIndexDB<Tuple2<Long, Long>, Trade, byte[]> pairIndex;
    ArrayIndexDB<Tuple2<Long, Long>, Trade, byte[]> assetIndex;
    SimpleIndexDB<Tuple2<Long, Long>, Trade, byte[]> reverseIndex;

    public TradeSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, false);
    }

    @Override
    public void openMap() {

        String NAME_TABLE = "TRADES_TABLE";
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

        String tradesKeyPairIndexName = "tradesKeyPair";
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
                    // обратная сортировка поэтому все вычитаем Однако тут по другому минусы учитываются - они больше чем положительные числа!
                    // поэтому нужно еще делать корректировку как у Чисел
                    System.arraycopy(Longs.toByteArray(Long.MAX_VALUE - value.getInitiator()),
                            0, filter, 16, 8);
                    System.arraycopy(Ints.toByteArray(Integer.MAX_VALUE - value.getSequence()),
                            0, filter, 24, 4);

                    return filter;
                }, (result) -> result);

        String tradesAssetKeyIndexName = "tradesAssetKey";
        assetIndex = new ArrayIndexDB<>(
                tradesAssetKeyIndexName,
                (key, trade) -> {
                    byte[][] keys = new byte[2][];
                    keys[0] = Longs.toByteArray(trade.getHaveKey());
                    keys[1] = Longs.toByteArray(trade.getWantKey());
                    return keys;
                }, (result) -> result);

        String tradesKeyReverseIndexName = "tradesKeyReverse";
        reverseIndex = new SimpleIndexDB<>(
                tradesKeyReverseIndexName,
                (key, value) -> {
                    byte[] buffer = new byte[16];
                    System.arraycopy(Longs.toByteArray(key.b), 0, buffer, 0, 8);
                    System.arraycopy(Longs.toByteArray(key.a), 0, buffer, 8, 8);
                    return buffer;
                }, (result) -> result);

        indexes.add(pairIndex);
        indexes.add(assetIndex);
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
    public IteratorCloseable<Tuple2<Long, Long>> getIteratorByInitiator(Long orderID, boolean descending) {
        return map.getIndexIteratorFilter(Longs.toByteArray(orderID), descending, false);
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getIteratorByTarget(Long orderID, boolean descending) {
        return map.getIndexIteratorFilter(reverseIndex.getColumnFamilyHandle(), Longs.toByteArray(orderID), descending, true);
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getAssetIterator(long assetKey, boolean descending) {
        return map.getIndexIteratorFilter(assetIndex.getColumnFamilyHandle(), Longs.toByteArray(assetKey), descending, true);
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairIteratorDesc(long have, long want) {
        byte[] filter = new byte[16];
        makeKey(filter, have, want);
        return map.getIndexIteratorFilter(pairIndex.getColumnFamilyHandle(), filter, false, true);
    }

    /**
     * Так как тут основной индекс - он без обратной сортировки
     *
     * @param startHeight
     * @param stopHeight
     * @return
     */
    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairHeightIterator(int startHeight, int stopHeight) {
        byte[] startBytes;
        if (startHeight > 0) {
            startBytes = new byte[4];
            System.arraycopy(Ints.toByteArray(startHeight), 0, startBytes, 0, 4);
        } else {
            startBytes = new byte[0];
        }

        byte[] stopBytes;
        if (stopHeight > 0) {
            stopBytes = new byte[8];
            System.arraycopy(Longs.toByteArray(Transaction.makeDBRef(stopHeight, Integer.MAX_VALUE)), 0, stopBytes, 0, 8);
        } else {
            stopBytes = null;
        }

        return map.getIndexIteratorFilter(startBytes, stopBytes, false, false);
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairHeightIterator(long have, long want, int startHeight, int stopHeight) {

        byte[] startBytes;
        if (startHeight > 0) {
            startBytes = new byte[20];
            makeKey(startBytes, have, want);
            System.arraycopy(Ints.toByteArray(Integer.MAX_VALUE - startHeight), 0, startBytes, 16, 4);
        } else {
            startBytes = new byte[16];
            makeKey(startBytes, have, want);
            //startBytes[16] = (byte) 255; // больше делаем 1 байт чтобы захватывать значения все в это Высоте
        }

        byte[] stopBytes;
        if (stopHeight > 0) {
            stopBytes = new byte[24];
            makeKey(stopBytes, have, want);
            // так как тут обратный отсчет то вычитаем со старта еще и все номера транзакций
            System.arraycopy(Longs.toByteArray(Long.MAX_VALUE - Transaction.makeDBRef(stopHeight, 0)), 0, stopBytes, 16, 8);
            //stopBytes[24] = (byte) 255; // больше делаем 1 байт чтобы захватывать значения все в это Высоте
        } else {
            stopBytes = new byte[16];
            makeKey(stopBytes, have, want);
            // из-за того что тут RockStoreIteratorFilter(org.rocksdb.RocksIterator, boolean, boolean, byte[], byte[])
            // использует сравнение
            //stopBytes[16] = (byte) 255; // больше делаем 1 байт чтобы захватывать значения все в это Высоте
        }

        return map.getIndexIteratorFilter(pairIndex.getColumnFamilyHandle(), startBytes, stopBytes, false, true);

    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getIteratorFromID(long[] startTradeID) {
        byte[] startBytes;
        if (startTradeID != null) {
            startBytes = new byte[16];
            System.arraycopy(Longs.toByteArray(startTradeID[0]), 0, startBytes, 0, 8);
            System.arraycopy(Longs.toByteArray(startTradeID[1]), 0, startBytes, 8, 8);
        } else {
            startBytes = new byte[0];
        }

        return map.getIndexIteratorFilter(startBytes, null, false, false);
    }

    /**
     * Так как тут основной индекс - он без обратной сортировки
     * @param startOrderID
     * @param stopOrderID
     * @return
     */
    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairOrderIDIterator(long startOrderID, long stopOrderID) {
        byte[] startBytes;
        if (startOrderID > 0) {
            startBytes = new byte[8];
            System.arraycopy(Longs.toByteArray(startOrderID), 0, startBytes, 0, 8);
        } else {
            startBytes = new byte[0];
        }

        byte[] stopBytes;
        if (stopOrderID > 0) {
            stopBytes = new byte[8];
            System.arraycopy(Longs.toByteArray(stopOrderID), 0, stopBytes, 0, 8);
            //stopBytes[24] = (byte) 255; // больше делаем 1 байт чтобы захватывать значения все Sequence
        } else {
            stopBytes = null;
        }

        return map.getIndexIteratorFilter(startBytes, stopBytes, false, false);
    }

    @Override
    public IteratorCloseable<Tuple2<Long, Long>> getPairOrderIDIterator(long have, long want, long startOrderID, long stopOrderID) {

        byte[] startBytes;
        if (startOrderID > 0) {
            startBytes = new byte[24];
            makeKey(startBytes, have, want);
            System.arraycopy(Longs.toByteArray(Long.MAX_VALUE - startOrderID), 0, startBytes, 16, 8);
        } else {
            startBytes = new byte[16];
            makeKey(startBytes, have, want);
        }

        byte[] stopBytes;
        if (stopOrderID > 0) {
            stopBytes = new byte[24];
            makeKey(stopBytes, have, want);
            System.arraycopy(Longs.toByteArray(Long.MAX_VALUE - stopOrderID), 0, stopBytes, 16, 8);
            //stopBytes[24] = (byte) 255; // больше делаем 1 байт чтобы захватывать значения все Sequence
        } else {
            stopBytes = new byte[16];
            makeKey(stopBytes, have, want);
            //stopBytes[16] = (byte) 255; // больше делаем 1 байт чтобы захватывать значения все в это Высоте
        }

        return map.getIndexIteratorFilter(pairIndex.getColumnFamilyHandle(), startBytes, stopBytes, false, true);

    }
}
