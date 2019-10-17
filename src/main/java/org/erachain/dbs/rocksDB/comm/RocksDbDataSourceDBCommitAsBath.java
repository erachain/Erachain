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
 * Транзакции с помощью пакета и нативные методы с пакетом типа writeBatch.getFromBatchAndDB
 */
@Slf4j
public class RocksDbDataSourceDBCommitAsBath extends RocksDbDataSourceImpl implements Transacted {

    ReadOptions readOptions;

    protected WriteBatchWithIndex writeBatch;

    public RocksDbDataSourceDBCommitAsBath(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                           WriteOptions writeOptions, ReadOptions readOptions) {
        super(pathName, name, indexes, settings, writeOptions);
        this.readOptions = readOptions;

        // Создаем или открываем ДБ
        initDB();
    }

    public RocksDbDataSourceDBCommitAsBath(String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, settings,
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions());
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = RocksDB.open(options, getDbPathAndFile().toString());
        writeBatch = new WriteBatchWithIndex(true);
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = RocksDB.open(dbOptions, getDbPathAndFile().toString(), columnFamilyDescriptors, columnFamilyHandles);
        writeBatch = new WriteBatchWithIndex(true);
    }

    @Override
    public void put(byte[] key, byte[] value) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            writeBatch.put(key, value);
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
            writeBatch.put(columnFamilyHandle, key, value);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(this.getClass().getSimpleName() + " : " + columnFamilyHandle.toString());
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(byte[] key) {
        if (quitIfNotAlive()) {
            return false;
        }
        resetDbLock.readLock().lock();
        try {
            return writeBatch.getFromBatchAndDB(dbCore, readOptions, key) != null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return false;
    }

    @Override
    public boolean contains(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return false;
        }
        resetDbLock.readLock().lock();
        try {
            return writeBatch.getFromBatchAndDB(dbCore, columnFamilyHandle, readOptions, key) != null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return false;
    }

    @Override
    public byte[] get(byte[] key) {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        try {
            return writeBatch.getFromBatchAndDB(dbCore, readOptions, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        try {
            return writeBatch.getFromBatchAndDB(dbCore, columnFamilyHandle, readOptions, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void remove(byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            writeBatch.delete(key);
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
            writeBatch.delete(columnFamilyHandle, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    /**
     * Используем newIteratorWithBase - для перебора вместе с ключами от родительской Таблицы
     * @return
     */
    @Override
    public RocksIterator getIterator() {
        if (quitIfNotAlive()) {
            return null;
        }

        return writeBatch.newIteratorWithBase(dbCore.newIterator());
    }

    /**
     * Используем newIteratorWithBase - для перебора вместе с ключами от родительской Таблицы
     * @return
     */
    @Override
    public RocksIterator getIterator(ColumnFamilyHandle indexDB) {
        if (quitIfNotAlive()) {
            return null;
        }

        return writeBatch.newIteratorWithBase(indexDB, dbCore.newIterator(indexDB));
    }

    @Override
    public void commit() {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            ////dbCore.flushWal(true);
            dbCore.write(writeOptions, writeBatch);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (false) {
                writeBatch.close();
                writeBatch = new WriteBatchWithIndex(true);
            } else {
                writeBatch.clear();
            }
            logger.debug(" writeBatch commit");

            resetDbLock.readLock().unlock();
        }

    }

    @Override
    public void rollback() {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();

        if (false) {
            writeBatch.close();
            writeBatch = new WriteBatchWithIndex(true);
        } else {
            writeBatch.clear();
        }

        logger.debug("writeBatch rollback");

        resetDbLock.readLock().unlock();

    }

    @Override
    public void close() {
        resetDbLock.writeLock().lock();
        try {
            if (!isAlive()) {
                return;
            }
            alive = false;
            writeBatch.close();
            ////dbCore.write(new WriteOptions().setSync(true), new WriteBatch());
            dbCore.syncWal();
            dbCore.closeE();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.debug("writeBatch close");
            resetDbLock.writeLock().unlock();
        }
    }

}
