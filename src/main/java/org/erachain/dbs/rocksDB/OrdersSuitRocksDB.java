package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.OrderComparatorForTrade;
import org.erachain.core.item.assets.OrderComparatorForTradeReverse;
import org.erachain.database.DBASet;
import org.erachain.datachain.OrderMapSuit;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableBigDecimal;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableOrder;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class OrdersSuitRocksDB extends DBMapSuit<Long, Order> implements OrderMapSuit {

    private final String NAME_TABLE = "ORDERS_TABLE";

    SimpleIndexDB<Long, Order, Fun.Tuple4<Long, Long, BigDecimal, Long>> haveWantKeyIndex;
    SimpleIndexDB<Long, Order, Fun.Tuple4<Long, Long, BigDecimal, Long>> wantHaveKeyIndex;
    SimpleIndexDB<Long, Order, Fun.Tuple5<String, Long, Long, BigDecimal, Long>> addressHaveWantKeyIndex;

    public OrdersSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    protected void openMap() {

        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableLong(), new ByteableOrder(),
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

        haveWantKeyIndex = new SimpleIndexDB<Long, Order, Fun.Tuple4<Long, Long, BigDecimal, Long>>("orders_key_have_want",
                (aLong, order) -> new Fun.Tuple4<>(
                        order.getHaveAssetKey(),
                        order.getWantAssetKey(),
                        Order.calcPrice(order.getAmountHave(), order.getAmountWant(), 0),
                        order.getId()),
                (result, key) ->
                {
                    ByteableLong byteableLong = new ByteableLong();
                    return org.bouncycastle.util.Arrays.concatenate(
                            byteableLong.toBytesObject(result.a),
                            byteableLong.toBytesObject(result.b),
                            new ByteableBigDecimal().toBytesObject(result.c),
                            byteableLong.toBytesObject(result.d));
                });

        indexes.add(haveWantKeyIndex);

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        ///////////////////// HERE NOT PROTOCOL INDEXES
        addressHaveWantKeyIndex = new SimpleIndexDB<>("orders_key_address_have_want",
                (aLong, order) ->
                        new Fun.Tuple5<>(
                                order.getCreator().getAddress(),
                                order.getHaveAssetKey(),
                                order.getWantAssetKey(),
                                order.getPrice(),
                                aLong),
                (result, key) ->
                {
                    ByteableLong byteableLong = new ByteableLong();
                    return org.bouncycastle.util.Arrays.concatenate(org.bouncycastle.util.Arrays.concatenate(
                            new ByteableString().toBytesObject(result.a),
                            byteableLong.toBytesObject(result.b)),
                            org.bouncycastle.util.Arrays.concatenate(
                                    byteableLong.toBytesObject(result.c),
                                    new ByteableBigDecimal().toBytesObject(result.d),
                                    byteableLong.toBytesObject(result.e)));
                });

        wantHaveKeyIndex = new SimpleIndexDB<>("orders_key_want_have",
                (aLong, order) -> new Fun.Tuple4<>(
                        order.getWantAssetKey(),
                        order.getHaveAssetKey(),
                        Order.calcPrice(order.getAmountHave(), order.getAmountWant(), 0),
                        order.getId()),
                (result, key) ->
                {
                    ByteableLong byteableLong = new ByteableLong();
                    return org.bouncycastle.util.Arrays.concatenate(
                            byteableLong.toBytesObject(result.a),
                            byteableLong.toBytesObject(result.b),
                            new ByteableBigDecimal().toBytesObject(result.c),
                            byteableLong.toBytesObject(result.d));
                });

        indexes.add(addressHaveWantKeyIndex);
        indexes.add(wantHaveKeyIndex);
    }

    @Override
    public Iterator<Long> getHaveWantIterator(long have, long want) {
        return map.getIndexIteratorFilter(haveWantKeyIndex.getColumnFamilyHandle(), org.bouncycastle.util.Arrays.concatenate(
                Longs.toByteArray(have),
                Longs.toByteArray(want)), false);
    }

    @Override
    public Iterator<Long> getHaveWantIterator(long have) {
        return map.getIndexIteratorFilter(haveWantKeyIndex.getColumnFamilyHandle(),
                Longs.toByteArray(have),
                false);
    }

    @Override
    public Iterator<Long> getWantHaveIterator(long want, long have) {
        return map.getIndexIteratorFilter(wantHaveKeyIndex.getColumnFamilyHandle(), org.bouncycastle.util.Arrays.concatenate(
                Longs.toByteArray(want),
                Longs.toByteArray(have)), false);
    }

    @Override
    public Iterator<Long> getWantHaveIterator(long want) {
        return map.getIndexIteratorFilter(wantHaveKeyIndex.getColumnFamilyHandle(),
                Longs.toByteArray(want),
                false);
    }

    @Override
    public Iterator<Long> getAddressHaveWantIterator(String address, long have, long want) {
        return map.getIndexIteratorFilter(
                addressHaveWantKeyIndex.getColumnFamilyHandle(),
                org.bouncycastle.util.Arrays.concatenate(address.getBytes(),
                        Longs.toByteArray(have),
                        Longs.toByteArray(want)),
                false);
    }

    @Override
    public List<Long> getSubKeysWithParent(long have, long want) {

        List<Long> keysByIndex = map.filterAppropriateValuesAsKeys(
                org.bouncycastle.util.Arrays.concatenate(
                        Longs.toByteArray(have),
                        Longs.toByteArray(want)),
                haveWantKeyIndex.getColumnFamilyHandle());
        return keysByIndex.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }


    @Override
    public List<Order> getOrdersForTradeWithFork(long have, long want, boolean reverse) {
        //FILTER ALL KEYS
        Collection<Long> keys = this.getSubKeysWithParent(have, want);

        //GET ALL ORDERS FOR KEYS
        List<Order> orders = new ArrayList<Order>();

        for (Long key : keys) {
            Order order = this.get(key);
            if (order != null) {
                orders.add(order);
            } else {
                // возможно произошло удаление в момент запроса??
            }
        }

        if (reverse) {
            Collections.sort(orders, new OrderComparatorForTradeReverse());
        } else {
            Collections.sort(orders, new OrderComparatorForTrade());
        }

        //RETURN
        return orders;
    }

}
