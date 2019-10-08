package org.erachain.datachain;

import org.erachain.core.item.assets.Order;
import org.erachain.dbs.DBTab;

public interface CompletedOrderMap extends DBTab<T, U> {
    void add(Order order);

    void delete(Order order);
}
