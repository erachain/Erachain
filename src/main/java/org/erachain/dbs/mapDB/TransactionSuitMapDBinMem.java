package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionUncSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.settings.Settings;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.SerializerBase;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
public class TransactionSuitMapDBinMem extends TransactionSuitMapDB {

    public TransactionSuitMapDBinMem(DBASet databaseSet, DB database) {
        super(databaseSet, null, logger);
    }

    @Override
    public void openMap() {

        if (true) {
            database = DCSet.makeDBinMemory();
        } else {

            File dbFile = new File(Settings.getInstance().getDataChainPath(), "txPool.dat");
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
                    .cacheSize(2048)

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
                    .freeSpaceReclaimQ(10)
                    // .remove + .put - java.io.IOError: java.io.IOException: no free space to expand Volume
                    .sizeLimit(0.3) // ограничивает рост базы - если freeSpaceReclaimQ < 3
                    .make();
        }

        sizeEnable = true; // разрешаем счет размера - это будет немного тормозить работу

        // OPEN MAP
        map = database.createHashMap("transactions")
                .keySerializer(SerializerBase.LONG)
                .valueSerializer(new TransactionUncSerializer())
                .counterEnable() // разрешаем счет размера - это будет немного тормозить работу
                .makeOrGet();

    }

    @Override
    public int size() {
        if (database.getEngine().isClosed())
            return -1;

        return super.size();
    }

    @Override
    public Transaction get(Long key) {
        if (database.getEngine().isClosed())
            return getDefaultValue(key);

        return super.get(key);
    }

    @Override
    public Set<Long> keySet() {
        if (database.getEngine().isClosed())
            return new TreeSet<>();
        return super.keySet();
    }

    @Override
    public Collection<Transaction> values() {
        if (database.getEngine().isClosed())
            return new TreeSet<>();
        return super.values();
    }

    @Override
    public boolean set(Long key, Transaction value) {
        if (database.getEngine().isClosed())
            return false;
        return super.set(key, value);
    }

    @Override
    public void put(Long key, Transaction value) {
        if (database.getEngine().isClosed())
            return;
        super.put(key, value);
    }

    @Override
    public Transaction remove(Long key) {
        if (database.getEngine().isClosed())
            return getDefaultValue(key);
        return super.remove(key);
    }

    @Override
    public void delete(Long key) {
        if (database.getEngine().isClosed())
            return;
        try {
            super.delete(key);
        } catch (Exception IllegalAccessError) {

        }
    }

    @Override
    public Transaction removeValue(Long key) {
        if (database.getEngine().isClosed())
            return getDefaultValue(key);
        return super.removeValue(key);
    }

    @Override
    public void deleteValue(Long key) {
        if (database.getEngine().isClosed())
            return;
        super.deleteValue(key);
    }

    @Override
    public boolean contains(Long key) {
        if (database.getEngine().isClosed())
            return false;
        return super.contains(key);
    }

    @Override
    public IteratorCloseable<Long> getIndexIterator(int index, boolean descending) {
        if (database.getEngine().isClosed())
            return new IteratorCloseableImpl(new TreeSet<Long>().iterator());
        return super.getIndexIterator(index, descending);
    }

    @Override
    public IteratorCloseable<Long> getIterator() {
        if (database.getEngine().isClosed())
            return new IteratorCloseableImpl(new TreeSet<Long>().iterator());
        return super.getIterator();
    }

    @Override
    public synchronized void close() {
        try {
            // может быть ошибка
            database.getEngine().clearCache();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            database.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        super.close();
    }

    @Override
    public synchronized void clear() {
        if (this.database.getEngine().isClosed())
            return;
        close();
        openMap();
        createIndexes();
    }
}
