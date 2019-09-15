package org.erachain.datachain;

import org.erachain.dbs.mapDB.TransactionSuitMapDB;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class TransactionTabNativeMemForked extends TransactionTabImpl
{

    static Logger logger = LoggerFactory.getLogger(TransactionTabNativeMemForked.class.getSimpleName());

    public TransactionTabNativeMemForked(TransactionTab parent, DCSet databaseSet) {
        super(parent, databaseSet);
    }

    @Override
    protected void getMap() {
        map = new org.erachain.dbs.nativeMemMap.nativeMapTreeMap(parent, databaseSet, null);
    }

    @Override
    protected void createIndexes() {
    }

    @Override
    Iterable typeKeys(String sender, Long timestamp, Integer type) {
        return Fun.filter(((TransactionSuitMapDB)map).typeKey, new Fun.Tuple3<String, Long, Integer>(sender, timestamp, type));
    }
    @Override
    public Iterable senderKeys(String sender) {
        return Fun.filter(((TransactionSuitMapDB)map).senderKey, sender);
    }
    @Override
    public Iterable recipientKeys(String recipient) {
        return Fun.filter(((TransactionSuitMapDB)map).recipientKey, recipient);
    }

    @Override
    public Iterator<Long> getTimestampIterator() {
        return null;
    }

    @Override
    public Iterator<Long> getCeatorIterator() {
        return null;
    }

}
