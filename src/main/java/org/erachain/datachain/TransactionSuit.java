package org.erachain.datachain;

import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.DBMapSuit;

import java.util.Collection;
import java.util.Iterator;

public interface TransactionSuit {

    int TIMESTAMP_INDEX = 1;

    Transaction DEFAULT_VALUE = null;

    Iterator<Long> typeIterator(String sender, Long timestamp, Integer type);

    Iterator<Long> senderIterator(String sender);

    Iterator<Long> recipientIterator(String recipient);

    Iterator<Long> getTimestampIterator(boolean descending);

    //Iterator<Long> getCeatorIterator();

}
