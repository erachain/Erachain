package org.erachain.dbs.rocksDB.comm;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.Transacted;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.settings.Settings;
import org.mapdb.Fun;
import org.rocksdb.*;

import java.util.*;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

/**
 * Транзакции с помощью пакета и вспомогательных карт deleted & puts - скорее всего
 * они медленне работают чем нативные методы с пакетом типа writeBatch.getFromBatchAndDB
 */
@Slf4j
public class RocksDbDataSourceDBCommitAsBathDelPuts extends RocksDbDataSourceImpl implements Transacted {

    ReadOptions readOptions;
    DBOptions dbOptions = new DBOptions();
    /**
     * Нужно для учета удаленных ключей
     */
    Set deleted;
    /**
     * Нужно для учета размера после слияния
     */
    Map puts;

    protected WriteBatchWithIndex writeBatch;

    public RocksDbDataSourceDBCommitAsBathDelPuts(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                                  WriteOptions writeOptions, ReadOptions readOptions, boolean enableSize) {
        super(pathName, name, indexes, settings, writeOptions, enableSize);
        this.readOptions = readOptions;

        // Создаем или открываем ДБ
        initDB();
    }

    public RocksDbDataSourceDBCommitAsBathDelPuts(String name, List<IndexDB> indexes, RocksDbSettings settings, boolean enableSize) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, settings,
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(), enableSize);
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = RocksDB.open(options, getDbPathAndFile().toString());
        writeBatch = new WriteBatchWithIndex(true);
        deleted = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        puts = new TreeMap<>(Fun.BYTE_ARRAY_COMPARATOR);
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = RocksDB.open(dbOptions, getDbPathAndFile().toString(), columnFamilyDescriptors, columnFamilyHandles);
        writeBatch = new WriteBatchWithIndex(true);
        deleted = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        puts = new TreeMap<>(Fun.BYTE_ARRAY_COMPARATOR);
    }

    @Override
    public void put(byte[] key, byte[] value) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            writeBatch.put(key, value);
            deleted.remove(key);

            // запомним что это делали в любом случае добавляем - ведь может быть новое значение
            puts.put(key, value);

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
            deleted.remove(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key));

            // запомним что это делали в любом случае добавляем - ведь может быть новое значение
            puts.put(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key), value);

        } catch (RocksDBException e) {
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
            if (true) {
                if (writeBatch.getFromBatch(dbOptions, key) != null)
                    return true;
            } else {
                if (deleted.contains(key))
                    return false;
                if (puts.containsKey(key))
                    return true;

            }
            // быстрая проверка - потенциально он может содержаться в базе?
            if (!dbCore.keyMayExist(key, inCache)) return false;
            // теперь ищем по настоящему
            return dbCore.get(key) != null;

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
            if (true) {
                if (writeBatch.getFromBatch(columnFamilyHandle, dbOptions, key) != null)
                    return true;
            } else {
                if (deleted.contains(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key)))
                    return false;
                if (puts.containsKey(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key)))
                    return true;
            }
            // быстрая проверка - потенциально он может содержаться в базе?
            if (!dbCore.keyMayExist(columnFamilyHandle, key, inCache)) return false;
            // теперь ищем по настоящему
            return dbCore.get(columnFamilyHandle, key) != null;

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
            if (true) {
                return writeBatch.getFromBatchAndDB(dbCore, readOptions, key);
            } else {
                if (deleted.contains(key))
                    return null;
                byte[] value = (byte[]) puts.get(key);
                if (value != null)
                    return value;

                return dbCore.get(key);
            }
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
            if (true) {
                return writeBatch.getFromBatchAndDB(dbCore, columnFamilyHandle, readOptions, key);
            } else {
                if (deleted.contains(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key)))
                    return null;
                byte[] value = (byte[]) puts.get(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key));
                if (value != null)
                    return value;
                return dbCore.get(columnFamilyHandle, key);
            }
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void delete(byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            writeBatch.delete(key);
            puts.remove(key);

            // запомним что это делали - если есть вероятность что такой ключ есть
            if (dbCore.keyMayExist(key, inCache)) {
                deleted.add(key);
            }
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void delete(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            writeBatch.delete(columnFamilyHandle, key);
            puts.remove(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key));

            // запомним что это делали - если есть вероятность что такой ключ есть
            if (dbCore.keyMayExist(columnFamilyHandle, key, inCache)) {
                deleted.add(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key));
            }
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
            ///dbCore.flushWal(true);
            dbCore.write(writeOptions, writeBatch);
            logger.debug(" dbCore.write");
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (false) {
                writeBatch.close();
                writeBatch = new WriteBatchWithIndex(true);
            } else {
                writeBatch.clear();
            }

            deleted = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
            puts = new TreeMap<>(Fun.BYTE_ARRAY_COMPARATOR);

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

        logger.debug("writeBatch close");

        deleted = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        puts = new TreeMap<>(Fun.BYTE_ARRAY_COMPARATOR);


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
            //dbCore.write(new WriteOptions().setSync(true), new WriteBatch());
            dbCore.syncWal();
            dbCore.closeE();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

}
