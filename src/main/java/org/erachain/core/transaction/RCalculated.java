package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * ## typeBytes
 * 0 - record type
 * 1 - record version
 * 2 - property 1
 * 3 = property 2
 * <br>
 * flags - is reference to Parent transaction
 */
public class RCalculated extends TransactionAmount {

    private static final byte TYPE_ID = (byte) Transaction.CALCULATED_TRANSACTION;
    private static final String NAME_ID = "_protocol_";
    protected String message;

    public RCalculated(byte[] typeBytes, Account recipient, long key,
                       BigDecimal amount, String message, long txReference, long seqNo) {
        super(typeBytes, NAME_ID, null, null, null, (byte) 0, recipient, amount, key, 0L, txReference);

        this.message = message;
        if (message == null)
            this.message = "";

        if (seqNo > 0)
            this.setHeightSeq(seqNo);

    }

    public RCalculated(Account recipient, long key,
                       BigDecimal amount, String message, long txReference, long seqNo) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, recipient, key, amount, message, txReference, seqNo);
    }

    // GETTERS/SETTERS

    @Override
    public int hashCode() {
        return Long.hashCode(dbRef);
    }

    @Override
    public boolean equals(Object transaction) {
        if (transaction instanceof RCalculated)
            return dbRef == ((Transaction) transaction).getDBRef();
        return false;
    }

    @Override
    public Long getTimestamp() {
        if (this.timestamp > 0) {
            return this.timestamp;
        }
        if (this.height > 0) {
            return (this.timestamp = Controller.getInstance().blockChain.getTimestamp(this.height) + seqNo);
        } else
            return 0L;
    }

    @Override
    public String viewTypeName() {
        return NAME_ID;
    }

    @Override
    public String viewSignature() {
        return "calculated_" + Transaction.viewDBRef(flags) + ":" + viewHeightSeq();
    }

    @Override
    public String viewSubTypeName() {
        return "";
    }

    @Override
    public String viewFullTypeName() {
        return viewTypeName();
    }

    @Override
    public String viewActionType() {
        return "";
    }

    @Override
    public boolean hasPublicText() {
        return false;
    }

    @Override
    public String getTitle() {
        return this.message + " (@" + Transaction.viewDBRef(flags) + ")";
    }

    @Override
    public String getTitle(JSONObject langObj) {
        return Lang.T(this.message, langObj) + " (@" + Transaction.viewDBRef(flags) + ")";
    }

    public String getMessage() {
        return this.message;
    }

    // PARSE/CONVERT

    public static Transaction Parse(byte[] data) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ REFERENCE
        byte[] flagsBytes = Arrays.copyOfRange(data, position, position + FLAGS_LENGTH);
        long txReference = Longs.fromByteArray(flagsBytes);
        position += FLAGS_LENGTH;

        long seqNo = 0;
        //READ SEQ_NO
        byte[] seqNoBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
        seqNo = Longs.fromByteArray(seqNoBytes);
        position += TIMESTAMP_LENGTH;

        ///////////////// LOAD

        // READ RECIPIENT
        byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
        Account recipient = new Account(recipientBytes);
        position += RECIPIENT_LENGTH;

        long key = 0;
        BigDecimal amount = null;
        
        if (typeBytes[2] >= 0) {
            // IF here is AMOUNT

            // READ KEY
            byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
            key = Longs.fromByteArray(keyBytes);
            position += KEY_LENGTH;

            // READ AMOUNT
            byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
            amount = new BigDecimal(new BigInteger(amountBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
            position += AMOUNT_LENGTH;

            // CHECK ACCURACY of AMOUNT
            if (typeBytes[3] != -1) {
                // not use old FLAG from vers 2
                int accuracy = typeBytes[3] & SCALE_MASK;
                if (accuracy > 0) {
                    if (accuracy >= TransactionAmount.SCALE_MASK_HALF) {
                        accuracy -= TransactionAmount.SCALE_MASK + 1;
                    }

                    // RESCALE AMOUNT
                    amount = amount.scaleByPowerOfTen(-accuracy);
                }
            }

        }

        // MESSAGE LEN
        Short shortLen = Shorts.fromByteArray(Arrays.copyOfRange(data, position, position + Short.BYTES));
        position += Short.BYTES;
        int messageLen = Short.toUnsignedInt(shortLen);

        // MESSAGE
        byte[] messageBytes = Arrays.copyOfRange(data, position, position + messageLen);
        String message = new String(messageBytes, StandardCharsets.UTF_8);
        position += messageLen;

        return new RCalculated(typeBytes, recipient, key, amount, message, txReference, seqNo);

    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        // GET BASE
        JSONObject transaction = this.getJsonBase();

        transaction.put("timestamp", Controller.getInstance().blockChain.getTimestamp(this.height));

        if (message.length() > 0) {
            transaction.put("message", this.message);
        }

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = new byte[0];

        // WRITE TYPE
        data = Bytes.concat(data, this.typeBytes);

        // WRITE FLAGS
        byte[] flagsBytes = Longs.toByteArray(this.flags);
        data = Bytes.concat(data, flagsBytes);

        byte[] dbRefBytes = Longs.toByteArray(this.dbRef);
        data = Bytes.concat(data, dbRefBytes);

        ////////// LOAD

        // WRITE RECIPIENT
        data = Bytes.concat(data, this.recipient.getAddressBytes());

        if (this.amount != null) {

            // WRITE KEY
            byte[] keyBytes = Longs.toByteArray(this.key);
            keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
            data = Bytes.concat(data, keyBytes);

            // CALCULATE ACCURACY of AMMOUNT
            int different_scale = this.amount.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
            BigDecimal amountBase;
            if (different_scale != 0) {
                // RESCALE AMOUNT
                amountBase = this.amount.scaleByPowerOfTen(different_scale);
                if (different_scale < 0)
                    different_scale += TransactionAmount.SCALE_MASK + 1;

                // WRITE ACCURACY of AMMOUNT
                data[3] = (byte) (data[3] | different_scale);
            } else {
                amountBase = this.amount;
            }

            // WRITE AMOUNT
            byte[] amountBytes = Longs.toByteArray(amountBase.unscaledValue().longValue());
            amountBytes = Bytes.ensureCapacity(amountBytes, AMOUNT_LENGTH, 0);
            data = Bytes.concat(data, amountBytes);
        }

        ///////////////////
        // WRITE HEAD
        byte[] messageBytes = this.message.getBytes(StandardCharsets.UTF_8);
        // HEAD SIZE
        data = Bytes.concat(data, Shorts.toByteArray((short) messageBytes.length));
        // HEAD
        data = Bytes.concat(data, messageBytes);

        return data;
    }

    //PROCESS/ORPHAN

    @Override
    public void processBody(Block block, int forDeal) {
        // ввобщето тут вызов ошибки должен быть
        Long error = null;
        error++;
    }

    @Override
    public void orphanBody(Block block, int forDeal) {
        // ввобщето тут вызов ошибки должен быть
        Long error = null;
        error++;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {

        return TYPE_LENGTH + FLAGS_LENGTH + TIMESTAMP_LENGTH
                /// LOAD
                + RECIPIENT_LENGTH
                + (this.amount == null ? 0 : AMOUNT_LENGTH + KEY_LENGTH)
                + Short.BYTES + message.getBytes(StandardCharsets.UTF_8).length;
    }

}