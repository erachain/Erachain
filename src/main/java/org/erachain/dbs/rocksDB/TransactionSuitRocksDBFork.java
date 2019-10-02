package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.TransactionSuit;
import org.erachain.datachain.TransactionTab;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableTransactionSingle;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.util.ArrayList;
import java.util.Iterator;

@Slf4j
public class TransactionSuitRocksDBFork extends DBMapSuitFork<Long, Transaction> implements TransactionSuit
{

    private final String NAME_TABLE = "TRANSACTIONS_UNCONFIRMED_TABLE_FORK";

    public TransactionSuitRocksDBFork(TransactionTab parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, null);
    }

    @Override
    protected void getMap() {

        map = new DBRocksDBTableTransactionSingle<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(),
                databaseSet);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
        indexes = new ArrayList<>();
    }

    @Override
    public Iterator<Long> getTimestampIterator(boolean descending) {
        return null;
    }

    @Override
    public Iterator typeIterator(String sender, Long timestamp, Integer type) {
        return null;
    }

    @Override
    public Iterator senderIterator(String sender) {
        return null;
    }

    @Override
    public Iterator recipientIterator(String recipient) {
        return null;
    }


}
