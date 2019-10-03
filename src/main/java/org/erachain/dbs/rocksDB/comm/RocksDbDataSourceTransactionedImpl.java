package org.erachain.dbs.rocksDB.comm;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.Transacted;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.settings.Settings;
import org.rocksdb.*;

import java.util.List;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

/**
 * Самый низкий уровень доступа к функциям RocksDB
 */
@Slf4j
public abstract class RocksDbDataSourceTransactionedImpl extends RocksDbDataSourceImpl
        implements Transacted
        // DB<byte[], byte[]>, Flusher, DbSourceInter<byte[]>
{

    TransactionDBOptions transactionDbOptions;
    public Transaction dbTransaction;
    WriteOptions writeOptions;
    ReadOptions readOptions;

    public RocksDbDataSourceTransactionedImpl(RocksDB dbCore,
                                              String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                              WriteOptions writeOptions) {
        super(dbCore, pathName, name, indexes, settings, writeOptions);
    }

    public RocksDbDataSourceTransactionedImpl(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                              WriteOptions writeOptions) {
        super(pathName, name, indexes, settings, writeOptions);
    }

    public RocksDbDataSourceTransactionedImpl(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(pathName, name, indexes, settings, new WriteOptions().setSync(true).setDisableWAL(false));
    }

    public RocksDbDataSourceTransactionedImpl(String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, settings);
    }

    @Override
    public void put(byte[] key, byte[] value) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbTransaction.put(key, value);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbTransaction.put(columnFamilyHandle, key, value);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void put(byte[] key, byte[] value, WriteOptions writeOptions) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbTransaction.put(key, value);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    final StringBuilder inCache = new StringBuilder();

    @Override
    public void remove(byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbTransaction.delete(key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbTransaction.delete(columnFamilyHandle, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void remove(byte[] key, WriteOptions writeOptions) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbTransaction.delete(key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void commit() {
        resetDbLock.writeLock().lock();
        try {
            dbTransaction.commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public void rollback() {
        resetDbLock.writeLock().lock();
        try {
            dbTransaction.rollback();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

}