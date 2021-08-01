package org.erachain.core.exdata.exActions;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Result: recipient + balance + accrual + Validate_Result {code, mess}
 */

public class ExPays extends ExAction<List<Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>>>> {

    public static final byte BASE_LENGTH = 4 + 3;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:00");

    public static final int MAX_COUNT = 1 << 17;
    private static final byte AMOUNT_FLAG_MASK = -128;
    private static final byte AMOUNT_MIN_FLAG_MASK = 64;
    private static final byte AMOUNT_MAX_FLAG_MASK = 32;
    private static final byte BALANCE_FLAG_MASK = 16;
    private static final byte BALANCE_AMOUNT_MIN_FLAG_MASK = 8;
    private static final byte BALANCE_AMOUNT_MAX_FLAG_MASK = 4;
    private static final byte ACTIVE_START_FLAG_MASK = 2;
    private static final byte ACTIVE_END_FLAG_MASK = 1;

    public static final byte PAYMENT_METHOD_TOTAL = 0; // by TOTAL
    public static final byte PAYMENT_METHOD_COEFF = 1; // by coefficient
    public static final byte PAYMENT_METHOD_ABSOLUTE = 2; // by ABSOLUTE VALUE

    public static final byte FILTER_PERSON_NONE = 0;
    public static final byte FILTER_PERSON_ONLY = 1;
    public static final byte FILTER_PERSON_ONLY_MAN = 2;
    public static final byte FILTER_PERSON_ONLY_WOMAN = 3;

    private static final byte NOT_FILTER_PERSONS = -1; //
    private static final byte NOT_FILTER_GENDER = -2; //

    private static final Logger LOGGER = LoggerFactory.getLogger(ExPays.class);

    public static final String FILTER_PERS_ALL = "All";
    public static final String FILTER_PERS_ONLY = "Only certified addresses";
    public static final String FILTER_PERS_MAN = "Only for Men";
    public static final String FILTER_PERS_WOMAN = "Only for Women";

    /**
     * 0 - version; 1..3 - flags;
     */
    private int flags; // 4

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
    private Long filterTimeStart; // 44 - in msec
    public Long filterTimeEnd; // 52 - in msec

    private final int filterByGender; // 53 = gender or all
    public boolean useSelfBalance; // 54

    /////////////////
    int payAction;
    AssetCls filterAsset;
    private int resultsCount;
    private long totalFeeBytes;
    private long iteratorUses;
    private int maxIndex;
    private BigDecimal maxBal;

    public String errorValue;


    /**
     * make FLAGS internal
     *
     * @param flags
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
     * @param filterTimeStart
     * @param filterTimeEnd
     * @param filterByGender
     * @param useSelfBalance
     */
    public ExPays(int flags, Long assetKey, int balancePos, boolean backward, int payMethod, BigDecimal payMethodValue, BigDecimal amountMin, BigDecimal amountMax,
                  Long filterAssetKey, int filterBalancePos, int filterBalanceSide,
                  BigDecimal filterBalanceMIN, BigDecimal filterBalanceMAX,
                  int filterTXType, Long filterTimeStart, Long filterTimeEnd,
                  int filterByGender, boolean useSelfBalance) {

        super(FILTERED_ACCRUALS_TYPE);

        this.flags = flags;

        if (true || // запретить без действий по активу - так как это не письма явно - письма отдельно!
                assetKey != null && assetKey != 0L) {
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

        if (true || // запретить без фильтрации по активу - так как это не письма явно - письма отдельно!
                filterAssetKey != null && filterAssetKey != 0L) {
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

        if (filterTimeStart != null) {
            this.flags |= ACTIVE_START_FLAG_MASK;
            this.filterTimeStart = filterTimeStart;
        }
        if (filterTimeEnd != null) {
            this.flags |= ACTIVE_END_FLAG_MASK;
            this.filterTimeEnd = filterTimeEnd;
        }

        this.filterByGender = filterByGender;
        this.useSelfBalance = useSelfBalance;
    }

    public ExPays(int flags, Long assetKey, int balancePos, boolean backward, int payMethod, BigDecimal payMethodValue, BigDecimal amountMin, BigDecimal amountMax,
                  Long filterAssetKey, int filterBalancePos, int filterBalanceSide,
                  BigDecimal filterBalanceMIN, BigDecimal filterBalanceMAX,
                  int filterTXType, Long filterTimeStart, Long filterTimeEnd,
                  int filterByGender, boolean useSelfBalance,
                  int resultsCount, BigDecimal totalPay, long totalFeeBytes) {
        this(flags, assetKey, balancePos, backward, payMethod, payMethodValue, amountMin, amountMax,
                filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceMIN, filterBalanceMAX,
                filterTXType, filterTimeStart, filterTimeEnd,
                filterByGender, useSelfBalance);

        this.resultsCount = resultsCount;
        this.totalPay = totalPay;
        this.totalFeeBytes = totalFeeBytes;
    }

    public int getResultsCount() {
        return resultsCount;
    }

    public long getTotalFeeBytes() {
        return totalFeeBytes;
    }

    public static String viewPayMethod(int mode) {
        return "PAY_METHOD_" + mode;
    }

    public static String viewFilterPersMode(int mode) {
        switch (mode) {
            case 0:
                return "All";
            case 1:
                return "Only certified addresses";
            case 2:
                return "Only for Men";
            case 3:
                return "Only for Women";
        }
        return "--";
    }

    public void calcTotalFeeBytes() {
        totalFeeBytes = 10L * resultsCount + 5L * iteratorUses;
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


    /**
     * Используется ли Итераторы дополнительные для вычисления активности? Нужно для вычисления Комиссии
     *
     * @return
     */
    public boolean hasFilterActives() {
        return filterTXType != 0 || hasTXTypeFilterActiveStart() || hasTXTypeFilterActiveEnd();
    }

    public boolean hasTXTypeFilterActiveStart() {
        return (this.flags & ACTIVE_START_FLAG_MASK) != 0;
    }

    public boolean hasTXTypeFilterActiveEnd() {
        return (this.flags & ACTIVE_END_FLAG_MASK) != 0;
    }

    @Override
    public String viewResults() {
        return "results";
    }

    @Override
    public void updateItemsKeys(List listTags) {
        if (hasAmount()) {
            listTags.add(new Object[]{ItemCls.ASSET_TYPE, getAssetKey(), getAsset().getTags()});
        }
    }

    public void setDC(DCSet dcSet) {
        if (this.dcSet == null || !this.dcSet.equals(dcSet)) {
            this.dcSet = dcSet;
            if (hasAmount()) {
                this.asset = this.dcSet.getItemAssetMap().get(this.assetKey);
            }
        }
    }

    public int parseDBData(byte[] dbData, int position) {
        resultsCount = Ints.fromByteArray(Arrays.copyOfRange(dbData, position, position + Integer.BYTES));
        position += Integer.BYTES;

        totalFeeBytes = Longs.fromByteArray(Arrays.copyOfRange(dbData, position, position + Long.BYTES));
        position += Long.BYTES;

        int len = dbData[position++];
        if (len == 0) {
            totalPay = null;
        } else {
            int scale = dbData[position++];
            totalPay = new BigDecimal(new BigInteger(Arrays.copyOfRange(dbData, position, position + len)), scale);
        }

        position += len;

        return position;

    }

    public int getLengthDBData() {
        return Integer.BYTES + Long.BYTES
                + (totalPay == null ? 1 : 2 + totalPay.unscaledValue().toByteArray().length);
    }

    public byte[] getDBdata() {

        byte[] buff;
        byte[] dbData;

        if (totalPay == null) {
            dbData = new byte[Integer.BYTES + Long.BYTES + 1];
            buff = null;
        } else {
            buff = this.totalPay.unscaledValue().toByteArray();
            dbData = new byte[Integer.BYTES + Long.BYTES + 2 + buff.length];
        }

        int pos = 0;
        System.arraycopy(Ints.toByteArray(resultsCount), 0, dbData, pos, Integer.BYTES);
        pos += Integer.BYTES;
        System.arraycopy(Longs.toByteArray(totalFeeBytes), 0, dbData, pos, Long.BYTES);
        pos += Long.BYTES;
        if (totalPay == null) {
            dbData[pos++] = (byte) 0;
            return dbData;
        }

        dbData[pos++] = (byte) buff.length;

        dbData[pos++] = (byte) this.totalPay.scale();
        System.arraycopy(buff, 0, dbData, pos, buff.length);

        return dbData;

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
            outStream.write(Longs.toByteArray(this.filterTimeStart));
        }
        if (hasTXTypeFilterActiveEnd()) {
            outStream.write(Longs.toByteArray(this.filterTimeEnd));
        }

        outStream.write(new byte[]{(byte) filterTXType, (byte) filterByGender, (byte) (useSelfBalance ? 1 : 0)});

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
     * @param filterTimeStartStr    'yyyy-MM-dd hh:mm:00' or Timestamp[sec] or SeqNo: 123-1
     * @param filterTimeXEndStr     'yyyy-MM-dd hh:mm:00' or Timestamp[sec] or SeqNo: 123-1
     * @param filterByPerson
     * @param selfPay
     * @return
     */
    public static Fun.Tuple2<ExAction, String> make(Long assetKey, int balancePos, boolean backward,
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
                    Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                    filterTimeStart = timestamp.getTime();
                } catch (Exception e) {
                    filterTimeStart = Transaction.parseDBRefSeqNo(filterTimeStartStr);
                    if (filterTimeStart == null) {
                        try {
                            filterTimeStart = Long.parseLong(filterTimeStartStr) * 1000L;
                        } catch (Exception e1) {
                        }
                    } else {
                        filterTimeStart = Controller.getInstance().blockChain.getTimestampByDBRef((filterTimeStart));
                    }
                }
            }

            ++steep;
            if (filterTimeXEndStr == null || filterTimeXEndStr.isEmpty()) {
                filterTimeEnd = null;
            } else {
                try {
                    Date parsedDate = DATE_FORMAT.parse(filterTimeXEndStr);
                    Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                    filterTimeEnd = timestamp.getTime();
                } catch (Exception e) {
                    filterTimeEnd = Transaction.parseDBRefSeqNo(filterTimeXEndStr);
                    if (filterTimeEnd == null) {
                        try {
                            filterTimeEnd = Long.parseLong(filterTimeXEndStr) * 1000L;
                        } catch (Exception e1) {
                        }
                    } else {
                        filterTimeEnd = Controller.getInstance().blockChain.getTimestampByDBRef(filterTimeEnd);
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
        return new Fun.Tuple2<>(new ExPays(flags, assetKey, balancePos, backward, payMethod, payMethodValueBG, amountMinBG, amountMaxBG,
                filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceMoreThenBG, filterBalanceLessThenBG,
                filterTXType, filterTimeStart, filterTimeEnd,
                filterByPerson, selfPay), null);

    }

    public static Fun.Tuple2<ExAction, String> parseJSON_local(JSONObject jsonObject) throws Exception {
        long assetKey = Long.valueOf(jsonObject.getOrDefault("assetKey", 0L).toString());
        int position = Integer.valueOf(jsonObject.getOrDefault("position", 1).toString());
        boolean backward = Boolean.valueOf((boolean) jsonObject.getOrDefault("backward", false));

        int payMethod = Integer.valueOf(jsonObject.getOrDefault("method", 1).toString());
        String value = (String) jsonObject.get("methodValue");
        String amountMin = (String) jsonObject.get("amountMin");
        String amountMax = (String) jsonObject.get("amountMax");

        long filterAssetKey = Long.valueOf(jsonObject.getOrDefault("filterAssetKey", 0l).toString());
        int filterPos = Integer.valueOf(jsonObject.getOrDefault("filterBalPos", 1).toString());
        int filterSide = Integer.valueOf(jsonObject.getOrDefault("filterBalSide", 1).toString());

        int filterTXType = Integer.valueOf(jsonObject.getOrDefault("filterTXType", 1).toString());
        String filterGreatEqual = (String) jsonObject.get("filterGreatEqual");
        String filterLessEqual = (String) jsonObject.get("filterLessEqual");
        String filterTimeStart = (String) jsonObject.get("activeAfter");
        String filterTimeEnd = (String) jsonObject.get("activeBefore");

        int filterPerson = Integer.valueOf(jsonObject.getOrDefault("filterPerson", 0).toString());
        boolean selfUse = Boolean.valueOf((boolean) jsonObject.getOrDefault("selfUse", false));

        return make(assetKey, position, backward, payMethod, value,
                amountMin, amountMax, filterAssetKey, filterPos, filterSide,
                filterGreatEqual, filterLessEqual,
                filterTXType, filterTimeStart, filterTimeEnd,
                filterPerson, selfUse);
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

        if (resultsCount > 0) {
            json.put("Label_Counter", Lang.T("Counter", langObj));
            json.put("Label_Total_Amount", Lang.T("Total Amount", langObj));
            json.put("Label_Additional_Fee", Lang.T("Additional Fee", langObj));

        }

        json.put("payMethodName", Lang.T(ExPays.viewPayMethod(payMethod), langObj));
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

        if (resultsCount > 0) {
            toJson.put("resultsCount", resultsCount);
            toJson.put("totalPay", totalPay.toPlainString());
            toJson.put("totalFeeBytes", totalFeeBytes);
            toJson.put("totalFee", BlockChain.feeBG(totalFeeBytes).toPlainString());
        }

        return toJson;
    }

    public String getInfoHTML() {

        String out = "<h3>" + Lang.T("Accruals") + "</h3>";
        out += Lang.T("Asset") + ": <b>" + asset.getName() + "<br>";
        out += Lang.T("Count # кол-во") + ": <b>" + resultsCount
                + "</b>, " + Lang.T("Additional Fee") + ": <b>" + BlockChain.feeBG(totalFeeBytes)
                + "</b>, " + Lang.T("Total") + ": <b>" + totalPay;

        return out;
    }

    @Override
    public int preProcess(int height, Account creator, boolean andPreValid) {
        makeFilterPayList(dcSet, height, asset, creator, andPreValid);
        if (resultCode != Transaction.VALIDATE_OK) {
            return resultCode;
        }

        if (payMethod == PAYMENT_METHOD_TOTAL) {
            calcAccrualsForMethodTotal();
        }
        return resultCode;
    }

    public boolean calcAccrualsForMethodTotal() {

        if (resultsCount == 0)
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
                Order.calcPriceScale(totalBalances, scale, 3), RoundingMode.HALF_DOWN);
        Fun.Tuple4 item;
        BigDecimal accrual;
        maxBal = BigDecimal.ZERO;
        for (int index = 0; index < resultsCount; index++) {
            item = results.get(index);
            accrual = (BigDecimal) item.b;
            accrual = accrual.multiply(coefficient).setScale(scale, RoundingMode.DOWN);

            totalPay = totalPay.add(accrual);
            results.set(index, new Fun.Tuple4(item.a, item.b, accrual, item.d));

            if (maxBal.compareTo(accrual.abs()) < 0) {
                // запомним максимальное для скидывания остатка
                maxBal = accrual.abs();
                maxIndex = index;
            }
        }

        BigDecimal totalDiff = payMethodValue.subtract(totalPay);
        if (totalDiff.signum() != 0) {
            // есть нераспределенный остаток
            Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>> maxItem = results.get(maxIndex);
            results.set(maxIndex, new Fun.Tuple4(maxItem.a, maxItem.b, maxItem.c.add(totalDiff), maxItem.d));

            totalPay = payMethodValue;
        }

        return true;
    }

    /**
     * Тут именно делает список проходя по Итератору балансы заданного актива. Однако после надо еще запустить calcAccrualsForMethodTotal
     *
     * @param dcSet
     * @param height
     * @param asset
     * @param creator
     * @param andValidate
     * @return
     */
    public void makeFilterPayList(DCSet dcSet, int height, AssetCls asset, Account creator, boolean andValidate) {

        results = new ArrayList<>();

        iteratorUses = 0L;
        resultsCount = 0;

        errorValue = null;
        resultCode = Transaction.VALIDATE_OK;

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

        resultsCount = 0;

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

                // IF send from PERSON to ANONYMOUS
                if (hasAmount && andValidate && !TransactionAmount.isValidPersonProtect(dcSet, height, recipient,
                        creatorIsPerson, assetKey, balancePos,
                        asset)) {
                    resultCode = Transaction.RECEIVER_NOT_PERSONALIZED;
                    errorValue = null;
                    results.add(new Fun.Tuple4(recipient, balance, accrual, new Fun.Tuple2<>(resultCode, errorValue)));
                } else {

                    // не проверяем на 0 - так это может быть рассылка писем всем
                    results.add(new Fun.Tuple4(recipient, balance, accrual, null));
                }

                resultsCount++;
                if (andValidate && resultsCount > MAX_COUNT) {
                    errorValue = "" + MAX_COUNT;
                    resultCode = Transaction.INVALID_MAX_ITEMS_COUNT;
                    return;
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
                totalPay = payMethodValue.multiply(new BigDecimal(resultsCount));
                break;
            default:
                totalPay = totalBalances;
        }

        calcTotalFeeBytes();
        return;

    }

    public int isValid(RSignNote rNote) {

        if (hasAmount()) {
            if (this.assetKey == 0L) {
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

        if (assetKey != 0 && filterAssetKey != null
                && assetKey == filterAssetKey
                && balancePos == filterBalancePos
                && payMethod != PAYMENT_METHOD_ABSOLUTE) {
            // при откате невозможно тогда будет правильно рассчитать - так как съехала общая сумма
            errorValue = "Accruals: assetKey == filterAssetKey && balancePos == filterBalancePos for not ABSOLUTE method";
            return Transaction.INVALID_TRANSFER_TYPE;
        }

        makeFilterPayList(dcSet, height, asset, rNote.getCreator(), true);

        if (resultsCount < 0) {
            // ERROR on make LIST
            return -resultsCount;

        } else if (resultsCount > 0) {
            height = rNote.getBlockHeight();

            if (payMethod == PAYMENT_METHOD_TOTAL) {
                // просчитаем значения для точного округления Общей Суммы
                if (!calcAccrualsForMethodTotal()) {
                    // ошибка подсчета Общего значения - был взят в учет минус общий
                    errorValue = "Accruals: PayTotal == 0 && payMethod == PAYMENT_METHOD_TOTAL";
                    return Transaction.INVALID_AMOUNT;
                }
            }

            Account recipient = results.get(0).a;
            PublicKeyAccount creator = rNote.getCreator();
            byte[] signature = rNote.getSignature();
            boolean creatorIsPerson = creator.isPerson(dcSet, height);

            // возьмем знаки (минус) для создания позиции баланса такой
            Fun.Tuple2<Integer, Integer> signs = Account.getSignsForBalancePos(balancePos);
            long key = signs.a * assetKey;

            // комиссию не проверяем так как она не правильно считается внутри?
            long actionFlags = Transaction.NOT_VALIDATE_FLAG_FEE;

            BigDecimal totalFeeBG = rNote.getFee();
            Fun.Tuple2<Integer, String> result;
            // проверим как будто всю сумму одному переводим - с учетом комиссии полной
            if (backward) {
                // 1. balancePos == Account.BALANCE_POS_DEBT
                // тут надо делать проверку на общую сумму по списку долгов у получателей, подсчитав ее заранее - что накладно
                // иначе она не пройдет - так как у одного адресата нет того долга
                // 2. balancePos == Account.BALANCE_POS_HOLD
                // тут вообще нельзя проверку общую делать
            } else {
                result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                        key, asset, signs.b > 0 ? totalPay : totalPay.negate(), recipient,
                        backward, totalFeeBG, null, creatorIsPerson, actionFlags, rNote.getTimestamp());
                if (result.a != Transaction.VALIDATE_OK) {
                    errorValue = result.b + " - Accruals: totalPay + totalFee = " + totalPay.toPlainString() + " / " + totalFeeBG.toPlainString();
                    return result.a;
                }
            }

            ////////// TODO NEED CHECK ALL
            boolean needCheckAllList = false;
            if (needCheckAllList) {

                for (Fun.Tuple4 item : results) {

                    recipient = (Account) item.a;
                    if (recipient == null)
                        break;

                    if (creator.equals(recipient))
                        // пропустим себя
                        continue;

                    BigDecimal amount = (BigDecimal) item.c;

                    result = TransactionAmount.isValidAction(dcSet, height, creator, signature,
                            key, asset, signs.b > 0 ? amount : amount.negate(), recipient,
                            backward, BigDecimal.ZERO, null, creatorIsPerson, actionFlags, rNote.getTimestamp());

                    if (result.a != Transaction.VALIDATE_OK) {
                        errorValue = result.b + " - Accruals: " + amount.toPlainString() + " -> " + recipient.getAddress();
                        return result.a;
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
            for (Fun.Tuple4 item : results) {

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

        if (results == null) {
            resultsCount = preProcess(rNote);
        }

        if (resultsCount == 0)
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

        if (results == null) {
            resultsCount = preProcess(rNote);
        }

        if (resultsCount == 0)
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
