package org.erachain.datachain;

import java.util.Iterator;

/**
 * Iterators for this TAB
 */
public interface TransactionFinalSuit {

    Iterator<Long> getBlockIterator(Integer height);

    Iterator<Long> getIteratorByRecipient(String address);

    Iterator<Long> getIteratorBySender(String address);

    Iterator<Long> getIteratorByTypeAndAddress(String address, Integer type);

    Iterator<Long> getIteratorByTitleAndType(String filter, Integer type);

    Iterable getIteratorByTitleAndType(String filter, boolean asFilter, Integer type, int offset, int limit);

    Iterator<Long> getIteratorByAddress(String address);

    int getTransactionsByAddressCount(String address);

    int findTransactionsCount(String address, String sender, String recipient, final int minHeight,
                              final int maxHeight, int type, int service, boolean desc, int offset, int limit);

    Iterable findTransactionsKeys(String address, String sender, String recipient, final int minHeight,
                                  final int maxHeight, int type, final int service, boolean desc, int offset, int limit);
}
