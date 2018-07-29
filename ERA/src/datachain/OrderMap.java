package datachain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import core.crypto.Base58;
import core.item.assets.OrderKeysComparatorForTrade;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;

import core.item.assets.Order;
import core.item.assets.OrderComparatorForTrade;
import core.item.assets.OrderComparatorForTradeReverse;
import database.DBMap;
import utils.ObserverMessage;

/*
 * Tuple5
 * 	private BigInteger id;
	private Account creator;
	protected long timestamp;
	private boolean isExecutable = true;
	private BigDecimal Price;

Tuple3
	private long have;
	private BigDecimal amountHave;
	private BigDecimal fulfilledHave;

Tuple3
	private long want;
	private BigDecimal amountWant;
	private BigDecimal fulfilledWant;
 */

public class OrderMap extends DCMap<byte[],
        Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    @SuppressWarnings("rawtypes")
    private BTreeMap haveWantKeyMap;
    @SuppressWarnings("rawtypes")
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

    /*
    public static Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> setExecutable(Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order, boolean executable) {
        Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> newOrder = new Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>(
                new Tuple5<BigInteger, String, Long, Boolean, BigDecimal>(order.a.a, order.a.b, order.a.c, executable, order.a.e),
                order.b, order.c);
        return newOrder;
    }
    */

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getMap(DB database) {
        //OPEN MAP
        return this.openMap(database);
    }

    @Override
    protected Map<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        return this.openMap(database);
    }

    @SuppressWarnings("unchecked")
    private Map<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> openMap(DB database) {
        //OPEN MAP
        BTreeMap<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> map = database.createTreeMap("orders")
                //.valueSerializer(new OrderSerializer())
                .comparator(Fun.BYTE_ARRAY_COMPARATOR)
                .makeOrGet();

        //HAVE/WANT KEY
        this.haveWantKeyMap = database.createTreeMap("orders_key_have_want")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKey(map, this.haveWantKeyMap,
                new Fun.Function2<Tuple4<Long, Long, BigDecimal, byte[]>, byte[],
                        Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>() {
                    @Override
                    public Tuple4<Long, Long, BigDecimal, byte[]> run(
                            byte[] key, Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                                                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> value) {
                        return new Tuple4<Long, Long, BigDecimal, byte[]>(value.b.a, value.c.a,
                                Order.calcPrice(value.b.b, value.c.b), key);
                    }
                });

        // WANT/HAVE KEY
        this.wantHaveKeyMap = database.createTreeMap("orders_key_want_have")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKey(map, this.wantHaveKeyMap,
                new Fun.Function2<Tuple4<Long, Long, BigDecimal, byte[]>, byte[],
                    Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                        Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>() {
                @Override
                public Tuple4<Long, Long, BigDecimal, byte[]> run(
                        byte[] key, Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                                              Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> value) {
                return new Tuple4<Long, Long, BigDecimal, byte[]>(value.c.a, value.b.a,
                        Order.calcPrice(value.b.b, value.c.b), key);
            }
        });


        //RETURN
        return map;
    }

    @Override
    protected Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    // GET KEYs with FORKED rules
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Collection<byte[]> getSubKeysWithParent(long have, long want) {

        Collection<byte[]> keys;
        //FILTER ALL KEYS

        ///if (false) {
            // in haveWantKeyMap - NOT ALL ORDERS!
        keys = ((BTreeMap<Tuple4, byte[]>) this.haveWantKeyMap).subMap(
                //Fun.t4(have, want, null, null),
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).values();
        //keys = new TreeSet<byte[]>(keys);
        ///} else {
            // in wantHaveKeyMap - ALL ORDERS
        Collection<byte[]> keysWH;
        keysWH = ((BTreeMap<Tuple4, byte[]>) this.wantHaveKeyMap).subMap(
                //Fun.t4(have, want, null, null),
                Fun.t4(want, have, null, null),
                Fun.t4(want, have, Fun.HI(), Fun.HI())).values();
        //keysWH = new TreeSet<byte[]>(keysWH);
            
        //}

        if (!keysWH.isEmpty()) {
            // add only new unique
            Set<byte[]> combinedKeys = new TreeSet<byte[]>(keys);
            combinedKeys.addAll(keysWH);
            keys = combinedKeys;
        }

            
        //IF THIS IS A FORK
        if (this.parent != null) {

            //GET ALL KEYS FOR FORK in PARENT
            Collection<byte[]> parentKeys = ((OrderMap) this.parent).getSubKeysWithParent(have, want);

            // REMOVE those who DELETED here
            if (this.deleted != null) {
                //DELETE DELETED
                for (byte[] deleted : this.deleted) {
                    parentKeys.remove(deleted);
                }
            }

            //COMBINE LISTS
            Set<byte[]> combinedKeys = new TreeSet<byte[]>(keys);
            combinedKeys.addAll(parentKeys);

            //CONVERT SET BACK TO COLLECTION
            keys = combinedKeys;

        }

        return keys;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<byte[]> getKeysHave(long have) {

        //FILTER ALL KEYS
        Collection<byte[]> keys = ((BTreeMap<Tuple4, byte[]>) this.haveWantKeyMap).subMap(
                Fun.t4(have, null, null, null),
                Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).values();

        return keys;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<byte[]> getKeysWant(long want) {

        //FILTER ALL KEYS
        Collection<byte[]> keys = ((BTreeMap<Tuple4, byte[]>) this.wantHaveKeyMap).subMap(
                Fun.t4(want, null, null, null),
                Fun.t4(want, Fun.HI(), Fun.HI(), Fun.HI())).values();

        return keys;
    }

    public List<Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrders(long haveWant) {
        return getOrders(haveWant, false);
    }

    public List<Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrders(long haveWant, boolean filter) {
        Map<byte[], Boolean> orderKeys = new TreeMap<byte[], Boolean>();

        //FILTER ALL KEYS
        Collection<byte[]> keys = this.getKeysHave(haveWant);

        for (byte[] key : keys) {
            orderKeys.put(key, true);
        }

        keys = this.getKeysWant(haveWant);

        for (byte[] key : keys) {
            orderKeys.put(key, true);
        }

        //GET ALL ORDERS FOR KEYS
        List<Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = new ArrayList<Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>();

        for (Map.Entry<byte[], Boolean> orderKey : orderKeys.entrySet()) {
            orders.add(this.get(orderKey.getKey()));
        }

        return orders;
    }

    /*
    public boolean isExecutable(DCSet db, byte[] key) {

        Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = this.get(key);
        // если произведение остатка
        BigDecimal left = order.b.b.subtract(order.b.c);
        if (left.signum() <= 0
                || left.multiply(order.a.e).compareTo(BigDecimal.ONE.scaleByPowerOfTen(-order.c.b.scale())) < 0)
            return false;

        return true;

    }
    */

    public List<Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrdersForTradeWithFork(long have, long want, boolean reverse) {
        //FILTER ALL KEYS
        Collection<byte[]> keys = this.getSubKeysWithParent(have, want);

        //GET ALL ORDERS FOR KEYS
        List<Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = new ArrayList<Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>();

        for (byte[] key : keys) {
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
    public SortableList<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrdersSortableList(long have, long want) {

        //FILTER ALL KEYS
        Collection<byte[]> keys;
        
        keys = ((BTreeMap<Tuple4, byte[]>) this.haveWantKeyMap).subMap(
                Fun.t4(have, want, null, null),
                Fun.t4(have, want, Fun.HI(), Fun.HI())).values();
        
        if (false) {
            keys = ((BTreeMap<Tuple4, byte[]>) this.haveWantKeyMap).subMap(
                    Fun.t4(have, want, null, null),
                    Fun.t4(have, want, Fun.HI(), Fun.HI())).values();
        } else if (false) {
            keys = ((BTreeMap<Tuple4, byte[]>) this.wantHaveKeyMap).subMap(
                    Fun.t4(have, want, null, null),
                    Fun.t4(have, want, Fun.HI(), Fun.HI())).values();
        } else if (false) {
            keys = ((BTreeMap<Tuple4, byte[]>) this.haveWantKeyMap).subMap(
                    Fun.t4(want, have, null, null),
                    Fun.t4(want, have, Fun.HI(), Fun.HI())).values();
        } else {
            Collection<byte[]> keysWH;
            // CORRECT! - haveWantKeyMap LOSES some orders!
            // https://github.com/icreator/Erachain/issues/178
            keysWH = ((BTreeMap<Tuple4, byte[]>) this.wantHaveKeyMap).subMap(
                    Fun.t4(want, have, null, null),
                    Fun.t4(want, have, Fun.HI(), Fun.HI())).values();
            
            if (!keysWH.isEmpty()) {
                // add only new unique
                Set<byte[]> combinedKeys = new TreeSet<byte[]>(keys);
                combinedKeys.addAll(keysWH);
                keys = combinedKeys;
            }

        }

        List<byte[]> keysList = new ArrayList<byte[]>(keys);
        Collections.sort(keysList, new OrderKeysComparatorForTrade());

        //RETURN
        return new SortableList<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>(this, keysList);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrdersHaveSortableList(long have) {
        //FILTER ALL KEYS
        Collection<byte[]> keys = ((BTreeMap<Tuple4, byte[]>) this.haveWantKeyMap).subMap(
                Fun.t4(have, null, null, null),
                Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).values();

        //RETURN
        return new SortableList<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>(this, keys);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrdersWantSortableList(long want) {
        //FILTER ALL KEYS
        Collection<byte[]> keys = ((BTreeMap<Tuple4, byte[]>) this.haveWantKeyMap).subMap(
                Fun.t4(null, want, null, null),
                Fun.t4(Fun.HI(), want, Fun.HI(), Fun.HI())).values();

        //RETURN
        return new SortableList<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>(this, keys);
    }

    public void add(Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order) {

        if (Base58.encode(order.a.a)
                .equals("nQhYYc4tSM2sPLpiceCWGKhdt5MKhu82LrTM9hCKgh3iyQzUiZ8H7s4niZrgy4LR4Zav1zXD7kra4YWRd3Fstd")) {
            int error = 0;
            error ++;
        }

        // this order is NOT executable
        ////this.set(order.a.a, setExecutable(order, true));
        this.set(order.a.a, order);
    }

    /*
    @Override
    public Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> get(byte[] key) {
        Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = super.get(key);
        if (order == null)
            return null;

        return setExecutable(order, true);
    }
    */


    public void delete(Order order) {

        if (Base58.encode(order.getId())
                .equals("nQhYYc4tSM2sPLpiceCWGKhdt5MKhu82LrTM9hCKgh3iyQzUiZ8H7s4niZrgy4LR4Zav1zXD7kra4YWRd3Fstd")) {
            int error = 0;
            error ++;
        }

        this.delete(order.getId());
    }
}
