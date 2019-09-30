package org.erachain.dbs.rocksDB.integration;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.common.RocksDbDataSourceImpl;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Before;
import org.junit.Test;
import org.rocksdb.*;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;
import static org.junit.Assert.assertEquals;

@Slf4j
public class RocksDBTransactions {

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
        String NAME_DATABASE = "TestRocksDB1";

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
            Options createOptions = new Options();
            DBOptions openOptions = new DBOptions();
            TransactionDBOptions transactionDBOptions = new TransactionDBOptions();
            WriteOptions writeOptions = new WriteOptions().setSync(true).setDisableWAL(false);

            List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
            DBRocksDBTable<byte[], byte[]> rocksDB = new DBRocksDBTable(NAME_DATABASE);
            TransactionDB rocksDBTransact = RocksDbDataSourceImpl.initDB(
                    tempDir.toPath(), createOptions, openOptions, transactionDBOptions,
                    columnFamilyDescriptors, columnFamilyHandles);

            Transaction transaction = rocksDBTransact.beginTransaction(writeOptions);

            int k = 0;
            int rollbacks = 0;
            for (Map.Entry<byte[], byte[]> entry : entrySet) {
                try {

                    k++;
                    transaction.put(entry.getKey(), entry.getValue());

                    if (k % 50 == 0) {
                        // TRY ROLLBACK
                        assertEquals(k, rocksDB.size());

                        transaction.rollback();

                        rollbacks += 10;
                        assertEquals(k - rollbacks, rocksDB.size());

                        break;


                    } else if (k % 10 == 0) {
                        // TRY COMMIT
                        rocksDB.commit();
                        assertEquals(k, rocksDB.size());

                    }
                } catch (RocksDBException e) {
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
