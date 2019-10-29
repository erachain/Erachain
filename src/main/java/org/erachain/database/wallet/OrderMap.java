package org.erachain.database.wallet;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.Order;
import org.erachain.database.AutoKeyDBMap;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.LongAndOrderSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.DBTab;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

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
public class OrderMap extends AutoKeyDBMap<Tuple2<String, Long>, Tuple2<Long, Order>> {

    public OrderMap(DBASet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.WALLET_RESET_ORDER_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.WALLET_LIST_ORDER_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.WALLET_ADD_ORDER_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.WALLET_REMOVE_ORDER_TYPE);
        }
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = this.openMap(database);
    }

    @Override
    protected void getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        map = this.openMap(database);
    }

    private Map<Tuple2<String, Long>, Tuple2<Long, Order>> openMap(DB database) {
        //OPEN MAP
        BTreeMap<Tuple2<String, Long>, Tuple2<Long, Order>> map = database.createTreeMap("orders")
                //.keySerializer(BTreeKeySerializer.TUPLE2)
                .valueSerializer(new LongAndOrderSerializer())
                .makeOrGet();

        makeAutoKey(database, map, "orders");

        //RETURN
        return map;
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

    public boolean set(Tuple2<String, Long> key, Order order) {
        return super.set(key, new Tuple2<Long, Order>(null, order));
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
            Tuple2<Long, Order> item = map.get(key);
            if (item == null || item.b == null)
                continue;

            order = map.get(key).b;

            if (dcSet.getOrderMap().contains(key.b))
                // ACTIVE
                orderFromChain = dcSet.getOrderMap().get(key.b);
            else
                // CANCELED TOO
                orderFromChain = dcSet.getCompletedOrderMap().get(key.b);

            if (orderFromChain == null || orderFromChain.getFulfilledHave().compareTo(order.getFulfilledHave()) == 0)
                continue;

            this.set(key, orderFromChain);
        }
    }
}
