package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.Transacted;
import org.erachain.dbs.TransactedThrows;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.rocksdb.*;

import java.util.List;

/**
 * База данных RocksDB с поддержкой транзакционной модели.
 * Причем сама база не делает commit & rollback. Для этого нужно отдельно создавать Транзакцию
 */
@Slf4j
public class RocksDbDataSourceOptTransactionSingle extends RocksDbDataSourceImpl implements Transacted {

    ReadOptions readOptions;
    WriteOptions writeOptions;

    public RocksDbDataSourceOptTransactionSingle(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                                 WriteOptions writeOptions, ReadOptions readOptions, boolean enableSize) {
        super(pathName, name, indexes, settings, writeOptions, enableSize);
        // Создаем или открываем ДБ
        initDB();

        this.readOptions = readOptions;
        this.writeOptions = writeOptions;

        // оборачиваем в костюм Транзакцию от нее
        table = new RocksDbComOptTransactionSingle((OptimisticTransactionDB) dbCore, writeOptions, readOptions);

    }

    public RocksDbDataSourceOptTransactionSingle(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings, boolean enableSize) {
        this(pathName, name, indexes, settings,
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(), enableSize);
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = OptimisticTransactionDB.open(options, getDbPathAndFile().toString());

    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = OptimisticTransactionDB.open(dbOptions, getDbPathAndFile().toString(), columnFamilyDescriptors, columnFamilyHandles);

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
        super.close();
    }

}
