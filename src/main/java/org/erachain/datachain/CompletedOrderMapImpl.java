package org.erachain.datachain;

import org.erachain.core.item.assets.Order;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.CompletedOrdersSuitMapDB;
import org.erachain.dbs.mapDB.CompletedOrdersSuitMapDBFork;
import org.erachain.dbs.rocksDB.CompletedOrdersSuitRocksDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Хранит исполненные ордера, или отмененные - все что уже не активно<br>
 * <br>
 * Ключ: ссылка на запись создания заказа<br>
 * Значение: заказ<br>
 */
public class CompletedOrderMapImpl extends DBTabImpl<Long, Order> implements CompletedOrderMap {

    public CompletedOrderMapImpl(int dbs, DCSet databaseSet, DB database) {
        super(dbs, databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_COMPL_ORDER_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_COMPL_ORDER_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_COMPL_ORDER_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_COMPL_ORDER_TYPE);
        }
    }

    public CompletedOrderMapImpl(int dbs, CompletedOrderMap parent, DCSet dcSet) {
        super(dbs, parent, dcSet);
    }

    @Override
    protected void openMap() {
        // OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new CompletedOrdersSuitRocksDB(databaseSet, database);
                    break;
                default:
                    map = new CompletedOrdersSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    //map = new BlocksSuitMapDBFotk((TransactionTab) parent, databaseSet);
                    //break;
                default:
                    map = new CompletedOrdersSuitMapDBFork((CompletedOrderMap) parent, databaseSet);
            }
        }
    }

    @Override
    public void add(Order order) {

        this.set(order.getId(), order);
    }

    @Override
    public void delete(Order order) {

        this.remove(order.getId());
    }
}
