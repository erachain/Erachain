package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.AssetCls;
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
    private int balancePos; // 13
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
    private boolean selfPay; // 54

    /////////////////
    private int height;
    AssetCls asset;
    public List<Fun.Tuple2> payouts;
    public BigDecimal totalPay;
    public BigDecimal totalFee;
    public String errorValue;


    public ExPays(int flags, Long assetKey, int balancePos, boolean backward, BigDecimal amountMin, BigDecimal amountMax,
                  int payMethod, BigDecimal payMethodValue, Long filterAssetKey, int filterBalancePos, int filterBalanceSide,
                  BigDecimal filterBalanceLessThen, BigDecimal filterBalanceMoreThen,
                  Integer filterTXType, Long filterTXStartSeqNo, Long filterTXEndSeqNo,
                  Integer filterByGender, boolean selfPay) {
        this.flags = flags;

        if (assetKey != null && assetKey != 0) {
            this.flags |= AMOUNT_FLAG_MASK;
            this.assetKey = assetKey;
            this.balancePos = balancePos;
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

    public byte[] toByte() throws Exception {

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

    public int isValid(DCSet dcSet, RSignNote rNote) {

        if (hasAmount() && (
                this.balancePos < 0 || this.balancePos > 5
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


        payouts = new ArrayList<>();
        int count = makePayList(rNote, payouts);

        if (count < 0) {
            // ERROR on make LIST
            return -count;

        } else if (count > 0) {
            height = rNote.getBlockHeight();
            asset = dcSet.getItemAssetMap().get(assetKey);

            totalPay = (BigDecimal) payouts.get(count).b;
            totalFee = BigDecimal.ZERO;

            long actionFlags = 0L;
            Account recipient = new Account((byte[]) payouts.get(1).a);
            PublicKeyAccount creator = rNote.getCreator();
            byte[] signature = rNote.getSignature();
            // проверим как будто всю сумму одному переводим
            int result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                    assetKey, asset, totalPay, recipient,
                    backward, rNote.getFee(), null, false, actionFlags);
            if (result != Transaction.VALIDATE_OK)
                return result;

            boolean needCheckAllList = false;
            if (needCheckAllList) {
                for (Fun.Tuple2 item : payouts) {
                    if (item.a instanceof Integer)
                        // end
                        break;

                    recipient = (Account) item.a;
                    BigDecimal amount = (BigDecimal) item.b;

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

    public int makePayList(Transaction transaction, List<Fun.Tuple2> payouts) {

        BigDecimal totalSendAmount = BigDecimal.ZERO;

        boolean onlyPerson = filterByGender != NOT_FILTER_PERSONS;
        byte[] accountFrom = transaction.getCreator().getShortAddressBytes();

        DCSet dcSet = transaction.getDCSet();
        ItemAssetBalanceMap balancesMap = dcSet.getAssetBalanceMap();
        TransactionFinalMapImpl txMap = dcSet.getTransactionFinalMap();

        byte[] key;
        Fun.Tuple2<BigDecimal, BigDecimal> balance;

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
        int actionType = balancePos;

        HashSet<Long> usedPersons = new HashSet<>();
        PersonCls person;

        try (IteratorCloseable<byte[]> iterator = balancesMap.getIteratorByAsset(filterAssetKey)) {
            while (iterator.hasNext()) {
                key = iterator.next();

                balance = Account.getBalanceInPosition(balancesMap.get(key), filterBalancePos);

                // только тем у кого положительный баланс и больше чем задано
                if (balance.b.compareTo(filterBalanceLessThen) < 0)
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
                if (!TransactionAmount.isValidPersonProtect(dcSet, height, recipient,
                        isPerson, filterAssetKey, actionType,
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

                BigDecimal sendAmount;
                if (amountMin != null && amountMin.signum() > 0) {
                    sendAmount = amountMin;
                } else {
                    sendAmount = BigDecimal.ZERO;
                }

                if (payMethodValue != null && payMethodValue.signum() > 0) {
                    sendAmount = sendAmount.add(balance.b.multiply(payMethodValue));
                }

                payouts.add(new Fun.Tuple2(recipient, sendAmount));

                // просчитаем тоже даже если ошибка
                totalSendAmount = totalSendAmount.add(sendAmount);
                count++;
                if (onlyPerson) {
                    // учтем что такой персоне давали
                    usedPersons.add(addressDuration.a);
                }

            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        payouts.add(new Fun.Tuple2(count, totalSendAmount));
        return count;

    }

    public void process(Transaction transaction) {
        List<Fun.Tuple2> payouts = new ArrayList<>();
        DCSet dcSet = transaction.getDCSet();

        int count = makePayList(transaction, payouts);
        if (count == 0)
            return;

        int height = transaction.getBlockHeight();
        BigDecimal totalPay = (BigDecimal) payouts.get(count).b;
        BigDecimal totalFee = BigDecimal.ZERO;


    }

    public void orphan(Transaction transaction) {
        List<Fun.Tuple2> payouts = new ArrayList<>();
        int count = makePayList(transaction, payouts);
        if (count == 0)
            return;
        BigDecimal totalPay = (BigDecimal) payouts.get(count).b;
        BigDecimal totalFee = BigDecimal.ZERO;

    }

}
