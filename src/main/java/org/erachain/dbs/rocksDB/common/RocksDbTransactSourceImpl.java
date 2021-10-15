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
    public Transaction dbCoreTransact;
    public TransactionDB dbCore;

    protected ReadOptions transactReadOptions = new ReadOptions();

    TransactionDBOptions transactionDbOptions = new TransactionDBOptions();

    public RocksDbTransactSourceImpl(String parentName, String name, List<IndexDB> indexes, RocksDbSettings settings, boolean enableSize) {
        super(parentName, name, indexes, settings, enableSize);
    }
    public RocksDbTransactSourceImpl(String name, List<IndexDB> indexes, RocksDbSettings settings, boolean enableSize) {
        this(Settings.getInstance().getDataChainPath() + ROCKS_DB_FOLDER, name, indexes, settings, enableSize);
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        transactionDbOptions = new TransactionDBOptions();
        dbCore = TransactionDB.open(options, transactionDbOptions, getDbPathAndFile().toString());
        dbCoreTransact = dbCore.beginTransaction(writeOptions);
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = TransactionDB.open(dbOptions, transactionDbOptions, getDbPathAndFile().toString(), columnFamilyDescriptors, columnFamilyHandles);
        dbCoreTransact = dbCore.beginTransaction(writeOptions);
    }

    @Override
    public RocksIterator getIterator() {
        if (quitIfNotAlive()) {
            return null;
        }

        return dbCoreTransact.getIterator(transactReadOptions, dbCore.getDefaultColumnFamily());
    }

    @Override
    public RocksIterator getIterator(ColumnFamilyHandle indexDB) {
        if (quitIfNotAlive()) {
            return null;
        }

        return dbCoreTransact.getIterator(transactReadOptions, indexDB);
    }

    @Override
    public void commit() {
        // сольем старый и начнем новый
        resetDbLock.writeLock().lock();
        try {
            dbCoreTransact.commit();
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            dbCoreTransact.close();
            dbCoreTransact = dbCore.beginTransaction(writeOptions);
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public void rollback() {
        resetDbLock.writeLock().lock();
        try {
            dbCoreTransact.rollback();
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            dbCoreTransact.close();
            dbCoreTransact = dbCore.beginTransaction(writeOptions);
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
            dbCoreTransact.commit();
            dbCoreTransact.close();
            writeOptions.dispose();
            dbCore.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

}
