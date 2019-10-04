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
 * Обычная база данных RocksDB - без Транзакционной системы
 */
@Slf4j
public class RocksDbDataSourceDBCommitAsBath extends RocksDbDataSourceImpl implements Transacted {

    /**
     * Нужно для учета удаленных ключей
     */
    Set deleted;
    /**
     * Нужно для учета размера после слияния
     */
    Map puts;

    WriteBatch writeBatch;

    public RocksDbDataSourceDBCommitAsBath(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings, WriteOptions writeOptions) {
        super(pathName, name, indexes, settings, writeOptions);
        // Создаем или открываем ДБ
        initDB();
    }

    public RocksDbDataSourceDBCommitAsBath(String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, settings,
                new WriteOptions().setSync(true).setDisableWAL(false));
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = RocksDB.open(options, getDbPathAndFile().toString());
        writeBatch = new WriteBatch();
        deleted = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        puts = new TreeMap<>(Fun.BYTE_ARRAY_COMPARATOR);
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = RocksDB.open(dbOptions, getDbPathAndFile().toString(), columnFamilyDescriptors, columnFamilyHandles);
        writeBatch = new WriteBatch();
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
            if (deleted.contains(key))
                return false;
            if (puts.containsKey(key))
                return true;
            return dbCore.keyMayExist(key, inCache);
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
            if (deleted.contains(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key)))
                return false;
            if (puts.containsKey(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key)))
                return true;
            return dbCore.keyMayExist(columnFamilyHandle, key, inCache);
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
            if (deleted.contains(key))
                return null;
            byte[] value = (byte[]) puts.get(key);
            if (value != null)
                return value;

            return dbCore.get(key);
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
            if (deleted.contains(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key)))
                return null;
            byte[] value = (byte[]) puts.get(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key));
            if (value != null)
                return value;
            return dbCore.get(columnFamilyHandle, key);
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
            writeBatch.delete(key);
            puts.remove(key);

            // запомним что это делали
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
    public void remove(ColumnFamilyHandle columnFamilyHandle, byte[] key) {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            writeBatch.delete(columnFamilyHandle, key);
            puts.remove(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key));

            // запомним что это делали
            if (dbCore.keyMayExist(columnFamilyHandle, key, inCache)) {
                deleted.add(Bytes.concat(new byte[]{Ints.toByteArray(columnFamilyHandle.getID())[3]}, key));
            }
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void commit() {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.write(writeOptions, writeBatch);
            logger.debug(" dbCore.write");
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (true) {
                writeBatch.close();
                writeBatch = new WriteBatch();
            } else {
                writeBatch.clear();
            }
            ///writeBatch = new WriteBatch();
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

        if (true) {
            writeBatch.close();
            writeBatch = new WriteBatch();
        } else {
            writeBatch.clear();
        }

        logger.debug("writeBatch close");

        ///writeBatch = new WriteBatch();
        deleted = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        puts = new TreeMap<>(Fun.BYTE_ARRAY_COMPARATOR);


        resetDbLock.readLock().unlock();

    }

}
