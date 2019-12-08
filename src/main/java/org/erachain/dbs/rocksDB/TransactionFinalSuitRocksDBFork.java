package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.datachain.TransactionFinalSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDB;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.rocksdb.WriteOptions;

import java.util.Iterator;

@Slf4j
public class TransactionFinalSuitRocksDBFork extends DBMapSuitFork<Long, Transaction> implements TransactionFinalSuit {

    public TransactionFinalSuitRocksDBFork(TransactionFinalMap parent, DBASet databaseSet, boolean sizeEnable) {
        super(parent, databaseSet, logger, sizeEnable, null);
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
    public void deleteForBlock(Integer height) {
        Iterator<Long> iterator = getBlockIterator(height);
        while (iterator.hasNext()) {
            map.remove(iterator.next());
        }
    }

    @Override
    public IteratorCloseable<Long> getBlockIterator(Integer height) {
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        return map.getIndexIteratorFilter(Ints.toByteArray(height), false, false);
    }

    @Override
    public IteratorCloseable<Long> getIteratorBySender(String address) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByRecipient(String address) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndType(String address, Integer type) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByTitleAndType(String filter, boolean asFilter, Integer type) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddress(String address) {
        return null;
    }

}
