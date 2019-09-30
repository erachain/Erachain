package org.erachain.dbs.rocksDB.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.Transacted;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.settings.Settings;
import org.rocksdb.*;

import java.util.List;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

@Slf4j
public class RocksDbTransactSourceImpl extends RocksDbDataSourceImpl implements Transacted {

    @Getter
    public TransactionDB dbCoreParent;
    public Transaction dbCore;

    protected ReadOptions transactReadOptions = new ReadOptions();

    TransactionDBOptions transactionDbOptions = new TransactionDBOptions()
            ;

    public RocksDbTransactSourceImpl(String parentName, String name, List<IndexDB> indexes, RocksDbSettings settings) {
        super(parentName, name, indexes, settings);
    }
    public RocksDbTransactSourceImpl(String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, settings);
    }

    @Override
    protected void createDB(Options options) throws RocksDBException {
        dbCoreParent = TransactionDB.open(options, transactionDbOptions, getDbPath().toString());
        dbCore = dbCoreParent.beginTransaction(writeOptions);
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCoreParent = TransactionDB.open(dbOptions, transactionDbOptions, getDbPath().toString(), columnFamilyDescriptors, columnFamilyHandles);
        dbCore = dbCoreParent.beginTransaction(writeOptions);
    }

    @Override
    public RocksIterator getIterator() {
        if (quitIfNotAlive()) {
            return null;
        }

        return dbCore.getIterator(transactReadOptions, dbCoreParent.getDefaultColumnFamily());
    }

    @Override
    public RocksIterator getIterator(ColumnFamilyHandle indexDB) {
        if (quitIfNotAlive()) {
            return null;
        }

        return dbCore.getIterator(transactReadOptions, indexDB);
    }

    public int parentSize() {
        try {
            byte[] sizeBytes = dbCoreParent.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
            return byteableInteger.receiveObjectFromBytes(sizeBytes);
        } catch (RocksDBException e) {
            return -1;
        }
    }


    @Override
    public void commit() {
        // сольем старый и начнем новый
        resetDbLock.writeLock().lock();
        try {
            dbCore.commit();
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            dbCore.close();
            dbCore = dbCoreParent.beginTransaction(writeOptions);
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public void rollback() {
        resetDbLock.writeLock().lock();
        try {
            dbCore.rollback();
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            dbCore.close();
            dbCore = dbCoreParent.beginTransaction(writeOptions);
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public void close() {
        resetDbLock.writeLock().lock();
        try {
            if (!isAlive()) {
                return;
            }
            alive = false;
            dbCore.commit();
            dbCore.close();
            writeOptions.dispose();
            dbCoreParent.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

}
