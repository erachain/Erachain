package org.erachain.dbs.rocksDB.integration;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.rocksDB.common.RocksDB;
import org.junit.Before;
import org.junit.Test;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;
import org.rocksdb.TransactionDB;
import org.rocksdb.WriteOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class RocksDBVsMapDB {

    private Map<byte[], byte[]> data = new HashMap<>();

    private long countData = 10000;

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
        InnerDBRocksDBTest<byte[], byte[]> rocksDB = new InnerDBRocksDBTest<>();
        String NAME_DATABASE = "TestRocksDB";
        long timeMillisBefore = System.currentTimeMillis();
        RocksDB db = new RocksDB(NAME_DATABASE);
        rocksDB.setDb(db);
        for (Map.Entry<byte[], byte[]> entry : entrySet) {
            rocksDB.put(entry.getKey(), entry.getValue());
        }
        db.close();
        long timeMillisAfter = System.currentTimeMillis();
        long total = timeMillisAfter - timeMillisBefore;
        logger.info("total time rocksDB = " + total);
        logger.info("End test RocksDB productivity");
    }

    @Test
    public void rocksDBProductivity() {
        logger.info("Start test RocksDB productivity simple");
        InnerDBRocksDBTest<byte[], byte[]> rocksDB = new InnerDBRocksDBTest<>();
        String NAME_DATABASE = "TestRocksDB";
        long timeMillisBefore = System.currentTimeMillis();
        RocksDB db = new RocksDB(NAME_DATABASE);
        rocksDB.setDb(db);
        for (Map.Entry<byte[], byte[]> entry : entrySet) {
            rocksDB.put(entry.getKey(), entry.getValue());
        }
        long timeMillisAfter = System.currentTimeMillis();
        long total = timeMillisAfter - timeMillisBefore;
        logger.info("total time rocksDB = " + total);
        db.close();
        logger.info("End test RocksDB productivity");
    }


    @Test
    public void rocksDBProductivityWithCommits() {
        logger.info("Start test RocksDB productivity commit");
        InnerDBRocksDBTest<byte[], byte[]> rocksDB = new InnerDBRocksDBTest<>();
        String NAME_DATABASE = "TestRocksDB";
        long timeMillisBefore = System.currentTimeMillis();
        RocksDB db = new RocksDB(NAME_DATABASE);
        TransactionDB transactionDB = (TransactionDB) db.getDb().getDatabase();
        rocksDB.setDb(db);
        boolean flagBegin = true;
        int k = 0;
        Transaction transaction = null;
        for (Map.Entry<byte[], byte[]> entry : entrySet) {
            if (flagBegin) {
                transaction = transactionDB.beginTransaction(new WriteOptions());
                flagBegin = false;
            }
            k++;
            try {
                if (k % 1000 != 0) {
                    rocksDB.put(entry.getKey(), entry.getValue());
                    continue;
                }
                transaction.commit();
                flagBegin = true;
            } catch (RocksDBException e) {
                try {
                    transaction.rollback();
                } catch (RocksDBException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
        long timeMillisAfter = System.currentTimeMillis();
        long total = timeMillisAfter - timeMillisBefore;
        logger.info("total time rocksDB = " + total);
        db.close();
        logger.info("End test RocksDB productivity");
    }

}
