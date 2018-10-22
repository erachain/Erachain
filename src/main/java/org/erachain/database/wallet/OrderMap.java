package org.erachain.database.wallet;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.Order;
import org.erachain.database.DBMap;
import org.erachain.database.serializer.OrderSerializer;
import org.erachain.datachain.DCMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IDB;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import org.erachain.utils.ObserverMessage;

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
public class OrderMap extends DCMap<Tuple2<String, Long>, Order> {
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
    protected Map<Tuple2<String, Long>, Order> getMap(DB database) {
        //OPEN MAP
        return this.openMap(database);
    }

    @Override
    protected Map<Tuple2<String, Long>, Order> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        return this.openMap(database);
    }

    private Map<Tuple2<String, Long>, Order> openMap(DB database) {
        //OPEN MAP
        BTreeMap<Tuple2<String, Long>, Order> map = database.createTreeMap("orders")
                //.keySerializer(BTreeKeySerializer.TUPLE2)
                .valueSerializer(new OrderSerializer())
                .makeOrGet();

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

    public void add(Order order) {

        this.set(new Tuple2<String, Long>(order.getCreator().getAddress(), order.getId()), order);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Account account) {
        //GET ALL ORDERS THAT BELONG TO THAT ADDRESS
        Map<Tuple2<String, Long>, Order> accountOrders = ((BTreeMap) this.map).subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI()));

        //DELETE NAMES
        for (Tuple2<String, Long> key : accountOrders.keySet()) {
            this.delete(key);
        }
    }

    public void delete(Order order) {
        this.delete(new Tuple2<String, Long>(order.getCreator().getAddress(), order.getId()));
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

    // UPDATE FULFILL if was WALLET SYNCHRONIZATION
    public void updateLefts() {

        DCSet dcSet = DCSet.getInstance();
        Order order;
        Order orderFromChain;
        for (Tuple2<String, Long> key: map.keySet()) {
            order = map.get(key);
            if (dcSet.getOrderMap().contains(key.b))
                // ACTIVE
                orderFromChain = dcSet.getOrderMap().get(key.b);
            else
                // CANCELED TOO
                orderFromChain = dcSet.getCompletedOrderMap().get(key.b);

            if (orderFromChain.getFulfilledHave().compareTo(order.getFulfilledHave()) == 0)
                continue;

            this.set(key, orderFromChain);
        }
    }
}
