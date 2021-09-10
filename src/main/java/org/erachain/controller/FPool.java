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
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.settings.Settings;
import org.erachain.utils.MonitoredThread;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class FPool extends MonitoredThread {

    final static int PENDING_PERIOD = 5;
    Controller controller;
    BlockChain blockChain;
    DCSet dcSet;
    DPSet dpSet;
    BigDecimal tax = new BigDecimal("5");

    BlockingQueue<Block> blockingQueue = new ArrayBlockingQueue<Block>(3);

    private boolean runned;

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


    //TreeMap<Integer, Object[]> pendingBlocks;
    HashMap<Tuple2<Long, String>, BigDecimal> results;
    //TreeMap<Tuple2<Long, String>, BigDecimal> pendingPays;

    private void addRewards(AssetCls asset, BigDecimal totalEarn, BigDecimal totalForginAmount, HashMap<String, BigDecimal> credits) {
        BigDecimal amount;
        int scale = asset.getScale() + 6;
        Tuple2<Long, String> key;
        for (String address : credits.keySet()) {
            amount = totalEarn.multiply(credits.get(address)).divide(totalForginAmount, scale, RoundingMode.DOWN);

            key = new Tuple2<>(asset.getKey(), address);

            if (results.containsKey(key)) {
                results.put(key, results.get(key).add(amount));
            } else {
                results.put(key, amount);
            }
        }

    }

    private boolean processMessage(Block block) {

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
                (forger.getAddress(), AssetCls.FEE_KEY, ""), false)) {

            Tuple3<String, Long, String> key;
            BigDecimal creditAmount;
            while (iterator.hasNext()) {
                key = iterator.next();

                if (!forger.equals(key.a) || !key.b.equals(AssetCls.FEE_KEY))
                    break;

                creditAmount = creditMap.get(key);
                if (creditAmount.signum() <= 0)
                    continue;

                credits.put(key.c, creditAmount);
            }

        } catch (IOException e) {
            return false;
        }

        results = new HashMap<>();

        // FOR FEE
        addRewards(BlockChain.FEE_ASSET, feeEarn, totalForginAmount, credits);

        if (false) {
            // FOR ERA
            addRewards(BlockChain.ERA_ASSET, totalEmite, totalForginAmount, credits);
        }

        if (credits.size() == 0)
            return true;

        // for all ERNAED assets
        for (AssetCls assetEran : earnedAllAssets.keySet()) {
            Fun.Tuple2<BigDecimal, BigDecimal> item = earnedAllAssets.get(assetEran);
            addRewards(assetEran, item.a, totalForginAmount, credits);
        }

        dpSet.getBlocksMap().put(block.heightBlock, new Object[]{block.getSignature(), results});

        return true;

    }

    private void checkPending() {
        try (IteratorCloseable iterator = IteratorCloseableImpl.make(dpSet.getBlocksMap().getIterator())) {
            for (Integer height : dpSet.getBlocksMap().keySet()) {
                if (height + PENDING_PERIOD > controller.getMyHeight())
                    continue;

                HashMap<Tuple2<Long, String>, BigDecimal> blockResults;
                Object[] item = pendingBlocks.get(height);
                if (dcSet.getBlockSignsMap().contains((byte[]) item[0])) {
                    blockResults = (HashMap<Tuple2<Long, String>, BigDecimal>) item[1];
                    for (Tuple2<Long, String> key : blockResults.keySet()) {
                        if (pendingPays.containsKey(key)) {
                            pendingPays.put(key, pendingPays.get(key).add(results.get(key)));
                        } else {
                            pendingPays.put(key, results.get(key));
                        }
                    }
                } else {
                    // block was orphaned
                    pendingBlocks.remove(height);
                }

            }
        }

    }

    private void payout() {
        IteratorCloseable
        try (Iterator iterator = pendingPays.entrySet().iterator()) {

        }
    }

    @Override
    public void run() {

        runned = true;

        FPoolMap map = dpSet.getFPoolMap();

        while (runned) {

            // PROCESS
            try {
                processMessage(blockingQueue.poll(BlockChain.GENERATING_MIN_BLOCK_TIME(0), TimeUnit.SECONDS));

                checkPending();

                payout();

            } catch (OutOfMemoryError e) {
                blockingQueue = null;
                LOGGER.error(e.getMessage(), e);
                dpSet.close();
                Controller.getInstance().stopAndExit(2457);
                return;
            } catch (IllegalMonitorStateException e) {
                blockingQueue = null;
                dpSet.close();
                Controller.getInstance().stopAndExit(2458);
                return;
            } catch (InterruptedException e) {
                blockingQueue = null;
                break;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }

        }

        dpSet.close();
        LOGGER.info("Forging Pool halted");

    }

    public void halt() {
        this.runned = false;
        interrupt();
    }

}