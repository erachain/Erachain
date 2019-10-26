package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBOptTransacted;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableOptTransaction;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Before;
import org.junit.Test;
import org.rocksdb.OptimisticTransactionDB;
import org.rocksdb.WriteOptions;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;
import static org.junit.Assert.assertEquals;

@Slf4j
public class RocksDbDataSourceOptTransactionTest {

    private List<Map.Entry<byte[], byte[]>> data = new ArrayList<>();

    private long countData = 100000;
    private int countCommit = 10000;

    WriteOptions writeOptions;
    List<IndexDB> indexes = new ArrayList<>();
    RocksDbSettings dbSettings = new RocksDbSettings();

    @Before
    public void generateData() {
        for (int i = 0; i < countData; i++) {
            data.add(new AbstractMap.SimpleEntry(UUID.randomUUID().toString().getBytes(), UUID.randomUUID().toString().getBytes()));
        }
    }

    @Test
    public void test1() {
        //public RocksDbDataSourceOptTransaction(String pathName, String name, List< IndexDB > indexes,
        //    OptimisticTransactionDB dbCore, List< ColumnFamilyHandle > columnFamilyHandles,
        //    WriteOptions writeOptions, ReadOptions readOptions) {

        logger.info("Start test RocksDB productivity commit");
        String NAME_TABLE = "RocksDbDataSourceDB";

        // DELETE перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        File tempDir = new File(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER);
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        boolean twice = false;
        boolean found;

        List<IndexDB> indexes = new ArrayList<>();
        RocksDbSettings dbSettings = new RocksDbSettings();

        do {
            long timeMillisBefore = System.currentTimeMillis();

            DBRocksDBTableDBOptTransacted rocksDB = new DBRocksDBTableDBOptTransacted(NAME_TABLE);

            int k = 0;
            int step = 10;

            // создадим в базе несколько записей
            for (Map.Entry<byte[], byte[]> entry : data) {

                if (++k > step) break;

                rocksDB.put(entry.getKey(), entry.getValue());
            }

            logger.info("SIZE = " + rocksDB.size());

            k = 0;
            for (Map.Entry<byte[], byte[]> entry : data) {

                if (++k > step) break;

                assertEquals(rocksDB.containsKey(entry.getKey()), true);

            }

            // теперь в транзакцию будем закатывать
            RocksDbDataSourceOptTransaction dbOptTrans = new RocksDbDataSourceOptTransaction(NAME_TABLE,
                    (OptimisticTransactionDB) rocksDB.dbSource.getDbCore(), rocksDB.dbSource.getColumnFamilyHandles());

            for (int i = k; i < step; i++) {

                Map.Entry<byte[], byte[]> entry = data.get(i);

                // поиск в родительской базе
                assertEquals(rocksDB.containsKey(entry.getKey()), false);

                dbOptTrans.put(entry.getKey(), entry.getValue());

                // поиск в родительской базе
                assertEquals(rocksDB.containsKey(entry.getKey()), true);

                dbOptTrans.delete(entry.getKey());

                // поиск в родительской базе
                assertEquals(rocksDB.containsKey(entry.getKey()), false);

            }

            logger.info("SIZE = " + rocksDB.size());

            dbOptTrans.commit();

            long timeMillisAfter = System.currentTimeMillis();
            long total = timeMillisAfter - timeMillisBefore;
            logger.info("total time rocksDB = " + total);
            rocksDB.close();
            logger.info("End test RocksDB productivity");
            twice = !twice;

        } while (twice);
    }

    @Test
    public void testBench() {

        logger.info("Start test RocksDB productivity commit");
        String NAME_TABLE = "RocksDbDataSourceDB";

        // DELETE перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        File tempDir = new File(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER);
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        int countCommitTMP = 0;

        DBRocksDBTableDBOptTransacted rocksDB = new DBRocksDBTableDBOptTransacted(NAME_TABLE);

        long timeMillisBefore = System.currentTimeMillis();
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (++countCommitTMP > countCommit) {
                countCommitTMP = 0;
                logger.info("PUT to rocksDB on SIZE: " + rocksDB.size() + " ms: " + (System.currentTimeMillis() - timeMillisBefore));
                timeMillisBefore = System.currentTimeMillis();
            }

            rocksDB.put(entry.getKey(), entry.getValue());
        }

        logger.info("SIZE = " + rocksDB.size());

        countCommitTMP = 0;
        timeMillisBefore = System.currentTimeMillis();
        for (int i = 0; i < countData; i++) {

            if (++countCommitTMP > countCommit << 3) {
                countCommitTMP = 1;
                logger.info("SEEK to rocksDB on SIZE: " + rocksDB.size() + " ms: " + (System.currentTimeMillis() - timeMillisBefore));
                timeMillisBefore = System.currentTimeMillis();
            }

            rocksDB.containsKey(UUID.randomUUID().toString().getBytes());
        }

        countCommitTMP = 0;
        timeMillisBefore = System.currentTimeMillis();
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (++countCommitTMP > countCommit) {
                countCommitTMP = 1;
                logger.info("REMOVE to rocksDB on SIZE: " + rocksDB.size() + " ms: " + (System.currentTimeMillis() - timeMillisBefore));
                timeMillisBefore = System.currentTimeMillis();
            }

            rocksDB.delete(entry.getKey());
        }

        // теперь в транзакцию будем закатывать
        DBRocksDBTableOptTransaction dbOptTrans = new DBRocksDBTableOptTransaction(NAME_TABLE,
                (DBRocksDBTableDBOptTransacted) rocksDB);

        countCommitTMP = 0;
        timeMillisBefore = System.currentTimeMillis();
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (++countCommitTMP > countCommit) {
                countCommitTMP = 0;
                dbOptTrans.commit();
                logger.info("parent SIZE: " + rocksDB.size() + " parenSize: ");
                logger.info("PUT to rocksDB on SIZE: " + dbOptTrans.size() + " ms: " + (System.currentTimeMillis() - timeMillisBefore));
                timeMillisBefore = System.currentTimeMillis();
            }

            dbOptTrans.put(entry.getKey(), entry.getValue());
            assertEquals(rocksDB.containsKey(entry.getKey()), true);

        }

        dbOptTrans.commit();
        logger.info("SIZE = " + rocksDB.size());

        logger.info("PUT OPTIMISTIC TRANSACTION rocksDB = " + (System.currentTimeMillis() - timeMillisBefore));
        rocksDB.close();
        logger.info("End test RocksDB productivity");
    }

    @Test
    public void parentSize() {
    }

    @Test
    public void commit() {
    }

    @Test
    public void rollback() {
    }

    @Test
    public void close() {
    }
}