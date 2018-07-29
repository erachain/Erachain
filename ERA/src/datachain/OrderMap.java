package datachain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import core.crypto.Base58;
import core.item.assets.OrderKeysComparatorForTrade;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
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

/*
 for key = byte[] - HAS MAP + HASH SET need to use
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
                //.comparator(Fun.COMPARATOR)
                .comparator(Fun.BYTE_ARRAY_COMPARATOR)
                .makeOrGet();

        //HAVE/WANT KEY
        this.haveWantKeyMap = database.createTreeMap("orders_key_have_want")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKey(map, this.haveWantKeyMap,
                new Fun.Function2<Tuple3<Long, Long, BigDecimal>, byte[],
                        Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>() {
                    @Override
                    public Tuple3<Long, Long, BigDecimal> run(
                            byte[] key, Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                                                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> value) {
                        return new Tuple3<>(value.b.a, value.c.a,
                                Order.calcPrice(value.b.b, value.c.b));
                    }
                });

        // WANT/HAVE KEY
        this.wantHaveKeyMap = database.createTreeMap("orders_key_want_have")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND HAVE/WANT KEY
        Bind.secondaryKey(map, this.wantHaveKeyMap,
                new Fun.Function2<Tuple3<Long, Long, BigDecimal>, byte[],
                    Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                        Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>() {
                @Override
                public Tuple3<Long, Long, BigDecimal> run(
                        byte[] key, Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                                              Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> value) {
                return new Tuple3<>(value.c.a, value.b.a,
                        Order.calcPrice(value.b.b, value.c.b));
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

    //COMBINE LISTS
    private HashSet<byte[]> checkKeys(Long have, Long want, HashSet<byte[]> keys) {

        HashSet<byte[]> keysWH;

        if (false) {
            keysWH = new HashSet(((BTreeMap<Tuple3, byte[]>) this.haveWantKeyMap).subMap(
                    Fun.t3(have, want, null),
                    Fun.t3(have, want, Fun.HI())).values());
        } else if (false) {
            keysWH = new HashSet(((BTreeMap<Tuple3, byte[]>) this.wantHaveKeyMap).subMap(
                    Fun.t3(have, want, null),
                    Fun.t3(have, want, Fun.HI())).values());
        } else if (false) {
            keysWH = new HashSet(((BTreeMap<Tuple3, byte[]>) this.haveWantKeyMap).subMap(
                    Fun.t3(want, have, null),
                    Fun.t3(want, have, Fun.HI())).values());
        } else if (true) {
            // CORRECT! - haveWantKeyMap LOSES some orders!
            // https://github.com/icreator/Erachain/issues/178
            keysWH = new HashSet(((BTreeMap<Tuple3, byte[]>) this.wantHaveKeyMap).subMap(
                    Fun.t3(want, have, null),
                    Fun.t3(want, have, Fun.HI())).values());
        } else {
            keysWH = null;
        }

        if (keysWH != null && !keysWH.isEmpty()) {
            // add only new unique
            for (byte[] key: keysWH) {
                if (!keys.contains(key)) {
                    int error = 0;
                    error ++;
                }
            }

            keys.addAll(keysWH);
        }

        return keys;

    }

    // GET KEYs with FORKED rules
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected HashSet<byte[]> getSubKeysWithParent(long have, long want) {

        HashSet<byte[]> keys = new HashSet(((BTreeMap<Tuple3, byte[]>) this.haveWantKeyMap).subMap(
                Fun.t3(have, want, null),
                Fun.t3(have, want, Fun.HI())).values());

        keys = checkKeys(have, want, keys);

        //IF THIS IS A FORK
        if (this.parent != null) {

            //GET ALL KEYS FOR FORK in PARENT
            HashSet<byte[]> parentKeys = ((OrderMap) this.parent).getSubKeysWithParent(have, want);

            // REMOVE those who DELETED here
            if (this.deleted != null) {
                //DELETE DELETED
                for (byte[] deleted : this.deleted) {
                    parentKeys.remove(deleted);
                }
            }

            keys.addAll(parentKeys);

        }

        return keys;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<byte[]> getKeysHave(long have) {

        //FILTER ALL KEYS
        Collection<byte[]> keys = ((BTreeMap<Tuple3, byte[]>) this.haveWantKeyMap).subMap(
                Fun.t3(have, null, null),
                Fun.t3(have, Fun.HI(), Fun.HI())).values();

        return keys;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<byte[]> getKeysWant(long want) {

        //FILTER ALL KEYS
        Collection<byte[]> keys = ((BTreeMap<Tuple3, byte[]>) this.wantHaveKeyMap).subMap(
                Fun.t3(want, null, null),
                Fun.t3(want, Fun.HI(), Fun.HI())).values();

        return keys;
    }

    public List<Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrders(long haveWant) {

        //GET ALL ORDERS FOR KEYS
        List<Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = new ArrayList<Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>();

        for (byte[] key: this.getKeysHave(haveWant)) {
            orders.add(this.get(key));
        }

        for (byte[] key: this.getKeysWant(haveWant)) {
            orders.add(this.get(key));
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
        HashSet<byte[]> keys = new HashSet(((BTreeMap<Tuple3, byte[]>) this.haveWantKeyMap).subMap(
                Fun.t3(have, want, null),
                Fun.t3(have, want, Fun.HI())).values());

        keys = checkKeys(have, want, keys);

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
        Collection<byte[]> keys = ((BTreeMap<Tuple3, byte[]>) this.haveWantKeyMap).subMap(
                Fun.t3(have, null, null),
                Fun.t3(have, Fun.HI(), Fun.HI())).values();

        //RETURN
        return new SortableList<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>(this, keys);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<byte[], Tuple3<Tuple5<byte[], String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrdersWantSortableList(long want) {
        //FILTER ALL KEYS
        Collection<byte[]> keys = ((BTreeMap<Tuple3, byte[]>) this.haveWantKeyMap).subMap(
                Fun.t3(null, want, null),
                Fun.t3(Fun.HI(), want, Fun.HI())).values();

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
