package org.erachain.datachain;

import org.erachain.core.item.assets.Order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public interface OrderSuit {

    Iterator<Long> getHaveWantIterator(long have, long want);

    Iterator<Long> getHaveWantIterator(long have);

    Iterator<Long> getWantHaveIterator(long want, long have);

    Iterator<Long> getWantHaveIterator(long want);

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

    Iterator<Long> getAddressHaveWantIterator(String address, long have, long want);
}
