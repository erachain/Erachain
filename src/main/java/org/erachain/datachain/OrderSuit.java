package org.erachain.datachain;

import org.erachain.core.item.assets.Order;
import org.erachain.dbs.IteratorCloseable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public interface OrderSuit {

    Order getHaveWanFirst(long have, long want);

    IteratorCloseable<Long> getIteratorByAssetKey(long assetKey, boolean descending);

    IteratorCloseable<Long> getHaveWantIterator(long have, long want);

    IteratorCloseable<Long> getHaveWantIterator(long have);

    IteratorCloseable<Long> getWantHaveIterator(long want, long have);

    IteratorCloseable<Long> getWantHaveIterator(long want);

    /**
     * Unsorted if call from Forked DB
     *
     * @param have
     * @param want
     * @param limit
     * @param deleted
     * @return
     */
    HashMap<Long, Order> getUnsortedEntries(long have, long want, BigDecimal limit, Map deleted);

    IteratorCloseable<Long> getAddressIterator(String address);
    IteratorCloseable<Long> getAddressHaveWantIterator(String address, long have, long want);

}
