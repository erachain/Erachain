package org.erachain.core.transCalculated;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**

## typeBytes
0 - record type
1 - record version
2 - property 1
3 = property 2

## version 0
typeBytes[2] = -128 if NO AMOUNT
typeBytes[3] = -128 if NO DATA

## version 1
typeBytes[1] (version) = 1 - if backward - CONFISCATE CREDIT

## version 2
typeBytes[1] - version

#### PROPERTY 1
typeBytes[2].0 = -128 if NO AMOUNT
typeBytes[2].1 = -64 if backward - CONFISCATE CREDIT

#### PROPERTY 2
typeBytes[3].0 = -128 if NO DATA

## version 3

#### PROPERTY 1
typeBytes[2].0 = -128 if NO AMOUNT - check sign
typeBytes[2].1 = 64 if backward (CONFISCATE CREDIT, ...)

#### PROPERTY 2
typeBytes[3].0 = -128 if NO DATA - check sign = '10000000' = Integer.toBinaryString(128) - assertEquals((byte)128, (byte)-128);
typeBytes[3].3-7 = point accuracy: -16..16 = BYTE - 16

 */

public class CSend extends CalculatedAmount {

    public static final int NO_DATA_MASK = 128; // 0x10000000
    public static final int MAX_DATA_VIEW = 64;
    //private static int position;
    protected static final int BASE_LENGTH = Transaction.IS_TEXT_LENGTH + Transaction.ENCRYPTED_LENGTH + Transaction.DATA_SIZE_LENGTH;
    private static final byte TYPE_ID = (byte) Transaction.SEND_ASSET_TRANSACTION;
    private static final String NAME_ID = "Send";
    protected String head;
    protected byte[] data;
    protected byte[] encrypted;
    protected byte[] isText;

    public CSend(byte[] typeBytes, int blockNo, int transNo, long seqNo,
                 Account sender, Account recipient, long assetKey, BigDecimal amount,
                 String head, byte[] data, byte[] isText, byte[] encrypted) {
        super(typeBytes, NAME_ID, blockNo, transNo, seqNo, sender, recipient, assetKey, amount);

        this.head = head;
        if (head == null)
            this.head = "";

        if (data == null || data.length == 0) {
            // set version byte
            typeBytes[3] = (byte) (typeBytes[3] | NO_DATA_MASK);
        } else {
            this.data = data;
            this.encrypted = encrypted;
            this.isText = isText;
        }
    }

    public CSend(byte version, byte property1, byte property2, int blockNo, int transNo, long seqNo,
                 Account sender, Account recipient, long assetKey, BigDecimal amount,
                 String head, byte[] data, byte[] isText, byte[] encrypted
            ) {
        this(new byte[]{TYPE_ID, version, property1, property2}, blockNo, transNo, seqNo,
                sender, recipient, assetKey, amount, head, data, isText, encrypted);
    }

    ////////////////////////// SHOR -text DATA
    public CSend(byte[] typeBytes, int blockNo, int transNo, long seqNo,
                 Account sender, Account recipient, long assetKey, BigDecimal amount) {
        super(typeBytes, NAME_ID, blockNo, transNo, seqNo, sender, recipient, assetKey, amount);
        this.head = "";
        typeBytes[3] = (byte) (typeBytes[3] | (byte) -128);

    }

    // GETTERS/SETTERS

    public static Calculated Parse(byte[] data) throws Exception {

        //
        // CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < BASE_LENGTH) {
            throw new Exception("Data does not match RAW length " + data.length + " < " + BASE_LENGTH);
        }

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        // READ BLOCK NO
        byte[] bytes = Arrays.copyOfRange(data, position, position + BLOCK_NO_LENGTH);
        int blockNo = Ints.fromByteArray(bytes);
        position += BLOCK_NO_LENGTH;

        // READ TRANS NO
        bytes = Arrays.copyOfRange(data, position, position + TRANS_NO_LENGTH);
        int transNo = Ints.fromByteArray(bytes);
        position += TRANS_NO_LENGTH;

        // READ SEQ NO
        bytes = Arrays.copyOfRange(data, position, position + SEQ_NO_LENGTH);
        long seqNo = Longs.fromByteArray(bytes);
        position += SEQ_NO_LENGTH;

        // READ CREATOR
        byte[] senderBytes = Arrays.copyOfRange(data, position, position + Transaction.CREATOR_LENGTH);
        Account sender = Account.makeAccountFromShort(senderBytes);
        position += Transaction.CREATOR_LENGTH;

        // READ RECIPIENT
        byte[] recipientBytes = Arrays.copyOfRange(data, position, position + TransactionAmount.RECIPIENT_LENGTH);
        //Account recipient = new Account(Base58.encode(recipientBytes));
        Account recipient = Account.makeAccountFromShort(recipientBytes);
        position += TransactionAmount.RECIPIENT_LENGTH;

        // READ ASSET KEY
        byte[] keyBytes = Arrays.copyOfRange(data, position, position + TransactionAmount.KEY_LENGTH);
        long assetKey = Longs.fromByteArray(keyBytes);
        position += TransactionAmount.KEY_LENGTH;

        // READ AMOUNT
        byte[] amountBytes = Arrays.copyOfRange(data, position, position + TransactionAmount.AMOUNT_LENGTH);
        BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
        position += TransactionAmount.AMOUNT_LENGTH;

        // CHECK ACCURACY of AMOUNT
        if (typeBytes[3] != -1) {
            // not use old FLAG from vers 2
            int accuracy = typeBytes[3] & TransactionAmount.SCALE_MASK;
            if (accuracy > 0) {
                if (accuracy > TransactionAmount.SCALE_MASK_HALF + 1) {
                    accuracy -= TransactionAmount.SCALE_MASK + 1;
                }

                // RESCALE AMOUNT
                amount = amount.scaleByPowerOfTen(-accuracy);
            }
        }

        // HEAD LEN
        int headLen = Byte.toUnsignedInt(data[position]);
        position++;
        // HEAD
        byte[] headBytes = Arrays.copyOfRange(data, position, position + headLen);
        String head = new String(headBytes, StandardCharsets.UTF_8);
        position += headLen;

        // DATA +++
        byte[] arbitraryData = null;
        byte[] encryptedByte = null;
        byte[] isTextByte = null;
        if (typeBytes[3] >= 0) {
            // IF here is DATA

            // READ DATA SIZE
            byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + Transaction.DATA_SIZE_LENGTH);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            position += Transaction.DATA_SIZE_LENGTH;

            // READ DATA
            arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
            position += dataSize;

            encryptedByte = Arrays.copyOfRange(data, position, position + TransactionAmount.ENCRYPTED_LENGTH);
            position += TransactionAmount.ENCRYPTED_LENGTH;

            isTextByte = Arrays.copyOfRange(data, position, position + TransactionAmount.IS_TEXT_LENGTH);
            position += TransactionAmount.IS_TEXT_LENGTH;
        }

        return new CSend(typeBytes, blockNo, transNo, seqNo,
                sender, recipient, assetKey, amount,
                head, arbitraryData, isTextByte, encryptedByte);

    }

    public String getHead() {
        return this.head;
    }

    public byte[] getData() {
        return this.data;
    }

    public String viewData() {

        if (data == null)
            return "";
        if (this.isText()) {

            if (this.isEncrypted()) {
                return "encrypted";
            } else {
                if (this.data.length > MAX_DATA_VIEW << 4) {
                    return new String(data, StandardCharsets.UTF_8); // "{{" +
                    // new
                    // String(Arrays.copyOfRange(data,
                    // 0,
                    // MAX_DATA_VIEW),
                    // StandardCharsets.UTF_8)
                    // +
                    // "...}}";
                }
                return new String(this.data, StandardCharsets.UTF_8);
            }
        } else {
            if (this.data.length > MAX_DATA_VIEW) {
                return "{{" + Base58.encode(Arrays.copyOfRange(data, 0, MAX_DATA_VIEW)) + "...}}";
            }
            return "{{" + Base58.encode(data) + "}}";
        }
    }

    public byte[] getEncrypted() {

        byte[] enc = new byte[1];
        enc[0] = (isEncrypted()) ? (byte) 1 : (byte) 0;
        return enc;
    }

    public boolean isText() {
        if (data == null || data.length == 0)
            return false;
        return (Arrays.equals(this.isText, new byte[1])) ? false : true;
    }

    public boolean isEncrypted() {
        if (data == null || data.length == 0)
            return false;
        return (Arrays.equals(this.encrypted, new byte[1])) ? false : true;
    }

    // PARSE/CONVERT

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        // GET BASE
        JSONObject transaction = this.getJsonBase();

        if (head.length() > 0)
            transaction.put("head", this.head);

        if (data != null && data.length > 0) {

            // ADD CREATOR/SERVICE/DATA
            if (this.isText() && !this.isEncrypted()) {
                transaction.put("data", new String(this.data, StandardCharsets.UTF_8));
            } else {
                transaction.put("data", Base58.encode(this.data));
            }
            transaction.put("encrypted", this.isEncrypted());
            transaction.put("isText", this.isText());
        }

        return transaction;
    }

    @Override
    public byte[] toBytes() {

        byte[] data = super.toBytes();

        // WRITE HEAD
        byte[] headBytes = this.head.getBytes(StandardCharsets.UTF_8);
        // HEAD SIZE
        data = Bytes.concat(data, new byte[]{(byte) headBytes.length});
        // HEAD
        data = Bytes.concat(data, headBytes);

        if (this.data != null) {
            // WRITE DATA SIZE
            byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
            data = Bytes.concat(data, dataSizeBytes);

            // WRITE DATA
            data = Bytes.concat(data, this.data);

            // WRITE ENCRYPTED
            data = Bytes.concat(data, this.encrypted);

            // WRITE ISTEXT
            data = Bytes.concat(data, this.isText);
        }

        return data;
    }

    @Override
    public int getDataLength() {

        int dataLen = super.getDataLength() + 1 + head.getBytes(StandardCharsets.UTF_8).length;

        if (this.typeBytes[3] >= 0)
            return dataLen + BASE_LENGTH + this.data.length;
        else
            return dataLen;
    }

}