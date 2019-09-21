package org.erachain.dbs.rocksDB;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

public class TransactionSuitRocksDBFork extends DBMapSuitFork<Long, Transaction> implements TransactionSuit
{

    static Logger logger = LoggerFactory.getLogger(TransactionSuitRocksDBFork.class.getSimpleName());

    private final String NAME_TABLE = "TRANSACTIONS_UNCONFIRMED_TABLE_FORK";
    private final String timestampIndexName = "timestamp_unc_txs";

    private SimpleIndexDB<Long, Transaction, Long> timestampIndex;

    public TransactionSuitRocksDBFork(TransactionTab parent, DBASet databaseSet) {
        super(parent, databaseSet);
    }

    @Override
    protected void getMap() {

        timestampIndex = new SimpleIndexDB<>(timestampIndexName,
                new BiFunction<Long, Transaction, Long>() {
                    @Override
                    public Long apply(Long aLong, Transaction transaction) {
                        return transaction.getTimestamp();
                    }
                //}, (result, key) ->new ByteableLong().toBytesObject(result)); // создает Класс на лету и переопределяет его метод
                }, new IndexByteableLong()); // а тут мы уже создали заранее Класс

        List indexes = new ArrayList<>();
        indexes.add(timestampIndex);

        map = new DBRocksDBTable<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                databaseSet);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
    }

    @Override
    public Transaction getDefaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    public Iterator<Long> getTimestampIterator() {
        return map.getIndexIterator(timestampIndex.getColumnFamilyHandle(), false);
    }

    @Override
    public Iterable typeKeys(String sender, Long timestamp, Integer type) {
        return null;
    }

    @Override
    public Iterable senderKeys(String sender) {
        return null;
    }

    @Override
    public Iterable recipientKeys(String recipient) {
        return null;
    }

    @Override
    public Collection<Long> getFromToKeys(long fromKey, long toKey) {
        return null;
    }

}
