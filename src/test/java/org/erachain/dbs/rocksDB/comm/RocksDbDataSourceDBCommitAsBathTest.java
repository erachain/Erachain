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
            gb.process(dcSet);
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
                System.arraycopy(new byte[]{0, 0, 13, 22}, 0 , key4, 0, 4);
                mapTestDbs.delete(key4);

                assertEquals(mapTestDbs.contains(key4), false);

                dcSet.flush(0, true, false);

                assertEquals(mapTestDbs.contains(key4), false);

            } finally {
                dcSet.close();
            }
        }
    }

    @Test
    public void delete() {
    }
}