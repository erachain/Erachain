package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;

@Slf4j
public class RocksDbTransactSourceImpl extends RocksDbDataSourceImpl implements RocksDbTransactSource {

    //public Transaction transactionDB;
    private Transaction dbCore;

    public ReadOptions transactionReadOptions = new ReadOptions()
            ;


    protected void initDB() {
        super.initDB();
        dbCore = ((TransactionDB) super.dbCore).beginTransaction(writeOptions);

    }

    @Override
    public RocksIterator getIterator() {
        if (quitIfNotAlive()) {
            return null;
        }

        return dbCore.getIterator(transactionReadOptions, super.dbCore.getDefaultColumnFamily());
    }

    @Override
    public RocksIterator getIterator(ColumnFamilyHandle indexDB) {
        if (quitIfNotAlive()) {
            return null;
        }

        return dbCore.getIterator(transactionReadOptions, indexDB);
    }

    @Override
    public void commit() {
        // сольем старый и начнем новый
        resetDbLock.writeLock().lock();
        try {
            dbCore.commit();
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            dbCore = super.dbCore.beginTransaction(writeOptions);
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public void rollback() {
        resetDbLock.writeLock().lock();
        try {
            logger.debug("size before ROLLBACK: " + size());
            dbCore.rollback();
            logger.debug("size after ROLLBACK: " + size());
        } catch (RocksDBException e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.debug("size before ROLLBACK: " + size());
            dbCore = super.dbCore.beginTransaction(writeOptions);
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
            writeOptions.dispose();
            dbCore.close();
            super.dbCore.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

}
