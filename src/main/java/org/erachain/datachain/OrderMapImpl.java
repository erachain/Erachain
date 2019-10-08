package org.erachain.datachain;

import com.google.common.collect.Iterators;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.Order;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.OrdersSuitMapDB;
import org.erachain.dbs.mapDB.OrdersSuitMapDBFork;
import org.erachain.dbs.nativeMemMap.nativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.OrdersSuitRocksDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Хранение ордеров на бирже
 * Ключ: ссылка на запись создавшую заказ
 * Значение: Ордер
 * <p>
 * ВНИМАНИЕ !!! ВТОричные ключи не хранят дубли если созданы вручную а не
 * в mapDB.DBMapSuit#createIndex() (тут первичный ключ добавится автоматически)
 * - тоесть запись во втричном ключе не будет учтена иперезапишется если такой же ключ прийдет
 * Поэтому нужно добавлять униальность
 *
 * @return
 */
public class OrderMapImpl extends DBTabImpl<Long, Order> implements OrderMap {

    public OrderMapImpl(int dbsUsed, DCSet databaseSet, DB database) {
        super(dbsUsed, databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_ORDER_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_ORDER_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_ORDER_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_ORDER_TYPE);
        }
    }

    public OrderMapImpl(int dbsUsed, OrderMap parent, DCSet dcSet) {
        super(dbsUsed, parent, dcSet);
    }

    @Override
    protected void getMap() {
        // OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new OrdersSuitRocksDB(databaseSet, database);
                    break;
                default:
                    map = new OrdersSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new nativeMapTreeMapFork(parent, databaseSet, null, null);
                    break;
                default:
                    ///map = new nativeMapTreeMapFork(parent, databaseSet); - просто карту нельзя так как тут особые вызовы
                    map = new OrdersSuitMapDBFork((OrderMap) parent, databaseSet);
            }
        }
    }

    @Override
    public long getCount(long have, long want) {
        return Iterators.size(((OrderMapSuit) map).getHaveWantIterator(have, want));
    }

    @Override
    public long getCountHave(long have) {
        return Iterators.size(((OrderMapSuit) map).getHaveWantIterator(have));
    }

    @Override
    public long getCountWant(long want) {
        return Iterators.size(((OrderMapSuit) map).getWantHaveIterator(want));
    }

    @Override
    public List<Order> getOrders(long haveWant) {

        //GET ALL ORDERS FOR KEYS
        List<Order> orders = new ArrayList<Order>();

        Iterator<Long> iterator = ((OrderMapSuit) map).getHaveWantIterator(haveWant);

        while (iterator.hasNext()) {
            orders.add(map.get(iterator.next()));
        }

        iterator = ((OrderMapSuit) map).getWantHaveIterator(haveWant);

        while (iterator.hasNext()) {
            orders.add(map.get(iterator.next()));
        }

        return orders;
    }

    @Override
    public long getCountOrders(long haveWant) {

        return this.getCountHave(haveWant) + this.getCountWant(haveWant);
    }

    @Override
    public List<Long> getSubKeysWithParent(long have, long want) {
        return ((OrderMapSuit) map).getSubKeysWithParent(have, want);
    }

    @Override
    public List<Order> getOrdersForTradeWithFork(long have, long want, boolean reverse) {
        return ((OrderMapSuit) map).getOrdersForTradeWithFork(have, want, reverse);
    }

    @Override
    public List<Order> getOrders(long have, long want, int limit) {

        Iterator<Long> iterator = ((OrderMapSuit) map).getHaveWantIterator(have, want);

        iterator = Iterators.limit(iterator, limit);

        List<Order> orders = new ArrayList<>();
        while (iterator.hasNext()) {
            orders.add(get(iterator.next()));
        }

        return orders;
    }

    @Override
    public List<Order> getOrdersForAddress(
            String address, Long have, Long want) {

        Iterator<Long> iterator = ((OrderMapSuit) map).getAddressHaveWantIterator(address, have, want);

        //GET ALL ORDERS FOR KEYS
        List<Order> orders = new ArrayList<Order>();

        while (iterator.hasNext()) {

            Long key = iterator.next();
            Order order = this.get(key);

            // MAY BE NULLS!!!
            if (order != null)
                orders.add(this.get(key));
        }

        return orders;

    }

    @Override
    public boolean set(Long id, Order order) {
        if (BlockChain.CHECK_BUGS > 0) {
            if (((DCSet) this.getDBSet()).getCompletedOrderMap().contains(id)) {
                // если он есть в уже завершенных
                assert ("".equals("already in Completed"));
            }
        }

        return super.set(id, order);
    }

    @Override
    public Order remove(Long id) {
        if (BlockChain.CHECK_BUGS > 1) {
            if (((DCSet) this.getDBSet()).getCompletedOrderMap().contains(id)) {
                // если он есть в уже завершенных
                assert ("".equals("already in Completed"));
            }
        }
        return super.remove(id);
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
