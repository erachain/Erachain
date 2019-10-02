package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
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
public class RocksDbDataSourceDBTest {

    private Map<byte[], byte[]> data = new HashMap<>();

    private long countData = 100;

    private Set<Map.Entry<byte[], byte[]>> entrySet;

    @Before
    public void generateData() {
        for (int i = 0; i < countData; i++) {
            data.put(UUID.randomUUID().toString().getBytes(), UUID.randomUUID().toString().getBytes());
        }
        entrySet = data.entrySet();
    }

    @Test
    public void test1() {
        logger.info("Start test RocksDB productivity commit");
        String NAME_TABLE = "RocksDbDataSourceDB";

        // УДАЛИМ перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        File tempDir = new File(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER);
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        boolean twice = false;

        List<IndexDB> indexes = new ArrayList<>();
        RocksDbSettings dbSettings = new RocksDbSettings();

        do {
            long timeMillisBefore = System.currentTimeMillis();

            RocksDbDataSourceDB rocksDB = new RocksDbDataSourceDB(NAME_TABLE, indexes, dbSettings);

            int k = 0;
            int rollbacks = 0;

            int stepInit = 10;
            int stepCommit = twice ? stepInit << 1 : stepInit;
            int stepRollback = twice ? 30 : 20;

            for (Map.Entry<byte[], byte[]> entry : entrySet) {

                k++;

                if (twice) {
                    byte[] result = rocksDB.get(entry.getKey());
                    if (k <= stepInit) {
                        assertEquals(Arrays.equals(result, entry.getValue()), true);
                    } else {
                        assertEquals(result == null, true);
                    }
                    if (k == 1) {
                        logger.info("OPEN size :" + rocksDB.size());
                    }

                }

                try {
                    rocksDB.put(entry.getKey(), entry.getValue());

                    // по второму кругу первые 10 уже внесены и надо дальше пройти
                    if (k <= stepCommit) {
                        if (k == stepCommit) {
                            logger.info("size :" + rocksDB.size());
                            if (!twice) {
                                assertEquals(stepCommit, rocksDB.size());
                            } else {
                                assertEquals(stepCommit, rocksDB.size());
                            }

                        }
                        continue;
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
    public void openDB() {
    }
}