package org.erachain.dbs.rocksDB.common;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableTransactionSingle;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;
import org.rocksdb.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

@Slf4j
public class RocksDbDataSourceImplTest {

    @Test
    public void put() {
        logger.info("Start test RocksDB Com");
        String NAME_DATABASE = "TestRocksDBCom";

        // УДАЛИМ перед первым проходом - для проверки транзакционности при создании БД
        // а второй проход с уже созданной базой так же проверим, а то может быть разница в настройках у транзакций
        File tempDir = new File(Settings.getInstance().getDataDir() + ROCKS_DB_FOLDER);
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        boolean twice = false;
        do {



            Options createOptions = new Options();
            DBOptions openOptions = new DBOptions();
            TransactionDBOptions transactionDBOptions = new TransactionDBOptions();
            WriteOptions writeOptions = new WriteOptions().setSync(true).setDisableWAL(false);

            List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
            DBRocksDBTable<byte[], byte[]> rocksDB = new DBRocksDBTableTransactionSingle(NAME_DATABASE);
            TransactionDB rocksDBTransact = RocksDbDataSourceImpl.initDB(
                    tempDir.toPath(), createOptions, openOptions, transactionDBOptions,
                    columnFamilyDescriptors, columnFamilyHandles);

            Transaction transaction = rocksDBTransact.beginTransaction(writeOptions);
            twice = !twice;

        } while (twice);

    }

    @Test
    public void remove() {
    }
}