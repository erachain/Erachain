package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.Transacted;
import org.erachain.dbs.TransactedThrows;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.settings.Settings;
import org.rocksdb.*;

import java.util.ArrayList;
import java.util.List;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

/**
 * База данных RocksDB с поддержкой транзакционной модели.
 * Причем сама база не делает commit & rollback. Для этого нужно отдельно создавать Транзакцию
 */
@Slf4j
public class RocksDbDataSourceOptTransaction extends RocksDbDataSourceImpl implements Transacted {

    ReadOptions readOptions;
    WriteOptions writeOptions;

    public RocksDbDataSourceOptTransaction(String pathName, String name, List<IndexDB> indexes,
                                           OptimisticTransactionDB dbCore, List<ColumnFamilyHandle> columnFamilyHandles,
                                           WriteOptions writeOptions, ReadOptions readOptions) {
        super(pathName, name, indexes, null);
        this.alive = true;
        this.dbCore = dbCore;
        this.columnFamilyHandles = columnFamilyHandles;
        this.readOptions = readOptions;
        this.writeOptions = writeOptions;

        this.table = new RocksDbComOptTransaction(dbCore, writeOptions, readOptions);
        afterOpenTable();

    }

    public RocksDbDataSourceOptTransaction(String name, List<IndexDB> indexes,
                                           OptimisticTransactionDB dbCore, List<ColumnFamilyHandle> columnFamilyHandles) {
        this(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER, name, indexes, dbCore, columnFamilyHandles,
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions());
    }

    public RocksDbDataSourceOptTransaction(String name, OptimisticTransactionDB dbCore, List<ColumnFamilyHandle> columnFamilyHandles) {
        this(name, new ArrayList<>(), dbCore, columnFamilyHandles);
    }

    //public RocksDbDataSourceOptTransaction(DBRocksDBTable dbSource) {
    //    this(dbSource., name, indexes, dbCore, columnFamilyHandles,
    //            new WriteOptions().setSync(true).setDisableWAL(false),
    //            new ReadOptions());
    //}

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) {
        return;
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) {
        return;
    }

    @Override
    public void afterOpenTable() {
        columnFamilyFieldSize = columnFamilyHandles.get(columnFamilyHandles.size() - 1);

        if (create) {
            // уже есть выше put(columnFamilyFieldSize, SIZE_BYTE_KEY, new byte[]{0, 0, 0, 0});
        }
    }

    @Override
    public int parentSize() {
        try {
            byte[] sizeBytes = dbCore.get(columnFamilyFieldSize, SIZE_BYTE_KEY);
            return byteableInteger.receiveObjectFromBytes(sizeBytes);
        } catch (RocksDBException e) {
            return -1;
        }
    }

    @Override
    public void commit() {
        resetDbLock.writeLock().lock();
        try {
            ((TransactedThrows) table).commit();
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
            ((TransactedThrows) table).rollback();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    /**
     * Close only Transaction - not parentDB
     */
    @Override
    public void close() {
        resetDbLock.writeLock().lock();
        try {
            commit();
            table.close();
        } catch (Exception e) {
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

}
