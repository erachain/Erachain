package org.erachain.database.wallet;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBMap;
import org.erachain.database.IDB;
import org.erachain.database.serializer.OrderSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;

import java.util.Collection;
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
public class OrderMap extends DBMap<Tuple2<String, Long>, Order> {

    BTreeMap AUTOKEY_INDEX;
    private Atomic.Long atomicKey;

    public OrderMap(IDB databaseSet, DB database) {
        super(databaseSet, database);

        this.atomicKey = database.getAtomicLong("OrderMap_atomicKey");

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.WALLET_RESET_ORDER_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.WALLET_LIST_ORDER_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.WALLET_ADD_ORDER_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.WALLET_REMOVE_ORDER_TYPE);
        }
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

        this.AUTOKEY_INDEX = database.createTreeMap("dw_transactions_AUTOKEY_INDEX")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND
        Bind.secondaryKey(map, this.AUTOKEY_INDEX, new Fun.Function2<Long, Tuple2<String, Long>, Order>() {
            @Override
            public Long run(Tuple2<String, Long> key, Order value) {
                return -atomicKey.get();
            }
        });

        //RETURN
        return map;
    }

    @Override
    protected Order getDefaultValue() {
        return null;
    }

    public Collection<Tuple2<String, Long>> getFromToKeys(long fromKey, long toKey) {
        return AUTOKEY_INDEX.subMap(fromKey, toKey).values();
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

    public Order delete(Tuple2<String, Long> key) {
        if (this.atomicKey != null) {
            this.atomicKey.decrementAndGet();
        }
        return super.delete(key);
    }

    public void deleteAll(List<Account> accounts) {
        for (Account account : accounts) {
            this.delete(account);
        }
    }

    public boolean set(Tuple2<String, Long> key, Order order) {
        if (this.atomicKey != null) {
            this.atomicKey.incrementAndGet();
        }
        return super.set(key, order);
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
