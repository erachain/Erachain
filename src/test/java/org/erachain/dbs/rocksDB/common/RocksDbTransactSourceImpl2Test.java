package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableTransact;
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
public class RocksDbTransactSourceImpl2Test {


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

            DBRocksDBTableTransact<byte[], byte[]> rocksDB = new DBRocksDBTableTransact(NAME_TABLE);

            int k = 0;

            for (Map.Entry<byte[], byte[]> entry : entrySet) {
                if (k++ > 10) break;

                rocksDB.put(entry.getKey(), entry.getValue());
            }
            logger.info("size :" + rocksDB.size());

            k = 0;
            int rollbacks = 0;

            for (Map.Entry<byte[], byte[]> entry : entrySet) {
                k++;
                try {

                    if (true || k % 3 == 0) {
                        rocksDB.put(entry.getKey(), entry.getValue());
                    } else if (true || k % 5 == 0) {
                        //transaction2.put(entry.getKey(), entry.getValue());
                    }
                    //rocksDB.put(entry.getKey(), entry.getValue());

                    if (k % 50 == 0) {
                        // TRY ROLLBACK
                        //assertEquals(k, rocksDB.size());

                        logger.info("size before ROLLBACK :" + rocksDB.size());
                        rocksDB.rollback();
                        int size = rocksDB.size();
                        logger.info("size after ROLLBACK :" + size);

                        assertEquals(k - rollbacks, size);

                        break;

                    } else if (false && k % 10 == 0) {
                        // TRY COMMIT
                        rocksDB.commit();
                        assertEquals(k, rocksDB.size());

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
    public void keySet() {
    }

    @Test
    public void values() {
    }

    @Test
    public void getIterator() {
    }

    @Test
    public void getIterator1() {
    }

    @Test
    public void put() {
    }

    @Test
    public void put1() {
    }

    @Test
    public void get() {
    }

    @Test
    public void get1() {
    }

    @Test
    public void remove() {
    }

    @Test
    public void remove1() {
    }
}