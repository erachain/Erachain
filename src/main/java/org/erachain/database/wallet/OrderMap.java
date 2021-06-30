package org.erachain.database.wallet;

import org.erachain.core.item.assets.Order;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.OrderSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DCUMapImpl;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeMap;
import org.mapdb.DB;

import java.util.Map;

public class OrderMap extends DCUMapImpl<Long, Order> {

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

        HI = Long.MAX_VALUE;
        LO = 0L;

        //OPEN MAP
        map = this.openMap(database);
    }

    @Override
    protected void getMemoryMap() {
    }

    private Map<Long, Order> openMap(DB database) {
        //OPEN MAP
        BTreeMap<Long, Order> map = database.createTreeMap("orders")
                .valueSerializer(new OrderSerializer())
                .makeOrGet();

        //RETURN
        return map;
    }

    public void add(Order order) {
        this.set(order.getId(), order);
    }


    public void delete(Order order) {
        this.delete(order.getId());
    }

    // UPDATE FULFILL if was WALLET SYNCHRONIZATION
    public void updateLefts() {

        DCSet dcSet = DCSet.getInstance();
        Order order;
        Order orderFromChain;
        for (Long key : map.keySet()) {
            order = map.get(key);

            if (dcSet.getOrderMap().contains(key))
                // ACTIVE
                orderFromChain = dcSet.getOrderMap().get(key);
            else
                // CANCELED TOO
                orderFromChain = dcSet.getCompletedOrderMap().get(key);

            if (orderFromChain == null || orderFromChain.getFulfilledHave().compareTo(order.getFulfilledHave()) == 0)
                continue;

            this.set(key, orderFromChain);
        }
    }
}
