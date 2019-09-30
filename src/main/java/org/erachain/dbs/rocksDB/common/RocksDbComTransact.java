package org.erachain.dbs.rocksDB.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.settings.Settings;
import org.rocksdb.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.erachain.dbs.rocksDB.common.RocksDbDataSourceImpl.SIZE_BYTE_KEY;
import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;
import static org.rocksdb.RocksDB.loadLibrary;

/**
 * Самый низкий уровень доступа к функциям RocksDB
 */
@Slf4j
@NoArgsConstructor
public class RocksDbComTransact implements RocksDbCom //implements RocksDbDataSource, Transacted // implements DB<byte[], byte[]>
        //, Flusher, DbSourceInter<byte[]>
{
    protected String dataBaseName;

    //Глеб * эта переменная позаимствована из проекта "tron" нужна для создания каких-то настроек
    // Это включает логирование данных на диск синхронизированно - защищает от утрат при КРАХЕ но чуть медленне работает
    // Если ЛОЖЬ то данные утрачиваются при КРАХЕ
    protected boolean dbSync = true;

    protected WriteOptions writeOptions = new WriteOptions().setSync(dbSync).setDisableWAL(false);
    protected ReadOptions transactReadOptions = new ReadOptions();

    @Getter
    public Transaction dbCore;
    @Getter
    public TransactionDB dbCoreParent;
    //public OptimisticTransactionDB database;

    protected boolean alive;
    protected String parentName;
    List<IndexDB> indexes;
    RocksDbSettings settings;
    protected boolean create = false;
    protected String sizeDescriptorName = "size";
    @Getter
    protected List<ColumnFamilyHandle> columnFamilyHandles;

    protected ColumnFamilyHandle columnFamilyFieldSize;
    protected ByteableInteger byteableInteger = new ByteableInteger();


    protected ReadWriteLock resetDbLock = new ReentrantReadWriteLock();

    static {
        try {
            logger.info("load libraries");
            loadLibrary(new ArrayList<String>() {{
                add(".");
            }});
            logger.info("loaded success");
        } catch (Throwable throwable) {
            logger.error(throwable.getMessage(), throwable);
        }
    }

    public RocksDbComTransact(String parentName, String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this.dataBaseName = name;
        this.parentName = parentName;
        this.indexes = indexes;
        this.settings = settings;

    }

    public RocksDbComTransact(String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, settings);
    }

    @Override
    public Path getDbPath() {
        return Paths.get(parentName, dataBaseName);
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    protected boolean quitIfNotAlive() {
        if (!isAlive()) {
            logger.warn("db is not alive");
        }
        return !isAlive();
    }

    @Override
    public String getDBName() {
        return dataBaseName;
    }

    @Override
    public void put(byte[] key, byte[] value) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.put(key, value);
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
            dbCore.put(columnFamilyHandle, key, value);
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
            dbCore.put(key, value);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public byte[] get(byte[] key) {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        try {
            return dbCore.get(transactReadOptions, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return null;
    }

    @Override
    public byte[] get(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return null;
        }
        resetDbLock.readLock().lock();
        try {
            return dbCore.get(columnFamilyHandle, transactReadOptions, key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
        return null;
    }

    @Override
    public void remove(byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.delete(key);
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
            dbCore.delete(columnFamilyHandle, key);
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
            dbCore.delete(key);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        byte[] sizeBytes = get(columnFamilyFieldSize, SIZE_BYTE_KEY);
        return byteableInteger.receiveObjectFromBytes(sizeBytes);
    }

    @Override
    public int parentSize() {
        try {
            byte[] sizeBytes = dbCoreParent.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
            return byteableInteger.receiveObjectFromBytes(sizeBytes);
        } catch (RocksDBException e) {
            return -1;
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
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