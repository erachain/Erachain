package org.erachain.core;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.block.Block;
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
        blocksMap.recalcWeightFull(dcSet);
    }

    @Test
    public void checkMyHeaders() {

        DCSet dcSet = null;
        try {

            if (true) {
                File dbFile = new File("ERA/" + Settings.getInstance().getDataChainPath(), DCSet.DATA_FILE);
                DB database = DCSet.makeFileDB(dbFile);
                //CREATE INSTANCE
                dcSet = new DCSet(dbFile, database, false, false, false, 0);

            } else {
                dcSet = DCSet.getInstance(false, false, false);
            }
            BlocksHeadsMap blockHeadsMap = dcSet.getBlocksHeadsMap();

            int myHeight = dcSet.getBlockSignsMap().size();
            logger.info(" start TO: " + myHeight);
            int height = 1;
            Block.BlockHead blockHead = blockHeadsMap.get(height);
            byte[] lastSignature = blockHead.signature;
            while (myHeight > height++) {

                blockHead = blockHeadsMap.get(height);
                if (!Arrays.equals(lastSignature, blockHead.reference)) {
                    assertEquals("", "wrong reference at " + height);
                }

                lastSignature = blockHead.signature;

                if (height % 10000 == 0) {
                    logger.info(" checked: " + height);
                    dcSet.clearCache();
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (dcSet != null)
                dcSet.close();
        }
    }

}