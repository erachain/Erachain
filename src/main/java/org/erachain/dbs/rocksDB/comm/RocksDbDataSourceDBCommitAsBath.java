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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
    Set puts;

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
        puts = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = RocksDB.open(dbOptions, getDbPathAndFile().toString(), columnFamilyDescriptors, columnFamilyHandles);
        writeBatch = new WriteBatch();
        deleted = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        puts = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
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

            // запомним что это делали - если нет в родительской базе то запомним что добавляем
            if (!dbCore.keyMayExist(key, inCache)) {
                puts.add(key);
            }
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
            deleted.remove(Bytes.concat(Ints.toByteArray(columnFamilyHandle.getID()), key));

            // запомним что это делали - если нет в родительской базе то запомним что добавляем
            if (!dbCore.keyMayExist(columnFamilyHandle, key, inCache)) {
                puts.add(Bytes.concat(Ints.toByteArray(columnFamilyHandle.getID()), key));
            }

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
            if (puts.contains(key))
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
            if (deleted.contains(Bytes.concat(Ints.toByteArray(columnFamilyHandle.getID()), key)))
                return false;
            if (puts.contains(Bytes.concat(Ints.toByteArray(columnFamilyHandle.getID()), key)))
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
            puts.remove(Bytes.concat(Ints.toByteArray(columnFamilyHandle.getID()), key));

            // запомним что это делали
            if (dbCore.keyMayExist(columnFamilyHandle, key, inCache)) {
                deleted.add(Bytes.concat(Ints.toByteArray(columnFamilyHandle.getID()), key));
            }
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public int parentSize() {
        return -deleted.size() + puts.size();
    }

    @Override
    public void commit() {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();
        try {
            dbCore.write(writeOptions, writeBatch);
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            writeBatch.clear();
            ///writeBatch = new WriteBatch();
            deleted = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
            puts = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);

            resetDbLock.readLock().unlock();
        }

    }

    @Override
    public void rollback() {
        if (quitIfNotAlive()) {
            return;
        }
        resetDbLock.readLock().lock();

        writeBatch.clear();

        ///writeBatch = new WriteBatch();
        deleted = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);
        puts = new TreeSet<>(Fun.BYTE_ARRAY_COMPARATOR);

        resetDbLock.readLock().unlock();

    }
}
