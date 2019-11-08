package org.erachain.datachain;

import java.util.Iterator;

/**
 * Iterators for this TAB
 */
public interface TransactionFinalSuit {

    void deleteForBlock(Integer height);

    Iterator<Long> getBlockIterator(Integer height);

    Iterator<Long> getIteratorByRecipient(String address);

    Iterator<Long> getIteratorBySender(String address);

    Iterator<Long> getIteratorByAddressAndType(String address, Integer type);

    Iterator<Long> getIteratorByTitleAndType(String filter, boolean asFilter, Integer type);

    Iterator<Long> getIteratorByAddress(String address);

}
