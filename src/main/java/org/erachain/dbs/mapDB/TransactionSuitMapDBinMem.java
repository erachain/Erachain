package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.SerializerBase;

import java.io.File;

@Slf4j
public class TransactionSuitMapDBinMem extends TransactionSuitMapDB {

    public TransactionSuitMapDBinMem(DBASet databaseSet, DB database) {
        super(databaseSet, null, logger);
    }

    @Override
    public void getMap() {

        if (true) {
            database = DCSet.makeDBinMemory();
        } else {

            File dbFile = new File(Settings.getInstance().getDataDir(), "txPool.dat");
            dbFile.getParentFile().mkdirs();

            /// https://jankotek.gitbooks.io/mapdb/performance/
            //CREATE DATABASE
            database = DBMaker.newFileDB(dbFile)

                    .deleteFilesAfterClose()
                    .transactionDisable()

                    ////// ТУТ вряд ли нужно КЭШИРОВАТь при чтении что-либо
                    //////
                    // это чистит сама память если соталось 25% от кучи - так что она безопасная
                    // у другого типа КЭША происходит утечка памяти
                    //.cacheHardRefEnable()
                    //.cacheLRUEnable()
                    ///.cacheSoftRefEnable()
                    .cacheWeakRefEnable()

                    // количество точек в таблице которые хранятся в HashMap как в КЭШе
                    // - начальное значени для всех UNBOUND и максимальное для КЭШ по умолчанию
                    // WAL в кэш на старте закатывает все значения - ограничим для быстрого старта
                    .cacheSize(1024)

                    .checksumEnable()
                    .mmapFileEnableIfSupported() // ++ but -- error on asyncWriteEnable
                    .commitFileSyncDisable() // ++

                    //.snapshotEnable()
                    //.asyncWriteEnable()
                    //.asyncWriteFlushDelay(100)

                    // если при записи на диск блока процессор сильно нагружается - то уменьшить это
                    // не нагружать процессор для поиска свободного места в базе данных
                    // >2 - удаляет удаленные записи полностью и не раздувает базу
                    // 2 - не удаляет ключи и не сжимает базу при удалении записей, база растет
                    .freeSpaceReclaimQ(0)
                    // .remove + .put - java.io.IOError: java.io.IOException: no free space to expand Volume
                    .sizeLimit(0.3) // ограничивает рост базы - если freeSpaceReclaimQ < 3
                    .make();
        }

        // OPEN MAP
        map = database.createHashMap("transactions")
                .keySerializer(SerializerBase.LONG)
                .valueSerializer(new TransactionSerializer())
                .counterEnable()
                .makeOrGet();

    }

    @Override
    public void close() {
        database.close();
    }

    @Override
    public void clear() {
        close();
        getMap();
        createIndexes();
    }
}
