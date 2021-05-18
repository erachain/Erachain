package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
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
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

/**
 * StandardCharsets.UTF_8 JSON "TM" - template key "PR" - template params
 * "HS" - Hashes "MS" - message
 * <p>
 * PARAMS template:TemplateCls param_keys: [id:text] hashes_Set: [name:hash]
 * mess: message title: Title file_Set: [file Name, ZIP? , file byte[]]
 */

public class ExAirdrop {

    public static final byte BASE_LENGTH = 4 + 8 + 8 + 2;

    public static final int MAX_COUNT = Short.MAX_VALUE;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExAirdrop.class);

    /**
     * 0 - version; 1 - flags;
     */
    private int flags; // byte[2]

    private byte[][] addresses;
    private long assetKey;
    private BigDecimal amount;

    /////////////////
    DCSet dcSet;
    private int height;
    AssetCls asset;

    public String errorValue;

    public ExAirdrop(int flags, long assetKey, BigDecimal amount, byte[][] addresses) {
        this.flags = flags;
        this.assetKey = assetKey;
        this.amount = amount;
        this.addresses = addresses;

    }

    public Long getAssetKey() {
        return assetKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public AssetCls getAsset() {
        return asset;
    }


    public void setDC(DCSet dcSet) {
        if (this.dcSet == null || !this.dcSet.equals(dcSet)) {
            this.dcSet = dcSet;
            this.asset = this.dcSet.getItemAssetMap().get(this.assetKey);
        }
    }

    public int parseDBData(byte[] dbData, int position) {
        flags = Ints.fromByteArray(Arrays.copyOfRange(dbData, position, position + Integer.BYTES));
        position += Integer.BYTES;

        short listLen = (short) flags;
        flags = flags >>> 16;

        assetKey = Longs.fromByteArray(Arrays.copyOfRange(dbData, position, position + Long.BYTES));
        position += Long.BYTES;


        int scale = dbData[position++];
        int len = dbData[position++];
        amount = new BigDecimal(new BigInteger(Arrays.copyOfRange(dbData, position, position + len)), scale);

        position += len;

        addresses = new byte[listLen][];
        for (int i = 0; i < listLen; i++) {
            assetKey = Longs.fromByteArray(Arrays.copyOfRange(dbData, position, position + Long.BYTES));
        }
        position += listLen * Account.ADDRESS_SHORT_LENGTH;

        return position;

    }

    public byte[] toBytes() throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        outStream.write(Ints.toByteArray(flags));

        byte[] buff;
        outStream.write(Longs.toByteArray(this.assetKey));

        outStream.write(this.amount.scale());
        buff = this.amount.unscaledValue().toByteArray();
        outStream.write(buff.length);
        outStream.write(buff);

        return outStream.toByteArray();

    }

    public int length() {
        return BASE_LENGTH + addresses.length * Account.ADDRESS_SHORT_LENGTH;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ExAirdrop parse(byte[] data, int position) throws Exception {

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

        return new ExAirdrop(flags, assetKey, balancePos, backward, payMethod, payMethodValue, amountMin, amountMax,
                );
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
     * @param filterTimeStartStr    'yyyy-MM-dd hh:mm:00' or Timestamp[sec] or SeqNo: 123-1
     * @param filterTimeXEndStr     'yyyy-MM-dd hh:mm:00' or Timestamp[sec] or SeqNo: 123-1
     * @param filterByPerson
     * @param selfPay
     * @return
     */
    public static Fun.Tuple2<ExAirdrop, String> make(Long assetKey, int balancePos, boolean backward,
                                                     int payMethod, String payMethodValue, String amountMin, String amountMax,
                                                     Long filterAssetKey, int filterBalancePos, int filterBalanceSide,
                                                     String filterBalanceMoreThen, String filterBalanceLessThen,
                                                     int filterTXType, String filterTimeStartStr, String filterTimeXEndStr,
                                                     int filterByPerson, boolean selfPay) {

        int steep = 0;
        BigDecimal amountMinBG;
        BigDecimal amountMaxBG;
        BigDecimal payMethodValueBG;
        BigDecimal filterBalanceMoreThenBG;
        BigDecimal filterBalanceLessThenBG;
        Long filterTimeStart;
        Long filterTimeEnd;

        Controller cntr = Controller.getInstance();
        BlockChain chain = cntr.getBlockChain();

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
            if (filterTimeStartStr == null || filterTimeStartStr.isEmpty()) {
                filterTimeStart = null;
            } else {
                try {
                    Date parsedDate = DATE_FORMAT.parse(filterTimeStartStr);
                    Timestamp timestamp = new Timestamp(parsedDate.getTime());
                    filterTimeStart = timestamp.getTime();
                } catch (Exception e) {
                    filterTimeStart = Transaction.parseDBRefSeqNo(filterTimeStartStr);
                    if (filterTimeStart == null) {
                        try {
                            filterTimeStart = Long.parseLong(filterTimeStartStr) * 1000L;
                        } catch (Exception e1) {
                        }
                    } else {
                        filterTimeStart = Transaction.getTimestampByDBRef((filterTimeStart));
                    }
                }
            }

            ++steep;
            if (filterTimeXEndStr == null || filterTimeXEndStr.isEmpty()) {
                filterTimeEnd = null;
            } else {
                try {
                    Date parsedDate = DATE_FORMAT.parse(filterTimeXEndStr);
                    Timestamp timestamp = new Timestamp(parsedDate.getTime());
                    filterTimeEnd = timestamp.getTime();
                } catch (Exception e) {
                    filterTimeEnd = Transaction.parseDBRefSeqNo(filterTimeXEndStr);
                    if (filterTimeEnd == null) {
                        try {
                            filterTimeEnd = Long.parseLong(filterTimeXEndStr) * 1000L;
                        } catch (Exception e1) {
                        }
                    } else {
                        filterTimeEnd = Transaction.getTimestampByDBRef(filterTimeEnd);
                    }
                }
            }
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
                    error = "Wrong filterTimeStartStr";
                    break;
                case 6:
                    error = "Wrong filterTimeXEndStr";
                    break;
                default:
                    error = e.getMessage();
            }
            return new Fun.Tuple2<>(null, error);
        }

        if (assetKey == null || assetKey == 0L) {
            return new Fun.Tuple2<>(null, "Wrong assetKey (null or ZERO)");
        } else if (filterAssetKey == null || filterAssetKey == 0L) {
            return new Fun.Tuple2<>(null, "Wrong filterAssetKey (null or ZERO)");
        } else if (payMethodValueBG == null || payMethodValueBG.signum() == 0) {
            return new Fun.Tuple2<>(null, "Wrong payMethodValue (null or ZERO)");
        }

        int flags = 0;
        return new Fun.Tuple2<>(new ExAirdrop(flags, assetKey, balancePos, backward, payMethod, payMethodValueBG, amountMinBG, amountMaxBG,
                filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceMoreThenBG, filterBalanceLessThenBG,
                filterTXType, filterTimeStart, filterTimeEnd,
                filterByPerson, selfPay), null);

    }

    /**
     * Version 2 maker for BlockExplorer
     */
    public JSONObject makeJSONforHTML(JSONObject langObj) {
        JSONObject json = toJson();

        json.put("asset", asset.getName());
        if (filterAssetKey != null && filterAssetKey > 0L) {
            if (filterAsset == null)
                filterAsset = dcSet.getItemAssetMap().get(filterAssetKey);
            json.put("filterAsset", filterAsset.getName());
        }

        if (filteredAccrualsCount > 0) {
            json.put("Label_Counter", Lang.T("Counter", langObj));
            json.put("Label_Total_Amount", Lang.T("Total Amount", langObj));
            json.put("Label_Additional_Fee", Lang.T("Additional Fee", langObj));

        }

        json.put("payMethodName", Lang.T(ExAirdrop.viewPayMethod(payMethod), langObj));
        json.put("balancePosName", Lang.T(Account.balancePositionName(balancePos), langObj));
        json.put("filterBalancePosName", Lang.T(Account.balancePositionName(filterBalancePos), langObj));
        json.put("filterBalanceSideName", Lang.T(Account.balanceSideName(filterBalanceSide), langObj));
        json.put("filterTXTypeName", Lang.T(Transaction.viewTypeName(filterTXType), langObj));
        json.put("filterByGenderName", Lang.T(viewFilterPersMode(filterByGender), langObj));

        json.put("Label_Action_for_Asset", Lang.T("Action for Asset", langObj));
        json.put("Label_assetKey", Lang.T("Asset", langObj));
        json.put("Label_balancePos", Lang.T("Balance Position", langObj));
        json.put("Label_backward", Lang.T("Backward", langObj));

        json.put("Label_payMethod", Lang.T("Method of calculation", langObj));
        json.put("Label_payMethodValue", Lang.T("Value", langObj));
        json.put("Label_amountMin", Lang.T("Minimal Accrual", langObj));
        json.put("Label_amountMax", Lang.T("Maximum Accrual", langObj));

        json.put("Label_Filter_By_Asset_and_Balance", Lang.T("Filter By Asset and Balance", langObj));
        json.put("Label_balanceSide", Lang.T("Balance Side", langObj));
        json.put("Label_filterBalanceMIN", Lang.T("More or Equal", langObj));
        json.put("Label_filterBalanceMAX", Lang.T("Less or Equal", langObj));
        json.put("Label_Filter_by_Actions_and_Period", Lang.T("Filter by Actions and Period", langObj));
        json.put("Label_filterTXType", Lang.T("Action", langObj));
        json.put("Label_filterTimeStart", Lang.T("Time start", langObj));
        json.put("Label_filterTimeEnd", Lang.T("Time end", langObj));

        json.put("Label_Filter_by_Persons", Lang.T("Filter by Persons", langObj));
        json.put("Label_filterByGender", Lang.T("Gender", langObj));
        json.put("Label_selfUse", Lang.T("Accrual by creator account too", langObj));

        json.put("Label_", Lang.T("", langObj));
        return json;

    }

    public JSONObject toJson() {

        JSONObject toJson = new JSONObject();

        toJson.put("flags", flags);
        toJson.put("assetKey", assetKey);
        toJson.put("balancePos", balancePos);
        toJson.put("backward", backward);

        toJson.put("payMethod", payMethod);
        toJson.put("payMethodValue", payMethodValue.toPlainString());
        if (payMethod != PAYMENT_METHOD_ABSOLUTE) {
            toJson.put("amountMin", amountMin);
            toJson.put("amountMax", amountMax);
        }

        toJson.put("filterAssetKey", filterAssetKey);
        toJson.put("filterBalancePos", filterBalancePos);
        toJson.put("filterBalanceSide", filterBalanceSide);
        if (hasAssetFilterBalMIN())
            toJson.put("filterBalanceMIN", filterBalanceMIN);
        if (hasAssetFilterBalMAX())
            toJson.put("filterBalanceMAX", filterBalanceMAX);

        toJson.put("filterTXType", filterTXType);
        if (hasTXTypeFilterActiveStart())
            toJson.put("filterTimeStart", filterTimeStart);
        if (hasTXTypeFilterActiveEnd())
            toJson.put("filterTimeEnd", filterTimeEnd);

        toJson.put("filterByGender", filterByGender);
        toJson.put("useSelfBalance", useSelfBalance);

        if (filteredAccrualsCount > 0) {
            toJson.put("filteredAccrualsCount", filteredAccrualsCount);
            toJson.put("totalPay", totalPay.toPlainString());
            toJson.put("totalFeeBytes", totalFeeBytes);
            toJson.put("totalFee", BlockChain.feeBG(totalFeeBytes).toPlainString());
        }

        return toJson;
    }

    public boolean calcAccrualsForMethodTotal() {

        if (filteredAccrualsCount == 0)
            return false;

        // нужно подсчитать выплаты по общей сумме балансов
        int scale = asset.getScale();
        BigDecimal totalBalances = totalPay;
        if (totalBalances.signum() == 0)
            // возможно это просто высылка писем всем - без перечислений
            return false;

        // подсчитаем более точно сумму к выплате - по коэффициентам она округлится
        totalPay = BigDecimal.ZERO;
        BigDecimal coefficient = payMethodValue.divide(totalBalances,
                scale + Order.powerTen(totalBalances) + 3, RoundingMode.HALF_DOWN);
        Fun.Tuple4 item;
        BigDecimal accrual;
        maxBal = BigDecimal.ZERO;
        for (int index = 0; index < filteredAccrualsCount; index++) {
            item = filteredAccruals.get(index);
            accrual = (BigDecimal) item.b;
            accrual = accrual.multiply(coefficient).setScale(scale, RoundingMode.DOWN);

            totalPay = totalPay.add(accrual);
            filteredAccruals.set(index, new Fun.Tuple4(item.a, item.b, accrual, item.d));

            if (maxBal.compareTo(accrual.abs()) < 0) {
                // запомним максимальное для скидывания остатка
                maxBal = accrual.abs();
                maxIndex = index;
            }
        }

        BigDecimal totalDiff = payMethodValue.subtract(totalPay);
        if (totalDiff.signum() != 0) {
            // есть нераспределенный остаток
            Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>> maxItem = filteredAccruals.get(maxIndex);
            filteredAccruals.set(maxIndex, new Fun.Tuple4(maxItem.a, maxItem.b, maxItem.c.add(totalDiff), maxItem.d));

            totalPay = payMethodValue;
        }

        return true;
    }

    public int makeFilterPayList(DCSet dcSet, int height, AssetCls asset, Account creator, boolean andValidate) {

        filteredAccruals = new ArrayList<>();

        iteratorUses = 0L;
        filteredAccrualsCount = 0;

        int scale = asset.getScale();

        boolean onlyPerson = filterByGender > FILTER_PERSON_NONE;
        int gender = filterByGender - FILTER_PERSON_ONLY_MAN;
        byte[] accountFrom = creator.getShortAddressBytes();

        ItemAssetBalanceMap balancesMap = dcSet.getAssetBalanceMap();
        TransactionFinalMapImpl txMap = dcSet.getTransactionFinalMap();

        // определим - меняется ли позиция баланса если направление сменим
        // это нужно чтобы отсекать смену знака у балансов для тек активов у кого меняется позиция от знака
        // настроим данные платежа по знакам Актива ИКоличества, так как величина коэффициента способа всегда положительная
        Fun.Tuple2<Integer, Integer> signs = Account.getSignsForBalancePos(balancePos);
        int balancePosDirect = Account.balancePosition(assetKey * signs.a, new BigDecimal(signs.b), false, asset.isSelfManaged());
        int balancePosBackward = Account.balancePosition(assetKey * signs.a, new BigDecimal(signs.b), true, asset.isSelfManaged());
        int filterBySigNum;
        if (balancePosDirect != balancePosBackward) {
            if (balancePosDirect == TransactionAmount.ACTION_SPEND) {
                // используем только отрицательные балансы
                filterBySigNum = -1;
            } else {
                // используем только положительные балансы
                filterBySigNum = 1;
            }
        } else {
            filterBySigNum = 0;
        }

        boolean reversedBalancesInPosition = asset.isReverseBalancePos(balancePos);
        // сменим знак балансов для отрицательных
        if (reversedBalancesInPosition) {
            filterBySigNum *= -1;
        }


        Long filterTimeStartSeqNo = filterTimeStart == null ? null : Transaction.makeDBRef(
                Controller.getInstance().blockChain.getHeightOnTimestampMS(filterTimeStart), 0);
        Long filterTimeEndSeqNo = filterTimeEnd == null ? null : Transaction.makeDBRef(
                Controller.getInstance().blockChain.getHeightOnTimestampMS(filterTimeEnd), 0);

        byte[] key;
        BigDecimal balance;
        BigDecimal accrual;
        BigDecimal totalBalances = BigDecimal.ZERO;

        int count = 0;

        Fun.Tuple4<Long, Integer, Integer, Integer> addressDuration;
        Long myPersonKey = null;
        if (onlyPerson && !useSelfBalance) {
            addressDuration = dcSet.getAddressPersonMap().getItem(accountFrom);
            if (addressDuration != null) {
                myPersonKey = addressDuration.a;
            }
        }

        boolean creatorIsPerson = creator.isPerson(dcSet, height);

        HashSet<Long> usedPersons = new HashSet<>();
        PersonCls person;
        byte[] assetOwner = asset.getMaker().getShortAddressBytes();

        boolean hasAmount = hasAmount();
        boolean hasAssetFilter = hasAssetFilter();
        try (IteratorCloseable<byte[]> iterator = balancesMap.getIteratorByAsset(hasAssetFilter ? filterAssetKey : AssetCls.FEE_KEY)) {
            while (iterator.hasNext()) {
                key = iterator.next();

                balance = Account.balanceInPositionAndSide(balancesMap.get(key), filterBalancePos, filterBalanceSide);
                if (filterBySigNum != 0 && balance.signum() != 0 && filterBySigNum != balance.signum()) {
                    // произошла смена направления для актива у котро меняется Позиция баланса - пропускаем такое
                    continue;
                }

                if (hasAssetFilter && filterBalanceMIN != null && balance.compareTo(filterBalanceMIN) < 0
                        || filterBalanceMAX != null && balance.compareTo(filterBalanceMAX) > 0)
                    continue;

                byte[] recipientShort = ItemAssetBalanceMap.getShortAccountFromKey(key);
                if ((filterBySigNum == 0 || asset.getQuantity() <= 0) && Arrays.equals(assetOwner, recipientShort))
                    // создателю актива не даем если это в обе стороны балансы обработка - иначе Общая величина будет всегда = 0
                    continue;

                if (onlyPerson) {
                    // так как тут сортировка по убыванию значит первым встретится тот счет на котром больше всего актива
                    // - он и будет выбран куда 1 раз пошлем актив свой
                    addressDuration = dcSet.getAddressPersonMap().getItem(recipientShort);
                    if (addressDuration == null)
                        continue;
                    if (usedPersons.contains(addressDuration.a))
                        continue;

                    if (!useSelfBalance && myPersonKey != null && myPersonKey.equals(addressDuration.a)) {
                        // сами себе не платим?
                        continue;
                    }

                    person = (PersonCls) dcSet.getItemPersonMap().get(addressDuration.a);

                    if (gender >= 0 && person.getGender() != gender) {
                        continue;
                    }

                } else {

                    if (!useSelfBalance && Arrays.equals(accountFrom, recipientShort)) {
                        // сами себе не платим
                        continue;
                    }

                    addressDuration = null;
                    person = null;
                }

                Account recipient = new Account(recipientShort);

                /// если задано то проверим - входит ли в в диапазон
                // - собранные блоки учитываем? да - иначе долго будет делать поиск
                if (filterTXType != 0 || filterTimeStartSeqNo != null || filterTimeEndSeqNo != null) {
                    iteratorUses++; // учтем для начисления комиссии за каждый созданный итератор!
                    // на счете должна быть активность в заданном диапазоне для данного типа
                    if (!txMap.isCreatorWasActive(recipientShort, filterTimeStartSeqNo, filterTXType, filterTimeEndSeqNo))
                        continue;
                }

                // IF send from PERSON to ANONYMOUS
                if (hasAmount && andValidate && !TransactionAmount.isValidPersonProtect(dcSet, height, recipient,
                        creatorIsPerson, assetKey, balancePos,
                        asset)) {
                    errorValue = recipient.getAddress();
                    return (filteredAccrualsCount = -Transaction.RECEIVER_NOT_PERSONALIZED);
                }

                if (!hasAmount) {
                    accrual = null;
                } else {
                    switch (payMethod) {
                        case PAYMENT_METHOD_COEFF:
                            // нужно вычислить сразу сколько шлем
                            accrual = balance.multiply(payMethodValue).setScale(scale, RoundingMode.HALF_DOWN);
                            if (amountMin != null && amountMin.compareTo(accrual) > 0) {
                                accrual = amountMin;
                            } else if (amountMax != null && amountMax.compareTo(accrual) < 0) {
                                accrual = amountMax;
                            }
                            totalBalances = totalBalances.add(accrual);
                            break;
                        case PAYMENT_METHOD_ABSOLUTE:
                            accrual = payMethodValue.setScale(scale, RoundingMode.HALF_DOWN);
                            break;
                        default:
                            accrual = null;
                            totalBalances = totalBalances.add(balance);
                    }
                }

                // не проверяем на 0 - так это может быть рассылка писем всем
                filteredAccruals.add(new Fun.Tuple4(recipient, balance, accrual, null));

                count++;
                if (andValidate && count > MAX_COUNT) {
                    errorValue = "MAX count over: " + MAX_COUNT;
                    return (filteredAccrualsCount = -Transaction.INVALID_BLOCK_TRANS_SEQ_ERROR);
                }

                if (onlyPerson) {
                    // учтем что такой персоне давали
                    usedPersons.add(addressDuration.a);
                }

            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        switch (payMethod) {
            case PAYMENT_METHOD_ABSOLUTE:
                totalPay = payMethodValue.multiply(new BigDecimal(count));
                break;
            default:
                totalPay = totalBalances;
        }

        filteredAccrualsCount = count;
        calcTotalFeeBytes();
        return count;

    }

    public int makeFilterPayList(Transaction transaction, boolean andValidate) {
        return makeFilterPayList(transaction.getDCSet(), height, asset, transaction.getCreator(), andValidate);
    }

    public void checkValidList(DCSet dcSet, int height, AssetCls asset, Account creator) {

        if (!hasAmount()) {
            filteredAccrualsCount = 0;
            filteredAccruals = new ArrayList<>();
            return;
        }

        filteredAccrualsCount = makeFilterPayList(dcSet, height, asset, creator, false);
        if (filteredAccrualsCount == 0)
            return;

        if (payMethod == PAYMENT_METHOD_TOTAL) {
            // просчитаем значения для точного округления Общей Суммы
            if (!calcAccrualsForMethodTotal())
                // нет значений
                return;
        }

        Account recipient;
        //byte[] signature = rNote.getSignature();
        boolean creatorIsPerson = creator.isPerson(dcSet, height);

        // возьмем знаки (минус) для создания позиции баланса такой
        Fun.Tuple2<Integer, Integer> signs = Account.getSignsForBalancePos(balancePos);
        long key = signs.a * assetKey;

        // комиссию не проверяем так как она не правильно считается внутри?
        long actionFlags = Transaction.NOT_VALIDATE_FLAG_FEE;

        int result;
        Fun.Tuple4 item;
        BigDecimal amount;
        byte[] signature = new byte[0];
        for (int index = 0; index < filteredAccrualsCount; index++) {

            item = filteredAccruals.get(index);
            recipient = (Account) item.a;

            if (creator.equals(recipient))
                // пропустим себя
                continue;

            amount = (BigDecimal) item.c;

            result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                    key, asset, signs.b > 0 ? amount : amount.negate(), recipient,
                    backward, BigDecimal.ZERO, null, creatorIsPerson, actionFlags);

            if (result != Transaction.VALIDATE_OK) {
                filteredAccruals.set(index, new Fun.Tuple4(item.a, item.b, item.c, new Fun.Tuple2<>(result, "")));
            }
        }
    }

    public int isValid(RSignNote rNote) {

        if (hasAmount()) {
            if (this.assetKey == null || this.assetKey == 0L) {
                errorValue = "Accruals: assetKey == null or ZERO";
                return Transaction.INVALID_ITEM_KEY;
            } else if (this.balancePos < TransactionAmount.ACTION_SEND || this.balancePos > TransactionAmount.ACTION_SPEND) {
                errorValue = "Accruals: balancePos out off range";
                return Transaction.INVALID_BALANCE_POS;
            } else if (this.payMethodValue == null || payMethodValue.signum() == 0) {
                errorValue = "Accruals: payMethodValue == null";
                return Transaction.INVALID_AMOUNT;
            } else if (payMethodValue.signum() < 0) {
                errorValue = "Accruals: payMethodValue < 0";
                return Transaction.INVALID_AMOUNT;
            }
            if (payMethod != PAYMENT_METHOD_TOTAL && useSelfBalance) {
                errorValue = "Accruals: payMethodValue is not by TOTAL && useSelfBalance";
                return Transaction.INVALID_AMOUNT;
            }
            if (payMethod != PAYMENT_METHOD_COEFF && (amountMin != null || amountMax != null)) {
                errorValue = "Accruals: payMethod != PAYMENT_METHOD_COEFF && (amountMin != null || amountMax != null)";
                return Transaction.INVALID_AMOUNT;
            }

        }

        if (hasAssetFilter()) {
            if (this.filterAssetKey == null || this.filterAssetKey == 0L) {
                errorValue = "Accruals: filterAssetKey == null or ZERO";
                return Transaction.INVALID_ITEM_KEY;
            } else if (this.filterBalancePos < TransactionAmount.ACTION_SEND || this.filterBalancePos > TransactionAmount.ACTION_SPEND) {
                errorValue = "Accruals: filterBalancePos";
                return Transaction.INVALID_BALANCE_POS;
            } else if (this.filterBalanceSide < Account.BALANCE_SIDE_DEBIT || this.filterBalanceSide > Account.BALANCE_SIDE_CREDIT) {
                errorValue = "Accruals: filterBalanceSide";
                return Transaction.INVALID_BALANCE_SIDE;
            }
        }

        if (this.filterTXType != 0 && !Transaction.isValidTransactionType(this.filterTXType)) {
            errorValue = "Accruals: filterTXType= " + filterTXType;
            return Transaction.INVALID_TRANSACTION_TYPE;
        }

        if (assetKey != null && filterAssetKey != null
                && assetKey.equals(filterAssetKey)
                && balancePos == filterBalancePos
                && payMethod != PAYMENT_METHOD_ABSOLUTE) {
            // при откате невозможно тогда будет правильно рассчитать - так как съехала общая сумма
            errorValue = "Accruals: assetKey == filterAssetKey && balancePos == filterBalancePos for not ABSOLUTE method";
            return Transaction.INVALID_TRANSFER_TYPE;
        }

        filteredAccrualsCount = makeFilterPayList(rNote, true);

        if (filteredAccrualsCount < 0) {
            // ERROR on make LIST
            return -filteredAccrualsCount;

        } else if (filteredAccrualsCount > 0) {
            height = rNote.getBlockHeight();

            if (payMethod == PAYMENT_METHOD_TOTAL) {
                // просчитаем значения для точного округления Общей Суммы
                if (!calcAccrualsForMethodTotal()) {
                    // ошибка подсчета Общего значения - был взят в учет минус общий
                    errorValue = "Accruals: PayTotal == 0 && payMethod == PAYMENT_METHOD_TOTAL";
                    return Transaction.INVALID_AMOUNT;
                }
            }

            Account recipient = filteredAccruals.get(0).a;
            PublicKeyAccount creator = rNote.getCreator();
            byte[] signature = rNote.getSignature();
            boolean creatorIsPerson = creator.isPerson(dcSet, height);

            // возьмем знаки (минус) для создания позиции баланса такой
            Fun.Tuple2<Integer, Integer> signs = Account.getSignsForBalancePos(balancePos);
            long key = signs.a * assetKey;

            // комиссию не проверяем так как она не правильно считается внутри?
            long actionFlags = Transaction.NOT_VALIDATE_FLAG_FEE;

            BigDecimal totalFeeBG = rNote.getFee();
            int result;
            // проверим как будто всю сумму одному переводим - с учетом комиссии полной
            result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                    key, asset, signs.b > 0 ? totalPay : totalPay.negate(), recipient,
                    backward, totalFeeBG, null, creatorIsPerson, actionFlags);
            if (result != Transaction.VALIDATE_OK) {
                errorValue = "Accruals: totalPay + totalFee = " + totalPay.toPlainString() + " / " + totalFeeBG.toPlainString();
                return result;
            }

            ////////// TODO NEED CHECK ALL
            boolean needCheckAllList = false;
            if (needCheckAllList) {

                for (Fun.Tuple4 item : filteredAccruals) {

                    recipient = (Account) item.a;
                    if (recipient == null)
                        break;

                    if (creator.equals(recipient))
                        // пропустим себя
                        continue;

                    BigDecimal amount = (BigDecimal) item.c;

                    result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                            key, asset, signs.b > 0 ? amount : amount.negate(), recipient,
                            backward, BigDecimal.ZERO, null, creatorIsPerson, actionFlags);

                    if (result != Transaction.VALIDATE_OK) {
                        errorValue = "Accruals: " + amount.toPlainString() + " -> " + recipient.getAddress();
                        return result;
                    }

                }
            }
        }

        return Transaction.VALIDATE_OK;
    }

    public void processBody(Transaction rNote, boolean asOrphan, Block block) {
        PublicKeyAccount creator = rNote.getCreator();

        if (hasAssetFilter()) {
            long absKey = assetKey;

            // возьмем знаки (минус) для создания позиции баланса такой
            Fun.Tuple2<Integer, Integer> signs = Account.getSignsForBalancePos(balancePos);
            Long actionPayKey = signs.a * assetKey;
            boolean isAmountNegate;
            BigDecimal actionPayAmount;
            boolean incomeReverse = balancePos == Account.BALANCE_POS_HOLD;
            boolean reversedBalancesInPosition = asset.isReverseBalancePos(balancePos);
            boolean backwardAction;

            Account recipient;
            for (Fun.Tuple4 item : filteredAccruals) {

                recipient = (Account) item.a;
                if (recipient == null)
                    break;

                actionPayAmount = (BigDecimal) item.c;

                isAmountNegate = actionPayAmount.signum() < 0;
                backwardAction = (reversedBalancesInPosition ^ backward) ^ isAmountNegate;

                if (!asOrphan && block != null) {
                    rNote.addCalculated(block, recipient, absKey, actionPayAmount,
                            asset.viewAssetTypeAction(backwardAction, balancePos, asset.getMaker().equals(creator)));
                }

                if (creator.equals(recipient))
                    // пропустим себя в любом случае - хотя КАлькулейтед оставим для виду
                    continue;

                // сбросим направлени от фильтра
                actionPayAmount = actionPayAmount.abs();
                // зазадим направление от Действия нашего
                actionPayAmount = signs.b > 0 ? actionPayAmount : actionPayAmount.negate();

                TransactionAmount.processAction(dcSet, asOrphan, creator, recipient, balancePos, absKey,
                        asset, actionPayKey, actionPayAmount, backwardAction,
                        incomeReverse);


            }
        }

    }

    public void process(Transaction rNote, Block block) {

        if (filteredAccruals == null) {
            filteredAccrualsCount = makeFilterPayList(rNote, false);
        }

        if (filteredAccrualsCount == 0)
            return;

        if (payMethod == PAYMENT_METHOD_TOTAL) {
            if (!calcAccrualsForMethodTotal())
                // не удалось просчитать значения
                return;
        }

        height = rNote.getBlockHeight();
        asset = dcSet.getItemAssetMap().get(assetKey);

        processBody(rNote, false, block);

    }

    public void orphan(Transaction rNote) {

        if (filteredAccruals == null) {
            filteredAccrualsCount = makeFilterPayList(rNote, false);
        }

        if (filteredAccrualsCount == 0)
            return;

        if (payMethod == PAYMENT_METHOD_TOTAL) {
            if (!calcAccrualsForMethodTotal())
                // не удалось просчитать значения
                return;
        }

        height = rNote.getBlockHeight();
        asset = dcSet.getItemAssetMap().get(assetKey);

        processBody(rNote, true, null);

    }

}
