package org.erachain.dbs.rocksDB.integration;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.Transacted;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;
import static org.junit.Assert.assertEquals;

@Slf4j
public class RocksDBVsMapDB {

    private Map<byte[], byte[]> data = new HashMap<>();

    private long countData = 100000;

    private Set<Map.Entry<byte[], byte[]>> entrySet;

    @Before
    public void generateData() {
        for (int i = 0; i < countData; i++) {
            data.put(UUID.randomUUID().toString().getBytes(), UUID.randomUUID().toString().getBytes());
        }
        entrySet = data.entrySet();
    }

    @Test
    public void rocksDBProductivityClose() {
        logger.info("Start test RocksDB productivity simple close");
        String NAME_DATABASE = "TestRocksDB";
        long timeMillisBefore = System.currentTimeMillis();
        DBRocksDBTable<byte[], byte[]> rocksDB = new DBRocksDBTableDB(NAME_DATABASE, true);
        for (Map.Entry<byte[], byte[]> entry : entrySet) {
            rocksDB.put(entry.getKey(), entry.getValue());
        }
        rocksDB.close();
        long timeMillisAfter = System.currentTimeMillis();
        long total = timeMillisAfter - timeMillisBefore;
        logger.info("total time rocksDB = " + total);
        logger.info("End test RocksDB productivity");
    }

    @Test
    public void rocksDBProductivity() {
        logger.info("Start test RocksDB productivity simple");
        String NAME_DATABASE = "TestRocksDB";
        long timeMillisBefore = System.currentTimeMillis();
        DBRocksDBTable<byte[], byte[]> rocksDB = new DBRocksDBTableDB(NAME_DATABASE, true);
        for (Map.Entry<byte[], byte[]> entry : entrySet) {
            rocksDB.put(entry.getKey(), entry.getValue());
        }
        long timeMillisAfter = System.currentTimeMillis();
        long total = timeMillisAfter - timeMillisBefore;
        logger.info("total time rocksDB = " + total);
        rocksDB.close();
        logger.info("End test RocksDB productivity");
    }


    /**
     * Если тут создается новый файл то все проходит успешно. Если файл уже создан то он открывается и ошибка преобразования
     * Нужно удалять файл для успешного теста
     * Видимо dbOptions не такие же как при создании базы
     */
    @Test
    public void rocksDBProductivityWithCommits() {
        logger.info("Start test RocksDB productivity commit");
        String NAME_DATABASE = "TestRocksDB1";

        // DELETE перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        try {
            File tempDir = new File(Settings.getInstance().getDataChainPath() + ROCKS_DB_FOLDER);
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        boolean twice = false;
        do {
            long timeMillisBefore = System.currentTimeMillis();
            DBRocksDBTable<byte[], byte[]> rocksDB = new DBRocksDBTableTransactionSingle(NAME_DATABASE, true);
            int k = 0;
            int rollbacks = 0;
            for (Map.Entry<byte[], byte[]> entry : entrySet) {
                k++;
                rocksDB.put(entry.getKey(), entry.getValue());

                if (k % 50 == 0) {
                    // TRY ROLLBACK
                    assertEquals(k, rocksDB.size());

                    ((Transacted) rocksDB).rollback();
                    rollbacks += 10;
                    assertEquals(k - rollbacks, rocksDB.size());

                    break;

                } else if (k % 10 == 0) {
                    // TRY COMMIT
                    ((Transacted) rocksDB).commit();
                    assertEquals(k, rocksDB.size());

                }
            }

            long timeMillisAfter = System.currentTimeMillis();
            long total = timeMillisAfter - timeMillisBefore;
            logger.info("total time rocksDB = " + total);
            rocksDB.close();
            logger.info("End test RocksDB productivity");
            twice = !twice;

        } while (twice);
    }

    @Test
    public void rocksDBProductivityWithCommitsICreator() {
        logger.info("Start test RocksDB productivity commit");
        String NAME_DATABASE = "TestRocksDB1";

        // DELETE перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        try {
            File tempDir = new File(Settings.getInstance().getDataChainPath() + ROCKS_DB_FOLDER);
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        boolean twice = false;
        do {
            long timeMillisBefore = System.currentTimeMillis();
            DBRocksDBTable<byte[], byte[]> rocksDB = new DBRocksDBTableTransactionSingle(NAME_DATABASE, true);
            int k = 0;
            int rollbacks = 0;
            for (Map.Entry<byte[], byte[]> entry : entrySet) {
                k++;
                rocksDB.put(entry.getKey(), entry.getValue());

                if (k % 50 == 0) {
                    // TRY ROLLBACK
                    assertEquals(k, rocksDB.size());

                    ((Transacted) rocksDB).rollback();
                    rollbacks += 10;
                    assertEquals(k - rollbacks, rocksDB.size());

                    break;

                } else if (k % 10 == 0) {
                    // TRY COMMIT
                    ((Transacted) rocksDB).commit();
                    assertEquals(k, rocksDB.size());

                }
            }

            long timeMillisAfter = System.currentTimeMillis();
            long total = timeMillisAfter - timeMillisBefore;
            logger.info("total time rocksDB = " + total);
            rocksDB.close();
            logger.info("End test RocksDB productivity");
            twice = !twice;

        } while (twice);
    }

}
