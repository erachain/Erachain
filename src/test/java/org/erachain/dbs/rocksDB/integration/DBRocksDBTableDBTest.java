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
import java.nio.file.NoSuchFileException;
import java.util.*;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;
import static org.junit.Assert.assertEquals;

@Slf4j
public class DBRocksDBTableDBTest {

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
        String NAME_TABLE = "RocksDbTable";

        // DELETE перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        File tempDir = new File(Settings.getInstance().getDataChainPath() + ROCKS_DB_FOLDER);
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (NoSuchFileException e) {
        } catch (Throwable e) {
        }

        boolean twice = false;
        boolean found;

        List<IndexDB> indexes = new ArrayList<>();
        RocksDbSettings dbSettings = new RocksDbSettings();

        do {
            long timeMillisBefore = System.currentTimeMillis();

            DBRocksDBTable rocksDB = new DBRocksDBTableDB(NAME_TABLE, true);

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

                found = rocksDB.containsKey(entry.getKey());
                found = rocksDB.containsKey(UUID.randomUUID().toString().getBytes());

            }

            k = 0;
            for (Map.Entry<byte[], byte[]> entry : data) {

                k++;

                found = rocksDB.containsKey(entry.getKey());
                assertEquals(found, true);

                rocksDB.delete(entry.getKey());

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
}