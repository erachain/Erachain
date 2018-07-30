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

/*
 for key = Long - HAS MAP + HASH SET need to use
 OLD - Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>
 */
public class OrderMap extends DCMap<Long, Order> {
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
                        //return new Tuple3<>(value.b.a, value.c.a,
                        //        Order.calcPrice(value.b.b, value.c.b));
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
                        //return new Tuple3<>(value.c.a, value.b.a,
                        //        Order.calcPrice(value.b.b, value.c.b));
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

    //COMBINE LISTS
    private List<Long> checkKeys(Long have, Long want, List<Long> keys) {

        List<Long> keysWH;

        if (false) {
            keysWH = new ArrayList<>(((BTreeMap<Tuple3, Long>) this.haveWantKeyMap).subMap(
                    Fun.t3(have, want, null),
                    Fun.t3(have, want, Fun.HI())).values());
        } else if (false) {
            keysWH = new ArrayList<>(((BTreeMap<Tuple3, Long>) this.wantHaveKeyMap).subMap(
                    Fun.t3(have, want, null),
                    Fun.t3(have, want, Fun.HI())).values());
        } else if (false) {
            keysWH = new ArrayList<>(((BTreeMap<Tuple3, Long>) this.haveWantKeyMap).subMap(
                    Fun.t3(want, have, null),
                    Fun.t3(want, have, Fun.HI())).values());
        } else if (true) {
            // CORRECT! - haveWantKeyMap LOSES some orders!
            // https://github.com/icreator/Erachain/issues/178
            keysWH = new ArrayList<>(((BTreeMap<Tuple3, Long>) this.wantHaveKeyMap).subMap(
                    Fun.t3(want, have, null),
                    Fun.t3(want, have, Fun.HI())).values());
        } else {
            keysWH = null;
        }

        if (keysWH != null && !keysWH.isEmpty()) {
            boolean equal;
            // add only new unique
            for (Long keyWH: keysWH) {
                equal = false;
                for (Long key: keys) {
                    if (key.equals(keyWH)) {
                        equal = true;
                        break;
                    }
                }
                if (!equal) {
                    keys.add(keyWH);
                    int error = 0;
                    error ++;
                }
            }
        }

        return keys;

    }

    // GET KEYs with FORKED rules
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected List<Long> getSubKeysWithParent(long have, long want) {

        List<Long> keys = new ArrayList<>(((BTreeMap<Tuple3, Long>) this.haveWantKeyMap).subMap(
                Fun.t3(have, want, null),
                Fun.t3(have, want, Fun.HI())).values());

        keys = checkKeys(have, want, keys);

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

    /*
    public boolean isExecutable(DCSet db, Long key) {

        Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = this.get(key);
        // если произведение остатка
        BigDecimal left = order.b.b.subtract(order.b.c);
        if (left.signum() <= 0
                || left.multiply(order.a.e).compareTo(BigDecimal.ONE.scaleByPowerOfTen(-order.c.b.scale())) < 0)
            return false;

        return true;

    }
    */

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

        keys = checkKeys(have, want, keys);

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

        // this order is NOT executable
        ////this.set(order.a.a, setExecutable(order, true));
        this.set(order.getId(), order);
    }

    /*
    @Override
    public Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> get(Long key) {
        Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = super.get(key);
        if (order == null)
            return null;

        return setExecutable(order, true);
    }
    */


    public void delete(Order order) {

        this.delete(order.getId());
    }
}
