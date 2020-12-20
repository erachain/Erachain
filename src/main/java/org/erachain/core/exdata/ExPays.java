package org.erachain.core.exdata;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.exdata.exLink.ExLinkAuthor;
import org.erachain.core.exdata.exLink.ExLinkSource;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

/**
 * StandardCharsets.UTF_8 JSON "TM" - template key "PR" - template params
 * "HS" - Hashes "MS" - message
 * <p>
 * PARAMS template:TemplateCls param_keys: [id:text] hashes_Set: [name:hash]
 * mess: message title: Title file_Set: [file Name, ZIP? , file byte[]]
 */

public class ExPays {

    /**
     * flags[1] masks
     */

    private static final byte AMOUNT_FLAG_MASK = 64;
    private static final byte BALANCE_FLAG_MASK = 32;
    private static final byte TXTYPE_FLAG_MASK = 16;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExPays.class);

    /**
     * 0 - version; 1 - flag 1;
     */
    private int flags;

    private final Long assetKey;
    private final int balancePos;
    private final boolean backward;
    private final BigDecimal amountMin;
    private final BigDecimal amountMax;

    private final int payMethod; // 0 - by Total, 1 - by Percent
    private final BigDecimal payMethodValue;

    private Long filterAssetKey;
    private int filterBalancePos;
    private int filterBalanceSide;
    private BigDecimal filterBalanceLessThen;
    private BigDecimal filterBalanceMoreThen;

    private Integer filterTXType;
    private Long filterTXStart;
    private Long filterTXEnd;

    private final Integer filterByPerson; // = gender or all


    public ExPays(int flags, Long assetKey, int balancePos, boolean backward, BigDecimal amountMin, BigDecimal amountMax,
                  int payMethod, BigDecimal payMethodValue, Long filterAssetKey, int filterBalancePos, int filterBalanceSide,
                  BigDecimal filterBalanceLessThen, BigDecimal filterBalanceMoreThen,
                  Integer filterTXType, Long filterTXStart, Long filterTXEnd, Integer filterByPerson) {
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
            this.filterTXStart = filterTXStart;
            this.filterTXEnd = filterTXEnd;
        }

        this.filterByPerson = filterByPerson;
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

        outStream.write(flags);

        byte[] buff;
        if (hasAmount()) {
            outStream.write(Longs.toByteArray(this.assetKey));

            //WRITE AMOUNT MIN SCALE
            outStream.write((byte) this.amountMin.scale());

            //WRITE AMOUNT MIN
            buff = this.amountMin.unscaledValue().toByteArray();
            outStream.write((byte) buff.length);
            outStream.write(buff);

            //WRITE AMOUNT MAX SCALE
            outStream.write((byte) this.amountMax.scale());

            //WRITE AMOUNT MAX
            buff = this.amountMax.unscaledValue().toByteArray();
            outStream.write((byte) buff.length);
            outStream.write(buff);

            buff = new byte[]{(byte) balancePos, (byte) (backward ? 1 : 0), (byte) payMethod};
            outStream.write(buff);

            //WRITE AMOUNT MAX SCALE
            outStream.write((byte) this.payMethodValue.scale());

            //WRITE AMOUNT MAX
            buff = this.payMethodValue.unscaledValue().toByteArray();
            outStream.write((byte) buff.length);
            outStream.write(buff);
        }

        if (hasAssetFilter()) {
            outStream.write(Longs.toByteArray(this.filterAssetKey));
            buff = new byte[]{(byte) filterBalancePos, (byte) filterBalancePos};
            outStream.write(buff);

            outStream.write((byte) this.filterBalanceLessThen.scale());
            buff = this.filterBalanceLessThen.unscaledValue().toByteArray();
            outStream.write((byte) buff.length);
            outStream.write(buff);

            outStream.write((byte) this.filterBalanceMoreThen.scale());
            buff = this.filterBalanceMoreThen.unscaledValue().toByteArray();
            outStream.write((byte) buff.length);
            outStream.write(buff);
        }

        if (hasTXTypeFilter()) {
            outStream.write(new byte[]{(byte) (int) filterTXType});
            outStream.write(Longs.toByteArray(this.filterTXStart));
            outStream.write(Longs.toByteArray(this.filterTXEnd));
        }

        outStream.write(new byte[]{(byte) (int) filterByPerson});

    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ExPays parse(byte[] data) throws Exception {

        //CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < 10) {
            throw new Exception("Data does not match block length " + data.length);
        }

        int position = 0;
        int scale;
        int len;

        int flags = Ints.fromByteArray(Arrays.copyOfRange(data, position, Integer.BYTES));
        position += Integer.BYTES;

        Long assetKey = null;
        int balancePos;
        boolean backward;
        BigDecimal amountMin = null;
        BigDecimal amountMax = null;
        int payMethod;
        BigDecimal payMethodValue = null;

        if ((flags & AMOUNT_FLAG_MASK) != 0) {
            assetKey = Longs.fromByteArray(Arrays.copyOfRange(data, position, Long.BYTES));
            position += Long.BYTES;

            scale = data[position++];
            len = data[position++];
            amountMin = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, len)), scale);
            position += len;

            scale = data[position++];
            len = data[position++];
            amountMax = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, len)), scale);
            position += len;

            balancePos = data[position++];
            backward = data[position++] > 0;
            payMethod = data[position++];
            ;

            scale = data[position++];
            len = data[position++];
            payMethodValue = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, len)), scale);
            position += len;

        }

        Long filterAssetKey = null;
        int filterBalancePos;
        int filterBalanceSide;
        BigDecimal filterBalanceLessThen = null;
        BigDecimal filterBalanceMoreThen = null;

        if ((flags & BALANCE_FLAG_MASK) != 0) {
            filterAssetKey = Longs.fromByteArray(Arrays.copyOfRange(data, position, Long.BYTES));
            position += Long.BYTES;

            filterBalancePos = data[position++];
            filterBalanceSide = data[position++];

            scale = data[position++];
            len = data[position++];
            filterBalanceLessThen = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, len)), scale);
            position += len;

            scale = data[position++];
            len = data[position++];
            filterBalanceMoreThen = new BigDecimal(new BigInteger(Arrays.copyOfRange(data, position, len)), scale);
            position += len;

        }

        int filterTXType;
        Long filterTXStart = null;
        Long filterTXEnd = null;

        if ((flags & TXTYPE_FLAG_MASK) != 0) {

            filterTXType = data[position++];

            filterTXStart = Longs.fromByteArray(Arrays.copyOfRange(data, position, Long.BYTES));
            position += Long.BYTES;

            filterTXEnd = Longs.fromByteArray(Arrays.copyOfRange(data, position, Long.BYTES));
            position += Long.BYTES;

        }

        int filterByPerson = data[position++];

        return new ExPays(flags, assetKey, balancePos, backward, amountMin, amountMax,
                payMethod, payMethodValue, filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceLessThen, filterBalanceMoreThen,
                filterTXType, filterTXStart, filterTXEnd, filterByPerson);
    }

    public static byte[] make(int flags, Long assetKey, int balancePos, boolean backward, BigDecimal amountMin, BigDecimal amountMax,
                              int payMethod, BigDecimal payMethodValue, Long filterAssetKey, int filterBalancePos, int filterBalanceSide,
                              BigDecimal filterBalanceLessThen, BigDecimal filterBalanceMoreThen,
                              Integer filterTXType, Long filterTXStart, Long filterTXEnd, Integer filterByPerson)
            throws Exception {


        return new ExPays(flags, assetKey, balancePos, backward, amountMin, amountMax,
                payMethod, payMethodValue, filterAssetKey, filterBalancePos, filterBalanceSide,
                filterBalanceLessThen, filterBalanceMoreThen,
                filterTXType, filterTXStart, filterTXEnd, filterByPerson).toByte();

    }

    /**
     * Version 2 maker for BlockExplorer
     */
    public void makeJSONforHTML(Map output,
                                int blockNo, int seqNo, JSONObject langObj) {

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

        return Transaction.VALIDATE_OK;
    }

    public void process(Transaction transaction) {
        if (exLink != null)
            exLink.process(transaction);

        if (authors != null) {
            for (ExLinkAuthor author : authors) {
                author.process(transaction);
            }
        }

        if (sources != null) {
            for (ExLinkSource source : sources) {
                source.process(transaction);
            }
        }

    }

    public void orphan(Transaction transaction) {
        if (exLink != null)
            exLink.orphan(transaction);

        if (authors != null) {
            for (ExLinkAuthor author : authors) {
                author.orphan(transaction);
            }
        }

        if (sources != null) {
            for (ExLinkSource source : sources) {
                source.orphan(transaction);
            }
        }
    }

}
