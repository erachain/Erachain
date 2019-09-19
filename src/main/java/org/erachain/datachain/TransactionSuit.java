package org.erachain.datachain;

import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.DBMapSuit;

import java.util.Collection;
import java.util.Iterator;

public interface TransactionSuit {

    int TIMESTAMP_INDEX = 1;

    Transaction DEFAULT_VALUE = null;

    Iterable typeKeys(String sender, Long timestamp, Integer type);

    Iterable senderKeys(String sender);

    Iterable recipientKeys(String recipient);

    Iterator<Long> getTimestampIterator();

    //Iterator<Long> getCeatorIterator();

    Collection<Long> getFromToKeys(long fromKey, long toKey);

}
