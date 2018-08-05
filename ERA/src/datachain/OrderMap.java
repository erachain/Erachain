package datachain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import core.crypto.Base58;
import core.item.assets.*;
import database.serializer.OrderSerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import database.DBMap;
import utils.ObserverMessage;

/*
 for key = Long as Block Height + Transaction Sequence
 */
public class OrderMap extends DCMap<Long, Order> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    @SuppressWarnings("rawtypes")
    private BTreeMap haveWantKeyMap;
    @SuppressWarnings("rawtypes")
    // TODO: cut index to WANT only
    private BTreeMap wantHaveKeyMap;

    public OrderMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_ORDER_TYPE);
            if (databaseSet.isDynamicGUI()) {
                this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_ORDER_TYPE);
                this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_ORDER_TYPE);
            }
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ORDER_TYPE);
        }
    }

    public OrderMap(OrderMap parent, DCSet dcSet) {
        super(parent, dcSet);

    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Long, Order> getMap(DB database) {
        //OPEN MAP
        return this.openMap(database);
    }

    @Override
    protected Map<Long, Order> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        return this.openMap(database);
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Order> openMap(DB database) {
        //OPEN MAP
        BTreeMap<Long, Order> map = database.createTreeMap("orders")
                .valueSerializer(new OrderSerializer())
                //.comparator(Fun.BYTE_ARRAY_COMPARATOR) // for byte[]
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //HAVE/WANT KEY
        this.haveWantKeyMap = database.createTreeMap("orders_key_have_want")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKey(map, this.haveWantKeyMap,
                new Fun.Function2<Tuple3<Long, Long, BigDecimal>, Long,
                        Order>() {
                    @Override
                    public Tuple3<Long, Long, BigDecimal> run(
                            Long key, Order value) {
                        return new Tuple3<>(value.getHave(), value.getWant(),
                                Order.calcPrice(value.getAmountHave(), value.getAmountWant()));
                    }
                });

        // WANT/HAVE KEY
        this.wantHaveKeyMap = database.createTreeMap("orders_key_want_have")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKey(map, this.wantHaveKeyMap,
                new Fun.Function2<Tuple3<Long, Long, BigDecimal>, Long,
                        Order>() {
                    @Override
                    public Tuple3<Long, Long, BigDecimal> run(
                            Long key, Order value) {
                        return new Tuple3<>(value.getWant(), value.getHave(),
                                Order.calcPrice(value.getAmountHave(), value.getAmountWant()));
            }
        });


        //RETURN
        return map;
    }

    @Override
    protected Order getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    // GET KEYs with FORKED rules
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected List<Long> getSubKeysWithParent(long have, long want) {

        List<Long> keys = new ArrayList<>(((BTreeMap<Tuple3, Long>) this.haveWantKeyMap).subMap(
                Fun.t3(have, want, null),
                Fun.t3(have, want, Fun.HI())).values());

        //IF THIS IS A FORK
        if (this.parent != null) {

            //GET ALL KEYS FOR FORK in PARENT
            List<Long> parentKeys = ((OrderMap) this.parent).getSubKeysWithParent(have, want);

            // REMOVE those who DELETED here
            if (this.deleted != null) {
                //DELETE DELETED
                for (Long deleted : this.deleted) {
                    parentKeys.remove(deleted);
                }
            }

            keys.addAll(parentKeys);

        }

        return keys;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<Long> getKeysHave(long have) {

        //FILTER ALL KEYS
        Collection<Long> keys = ((BTreeMap<Tuple3, Long>) this.haveWantKeyMap).subMap(
                Fun.t3(have, null, null),
                Fun.t3(have, Fun.HI(), Fun.HI())).values();

        return keys;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<Long> getKeysWant(long want) {

        //FILTER ALL KEYS
        Collection<Long> keys = ((BTreeMap<Tuple3, Long>) this.wantHaveKeyMap).subMap(
                Fun.t3(want, null, null),
                Fun.t3(want, Fun.HI(), Fun.HI())).values();

        return keys;
    }

    public List<Order> getOrders(long haveWant) {

        //GET ALL ORDERS FOR KEYS
        List<Order> orders = new ArrayList<Order>();

        for (Long key: this.getKeysHave(haveWant)) {
            orders.add(this.get(key));
        }

        for (Long key: this.getKeysWant(haveWant)) {
            orders.add(this.get(key));
        }

        return orders;
    }

    public List<Order> getOrdersForTradeWithFork(long have, long want, boolean reverse) {
        //FILTER ALL KEYS
        Collection<Long> keys = this.getSubKeysWithParent(have, want);

        //GET ALL ORDERS FOR KEYS
        List<Order> orders = new ArrayList<Order>();

        for (Long key : keys) {
            orders.add(this.get(key));
        }

        if (reverse) {
            Collections.sort(orders, new OrderComparatorForTradeReverse());
        } else {
            Collections.sort(orders, new OrderComparatorForTrade());
        }
            
        //RETURN
        return orders;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<Long, Order> getOrdersSortableList(long have, long want, boolean reverse) {

        //FILTER ALL KEYS
        List<Long> keys = new ArrayList<>(((BTreeMap<Tuple3, Long>) this.haveWantKeyMap).subMap(
                Fun.t3(have, want, null),
                Fun.t3(have, want, Fun.HI())).values());

        if (reverse) {
            Collections.sort(keys, new OrderKeysComparatorForTradeReverse());
        } else {
            Collections.sort(keys, new OrderKeysComparatorForTrade());
        }

        //RETURN
        return new SortableList<Long, Order>(this, keys);
    }

    /*
    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<Long, Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrdersHaveSortableList(long have) {
        //FILTER ALL KEYS
        Collection<Long> keys = ((BTreeMap<Tuple3, Long>) this.haveWantKeyMap).subMap(
                Fun.t3(have, null, null),
                Fun.t3(have, Fun.HI(), Fun.HI())).values();

        //RETURN
        return new SortableList<Long, Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>(this, keys);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<Long, Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrdersWantSortableList(long want) {
        //FILTER ALL KEYS
        Collection<Long> keys = ((BTreeMap<Tuple3, Long>) this.haveWantKeyMap).subMap(
                Fun.t3(null, want, null),
                Fun.t3(Fun.HI(), want, Fun.HI())).values();

        //RETURN
        return new SortableList<Long, Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>(this, keys);
    }
    */

    public void add(Order order) {

        this.set(order.getId(), order);
    }

    public void delete(Order order) {

        this.delete(order.getId());
    }
}
