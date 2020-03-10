package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.TransactionMap;
import org.erachain.datachain.TransactionSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDB;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.rocksdb.WriteOptions;

import java.util.Iterator;

@Slf4j
public class TransactionSuitRocksDBFork extends DBMapSuitFork<Long, Transaction> implements TransactionSuit
{

    ///private final String NAME_TABLE = "TRANSACTIONS_UNCONFIRMED_TABLE_FORK";

    public TransactionSuitRocksDBFork(TransactionMap parent, DBRocksDBTable parentMap, DBASet databaseSet) {
        super(parent, databaseSet, logger, true, null);
    }

    @Override
    public void openMap() {

        map = new DBRocksDBTableDB<>(new ByteableLong(), new ByteableTransaction(), indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false), sizeEnable);
    }

    @Override
    public IteratorCloseable<Long> getTimestampIterator(boolean descending) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> typeIterator(String sender, Long timestamp, Integer type) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> senderIterator(String sender) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> recipientIterator(String recipient) {
        return null;
    }

    @Override
    public boolean writeToParent() {

        boolean updated = false;

        Iterator<Long> iterator = this.map.keySet().iterator();

        while (iterator.hasNext()) {
            Long key = iterator.next();
            // тут через Очередь сработает - без ошибок от закрытия
            parent.put(key, this.map.get(key));
            updated = true;
        }

        if (deleted != null) {
            Iterator<Long> iteratorDeleted = this.deleted.keySet().iterator();
            while (iteratorDeleted.hasNext()) {
                // тут через Очередь сработает - без ошибок от закрытия
                parent.delete(iteratorDeleted.next());
                updated = true;
            }
        }

        return updated;
    }

}
