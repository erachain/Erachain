package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.datachain.TransactionFinalSuit;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.util.ArrayList;
import java.util.Iterator;

@Slf4j
public class TransactionFinalSuitRocksDBFork extends DBMapSuitFork<Long, Transaction> implements TransactionFinalSuit {

    public TransactionFinalSuitRocksDBFork(TransactionFinalMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, null);
    }

    @Override
    protected void getMap() {

        // make fork in TEMP dir
        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableLong(), new ByteableTransaction(), indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(),
                databaseSet);
    }

    @Override
    protected void createIndexes() {
        // USE counter index
        indexes = new ArrayList<>();
    }

    @Override
    public Iterator<Long> getBlockIterator(Integer height) {
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(Ints.toByteArray(height), false);
    }

    @Override
    public Iterator<Long> getIteratorBySender(String address) {
        return null;
    }

    @Override
    public Iterator<Long> getIteratorByRecipient(String address) {
        return null;
    }

    @Override
    public Iterator<Long> getIteratorByAddressAndType(String address, Integer type) {
        return null;
    }

    @Override
    public Iterator<Long> getIteratorByTitleAndType(String filter, boolean asFilter, Integer type) {
        return null;
    }

    @Override
    public Iterator<Long> getIteratorByAddress(String address) {
        return null;
    }

    @Override
    public Iterator findTransactionsKeys(String address, String sender, String recipient, final int minHeight,
                                         final int maxHeight, int type, final int service, boolean desc, int offset, int limit) {
        return null;
    }

}
