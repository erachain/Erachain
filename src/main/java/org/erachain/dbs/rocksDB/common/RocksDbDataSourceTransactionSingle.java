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
public class RocksDbDataSourceTransactionSingle extends RocksDbDataSourceImpl implements Transacted {

    TransactionDBOptions transactionDbOptions = new TransactionDBOptions();

    ReadOptions readOptions;
    WriteOptions writeOptions;

    public RocksDbDataSourceTransactionSingle(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings,
                                              WriteOptions writeOptions, ReadOptions readOptions) {
        super(pathName, name, indexes, settings, writeOptions);
        // Создаем или открываем ДБ
        initDB();

        this.readOptions = readOptions;
        this.writeOptions = writeOptions;

        // оборачиваем в костюм Транзакцию от нее
        table = new RocksDbComTransactionSingle((TransactionDB) dbCore, writeOptions, readOptions);
        afterOpenTable();

    }

    public RocksDbDataSourceTransactionSingle(String pathName, String name, List<IndexDB> indexes, RocksDbSettings settings) {
        this(pathName, name, indexes, settings,
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions());
    }

    @Override
    protected void createDB(Options options, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = TransactionDB.open(options, transactionDbOptions, getDbPathAndFile().toString());
    }

    @Override
    protected void openDB(DBOptions dbOptions, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws RocksDBException {
        dbCore = TransactionDB.open(dbOptions, transactionDbOptions, getDbPathAndFile().toString(), columnFamilyDescriptors, columnFamilyHandles);
    }

    @Override
    public void commit() {
        try {
            ((Transacted) table).commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public void rollback() {
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
            ((TransactedThrows) table).commit();
            table.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
        super.close();
    }

}
