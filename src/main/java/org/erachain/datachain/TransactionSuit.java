package org.erachain.datachain;

import java.util.Iterator;

public interface TransactionSuit {

    int TIMESTAMP_INDEX = 1;

    Iterator<Long> typeIterator(String sender, Long timestamp, Integer type);

    Iterator<Long> senderIterator(String sender);

    Iterator<Long> recipientIterator(String recipient);

    Iterator<Long> getTimestampIterator(boolean descending);

    //Iterator<Long> getCeatorIterator();

}
