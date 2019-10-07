package org.erachain.dbs.mapDB;

// 30/03

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.OrderComparatorForTrade;
import org.erachain.core.item.assets.OrderComparatorForTradeReverse;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.OrderSerializer;
import org.erachain.datachain.OrderMap;
import org.erachain.datachain.OrderMapSuit;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.*;

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
public class OrdersSuitMapDBFork extends DBMapSuitFork<Long, Order> implements OrderMapSuit {

    @SuppressWarnings("rawtypes")
    private BTreeMap haveWantKeyMap;

    public OrdersSuitMapDBFork(OrderMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, null);
    }

    @Override
    protected void getMap() {
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

    }

    //@Override
    protected void getMemoryMap() {
        getMap();
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
        return null;
    }

    @Override
    public Iterator<Long> getWantHaveIterator(long want) {
        return null;
    }

    @Override
    public Iterator<Long> getAddressHaveWantIterator(String address, long have, long want) {
        return null;
    }

    // GET KEYs with FORKED rules
    @Override
    public List<Long> getSubKeysWithParent(long have, long want) {

        List<Long> keys = new ArrayList<>(((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).values());

        //USE THE FORK KEYS

        //GET ALL KEYS FOR FORK in PARENT - getOrdersForTradeWithFork
        List<Long> parentKeys = ((OrderMap) this.parent).getSubKeysWithParent(have, want);

        // REMOVE those who DELETED here
        if (this.deleted != null) {
            //DELETE DELETED
            for (Object deleted : this.deleted.keySet()) {
                parentKeys.remove((Long) deleted);
            }
        }

        keys.addAll(parentKeys);

        return keys;
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
