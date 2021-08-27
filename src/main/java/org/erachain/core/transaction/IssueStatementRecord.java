package org.erachain.core.transaction;

//import java.math.BigDecimal;
//import java.math.BigInteger;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;


// issue statement
public class IssueStatementRecord extends Transaction {

    protected static final byte HAS_TEMPLATE_MASK = (byte) (1 << 7);
    public static final byte TYPE_ID = (byte) ISSUE_STATEMENT_TRANSACTION;
    public static final String TYPE_NAME = "Issue Statement";
    /*
    PROPERTIES:
    [0] - type
    [1] - version
    [2] bits[0] - =1 - has Template
    [2] bits [6,7] - signers: 0 - none; 1..3 = 1..3; 4 = LIST -> 1 byte for LIST.len + 3
    [3] - < 0 - has DATA
     */
    protected long key; // key for Template
    protected byte[] data;
    protected byte[] encrypted;
    protected byte[] isText;
    protected PublicKeyAccount[] signers; // for all it need ecnrypt
    protected byte[][] signatures; // - multi sign

    public IssueStatementRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, byte feePow, long templateKey, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference) {

        super(typeBytes, TYPE_NAME, creator, null, null, feePow, timestamp, reference);

        this.key = templateKey;
        this.data = data;
        this.encrypted = encrypted;
        this.isText = isText;
    }

    public IssueStatementRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, byte feePow, long templateKey, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, linkTo, feePow, templateKey, data, isText, encrypted, timestamp, reference);
        this.signature = signature;
    }

    public IssueStatementRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, byte feePow, long templateKey, byte[] data,
                                byte[] isText, byte[] encrypted, long timestamp, Long reference, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, linkTo, feePow, templateKey, data, isText, encrypted, timestamp, reference);
        this.signature = signature;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
    }


    // asPack
    public IssueStatementRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, long templateKey, byte[] data, byte[] isText, byte[] encrypted, Long reference, byte[] signature) {
        this(typeBytes, creator, linkTo, (byte) 0, templateKey, data, isText, encrypted, 0L, reference);
        this.signature = signature;
        // not need this.calcFee();
    }

    public IssueStatementRecord(PublicKeyAccount creator, ExLink linkTo, byte feePow, long templateKey, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, linkTo, feePow, templateKey, data, isText, encrypted, timestamp, reference, signature);
        // set props
        this.setTypeBytes();
    }

    public IssueStatementRecord(PublicKeyAccount creator, ExLink linkTo, byte feePow, long templateKey, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, linkTo, feePow, templateKey, data, isText, encrypted, timestamp, reference);
        // set props
        this.setTypeBytes();
    }

    public IssueStatementRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, byte feePow, long templateKey, byte[] data,
                                byte[] isText, byte[] encrypted, PublicKeyAccount[] signers, byte[][] signatures, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, linkTo, feePow, templateKey, data, isText, encrypted, timestamp, reference, signature);
        this.signers = signers;
        this.signatures = signatures;
        this.setTypeBytes();
    }

    public IssueStatementRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, byte feePow, long templateKey, byte[] data,
                                byte[] isText, byte[] encrypted, PublicKeyAccount[] signers, byte[][] signatures,
                                long timestamp, Long reference, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, linkTo, feePow, templateKey, data, isText, encrypted, timestamp, reference, signature);
        this.signers = signers;
        this.signatures = signatures;
        this.setTypeBytes();
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
    }

    // as Pack
    public IssueStatementRecord(byte[] typeBytes, PublicKeyAccount creator, ExLink linkTo, long templateKey, byte[] data,
                                byte[] isText, byte[] encrypted, PublicKeyAccount[] signers, byte[][] signatures, Long reference, byte[] signature) {
        this(typeBytes, creator, linkTo, templateKey, data, isText, encrypted, reference, signature);
        this.signers = signers;
        this.signatures = signatures;
        this.setTypeBytes();
    }

    public IssueStatementRecord(byte prop1, byte prop2, byte prop3, PublicKeyAccount creator, ExLink linkTo, byte feePow, long templateKey, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, prop1, prop2, prop3}, creator, linkTo, feePow, templateKey, data, isText, encrypted, timestamp, reference);
    }

    public static boolean hasTemplate(byte[] typeBytes) {
        if (typeBytes[2] < 0) return true;
        return false;
    }

    public static int getSignersLength(byte[] typeBytes) {
        byte mask = ~HAS_TEMPLATE_MASK;
        return typeBytes[2] & mask;
    }

    // releaserReference = null - not a pack
    // releaserReference = reference for releaser account - it is as pack
    public static Transaction Parse(byte[] data, int forDeal) throws Exception {

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

        if (data.length < test_len) {
            throw new Exception("Data does not match RAW length " + data.length + " < " + test_len);
        }

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        long timestamp = 0;
        if (forDeal > Transaction.FOR_MYPACK) {
            //READ TIMESTAMP
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            timestamp = Longs.fromByteArray(timestampBytes);
            position += TIMESTAMP_LENGTH;
        }

        //READ REFERENCE
        byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
        Long reference = Longs.fromByteArray(referenceBytes);
        position += REFERENCE_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        ExLink linkTo;
        if ((typeBytes[2] & HAS_EXLINK_MASK) > 0) {
            linkTo = ExLink.parse(data, position);
            position += linkTo.length();
        } else {
            linkTo = null;
        }

        byte feePow = 0;
        if (forDeal > Transaction.FOR_PACK) {
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
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

        //////// local parameters

        long key = 0L;
        if (hasTemplate(typeBytes)) {
            //READ KEY
            byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
            key = Longs.fromByteArray(keyBytes);
            position += KEY_LENGTH;
        }

        // DATA +++ - from org.erachain.core.transaction.RSend.Parse(byte[], Long)
        byte[] arbitraryData = null;
        byte[] encryptedByte = null;
        byte[] isTextByte = null;
        if (typeBytes[3] < 0) {
            // IF here is DATA

            //READ DATA SIZE
            byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            position += DATA_SIZE_LENGTH;

            //READ DATA
            arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
            position += dataSize;

            encryptedByte = Arrays.copyOfRange(data, position, position + ENCRYPTED_LENGTH);
            position += ENCRYPTED_LENGTH;

            isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
            position += IS_TEXT_LENGTH;
        }

        int signersLen = getSignersLength(typeBytes);
        PublicKeyAccount[] signers = null;
        byte[][] signatures = null;
        if (signersLen > 0) {
            if (signersLen == 4) {
                //READ ONE BITE for len
                byte[] signersLenBytes = Arrays.copyOfRange(data, position, position + 1);
                signersLen = Byte.toUnsignedInt(signersLenBytes[0]) + 4;
                position++;
            }
            signers = new PublicKeyAccount[signersLen];
            signatures = new byte[signersLen][];
            for (int i = 0; i < signersLen; i++) {
                signers[i] = new PublicKeyAccount(Arrays.copyOfRange(data, position, position + CREATOR_LENGTH));
                position += CREATOR_LENGTH;
                signatures[i] = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
                position += SIGNATURE_LENGTH;
            }
        }

        if (signersLen == 0) {
            if (forDeal > Transaction.FOR_MYPACK) {
                return new IssueStatementRecord(typeBytes, creator, linkTo, feePow, key, arbitraryData, isTextByte, encryptedByte,
                        timestamp, reference, signatureBytes, seqNo, feeLong);
            } else {
                return new IssueStatementRecord(typeBytes, creator, linkTo, key, arbitraryData, isTextByte, encryptedByte, reference, signatureBytes);
            }
        } else {
            if (forDeal > Transaction.FOR_MYPACK) {
                return new IssueStatementRecord(typeBytes, creator, linkTo, feePow, key, arbitraryData, isTextByte, encryptedByte,
                        signers, signatures, timestamp, reference, signatureBytes, seqNo, feeLong);
            } else {
                return new IssueStatementRecord(typeBytes, creator, linkTo, key, arbitraryData, isTextByte, encryptedByte, signers, signatures, reference, signatureBytes);
            }

        }

    }

    //GETTERS/SETTERS
    public void setSidnerSignature(int index, byte[] signature) {
        if (signatures == null)
            signatures = new byte[signers.length][];

        signatures[index] = signature;

    }

    protected boolean hasTemplate() {
        return hasTemplate(this.typeBytes);
    }

    //public static String getName() { return "Statement"; }

    protected void setTypeBytes() {

        byte vers = 0;

        byte prop1 = 0;
        if (this.signers != null && this.signers.length > 0) {
            int len = this.signers.length;
            if (len < 4) {
                prop1 = (byte) len;
            } else {
                prop1 = (byte) 4;
            }
        }
        // set has PLATE byte
        if (this.key > 0) prop1 = (byte) (HAS_TEMPLATE_MASK | prop1);

        byte prop2 = 0;
        if (data != null && data.length > 0) {
            prop2 = (byte) (prop2 | (byte) -128);
        }

        if (this.typeBytes == null) {
            this.typeBytes = new byte[]{TYPE_ID, vers, prop1, prop2};
        } else {
            this.typeBytes[2] = prop1; // property 1
            this.typeBytes[3] = prop2; // property 2
        }
    }

    @Override
    public long getKey() {
        return this.key;
    }

    public byte[] getData() {
        return this.data;
    }

    public boolean isText() {
        if (data == null || data.length == 0) return false;
        return (Arrays.equals(this.isText, new byte[1])) ? false : true;
    }

    public boolean isEncrypted() {
        if (data == null || data.length == 0) return false;
        return (Arrays.equals(this.encrypted, new byte[1])) ? false : true;
    }

    public PublicKeyAccount[] getSigners() {
        return this.signers;
    }

    public String[] getSignersB58() {
        String[] pbKeys = new String[0];
        int i = 0;
        for (PublicKeyAccount key : this.signers) {
            pbKeys[i++] = Base58.encode(key.getPublicKey());
        }
        ;
        return pbKeys;
    }

    public byte[][] getSignersSignatures() {
        return this.signatures;
    }

    public String[] getSignersSignaturesB58() {
        String[] items = new String[0];
        int i = 0;
        for (byte[] item : this.signatures) {
            items[i++] = Base58.encode(item);
        }
        ;
        return items;
    }

    @Override
    public boolean hasPublicText() {
        if (data == null || data.length == 0)
            return false;
        if (!Arrays.equals(this.encrypted, new byte[1]))
            return false;

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        //ADD CREATOR/SERVICE/DATA
        if (data != null && data.length > 0) {

            //ADD CREATOR/SERVICE/DATA
            if (this.isText() && !this.isEncrypted()) {
                transaction.put("data", new String(this.data, StandardCharsets.UTF_8));
            } else {
                transaction.put("data", Base58.encode(this.data));
            }
            transaction.put("encrypted", this.isEncrypted());
            transaction.put("isText", this.isText());
        }

        if (this.key > 0)
            transaction.put("template", this.key);

        if (signers != null && signers.length > 0) {
            transaction.put("singers", this.getSignersB58());
            transaction.put("signatures", this.getSignersSignaturesB58());
        }
        return transaction;
    }

    //@Override
    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        if (this.key > 0) {
            //WRITE KEY
            byte[] keyBytes = Longs.toByteArray(this.key);
            keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
            data = Bytes.concat(data, keyBytes);

        }
        if (this.data != null) {

            //WRITE DATA SIZE
            byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
            data = Bytes.concat(data, dataSizeBytes);

            //WRITE DATA
            data = Bytes.concat(data, this.data);

            //WRITE ENCRYPTED
            data = Bytes.concat(data, this.encrypted);

            //WRITE ISTEXT
            data = Bytes.concat(data, this.isText);
        }

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {

        int base_len;
        if (forDeal == FOR_MYPACK)
            base_len = BASE_LENGTH_AS_MYPACK;
        else if (forDeal == FOR_PACK)
            base_len = BASE_LENGTH_AS_PACK;
        else if (forDeal == FOR_DB_RECORD)
            base_len = BASE_LENGTH_AS_DBRECORD;
        else
            base_len = BASE_LENGTH;

        if (exLink != null)
            base_len += exLink.length();

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        int add_len = 0;
        if (this.data != null && this.data.length > 0)
            add_len += IS_TEXT_LENGTH + ENCRYPTED_LENGTH + DATA_SIZE_LENGTH + this.data.length;
        if (this.key > 0)
            add_len += KEY_LENGTH;

        return base_len + add_len;
    }

    //@Override
    @Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        //CHECK DATA SIZE
        if (data.length > BlockChain.MAX_REC_DATA_BYTES || data.length < 1) {
            return INVALID_DATA_LENGTH;
        }


        int result = super.isValid(forDeal, flags);
        if (result != Transaction.VALIDATE_OK) return result;

        // ITEM EXIST? - for assets transfer not need - amount expect instead
        if (!this.dcSet.getItemTemplateMap().contains(this.key))
            return Transaction.ITEM_DOES_NOT_EXIST;

        return Transaction.VALIDATE_OK;

    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>(2, 1);
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        return new HashSet<>(1, 1);
    }

    @Override
    public boolean isInvolved(Account account) {

        if (account.equals(this.creator)) {
            return true;
        }

        return false;
    }

}
