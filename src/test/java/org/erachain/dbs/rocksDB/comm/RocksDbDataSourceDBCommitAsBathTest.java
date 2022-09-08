package org.erachain.dbs.rocksDB.comm;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.block.GenesisBlock;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMapSigns;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

@Slf4j
public class RocksDbDataSourceDBCommitAsBathTest {

    int[] TESTED_DBS = new int[]{
            IDB.DBS_ROCK_DB, IDB.DBS_MAP_DB,
            IDB.DBS_NATIVE_MAP, IDB.DBS_MAP_DB_IN_MEM};

    DCSet dcSet;
    GenesisBlock gb;

    private void init(int dbs) {

        logger.info(" ********** open DBS: " + dbs);

        File tempDir = new File(Settings.getInstance().getDataTempDir());
        try {
            Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
        } catch (Throwable e) {
        }

        dcSet = DCSet.createEmptyHardDatabaseSetWithFlush(null, dbs);
        gb = new GenesisBlock();

        try {
            gb.process(dcSet, false);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Суть в том что в пакете с незакрытой трнзакцией нужно поиск делать по пакету с учетом уделенных
     */
    @Test
    public void contains() {
        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // берем ту таблицу которая во всех СУБД реализована
                TransactionFinalMapSigns mapTestDbs = dcSet.getTransactionFinalMapSigns();

                byte[] key1 = new byte[64];
                System.arraycopy(new byte[]{0, 0, 123, 12}, 0 , key1, 0, 4);
                mapTestDbs.set(key1, 12L);
                byte[] key2 = new byte[64];
                System.arraycopy(new byte[]{0, 0, 13, 12}, 0 , key2, 0, 4);
                mapTestDbs.put(key2, 123L);

                byte[] key3 = new byte[64];
                System.arraycopy(new byte[]{0, 0, 123, 12}, 0 , key3, 0, 4);
                assertEquals(mapTestDbs.contains(key3), true);

                byte[] key4 = new byte[64];
                System.arraycopy(new byte[]{0, 0, 13, 12}, 0, key4, 0, 4);
                mapTestDbs.delete(key4);

                assertEquals(mapTestDbs.contains(key4), false);

                dcSet.flush(0, true, false);

                assertEquals(mapTestDbs.contains(key4), false);

            } finally {
                dcSet.close();
            }
        }
    }

    /**
     * Перед запуском установить подсчет записей в этой таблице
     */
    @Test
    public void size() {
        for (int dbs : TESTED_DBS) {

            try {
                init(dbs);

                // берем ту таблицу которая во всех СУБД реализована
                TransactionFinalMapSigns mapTestDbs = dcSet.getTransactionFinalMapSigns();

                if (mapTestDbs.size() < 0)
                    assertEquals("Перед запуском установить подсчет записей в этой таблице - datachain.DCSet.SIZE_ENABLE_IN_FINAL = false", "");
                int sizeGenesis = mapTestDbs.size();

                byte[] key1 = new byte[64];
                System.arraycopy(new byte[]{0, 0, 123, 12}, 0, key1, 0, 4);
                mapTestDbs.set(key1, 12L);
                assertEquals(sizeGenesis + 1, mapTestDbs.size());

                byte[] key2 = new byte[64];
                System.arraycopy(new byte[]{0, 0, 13, 12}, 0, key2, 0, 4);
                mapTestDbs.put(key2, 123L);
                assertEquals(sizeGenesis + 2, mapTestDbs.size());

                byte[] key3 = new byte[64];
                System.arraycopy(new byte[]{0, 0, 123, 12}, 0, key3, 0, 4);
                assertEquals(mapTestDbs.contains(key3), true);

                /// DELETE
                byte[] key4 = new byte[64];
                System.arraycopy(new byte[]{0, 0, 13, 12}, 0, key4, 0, 4);
                mapTestDbs.delete(key4);

                assertEquals(mapTestDbs.contains(key4), false);
                assertEquals(sizeGenesis + 1, mapTestDbs.size());

                dcSet.flush(0, true, false);

                assertEquals(sizeGenesis + 1, mapTestDbs.size());

                assertEquals(mapTestDbs.contains(key4), false);

                /// REMOVE
                byte[] key5 = new byte[64];
                System.arraycopy(new byte[]{0, 0, 123, 12}, 0, key5, 0, 4);
                mapTestDbs.remove(key5);

                assertEquals(mapTestDbs.contains(key5), false);
                assertEquals(sizeGenesis, mapTestDbs.size());

                dcSet.flush(0, true, false);

                assertEquals(sizeGenesis, mapTestDbs.size());


            } finally {
                dcSet.close();
            }
        }
    }


    @Test
    public void delete() {
    }
}