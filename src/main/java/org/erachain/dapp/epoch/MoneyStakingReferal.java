package org.erachain.dapp.epoch;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.*;
import org.erachain.dapp.DApp;
import org.erachain.dapp.DAppFactory;
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

import static org.erachain.core.account.Account.BALANCE_POS_OWN;

/**
 * Описание тут https://docs.google.com/document/d/1I0AscIkvACigv-aViHf3uNcc1LXXNm3dd4pNd-w-xfE/edit#heading=h.b6s2qmv4aszg
 * Это стейкинг с рефералкой, причем % растет от числа приглашенных и суммы на их счетах
 * Пересчет коэффициентов и для получателя (но не начисление %%), так как баланс у него меняется - поэтому пересчет
 * {"id":1012, "annualPercentage":"120"}
 */
public class MoneyStakingReferal extends EpochDAppItemJson {


    // TODO увеличить комиссию с таких транзакций - от периода стейка но не более? И используется ли подсчет рефералов?
    static public final int ID = 1022;
    static public final String NAME = "Staking REFI";
    static public final boolean DISABLED = true;

    // Формальный счёт админа - он не имеет никакой силы и не исполняет команды
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

    /**
     * Число блоков между последней обработкой и текущей - чтобы часто не пересчитывать и не спамить
     */
    static final int SKIP_BLOCKS = BlockChain.TEST_MODE ? BlockChain.DEMO_MODE ? 10 : 3 : 100;

    /**
     * For calculate reward - amount of blocks in one year
     */
    static final BigDecimal STAKE_PERIOD_MULTI = BigDecimal.ONE.divide(new BigDecimal(30L * 24L * 120L * 3600L), 10, RoundingMode.HALF_DOWN);

    /**
     * Постоянный множитель доходности annualPercentage - Reward multiplier as a Percentage per year.
     * Пример: {"id":1012, "annualPercentage":"2.5"}
     * !!! Внимание - если не задано, то будет использовано referalLevels
     */
    static final String STAKE_MULTI_KEY = "annualPercentage";
    /**
     * Множитель доходности с уровнями по рефералам и их депозитам
     * referalLevels[[Total Referals, Total referals Stake, Reward multiplier as a Percentage per year], ...] -
     * "referalLevels":[[5, "150.0", "2.5"], [20, "1500", 5], [50, 15000, "7.5"]] - 3 уровня наград у каждого свой множитель награды,
     * по числу приглашенных рефералов и общей сумме актива на их счетах. Рефералы учитываются только ваши личные (без подсчета в глубь)
     * Если заданы уровни по рефералам то нагрузка на расчеты больше - и комиссия за транзакции тоже.
     * !!! Внимание - должен быть задан хотя бы один уровень!
     * referalLevels используется если не задан annualPercentage
     */
    static final String STAKE_MULTI_BY_REFERALS_KEY = "referalLevels";
    /**
     * Учитывать ли размер депозитов у рефералов
     */
    static final String CALC_STAKE_KEY = "calcReferalsStake";
    /**
     * Сдвиг по модулю 2 дохода от процентам пользователю - какая доля идет в рефералку
     * - сдвиг на 3 по умолчанию - это одна восьмая. Эта доля эмитируется контрактом вдобавок к доходу от процентов.
     */
    public static final String REFERRAL_SHIFT_2_KEY = "levelShift";
    /**
     * Максимальное число приглашенных, которое учитывается для расчета множителя доходности
     */
    public static final String MAX_REFERRALS_COUNT_KEY = "maxReferalsCount";
    /**
     * Максимальная сумма актива на всех счетах приглашенных, которое учитывается для расчета множителя доходности
     */
    public static final String MAX_REFERRALS_STAKE_KEY = "maxReferaksStake";

    private MoneyStakingReferal() {
        super(ID, MAKER);
    }

    public MoneyStakingReferal(int itemType, long itemKey, String itemDescription, String dataStr, String status) {
        super(ID, MAKER, itemType, itemKey, itemDescription, dataStr, status);
    }

    public MoneyStakingReferal(ItemCls item, String itemDescription, JSONObject itemPars, Transaction commandTx, Block block) {
        super(ID, MAKER, item, itemDescription, itemPars, commandTx, block);
    }

    @Override
    public DApp of(String itemDescription, JSONObject itemPars, ItemCls item, Transaction commandTx, Block block) {
        if (commandTx instanceof TransferredBalances)
            return new MoneyStakingReferal(item, itemDescription, itemPars, commandTx, block);
        return null;
    }

    @Override
    public DApp of(String dataStr, Transaction commandTx, Block block) {
        throw new RuntimeException("Wrong OF(...)");
    }

    public static void setDAppFactory() {
        DApp instance = new MoneyStakingReferal();
        DAppFactory.STOCKS.put(MAKER, instance);
        DAppFactory.DAPP_BY_ID.put(ID, instance);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isDisabled(int height) {
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

        // определим множитель для пустой точки - начальный уровень
        if (referrals == null) {
            return new BigDecimal(((JSONArray) values.get(STAKE_MULTI_BY_REFERALS_KEY)).get(0).toString());
        }

        JSONArray stakesMulti = (JSONArray) values.get(STAKE_MULTI_BY_REFERALS_KEY);
        Iterator iterator = stakesMulti.iterator();
        while (iterator.hasNext()) {
            // TODO организовать выбор уровня
        }

        return null;

    }

    /**
     * point[height of withdraw, current reward, Timestamp of calculating rewards Block (ms), multi, total referrals and stakes]
     *
     * @param assetKey
     * @param account
     * @param stake
     * @param point
     * @return
     */
    private Object[] makeNewPoint(Long assetKey,
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
                    .multiply(STAKE_PERIOD_MULTI).setScale(commandTx.getAsset().getScale(), RoundingMode.HALF_DOWN);

            BigDecimal pendingRewardNew = pendingReward.add(reward);

            int ASSET_QUALITY = (Integer) values.get("ASSET_QUALITY");
            if (ASSET_QUALITY > 0) {
                BigDecimal totalLeft = MAKER.getBalanceForPosition(assetKey, BALANCE_POS_OWN).b;
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
     * @param person
     * @return
     */
    private Tuple2<Integer, BigDecimal> calcReferrals(Long assetKey, PersonCls person) {

        boolean calcStake = (Boolean) values.getOrDefault(CALC_STAKE_KEY, Boolean.FALSE);
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
            TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses;
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
    private boolean job(boolean asOrphan) {

        RSend rSend = (RSend) commandTx;
        if (rSend.balancePosition() != BALANCE_POS_OWN)
            return true;

        PublicKeyAccount sender = commandTx.getCreator();
        String senderAddress = sender.getAddress();

        Account recipient = rSend.getRecipient();
        String recipientAddress = recipient.getAddress();

        if (asOrphan) {
            Object[] state = removeState(commandTx.getDBRef());

            // RESTORE OLD POINT
            valuePut(senderAddress, state[0]);

            // RESTORE OLD POINT
            valuePut(recipientAddress, state[1]);

            BigDecimal stakeReward = (BigDecimal) state[2];
            if (stakeReward != null && stakeReward.signum() > 0) {
                transfer(dcSet, block, commandTx, stock, sender, stakeReward, commandTx.getAssetKey(), true, null, null);

                int REFERRAL_SHARE_2 = (Integer) values.getOrDefault(REFERRAL_SHIFT_2_KEY, 3);
                JSONArray levelsMilti = (JSONArray) values.get(STAKE_MULTI_BY_REFERALS_KEY);
                int REFERRAL_LEVEL_DEEP = levelsMilti.size();

                // ORPHAN REFERRALS
                AssetCls asset = commandTx.getAsset();
                processReferral(dcSet, REFERRAL_LEVEL_DEEP, REFERRAL_SHARE_2, stakeReward, sender, asOrphan,
                        asset, null, null, commandTx.getDBRef(), commandTx.getTimestamp());

            }

        } else {

            status = "";

            Long assetKey = commandTx.getAssetKey();
            Long blockTimestamp = block.getTimestamp();
            Integer height = commandTx.getBlockHeight();

            BigDecimal stakeReward;

            Object[] recipientPoint;
            if (recipient.equals(commandTx.getAsset().getMaker())) {
                recipientPoint = null;
                status += " Ignore recipient sender.";

            } else {
                /////////// RECIPIENT REWARDS
                BigDecimal stake = recipient.getBalanceForPosition(assetKey, BALANCE_POS_OWN).b;
                recipientPoint = (Object[]) valueGet(recipientAddress);

                Object[] pointNew = makeNewPoint(assetKey, recipient, stake, recipientPoint);
                status += " Receiver reward: " + ((BigDecimal) pointNew[1]).toPlainString() + ".";

                // STORE NEW POINT
                valuePut(recipientAddress, pointNew);
            }

            Object[] senderPoint;

            if (sender.equals(commandTx.getAsset().getMaker())) {
                senderPoint = null;
                stakeReward = null;
                status += " Ignore admin sender.";

            } else {
                /////////// SENDER REWARDS
                BigDecimal stake = sender.getBalanceForPosition(assetKey, BALANCE_POS_OWN).b;
                senderPoint = (Object[]) valueGet(senderAddress);

                Object[] pointNew = makeNewPoint(assetKey, sender, stake, senderPoint);
                status += " Sender reward " + ((BigDecimal) pointNew[1]).toPlainString() + ".";

                int lastHeightAction = (Integer) pointNew[0];
                if (height - lastHeightAction >= SKIP_BLOCKS) {
                    stakeReward = (BigDecimal) pointNew[1];
                    transfer(dcSet, block, commandTx, stock, sender, stakeReward, assetKey, false, null, "Smart Stake reward");

                    // PROCESS REFERRALS
                    AssetCls asset = commandTx.getAsset();

                    if (!values.containsKey(STAKE_MULTI_KEY)) {
                        int REFERRAL_SHARE_2 = (Integer) values.getOrDefault(REFERRAL_SHIFT_2_KEY, 3);
                        JSONArray levelsMilti = (JSONArray) values.get(STAKE_MULTI_BY_REFERALS_KEY);
                        int REFERRAL_LEVEL_DEEP = levelsMilti.size();

                        Long personKey = processReferral(dcSet, REFERRAL_LEVEL_DEEP, REFERRAL_SHARE_2, stakeReward, sender, asOrphan,
                                asset, block,
                                "Smart Stake referral bonus " + "@" + commandTx.viewHeightSeq(),
                                commandTx.getDBRef(), commandTx.getTimestamp());

                        // CALC new REFERRALS COUNT
                        if (personKey != null) {
                            PersonCls person = (PersonCls) dcSet.getItemPersonMap().get(personKey);
                            if (person.isAlive(block == null ? commandTx.getTimestamp() : block.getTimestamp()) // block may be NULL on test unconfirmed
                            ) {
                                // Нет рефеларных множителей - пропустим подсчет сумм по рефералке
                                Tuple2<Integer, BigDecimal> referrals = values.containsKey(STAKE_MULTI_KEY) ? new Tuple2(null, null) : calcReferrals(assetKey, person);
                                BigDecimal newMulti = stakeMulti(referrals);
                                status += " New Multi: " + newMulti.toPlainString() + ".";
                                pointNew[3] = newMulti;
                                pointNew[4] = referrals;
                            }
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
                valuePut(senderAddress, pointNew);

            }

            // STORE STATE for ORPHAN
            putState(commandTx.getDBRef(), new Object[]{senderPoint, recipientPoint, stakeReward});

        }

        return true;

    }

    @Override
    public boolean processByTime() {
        fail("unknown command");
        return false;
    }

    @Override
    public boolean process() {
        if (block == null) {
            // Это еще неподтвержденная - нечего исполнять или не Послать
            return true;
        }
        return job(false);
    }

    @Override
    public void orphanByTime() {
    }

    @Override
    public void orphanBody() {
        job(true);
    }

    /// PARSE / TOBYTES

    public static MoneyStakingReferal Parse(byte[] bytes, int pos, int forDeal) {

        // skip ID
        pos += 4;

        byte[] itemTypeBytes = Arrays.copyOfRange(bytes, pos, pos + 2);
        int itemType = Shorts.fromByteArray(itemTypeBytes);
        pos += 2;

        byte[] itemKeyBytes = Arrays.copyOfRange(bytes, pos, pos + 8);
        int itemKey = Ints.fromByteArray(itemKeyBytes);
        pos += 8;


        String data;
        String status;
        String itemDesc;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] statusSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int statusLen = Ints.fromByteArray(statusSizeBytes);
            pos += 4;
            byte[] statusBytes = Arrays.copyOfRange(bytes, pos, pos + statusLen);
            pos += statusLen;
            status = new String(statusBytes, StandardCharsets.UTF_8);

            byte[] dataSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            pos += 4;
            byte[] dataBytes = Arrays.copyOfRange(bytes, pos, pos + dataSize);
            pos += dataSize;
            data = new String(dataBytes, StandardCharsets.UTF_8);

            byte[] itemDescSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int itemDescLen = Ints.fromByteArray(itemDescSizeBytes);
            pos += 4;
            byte[] itemDescBytes = Arrays.copyOfRange(bytes, pos, pos + itemDescLen);
            pos += itemDescLen;
            itemDesc = new String(itemDescBytes, StandardCharsets.UTF_8);

        } else {
            data = "";
            status = "";
            itemDesc = "";
        }

        return new MoneyStakingReferal(itemType, itemKey, itemDesc, data, status);
    }

    public String getCommandInfo(String command, String format) {
        return format;
    }

}
