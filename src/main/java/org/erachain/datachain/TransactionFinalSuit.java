package org.erachain.datachain;

import org.erachain.dbs.IteratorCloseable;

/**
 * Iterators for this TAB
 */
public interface TransactionFinalSuit {

    void deleteForBlock(Integer height);

    IteratorCloseable<Long> getBlockIterator(Integer height);

    IteratorCloseable<Long> getIteratorByRecipient(String address);

    IteratorCloseable<Long> getIteratorBySender(String address);

    IteratorCloseable<Long> getIteratorByAddressAndType(String address, Integer type);

    IteratorCloseable<Long> getIteratorByAddressAndTypeFrom(String address, Integer type, Long fromID);

    IteratorCloseable<Long> getIteratorByTitleAndType(String filter, boolean asFilter, Integer type);

    IteratorCloseable<Long> getIteratorByAddress(String address);

    IteratorCloseable<Long> getBiDirectionAddressIterator(String address, Long fromSeqNo, boolean descending);

    }
