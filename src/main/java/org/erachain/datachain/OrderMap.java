package org.erachain.datachain;

import org.erachain.core.item.assets.Order;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OrderMap extends DBTab<Long, Order> {

    int getCount(long have, long want);

    int getCount(long have, long want, int limit);

    int getCountHave(long have, int limit);

    IteratorCloseable<Long> iteratorByAssetKey(long haveWant, boolean descending);

    @SuppressWarnings({"unchecked", "rawtypes"})
    int getCountWant(long want, int limit);

    List<Order> getOrders(long haveWant);

    int getCountOrders(long haveWant);

    HashMap<Long, Order> getProtocolEntries(long have, long want, BigDecimal limit, Map deleted);

    List<Order> getOrdersForTradeWithFork(long have, long want, BigDecimal limit);

    List<Order> getOrdersForTrade(long have, long want, boolean reverse);

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Order> getOrders(long have, long want, int limit);

    /**
     * Первый выгодный ордер - для Инфо по паре
     *
     * @param have
     * @param want
     * @return
     */
    Order getHaveWanFirst(long have, long want);

    List<Order> getOrdersForAddress(String address, Long have, Long want, int limit);

    /**
     * Тут не эффективноп ока так как Ключ в Ордерах активных ннадо делать чисто по Адресу - без ключей пары
     *
     * @param address
     * @param fromOrder
     * @param limit
     * @return
     */
    Set<Long> getKeysForAddressFromID(String address, long fromOrder, int limit);

    List<Order> getOrdersForAddress(String address, int limit);

    void put(Order order);

    void delete(Order order);
}
