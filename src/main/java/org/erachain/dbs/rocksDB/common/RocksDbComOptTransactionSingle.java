package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.OptimisticTransactionDB;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

/**
 * Транзакция RocksDB. тут подразумевается что у данной Базы есть только один этот транзакционный класс.
 * Поэтому закрытие трнзакции вызывает и закрытие родительской базы данных
 * Работа с записями идет в Транзакцию и изменения сольются в родительскую базу только после commit.
 * По сути это аналог реализации форка от бюазы данных в MapDB
 */
@Slf4j
public class RocksDbComOptTransactionSingle extends RocksDbComOptTransaction {
    public RocksDbComOptTransactionSingle(OptimisticTransactionDB parentDB, WriteOptions writeOptions, ReadOptions readOptions) {
        super(parentDB, writeOptions, readOptions);
    }

    /**
     * Закрывает и базу данных родительскую тоже
     */
    @Override
    public void close() {
        super.close();
        parentDB.close();
    }
}