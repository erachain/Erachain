package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.exdata.exLink.ExLink;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * TODO
 * ver =1 - vouching incommed transfers - assets etc.
 * ++ FEE = 0, no TIMESTAMP??, max importance for including in block
 */
public class RVouch extends Transaction {

    protected static final int LOAD_LENGTH = HEIGHT_LENGTH + SEQ_LENGTH;
    protected static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH;

    public static final byte TYPE_ID = (byte) Transaction.SIGN_TRANSACTION;
    public static final String TYPE_NAME = "Sign / Vouch";

    static Logger LOGGER = LoggerFactory.getLogger(RVouch.class.getName());
    protected int refHeight;
    protected int refSeqNo;

    public RVouch(byte[] typeBytes, PublicKeyAccount creator, byte feePow, int refHeight, int refSeqNo, long timestamp, Long reference) {
        super(typeBytes, TYPE_NAME, creator, null, null, feePow, timestamp, reference);

        this.refHeight = refHeight;
        this.refSeqNo = refSeqNo;
    }

    public RVouch(byte[] typeBytes, PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, feePow, height, seq, timestamp, reference);
        this.signature = signature;
    }

    public RVouch(byte[] typeBytes, PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp,
                  Long reference, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, feePow, height, seq, timestamp, reference);
        this.signature = signature;
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    // as pack
    public RVouch(byte[] typeBytes, PublicKeyAccount creator, int height, int seq, Long reference, byte[] signature) {
        this(typeBytes, creator, (byte) 0, height, seq, 0l, reference);
        this.signature = signature;
    }

    public RVouch(PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, height, seq, timestamp, reference);
    }

    public RVouch(PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, height, seq, timestamp, reference, signature);
    }

    // as pack
    public RVouch(PublicKeyAccount creator, int height, int seq, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, (byte) 0, height, seq, 0l, reference);
    }


    //GETTERS/SETTERS

    public int getRefHeight() {
        return this.refHeight;
    }

    public int getRefSeqNo() {
        return this.refSeqNo;
    }

    @Override
    public boolean hasPublicText() {
        return false;
    }

    //PARSE/CONVERT


    public static Transaction Parse(byte[] data, int forDeal) throws Exception {

        //CHECK IF WE MATCH BLOCK LENGTH
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

        ExLink exLink;
        if ((typeBytes[2] & HAS_EXLINK_MASK) > 0) {
            exLink = ExLink.parse(data, position);
            position += exLink.length();
        } else {
            exLink = null;
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

        //READ HEIGHT
        byte[] heightBytes = Arrays.copyOfRange(data, position, position + HEIGHT_LENGTH);
        int vouchHeight = Ints.fromByteArray(heightBytes);
        position += HEIGHT_LENGTH;

        //READ SEQ
        byte[] seqBytes = Arrays.copyOfRange(data, position, position + SEQ_LENGTH);
        int vouchSeqNo = Ints.fromByteArray(seqBytes);
        position += SEQ_LENGTH;

        if (forDeal > Transaction.FOR_MYPACK) {
            return new RVouch(typeBytes, creator, feePow, vouchHeight, vouchSeqNo, timestamp, reference,
                    signatureBytes, seqNo, feeLong);
        } else {
            return new RVouch(typeBytes, creator, vouchHeight, vouchSeqNo, reference, signatureBytes);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        transaction.put("refHeight", this.refHeight);
        transaction.put("refSeqNo", this.refSeqNo);

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        //WRITE HEIGHT
        byte[] heightBytes = Ints.toByteArray(this.refHeight);
        data = Bytes.concat(data, heightBytes);

        //SEQ HEIGHT
        byte[] seqBytes = Ints.toByteArray(this.refSeqNo);
        data = Bytes.concat(data, seqBytes);

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        // not include item reference

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

        return base_len;

    }

    @Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        if (this.refHeight < 2) {
            //CHECK HEIGHT - not 0 and NOT GENESIS
            return INVALID_BLOCK_HEIGHT;
        }

        if (this.refSeqNo <= 0) {
            //CHECK DATA SIZE
            return INVALID_BLOCK_TRANS_SEQ_ERROR;
        }

        int result = super.isValid(forDeal, flags);
        if (result != Transaction.VALIDATE_OK) return result;

        Transaction transaction = this.dcSet.getTransactionFinalMap().get(Transaction.makeDBRef(this.refHeight, this.refSeqNo));
        if (transaction == null || transaction.getType() == Transaction.CALCULATED_TRANSACTION) {
            if (height > BlockChain.ALL_BALANCES_OK_TO)
                return INVALID_BLOCK_TRANS_SEQ_ERROR;
        }

        if (transaction instanceof RSignNote) {
            RSignNote note = (RSignNote) transaction;
            HashSet<Account> recipients = note.getRecipientAccounts();
            if (note.isCanSignOnlyRecipients()) {
                boolean notFound = true;
                for (Account recipient : recipients) {
                    if (recipient.equals(creator)) {
                        notFound = false;
                        break;
                    }
                }
                if (notFound) {
                    return WRONG_SIGNER;
                }
            }
        }

        return Transaction.VALIDATE_OK;

    }


    @Override
    public void processBody(Block block, int forDeal) {

        super.processBody(block, forDeal);

        if (block == null)
            return;

        // make key for vouching record
        Long recordKey = Transaction.makeDBRef(this.refHeight, this.refSeqNo);
        // find value
        Tuple2<BigDecimal, List<Long>> value = this.dcSet.getVouchRecordMap().get(recordKey);

        // update value
        Tuple2<BigDecimal, List<Long>> valueNew;
        BigDecimal amount = this.creator.getBalanceUSE(Transaction.RIGHTS_KEY, this.dcSet);
        List<Long> listNew;
        if (value == null) {
            listNew = new ArrayList<Long>();
        } else {
            listNew = new ArrayList(value.b); // need clone!
            amount = amount.add(value.a);
        }

        listNew.add(Transaction.makeDBRef(this.height, this.seqNo));

        valueNew =
                new Tuple2<BigDecimal, List<Long>>(
                        amount,
                        listNew
                );
        this.dcSet.getVouchRecordMap().put(recordKey, valueNew);

    }

    @Override
    public void orphanBody(Block block, int forDeal) {

        super.orphanBody(block, forDeal);

        // make key for vouching record
        Long recordKey = Transaction.makeDBRef(this.refHeight, this.refSeqNo);
        // find value
        Tuple2<BigDecimal, List<Long>> value = this.dcSet.getVouchRecordMap().get(recordKey);
        // update value
        List<Long> listNew = new ArrayList(value.b); // need clone!

        listNew.remove(Transaction.makeDBRef(this.height, this.seqNo));

        Tuple2<BigDecimal, List<Long>> valueNew =
                new Tuple2<BigDecimal, List<Long>>(
                        value.a.subtract(this.creator.getBalanceUSE(Transaction.RIGHTS_KEY, this.dcSet)),
                        listNew
                );
        this.dcSet.getVouchRecordMap().put(recordKey, valueNew);

    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>(4, 1);
        accounts.add(this.creator);
        accounts.addAll(this.getRecipientAccounts());
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {

        HashSet<Account> accounts = new HashSet<Account>(2, 1);
        if (isWiped())
            return accounts;

        // НЕЛЬЗЯ ссылаться на новую запись см. issue #1241 - иначе при откате ссылается на уже удаленную запись
        if (false) {
            Transaction record = dcSet.getTransactionFinalMap().get(refHeight, refSeqNo);
            if (record == null) {
                ///throw new Exception(this.toString() + " - not found record: " + vouchHeight + "-" + vouchSeqNo);
            } else {
                accounts.addAll(record.getInvolvedAccounts());
            }

        }


        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {
        if (account.equals(creator)) return true;

        for (Account recipient : this.getRecipientAccounts()) {
            if (account.equals(recipient))
                return true;
        }

        return false;
    }

}