package org.erachain.datachain;

import com.google.common.collect.Iterators;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.OrderComparatorForTrade;
import org.erachain.core.item.assets.OrderComparatorForTradeReverse;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.mapDB.OrdersSuitMapDB;
import org.erachain.dbs.mapDB.OrdersSuitMapDBFork;
import org.erachain.dbs.rocksDB.OrdersSuitRocksDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

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
    public void openMap() {
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
                    //тут будет ошибка
                    // см issues/1276 map = new NativeMapTreeMapFork(parent, databaseSet, null, null); - просто карту нельзя так как тут особые вызовы
                    //break;
                default:
                    map = new OrdersSuitMapDBFork((OrderMap) parent, databaseSet);
            }
        }
    }

    @Override
    public int getCount(long have, long want) {
        if (Controller.getInstance().onlyProtocolIndexing) {
            return 0;
        }
        try (IteratorCloseable iterator = ((OrderSuit) map).getHaveWantIterator(have, want)) {
            return Iterators.size(iterator);
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public int getCount(long have, long want, int limit) {
        if (Controller.getInstance().onlyProtocolIndexing) {
            return 0;
        }
        try (IteratorCloseable iterator = ((OrderSuit) map).getHaveWantIterator(have, want)) {
            return Iterators.size(Iterators.limit(iterator, limit));
        } catch (IOException e) {
            return 0;
        }
    }


    @Override
    public int getCountHave(long have, int limit) {
        if (Controller.getInstance().onlyProtocolIndexing) {
            return 0;
        }
        try (IteratorCloseable iterator = ((OrderSuit) map).getHaveWantIterator(have)) {
            return Iterators.size(Iterators.limit(iterator, limit));
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public int getCountWant(long want, int limit) {
        try (IteratorCloseable iterator = ((OrderSuit) map).getWantHaveIterator(want)) {
            return Iterators.size(Iterators.limit(iterator, limit));
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public List<Order> getOrders(long haveWant) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return new ArrayList<>();
        }

        //GET ALL ORDERS FOR KEYS
        List<Order> orders = new ArrayList<Order>();

        try (IteratorCloseable<Long> iterator = ((OrderSuit) map).getHaveWantIterator(haveWant)) {
            while (iterator.hasNext()) {
                orders.add(map.get(iterator.next()));
            }
        } catch (IOException e) {
        }

        try (IteratorCloseable<Long> iterator = ((OrderSuit) map).getWantHaveIterator(haveWant)) {
            while (iterator.hasNext()) {
                orders.add(map.get(iterator.next()));
            }
        } catch (IOException e) {
        }

        return orders;
    }

    @Override
    public int getCountOrders(long haveWant) {
        if (Controller.getInstance().onlyProtocolIndexing) {
            return 0;
        }

        return this.getCountHave(haveWant, 200) + this.getCountWant(haveWant, 200);
    }


    @Override
    public HashMap<Long, Order> getProtocolEntries(long have, long want, BigDecimal stopPrice, Map deleted) {
        return ((OrderSuit) map).getUnsortedEntries(have, want, stopPrice, deleted);
    }

    @Override
    public List<Order> getOrdersForTradeWithFork(long have, long want, BigDecimal stopPrice) {

        //FILTER ALL KEYS
        HashMap<Long, Order> unsortedEntries = ((OrderSuit) map).getUnsortedEntries(have, want, stopPrice, null);

        //GET ALL ORDERS FOR KEYS
        List<Order> orders = new ArrayList<Order>();

        for (Order order : unsortedEntries.values()) {
            if (order != null) {
                orders.add(order);
            } else {
                // возможно произошло удаление в момент запроса??
            }
        }

        Collections.sort(orders, new OrderComparatorForTrade());

        //RETURN
        return orders;
    }

    @Override
    public List<Order> getOrdersForTrade(long have, long want, boolean reverse) {
        //FILTER ALL KEYS
        HashMap<Long, Order> unsortedEntries = ((OrderSuit) map).getUnsortedEntries(have, want, null, null);

        //GET ALL ORDERS FOR KEYS
        List<Order> orders = new ArrayList<Order>();

        for (Order order : unsortedEntries.values()) {
            if (order != null) {
                orders.add(order);
            } else {
                // возможно произошло удаление в момент запроса??
            }
        }

        if (reverse) {
            Collections.sort(orders, new OrderComparatorForTradeReverse());
        } else {
            Collections.sort(orders, new OrderComparatorForTrade());
        }

        //RETURN
        return orders;
    }

    @Override
    public List<Order> getOrders(long have, long want, int limit) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return new ArrayList<>();
        }

        List<Order> orders = new ArrayList<>();
        try (IteratorCloseable<Long> iterator = ((OrderSuit) map).getHaveWantIterator(have, want)) {

            int counter = limit;
            while (iterator.hasNext()) {
                orders.add(get(iterator.next()));
                if (limit > 0 && --counter < 0)
                    break;
            }
        } catch (IOException e) {
        }

        return orders;
    }

    @Override
    public Order getHaveWanFirst(long have, long want) {
        return ((OrderSuit) map).getHaveWanFirst(have, want);
    }

    @Override
    public IteratorCloseable<Long> iteratorByAssetKey(long assetKey, boolean descending) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        return ((OrderSuit) this.map).getIteratorByAssetKey(assetKey, descending);

    }


    @Override
    public List<Order> getOrdersForAddress(
            String address, Long have, Long want, int limit) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return new ArrayList<>();
        }

        List<Order> orders = new ArrayList<Order>();
        try (IteratorCloseable<Long> iterator = ((OrderSuit) map).getAddressHaveWantIterator(address, have, want)) {

            //GET ALL ORDERS FOR KEYS

            int counter = limit;
            while (iterator.hasNext()) {

                Long key = iterator.next();
                Order order = this.get(key);

                // MAY BE NULLS!!!
                if (order != null)
                    orders.add(this.get(key));

                if (limit > 0 && --counter < 0)
                    break;
            }
        } catch (IOException e) {
        }

        return orders;

    }

    @Override
    public Set<Long> getKeysForAddressFromID(String address, long fromOrder, int limit) {

        Set<Long> keys = new TreeSet<Long>();

        if (Controller.getInstance().onlyProtocolIndexing) {
            return keys;
        }

        try (IteratorCloseable<Long> iterator = ((OrderSuit) map).getAddressIterator(address)) {

            //GET ALL ORDERS FOR KEYS
            int counter = limit;
            while (iterator.hasNext()) {

                Long key = iterator.next();
                if (fromOrder > 0 && key < fromOrder)
                    continue;

                Order order = this.get(key);

                // MAY BE NULLS!!!
                if (order != null)
                    keys.add(key);

                if (limit > 0 && --counter < 0)
                    break;

            }
        } catch (IOException e) {
        }

        return keys;
    }

    @Override
    public List<Order> getOrdersForAddress(String address, int limit) {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return new ArrayList<>();
        }

        List<Order> orders = new ArrayList<Order>();
        try (IteratorCloseable<Long> iterator = ((OrderSuit) map).getAddressIterator(address)) {

            //GET ALL ORDERS FOR KEYS
            int counter = limit;
            while (iterator.hasNext()) {

                Long key = iterator.next();
                Order order = this.get(key);

                // MAY BE NULLS!!!
                if (order != null)
                    orders.add(this.get(key));

                if (limit > 0 && --counter < 0)
                    break;

            }
        } catch (IOException e) {
        }

        return orders;

    }

    @Override
    public Order get(Long id) {
        Order order = super.get(id);
        if (order != null) {
            if (order.isNotTraded()) {
                order.setStatus(Order.OPENED);
            } else {
                order.setStatus(Order.FULFILLED);
            }
        }
        return order;
    }

    @Override
    public boolean set(Long id, Order order) {
        if (BlockChain.CHECK_BUGS > -1) {
            if (((DCSet) this.getDBSet()).getCompletedOrderMap().contains(id)) {
                // если он есть в уже завершенных
                LOGGER.error("already in Completed");
                Long error = null;
                ++error;
            }
        }

        return super.set(id, order);
    }

    @Override
    public void put(Long id, Order order) {
        if (BlockChain.CHECK_BUGS > -1) {
            if (((DCSet) this.getDBSet()).getCompletedOrderMap().contains(id)) {
                // если он есть в уже завершенных
                LOGGER.error("already in Completed");
                Long error = null;
                ++error;
            }
        }

        super.put(id, order);
    }

    @Override
    public Order remove(Long id) {
        if (BlockChain.CHECK_BUGS > -1) {
            if (((DCSet) this.getDBSet()).getCompletedOrderMap().contains(id)) {
                // если он есть в уже завершенных
                LOGGER.error("already in Completed");
                Long error = null;
                ++error;
            }
        }
        return super.remove(id);
    }

    @Override
    public void delete(Long id) {
        if (BlockChain.CHECK_BUGS > -1) {
            if (((DCSet) this.getDBSet()).getCompletedOrderMap().contains(id)) {
                // если он есть в уже завершенных
                LOGGER.error("Order [" + Transaction.viewDBRef(id) + "] already in Completed");
                Long error = null;
                ++error;
            }
        }
        super.delete(id);
    }

    @Override
    public void put(Order order) {
        this.put(order.getId(), order);
    }

    @Override
    public void delete(Order order) {
        this.delete(order.getId());
    }
}
