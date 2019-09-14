package org.erachain.datachain;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableTuple3StringLongInteger;
import org.erachain.dbs.rocksDB.integration.DBMapDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.InnerDBTable;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.erachain.utils.ObserverMessage;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

public class TransactionRocksDBMap extends TransactionMapImpl
{

    static Logger logger = LoggerFactory.getLogger(TransactionRocksDBMap.class.getSimpleName());

    public TransactionRocksDBMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }


    @Override
    protected void getMap() {
        map = new org.erachain.dbs.rocksDB.TransactionRocksDBMap(databaseSet, database);
    }

    @Override
    protected void createIndexes() {
    }

    @Override
    Iterable typeKeys(String address, Long timestamp, Integer type) {
        /*
        IndexByteableTuple3StringLongInteger indexByteableTuple3StringLongInteger = new IndexByteableTuple3StringLongInteger();
        return ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(
                indexByteableTuple3StringLongInteger.toBytes(new Fun.Tuple3<>(address, timestamp, type), null),
                ((DBRocksDBTable) map).list);

         */
        return null;
    }

    @Override
    Iterable senderKeys(String sender) {
        IndexByteableTuple3StringLongInteger indexByteableTuple3StringLongInteger = new IndexByteableTuple3StringLongInteger();
        return ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(
                indexByteableTuple3StringLongInteger.toBytes(new Fun.Tuple3<>(sender, null, null, null),
                ((org.erachain.dbs.rocksDB.TransactionRocksDBMap)map).getSenderIndex()));
    }

    //@Override
    Iterable sendKeys(byte[] recipient) {
        return ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(recipient,
                ((org.erachain.dbs.rocksDB.TransactionRocksDBMap)map).getRecientIndex());
    }

    @Override
    Iterable recipientKeys(String recipient) {
        return (Iterable) ((DBMapDB) map).getIterator(false);
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
