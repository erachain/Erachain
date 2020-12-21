package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.datachain.TransactionFinalMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * StandardCharsets.UTF_8 JSON "TM" - template key "PR" - template params
 * "HS" - Hashes "MS" - message
 * <p>
 * PARAMS template:TemplateCls param_keys: [id:text] hashes_Set: [name:hash]
 * mess: message title: Title file_Set: [file Name, ZIP? , file byte[]]
 */

public class ExPays {

    public static final byte BASE_LENGTH = 4 + 2;

    public static final int MAX_COUNT = Integer.MAX_VALUE >> 1;
    private static final byte AMOUNT_FLAG_MASK = 64;
    private static final byte BALANCE_FLAG_MASK = 32;
    private static final byte TXTYPE_FLAG_MASK = 16;

    private static final byte PAYMENT_METHOD_TOTAL = 1; // by TOTAL
    private static final byte PAYMENT_METHOD_COEFF = 2; // by coefficient

    private static final byte NOT_FILTER_PERSONS = -1; //
    private static final byte NOT_FILTER_GENDER = -2; //

    private static final Logger LOGGER = LoggerFactory.getLogger(ExPays.class);

    /**
     * 0 - version; 1..3 - flags;
     */
    private int flags; // 4

    private Long assetKey; // 12
    private int actionType; // 13
    private boolean backward; // 14
    private int payMethod; // 15 0 - by Total, 1 - by Percent
    private BigDecimal payMethodValue; // 17
    private BigDecimal amountMin; // 19
    private BigDecimal amountMax; //21

    private Long filterAssetKey; // 29
    private int filterBalancePos; //30
    private int filterBalanceSide; //31
    private BigDecimal filterBalanceLessThen; // 33
    private BigDecimal filterBalanceMoreThen; // 35

    private Integer filterTXType; // 36
    private Long filterTXStartSeqNo; // 44
    public Long filterTXEndSeqNo; // 52

    private final Integer filterByGender; // 53 = gender or all
    public boolean selfPay; // 54

    /////////////////
    DCSet dcSet;
    private int height;
    AssetCls asset;
    public List<Fun.Tuple3> filteredPayouts;
    public int filteredPayoutsCount;
    public BigDecimal totalPay;
    public BigDecimal totalFee;
    public String errorValue;


    public ExPays(int flags, Long assetKey, int actionType, boolean backward, BigDecimal amountMin, BigDecimal amountMax,
                  int payMethod, BigDecimal payMethodValue, Long filterAssetKey, int filterBalancePos, int filterBalanceSide,
                  BigDecimal filterBalanceLessThen, BigDecimal filterBalanceMoreThen,
                  Integer filterTXType, Long filterTXStartSeqNo, Long filterTXEndSeqNo,
                  Integer filterByGender, boolean selfPay) {
        this.flags = flags;

        if (assetKey != null && assetKey != 0) {
            this.flags |= AMOUNT_FLAG_MASK;
            this.assetKey = assetKey;
            this.actionType = actionType;
            this.backward = backward;
            this.amountMin = amountMin;
            this.amountMax = amountMax;
            this.payMethod = payMethod;
            this.payMethodValue = payMethodValue;
        }


        if (filterAssetKey != null && filterAssetKey != 0) {
            this.flags |= BALANCE_FLAG_MASK;
            this.filterAssetKey = filterAssetKey;
            this.filterBalancePos = filterBalancePos;
            this.filterBalanceSide = filterBalanceSide;
            this.filterBalanceLessThen = filterBalanceLessThen;
            this.filterBalanceMoreThen = filterBalanceMoreThen;
        }

        if (filterTXType != null && filterTXType != 0) {
            this.flags |= TXTYPE_FLAG_MASK;
            this.filterTXType = filterTXType;
            this.filterTXStartSeqNo = filterTXStartSeqNo;
            this.filterTXEndSeqNo = filterTXEndSeqNo;
        }

        this.filterByGender = filterByGender;
        this.selfPay = selfPay;
    }

    public boolean hasAmount() {
        return (this.flags & AMOUNT_FLAG_MASK) != 0;
    }

    public boolean hasAssetFilter() {
        return (this.flags & BALANCE_FLAG_MASK) != 0;
    }

    public boolean hasTXTypeFilter() {
        return (this.flags & TXTYPE_FLAG_MASK) != 0;
    }

    public void setDC(DCSet dcSet) {
        this.dcSet = dcSet;
        if (hasAmount() && this.asset == null) {
            this.asset = this.dcSet.getItemAssetMap().get(this.assetKey);
        }
    }

    public byte[] toByte() throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        outStream.write(Ints.toByteArray(flags));

        byte[] buff;
        if (hasAmount()) {
            outStream.write(Longs.toByteArray(this.assetKey));

            buff = new byte[]{(byte) actionType, (byte) (backward ? 1 : 0), (byte) payMethod};
            outStream.write(buff);

            outStream.write(this.payMethodValue.scale());
            buff = this.payMethodValue.unscaledValue().toByteArray();
            outStream.write(buff.length);
            outStream.write(buff);

            outStream.write(this.amountMin.scale());
            buff = this.amountMin.unscaledValue().toByteArray();
            outStream.write(buff.length);
            outStream.write(buff);

            outStream.write(this.amountMax.scale());
            buff = this.amountMax.unscaledValue().toByteArray();
            outStream.write(buff.length);
            outStream.write(buff);

        }

        if (hasAssetFilter()) {
            outStream.write(Longs.toByteArray(this.filterAssetKey));
            buff = new byte[]{(byte) filterBalancePos, (byte) filterBalanceSide};
            outStream.write(buff);

            outStream.write(this.filterBalanceLessThen.scale());
            buff = this.filterBalanceLessThen.unscaledValue().toByteArray();
            outStream.write(buff.length);
            outStream.write(buff);

            outStream.write(this.filterBalanceMoreThen.scale());
            buff = this.filterBalanceMoreThen.unscaledValue().toByteArray();
            outStream.write(buff.length);
            outStream.write(buff);
        }

        if (hasTXTypeFilter()) {
            outStream.write(filterTXType);
            outStream.write(Longs.toByteArray(this.filterTXStartSeqNo));
            outStream.write(Longs.toByteArray(this.filterTXEndSeqNo));
        }

        outStream.write(new byte[]{(byte) (int) filterByGender, (byte) (selfPay ? 1 : 0)});

        return outStream.toByteArray();

    }

    public int length() {
        int len = BASE_LENGTH;

        if (hasAmount()) {
            len += Transaction.KEY_LENGTH + 3 + 6
                    + payMethodValue.unscaledValue().toByteArray().length
                    + amountMin.unscaledValue().toByteArray().length
                    + amountMax.unscaledValue().toByteArray().length;
        }

        if (hasAssetFilter()) {
            len += Transaction.KEY_LENGTH + 2 + 4
                    + filterBalanceLessThen.unscaledValue().toByteArray().length
                    + filterBalanceMoreThen.unscaledValue().toByteArray().length;
        }

        if (hasTXTypeFilter()) {
            len += 1 + 8 + 8;
        }

        return len;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ExPays parse(byte[] data, int position) throws Exception {

        int scale;
        int len;

        int flags = Ints.fromByteArray(Arrays.copyOfRange(data, position, Integer.BYTES));
        position += Integer.BYTES;

        Long assetKey = null;
        int balancePos = 0;
        boolean backward = false;
        BigDecimal amountMin = null;
        BigDecimal amountMax = null;
        int payMethod = 0;
        BigDecimal payMethodValue = null;

        if ((flags & AMOUNT_FLAG_MASK) != 0) {
            assetKey = Longs.fromByteArray(Arrays.copyOfRange(data, position, position + Long.BYTES));
            position += Long.BYTES;

            balancePos = data[position++];
            backward = data[position++] > 0;
            payMethod = data[position++];

            scale = data[position++];
            len = data[position++];
            payMethodValue = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + len)), scale);
            position += len;

            scale = data[position++];
            len = data[position++];
            amountMin = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + len)), scale);
            position += len;

            scale = data[position++];
            len = data[position++];
            amountMax = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + len)), scale);
            position += len;

        }

        Long filterAssetKey = null;
        int filterBalancePos = 0;
        int filterBalanceSide = 0;
        BigDecimal filterBalanceLessThen = null;
        BigDecimal filterBalanceMoreThen = null;

        if ((flags & BALANCE_FLAG_MASK) != 0) {
            filterAssetKey = Longs.fromByteArray(Arrays.copyOfRange(data, position, position + Long.BYTES));
            position += Long.BYTES;

            filterBalancePos = data[position++];
            filterBalanceSide = data[position++];

            scale = data[position++];
            len = data[position++];
            filterBalanceLessThen = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + len)), scale);
            position += len;

            scale = data[position++];
            len = data[position++];
            filterBalanceMoreThen = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + len)), scale);
            position += len;

        }

        Integer filterTXType = null;
        Long filterTXStart = null;
        Long filterTXEnd = null;

        if ((flags & TXTYPE_FLAG_MASK) != 0) {

            filterTXType = (int) data[position++];

            filterTXStart = Longs.fromByteArray(Arrays.copyOfRange(data, position, position + Long.BYTES));
            position += Long.BYTES;

            filterTXEnd = Longs.fromByteArray(Arrays.copyOfRange(data, position, position + Long.BYTES));
            position += Long.BYTES;

        }

        int filterByPerson = data[position++];
        boolean selfPay = data[position++] > 0;

        return new ExPays(flags, assetKey, balancePos, backward, amountMin, amountMax,
                payMethod, payMethodValue, filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceLessThen, filterBalanceMoreThen,
                filterTXType, filterTXStart, filterTXEnd,
                filterByPerson, selfPay);
    }

    public static byte[] make(int flags, Long assetKey, int balancePos, boolean backward, BigDecimal amountMin, BigDecimal amountMax,
                              int payMethod, BigDecimal payMethodValue, Long filterAssetKey, int filterBalancePos, int filterBalanceSide,
                              BigDecimal filterBalanceLessThen, BigDecimal filterBalanceMoreThen,
                              Integer filterTXType, Long filterTXStart, Long filterTXEnd,
                              Integer filterByPerson, boolean selfPay)
            throws Exception {


        return new ExPays(flags, assetKey, balancePos, backward, amountMin, amountMax,
                payMethod, payMethodValue, filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceLessThen, filterBalanceMoreThen,
                filterTXType, filterTXStart, filterTXEnd,
                filterByPerson, selfPay).toByte();

    }

    /**
     * Version 2 maker for BlockExplorer
     */
    public JSONObject makeJSONforHTML(JSONObject langObj) {
        JSONObject json = new JSONObject();
        return json;

    }

    public JSONObject toJson() {

        JSONObject toJson = new JSONObject();


        return toJson;
    }

    public void calcPayoutsForMethodTotal() {
        // нужно подсчитать выплаты по общей сумме балансов
        int scale = asset.getScale();
        BigDecimal totalBalances = (BigDecimal) filteredPayouts.get(filteredPayoutsCount).c;
        BigDecimal coefficient = payMethodValue.divide(totalBalances,
                scale + Order.powerTen(totalBalances) + 3, RoundingMode.HALF_DOWN);
        for (int index = 0; index < filteredPayoutsCount; index++) {
            Fun.Tuple3 item = filteredPayouts.get(index);
            BigDecimal amount = (BigDecimal) item.b;
            filteredPayouts.set(index, new Fun.Tuple3(item.a, item.b,
                    amount.multiply(coefficient).setScale(scale)));
        }
    }

    public int isValid(RSignNote rNote) {

        if (hasAmount() && (
                this.actionType < 0 || this.actionType > 5
                        || this.amountMin == null
                        || this.amountMax == null
                        || this.payMethodValue == null
        )
        ) {
            return Transaction.INVALID_AMOUNT;
        }


        if (hasAssetFilter() && (
                this.filterBalancePos < 0 || this.filterBalancePos > 5
                        || this.filterBalanceSide < 0 || this.filterBalanceSide > 3
                        || this.filterBalanceLessThen == null
                        || this.filterBalanceMoreThen == null
        )
        ) {
            return Transaction.INVALID_BACKWARD_ACTION;
        }

        if (hasTXTypeFilter() && (
                this.filterTXType < 0 || this.filterTXType > 200
        )) {
            return Transaction.INVALID_TRANSACTION_TYPE;
        }

        if (assetKey != null && filterAssetKey != null
                && assetKey.equals(filterAssetKey)
                && actionType == filterBalancePos) {
            // при откате невозможно тогда будет правильно рассчитать - так как съехала общая сумма
            return Transaction.INVALID_TRANSFER_TYPE;
        }

        filteredPayouts = new ArrayList<>();
        filteredPayoutsCount = filterPayList(rNote, true);

        if (filteredPayoutsCount < 0) {
            // ERROR on make LIST
            return -filteredPayoutsCount;

        } else if (filteredPayoutsCount > 0) {
            height = rNote.getBlockHeight();

            totalFee = BigDecimal.valueOf(25L * filteredPayoutsCount * BlockChain.FEE_PER_BYTE, BlockChain.FEE_SCALE);

            long actionFlags = 0L;
            Account recipient = new Account((byte[]) filteredPayouts.get(0).a);
            PublicKeyAccount creator = rNote.getCreator();
            byte[] signature = rNote.getSignature();

            if (filterTXType == PAYMENT_METHOD_COEFF) {
                totalPay = (BigDecimal) filteredPayouts.get(filteredPayoutsCount).c;
            } else {
                totalPay = payMethodValue;
            }

            // проверим как будто всю сумму одному переводим
            int result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                    assetKey, asset, totalPay, recipient,
                    backward, rNote.getFee(), null, false, actionFlags);
            if (result != Transaction.VALIDATE_OK)
                return result;

            if (filterTXType == PAYMENT_METHOD_TOTAL) {
                calcPayoutsForMethodTotal();
            }

            ////////// TODO NEED CHECK ALL
            boolean needCheckAllList = false;
            if (needCheckAllList) {

                for (Fun.Tuple3 item : filteredPayouts) {

                    recipient = (Account) item.a;
                    if (recipient == null)
                        break;
                    BigDecimal amount = (BigDecimal) item.c;

                    result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                            assetKey, asset, amount, recipient,
                            backward, BigDecimal.ZERO, null, false, actionFlags);

                    if (result != Transaction.VALIDATE_OK) {
                        errorValue = amount.toPlainString() + " -> " + recipient.getAddress();
                        return result;
                    }

                }
            }
        }

        return Transaction.VALIDATE_OK;
    }

    public int filterPayList(Transaction transaction, boolean andValidate) {

        int scale = asset.getScale();

        boolean onlyPerson = filterByGender != NOT_FILTER_PERSONS;
        byte[] accountFrom = transaction.getCreator().getShortAddressBytes();

        DCSet dcSet = transaction.getDCSet();
        ItemAssetBalanceMap balancesMap = dcSet.getAssetBalanceMap();
        TransactionFinalMapImpl txMap = dcSet.getTransactionFinalMap();

        byte[] key;
        BigDecimal balance;
        BigDecimal payout;
        BigDecimal totalBalances = BigDecimal.ZERO;

        int count = 0;

        Fun.Tuple4<Long, Integer, Integer, Integer> addressDuration;
        Long myPersonKey = null;
        if (onlyPerson && !selfPay) {
            addressDuration = dcSet.getAddressPersonMap().getItem(accountFrom);
            if (addressDuration != null) {
                myPersonKey = addressDuration.a;
            }
        } else {
            myPersonKey = null;
        }

        boolean isPerson = transaction.getCreator().isPerson(dcSet, height, transaction.getCreatorPersonDuration());

        HashSet<Long> usedPersons = new HashSet<>();
        PersonCls person;

        try (IteratorCloseable<byte[]> iterator = balancesMap.getIteratorByAsset(filterAssetKey)) {
            while (iterator.hasNext()) {
                key = iterator.next();

                balance = Account.balanceInPositionAndSide(balancesMap.get(key), filterBalancePos, filterBalanceSide);

                if (filterBalanceLessThen != null && balance.compareTo(filterBalanceLessThen) > 0
                        || filterBalanceMoreThen != null && balance.compareTo(filterBalanceMoreThen) < 0)
                    continue;

                byte[] recipentShort = ItemAssetBalanceMap.getShortAccountFromKey(key);

                if (onlyPerson) {
                    // так как тут сортировка по убыванию значит первым встретится тот счет на котром больше всего актива
                    // - он и будет выбран куда 1 раз пошлем актив свой
                    addressDuration = dcSet.getAddressPersonMap().getItem(recipentShort);
                    if (addressDuration == null)
                        continue;
                    if (usedPersons.contains(addressDuration.a))
                        continue;

                    if (!selfPay && myPersonKey != null && myPersonKey.equals(addressDuration.a)) {
                        // сами себе не платим?
                        continue;
                    }

                    person = (PersonCls) dcSet.getItemPersonMap().get(addressDuration.a);

                    if (filterByGender != NOT_FILTER_GENDER) {
                        if (person.getGender() != filterByGender) {
                            continue;
                        }
                    }
                } else {

                    if (!selfPay && Arrays.equals(accountFrom, recipentShort)) {
                        // сами себе не платим?
                        continue;
                    }

                    addressDuration = null;
                    person = null;
                }

                Account recipient = new Account(recipentShort);

                // IF send from PERSON to ANONYMOUS
                if (andValidate && !TransactionAmount.isValidPersonProtect(dcSet, height, recipient,
                        isPerson, assetKey, actionType,
                        asset)) {
                    errorValue = recipient.getAddress();
                    return -Transaction.RECEIVER_NOT_PERSONALIZED;
                }

                /// если задано то проверим - входит ли в в диаппазон
                // - собранные блоки учитываем? да - иначе долго будет делать поиск
                if (filterTXStartSeqNo != null || filterTXEndSeqNo != null) {
                    // на счете должна быть активность в заданном диаппазоне для данного типа
                    if (!txMap.isCreatorWasActive(recipentShort, assetKey, filterTXType, filterTXEndSeqNo))
                        continue;
                }

                if (payMethodValue != null && payMethod == PAYMENT_METHOD_COEFF) {
                    // нужно вычислить сразу сколько шлем
                    payout = balance.multiply(payMethodValue).setScale(scale, RoundingMode.HALF_DOWN);
                } else {
                    payout = null;
                }

                // не проверяем на 0 - так это может быть рассылка писем всем
                filteredPayouts.add(new Fun.Tuple3(recipient, balance, payout));

                // просчитаем тоже даже если ошибка
                totalBalances = totalBalances.add(balance);
                count++;
                if (andValidate && count > MAX_COUNT) {
                    errorValue = "MAX count over: " + MAX_COUNT;
                    return -Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR;
                }

                if (onlyPerson) {
                    // учтем что такой персоне давали
                    usedPersons.add(addressDuration.a);
                }

            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        filteredPayouts.add(new Fun.Tuple3(null, null, totalBalances));

        return count;

    }

    public void process(Transaction rNote) {

        if (filteredPayouts == null) {
            filteredPayouts = new ArrayList<>();
            filteredPayoutsCount = filterPayList(rNote, false);
            if (filterTXType == PAYMENT_METHOD_TOTAL) {
                calcPayoutsForMethodTotal();
            }
        }

        if (filteredPayoutsCount == 0)
            return;

        height = rNote.getBlockHeight();
        asset = dcSet.getItemAssetMap().get(assetKey);

        totalFee = BigDecimal.ZERO;


    }

    public void orphan(Transaction rNote) {

        if (filteredPayouts == null) {
            filteredPayouts = new ArrayList<>();
            filteredPayoutsCount = filterPayList(rNote, false);
            if (filterTXType == PAYMENT_METHOD_TOTAL) {
                calcPayoutsForMethodTotal();
            }
        }

        if (filteredPayoutsCount == 0)
            return;

        height = rNote.getBlockHeight();
        asset = dcSet.getItemAssetMap().get(assetKey);

        totalFee = BigDecimal.ZERO;

    }

}
