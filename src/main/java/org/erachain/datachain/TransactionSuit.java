package org.erachain.datachain;

import org.erachain.dbs.IteratorCloseable;

public interface TransactionSuit {

    int TIMESTAMP_INDEX = 1;

    IteratorCloseable<Long> typeIterator(String sender, Long timestamp, Integer type);

    IteratorCloseable<Long> senderIterator(String sender);

    IteratorCloseable<Long> recipientIterator(String recipient);

    IteratorCloseable<Long> getTimestampIterator(boolean descending);

}
