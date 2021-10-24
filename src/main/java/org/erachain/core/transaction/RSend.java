package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.smartcontracts.SmartContract;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**

## typeBytes
0 - record type
1 - record version
2 - property 1
3 = property 2

## version 0
 typeBytes[2] = -128 if NO AMOUNT (NO_AMOUNT_MASK)
 typeBytes[3] = -128 if NO DATA

## version 1
typeBytes[1] (version) = 1 - if backward - CONFISCATE CREDIT

## version 2
typeBytes[1] - version

 #### PROPERTY 1
 typeBytes[2].0 = -128 if NO AMOUNT (NO_AMOUNT_MASK)
 typeBytes[2].1 = 64 if backward - CONFISCATE CREDIT

#### PROPERTY 2
typeBytes[3].0 = -128 if NO DATA

## version 3

 #### PROPERTY 1
 typeBytes[2].0 = -128 if NO AMOUNT - check sign (NO_AMOUNT_MASK)
 typeBytes[2].1 = 64 if backward (CONFISCATE CREDIT, ...)
 typeBytes[2].3 = 32 - HAS_EXLINK_MASK

 #### PROPERTY 2
 typeBytes[3].0 = -128 if NO DATA - check sign (NO_AMOUNT_MASK) = '10000000' = Integer.toBinaryString(128) - assertEquals((byte)128, (byte)-128);
 typeBytes[3].3-7 = point accuracy: -16..16 = BYTE - 16

 */

public class RSend extends TransactionAmount {

    public static final byte NO_DATA_MASK = -128; // 0x10000000
    public static final byte MAX_DATA_VIEW = 64;

    static Logger LOGGER = LoggerFactory.getLogger(RSend.class.getName());

    protected static final int LOAD_LENGTH = IS_TEXT_LENGTH + ENCRYPTED_LENGTH + DATA_SIZE_LENGTH;

    public static final byte TYPE_ID = (byte) Transaction.SEND_ASSET_TRANSACTION;
    public static final String TYPE_NAME = "Send";
    protected String title;
    protected byte[] data;
    protected byte[] encrypted;
    protected byte[] isText;

    public RSend(byte[] typeBytes, PublicKeyAccount creator, ExLink exLink, SmartContract smartContract, byte feePow, Account recipient, long key,
                 BigDecimal amount, String title, byte[] data, byte[] isText, byte[] encrypted, long timestamp,
                 long flags) {
        super(typeBytes, TYPE_NAME, creator, exLink, smartContract, feePow, recipient, amount, key, timestamp, flags);

        if (isText != null)
            assert (isText.length == 1);
        if (encrypted != null)
            assert (encrypted.length == 1);

        this.title = title;
        if (title == null)
            this.title = "";

        if (data == null || data.length == 0) {
            // set version byte
            typeBytes[3] = (byte) (typeBytes[3] | NO_DATA_MASK);
        } else {
            // RESET 0 bit
            typeBytes[3] = (byte) (typeBytes[3] & ~NO_DATA_MASK);
            this.data = data;
            this.encrypted = encrypted;
            this.isText = isText;
        }

    }

    public RSend(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key,
                 BigDecimal amount, String head, byte[] data, byte[] isText, byte[] encrypted, long timestamp,
                 long flags, byte[] signature) {
        this(typeBytes, creator, null, null, feePow, recipient, key, amount, head, data, isText, encrypted, timestamp, flags);
        this.signature = signature;
    }

    public RSend(byte[] typeBytes, PublicKeyAccount creator, ExLink exLink, SmartContract smartContract, byte feePow, Account recipient, long key,
                 BigDecimal amount, String title, byte[] data, byte[] isText, byte[] encrypted, long timestamp,
                 long flags, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, exLink, smartContract, feePow, recipient, key, amount, title, data, isText, encrypted, timestamp, flags);
        this.signature = signature;
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }


    // as pack
    public RSend(byte[] typeBytes, PublicKeyAccount creator, ExLink exLink, SmartContract smartContract, Account recipient, long key, BigDecimal amount,
                 String title, byte[] data, byte[] isText, byte[] encrypted, long flags, byte[] signature) {
        this(typeBytes, creator, exLink, smartContract, (byte) 0, recipient, key, amount, title, data, isText, encrypted, 0L, flags);
        this.signature = signature;
    }

    // FOR BACKWARDS - CONFISCATE CREDIT
    public RSend(byte version, byte property1, byte property2, PublicKeyAccount creator, ExLink exLink, SmartContract smartContract, byte feePow,
                 Account recipient, long key, BigDecimal amount, String title, byte[] data, byte[] isText, byte[] encrypted,
                 long timestamp, long flags) {
        this(new byte[]{TYPE_ID, version, property1, property2}, creator, exLink, smartContract, feePow, recipient, key, amount, title, data,
                isText, encrypted, timestamp, flags);
    }

    public RSend(PublicKeyAccount creator, ExLink exLink, SmartContract smartContract, byte feePow, Account recipient, long key, BigDecimal amount, String title,
                 byte[] data, byte[] isText, byte[] encrypted, long timestamp, long flags) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, exLink, smartContract, feePow, recipient, key, amount, title, data, isText, encrypted,
                timestamp, flags);
    }

    public RSend(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, String title,
                 byte[] data, byte[] isText, byte[] encrypted, long timestamp, long flags, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, recipient, key, amount, title, data, isText, encrypted,
                timestamp, flags, signature);
    }

    // as pack
    public RSend(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, String title, byte[] data,
                 byte[] isText, byte[] encrypted, long flags) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, null, null, (byte) 0, recipient, key, amount, title, data, isText, encrypted,
                0L, flags);
    }

    ////////////////////////// SHOR -text DATA
    public RSend(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key,
                 BigDecimal amount, long timestamp, long flags) {
        super(typeBytes, TYPE_NAME, creator, null, null, feePow, recipient, amount, key, timestamp, flags);
        this.title = "";
        typeBytes[3] = (byte) (typeBytes[3] | NO_DATA_MASK);

    }

    public RSend(byte[] typeBytes, PublicKeyAccount creator, byte feePow, Account recipient, long key,
                 BigDecimal amount, long timestamp, long flags, byte[] signature) {
        this(typeBytes, creator, feePow, recipient, key, amount, timestamp, flags);
        this.signature = signature;
        // this.calcFee();
    }

    // as pack
    public RSend(byte[] typeBytes, PublicKeyAccount creator, Account recipient, long key, BigDecimal amount) {
        this(typeBytes, creator, (byte) 0, recipient, key, amount, 0L, 0L);
    }

    public RSend(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, long timestamp,
                 long flags) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, recipient, key, amount, timestamp, flags);
    }

    public RSend(PublicKeyAccount creator, byte feePow, Account recipient, long key, BigDecimal amount, long timestamp,
                 long flags, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, recipient, key, amount, timestamp, flags,
                signature);
    }

    // as pack
    public RSend(PublicKeyAccount creator, Account recipient, long key, BigDecimal amount, long flags) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, (byte) 0, recipient, key, amount, 0L, flags);
    }

    // GETTERS/SETTERS

    public static Transaction Parse(byte[] data, int forDeal) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        int test_len;
        if (forDeal == Transaction.FOR_MYPACK) {
            test_len = BASE_LENGTH_AS_MYPACK;
        } else if (forDeal == Transaction.FOR_PACK) {
            test_len = BASE_LENGTH_AS_PACK;
        } else if (forDeal == Transaction.FOR_DB_RECORD) {
            test_len = BASE_LENGTH_AS_DBRECORD;
        } else {
            test_len = BASE_LENGTH;
        }

        if (typeBytes[2] < 0) {
            // without AMOUNT
            test_len -= KEY_LENGTH + AMOUNT_LENGTH;
        }
        if (typeBytes[3] < 0) {
            test_len -= DATA_SIZE_LENGTH + ENCRYPTED_LENGTH + IS_TEXT_LENGTH;
        }

        if (data.length < test_len) {
            throw new Exception("Data does not match RAW length " + data.length + " < " + test_len);
        }

        long timestamp = 0;
        if (forDeal > Transaction.FOR_MYPACK) {
            //READ TIMESTAMP
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            timestamp = Longs.fromByteArray(timestampBytes);
            position += TIMESTAMP_LENGTH;
        }

        //READ FLAGS
        byte[] flagsBytes = Arrays.copyOfRange(data, position, position + FLAGS_LENGTH);
        long flagsTX = Longs.fromByteArray(flagsBytes);
        position += FLAGS_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        ExLink exLink;
        if ((typeBytes[2] & HAS_EXLINK_MASK) > 0) {
            exLink = ExLink.parse(data, position);
            position += exLink.length();
        } else {
            exLink = null;
        }

        SmartContract smartContract;
        if ((typeBytes[2] & HAS_SMART_CONTRACT_MASK) > 0) {
            smartContract = SmartContract.Parses(data, position, forDeal);
            position += smartContract.length(forDeal);
        } else {
            smartContract = null;
        }

        byte feePow = 0;
        if (forDeal > Transaction.FOR_PACK) {
            // READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        // READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        long feeLong = 0;
        long seqNo = 0;
        if (forDeal == FOR_DB_RECORD) {
            //READ SEQ_NO
            byte[] seqNoBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            seqNo = Longs.fromByteArray(seqNoBytes);
            position += TIMESTAMP_LENGTH;

            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

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

        // HEAD LEN
        int titleLen = Byte.toUnsignedInt(data[position++]);
        // HEAD
        byte[] titleBytes = Arrays.copyOfRange(data, position, position + titleLen);
        String title = new String(titleBytes, StandardCharsets.UTF_8);
        position += titleLen;

        // DATA +++
        byte[] arbitraryData = null;
        byte[] encryptedByte = null;
        byte[] isTextByte = null;
        if (typeBytes[3] >= 0) {
            // IF here is DATA

            // READ DATA SIZE
            byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            position += DATA_SIZE_LENGTH;

            // READ DATA
            arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
            position += dataSize;

            encryptedByte = Arrays.copyOfRange(data, position, position + ENCRYPTED_LENGTH);
            position += ENCRYPTED_LENGTH;

            isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
            position += IS_TEXT_LENGTH;
        }

        if (forDeal > Transaction.FOR_MYPACK) {
            return new RSend(typeBytes, creator, exLink, smartContract, feePow, recipient, key, amount, title, arbitraryData, isTextByte,
                    encryptedByte, timestamp, flagsTX, signatureBytes, seqNo, feeLong);
        } else {
            return new RSend(typeBytes, creator, exLink, smartContract, recipient, key, amount, title, arbitraryData, isTextByte,
                    encryptedByte, flagsTX, signatureBytes);
        }

    }

    @Override
    public String getTitle() {
        return this.title;
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
        return !Arrays.equals(this.isText, new byte[1]);
    }

    public boolean isEncrypted() {
        if (data == null || data.length == 0)
            return false;
        return !Arrays.equals(this.encrypted, new byte[1]);
    }

    @Override
    public boolean hasPublicText() {
        return hasPublicText(title, data, isText(), isEncrypted(), null);
    }

    // PARSE/CONVERT

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        // GET BASE
        JSONObject transaction = this.getJsonBase();

        if (data != null && data.length > 0) {

            // ADD CREATOR/SERVICE/DATA
            if (this.isText() && !this.isEncrypted()) {
                transaction.put("message", new String(this.data, StandardCharsets.UTF_8));
            } else {
                transaction.put("data", Base64.getEncoder().encodeToString(this.data));
            }
            transaction.put("encrypted", this.isEncrypted());
            transaction.put("isText", this.isText());
        }

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        // WRITE HEAD
        byte[] headBytes = this.title.getBytes(StandardCharsets.UTF_8);
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
    public int getDataLength(int forDeal, boolean withSignature) {

        int dataLen = super.getDataLength(forDeal, withSignature) + 1 + title.getBytes(StandardCharsets.UTF_8).length;

        if (this.typeBytes[3] >= 0)
            return dataLen + LOAD_LENGTH + this.data.length;
        else
            return dataLen;
    }

    // @Override
    @Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        if (title != null && title.getBytes(StandardCharsets.UTF_8).length > MAX_TITLE_BYTES_LENGTH) {
            errorValue = "bytes: " + title.getBytes(StandardCharsets.UTF_8).length;
            return INVALID_TITLE_LENGTH;
        }

        if (this.data != null) {
            // CHECK DATA SIZE
            if (data.length > MAX_DATA_BYTES_LENGTH) {
                errorValue = "bytes: " + data.length;
                return INVALID_DATA_LENGTH;
            }
        }

        return super.isValid(forDeal, flags);
    }

}