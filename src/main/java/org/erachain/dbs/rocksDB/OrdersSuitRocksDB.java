package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.Order;
import org.erachain.database.DBASet;
import org.erachain.datachain.OrderSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableBigDecimal;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableOrder;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class OrdersSuitRocksDB extends DBMapSuit<Long, Order> implements OrderSuit {

    private final String NAME_TABLE = "ORDERS_TABLE";

    SimpleIndexDB<Long, Order, byte[]> haveWantKeyIndex;
    SimpleIndexDB<Long, Order, Fun.Tuple4<Long, Long, BigDecimal, Long>> wantHaveKeyIndex;
    SimpleIndexDB<Long, Order, Fun.Tuple5<String, Long, Long, BigDecimal, Long>> addressHaveWantKeyIndex;

    IndexByteableBigDecimal bgToBytes = new IndexByteableBigDecimal();
    ByteableLong byteableLong = new ByteableLong();

    public OrdersSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, false);
    }

    @Override
    public void openMap() {

        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableLong(), new ByteableOrder(),
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

        haveWantKeyIndex = new SimpleIndexDB<Long, Order, byte[]>("orders_key_have_want",
                (aLong, order) ->
                        org.bouncycastle.util.Arrays.concatenate(
                                byteableLong.toBytesObject(order.getHaveAssetKey()),
                                byteableLong.toBytesObject(order.getWantAssetKey()),

                                // по остаткам цены НЕЛЬЗЯ! так как при изменении цены после покусывания стрый ключ не находится!
                                // и потом при поиске по итераторы находятся эти неудалившиеся ключи!
                                bgToBytes.toBytes(order.calcLeftPrice()),
                                //// теперь можно - в Обработке ордера сделал решение этой проблемы value.getPrice(),
                                /////bgToBytes.toBytes(order.getPrice()),
                                //bgToBytes.toBytes(Order.calcPrice(order.getAmountHave(), order.getAmountWant(), 0)),

                                byteableLong.toBytesObject(order.getId())
                        ),
                result -> result
        );

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
                (result) ->
                {
                    return org.bouncycastle.util.Arrays.concatenate(org.bouncycastle.util.Arrays.concatenate(
                            new ByteableString().toBytesObject(result.a),
                            byteableLong.toBytesObject(result.b)),
                            org.bouncycastle.util.Arrays.concatenate(
                                    byteableLong.toBytesObject(result.c),
                                    bgToBytes.toBytes(result.d),
                                    byteableLong.toBytesObject(result.e)));
                });

        wantHaveKeyIndex = new SimpleIndexDB<>("orders_key_want_have",
                (aLong, order) -> new Fun.Tuple4<>(
                        order.getWantAssetKey(),
                        order.getHaveAssetKey(),
                        //Order.calcPrice(order.getAmountHave(), order.getAmountWant(), 0),
                        order.getPrice(),
                        order.getId()),
                (result) ->
                {
                    return org.bouncycastle.util.Arrays.concatenate(
                            byteableLong.toBytesObject(result.a),
                            byteableLong.toBytesObject(result.b),
                            bgToBytes.toBytes(result.c),
                            byteableLong.toBytesObject(result.d));
                });

        indexes.add(addressHaveWantKeyIndex);
        indexes.add(wantHaveKeyIndex);
    }

    @Override
    public IteratorCloseable<Long> getHaveWantIterator(long have, long want) {
        return map.getIndexIteratorFilter(haveWantKeyIndex.getColumnFamilyHandle(), org.bouncycastle.util.Arrays.concatenate(
                Longs.toByteArray(have),
                Longs.toByteArray(want)), false, true);
    }

    @Override
    public IteratorCloseable<Long> getHaveWantIterator(long have) {
        return map.getIndexIteratorFilter(haveWantKeyIndex.getColumnFamilyHandle(),
                Longs.toByteArray(have),
                false, true);
    }

    @Override
    public IteratorCloseable<Long> getWantHaveIterator(long want, long have) {
        return map.getIndexIteratorFilter(wantHaveKeyIndex.getColumnFamilyHandle(), org.bouncycastle.util.Arrays.concatenate(
                Longs.toByteArray(want),
                Longs.toByteArray(have)), false, true);
    }

    @Override
    public IteratorCloseable<Long> getWantHaveIterator(long want) {
        return map.getIndexIteratorFilter(wantHaveKeyIndex.getColumnFamilyHandle(),
                Longs.toByteArray(want),
                false, true);
    }

    @Override
    public IteratorCloseable<Long> getAddressHaveWantIterator(String address, long have, long want) {
        return map.getIndexIteratorFilter(
                addressHaveWantKeyIndex.getColumnFamilyHandle(),
                org.bouncycastle.util.Arrays.concatenate(address.getBytes(),
                        Longs.toByteArray(have),
                        Longs.toByteArray(want)),
                false, true);
    }

    @Override
    public IteratorCloseable<Long> getAddressIterator(String address) {
        return map.getIndexIteratorFilter(
                addressHaveWantKeyIndex.getColumnFamilyHandle(),
                address.getBytes(), false, true);
    }

    @Override
    public HashMap<Long, Order> getUnsortedEntries(long have, long want, BigDecimal stopPrice, Map deleted) {

        Iterator<Long> iterator = getHaveWantIterator(have, want);

        HashMap<Long, Order> result = new HashMap();
        while (iterator.hasNext()) {
            Long key = iterator.next();
            if (deleted != null && deleted.containsKey(key)) {
                // SKIP deleted in FORK
                continue;
            }

            Order order = get(key);
            if (BlockChain.CHECK_BUGS > 0 &&
                    // почемуто выскакивало за диаппазон пары
                    (true || order.getHaveAssetKey() != have || order.getWantAssetKey() != want)
            ) {
                Long error = null;
                ++error;
            }
            result.put(key, order);
            // сдесь ходябы одну заявку с неподходящей вроде бы ценой нужно взять
            // причем берем по Остаткам Цену теперь
            if (stopPrice != null && order.calcLeftPrice().compareTo(stopPrice) > 0) {
                break;
            }
        }

        return result;
    }

}
