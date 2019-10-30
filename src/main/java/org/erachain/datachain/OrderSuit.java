package org.erachain.datachain;

import org.erachain.core.item.assets.Order;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public interface OrderSuit {

    Iterator<Long> getHaveWantIterator(long have, long want);

    Iterator<Long> getHaveWantIterator(long have);

    Iterator<Long> getWantHaveIterator(long want, long have);

    Iterator<Long> getWantHaveIterator(long want);

    HashSet<Long> getSubKeysWithParent(long have, long want);
    List<Order> getOrdersForTradeWithFork(long have, long want, boolean reverse);

    Iterator<Long> getAddressHaveWantIterator(String address, long have, long want);
}
