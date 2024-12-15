package org.erachain.dapp.epoch;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.IssueItemRecord;
import org.erachain.core.transaction.RCalculated;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.EpochDAPPjson;
import org.erachain.datachain.*;
import org.erachain.dbs.IteratorCloseable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Описание тут https://docs.google.com/document/d/1I0AscIkvACigv-aViHf3uNcc1LXXNm3dd4pNd-w-xfE/edit#heading=h.b6s2qmv4aszg
 * Это стейкинг с рефералкой, причем % растет от числа приглашенных и суммы на их счетах
 */
public class MoneyStaking extends EpochDAPPjson {


    // TODO увеличить комиисию с таких трнзакций - от периода стейка но не более? И используется ли подсчет рефералов?
    static public final int ID = 1012;
    static public final String NAME = "Money Staking";
    static public final boolean DISABLED = false;
    static public final String SHORT = "Get reward by your deposit and by your Referrals";
    static public final String DESC = "This is financial Decentralized Application (smart-contract). It give a Reward to You by your deposit and by your Referrals";

    // APPBjF5fbGj18aaXKSXemmHConG7JLBiJg
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

    /**
     * Число блоков между последней обработкой и текущей - чтобы часто не пересчитывать и не спамить
     */
    static final int SKIP_BLOCKS = BlockChain.TEST_MODE ? BlockChain.DEMO_MODE ? 100 : 1000 : 10;

    /**
     * For calculate reward - amount of blocks in one year
     */
    static final BigDecimal STAKE_PERIOD_MULTI = new BigDecimal(1.0d).divide(new BigDecimal(30.0d * 24.0d * 120.0d * 3600), 10, RoundingMode.HALF_DOWN);

    static final String CALC_STAKE_KEY = "CALC_STAKE";
    /**
     * Множитель доходности STAKE_MULTI[[Total Referals, Total referals Stake, Reward multiplier as a Percentage per year], ...] -
     * "STAKE_MULTI":[[5, "150.0", "2.5"], [20, "1500", 5], [50, 15000, "7.5"]] - 3 уровня наград у каждого свой множитель награды, по числу приглашенных перефералов и общей сумме актива на их счетах. Рефералы учитываются только ваши личные (без подсчета в глубь)
     * Если заданы уровни по рефералам то нагрузка больше - и комиссия тоже
     */
    static final String STAKE_MULTI_BY_REFERALS_KEY = "STAKE_MULTI_BY_REFERALS";
    static final String STAKE_MULTI_KEY = "STAKE_MULTI";
    /**
     * Сдвиг по модулю 2 дохода от процентам пользователю - какая доля идет в рефералку - сдвиг на 3 по умолчанию - это одна восьмая. Эта доля эмитируется контрактом вдобавок к доходу от процентов.
     */
    public static final String REFERRAL_SHARE_2_KEY = "REFERRAL_SHARE_2";
    /**
     * Максимальное число приглашенных, которое учитывается для расчета множителя доходности
     */
    public static final String MAX_REFERRALS_COUNT_KEY = "MAX_REFERRALS_COUNT";
    /**
     * Максимальная сумма актива на всех счетах приглашенных, которое учитывается для расчета множителя доходности
     */
    public static final String MAX_REFERRALS_STAKE_KEY = "MAX_REFERRALS_STAKE";

    public MoneyStaking() {
        super(ID, MAKER, null, null);
    }

    public MoneyStaking(String dataStr, String status) {
        super(ID, MAKER, dataStr, status);
    }

    public MoneyStaking(String dataStr, JSONObject values) {
        super(ID, MAKER, dataStr, values, "");
    }

    @Override
    public MoneyStaking of(String dataStr, JSONObject values) {
        return new MoneyStaking(dataStr, values);
    }

    public static MoneyStaking initInfo(HashMap<Account, Integer> stocks) {
        MoneyStaking dapp = new MoneyStaking();
        stocks.put(MAKER, ID);
        return dapp;
    }

    public String getName() {
        return NAME;
    }

    public boolean isDisabled() {
        return DISABLED;
    }

    /**
     * Вычисляем множитель для награды от Рефералов и общего количества актива на руках у них
     *
     * @param referrals
     * @return
     */
    private BigDecimal stakeMulti(Tuple2<Integer, BigDecimal> referrals) {

        // Если задан постоянный множитель
        if (values.containsKey(STAKE_MULTI_KEY))
            return new BigDecimal(values.get(STAKE_MULTI_KEY).toString());

        // определим множитель для пустой точки
        if (referrals == null) {
            return new BigDecimal(((JSONArray) values.get(STAKE_MULTI_BY_REFERALS_KEY)).get(0).toString());
        }

        // TODO
        JSONArray stakesMulti = (JSONArray) values.get(STAKE_MULTI_BY_REFERALS_KEY);

        stakesMulti.iterator();

        return null;

    }

    /**
     * point[height of withdraw, current reward, Timestamp of calculating rewards Block (ms), multi, total referrals and stakes]
     *
     * @param assetKey
     * @param blockTimestamp
     * @param height
     * @param account
     * @param stake
     * @param point
     * @return
     */
    private Object[] makeNewPoint(RSend rSend, Long assetKey, Long blockTimestamp, Integer height,
                                  Account account, BigDecimal stake, Object[] point) {

        Object[] pointNew;

        if (point == null) {
            pointNew = new Object[]{height, BigDecimal.ZERO, blockTimestamp, stakeMulti(null), null};
        } else if (blockTimestamp.equals(point[2])) {
            // не пересчитываем - только множитель
            pointNew = new Object[]{point[0], point[1], blockTimestamp, stakeMulti((Tuple2<Integer, BigDecimal>) point[4]), point[4]};
        } else {
            // расчет новой ожидающей награды у получателя и обновление даты для нового начала отсчета потом
            BigDecimal pendingReward = (BigDecimal) point[1];
            Long pendingRewardTimestamp = (Long) point[2];
            BigDecimal rewardMulti = (BigDecimal) point[3];

            BigDecimal reward = stake.multiply(new BigDecimal((blockTimestamp - pendingRewardTimestamp) / 1000)).multiply(rewardMulti)
                    .multiply(STAKE_PERIOD_MULTI).setScale(rSend.getAsset().getScale(), RoundingMode.HALF_DOWN);

            BigDecimal pendingRewardNew = pendingReward.add(reward);

            int ASSET_QUALITY = (Integer) values.get("ASSET_QUALITY");
            if (ASSET_QUALITY > 0) {
                BigDecimal totalLeft = MAKER.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN).b;
                if (totalLeft.compareTo(pendingRewardNew) < 0) {
                    pendingRewardNew = totalLeft;
                }
            }

            pointNew = new Object[]{point[0], pendingRewardNew, blockTimestamp, stakeMulti((Tuple2<Integer, BigDecimal>) point[4]), point[4]};
        }

        return pointNew;
    }

    /**
     * how many referrals invite that person.
     * Учитывает число приглашенных первого уровня и сумму актива на всех из счетах
     *
     * @param dcSet
     * @param person
     * @param calcStake
     * @return
     */
    private Tuple2<Integer, BigDecimal> calcReferrals(DCSet dcSet, Long assetKey, PersonCls person, boolean calcStake) {

        BigDecimal MAX_REFERRALS_STAKE = (BigDecimal) values.get(MAX_REFERRALS_STAKE_KEY);
        int MAX_REFERRALS_COUNT = (Integer) values.getOrDefault(MAX_REFERRALS_COUNT_KEY, 100);
        if (MAX_REFERRALS_COUNT > 100) MAX_REFERRALS_COUNT = 100;

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

                if (calcStake)
                    for (String address : addresses.keySet()) {
                        referralStake = balancesMap.get(crypto.getShortBytesFromAddress(address), assetKey).a.b;
                        totalStake = totalStake.add(referralStake);
                    }

                count++;

                if (count >= MAX_REFERRALS_COUNT && (MAX_REFERRALS_STAKE == null || totalStake.compareTo(MAX_REFERRALS_STAKE) >= 0)) {
                    // MAX MULTI REACHED
                    maxReached = true;
                    break;
                }
            }
        } catch (IOException e) {
            Long error = null;
            error++;
        }

        if (maxReached)
            status += " Referrals count: >=" + MAX_REFERRALS_COUNT + (MAX_REFERRALS_STAKE == null ? "" : ", stake: >=" + MAX_REFERRALS_STAKE.toPlainString()) + ".";
        else
            status += " Referrals count: " + count + ", stake: " + totalStake.toPlainString() + ".";

        return new Tuple2<>(count, totalStake);

    }

    private static void processReferralLevel(int MAX_LEVEL, DCSet dcSet, int level, BigInteger referralGift, Account invitedAccount,
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
            processReferralLevel(MAX_LEVEL, dcSet, level, referralGift, issuerAccount, issuerPersonKey, asOrphan,
                    royaltyAssetKey, royaltyAssetScale,
                    txCalculated, message, dbRef, timestamp);
            return;
        }

        int directLevel = MAX_LEVEL - level;

        if (level > 1) {

            BigInteger giftNext = referralGift.shiftRight(1);

            BigDecimal giftBG = new BigDecimal(referralGift.subtract(giftNext), royaltyAssetScale);
            issuerAccount.changeBalance(dcSet, asOrphan, false, royaltyAssetKey, giftBG,
                    false, false, false);

            if (txCalculated != null && !asOrphan) {
                messageLevel = message + " @P:" + invitedPersonKey + " level." + (1 + directLevel);
                txCalculated.add(new RCalculated(issuerAccount, royaltyAssetKey, giftBG,
                        messageLevel, 0L, dbRef));
            }

            if (giftNext.signum() > 0) {
                processReferralLevel(MAX_LEVEL, dcSet, --level, giftNext, issuerAccount, issuerPersonKey, asOrphan,
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

    private static Long processReferral(DCSet dcSet, int level, int REFERRAL_SHARE_2, BigDecimal stakeReward, Account creator, boolean asOrphan,
                                        AssetCls royaltyAsset,
                                        Block block,
                                        String message, long dbRef, long timestamp) {

        if (stakeReward.signum() <= 0)
            return null;

        BigInteger referralGift = stakeReward.setScale(royaltyAsset.getScale(), BigDecimal.ROUND_DOWN).unscaledValue().shiftRight(REFERRAL_SHARE_2);
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

        processReferralLevel(level, dcSet, level, referralGift, creator, personDuration.a, asOrphan,
                royaltyAssetKey, royaltyAssetScale,
                txCalculated, message, dbRef, timestamp);

        return personDuration.a;

    }

    ///////// COMMANDS
    private boolean job(DCSet dcSet, Block block, RSend rSend, boolean asOrphan) {

        int REFERRAL_SHARE_2 = (Integer) values.getOrDefault(REFERRAL_SHARE_2_KEY, 3);
        boolean calcStake = (Boolean) values.getOrDefault(CALC_STAKE_KEY, Boolean.FALSE);
        JSONArray levelsMilti = (JSONArray) values.get(STAKE_MULTI_BY_REFERALS_KEY);
        int REFERRAL_LEVEL_DEEP = levelsMilti.size();

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
                processReferral(dcSet, REFERRAL_LEVEL_DEEP, REFERRAL_SHARE_2, stakeReward, sender, asOrphan,
                        asset, null, null, rSend.getDBRef(), rSend.getTimestamp());

            }

        } else {

            status = "";

            Long assetKey = rSend.getAssetKey();
            Long blockTimestamp = block.getTimestamp();
            Integer height = rSend.getBlockHeight();

            BigDecimal stakeReward;

            Object[] recipientPoint;
            if (recipient.equals(rSend.getAsset().getMaker())) {
                recipientPoint = null;
                status += " Ignore recipient sender.";

            } else {
                /////////// RECIPIENT REWARDS
                BigDecimal stake = recipient.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN).b;
                recipientPoint = (Object[]) valueGet(dcSet, recipientAddress);

                Object[] pointNew = makeNewPoint(rSend, assetKey, blockTimestamp, height, recipient, stake, recipientPoint);
                status += " Receiver reward: " + ((BigDecimal) pointNew[1]).toPlainString() + ".";

                // STORE NEW POINT
                valuePut(dcSet, recipientAddress, pointNew);
            }

            Object[] senderPoint;

            if (sender.equals(rSend.getAsset().getMaker())) {
                senderPoint = null;
                stakeReward = null;
                status += " Ignore admin sender.";

            } else {
                /////////// SENDER REWARDS
                BigDecimal stake = sender.getBalanceForPosition(assetKey, Account.BALANCE_POS_OWN).b;
                senderPoint = (Object[]) valueGet(dcSet, senderAddress);

                Object[] pointNew = makeNewPoint(rSend, assetKey, blockTimestamp, height, sender, stake, senderPoint);
                status += " Sender reward " + ((BigDecimal) pointNew[1]).toPlainString() + ".";

                int lastHeightAction = (Integer) pointNew[0];
                if (height - lastHeightAction >= SKIP_BLOCKS) {
                    stakeReward = (BigDecimal) pointNew[1];
                    transfer(dcSet, block, rSend, stock, sender, stakeReward, assetKey, false, null, "REFI Stake reward");

                    // PROCESS REFERRALS
                    AssetCls asset = rSend.getAsset();
                    Long personKey = processReferral(dcSet, REFERRAL_LEVEL_DEEP, REFERRAL_SHARE_2, stakeReward, sender, asOrphan,
                            asset, block,
                            "REFI referral bonus " + "@" + rSend.viewHeightSeq(),
                            rSend.getDBRef(), rSend.getTimestamp());

                    // CALC new REFERRALS COUNT
                    if (personKey != null) {
                        PersonCls person = (PersonCls) dcSet.getItemPersonMap().get(personKey);
                        if (person.isAlive(block == null ? rSend.getTimestamp() : block.getTimestamp()) // block may be NULL on test unconfirmed
                        ) {
                            // Нет рефеларных множителей - пропустим подсчет сумм по рефералке
                            Tuple2<Integer, BigDecimal> referrals = values.containsKey(STAKE_MULTI_KEY) ? new Tuple2(null, null) : calcReferrals(dcSet, assetKey, person, calcStake);
                            BigDecimal newMulti = stakeMulti(referrals);
                            status += " New Multi: " + newMulti.toPlainString() + ".";
                            pointNew[3] = newMulti;
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

    @Override
    public boolean processByTime(DCSet dcSet, Block block, Transaction transaction) {
        fail("unknown command");
        return false;
    }

    @Override
    public boolean process(DCSet dcSet, Block block, Transaction commandTX) {
        return job(dcSet, block, (RSend) commandTX, false);
    }

    @Override
    public void orphanByTime(DCSet dcSet, Block block, Transaction transaction) {
    }

    @Override
    public void orphanBody(DCSet dcSet, Transaction commandTX) {
        job(dcSet, null, (RSend) commandTX, true);
    }

    public static MoneyStaking make(RSend txSend, String dataStr) {
        return new MoneyStaking(dataStr, "");
    }

    /// PARSE / TOBYTES

    public static MoneyStaking Parse(byte[] bytes, int pos, int forDeal) {

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

        return new MoneyStaking(data, status);
    }

    public String getCommandInfo(String command, String format) {
        return format;
    }

}
