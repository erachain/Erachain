package datachain;

import com.google.common.primitives.UnsignedBytes;
import core.item.assets.Order;
import core.item.assets.Trade;
import database.DBMap;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.ObserverMessage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/*
 *
 * private BigInteger initiator;
	private BigInteger target;
	private BigDecimal amountHave;
	private BigDecimal amountWant;
	private long timestamp;
 */
@SuppressWarnings("rawtypes")
public class TradeMap extends DCMap<Tuple2<Long, Long>,
        Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    private BTreeMap pairKeyMap;
    private BTreeMap wantKeyMap;
    private BTreeMap haveKeyMap;
    private BTreeMap reverseKeyMap;

    public TradeMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_TRADE_TYPE);
            if (databaseSet.isDynamicGUI()) {
                this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_TRADE_TYPE);
                this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRADE_TYPE);
            }
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ORDER_TYPE);
        }
    }

    public TradeMap(TradeMap parent, DCSet dcSet) {
        super(parent, dcSet);

    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> getMap(DB database) {
        //OPEN MAP
        return this.openMap(database);
    }

    @Override
    protected Map<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        return this.openMap(database);
    }

    @SuppressWarnings("unchecked")
    private Map<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> openMap(final DB database) {
        //OPEN MAP

        /* EXAMPLE for Long, Long
        Fun.Tuple2Comparator<Long, Long> comparator = new Fun.Tuple2Comparator<Long, Long>(Fun.COMPARATOR,
                UnsignedBytes.lexicographicalComparator());
        NavigableSet<Tuple2<Integer, Long>> heightIndex = database.createTreeSet("transactions_index_timestamp")
                .comparator(comparator).makeOrGet();
                */

        BTreeMap<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> map = database.createTreeMap("trades")
                //.comparator(new Fun.Tuple2Comparator(Fun.BYTE_ARRAY_COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR)) - for Tuple2<byte[]m byte[]>
                .comparator(Fun.TUPLE2_COMPARATOR)
                .makeOrGet();


        //CHECK IF NOT MEMORY DATABASE
        if (parent == null) {
            //PAIR KEY
            this.pairKeyMap = database.createTreeMap("trades_key_pair")
                    .comparator(Fun.COMPARATOR)
                    .makeOrGet();

            //BIND PAIR KEY
            Bind.secondaryKey(map, this.pairKeyMap, new Fun.Function2<Tuple2<String, Long>, Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>() {
                @Override
                public Tuple2<String, Long> run(Tuple2<Long, Long> key, Tuple5<Long, Long, BigDecimal, BigDecimal, Long> value) {
                    Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
                            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = Order.getOrder(getDCSet(), value.a);
                    long have = order.b.a;
                    long want = order.c.a;
                    String pairKey;
                    if (have > want) {
                        pairKey = have + "/" + want;
                    } else {
                        pairKey = want + "/" + have;
                    }

                    return new Tuple2<String, Long>(pairKey, Long.MAX_VALUE - value.e);
                }
            });

            //
            this.wantKeyMap = database.createTreeMap("trades_key_want")
                    .comparator(Fun.COMPARATOR)
                    .makeOrGet();

            //BIND
            Bind.secondaryKey(map, this.wantKeyMap, new Fun.Function2<Tuple2<String, Long>, Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>() {
                @Override
                public Tuple2<String, Long> run(Tuple2<Long, Long> key, Tuple5<Long, Long, BigDecimal, BigDecimal, Long> value) {
                    Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
                            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = Order.getOrder(getDCSet(), value.a);
                    long want = order.c.a;

                    String wantKey;
                    wantKey = String.valueOf(want);

                    return new Tuple2<String, Long>(wantKey, Long.MAX_VALUE - value.e);
                }
            });

            //
            this.haveKeyMap = database.createTreeMap("trades_key_have")
                    .comparator(Fun.COMPARATOR)
                    .makeOrGet();

            //BIND
            Bind.secondaryKey(map, this.haveKeyMap, new Fun.Function2<Tuple2<String, Long>, Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>() {
                @Override
                public Tuple2<String, Long> run(Tuple2<Long, Long> key, Tuple5<Long, Long, BigDecimal, BigDecimal, Long> value) {
                    Tuple3<Tuple5<Long, String, Long, Boolean, BigDecimal>,
                            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = Order.getOrder(getDCSet(), value.a);
                    long have = order.b.a;

                    String haveKey;
                    haveKey = String.valueOf(have);

                    return new Tuple2<String, Long>(haveKey, Long.MAX_VALUE - value.e);
                }
            });

            //REVERSE KEY
            this.reverseKeyMap = database.createTreeMap("trades_key_reverse")
                    .comparator(new Fun.Tuple2Comparator(Fun.BYTE_ARRAY_COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR))
                    .makeOrGet();

            //BIND REVERSE KEY
            Bind.secondaryKey(map, this.reverseKeyMap, new Fun.Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>() {
                @Override
                public Tuple2<Long, Long> run(Tuple2<Long, Long> key, Tuple5<Long, Long, BigDecimal, BigDecimal, Long> value) {

                    return new Tuple2<Long, Long>(key.b, key.a);
                }
            });
            Bind.secondaryKey(map, this.reverseKeyMap, new Fun.Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>() {
                @Override
                public Tuple2<Long, Long> run(Tuple2<Long, Long> key, Tuple5<Long, Long, BigDecimal, BigDecimal, Long> value) {
                    return new Tuple2<Long, Long>(key.a, key.b);
                }
            });
        }

        //RETURN
        return map;
    }

    @Override
    protected Tuple5<Long, Long, BigDecimal, BigDecimal, Long> getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public void add(Tuple5<Long, Long, BigDecimal, BigDecimal, Long> trade) {
        this.set(new Tuple2<Long, Long>(trade.a, trade.b), trade);
    }

    @SuppressWarnings("unchecked")
    private Collection<Tuple2> getKeys(Order order) {

        Map uncastedMap = this.map;

        //FILTER ALL KEYS
        Collection<Tuple2> keys = ((BTreeMap<Tuple2, Order>) uncastedMap).subMap(
                Fun.t2(order.getId(), null),
                Fun.t2(order.getId(), Fun.HI())).keySet();

        //IF THIS IS A FORK
        if (this.parent != null) {
            //GET ALL KEYS FOR FORK
            Collection<Tuple2> forkKeys = ((TradeMap) this.parent).getKeys(order);

            //COMBINE LISTS
            Set<Tuple2> combinedKeys = new TreeSet<Tuple2>(keys);
            combinedKeys.addAll(forkKeys);

            if (this.deleted != null) {
                //DELETE DELETED
                for (Tuple2 deleted : this.deleted) {
                    combinedKeys.remove(deleted);
                }
            }

            //CONVERT SET BACK TO COLLECTION
            keys = combinedKeys;
        }

        return keys;
    }

    @SuppressWarnings("unchecked")
    public List<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> getInitiatedTrades(Order order) {
        //FILTER ALL TRADES
        Collection<Tuple2> keys = this.getKeys(order);

        //GET ALL TRADES FOR KEYS
        List<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> trades = new ArrayList<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>();
        for (Tuple2 key : keys) {
            trades.add(this.get(key));
        }

        //RETURN
        return trades;
    }

    @SuppressWarnings("unchecked")
    public SortableList<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> getTrades(Long orderID) {
        //ADD REVERSE KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple2, Tuple2<Long, Long>>) this.reverseKeyMap).subMap(
                Fun.t2(orderID, null),
                Fun.t2(orderID, Fun.HI())).values();

        //RETURN
        return new SortableList<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>(this, keys);
    }

    @SuppressWarnings("unchecked")
    public SortableList<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> getTradesByOrderID(Long orderID) {
        //ADD REVERSE KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple2, Tuple2<Long, Long>>) this.reverseKeyMap).subMap(
                Fun.t2(orderID, null),
                Fun.t2(orderID, Fun.HI())).values();

        //RETURN
        return new SortableList<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>(this, keys);
    }


    @SuppressWarnings("unchecked")
    public List<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> getTrades(long haveWant)
    // get trades for order as HAVE and as WANT
    {

        String haveKey = String.valueOf(haveWant);
        HashSet<Tuple2<Long, Long>> tradesKeys = new HashSet<Tuple2<Long, Long>>(((BTreeMap<Tuple2, Tuple2<Long, Long>>)
                this.haveKeyMap).subMap(
                    Fun.t2(haveKey, null),
                    Fun.t2(haveKey, Fun.HI())).values());

        String wantKey = String.valueOf(haveWant);

        tradesKeys.addAll(((BTreeMap<Tuple2, Tuple2<Long, Long>>) this.wantKeyMap).subMap(
                Fun.t2(wantKey, null),
                Fun.t2(wantKey, Fun.HI())).values());

        //GET ALL ORDERS FOR KEYS
        List<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> trades = new ArrayList<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>();

        for (Tuple2<Long, Long> tradeKey : tradesKeys) {
            trades.add(this.get(tradeKey));
        }

        //RETURN
        return trades;
    }

    @SuppressWarnings("unchecked")
    public SortableList<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> getTradesSortableList(long have, long want) {
        String pairKey;
        if (have > want) {
            pairKey = have + "/" + want;
        } else {
            pairKey = want + "/" + have;
        }

        //FILTER ALL KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple2, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t2(pairKey, null),
                Fun.t2(pairKey, Fun.HI())).values();

        //RETURN
        return new SortableList<Tuple2<Long, Long>, Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>(this, keys);
    }

    @SuppressWarnings("unchecked")
    public List<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> getTrades(long have, long want) {
        String pairKey;
        if (have > want) {
            pairKey = have + "/" + want;
        } else {
            pairKey = want + "/" + have;
        }

        //FILTER ALL KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple2, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t2(pairKey, null),
                Fun.t2(pairKey, Fun.HI())).values();

        List<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> trades = new ArrayList<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>();

        for (Tuple2<Long, Long> key : keys) {
            trades.add(this.get(key));
        }

        //RETURN
        return trades;
    }

    /**
     * Get transaction by timestamp
     *
     * @param have      include
     * @param want      wish
     * @param timestamp is time
     */
    public List<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> getTradesByTimestamp(long have, long want, long timestamp) {
        String pairKey;
        if (have > want)
            pairKey = have + "/" + want;
        else
            pairKey = want + "/" + have;

        //FILTER ALL KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple2, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t2(pairKey, timestamp),
                Fun.t2(pairKey, Fun.HI())).values();

        List<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>> trades = new ArrayList<Tuple5<Long, Long, BigDecimal, BigDecimal, Long>>();

        for (Tuple2<Long, Long> key : keys) {
            trades.add(this.get(key));
        }

        //RETURN
        return trades;
    }

    public void delete(Trade trade) {
        this.delete(new Tuple2<Long, Long>(trade.getInitiator(), trade.getTarget()));
    }
}
