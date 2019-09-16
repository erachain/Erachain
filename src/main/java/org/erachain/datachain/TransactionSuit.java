package org.erachain.datachain;

import org.erachain.core.transaction.Transaction;

import java.util.Iterator;

public interface TransactionSuit {

    Transaction DEFAULT_VALUE = null;

    Iterable typeKeys(String sender, Long timestamp, Integer type);

    Iterable senderKeys(String sender);

    Iterable recipientKeys(String recipient);

    Iterator<Long> getTimestampIterator();

    //Iterator<Long> getCeatorIterator();

}
