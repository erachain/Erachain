package org.erachain.datachain;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;

public interface OrderSuit {

    Iterator<Long> getHaveWantIterator(long have, long want);

    Iterator<Long> getHaveWantIterator(long have);

    Iterator<Long> getWantHaveIterator(long want, long have);

    Iterator<Long> getWantHaveIterator(long want);

    HashSet<Long> getSubKeysWithParent(long have, long want, BigDecimal limit);

    Iterator<Long> getSubIteratorWithParent(long have, long want, BigDecimal limit);

    Iterator<Long> getAddressHaveWantIterator(String address, long have, long want);
}
