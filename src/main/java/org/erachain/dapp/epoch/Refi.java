package org.erachain.dapp.epoch;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.EpochDAPPjson;
import org.erachain.datachain.DCSet;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class Refi extends EpochDAPPjson {


    static public final int ID = 1012;
    static public final String NAME = "Referal dApp";
    static public final String ASSET_NAME = "REFI";
    static public final long ASSET_QUALITY = 25000000;

    // APPBjF5fbGj18aaXKSXemmHConG7JLBiJg
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

    public static final int ASSET_DECIMALS = 18;
    /**
     * admin account
     */
    final static public Account adminAddress = new Account("7NhZBb8Ce1H2S2MkPerrMnKLZNf9ryNYtP");

    /**
     * ASSET KEY
     */
    static final private Tuple2 INIT_KEY = new Tuple2(ID, "i");
    final static public String COMMAND_JOB = "job";

    static final int SKIP = 10;

    /**
     * por calculate reward - amount of blocks in one year
     */
    static final BigDecimal STAKE_PERIOD_KOEFF = new BigDecimal(1.0d / (30.0d * 24.0d * 120.0d)).setScale(ASSET_DECIMALS + 3, RoundingMode.HALF_DOWN);
    static final BigDecimal STAKE_KOEFF_1 = new BigDecimal("0.1");
    static final BigDecimal STAKE_KOEFF_2 = new BigDecimal("0.15");
    static final BigDecimal STAKE_KOEFF_3 = new BigDecimal("0.20");
    static final BigDecimal STAKE_KOEFF_4 = new BigDecimal("0.25");
    static final BigDecimal STAKE_KOEFF_5 = new BigDecimal("0.30");
    static final BigDecimal STAKE_KOEFF_6 = new BigDecimal("0.35");


    public Refi(String data, String status) {
        super(ID, MAKER, data, status);
    }

    public String getName() {
        return NAME;
    }

    private boolean isAdminCommand(Account txCreator) {
        return txCreator.equals(adminAddress);
    }

    private static BigDecimal stakeKoeff(Account account, BigDecimal stake) {

        if (stake.compareTo(new BigDecimal(150000)) >= 0) {
            return STAKE_KOEFF_6;
        } else if (stake.compareTo(new BigDecimal(50000)) >= 0) {
            return STAKE_KOEFF_5;
        } else if (stake.compareTo(new BigDecimal(15000)) >= 0) {
            return STAKE_KOEFF_4;
        } else if (stake.compareTo(new BigDecimal(1500)) >= 0) {
            return STAKE_KOEFF_3;
        } else if (stake.compareTo(new BigDecimal(150)) >= 0) {
            return STAKE_KOEFF_2;
        } else if (stake.compareTo(new BigDecimal(10)) >= 0) {
            return STAKE_KOEFF_1;
        } else
            return BigDecimal.ZERO;
    }

    private static Object[] makeNewPoin(Long refDB, Integer height,
                                        Account account, BigDecimal stake, Object[] point) {

        Object[] pointNew;

        if (point == null) {
            pointNew = new Object[]{height, BigDecimal.ZERO, height, stakeKoeff(account, stake)};
        } else if (height.equals(point[2])) {
            // не пересчитываем - только коэффициент
            pointNew = new Object[]{point[0], point[1], height, stakeKoeff(account, stake)};
        } else {
            // расчет новой ожидающей награды у получателя и обновление даты для нового начала отсчета потом
            BigDecimal pendingReward = (BigDecimal) point[1];
            Integer pendingRewardHeight = (Integer) point[2];
            BigDecimal rewardKoeff = (BigDecimal) point[3];
            BigDecimal reward = stake.multiply(new BigDecimal(height - pendingRewardHeight)).multiply(rewardKoeff)
                    .multiply(STAKE_PERIOD_KOEFF).setScale(ASSET_DECIMALS, RoundingMode.HALF_DOWN);
            BigDecimal pendingRewardNew = pendingReward.add(reward);

            pointNew = new Object[]{point[0], pendingRewardNew, height, stakeKoeff(account, stake)};
        }

        return pointNew;
    }

    ///////// COMMANDS
    private boolean job(DCSet dcSet, Block block, RSend rSend, boolean asOrphan) {

        PublicKeyAccount sender = rSend.getCreator();
        String senderAddress = sender.getAddress();

        Account recipient = rSend.getRecipient();
        String recipientAddress = recipient.getAddress();

        if (asOrphan) {
            Object[] state = removeState(dcSet, rSend.getDBRef());

            // RESTORE OLD POINT
            valueSet(dcSet, senderAddress, state[0]);

            // RESTORE OLD POINT
            valueSet(dcSet, recipientAddress, state[1]);

            BigDecimal stakeReward = (BigDecimal) state[2];
            if (stakeReward.signum() > 0) {
                transfer(dcSet, block, rSend, stock, sender, stakeReward, rSend.getAssetKey(), true, null, null);
            }

        } else {

            Long assetKey = rSend.getAssetKey();
            Long refDB = rSend.getDBRef();
            Integer height = rSend.getBlockHeight();

            BigDecimal stakeReward;

            Object[] recipientPoint;
            if (recipient.equals(adminAddress)) {
                recipientPoint = null;

            } else {
                /////////// RECIPIENT REWARDS
                BigDecimal stake = recipient.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN).b;
                recipientPoint = (Object[]) valueGet(dcSet, recipientAddress);

                Object[] pointNew = makeNewPoin(refDB, height, recipient, stake, recipientPoint);

                // STORE NEW POINT
                valuePut(dcSet, recipientAddress, pointNew);
            }

            Object[] senderPoint;

            if (sender.equals(adminAddress)) {
                senderPoint = null;
                stakeReward = null;

            } else {
                /////////// SENDER REWARDS
                BigDecimal stake = sender.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN).b;
                senderPoint = (Object[]) valueGet(dcSet, senderAddress);

                Object[] pointNew = makeNewPoin(refDB, height, sender, stake, senderPoint);

                int lastHeightAction = (Integer) pointNew[0];
                if (height - lastHeightAction >= SKIP) {
                    stakeReward = (BigDecimal) pointNew[1];
                    transfer(dcSet, block, rSend, stock, sender, stakeReward, assetKey, false, null, "stake reward");
                    // reset pending reward
                    pointNew[0] = height;
                    pointNew[1] = BigDecimal.ZERO;
                } else {
                    stakeReward = null;
                }

                // STORE NEW POINT
                valuePut(dcSet, senderAddress, pointNew);

            }

            // STORE STATE for ORPHAN
            putState(dcSet, rSend.getDBRef(), new Object[]{senderPoint, recipientPoint, stakeReward});
            status = "done";

        }

        return true;

    }

    //////////////////// ADMIN PROCCESS

    /// INIT
    private boolean init(DCSet dcSet, Block block, Transaction commandTX, boolean asOrphan) {

        Account adminAccount = commandTX.getCreator();
        Long assetKey;

        /**
         * issue asset
         */
        BigDecimal amount = new BigDecimal("10000");
        if (asOrphan) {

            // need to remove INIT_KEY - for reinit after orphans
            assetKey = (Long) dcSet.getSmartContractValues().remove(INIT_KEY);

            // BACKWARDS from ADMIN
            transfer(dcSet, block, commandTX, stock, adminAccount, amount, assetKey, true, null, null);

            // orphan GRAVITA ASSET
            dcSet.getItemAssetMap().decrementDelete(assetKey);

        } else {

            if (!isAdminCommand(adminAccount)) {
                fail("not admin");
                return false;
            }

            if (dcSet.getSmartContractValues().contains(INIT_KEY)) {
                fail("already initated");
                return false;
            }

            AssetVenture asset = new AssetVenture(null, stock, ASSET_NAME, null, null,
                    null, AssetCls.AS_INSIDE_ASSETS, ASSET_DECIMALS, ASSET_QUALITY);
            asset.setReference(commandTX.getSignature(), commandTX.getDBRef());

            //INSERT INTO DATABASE
            assetKey = dcSet.getItemAssetMap().incrementPut(asset);
            dcSet.getSmartContractValues().put(INIT_KEY, assetKey);

            // TRANSFER GRAVITA to ADMIN
            transfer(dcSet, block, commandTX, stock, adminAccount, amount, assetKey, false, null, "init");

            status = "done";
        }

        return true;
    }

    @Override
    public boolean processByTime(DCSet dcSet, Block block, Transaction transaction) {
        fail("unknow command");
        return false;
    }

    @Override
    public boolean process(DCSet dcSet, Block block, Transaction commandTX) {

        /// COMMANDS
        if (COMMAND_JOB.equals(command))
            return job(dcSet, block, (RSend) commandTX, false);

            /// ADMIN COMMANDS
        else if ("init".equals(command))
            return init(dcSet, block, commandTX, false);

        fail("unknow command");
        return false;

    }

    @Override
    public void orphanByTime(DCSet dcSet, Block block, Transaction transaction) {
    }

    @Override
    public void orphan(DCSet dcSet, Transaction commandTX) {

        /// COMMANDS
        if (COMMAND_JOB.equals(command))
            job(dcSet, null, (RSend) commandTX, true);

            /// ADMIN COMMANDS
        else if ("init".equals(command))
            init(dcSet, null, commandTX, true);

    }

    public static Refi make(RSend txSend, String dataStr) {
        return new Refi(dataStr, "");
    }

    public static Refi tryMakeJob(RSend txSend) {
        DCSet dcSet = txSend.getDCSet();
        if (!txSend.hasAmount()
                || txSend.balancePosition() != Account.BALANCE_POS_OWN || txSend.isBackward()) {
            return null;
        }

        Long assetKey = (Long) dcSet.getSmartContractValues().get(INIT_KEY);
        if (assetKey == null) {
            return null;
        }

        return assetKey.equals(txSend.getAssetKey()) ? new Refi(COMMAND_JOB, "") : null;
    }

    /// PARSE / TOBYTES

    public static Refi Parse(byte[] bytes, int pos, int forDeal) {

        // skip ID
        pos += 4;

        String data;
        String status;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] dataSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            pos += 4;
            byte[] dataBytes = Arrays.copyOfRange(bytes, pos, pos + dataSize);
            pos += dataSize;
            data = new String(dataBytes, StandardCharsets.UTF_8);

            byte[] statusSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int statusLen = Ints.fromByteArray(statusSizeBytes);
            pos += 4;
            byte[] statusBytes = Arrays.copyOfRange(bytes, pos, pos + statusLen);
            pos += statusLen;
            status = new String(statusBytes, StandardCharsets.UTF_8);

        } else {
            data = "";
            status = "";
        }

        return new Refi(data, status);
    }

    public static void setDAPPFactory(HashMap<Account, Integer> stocks) {
        stocks.put(MAKER, ID);
    }

}
