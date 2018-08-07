package datachain;

import com.google.common.primitives.UnsignedBytes;
import core.item.assets.Order;
import core.item.assets.Trade;
import database.DBMap;
import database.serializer.OrderSerializer;
import database.serializer.TradeSerializer;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.ObserverMessage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/*
ВНИМАНИЕ !!! ВТОричные ключи не хранят дубли - тоесть запись во втричном ключе не будет учтена иперезапишется если такой же ключ прийдет
Поэтому нужно добавлять униальность

Initiator DBRef (Long) + Target DBRef (Long) -> Trade
 */
@SuppressWarnings("rawtypes")
public class TradeMap extends DCMap<Tuple2<Long, Long>, Trade> {
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
    protected Map<Tuple2<Long, Long>, Trade> getMap(DB database) {
        //OPEN MAP
        return this.openMap(database);
    }

    @Override
    protected Map<Tuple2<Long, Long>, Trade> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        return this.openMap(database);
    }

    @SuppressWarnings("unchecked")
    private Map<Tuple2<Long, Long>, Trade> openMap(final DB database) {
        //OPEN MAP

        /* EXAMPLE for Long, Long
        Fun.Tuple2Comparator<Long, Long> comparator = new Fun.Tuple2Comparator<Long, Long>(Fun.COMPARATOR,
                UnsignedBytes.lexicographicalComparator());
        NavigableSet<Tuple2<Integer, Long>> heightIndex = database.createTreeSet("transactions_index_timestamp")
                .comparator(comparator).makeOrGet();

        //.comparator(new Fun.Tuple2Comparator(Fun.BYTE_ARRAY_COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR)) - for Tuple2<byte[]m byte[]>

                */

        BTreeMap<Tuple2<Long, Long>, Trade> map = database.createTreeMap("trades")
                .valueSerializer(new TradeSerializer())
                .comparator(Fun.TUPLE2_COMPARATOR)
                //.comparator(Fun.COMPARATOR)
                .makeOrGet();


        //CHECK IF NOT MEMORY DATABASE
        if (parent == null) {
            //PAIR KEY
            this.pairKeyMap = database.createTreeMap("trades_key_pair")
                    .comparator(Fun.TUPLE3_COMPARATOR)
                    .makeOrGet();

            //BIND PAIR KEY
            Bind.secondaryKey(map, this.pairKeyMap, new Fun.Function2<Tuple3<String, Long, Integer>, Tuple2<Long, Long>, Trade>() {
                @Override
                public Tuple3<String, Long, Integer> run(Tuple2<Long, Long> key, Trade value) {
                    long have = value.getHaveKey();
                    long want = value.getWantKey();
                    String pairKey;
                    if (have > want) {
                        pairKey = have + "/" + want;
                    } else {
                        pairKey = want + "/" + have;
                    }

                    return new Tuple3<String, Long, Integer>(pairKey, Long.MAX_VALUE - value.getInitiator(),
                            Integer.MAX_VALUE - value.getSequence());
                }
            });

            //
            this.wantKeyMap = database.createTreeMap("trades_key_want")
                    .comparator(Fun.TUPLE3_COMPARATOR)
                    .makeOrGet();

            //BIND
            Bind.secondaryKey(map, this.wantKeyMap, new Fun.Function2<Tuple3<String, Long, Integer>, Tuple2<Long, Long>, Trade>() {
                @Override
                public Tuple3<String, Long, Integer> run(Tuple2<Long, Long> key, Trade value) {
                    long want = value.getWantKey();

                    String wantKey;
                    wantKey = String.valueOf(want);

                    return new Tuple3<String, Long, Integer>(wantKey, Long.MAX_VALUE - value.getInitiator(),
                            Integer.MAX_VALUE - value.getSequence());
                }
            });

            //
            this.haveKeyMap = database.createTreeMap("trades_key_have")
                    .comparator(Fun.TUPLE3_COMPARATOR)
                    .makeOrGet();

            //BIND
            Bind.secondaryKey(map, this.haveKeyMap, new Fun.Function2<Tuple3<String, Long, Integer>, Tuple2<Long, Long>, Trade>() {
                @Override
                public Tuple3<String, Long, Integer> run(Tuple2<Long, Long> key, Trade value) {
                    long have = value.getHaveKey(); //order.getHave();

                    String haveKey;
                    haveKey = String.valueOf(have);

                    return new Tuple3<String, Long, Integer>(haveKey, Long.MAX_VALUE - value.getInitiator(),
                            Integer.MAX_VALUE - value.getSequence());
                }
            });

            //REVERSE KEY
            this.reverseKeyMap = database.createTreeMap("trades_key_reverse")
                    //.comparator(new Fun.Tuple2Comparator(Fun.BYTE_ARRAY_COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR))
                    .comparator(Fun.TUPLE2_COMPARATOR)
                    .makeOrGet();

            //BIND REVERSE KEY
            Bind.secondaryKey(map, this.reverseKeyMap, new Fun.Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Trade>() {
                @Override
                public Tuple2<Long, Long> run(Tuple2<Long, Long> key, Trade value) {

                    return new Tuple2<Long, Long>(key.b, key.a);
                }
            });
            Bind.secondaryKey(map, this.reverseKeyMap, new Fun.Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Trade>() {
                @Override
                public Tuple2<Long, Long> run(Tuple2<Long, Long> key, Trade value) {
                    return new Tuple2<Long, Long>(key.a, key.b);
                }
            });
        }

        //RETURN
        return map;
    }

    @Override
    protected Trade getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public void add(Trade trade) {
        this.set(new Tuple2<Long, Long>(trade.getInitiator(), trade.getTarget()), trade);
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
    public List<Trade> getInitiatedTrades(Order order) {
        //FILTER ALL TRADES
        Collection<Tuple2> keys = this.getKeys(order);

        //GET ALL TRADES FOR KEYS
        List<Trade> trades = new ArrayList<Trade>();
        for (Tuple2 key : keys) {
            trades.add(this.get(key));
        }

        //RETURN
        return trades;
    }

    @SuppressWarnings("unchecked")
    public SortableList<Tuple2<Long, Long>, Trade> getTrades(Long orderID) {
        //ADD REVERSE KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.reverseKeyMap).subMap(
                Fun.t3(orderID, null, null),
                Fun.t3(orderID, Fun.HI(), Fun.HI())).values();

        //RETURN
        return new SortableList<Tuple2<Long, Long>, Trade>(this, keys);
    }

    @SuppressWarnings("unchecked")
    public SortableList<Tuple2<Long, Long>, Trade> getTradesByOrderID(Long orderID) {
        //ADD REVERSE KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.reverseKeyMap).subMap(
                Fun.t3(orderID, null, null),
                Fun.t3(orderID, Fun.HI(), Fun.HI())).values();

        //RETURN
        return new SortableList<Tuple2<Long, Long>, Trade>(this, keys);
    }


    @SuppressWarnings("unchecked")
    public List<Trade> getTrades(long haveWant)
    // get trades for order as HAVE and as WANT
    {

        String haveKey = String.valueOf(haveWant);
        HashSet<Tuple2<Long, Long>> tradesKeys = new HashSet<Tuple2<Long, Long>>(((BTreeMap<Tuple3, Tuple2<Long, Long>>)
                this.haveKeyMap).subMap(
                    Fun.t3(haveKey, null, null),
                    Fun.t3(haveKey, Fun.HI(), Fun.HI())).values());

        String wantKey = String.valueOf(haveWant);

        tradesKeys.addAll(((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.wantKeyMap).subMap(
                Fun.t3(wantKey, null, null),
                Fun.t3(wantKey, Fun.HI(), Fun.HI())).values());

        //GET ALL ORDERS FOR KEYS
        List<Trade> trades = new ArrayList<Trade>();

        for (Tuple2<Long, Long> tradeKey : tradesKeys) {
            trades.add(this.get(tradeKey));
        }

        //RETURN
        return trades;
    }

    @SuppressWarnings("unchecked")
    public SortableList<Tuple2<Long, Long>, Trade> getTradesSortableList(long have, long want) {

        String pairKey;
        if (have > want) {
            pairKey = have + "/" + want;
        } else {
            pairKey = want + "/" + have;
        }

        //FILTER ALL KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t3(pairKey, null, null),
                Fun.t3(pairKey, Fun.HI(), Fun.HI())).values();

        //RETURN
        return new SortableList<Tuple2<Long, Long>, Trade>(this, keys);
    }

    @SuppressWarnings("unchecked")
    public List<Trade> getTrades(long have, long want) {
        String pairKey;
        if (have > want) {
            pairKey = have + "/" + want;
        } else {
            pairKey = want + "/" + have;
        }

        //FILTER ALL KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t3(pairKey, null, null),
                Fun.t3(pairKey, Fun.HI(), Fun.HI())).values();

        List<Trade> trades = new ArrayList<Trade>();

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
    public List<Trade> getTradesByTimestamp(long have, long want, long timestamp) {
        String pairKey;
        if (have > want)
            pairKey = have + "/" + want;
        else
            pairKey = want + "/" + have;

        //FILTER ALL KEYS
        Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.pairKeyMap).subMap(
                Fun.t3(pairKey, timestamp, timestamp),
                Fun.t3(pairKey, Fun.HI(), Fun.HI())).values();

        List<Trade> trades = new ArrayList<Trade>();

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
