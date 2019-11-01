package org.erachain.datachain;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;

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
     * @return
     */
    HashSet<Long> getUnsortedKeysWithParent(long have, long want, BigDecimal limit);

    Iterator<Long> getAddressHaveWantIterator(String address, long have, long want);
}
