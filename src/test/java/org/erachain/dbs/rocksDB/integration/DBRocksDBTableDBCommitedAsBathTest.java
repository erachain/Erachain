package org.erachain.dbs.rocksDB.integration;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Before;
import org.junit.Test;
import org.rocksdb.WriteOptions;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;
import static org.junit.Assert.assertEquals;

@Slf4j
public class DBRocksDBTableDBCommitedAsBathTest {

    private List<Map.Entry<byte[], byte[]>> data = new ArrayList<>();

    private long countData = 10000;
    private int countCommit = 1000;

    String NAME_TABLE = "RocksDbTableAsBatch";

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
        logger.info("Start test RocksDB productivity commit");

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

            DBRocksDBTableDBCommitedAsBath rocksDB = new DBRocksDBTableDBCommitedAsBath(NAME_TABLE);

            int k = 0;

            for (Map.Entry<byte[], byte[]> entry : data) {

                k++;

                try {
                    rocksDB.put(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            logger.info("SIZE = " + rocksDB.size());

            k = 0;
            for (Map.Entry<byte[], byte[]> entry : data) {

                k++;

                // CLONE - чтобы не было совпадения по ссылке на память где объект лежит
                found = rocksDB.containsKey(entry.getKey().clone());
                assertEquals(found, true);
                byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
                assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), true);

                found = rocksDB.containsKey(UUID.randomUUID().toString().getBytes());
                assertEquals(found, false);

            }

            k = 0;
            for (Map.Entry<byte[], byte[]> entry : data) {

                k++;

                found = rocksDB.containsKey(entry.getKey().clone());
                assertEquals(found, true);
                byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
                assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), true);

                rocksDB.remove(entry.getKey().clone());

                found = rocksDB.containsKey(entry.getKey().clone());
                assertEquals(found, false);
                value = (byte[]) rocksDB.get(entry.getKey().clone());
                assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), false);

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
    public void test2() {

        logger.info("Start test RocksDB productivity commit");

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

            DBRocksDBTableDBCommitedAsBath rocksDB = new DBRocksDBTableDBCommitedAsBath(NAME_TABLE);

            int k = 0;
            int step = 10;

            // создадим в базе несколько записей
            for (Map.Entry<byte[], byte[]> entry : data) {

                if (++k > step) break;

                rocksDB.put(entry.getKey(), entry.getValue());
            }

            rocksDB.commit();
            logger.info("SIZE = " + rocksDB.size());

            k = 0;
            for (Map.Entry<byte[], byte[]> entry : data) {

                if (++k > step) break;

                assertEquals(rocksDB.containsKey(entry.getKey().clone()), true);
                byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
                assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), true);

            }

            for (int i = k; i < step; i++) {

                Map.Entry<byte[], byte[]> entry = data.get(i);

                // поиск в родительской базе
                assertEquals(rocksDB.containsKey(entry.getKey().clone()), false);
                byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
                assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), false);

                rocksDB.put(entry.getKey(), entry.getValue());

                // поиск в родительской базе
                assertEquals(rocksDB.containsKey(entry.getKey().clone()), true);
                value = (byte[]) rocksDB.get(entry.getKey().clone());
                assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), true);

                rocksDB.remove(entry.getKey().clone());

                // поиск в родительской базе
                assertEquals(rocksDB.containsKey(entry.getKey().clone()), false);
                value = (byte[]) rocksDB.get(entry.getKey().clone());
                assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), false);

            }

            logger.info("SIZE = " + rocksDB.size());

            rocksDB.commit();

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

        // DELETE перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        File tempDir = new File(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER);
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        int countCommitTMP = 0;

        DBRocksDBTableDBCommitedAsBath rocksDB = new DBRocksDBTableDBCommitedAsBath(NAME_TABLE);

        long timeMillisBefore = System.currentTimeMillis();
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (++countCommitTMP > countCommit) {
                countCommitTMP = 0;
                logger.info("PUT to rocksDB on SIZE: " + rocksDB.size() + " ms: " + (System.currentTimeMillis() - timeMillisBefore));
                timeMillisBefore = System.currentTimeMillis();
            }

            rocksDB.put(entry.getKey().clone(), entry.getValue());
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

            rocksDB.remove(entry.getKey().clone());
        }

        // теперь в транзакцию будем закатывать

        countCommitTMP = 0;
        timeMillisBefore = System.currentTimeMillis();
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (++countCommitTMP > countCommit) {
                countCommitTMP = 0;
                rocksDB.commit();
                logger.info("parent SIZE: " + rocksDB.size());
                logger.info("PUT to rocksDB on SIZE: " + rocksDB.size() + " ms: " + (System.currentTimeMillis() - timeMillisBefore));
                timeMillisBefore = System.currentTimeMillis();
            }

            rocksDB.put(entry.getKey().clone(), entry.getValue());
            assertEquals(rocksDB.containsKey(entry.getKey().clone()), true);
            byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
            assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), true);

        }

        rocksDB.commit();
        logger.info("SIZE = " + rocksDB.size());

        logger.info("PUT OPTIMISTIC TRANSACTION rocksDB = " + (System.currentTimeMillis() - timeMillisBefore));
        rocksDB.close();
        logger.info("End test RocksDB productivity");
    }


    @Test
    public void size() {
        logger.info("Start test RocksDB productivity commit");

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

        int k = 0;
        int step = 3;

        do {

            DBRocksDBTableDBCommitedAsBath rocksDB = new DBRocksDBTableDBCommitedAsBath(NAME_TABLE);
            logger.info("SIZE = " + rocksDB.size());

            int i = 0;

            do {

                Map.Entry<byte[], byte[]> entry = data.get(k++);

                try {
                    rocksDB.put(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } while (i++ < step);

            logger.info("SIZE = " + rocksDB.size());

            rocksDB.commit();
            rocksDB.close();
            twice = !twice;

        } while (twice);
    }

    @Test
    public void parentSize() {
    }

    @Test
    public void commit() {
    }

    @Test
    public void rollbackPut() {
        logger.info("Start test RocksDB productivity commit");

        int step = 10;

        // DELETE перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        File tempDir = new File(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER);
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        int countCommitTMP = 0;

        DBRocksDBTableDBCommitedAsBath rocksDB = new DBRocksDBTableDBCommitedAsBath(NAME_TABLE);

        logger.info("pun in WriteBatch");

        int size;
        int k = step;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            rocksDB.put(entry.getKey(), entry.getValue());
        }

        size = rocksDB.size();
        logger.info("SIZE = " + size);

        logger.info("seek in uncommitted");

        /// проверим поиск с КЛОНЕ - первая половина должна находиться а вторая нет
        k = step << 1;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            assertEquals(rocksDB.containsKey(entry.getKey().clone()), k >= step);
            byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
            assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), k >= step);
        }

        logger.info("try commit");
        rocksDB.commit();

        assertEquals(size, rocksDB.size());
        logger.info("after commit SIZE = " + size);

        /// проверим поиск с КЛОНЕ после COMMIT - первая половина должна находиться а вторая нет
        k = step << 1;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            assertEquals(rocksDB.containsKey(entry.getKey().clone()), k >= step);
            byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
            assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), k >= step);
        }

        // добавим то что уже есть и сверх еще
        k = step << 1;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            rocksDB.put(entry.getKey().clone(), entry.getValue());
        }

        logger.info("uncommitted SIZE = " + rocksDB.size());
        assertEquals(size + step, rocksDB.size());

        /// должны все найтись
        k = step << 1;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            boolean found = rocksDB.containsKey(entry.getKey().clone());
            assertEquals(found, true);
            byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
            assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), true);
        }

        assertEquals(size + step, rocksDB.size());
        logger.info("new put SIZE = " + size);

        rocksDB.rollback();

        assertEquals(size, rocksDB.size());
        logger.info("rollback SIZE = " + rocksDB.size());

        /// проверим поиск с КЛОНЕ после ROLLBACK - первая половина должна находиться а вторая нет
        k = step << 1;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            assertEquals(rocksDB.containsKey(entry.getKey().clone()), k >= step);
            byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
            assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), k >= step);
        }

        rocksDB.close();
        logger.info("End test RocksDB productivity");
    }

    @Test
    public void rollbackRemove() {
        logger.info("Start test RocksDB productivity commit");

        int step = 10;

        // DELETE перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        File tempDir = new File(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER);
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        int countCommitTMP = 0;

        DBRocksDBTableDBCommitedAsBath rocksDB = new DBRocksDBTableDBCommitedAsBath(NAME_TABLE);

        logger.info("pun in WriteBatch");

        int size;
        int k = step << 1;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            rocksDB.put(entry.getKey().clone(), entry.getValue());
        }

        size = rocksDB.size();
        assertEquals(size, step << 1);

        logger.info("SIZE = " + size);

        logger.info("seek in uncommitted");

        k = step << 1;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            assertEquals(rocksDB.containsKey(entry.getKey().clone()), true);
            byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
            assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), true);
        }

        logger.info("try commit");
        rocksDB.commit();

        assertEquals(size, rocksDB.size());
        logger.info("after commit SIZE = " + size);

        k = step << 1;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            assertEquals(rocksDB.containsKey(entry.getKey().clone()), true);
            byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
            assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), true);
        }

        // половину удалим
        k = step;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            rocksDB.remove(entry.getKey().clone());
        }

        logger.info("uncommitted SIZE = " + rocksDB.size());
        assertEquals(size >> 1, rocksDB.size());

        /// проверим - первая половина должна НЕ находиться в базе а вторая должна
        k = step << 1;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            // тут первые должны быть найдены а вторая порция нет
            assertEquals(rocksDB.containsKey(entry.getKey().clone()), k < step);
            byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
            assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), k < step);
        }

        rocksDB.rollback();

        assertEquals(size, rocksDB.size());
        logger.info("rollback SIZE = " + rocksDB.size());

        /// проверим - должны все опять находиться

        k = step << 1;
        for (Map.Entry<byte[], byte[]> entry : data) {

            if (--k < 0)
                break;

            assertEquals(rocksDB.containsKey(entry.getKey().clone()), true);
            byte[] value = (byte[]) rocksDB.get(entry.getKey().clone());
            assertEquals(value != null && Arrays.equals(value, entry.getValue().clone()), true);
        }

        rocksDB.close();
        logger.info("End test RocksDB productivity");
    }

}