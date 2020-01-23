package org.erachain.datachain;

import org.erachain.dbs.IteratorCloseable;

/**
 * Iterators for this TAB
 */
public interface TransactionFinalSuit {

    void deleteForBlock(Integer height);

    IteratorCloseable<Long> getBlockIterator(Integer height);

    IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort);

    IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort);
    IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo);

    IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type);

    IteratorCloseable<Long> getIteratorByAddressAndTypeFrom(byte[] addressShort, Integer type, Long fromID);

    IteratorCloseable<Long> getIteratorByTitle(String filter, boolean asFilter, Long fromSeqNo, boolean descending);

    IteratorCloseable<Long> getIteratorByAddress(byte[] addressShort);

    IteratorCloseable<Long> getBiDirectionAddressIterator(byte[] addressShort, Long fromSeqNo, boolean descending);

    }
