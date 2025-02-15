package org.erachain.core;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.block.Block;
import org.erachain.database.IDB;
import org.erachain.database.wallet.BlocksHeadMap;
import org.erachain.datachain.BlockMap;
import org.erachain.datachain.BlocksHeadsMap;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.junit.Test;
import org.mapdb.DB;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@Slf4j
public class BlockChainTest {

    @Test
    public void getTargetedMin() {
    }

    @Test
    public void calcWinValue() {
        DCSet dcSet = null;
        try {
            dcSet = DCSet.getInstance(false, false, false);
        } catch (Exception e) {
        }

        BlocksHeadsMap blocksMap = dcSet.getBlocksHeadsMap();
        System.out.println(blocksMap.recalcWeightFull(dcSet));
    }

    /**
     * Находит Заголовки Блока битые и перезаписывает их данными Блока
     * Находит разрыв цепочки
     */
    @Test
    public void checkMyHeaders() {

        DCSet dcSet = null;
        try {

            //String path = "ERA/";
            String path = "C:/Erachain/DATA_MAIN/";
            if (true) {
                File dbFile = new File(path + Settings.getInstance().getDataChainPath(), DCSet.DATA_FILE);
                DB database = DCSet.makeFileDB(dbFile);
                //CREATE INSTANCE
                dcSet = new DCSet(dbFile, database, false, false, false, IDB.DBS_MAP_DB);

            } else {
                dcSet = DCSet.getInstance(false, false, false);
            }
            BlockMap blockMap = dcSet.getBlockMap();
            BlocksHeadsMap blockHeadsMap = dcSet.getBlocksHeadsMap();

            int myHeight = dcSet.getBlockMap().size();;
            logger.info("Start TO: " + myHeight);
            int height = 1;
            Block block = blockMap.get(height);
            Block.BlockHead blockHead;
            byte[] lastSignature = block.getSignature();
            boolean repairHead = false;
            while (myHeight > height++) {

                block = blockMap.get(height);
                if (!Arrays.equals(lastSignature, block.getReference())) {
                    assertEquals("", "wrong chain reference at " + height);
                }

                blockHead = blockHeadsMap.get(height);
                if (!Arrays.equals(block.getReference(), blockHead.reference)) {
                    repairHead = true;
                    logger.warn("wrong Head reference at " + height);
                }
                if (!Arrays.equals(block.getTransactionsHash(), blockHead.transactionsHash)) {
                    repairHead = true;
                    logger.warn("wrong head transactionsHash at " + height);
                }
                if (!Arrays.equals(block.getSignature(), blockHead.signature)) {
                    repairHead = true;
                    logger.warn("wrong head signature at " + height);
                }

                if (repairHead) {
                    blockHeadsMap.put(height, new Block.BlockHead(block, blockHead));
                }

                lastSignature = block.getSignature();

                if (height % 10000 == 0) {
                    logger.info("Checked: " + height);
                    dcSet.clearCache();
                }
            }

            logger.info("Done");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.info("close DB");
            if (dcSet != null)
                dcSet.close();
        }

    }

}