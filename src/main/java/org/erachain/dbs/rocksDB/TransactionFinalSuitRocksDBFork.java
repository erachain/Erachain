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

import java.io.IOException;

@Slf4j
public class TransactionFinalSuitRocksDBFork extends DBMapSuitFork<Long, Transaction> implements TransactionFinalSuit {

    public TransactionFinalSuitRocksDBFork(TransactionFinalMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, false, null);
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
        try (IteratorCloseable<Long> iterator = getBlockIterator(height)) {
            while (iterator.hasNext()) {
                map.remove(iterator.next());
            }
        } catch (IOException e) {
        }
    }

    @Override
    public IteratorCloseable<Long> getBlockIterator(Integer height) {
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        return map.getIndexIteratorFilter(Ints.toByteArray(height), false, false);
    }

    @Override
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndTypeFrom(byte[] addressShort, Integer type, Long fromID) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByTitle(String filter, boolean asFilter, Long fromSeqNo, boolean descending) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddress(byte[] addressShort) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getBiDirectionIterator(Long fromSeqNo, boolean descending) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getBiDirectionAddressIterator(byte[] addressShort, Long fromSeqNo, boolean descending) {
        return null;
    }

}
