package org.erachain.datachain;

import org.erachain.core.item.assets.Order;

import java.util.Iterator;
import java.util.List;

public interface OrderMapSuit {

    Iterator<Long> getHaveWantIterator(long have, long want);

    Iterator<Long> getHaveWantIterator(long have);

    Iterator<Long> getWantHaveIterator(long want, long have);

    Iterator<Long> getWantHaveIterator(long want);

    List<Long> getSubKeysWithParent(long have, long want);
    List<Order> getOrdersForTradeWithFork(long have, long want, boolean reverse);

    Iterator<Long> getAddressHaveWantIterator(String address, long have, long want);
}
