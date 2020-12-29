package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
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

    public static final byte BASE_LENGTH = 4 + 3;

    public static final int MAX_COUNT = Integer.MAX_VALUE >> 1;
    private static final byte AMOUNT_FLAG_MASK = -128;
    private static final byte AMOUNT_MIN_FLAG_MASK = 64;
    private static final byte AMOUNT_MAX_FLAG_MASK = 32;
    private static final byte BALANCE_FLAG_MASK = 16;
    private static final byte BALANCE_AMOUNT_MIN_FLAG_MASK = 8;
    private static final byte BALANCE_AMOUNT_MAX_FLAG_MASK = 4;
    private static final byte ACTIVE_START_FLAG_MASK = 2;
    private static final byte ACTIVE_END_FLAG_MASK = 1;

    private static final byte PAYMENT_METHOD_TOTAL = 0; // by TOTAL
    private static final byte PAYMENT_METHOD_COEFF = 1; // by coefficient
    private static final byte PAYMENT_METHOD_ABSOLUTE = 2; // by ABSOLUTE VALUE

    private static final byte FILTER_PERSON_NONE = 0;
    private static final byte FILTER_PERSON_ONLY = 1;
    private static final byte FILTER_PERSON_ONLY_MAN = 2;
    private static final byte FILTER_PERSON_ONLY_WOMAN = 3;

    private static final byte NOT_FILTER_PERSONS = -1; //
    private static final byte NOT_FILTER_GENDER = -2; //

    private static final Logger LOGGER = LoggerFactory.getLogger(ExPays.class);

    /**
     * 0 - version; 1..3 - flags;
     */
    private int flags; // 4

    private Long assetKey; // 12
    private int balancePos; // 13
    private boolean backward; // 14
    private int payMethod; // 15 0 - by Total, 1 - by Percent
    private BigDecimal payMethodValue; // 17
    private BigDecimal amountMin; // 19
    private BigDecimal amountMax; //21

    private Long filterAssetKey; // 29
    private int filterBalancePos; //30
    private int filterBalanceSide; //31
    private BigDecimal filterBalanceMIN; // 33
    private BigDecimal filterBalanceMAX; // 34

    private int filterTXType; // 36
    private Long filterTXStartSeqNo; // 44
    public Long filterTXEndSeqNo; // 52

    private final int filterByGender; // 53 = gender or all
    public boolean selfPay; // 54

    /////////////////
    DCSet dcSet;
    private int height;
    AssetCls asset;
    public List<Fun.Tuple3<Account, BigDecimal, BigDecimal>> filteredPayouts;
    public int filteredPayoutsCount;
    public BigDecimal totalPay;
    public BigDecimal totalFee;
    public String errorValue;


    /**
     * make FLAGS internal
     *  @param flags
     * @param assetKey
     * @param balancePos
     * @param backward
     * @param payMethod
     * @param payMethodValue
     * @param amountMin
     * @param amountMax
     * @param filterAssetKey
     * @param filterBalancePos
     * @param filterBalanceSide
     * @param filterBalanceMIN
     * @param filterBalanceMAX
     * @param filterTXType
     * @param filterTXStartSeqNo
     * @param filterTXEndSeqNo
     * @param filterByGender
     * @param selfPay
     */
    public ExPays(int flags, Long assetKey, int balancePos, boolean backward, int payMethod, BigDecimal payMethodValue, BigDecimal amountMin, BigDecimal amountMax,
                  Long filterAssetKey, int filterBalancePos, int filterBalanceSide,
                  BigDecimal filterBalanceMIN, BigDecimal filterBalanceMAX,
                  int filterTXType, Long filterTXStartSeqNo, Long filterTXEndSeqNo,
                  int filterByGender, boolean selfPay) {
        this.flags = flags;

        if (assetKey != null && assetKey != 0L && payMethodValue != null && payMethodValue.signum() != 0) {
            this.flags |= AMOUNT_FLAG_MASK;
            this.assetKey = assetKey;
            this.balancePos = balancePos;
            this.backward = backward;
            this.payMethod = payMethod;
            this.payMethodValue = payMethodValue;

            if (payMethod != PAYMENT_METHOD_ABSOLUTE) {
                if (amountMin != null) {
                    this.flags |= AMOUNT_MIN_FLAG_MASK;
                    this.amountMin = amountMin;
                }
                if (amountMax != null) {
                    this.flags |= AMOUNT_MAX_FLAG_MASK;
                    this.amountMax = amountMax;
                }
            }
        }

        if (filterAssetKey != null && filterAssetKey != 0L) {
            this.flags |= BALANCE_FLAG_MASK;
            this.filterAssetKey = filterAssetKey;
            this.filterBalancePos = filterBalancePos;
            this.filterBalanceSide = filterBalanceSide;
            if (filterBalanceMIN != null) {
                this.flags |= BALANCE_AMOUNT_MIN_FLAG_MASK;
                this.filterBalanceMIN = filterBalanceMIN;
            }
            if (filterBalanceMAX != null) {
                this.flags |= BALANCE_AMOUNT_MAX_FLAG_MASK;
                this.filterBalanceMAX = filterBalanceMAX;
            }
        }

        this.filterTXType = filterTXType;

        if (filterTXStartSeqNo != null) {
            this.flags |= ACTIVE_START_FLAG_MASK;
            this.filterTXStartSeqNo = filterTXStartSeqNo;
        }
        if (filterTXEndSeqNo != null) {
            this.flags |= ACTIVE_END_FLAG_MASK;
            this.filterTXEndSeqNo = filterTXEndSeqNo;
        }

        this.filterByGender = filterByGender;
        this.selfPay = selfPay;
    }

    public boolean hasAmount() {
        return (this.flags & AMOUNT_FLAG_MASK) != 0;
    }

    public boolean hasAmountMin() {
        return (this.flags & AMOUNT_MIN_FLAG_MASK) != 0;
    }

    public boolean hasAmountMax() {
        return (this.flags & AMOUNT_MAX_FLAG_MASK) != 0;
    }

    public boolean hasAssetFilter() {
        return (this.flags & BALANCE_FLAG_MASK) != 0;
    }

    public boolean hasAssetFilterBalMIN() {
        return (this.flags & BALANCE_AMOUNT_MIN_FLAG_MASK) != 0;
    }

    public boolean hasAssetFilterBalMAX() {
        return (this.flags & BALANCE_AMOUNT_MAX_FLAG_MASK) != 0;
    }

    public boolean hasTXTypeFilterActiveStart() {
        return (this.flags & ACTIVE_START_FLAG_MASK) != 0;
    }

    public boolean hasTXTypeFilterActiveEnd() {
        return (this.flags & ACTIVE_END_FLAG_MASK) != 0;
    }

    public void setDC(DCSet dcSet) {
        this.dcSet = dcSet;
        if (hasAmount() && this.asset == null) {
            this.asset = this.dcSet.getItemAssetMap().get(this.assetKey);
        }
    }

    public byte[] toBytes() throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        outStream.write(Ints.toByteArray(flags));

        byte[] buff;
        if (hasAmount()) {
            outStream.write(Longs.toByteArray(this.assetKey));

            buff = new byte[]{(byte) balancePos, (byte) (backward ? 1 : 0), (byte) payMethod};
            outStream.write(buff);

            outStream.write(this.payMethodValue.scale());
            buff = this.payMethodValue.unscaledValue().toByteArray();
            outStream.write(buff.length);
            outStream.write(buff);

            if (hasAmountMin()) {
                outStream.write(this.amountMin.scale());
                buff = this.amountMin.unscaledValue().toByteArray();
                outStream.write(buff.length);
                outStream.write(buff);
            }

            if (hasAmountMax()) {
                outStream.write(this.amountMax.scale());
                buff = this.amountMax.unscaledValue().toByteArray();
                outStream.write(buff.length);
                outStream.write(buff);
            }

        }

        if (hasAssetFilter()) {
            outStream.write(Longs.toByteArray(this.filterAssetKey));
            buff = new byte[]{(byte) filterBalancePos, (byte) filterBalanceSide};
            outStream.write(buff);

            if (hasAssetFilterBalMIN()) {
                outStream.write(this.filterBalanceMIN.scale());
                buff = this.filterBalanceMIN.unscaledValue().toByteArray();
                outStream.write(buff.length);
                outStream.write(buff);
            }

            if (hasAssetFilterBalMAX()) {
                outStream.write(this.filterBalanceMAX.scale());
                buff = this.filterBalanceMAX.unscaledValue().toByteArray();
                outStream.write(buff.length);
                outStream.write(buff);
            }
        }

        if (hasTXTypeFilterActiveStart()) {
            outStream.write(Longs.toByteArray(this.filterTXStartSeqNo));
        }
        if (hasTXTypeFilterActiveEnd()) {
            outStream.write(Longs.toByteArray(this.filterTXEndSeqNo));
        }

        outStream.write(new byte[]{(byte) filterTXType, (byte) filterByGender, (byte) (selfPay ? 1 : 0)});

        return outStream.toByteArray();

    }

    public int length() {
        int len = BASE_LENGTH;

        if (hasAmount()) {
            len += Transaction.KEY_LENGTH + 3
                    + payMethodValue.unscaledValue().toByteArray().length + 2
                    + (hasAmountMin() ? amountMin.unscaledValue().toByteArray().length + 2 : 0)
                    + (hasAmountMax() ? amountMax.unscaledValue().toByteArray().length + 2 : 0);
        }

        if (hasAssetFilter()) {
            len += Transaction.KEY_LENGTH + 2
                    + (hasAssetFilterBalMIN() ? filterBalanceMIN.unscaledValue().toByteArray().length + 2 : 0)
                    + (hasAssetFilterBalMAX() ? filterBalanceMAX.unscaledValue().toByteArray().length + 2 : 0);
        }

        if (hasTXTypeFilterActiveStart()) {
            len += Long.BYTES;
        }
        if (hasTXTypeFilterActiveEnd()) {
            len += Long.BYTES;
        }

        return len;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ExPays parse(byte[] data, int position) throws Exception {

        int scale;
        int len;

        int flags = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + Integer.BYTES));
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

            if ((flags & AMOUNT_MIN_FLAG_MASK) != 0) {
                scale = data[position++];
                len = data[position++];
                amountMin = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + len)), scale);
                position += len;
            }

            if ((flags & AMOUNT_MAX_FLAG_MASK) != 0) {
                scale = data[position++];
                len = data[position++];
                amountMax = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + len)), scale);
                position += len;
            }

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

            if ((flags & BALANCE_AMOUNT_MIN_FLAG_MASK) != 0) {
                scale = data[position++];
                len = data[position++];
                filterBalanceMoreThen = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + len)), scale);
                position += len;
            }

            if ((flags & BALANCE_AMOUNT_MAX_FLAG_MASK) != 0) {
                scale = data[position++];
                len = data[position++];
                filterBalanceLessThen = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, position + len)), scale);
                position += len;
            }

        }

        Long filterTXStart = null;
        Long filterTXEnd = null;

        if ((flags & ACTIVE_START_FLAG_MASK) != 0) {
            filterTXStart = Longs.fromByteArray(Arrays.copyOfRange(data, position, position + Long.BYTES));
            position += Long.BYTES;
        }
        if ((flags & ACTIVE_END_FLAG_MASK) != 0) {
            filterTXEnd = Longs.fromByteArray(Arrays.copyOfRange(data, position, position + Long.BYTES));
            position += Long.BYTES;
        }

        int filterTXType = data[position++];
        int filterByPerson = data[position++];
        boolean selfPay = data[position++] > 0;

        return new ExPays(flags, assetKey, balancePos, backward, payMethod, payMethodValue, amountMin, amountMax,
                filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceMoreThen, filterBalanceLessThen,
                filterTXType, filterTXStart, filterTXEnd,
                filterByPerson, selfPay);
    }

    /**
     * @param assetKey
     * @param balancePos
     * @param backward
     * @param payMethod
     * @param payMethodValue
     * @param amountMin
     * @param amountMax
     * @param filterAssetKey
     * @param filterBalancePos
     * @param filterBalanceSide
     * @param filterBalanceMoreThen
     * @param filterBalanceLessThen
     * @param filterTXType
     * @param filterTXStartStr      as SeqNo: 123-1
     * @param filterTXEndStr        as SeqNo: 123-1
     * @param filterByPerson
     * @param selfPay
     * @return
     */
    public static Fun.Tuple2<ExPays, String> make(Long assetKey, int balancePos, boolean backward,
                                                  int payMethod, String payMethodValue, String amountMin, String amountMax,
                                                  Long filterAssetKey, int filterBalancePos, int filterBalanceSide,
                                                  String filterBalanceMoreThen, String filterBalanceLessThen,
                                                  int filterTXType, String filterTXStartStr, String filterTXEndStr,
                                                  int filterByPerson, boolean selfPay) {

        int steep = 0;
        BigDecimal amountMinBG;
        BigDecimal amountMaxBG;
        BigDecimal payMethodValueBG;
        BigDecimal filterBalanceMoreThenBG;
        BigDecimal filterBalanceLessThenBG;
        Long filterTXStart;
        Long filterTXEnd;
        try {
            amountMinBG = amountMin == null || amountMin.isEmpty() ? null : new BigDecimal(amountMin);
            ++steep;
            amountMaxBG = amountMax == null || amountMax.isEmpty() ? null : new BigDecimal(amountMax);
            ++steep;
            payMethodValueBG = payMethodValue == null || payMethodValue.isEmpty() ? null : new BigDecimal(payMethodValue);
            ++steep;
            filterBalanceMoreThenBG = filterBalanceMoreThen == null || filterBalanceMoreThen.isEmpty() ? null : new BigDecimal(filterBalanceMoreThen);
            ++steep;
            filterBalanceLessThenBG = filterBalanceLessThen == null || filterBalanceLessThen.isEmpty() ? null : new BigDecimal(filterBalanceLessThen);
            ++steep;
            filterTXStart = filterTXStartStr == null || filterTXStartStr.isEmpty() ? null : Transaction.parseDBRef(filterTXStartStr);
            ++steep;
            filterTXEnd = filterTXEndStr == null || filterTXEndStr.isEmpty() ? null : Transaction.parseDBRef(filterTXEndStr);
        } catch (Exception e) {
            String error;
            switch (steep) {
                case 0:
                    error = "Wrong amountMin";
                    break;
                case 1:
                    error = "Wrong amountMax";
                    break;
                case 2:
                    error = "Wrong payMethodValue";
                    break;
                case 3:
                    error = "Wrong filterBalanceMoreThen";
                    break;
                case 4:
                    error = "Wrong filterBalanceLessThen";
                    break;
                case 5:
                    error = "Wrong filterTXStartStr";
                    break;
                case 6:
                    error = "Wrong filterTXEndStr";
                    break;
                default:
                    error = e.getMessage();
            }
            return new Fun.Tuple2<>(null, error);
        }

        int flags = 0;
        return new Fun.Tuple2<>(new ExPays(flags, assetKey, balancePos, backward, payMethod, payMethodValueBG, amountMinBG, amountMaxBG,
                filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceMoreThenBG, filterBalanceLessThenBG,
                filterTXType, filterTXStart, filterTXEnd,
                filterByPerson, selfPay), null);

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

    public boolean calcPayoutsForMethodTotal() {

        if (filteredPayoutsCount == 0)
            return false;

        // нужно подсчитать выплаты по общей сумме балансов
        int scale = asset.getScale();
        BigDecimal totalBalances = filteredPayouts.get(filteredPayoutsCount).c;
        if (totalBalances.signum() == 0)
            // возможно это просто высылка писем всем - без перечислений
            return false;

        BigDecimal coefficient = payMethodValue.divide(totalBalances,
                scale + Order.powerTen(totalBalances) + 3, RoundingMode.HALF_DOWN);
        for (int index = 0; index < filteredPayoutsCount; index++) {
            Fun.Tuple3 item = filteredPayouts.get(index);
            BigDecimal amount = (BigDecimal) item.b;
            filteredPayouts.set(index, new Fun.Tuple3(item.a, item.b,
                    amount.multiply(coefficient).setScale(scale, RoundingMode.DOWN)));
        }

        return true;
    }

    public long getLongFee() {
        return 10L * filteredPayoutsCount;
    }

    public int isValid(RSignNote rNote) {

        if (hasAmount()) {
            if (this.balancePos < TransactionAmount.ACTION_SEND || this.balancePos > TransactionAmount.ACTION_SPEND) {
                errorValue = "Payouts: balancePos";
                return Transaction.INVALID_AMOUNT;
            } else if (this.payMethodValue == null) {
                errorValue = "Payouts: payMethodValue == null";
                return Transaction.INVALID_AMOUNT;
            }
        }

        if (hasAssetFilter()) {
            if (this.filterBalancePos < TransactionAmount.ACTION_SEND || this.filterBalancePos > TransactionAmount.ACTION_SPEND) {
                errorValue = "Payouts: filterBalancePos";
                return Transaction.INVALID_BACKWARD_ACTION;
            } else if (this.filterBalanceSide < TransactionAmount.BALANCE_SIDE_DEBIT || this.filterBalanceSide > TransactionAmount.BALANCE_SIDE_CREDIT) {
                errorValue = "Payouts: filterBalanceSide";
                return Transaction.INVALID_BACKWARD_ACTION;
            }
        }

        if (this.filterTXType != 0 && !Transaction.isValidTransactionType(this.filterTXType)) {
            errorValue = "Payouts: filterTXType= " + filterTXType;
            return Transaction.INVALID_TRANSACTION_TYPE;
        }

        if (assetKey != null && filterAssetKey != null
                && assetKey.equals(filterAssetKey)
                && balancePos == filterBalancePos) {
            // при откате невозможно тогда будет правильно рассчитать - так как съехала общая сумма
            errorValue = "Payouts: assetKey == filterAssetKey && balancePos == filterBalancePos";
            return Transaction.INVALID_TRANSFER_TYPE;
        }

        filteredPayouts = new ArrayList<>();
        filteredPayoutsCount = filterPayList(rNote, true);

        if (filteredPayoutsCount < 0) {
            // ERROR on make LIST
            return -filteredPayoutsCount;

        } else if (filteredPayoutsCount > 0) {
            height = rNote.getBlockHeight();

            totalFee = BigDecimal.valueOf(getLongFee() * BlockChain.FEE_PER_BYTE, BlockChain.FEE_SCALE);

            long actionFlags = 0L;
            Account recipient = filteredPayouts.get(0).a;
            PublicKeyAccount creator = rNote.getCreator();
            byte[] signature = rNote.getSignature();

            if (filterTXType == PAYMENT_METHOD_COEFF) {
                totalPay = (BigDecimal) filteredPayouts.get(filteredPayoutsCount).c;
            } else {
                totalPay = payMethodValue;
            }

            // возьмем знаки (минус) для создания позиции баланса такой
            Fun.Tuple2<Integer, Integer> signs = Account.getSignsForBalancePos(balancePos);
            Long key = signs.a * assetKey;

            // проверим как будто всю сумму одному переводим
            int result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                    key, asset, signs.b > 0 ? totalPay : totalPay, recipient,
                    backward, rNote.getFee(), null, false, actionFlags);
            if (result != Transaction.VALIDATE_OK) {
                errorValue = "Payouts: on isValidAction";
                return result;
            }

            if (filterTXType == PAYMENT_METHOD_TOTAL) {
                if (!calcPayoutsForMethodTotal())
                    // не удалось просчитать значения
                    return Transaction.VALIDATE_OK;
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
                            key, asset, signs.b > 0 ? amount : amount.negate(), recipient,
                            backward, BigDecimal.ZERO, null, false, actionFlags);

                    if (result != Transaction.VALIDATE_OK) {
                        errorValue = "Payouts: " + amount.toPlainString() + " -> " + recipient.getAddress();
                        return result;
                    }

                }
            }
        }

        return Transaction.VALIDATE_OK;
    }

    public int filterPayList(Transaction transaction, boolean andValidate) {

        int scale = asset.getScale();

        boolean onlyPerson = filterByGender > FILTER_PERSON_NONE;
        int gender = filterByGender - FILTER_PERSON_ONLY_MAN;
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
        byte[] assetOwner = asset.getOwner().getShortAddressBytes();

        try (IteratorCloseable<byte[]> iterator = balancesMap.getIteratorByAsset(filterAssetKey)) {
            while (iterator.hasNext()) {
                key = iterator.next();

                balance = Account.balanceInPositionAndSide(balancesMap.get(key), filterBalancePos, filterBalanceSide);

                if (filterBalanceMIN != null && balance.compareTo(filterBalanceMIN) < 0
                        || filterBalanceMAX != null && balance.compareTo(filterBalanceMAX) > 0)
                    continue;

                byte[] recipientShort = ItemAssetBalanceMap.getShortAccountFromKey(key);
                if (Arrays.equals(assetOwner, recipientShort))
                    // создателю актива не даем ничего никогда
                    continue;

                if (onlyPerson) {
                    // так как тут сортировка по убыванию значит первым встретится тот счет на котром больше всего актива
                    // - он и будет выбран куда 1 раз пошлем актив свой
                    addressDuration = dcSet.getAddressPersonMap().getItem(recipientShort);
                    if (addressDuration == null)
                        continue;
                    if (usedPersons.contains(addressDuration.a))
                        continue;

                    if (!selfPay && myPersonKey != null && myPersonKey.equals(addressDuration.a)) {
                        // сами себе не платим?
                        continue;
                    }

                    person = (PersonCls) dcSet.getItemPersonMap().get(addressDuration.a);

                    if (person.getGender() != gender) {
                        continue;
                    }

                } else {

                    if (!selfPay && Arrays.equals(accountFrom, recipientShort)) {
                        // сами себе не платим?
                        continue;
                    }

                    addressDuration = null;
                    person = null;
                }

                Account recipient = new Account(recipientShort);

                /// если задано то проверим - входит ли в в диапазон
                // - собранные блоки учитываем? да - иначе долго будет делать поиск
                if (filterTXStartSeqNo != null || filterTXEndSeqNo != null) {
                    // на счете должна быть активность в заданном диапазоне для данного типа
                    if (!txMap.isCreatorWasActive(recipientShort, filterTXStartSeqNo, filterTXType, filterTXEndSeqNo))
                        continue;
                }

                // IF send from PERSON to ANONYMOUS
                if (andValidate && !TransactionAmount.isValidPersonProtect(dcSet, height, recipient,
                        isPerson, assetKey, balancePos,
                        asset)) {
                    errorValue = recipient.getAddress();
                    return -Transaction.RECEIVER_NOT_PERSONALIZED;
                }

                if (payMethodValue != null && payMethod == PAYMENT_METHOD_COEFF) {
                    // нужно вычислить сразу сколько шлем
                    payout = balance.multiply(payMethodValue).setScale(scale, RoundingMode.HALF_DOWN);
                } else if (payMethodValue != null && payMethod == PAYMENT_METHOD_ABSOLUTE) {
                    payout = payMethodValue.setScale(scale, RoundingMode.HALF_DOWN);
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

        if (count > 0)
            filteredPayouts.add(new Fun.Tuple3(null, null, totalBalances));

        return count;

    }

    public void processBody(Transaction rNote, boolean asOrphan, Block block) {
        PublicKeyAccount creator = rNote.getCreator();
        boolean isDirect = asset.isDirectBalances();
        long absKey = assetKey;
        boolean incomeReverse = balancePos == TransactionAmount.ACTION_HOLD;

        // возьмем знаки (минус) для создания позиции баланса такой
        Fun.Tuple2<Integer, Integer> signs = Account.getSignsForBalancePos(balancePos);
        Long key = signs.a * assetKey;

        Account recipient;
        for (Fun.Tuple3 item : filteredPayouts) {

            recipient = (Account) item.a;
            if (recipient == null)
                break;
            BigDecimal amount = (BigDecimal) item.c;

            TransactionAmount.processAction(dcSet, asOrphan, creator, recipient, balancePos, absKey,
                    key, signs.b > 0 ? amount : amount.negate(), backward,
                    isDirect, incomeReverse);

            if (!asOrphan && block != null)
                rNote.addCalculated(block, recipient, absKey, amount, "payout");

        }

    }

    public void process(Transaction rNote, Block block) {

        if (filteredPayouts == null) {
            filteredPayouts = new ArrayList<>();
            filteredPayoutsCount = filterPayList(rNote, false);
        }

        if (filteredPayoutsCount == 0)
            return;

        if (payMethod == PAYMENT_METHOD_TOTAL) {
            if (!calcPayoutsForMethodTotal())
                // не удалось просчитать значения
                return;
        }

        height = rNote.getBlockHeight();
        asset = dcSet.getItemAssetMap().get(assetKey);

        totalFee = BigDecimal.ZERO;

        processBody(rNote, false, block);

    }

    public void orphan(Transaction rNote) {

        if (filteredPayouts == null) {
            filteredPayouts = new ArrayList<>();
            filteredPayoutsCount = filterPayList(rNote, false);
        }

        if (filteredPayoutsCount == 0)
            return;

        if (payMethod == PAYMENT_METHOD_TOTAL) {
            if (payMethod == PAYMENT_METHOD_TOTAL) {
                if (!calcPayoutsForMethodTotal())
                    // не удалось просчитать значения
                    return;
            }
        }

        height = rNote.getBlockHeight();
        asset = dcSet.getItemAssetMap().get(assetKey);

        totalFee = BigDecimal.ZERO;

        processBody(rNote, true, null);

    }

}
