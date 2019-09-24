package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.TransactionSuit;
import org.erachain.datachain.TransactionTab;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableLong;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.BiFunction;

@Slf4j
public class TransactionSuitRocksDBFork extends DBMapSuitFork<Long, Transaction> implements TransactionSuit
{

    private final String NAME_TABLE = "TRANSACTIONS_UNCONFIRMED_TABLE_FORK";
    private final String timestampIndexName = "timestamp_unc_txs";

    private SimpleIndexDB<Long, Transaction, Long> timestampIndex;

    public TransactionSuitRocksDBFork(TransactionTab parent, DBASet databaseSet) {
        super(parent, databaseSet, logger);
    }

    @Override
    protected void getMap() {

        map = new DBRocksDBTable<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                databaseSet);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {

        timestampIndex = new SimpleIndexDB<>(timestampIndexName,
                new BiFunction<Long, Transaction, Long>() {
                    @Override
                    public Long apply(Long aLong, Transaction transaction) {
                        return transaction.getTimestamp();
                    }
                    //}, (result, key) ->new ByteableLong().toBytesObject(result)); // создает Класс на лету и переопределяет его метод
                }, new IndexByteableLong()); // а тут мы уже создали заранее Класс

        indexes = new ArrayList<>();
        indexes.add(timestampIndex);
    }

    @Override
    public Transaction getDefaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    public Iterator<Long> getTimestampIterator(boolean descending) {
        return map.getIndexIterator(timestampIndex.getColumnFamilyHandle(), descending);
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
