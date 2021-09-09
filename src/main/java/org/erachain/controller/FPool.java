package org.erachain.controller;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.DPSet;
import org.erachain.database.FPoolMap;
import org.erachain.datachain.CreditAddressesMap;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.settings.Settings;
import org.erachain.utils.MonitoredThread;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class FPool extends MonitoredThread {

    Controller controller;
    BlockChain blockChain;
    DCSet dcSet;
    DPSet dpSet;
    BigDecimal tax = new BigDecimal("5");

    BlockingQueue<Block> blockingQueue = new ArrayBlockingQueue<Block>(3);

    private boolean runned;

    TreeMap blocks = new TreeMap<Integer, Object[]>();

    private static final Logger LOGGER = LoggerFactory.getLogger(FPool.class.getSimpleName());

    FPool(Controller controller, BlockChain blockChain, DCSet dcSet) {
        this.controller = controller;
        this.blockChain = blockChain;
        this.dcSet = dcSet;

        this.setName("Forging Pool[" + this.getId() + "]");

        try {
            this.dpSet = DPSet.reCreateDB();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            try {
                this.dpSet.close();
            } catch (Exception e2) {
            }

            File dir = new File(Settings.getInstance().getFPoolDir());
            // delete dir
            if (dir.exists()) {
                try {
                    Files.walkFileTree(dir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
                } catch (IOException e3) {
                }
            }

            this.dpSet = DPSet.reCreateDB();
        }

        this.start();

    }

    public boolean offerBlock(Block block) {
        return blockingQueue.offer(block);
    }

    private void addRewards(Long assteKey, BigDecimal total, HashMap credits) {

    }

    public boolean processMessage(Block block) {

        if (block == null
                || !controller.isMyAccountByAddress(block.getCreator()))
            return false;

        PublicKeyAccount forger = block.getCreator();
        BigDecimal feeEarn = BigDecimal.valueOf(block.blockHead.totalFee + block.blockHead.emittedFee, BlockChain.FEE_SCALE);
        BigDecimal totalEmite = BigDecimal.ZERO;
        HashMap<AssetCls, Fun.Tuple2<BigDecimal, BigDecimal>> earnedAllAssets = block.getEarnedAllAssets();

        BigDecimal totalForginAmount = forger.getBalanceUSE(AssetCls.FEE_KEY);

        // make table of credits
        CreditAddressesMap creditMap = dcSet.getCredit_AddressesMap();
        HashMap<String, BigDecimal> credits = new HashMap();
        try (IteratorCloseable<Tuple3<String, Long, String>> iterator = creditMap.getIterator(new Tuple3<String, Long, String>
                (forger.getAddress(), AssetCls.FEE_KEY, null), false)) {

            Tuple3<String, Long, String> key;
            BigDecimal creditAmount;
            while (iterator.hasNext()) {
                key = iterator.next();
                creditAmount = creditMap.get(key);
                if (creditAmount.signum() <= 0)
                    continue;

                credits.put(key.c, creditAmount);
            }

        } catch (IOException e) {
            return false;
        }

        // FOR FEE
        addRewards(AssetCls.FEE_KEY, feeEarn, credits);

        if (false) {
            // FOR ERA
            addRewards(AssetCls.ERA_KEY, totalEmite, credits);
        }

        // for all ERNAED assets
        for (AssetCls assetEran : earnedAllAssets.keySet()) {
            Fun.Tuple2<BigDecimal, BigDecimal> item = earnedAllAssets.get(assetEran);
            addRewards(assetEran.getKey(), item.a, credits);
        }

        return true;

    }

    public void run() {

        runned = true;

        FPoolMap map = dpSet.getFPoolMap();

        while (runned) {

            // PROCESS
            try {
                processMessage(blockingQueue.poll(BlockChain.GENERATING_MIN_BLOCK_TIME(0), TimeUnit.SECONDS));
            } catch (OutOfMemoryError e) {
                blockingQueue = null;
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAndExit(2457);
                return;
            } catch (IllegalMonitorStateException e) {
                blockingQueue = null;
                Controller.getInstance().stopAndExit(2458);
                break;
            } catch (InterruptedException e) {
                blockingQueue = null;
                break;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }

        }

        LOGGER.info("Forging Pool halted");

    }

    public void halt() {
        this.runned = false;
        interrupt();
    }

}