package database.wallet;

import core.account.Account;
import core.item.assets.Order;
import database.DBMap;
import datachain.DCMap;
import datachain.IDB;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.ObserverMessage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Tuple4
 * 	private BigInteger id;
	private Account creator;
	protected long timestamp;
	private boolean isExecutable = true;

Tuple3
	private long have;
	private BigDecimal amountHave;
	private BigDecimal fulfilledHave;

Tuple3
	private long want;
	private BigDecimal amountWant;
	private BigDecimal fulfilledWant;

 */
public class OrderMap extends DCMap<Tuple2<String, BigInteger>,
        Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public OrderMap(IDB databaseSet, DB database) {
        super(databaseSet, database);

        this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.WALLET_RESET_ORDER_TYPE);
        this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.WALLET_ADD_ORDER_TYPE);
        this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.WALLET_REMOVE_ORDER_TYPE);
        this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.WALLET_LIST_ORDER_TYPE);
    }

    public OrderMap(OrderMap parent) {
        super(parent, null);

    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Tuple2<String, BigInteger>, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getMap(DB database) {
        //OPEN MAP
        return this.openMap(database);
    }

    @Override
    protected Map<Tuple2<String, BigInteger>, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        return this.openMap(database);
    }

    private Map<Tuple2<String, BigInteger>, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> openMap(DB database) {
        //OPEN MAP
        BTreeMap<Tuple2<String, BigInteger>, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> map = database.createTreeMap("orders")
                //.keySerializer(BTreeKeySerializer.TUPLE2)
                //.valueSerializer(new OrderSerializer())
                .makeOrGet();

        //RETURN
        return map;
    }

    @Override
    protected Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public void add(Order order) {

        this.set(new Tuple2<String, BigInteger>(order.getCreator().getAddress(), order.getId()),
                new Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                        Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>(
                        new Tuple5<BigInteger, String, Long, Boolean, BigDecimal>(order.getId(), order.getCreator().getAddress(), order.getTimestamp(), order.isExecutable(), order.getPrice()),
                        new Tuple3<Long, BigDecimal, BigDecimal>(order.getHave(), order.getAmountHave(), order.getFulfilledHave()),
                        new Tuple2<Long, BigDecimal>(order.getWant(), order.getAmountWant())
                ));
    }

    public void add(Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
            Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order) {

        this.set(new Tuple2<String, BigInteger>(order.a.b, order.a.a), order);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Account account) {
        //GET ALL ORDERS THAT BELONG TO THAT ADDRESS
        Map<Tuple2<String, BigInteger>, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> accountOrders = ((BTreeMap) this.map).subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI()));

        //DELETE NAMES
        for (Tuple2<String, BigInteger> key : accountOrders.keySet()) {
            this.delete(key);
        }
    }

    public void delete(Order order) {
        this.delete(new Tuple2<String, BigInteger>(order.getCreator().getAddress(), order.getId()));
    }

    public void deleteAll(List<Account> accounts) {
        for (Account account : accounts) {
            this.delete(account);
        }
    }

    public void addAll(Map<Account, List<Order>> orders) {
        //FOR EACH ACCOUNT
        for (Account account : orders.keySet()) {
            //FOR EACH TRANSACTION
            for (Order order : orders.get(account)) {
                this.add(order);
            }
        }
    }
}
