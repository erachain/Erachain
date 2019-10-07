package org.erachain.dbs.mapDB;

// 30/03

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.*;
import org.erachain.database.DBASet;
import org.erachain.database.SortableList;
import org.erachain.database.serializer.OrderSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.OrderMapImpl;
import org.erachain.datachain.OrderMapSuit;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
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
public class OrdersSuitMapDBFork extends DBMapSuit<Long, Order> implements OrderMapSuit {

    @SuppressWarnings("rawtypes")
    private BTreeMap haveWantKeyMap;
    @SuppressWarnings("rawtypes")
    // TODO: cut index to WANT only
    private BTreeMap wantHaveKeyMap;
    private BTreeMap addressHaveWantKeyMap;

    public OrdersSuitMapDBFork(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
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
        getMap();
    }

    @Override
    public Iterator<Long> getHaveWantIterator(long have, long want) {

        return ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).values().iterator();

    }

    // GET KEYs with FORKED rules
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected List<Long> getSubKeysWithParent(long have, long want) {

        List<Long> keys = new ArrayList<>(((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).values());

        //IF THIS IS A FORK
        if (this.parent != null) {

            //GET ALL KEYS FOR FORK in PARENT
            List<Long> parentKeys = ((OrderMapImpl) this.parent).getSubKeysWithParent(have, want);

            // REMOVE those who DELETED here
            if (this.deleted != null) {
                //DELETE DELETED
                for (Object deleted : this.deleted.keySet()) {
                    parentKeys.remove((Long) deleted);
                }
            }

            keys.addAll(parentKeys);

        }

        return keys;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Long> getKeysHave(long have) {

        //FILTER ALL KEYS
        Collection<Long> keys = ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, null, null, null),
                Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).values();

        return keys;
    }

    @Override
    public long getCountHave(long have) {

        long size = ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, null, null, null),
                Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).size();

        return size;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Order> getOrdersHave(long have, int limit) {

        //FILTER ALL KEYS
        Collection<Long> keys = ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, null, null, null),
                Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).values();

        Iterable<Long> iterable;
        if (limit > 0 && keys.size() > limit) {
            iterable = Iterables.limit(keys, limit);
        } else {
            iterable = keys;
        }

        Iterator iterator = iterable.iterator();
        List<Order> orders = new ArrayList<>();
        while (iterator.hasNext()) {
            orders.add(get((Long) iterator.next()));
        }

        return orders;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Long> getKeysWant(long want) {

        //FILTER ALL KEYS
        Collection<Long> keys = ((BTreeMap<Fun.Tuple4, Long>) this.wantHaveKeyMap).subMap(
                Fun.t4(want, null, null, null),
                Fun.t4(want, Fun.HI(), Fun.HI(), Fun.HI())).values();

        return keys;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public long getCountWant(long want) {

        long size = ((BTreeMap<Fun.Tuple4, Long>) this.wantHaveKeyMap).subMap(
                Fun.t4(want, null, null, null),
                Fun.t4(want, Fun.HI(), Fun.HI(), Fun.HI())).size();

        return size;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Order> getOrdersWant(long want, int limit) {

        //FILTER ALL KEYS
        Iterator<Long> iterator = ((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(want, null, null, null),
                Fun.t4(want, Fun.HI(), Fun.HI(), Fun.HI())).values().iterator();


        if (limit > 0 && Iterators.size(iterator) > limit) {
            Iterators.advance(iterator, limit);
        }

        List<Order> orders = new ArrayList<>();
        while (iterator.hasNext()) {
            orders.add(get(iterator.next()));
        }

        return orders;
    }

    @Override
    public List<Order> getOrders(long haveWant) {

        //GET ALL ORDERS FOR KEYS
        List<Order> orders = new ArrayList<Order>();

        for (Long key : this.getKeysHave(haveWant)) {
            orders.add(this.get(key));
        }

        for (Long key : this.getKeysWant(haveWant)) {
            orders.add(this.get(key));
        }

        return orders;
    }

    @Override
    public long getCountOrders(long haveWant) {

        return this.getCountHave(haveWant) + this.getCountWant(haveWant);
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

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<Long, Order> getOrdersSortableList(long have, long want, boolean reverse) {

        //FILTER ALL KEYS
        List<Long> keys = new ArrayList<>(((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).values());

        if (reverse) {
            Collections.sort(keys, new OrderKeysComparatorForTradeReverse());
        } else {
            Collections.sort(keys, new OrderKeysComparatorForTrade());
        }

        //RETURN
        return new SortableList<Long, Order>(this, keys);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Order> getOrders(long have, long want, int limit) {

        //FILTER ALL KEYS
        List<Long> keys = new ArrayList<>(((BTreeMap<Fun.Tuple4, Long>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).values());

        Iterable iterable = keys;

        if (limit > 0 && keys.size() > limit) {
            iterable = Iterables.limit(iterable, limit);
        }

        List<Order> orders = new ArrayList<Order>();

        Iterator iterator = iterable.iterator();
        while (iterator.hasNext()) {
            orders.add(this.get((Long) iterator.next()));
        }

        //RETURN
        return orders;
    }

    @Override
    public List<Order> getOrdersForAddress(
            String address, Long have, Long want) {

        Collection<Long> keys;
        keys = ((BTreeMap<Fun.Tuple5, Long>) this.addressHaveWantKeyMap).subMap(
                Fun.t5(address, have, want, null, null),
                Fun.t5(address, have, want, Fun.HI(), Fun.HI())).values();

        //GET ALL ORDERS FOR KEYS
        List<Order> orders = new ArrayList<Order>();

        for (Long key : keys) {

            Order order = this.get(key);

            // MAY BE NULLS!!!
            if (order != null)
                orders.add(this.get(key));
        }

        return orders;

    }

    public boolean set(Long id, Order order) {
        if (BlockChain.CHECK_BUGS > 0) {
            if (((DCSet) this.getDBSet()).getCompletedOrderMap().contains(id)) {
                // если он есть в уже завершенных
                assert ("".equals("already in Completed"));
            }
        }

        return super.set(id, order);
    }

    public Order remove(Long id) {
        if (BlockChain.CHECK_BUGS > 1) {
            if (((DCSet) this.getDBSet()).getCompletedOrderMap().contains(id)) {
                // если он есть в уже завершенных
                assert ("".equals("already in Completed"));
            }
        }
        return super.remove(id);
    }

    @Override
    public void add(Order order) {

        this.set(order.getId(), order);
    }

    @Override
    public void delete(Order order) {

        this.remove(order.getId());
    }

}
