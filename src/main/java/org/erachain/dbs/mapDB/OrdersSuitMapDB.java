package org.erachain.dbs.mapDB;

// 30/03

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Order;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.OrderSerializer;
import org.erachain.datachain.OrderSuit;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

;

//import com.sun.media.jfxmedia.logging.Logger;

/**
 * Хранит блоки полностью - с транзакциями
 * <p>
 * ключ: номер блока (высота, height)<br>
 * занчение: Блок<br>
 * <p>
 * Есть вторичный индекс, для отчетов (blockexplorer) - generatorMap
 * TODO - убрать длинный индек и вставить INT
 *
 * @return
 */

@Slf4j
public class OrdersSuitMapDB extends DBMapSuit<Long, Order> implements OrderSuit {

    @SuppressWarnings("rawtypes")
    private BTreeMap haveWantKeyMap;
    @SuppressWarnings("rawtypes")
    // TODO: cut index to WANT only
    private BTreeMap wantHaveKeyMap;
    private BTreeMap addressHaveWantKeyMap;

    public OrdersSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    public void openMap() {
        // OPEN MAP
        map = database.createTreeMap("orders")
                .valueSerializer(new OrderSerializer())
                //.comparator(Fun.BYTE_ARRAY_COMPARATOR) // for byte[]
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //HAVE/WANT KEY
        this.haveWantKeyMap = database.createTreeMap("orders_key_have_want")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        ///////////////////// HERE PROTOCOL INDEX

        //BIND HAVE/WANT KEY
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.haveWantKeyMap,
                new Fun.Function2<Fun.Tuple4<Long, Long, BigDecimal, Long>, Long,
                        Order>() {
                    @Override
                    public Fun.Tuple4<Long, Long, BigDecimal, Long> run(
                            Long key, Order value) {
                        return new Fun.Tuple4<>(value.getHaveAssetKey(), value.getWantAssetKey(),
                                value.calcPrice(),
                                value.getId());
                    }
                });

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        ///////////////////// HERE NOT PROTOCOL INDEXES

        // ADDRESS HAVE/WANT KEY
        this.addressHaveWantKeyMap = database.createTreeMap("orders_key_address_have_want")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.addressHaveWantKeyMap,
                new Fun.Function2<Fun.Tuple5<String, Long, Long, BigDecimal, Long>, Long, Order>() {
                    @Override
                    public Fun.Tuple5<String, Long, Long, BigDecimal, Long> run(
                            Long key, Order value) {
                        return new Fun.Tuple5<String, Long, Long, BigDecimal, Long>
                                (value.getCreator().getAddress(), value.getHaveAssetKey(), value.getWantAssetKey(), value.getPrice(),
                                        key);
                    }
                });

        // WANT/HAVE KEY
        this.wantHaveKeyMap = database.createTreeMap("orders_key_want_have")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.wantHaveKeyMap,
                new Fun.Function2<Fun.Tuple4<Long, Long, BigDecimal, Long>, Long,
                        Order>() {
                    @Override
                    public Fun.Tuple4<Long, Long, BigDecimal, Long> run(
                            Long key, Order value) {
                        return new Fun.Tuple4<>(value.getWantAssetKey(), value.getHaveAssetKey(),
                                value.calcPrice(),
                                value.getId());
                    }
                });
    }

    //@Override
    protected void getMemoryMap() {
        openMap();
    }

    @Override
    public Iterator<Long> getHaveWantIterator(long have, long want) {

        return ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).values().iterator();

    }

    @Override
    public Iterator<Long> getHaveWantIterator(long have) {
        return ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, null, null, null),
                Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).values().iterator();
    }

    @Override
    public Iterator<Long> getWantHaveIterator(long want, long have) {

        return ((BTreeMap<Fun.Tuple4, Long>) this.wantHaveKeyMap).subMap(
                Fun.t4(want, have, null, null),
                Fun.t4(want, have, Fun.HI(), Fun.HI())).values().iterator();

    }

    @Override
    public Iterator<Long> getWantHaveIterator(long want) {
        return ((BTreeMap<Fun.Tuple4, Long>) this.wantHaveKeyMap).subMap(
                Fun.t4(want, null, null, null),
                Fun.t4(want, Fun.HI(), Fun.HI(), Fun.HI())).values().iterator();
    }

    @Override
    public Iterator<Long> getAddressHaveWantIterator(String address, long have, long want) {
        return ((BTreeMap<Fun.Tuple5, Long>) this.addressHaveWantKeyMap).subMap(
                Fun.t5(address, have, want, null, null),
                Fun.t5(address, have, want, Fun.HI(), Fun.HI())).values().iterator();
    }

    @Override
    public HashSet<Long> getUnsortedKeysWithParent(long have, long want, BigDecimal limit) {

        Object limitOrHI = limit == null ? Fun.HI() : limit; // надо тут делать выбор иначе ошибка преобразования в subMap
        Collection<Long> keys = ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, limitOrHI, Fun.HI()))
                //Fun.t4(have, want, limit, Fun.HI()))
                .values();

        return new HashSet<>(keys);
    }

}
