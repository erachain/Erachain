package org.erachain.smartcontracts.epoch.shibaverse.server;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.ExData;
import org.erachain.core.exdata.exActions.ExListPays;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DPSet;
import org.erachain.database.FPoolBalancesMap;
import org.erachain.database.FPoolBlocksHistoryMap;
import org.erachain.database.FPoolBlocksMap;
import org.erachain.datachain.CreditAddressesMap;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.json.simple.JSONObject;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Farm_01 extends Thread {

    static int PENDING_PERIOD = 30;
    static HashMap<Long, BigDecimal> MIN_WITHDRAWS;
    static BigDecimal POOL_TAX;

    JSONObject settingsJSON;
    Controller controller;
    BlockChain blockChain;
    DCSet dcSet;
    DPSet dpSet;
    Account account;

    BlockingQueue<Block> blockingQueue = new ArrayBlockingQueue<Block>(3);
    HashMap<Tuple2<Long, String>, BigDecimal> results;

    private boolean runned;

    private static final Logger LOGGER = LoggerFactory.getLogger(Farm_01.class.getSimpleName());

    public Farm_01(JSONObject settingsJSON) {
        this.controller = Controller.getInstance();
        this.blockChain = controller.getBlockChain();
        this.dcSet = controller.getDCSet();


        this.settingsJSON = settingsJSON;
        LOGGER.info("Start\n" + settingsJSON.toJSONString());

        account = new Account((String) settingsJSON.get("account"));
        POOL_TAX = new BigDecimal(settingsJSON.get("tax").toString()).movePointLeft(2);
        PENDING_PERIOD = Integer.parseInt(settingsJSON.getOrDefault("pending_period", 30).toString());

        this.setName("Shiba FARM 01 [" + this.getId() + "]");

        DB database = DCSet.makeDBinMemory();

        this.start();

    }

    public boolean offerBlock(Block block) {
        return blockingQueue.offer(block);
    }

    public BigDecimal getTax() {
        return POOL_TAX;
    }

    public void setTax(BigDecimal newTax) {
        POOL_TAX = newTax;
    }

    public String getAddress() {
        return account.getAddress();
    }

    public int getPendingPeriod() {
        return PENDING_PERIOD;
    }

    public JSONObject getAddressBalances(String address) {
        return dpSet.getBalancesMap().getAddressBalances(address);
    }

    private boolean balanceReady(Long assteKey, BigDecimal balance) {
        BigDecimal min_withdraw = MIN_WITHDRAWS.get(assteKey);
        if (min_withdraw == null) {
            min_withdraw = MIN_WITHDRAWS.get(0L);
        }

        return balance.compareTo(min_withdraw) > 0;

    }

    private void addRewards(AssetCls asset, BigDecimal totalEarn, BigDecimal totalForginAmount,
                            HashMap<String, BigDecimal> previouseCredits, HashMap<String, BigDecimal> credits) {
        BigDecimal amount;
        int scale = asset.getScale() + 8;
        Tuple2<Long, String> key;

        // USE TAX
        totalEarn = totalEarn.multiply(BigDecimal.ONE.subtract(POOL_TAX));

        BigDecimal previouseCredit;
        BigDecimal currentCredit;
        for (String address : credits.keySet()) {
            previouseCredit = previouseCredits.get(address);
            currentCredit = credits.get(address);
            if (previouseCredit == null || previouseCredit.signum() <= 0)
                continue;

            if (previouseCredit.compareTo(currentCredit) > 0)
                previouseCredit = currentCredit;

            amount = totalEarn.multiply(previouseCredit).divide(totalForginAmount, scale, RoundingMode.DOWN);
            if (amount.signum() == 0)
                continue;

            amount = amount.stripTrailingZeros();

            key = new Tuple2<>(asset.getKey(), address);

            if (results.containsKey(key)) {
                results.put(key, results.get(key).add(amount));
            } else {
                results.put(key, amount);
            }
        }

    }

    private boolean processMessage(Block block) {

        if (block.heightBlock % 100 == 0) {
            return false;
        }

        BigDecimal feeEarn = BigDecimal.valueOf(block.blockHead.totalFee + block.blockHead.emittedFee, BlockChain.FEE_SCALE);
        BigDecimal totalEmite = BigDecimal.ZERO;
        HashMap<AssetCls, Tuple2<BigDecimal, BigDecimal>> earnedAllAssets = block.getEarnedAllAssets();

        BigDecimal totalForginAmount = new BigDecimal(block.getForgingValue());

        // make table of credits
        CreditAddressesMap creditMap = dcSet.getCredit_AddressesMap();
        HashMap<String, BigDecimal> credits = new HashMap();
        try (IteratorCloseable<Tuple3<String, Long, String>> iterator = creditMap.getCreditorsIterator(
                account.getAddress(), AssetCls.ERA_KEY)) {

            Tuple3<String, Long, String> key;
            BigDecimal creditAmount;
            while (iterator.hasNext()) {
                key = iterator.next();

                creditAmount = creditMap.get(key);
                if (creditAmount.signum() <= 0)
                    continue;

                credits.put(key.a, creditAmount);
            }

        } catch (IOException e) {
            return false;
        }

        if (credits.size() == 0)
            return true;

        results = new HashMap<>();

        Map.Entry<Integer, Object[]> previoseBlock = dpSet.getBlocksMap().lastEntry();
        if (previoseBlock == null)
            previoseBlock = dpSet.getBlocksHistoryMap().lastEntry();

        if (previoseBlock != null) {
            // MAKE REWARDS
            HashMap<String, BigDecimal> previouseCredits = (HashMap<String, BigDecimal>) previoseBlock.getValue()[1];

            // FOR FEE
            if (feeEarn.signum() > 0) {
                addRewards(BlockChain.FEE_ASSET, feeEarn, totalForginAmount, previouseCredits, credits);
            }

            if (totalEmite.signum() > 0) {
                // FOR ERA
                addRewards(BlockChain.ERA_ASSET, totalEmite, totalForginAmount, previouseCredits, credits);
            }

            if (false && BlockChain.TEST_MODE) {
                // TEST MODE
                earnedAllAssets = new HashMap<>();
                earnedAllAssets.put(BlockChain.ERA_ASSET, new Tuple2<>(new BigDecimal("10"), BigDecimal.ZERO));
                earnedAllAssets.put(controller.getAsset(AssetCls.BTC_KEY), new Tuple2<>(new BigDecimal("0.000001"), BigDecimal.ZERO));
                earnedAllAssets.put(controller.getAsset(18L), new Tuple2<>(new BigDecimal("1"), BigDecimal.ZERO));
            }

            // for all ERNAED assets
            if (earnedAllAssets != null) {
                for (AssetCls assetEran : earnedAllAssets.keySet()) {
                    Tuple2<BigDecimal, BigDecimal> item = earnedAllAssets.get(assetEran);
                    addRewards(assetEran, item.a, totalForginAmount, previouseCredits, credits);
                }
            }
        }

        dpSet.getBlocksMap().put(block.heightBlock, new Object[]{block.getSignature(), credits, results});
        results = null;

        return true;

    }

    /**
     * Pending block - await add to payouts. height + signature(STRING)
     *
     * @return
     */
    public Object[][] getPendingBlocks() {

        FPoolBlocksMap blocksMap = dpSet.getBlocksMap();
        Object[][] result = new Object[blocksMap.size()][];
        try (IteratorCloseable<Integer> iterator = IteratorCloseableImpl.make(blocksMap.getIterator())) {
            Integer height;
            int index = 0;
            while (iterator.hasNext()) {
                height = iterator.next();
                result[index++] = new Object[]{height, Base58.encode((byte[]) blocksMap.get(height)[0])};
            }
        } catch (IOException e) {
        }

        return result;
    }

    public TreeMap<Tuple2<Long, String>, BigDecimal> getPendingWithdraws() {

        FPoolBalancesMap balanceMap = dpSet.getBalancesMap();
        TreeMap<Tuple2<Long, String>, BigDecimal> result = new TreeMap();
        try (IteratorCloseable<Tuple2<Long, String>> iterator = IteratorCloseableImpl.make(balanceMap.getIterator())) {
            BigDecimal balance;
            Tuple2<Long, String> key;
            while (iterator.hasNext()) {
                key = iterator.next();
                balance = balanceMap.get(key);
                if (balance.signum() > 0)
                    result.put(key, balance);

            }
        } catch (IOException e) {
        }

        return result;
    }

    public TreeMap<Tuple2<Long, String>, BigDecimal> getPendingWithdraws(String address) {

        FPoolBalancesMap balanceMap = dpSet.getBalancesMap();
        TreeMap<Tuple2<Long, String>, BigDecimal> result = new TreeMap();
        try (IteratorCloseable<Tuple2<Long, String>> iterator = IteratorCloseableImpl.make(balanceMap.getIterator())) {
            BigDecimal balance;
            Tuple2<Long, String> key;
            while (iterator.hasNext()) {
                key = iterator.next();
                balance = balanceMap.get(key);
                if (balance.signum() > 0)
                    result.put(key, balance);

            }
        } catch (IOException e) {
        }

        return result;
    }

    private void checkPending() {

        FPoolBlocksMap blocksMap = dpSet.getBlocksMap();
        FPoolBlocksHistoryMap blocksHistoryMap = dpSet.getBlocksHistoryMap();
        FPoolBalancesMap balsMap = dpSet.getBalancesMap();
        try (IteratorCloseable<Integer> iterator = IteratorCloseableImpl.make(blocksMap.getIterator())) {
            Integer height;
            while (iterator.hasNext()) {
                height = iterator.next();
                if (height + PENDING_PERIOD > controller.getMyHeight())
                    break;

                HashMap<Tuple2<Long, String>, BigDecimal> blockResults;
                Object[] pendingBlock = blocksMap.get(height);
                if (dcSet.getBlockSignsMap().contains((byte[]) pendingBlock[0])) {
                    // block was confirmed!
                    blockResults = (HashMap<Tuple2<Long, String>, BigDecimal>) pendingBlock[2];
                    for (Tuple2<Long, String> key : blockResults.keySet()) {
                        if (balsMap.contains(key)) {
                            balsMap.put(key, balsMap.get(key).add(blockResults.get(key)));
                        } else {
                            balsMap.put(key, blockResults.get(key));
                        }
                    }

                    // ADD to processed blocks
                    blocksHistoryMap.put(height, pendingBlock);
                }

                blocksMap.delete(height);

            }
        } catch (IOException e) {
            return;
        }

    }

    /**
     * @param allLeftover
     * @return True if has some withdraws
     */
    public boolean withdraw(boolean allLeftover) {
        FPoolBalancesMap balsMap = dpSet.getBalancesMap();

        Long assetKeyToWithdraw = null;
        List<Tuple2<String, BigDecimal>> payouts = new ArrayList();
        BigDecimal total = BigDecimal.ZERO;

        try (IteratorCloseable<Tuple2<Long, String>> iterator = IteratorCloseableImpl.make(balsMap.getIterator())) {
            Tuple2<Long, String> key;
            Long assetKey;
            BigDecimal balance;

            while (iterator.hasNext()) {
                key = iterator.next();
                assetKey = key.a;
                if (assetKeyToWithdraw != null && !assetKeyToWithdraw.equals(assetKey)) {
                    // уже собрали массив выплат по данному активу - выходим
                    break;
                }

                balance = balsMap.get(key);
                if (!allLeftover && !balanceReady(assetKey, balance) || balance.signum() <= 0) {
                    continue;
                }

                // BALANCE is GOOD for WITHDRAW
                payouts.add(new Tuple2(key.b, balance));
                total = total.add(balance);

                if (assetKeyToWithdraw == null)
                    assetKeyToWithdraw = assetKey;

            }
        } catch (IOException e) {
            return false;
        }

        if (assetKeyToWithdraw == null) {
            return false;
        }

        // if Total too small
        if (!allLeftover) {
            BigDecimal min_withdraw = MIN_WITHDRAWS.get(assetKeyToWithdraw);
            if (min_withdraw == null) {
                min_withdraw = MIN_WITHDRAWS.get(0L);
            }

            if (total.movePointLeft(1).compareTo(min_withdraw) < 0) {
                return false;
            }
        }

        //if (true)
        //    return false;

        AssetCls assetWithdraw = controller.getAsset(assetKeyToWithdraw);

        Tuple3<byte[], BigDecimal, String>[] addresses = new Tuple3[payouts.size()];
        int index = 0;
        Crypto crypto = Crypto.getInstance();
        BigDecimal toPay;
        for (Tuple2<String, BigDecimal> item : payouts) {
            toPay = item.b.setScale(assetWithdraw.getScale(), RoundingMode.DOWN);
            addresses[index++] = new Tuple3<byte[], BigDecimal, String>(crypto.getShortBytesFromAddress(item.a),
                    toPay, "");
        }

        /// MAKE WITHDRAW
        byte[] flags = new byte[]{3, 0, 0, 0};
        ExListPays listPays = new ExListPays(0, assetKeyToWithdraw, Account.BALANCE_POS_OWN, false, addresses);
        JSONObject exDataJSON = new JSONObject();
        exDataJSON.put("MS", settingsJSON.get("message") + "<p>Tax: <b>" + settingsJSON.get("tax") + "</b></p>");
        ExData exData = new ExData(flags, null, listPays, (String) settingsJSON.get("title"),
                (byte) 0, null, (byte) 0, null,
                (byte) 0, null, null, exDataJSON, null);

        byte version = (byte) 3;
        byte property1 = (byte) 0;
        byte property2 = (byte) 0;
        byte feePow = 0;
        RSignNote issueDoc = null;
        try {
            issueDoc = (RSignNote) Controller.getInstance().r_SignNote(version, property1, property2,
                    account, feePow, 0, exData.toByte());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }

        int validate = Controller.getInstance().getTransactionCreator().afterCreate(issueDoc, Transaction.FOR_NETWORK, false, false);
        if (validate != Transaction.VALIDATE_OK) {
            LOGGER.error(issueDoc.makeErrorJSON2(validate).toJSONString());
            return false;
        }

        /// UPADTE BALANCCES by withdraw amounts
        Tuple2<Long, String> balsKey;
        for (Tuple3<byte[], BigDecimal, String> item : addresses) {
            balsKey = new Tuple2<>(assetKeyToWithdraw, crypto.getAddressFromShort(item.a));
            if (allLeftover) {
                // CLEAR ALL
                balsMap.delete(balsKey);
            } else {
                // SAVE LEFT BALANCES
                balsMap.put(balsKey, balsMap.get(balsKey).subtract(item.b));
            }
        }

        return true;
    }

    @Override
    public void run() {

        runned = true;

        int count = 0;
        while (runned) {

            // PROCESS
            try {

                boolean forged = processMessage(blockingQueue.poll(BlockChain.GENERATING_MIN_BLOCK_TIME(0), TimeUnit.SECONDS));
                if (!forged && count++ % (PENDING_PERIOD >> 1) != 0)
                    continue;

                checkPending();

                withdraw(false);

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