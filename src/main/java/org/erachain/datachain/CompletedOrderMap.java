package org.erachain.datachain;

import org.erachain.core.item.assets.Order;
import org.erachain.dbs.DBTab;

import java.util.List;

public interface CompletedOrderMap extends DBTab<Long, Order> {
    void put(Order order);

    void delete(Order order);

    List<Order> getOrders(long have, long want, int offset, int limit);

    List<Order> getOrdersByTimestamp(long have, long want, long startTimestamp, long stopTimestamp, int limit);

    List<Order> getOrdersByOrderID(long have, long want, long start, long stop, int limit);

    List<Order> getOrdersByHeight(long have, long want, int start, int stop, int limit);


}
