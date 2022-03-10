package org.erachain.dapp.epoch;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.IssueItemRecord;
import org.erachain.core.transaction.RCalculated;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.EpochDAPPjson;
import org.erachain.datachain.*;
import org.erachain.dbs.IteratorCloseable;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Refi extends EpochDAPPjson {


    static public final int ID = 1012;
    static public final String NAME = "Referral dApp";
    static public final String ASSET_NAME = "REFI";
    static public final long ASSET_QUALITY = 25000000;
    public static final int ASSET_DECIMALS = 18;

    // APPBjF5fbGj18aaXKSXemmHConG7JLBiJg
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

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
    public static final int REFERRAL_LEVEL_DEEP = 5;
    public static final int REFERRAL_SHARE2 = 3;
    public static final int MAX_REFERRALS_COUNT = 100;
    public static final BigDecimal MAX_REFERRALS_STAKE = new BigDecimal(150000);
    /**
     * divide by power of 2
     */
    public static final int[] REFERRAL_LEVEL_KOEFFS = new int[]{1, 1, 1, 1, 1};


    public Refi(String data, String status) {
        super(ID, MAKER, data, status);
    }

    public String getName() {
        return NAME;
    }

    private boolean isAdminCommand(Account txCreator) {
        return txCreator.equals(adminAddress);
    }

    private BigDecimal stakeKoeff(Tuple2<Integer, BigDecimal> referrals) {

        if (referrals == null)
            return STAKE_KOEFF_1;

        BigDecimal koeff;
        if (referrals.a >= MAX_REFERRALS_COUNT && referrals.b.compareTo(MAX_REFERRALS_STAKE) >= 0) {
            koeff = STAKE_KOEFF_6;
        } else if (referrals.a >= 50 && referrals.b.compareTo(new BigDecimal(50000)) >= 0) {
            koeff = STAKE_KOEFF_5;
        } else if (referrals.a >= 20 && referrals.b.compareTo(new BigDecimal(15000)) >= 0) {
            koeff = STAKE_KOEFF_4;
        } else if (referrals.a >= 10 && referrals.b.compareTo(new BigDecimal(1500)) >= 0) {
            koeff = STAKE_KOEFF_3;
        } else if (referrals.a >= 5 && referrals.b.compareTo(new BigDecimal(150)) >= 0) {
            koeff = STAKE_KOEFF_2;
        } else {
            koeff = STAKE_KOEFF_1;
        }

        return koeff;

    }

    /**
     * point[height of withdraw, current reward, height of calculating reward, koeff, total referrals]
     *
     * @param assetKey
     * @param refDB
     * @param height
     * @param account
     * @param stake
     * @param point
     * @return
     */
    private Object[] makeNewPoint(Long assetKey, Long refDB, Integer height,
                                  Account account, BigDecimal stake, Object[] point) {

        Object[] pointNew;

        if (point == null) {
            pointNew = new Object[]{height, BigDecimal.ZERO, height, stakeKoeff(null), null};
        } else if (height.equals(point[2])) {
            // не пересчитываем - только коэффициент
            pointNew = new Object[]{point[0], point[1], height, stakeKoeff((Tuple2<Integer, BigDecimal>) point[4]), point[4]};
        } else {
            // расчет новой ожидающей награды у получателя и обновление даты для нового начала отсчета потом
            BigDecimal pendingReward = (BigDecimal) point[1];
            Integer pendingRewardHeight = (Integer) point[2];
            BigDecimal rewardKoeff = (BigDecimal) point[3];
            BigDecimal reward = stake.multiply(new BigDecimal(height - pendingRewardHeight)).multiply(rewardKoeff)
                    .multiply(STAKE_PERIOD_KOEFF).setScale(ASSET_DECIMALS, RoundingMode.HALF_DOWN);

            BigDecimal pendingRewardNew = pendingReward.add(reward);

            if (ASSET_QUALITY > 0) {
                BigDecimal totalLeft = MAKER.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN).b;
                if (totalLeft.compareTo(pendingRewardNew) < 0) {
                    pendingRewardNew = totalLeft;
                }
            }

            pointNew = new Object[]{point[0], pendingRewardNew, height, stakeKoeff((Tuple2<Integer, BigDecimal>) point[4]), point[4]};
        }

        return pointNew;
    }

    /**
     * how many referrals invite that person
     *
     * @param dcSet
     * @param person
     * @return
     */
    private Tuple2<Integer, BigDecimal> calcReferrals(DCSet dcSet, Long assetKey, PersonCls person) {

        int count = 0;
        BigDecimal totalStake = BigDecimal.ZERO;
        boolean maxReached = false;

        ItemsValuesMap issuesMap = dcSet.getItemsValuesMap();
        TransactionFinalMapImpl txMap = dcSet.getTransactionFinalMap();
        PersonAddressMap personAddresses = dcSet.getPersonAddressMap();
        ItemAssetBalanceMap balancesMap = dcSet.getAssetBalanceMap();
        try (IteratorCloseable<Tuple3<Long, Byte, byte[]>> iterator = issuesMap.getIssuedPersonsIter(person.getKey(), ItemCls.PERSON_TYPE, false)) {
            Tuple3<Long, Byte, byte[]> key;
            Long dbRef;
            IssueItemRecord tx;
            Long personKey;
            TreeMap<String, Stack<Fun.Tuple3<Integer, Integer, Integer>>> addresses;
            BigDecimal referralStake;
            while (iterator.hasNext()) {
                key = iterator.next();
                dbRef = Longs.fromByteArray(issuesMap.get(key));
                tx = (IssueItemRecord) txMap.get(dbRef);
                if (tx.isWiped())
                    continue;

                personKey = tx.getKey();
                addresses = personAddresses.getItems(personKey);
                if (addresses == null || addresses.isEmpty())
                    continue;

                for (String address : addresses.keySet()) {
                    referralStake = balancesMap.get(crypto.getShortBytesFromAddress(address), assetKey).a.b;
                    totalStake = totalStake.add(referralStake);
                }

                count++;

                if (count >= MAX_REFERRALS_COUNT && totalStake.compareTo(MAX_REFERRALS_STAKE) >= 0) {
                    // MAX KOEFF REACHED
                    maxReached = true;
                    break;
                }
            }
        } catch (IOException e) {
            Long error = null;
            error++;
        }

        if (maxReached)
            status += " Referrals count: >=" + MAX_REFERRALS_COUNT + ", stake: >=" + MAX_REFERRALS_STAKE.toPlainString() + ".";
        else
            status += " Referrals count: " + count + ", stake: " + totalStake.toPlainString() + ".";

        return new Tuple2<>(count, totalStake);

    }

    private static void processReferralLevel(DCSet dcSet, int level, BigInteger referralGift, Account invitedAccount,
                                             long invitedPersonKey, boolean asOrphan,
                                             long royaltyAssetKey, int royaltyAssetScale,
                                             List<RCalculated> txCalculated, String message, long dbRef, long timestamp) {

        if (referralGift.signum() <= 0)
            return;

        String messageLevel;

        // CREATOR is PERSON
        // FIND person
        Account issuerAccount = PersonCls.getIssuer(dcSet, invitedPersonKey);
        Fun.Tuple4<Long, Integer, Integer, Integer> issuerPersonDuration = issuerAccount.getPersonDuration(dcSet);
        long issuerPersonKey;
        if (issuerPersonDuration == null) {
            // в тестовой сети возможно что каждый создает с неудостоверенного
            issuerPersonKey = -1;
        } else {
            issuerPersonKey = issuerPersonDuration.a;
        }

        if (issuerPersonKey < 0 // это возможно только для первой персоны и то если не она сама себя зарегала и в ДЕВЕЛОПЕ так что пусть там и будет
                || issuerPersonKey == invitedPersonKey // зацикливание - это возможно только в ДЕВЕЛОПЕ так что пусть там и будет
                || issuerPersonKey <= BlockChain.BONUS_STOP_PERSON_KEY
        ) {
            // break loop
            BigDecimal giftBG = new BigDecimal(referralGift, royaltyAssetScale);
            invitedAccount.changeBalance(dcSet, asOrphan, false, royaltyAssetKey,
                    giftBG, false, false, false);

            if (txCalculated != null && !asOrphan) {
                messageLevel = message + " top level";
                txCalculated.add(new RCalculated(invitedAccount, royaltyAssetKey, giftBG,
                        messageLevel, 0L, dbRef));

            }
            return;
        }

        // IS INVITER ALIVE ???
        PersonCls issuer = (PersonCls) dcSet.getItemPersonMap().get(issuerPersonKey);
        if (!issuer.isAlive(timestamp)) {
            // SKIP this LEVEL for DEAD persons
            processReferralLevel(dcSet, level, referralGift, issuerAccount, issuerPersonKey, asOrphan,
                    royaltyAssetKey, royaltyAssetScale,
                    txCalculated, message, dbRef, timestamp);
            return;
        }

        int directLevel = REFERRAL_LEVEL_DEEP - level;

        if (level > 1) {

            BigInteger giftNext = referralGift.shiftRight(REFERRAL_LEVEL_KOEFFS[directLevel]);

            BigDecimal giftBG = new BigDecimal(referralGift.subtract(giftNext), royaltyAssetScale);
            issuerAccount.changeBalance(dcSet, asOrphan, false, royaltyAssetKey, giftBG,
                    false, false, false);

            if (txCalculated != null && !asOrphan) {
                messageLevel = message + " @P:" + invitedPersonKey + " level." + (1 + directLevel);
                txCalculated.add(new RCalculated(issuerAccount, royaltyAssetKey, giftBG,
                        messageLevel, 0L, dbRef));
            }

            if (giftNext.signum() > 0) {
                processReferralLevel(dcSet, --level, giftNext, issuerAccount, issuerPersonKey, asOrphan,
                        royaltyAssetKey, royaltyAssetScale,
                        txCalculated, message, dbRef, timestamp);
            }

        } else {
            // this is END LEVEL
            // GET REST of GIFT
            BigDecimal giftBG = new BigDecimal(referralGift, royaltyAssetScale);
            issuerAccount.changeBalance(dcSet, asOrphan, false, royaltyAssetKey,
                    new BigDecimal(referralGift, royaltyAssetScale), false, false, false);

            if (txCalculated != null && !asOrphan) {
                messageLevel = message + " @P:" + invitedPersonKey + " level." + (1 + directLevel);
                txCalculated.add(new RCalculated(issuerAccount, royaltyAssetKey, giftBG,
                        messageLevel, 0L, dbRef));
            }
        }
    }

    private static Long processReferral(DCSet dcSet, int level, BigDecimal stakeReward, Account creator, boolean asOrphan,
                                        AssetCls royaltyAsset,
                                        Block block,
                                        String message, long dbRef, long timestamp) {

        if (stakeReward.signum() <= 0)
            return null;

        BigInteger referralGift = stakeReward.setScale(royaltyAsset.getScale(), BigDecimal.ROUND_DOWN).unscaledValue().shiftRight(REFERRAL_SHARE2);
        if (referralGift.signum() <= 0)
            return null;

        List<RCalculated> txCalculated = block == null ? null : block.getTXCalculated();
        long royaltyAssetKey = royaltyAsset.getKey();
        int royaltyAssetScale = royaltyAsset.getScale();

        Fun.Tuple4<Long, Integer, Integer, Integer> personDuration = creator.getPersonDuration(dcSet);
        if (personDuration == null
                || personDuration.a <= BlockChain.BONUS_STOP_PERSON_KEY) {

            // если рефералку никому не отдавать то она по сути исчезает - надо это отразить в общем балансе
            royaltyAsset.getMaker().changeBalance(dcSet, !asOrphan, false, royaltyAssetKey,
                    new BigDecimal(referralGift, royaltyAssetScale), false, false, true);

            return null;
        }

        processReferralLevel(dcSet, level, referralGift, creator, personDuration.a, asOrphan,
                royaltyAssetKey, royaltyAssetScale,
                txCalculated, message, dbRef, timestamp);

        return personDuration.a;

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
            valuePut(dcSet, senderAddress, state[0]);

            // RESTORE OLD POINT
            valuePut(dcSet, recipientAddress, state[1]);

            BigDecimal stakeReward = (BigDecimal) state[2];
            if (stakeReward != null && stakeReward.signum() > 0) {
                transfer(dcSet, block, rSend, stock, sender, stakeReward, rSend.getAssetKey(), true, null, null);

                // ORPHAN REFERRALS
                AssetCls asset = rSend.getAsset();
                processReferral(dcSet, REFERRAL_LEVEL_DEEP, stakeReward, sender, asOrphan,
                        asset, null, null, rSend.getDBRef(), rSend.getTimestamp());

            }

        } else {

            status = "";

            Long assetKey = rSend.getAssetKey();
            Long refDB = rSend.getDBRef();
            Integer height = rSend.getBlockHeight();

            BigDecimal stakeReward;

            Object[] recipientPoint;
            if (recipient.equals(adminAddress)) {
                recipientPoint = null;
                status += " Ignore recipient sender.";

            } else {
                /////////// RECIPIENT REWARDS
                BigDecimal stake = recipient.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN).b;
                recipientPoint = (Object[]) valueGet(dcSet, recipientAddress);

                Object[] pointNew = makeNewPoint(assetKey, refDB, height, recipient, stake, recipientPoint);
                status += " Receiver reward: " + ((BigDecimal) pointNew[1]).toPlainString() + ".";

                // STORE NEW POINT
                valuePut(dcSet, recipientAddress, pointNew);
            }

            Object[] senderPoint;

            if (sender.equals(adminAddress)) {
                senderPoint = null;
                stakeReward = null;
                status += " Ignore admin sender.";

            } else {
                /////////// SENDER REWARDS
                BigDecimal stake = sender.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN).b;
                senderPoint = (Object[]) valueGet(dcSet, senderAddress);

                Object[] pointNew = makeNewPoint(assetKey, refDB, height, sender, stake, senderPoint);
                status += " Sender reward " + ((BigDecimal) pointNew[1]).toPlainString() + ".";

                int lastHeightAction = (Integer) pointNew[0];
                if (height - lastHeightAction >= SKIP) {
                    stakeReward = (BigDecimal) pointNew[1];
                    transfer(dcSet, block, rSend, stock, sender, stakeReward, assetKey, false, null, ASSET_NAME + " stake reward");

                    // PROCESS REFERRALS
                    AssetCls asset = rSend.getAsset();
                    Long personKey = processReferral(dcSet, REFERRAL_LEVEL_DEEP, stakeReward, sender, asOrphan,
                            asset, block,
                            ASSET_NAME + " referral bonus " + "@" + rSend.viewHeightSeq(),
                            rSend.getDBRef(), rSend.getTimestamp());

                    // CALC new REFERRALS COUNT
                    if (personKey != null) {
                        PersonCls person = (PersonCls) dcSet.getItemPersonMap().get(personKey);
                        if (person.isAlive(block == null ? rSend.getTimestamp() : block.getTimestamp()) // block may be NULL on test unconfirmed
                        ) {
                            Tuple2<Integer, BigDecimal> referrals = calcReferrals(dcSet, assetKey, person);
                            BigDecimal newKoeff = stakeKoeff(referrals);
                            status += " New Koeff: " + newKoeff.toPlainString() + ".";
                            pointNew[3] = newKoeff;
                            pointNew[4] = referrals;
                        }
                    }

                    // reset pending reward
                    pointNew[0] = height;
                    pointNew[1] = BigDecimal.ZERO;
                    status += " Withdraw.";
                } else {
                    stakeReward = null;
                }


                // STORE NEW POINT
                valuePut(dcSet, senderAddress, pointNew);

            }

            // STORE STATE for ORPHAN
            putState(dcSet, rSend.getDBRef(), new Object[]{senderPoint, recipientPoint, stakeReward});

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

            if (ASSET_QUALITY > 0) {
                // ORPHAN QUANTITY
                stock.changeBalance(dcSet, true, false, assetKey,
                        new BigDecimal(ASSET_QUALITY), false, false, true);
            }

            // ORPHAN for ADMIN
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

            if (ASSET_QUALITY > 0) {
                // INIT QUANTITY
                stock.changeBalance(dcSet, false, false, assetKey,
                        new BigDecimal(ASSET_QUALITY), false, false, true);
            }

            // TRANSFER ASSET to ADMIN
            transfer(dcSet, block, commandTX, stock, adminAccount, amount, assetKey, false, null, "init");

            status = "done";
        }

        return true;
    }

    @Override
    public boolean processByTime(DCSet dcSet, Block block, Transaction transaction) {
        fail("unknown command");
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

        fail("unknown command");
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
