package org.erachain.dbs.rocksDB.integration;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;
import static org.junit.Assert.assertEquals;

@Slf4j
public class DBRocksDBTableTransactionSingleTest {

    private List<Map.Entry<byte[], byte[]>> data = new ArrayList<>();

    private long countData = 10000;

    @Before
    public void generateData() {
        for (int i = 0; i < countData; i++) {
            data.add(new AbstractMap.SimpleEntry(UUID.randomUUID().toString().getBytes(), UUID.randomUUID().toString().getBytes()));
        }
    }

    @Test
    public void test1() {
        logger.info("Start test RocksDB productivity commit");
        String NAME_TABLE = "RocksDbTableTransactionSimple";

        // УДАЛИМ перед первым проходом - для проверки транзакционности при создании БД
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

            DBRocksDBTable rocksDB = new DBRocksDBTableTransactionSingle(NAME_TABLE);
            logger.info("SIZE = " + rocksDB.size());

            int k = 0;

            for (Map.Entry<byte[], byte[]> entry : data) {

                k++;

                try {
                    rocksDB.put(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    break;
                }
            }

            logger.info("SIZE = " + rocksDB.size());

            k = 0;
            for (Map.Entry<byte[], byte[]> entry : data) {

                k++;

                found = rocksDB.containsKey(entry.getKey());
                found = rocksDB.containsKey(UUID.randomUUID().toString().getBytes());

            }

            k = 0;
            for (Map.Entry<byte[], byte[]> entry : data) {

                k++;

                found = rocksDB.containsKey(entry.getKey());
                assertEquals(found, true);

                rocksDB.remove(entry.getKey());

                found = rocksDB.containsKey(entry.getKey());
                assertEquals(found, false);

            }

            logger.info("SIZE = " + rocksDB.size());

            long timeMillisAfter = System.currentTimeMillis();
            long total = timeMillisAfter - timeMillisBefore;
            logger.info("total time rocksDB = " + total);
            rocksDB.close();
            logger.info("End test RocksDB productivity");
            twice = !twice;

        } while (twice);

    }

    @Test
    public void rocksDBCommit() {
        logger.info("Start test RocksDB productivity commit");
        String NAME_TABLE = "TestRocksDBCommit";

        // УДАЛИМ перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        File tempDir = new File(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER);
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        boolean twice = false;

        do {
            long timeMillisBefore = System.currentTimeMillis();

            DBRocksDBTableTransactionSingle<byte[], byte[]> rocksDB = new DBRocksDBTableTransactionSingle(NAME_TABLE);

            int k = 0;
            int rollbacks = 0;

            int stepInit = 10;
            int stepCommit = twice ? stepInit << 1 : stepInit;
            int stepRollback = twice ? 30 : 20;

            for (Map.Entry<byte[], byte[]> entry : data) {

                k++;

                if (twice && k <= stepCommit) {
                    byte[] result = rocksDB.get(entry.getKey());
                    if (k <= stepInit) {
                        assertEquals(Arrays.equals(result, entry.getValue()), true);
                    } else {
                        assertEquals(result == null, true);
                    }
                    if (k == 1) {
                        logger.info("OPEN size :" + rocksDB.size());
                        logger.info("OPEN size Parent:" + rocksDB.parentSize());
                    }

                }

                try {
                    rocksDB.put(entry.getKey(), entry.getValue());

                    // по второму кругу первые 10 уже внесены и надо дальше пройти
                    if (k <= stepCommit) {
                        if (k == stepCommit) {
                            logger.info("size :" + rocksDB.size());
                            logger.info("size Parent:" + rocksDB.parentSize());
                            if (!twice) {
                                assertEquals(stepCommit, rocksDB.size());
                                assertEquals(0, rocksDB.parentSize());
                            } else {
                                assertEquals(stepCommit, rocksDB.size());
                                assertEquals(stepInit, rocksDB.parentSize());
                            }

                            // TRY COMMIT
                            rocksDB.commit();

                            assertEquals(rocksDB.size(), rocksDB.parentSize());
                            logger.info("size :" + rocksDB.size());
                            logger.info("size Parent:" + rocksDB.parentSize());
                        }
                        continue;
                    } else if (k <= stepRollback) {
                        rollbacks++;
                        if (k == stepRollback) {
                            logger.info("size BEFORE rollback:" + rocksDB.size());
                            logger.info("size Parent:" + rocksDB.parentSize());

                            // TRY ROLLBACK
                            rocksDB.rollback();

                            byte[] result = rocksDB.get(entry.getKey());
                            assertEquals(Arrays.equals(result, entry.getValue()), false);

                            logger.info("size AFTER rollback:" + rocksDB.size());
                            logger.info("size Parent:" + rocksDB.parentSize());
                            assertEquals(k - rollbacks, rocksDB.size());
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
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
    public void rocksDBCommit2() {
        logger.info("Start test RocksDB productivity commit");
        String NAME_TABLE = "TestRocksDBCommit";

        // УДАЛИМ перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        File tempDir = new File(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER);
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        boolean twice = false;

        do {
            long timeMillisBefore = System.currentTimeMillis();

            DBRocksDBTableTransact2<byte[], byte[]> rocksDB = new DBRocksDBTableTransact2(NAME_TABLE);

            int k = 0;
            int rollbacks = 0;

            int stepInit = 10;
            int stepCommit = twice ? stepInit << 1 : stepInit;
            int stepRollback = twice ? 30 : 20;

            for (Map.Entry<byte[], byte[]> entry : data) {

                k++;

                if (twice && k <= stepCommit) {
                    byte[] result = rocksDB.get(entry.getKey());
                    if (k <= stepInit) {
                        assertEquals(Arrays.equals(result, entry.getValue()), true);
                    } else {
                        assertEquals(result == null, true);
                    }
                    if (k == 1) {
                        logger.info("OPEN size :" + rocksDB.size());
                        logger.info("OPEN size Parent:" + rocksDB.parentSize());
                    }

                }

                try {
                    rocksDB.put(entry.getKey(), entry.getValue());

                    // по второму кругу первые 10 уже внесены и надо дальше пройти
                    if (k <= stepCommit) {
                        if (k == stepCommit) {
                            logger.info("size :" + rocksDB.size());
                            logger.info("size Parent:" + rocksDB.parentSize());
                            if (!twice) {
                                assertEquals(stepCommit, rocksDB.size());
                                assertEquals(0, rocksDB.parentSize());
                            } else {
                                assertEquals(stepCommit, rocksDB.size());
                                assertEquals(stepInit, rocksDB.parentSize());
                            }

                            // TRY COMMIT
                            rocksDB.commit();

                            assertEquals(rocksDB.size(), rocksDB.parentSize());
                            logger.info("size :" + rocksDB.size());
                            logger.info("size Parent:" + rocksDB.parentSize());
                        }
                        continue;
                    } else if (k <= stepRollback) {
                        rollbacks++;
                        if (k == stepRollback) {
                            logger.info("size BEFORE rollback:" + rocksDB.size());
                            logger.info("size Parent:" + rocksDB.parentSize());

                            // TRY ROLLBACK
                            rocksDB.rollback();

                            byte[] result = rocksDB.get(entry.getKey());
                            assertEquals(Arrays.equals(result, entry.getValue()), false);

                            logger.info("size AFTER rollback:" + rocksDB.size());
                            logger.info("size Parent:" + rocksDB.parentSize());
                            assertEquals(k - rollbacks, rocksDB.size());
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
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
    public void rollback() {
    }
}