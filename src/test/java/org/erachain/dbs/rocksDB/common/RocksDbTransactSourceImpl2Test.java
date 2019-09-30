package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Before;
import org.junit.Test;
import org.rocksdb.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;
import static org.junit.Assert.assertEquals;

@Slf4j
public class RocksDbTransactSourceImpl2Test {


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

        Path path = Paths.get(tempDir.toPath().toString(), NAME_TABLE);
        List<IndexDB> indexes = new ArrayList<>();
        RocksDbSettings settings = new RocksDbSettings();


        boolean twice = false;
        do {
            long timeMillisBefore = System.currentTimeMillis();

            DBRocksDBTable<byte[], byte[]> rocksDB = new DBRocksDBTable(NAME_TABLE);
            WriteOptions transactWriteOptions = new WriteOptions().setSync(true).setDisableWAL(false);
            ReadOptions transactReadOptions = new ReadOptions();

            Transaction transaction1 = rocksDB.dbSource.getDbCore().beginTransaction(transactWriteOptions);
            List<ColumnFamilyHandle> families = rocksDB.dbSource.getColumnFamilyHandles();
            ColumnFamilyHandle sizeFamily = families.get(families.size() - 1);

            RocksDbDataSourceImpl dbSource = new RocksDbDataSourceImpl(path.toString(), indexes, settings);
            Transaction transaction2 = dbSource.dbCore.beginTransaction(transactWriteOptions);

            int k = 0;
            int rollbacks = 0;

            for (Map.Entry<byte[], byte[]> entry : entrySet) {
                k++;
                try {

                    if (k % 3 == 0) {
                        transaction1.put(entry.getKey(), entry.getValue());
                    } else if (true || k % 5 == 0) {
                        transaction2.put(entry.getKey(), entry.getValue());
                    }
                    //rocksDB.put(entry.getKey(), entry.getValue());

                    if (k % 50 == 0) {
                        // TRY ROLLBACK
                        //assertEquals(k, rocksDB.size());

                        logger.info("size before ROLLBACK :" + RocksDbTransactSourceImpl2.size(transaction1, sizeFamily, transactReadOptions));
                        transaction1.rollback();
                        int size = RocksDbTransactSourceImpl2.size(transaction1, sizeFamily, transactReadOptions);
                        logger.info("size after ROLLBACK :" + size);

                        assertEquals(k - rollbacks, size);

                        rocksDB.rollback();
                        rollbacks += 10;
                        assertEquals(k - rollbacks, rocksDB.size());

                        break;

                    } else if (false && k % 10 == 0) {
                        // TRY COMMIT
                        rocksDB.commit();
                        assertEquals(k, rocksDB.size());

                    }
                } catch (RocksDBException e) {
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