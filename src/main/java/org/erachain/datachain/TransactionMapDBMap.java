package org.erachain.datachain;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.DBMap;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.ReverseComparator;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;

public class TransactionMapDBMap extends TransactionMapImpl
{

    static Logger logger = LoggerFactory.getLogger(TransactionMapDBMap.class.getSimpleName());

    public TransactionMapDBMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
        TIMESTAMP_INDEX = 1;

    }

    @Override
    protected void getMap() {
        map = new org.erachain.dbs.mapDB.TransactionMapDBMap(databaseSet, database);
    }

    @Override
    protected void createIndexes() {
    }

    @Override
    Iterable typeKeys(String sender, Long timestamp, Integer type) {
        return Fun.filter(((org.erachain.dbs.mapDB.TransactionMapDBMap)map).typeKey, new Fun.Tuple3<String, Long, Integer>(sender, timestamp, type));
    }
    @Override
    public Iterable senderKeys(String sender) {
        return Fun.filter(((org.erachain.dbs.mapDB.TransactionMapDBMap)map).senderKey, sender);
    }
    @Override
    public Iterable recipientKeys(String recipient) {
        return Fun.filter(((org.erachain.dbs.mapDB.TransactionMapDBMap)map).recipientKey, recipient);
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
