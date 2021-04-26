package org.erachain.datachain;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.mapDB.CompletedOrdersSuitMapDB;
import org.erachain.dbs.mapDB.CompletedOrdersSuitMapDBFork;
import org.erachain.dbs.rocksDB.CompletedOrdersSuitRocksDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public void openMap() {
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
                    //map = new BlocksSuitMapDBFotk((TransactionMap) parent, databaseSet);
                    //break;
                default:
                    map = new CompletedOrdersSuitMapDBFork((CompletedOrderMap) parent, databaseSet);
            }
        }
    }

    // TODO сделать встроенный поиск первого значения
    @Override
    public List<Order> getOrders(long have, long want, int offset, int limit) {

        List<Order> orders = new ArrayList<Order>();
        try (IteratorCloseable<Long> iterator = this.map.getIterator()) {

            int counter = limit;
            while (iterator.hasNext()) {
                Long key = iterator.next();
                if (offset > 0) {
                    offset--;
                    continue;
                }

                Order order = this.get(key);
                if (order.getHaveAssetKey() == have && order.getWantAssetKey() == want
                        || order.getHaveAssetKey() == want && order.getWantAssetKey() == have) {
                    orders.add(order);
                    if (limit > 0 && --counter < 0)
                        break;
                }
            }
        } catch (IOException e) {
        }

        return orders;
    }

    /**
     * Get trades by timestamp. From Timestamp to deep.
     *
     * @param have           include
     * @param want           wish
     * @param startTimestamp is time
     * @param stopTimestamp
     * @param limit
     */
    @Override
    public List<Order> getOrdersByTimestamp(long have, long want, long startTimestamp, long stopTimestamp, int limit) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        // тут индекс не по времени а по номерам блоков как лонг
        //int heightStart = Controller.getInstance().getMyHeight();
        //int heightEnd = heightStart - Controller.getInstance().getBlockChain().getBlockOnTimestamp(timestamp);
        int fromBlock = startTimestamp == 0 ? 0 : Controller.getInstance().getBlockChain().getHeightOnTimestampMS(startTimestamp);
        int toBlock = stopTimestamp == 0 ? 0 : Controller.getInstance().getBlockChain().getHeightOnTimestampMS(stopTimestamp);

        //RETURN
        return getOrdersByHeight(have, want, fromBlock, toBlock, limit);
    }

    @Override
    public List<Order> getOrdersByOrderID(long have, long want, long startOrderID, long stopOrderID, int limit) {

        List<Order> orders = new ArrayList<Order>();
        try (IteratorCloseable<Long> iterator = this.map.getIterator()) {

            int counter = limit;
            while (iterator.hasNext()) {
                Long key = iterator.next();
                if (startOrderID > 0 && key < startOrderID)
                    continue;
                else if (stopOrderID > 0 && key > stopOrderID)
                    break;

                Order order = this.get(key);
                if (order.getHaveAssetKey() == have && order.getWantAssetKey() == want
                        || order.getHaveAssetKey() == want && order.getWantAssetKey() == have) {
                    orders.add(order);
                    if (limit > 0 && --counter < 0)
                        break;
                }
            }
        } catch (IOException e) {
        }

        return orders;
    }

    @Override
    public List<Order> getOrdersByHeight(long have, long want, int startHeight, int stopHeight, int limit) {

        Long startOrderID = startHeight > 0 ? Transaction.makeDBRef(startHeight, 0) : 0;
        Long stopOrderID = stopHeight > 0 ? Transaction.makeDBRef(stopHeight, Integer.MAX_VALUE) : 0;

        return getOrdersByOrderID(have, want, startOrderID, stopOrderID, limit);
    }

    @Override
    public IteratorCloseable<Long> getAddressIterator(String address, Long fromOrder) {
        return ((CompletedOrderSuit) map).getAddressIterator(address, fromOrder);
    }

    @Override
    public Order get(Long id) {
        Order order = super.get(id);
        if (order != null) {
            if (order.isFulfilled() || order.getAmountWantLeft().signum() == 0) {
                order.setStatus(Order.COMPLETED);
            } else {
                order.setStatus(Order.CANCELED);
            }
        }
        return order;
    }

    @Override
    public void put(Order order) {

        if (BlockChain.CHECK_BUGS > 3
                && (Transaction.viewDBRef(order.getId()).equals("-178617-18")
                || Transaction.viewDBRef(order.getId()).equals("-125300-1"))
        ) {
            boolean debug = true;
        }

        this.put(order.getId(), order);
    }

    @Override
    public void put(Long id, Order order) {

        if (BlockChain.CHECK_BUGS > 3
                && (Transaction.viewDBRef(id).equals("178617-18")
                || Transaction.viewDBRef(id).equals("125300-1"))
        ) {
            boolean debug = true;
        }

        super.put(id, order);
    }

    @Override
    public void delete(Order order) {

        if (BlockChain.CHECK_BUGS > 3
                && (Transaction.viewDBRef(order.getId()).equals("176395-2")
                || Transaction.viewDBRef(order.getId()).equals("125300-1"))
        ) {
            boolean debug = true;
        }

        this.delete(order.getId());
    }

    @Override
    public void delete(Long id) {

        if (BlockChain.CHECK_BUGS > 3
                && (Transaction.viewDBRef(id).equals("176395-2")
                || Transaction.viewDBRef(id).equals("125300-1"))
        ) {
            boolean debug = true;
        }

        super.delete(id);
    }
}
