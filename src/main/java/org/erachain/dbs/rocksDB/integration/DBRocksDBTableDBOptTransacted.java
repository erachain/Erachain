package org.erachain.dbs.rocksDB.integration;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.dbs.rocksDB.common.RocksDbDataSourceOptTransactedDB;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.rocksdb.ReadOptions;
import org.rocksdb.TransactionDBOptions;
import org.rocksdb.WriteOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Данный класс представляет собой основной доступ и функционал к таблице БД RocksDB
 * Тут происходит обработка настроенных вторичных индексов.
 * вызывается из SUIT
 *
 * @param <K>
 * @param <V>
 */
@Slf4j
public class DBRocksDBTableDBOptTransacted<K, V> extends DBRocksDBTable<K, V> {

    TransactionDBOptions transactionDbOptions;
    ReadOptions readOptions;

    public DBRocksDBTableDBOptTransacted(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE,
                                         List<IndexDB> indexes, RocksDbSettings settings,
                                         TransactionDBOptions transactionDbOptions,
                                         WriteOptions writeOptions, ReadOptions readOptions, DBASet dbaSet, boolean enableSize) {
        super(byteableKey, byteableValue, NAME_TABLE, indexes, settings, writeOptions, dbaSet, enableSize);
        this.transactionDbOptions = transactionDbOptions;
        this.readOptions = readOptions;
        openSource();
        afterOpen();
    }

    public DBRocksDBTableDBOptTransacted(Byteable byteableKey, Byteable byteableValue, String NAME_TABLE, List<IndexDB> indexes, DBASet dbaSet) {
        this(byteableKey, byteableValue, NAME_TABLE, indexes, RocksDbSettings.getDefaultSettings(),
                new TransactionDBOptions(),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(), dbaSet, enableSize);
    }

    public DBRocksDBTableDBOptTransacted(String NAME_TABLE) {
        this(new ByteableTrivial(), new ByteableTrivial(), NAME_TABLE,
                new ArrayList<>(), RocksDbSettings.getDefaultSettings(),
                new TransactionDBOptions(),
                new WriteOptions().setSync(true).setDisableWAL(false), new ReadOptions(), null, enableSize);
    }

    @Override
    public void openSource() {
        dbSource = new RocksDbDataSourceOptTransactedDB(this.root, NAME_TABLE, indexes, settings,
                transactionDbOptions, writeOptions, enableSize);
    }

}
