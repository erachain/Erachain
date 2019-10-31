package org.erachain.datachain;

import org.erachain.core.item.assets.Order;
import org.erachain.dbs.DBTab;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

public interface OrderMap extends DBTab<Long, Order> {

    long getCount(long have, long want);

    long getCountHave(long have);

    @SuppressWarnings({"unchecked", "rawtypes"})
    long getCountWant(long want);

    List<Order> getOrders(long haveWant);

    long getCountOrders(long haveWant);

    HashSet<Long> getSubKeysWithParent(long have, long want, BigDecimal limit);
    List<Order> getOrdersForTradeWithFork(long have, long want, BigDecimal limit);

    List<Order> getOrdersForTrade(long have, long want, boolean reverse);

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Order> getOrders(long have, long want, int limit);

    List<Order> getOrdersForAddress(String address, Long have, Long want);

    void put(Order order);

    void delete(Order order);
}
