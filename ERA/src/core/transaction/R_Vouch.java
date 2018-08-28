package core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import core.BlockChain;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


// TODO
// ver =1 - vouching incommed transfers - assets etc.
//   ++ FEE = 0, no TIMESTAMP??, max importance for including in block
public class R_Vouch extends Transaction {

    protected static final int LOAD_LENGTH = HEIGHT_LENGTH + SEQ_LENGTH;
    protected static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + LOAD_LENGTH;
    protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + LOAD_LENGTH;
    protected static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH;

    private static final byte TYPE_ID = (byte) Transaction.VOUCH_TRANSACTION;
    private static final String NAME_ID = "Vouch";
    static Logger LOGGER = Logger.getLogger(R_Vouch.class.getName());
    protected int vouchHeight;
    protected int vouchSeqNo;

    public R_Vouch(byte[] typeBytes, PublicKeyAccount creator, byte feePow, int vouchHeight, int vouchSeqNo, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

        this.vouchHeight = vouchHeight;
        this.vouchSeqNo = vouchSeqNo;
    }

    public R_Vouch(byte[] typeBytes, PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, feePow, height, seq, timestamp, reference);
        this.signature = signature;
    }
    public R_Vouch(byte[] typeBytes, PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp,
                   Long reference, byte[] signature, long feeLong) {
        this(typeBytes, creator, feePow, height, seq, timestamp, reference);
        this.signature = signature;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.AMOUNT_DEDAULT_SCALE);
    }

    // as pack
    public R_Vouch(byte[] typeBytes, PublicKeyAccount creator, int height, int seq, Long reference, byte[] signature) {
        this(typeBytes, creator, (byte) 0, height, seq, 0l, reference);
        this.signature = signature;
    }

    public R_Vouch(PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, height, seq, timestamp, reference);
    }

    public R_Vouch(PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, height, seq, timestamp, reference, signature);
    }

    // as pack
    public R_Vouch(PublicKeyAccount creator, int height, int seq, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, (byte) 0, height, seq, 0l, reference);
    }


    //GETTERS/SETTERS

    //public static String getName() { return "Send"; }

    public int getVouchHeight() {
        return this.vouchHeight;
    }

    public int getVouchSeqNo() {
        return this.vouchSeqNo;
    }

    @Override
    public boolean hasPublicText() {
        return false;
    }

    //PARSE/CONVERT


    public static Transaction Parse(byte[] data, int asDeal) throws Exception {

        //boolean asPack = releaserReference != null;

        //CHECK IF WE MATCH BLOCK LENGTH
        int test_len;
        if (asDeal == Transaction.FOR_MYPACK) {
            test_len = BASE_LENGTH_AS_MYPACK;
        } else if (asDeal == Transaction.FOR_PACK) {
            test_len = BASE_LENGTH_AS_PACK;
        } else if (asDeal == Transaction.FOR_DB_RECORD) {
            test_len = BASE_LENGTH_AS_DBRECORD;
        } else {
            test_len = BASE_LENGTH;
        }

        if (data.length < test_len) {
            throw new Exception("Data does not match block length " + data.length);
        }

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        long timestamp = 0;
        if (asDeal > Transaction.FOR_MYPACK) {
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

        byte feePow = 0;
        if (asDeal > Transaction.FOR_PACK) {
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        long feeLong = 0;
        if (asDeal == FOR_DB_RECORD) {
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

        if (asDeal > Transaction.FOR_MYPACK) {
            return new R_Vouch(typeBytes, creator, feePow, vouchHeight, vouchSeqNo, timestamp, reference,
                    signatureBytes, feeLong);
        } else {
            return new R_Vouch(typeBytes, creator, vouchHeight, vouchSeqNo, reference, signatureBytes);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        transaction.put("vouchHeight", this.vouchHeight);
        transaction.put("vouchSeqNo", this.vouchSeqNo);

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        //WRITE HEIGHT
        byte[] heightBytes = Ints.toByteArray(this.vouchHeight);
        heightBytes = Bytes.ensureCapacity(heightBytes, HEIGHT_LENGTH, 0);
        data = Bytes.concat(data, heightBytes);

        //SEQ HEIGHT
        byte[] seqBytes = Ints.toByteArray(this.vouchSeqNo);
        seqBytes = Bytes.ensureCapacity(seqBytes, SEQ_LENGTH, 0);
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

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        return base_len;

    }

    //@Override
    @Override
    public int isValid(int asDeal, long flags) {

        if (this.vouchHeight < 2) {
            //CHECK HEIGHT - not 0 and NOT GENESIS
            return INVALID_BLOCK_HEIGHT;
        }

        if (this.vouchSeqNo <= 0) {
            //CHECK DATA SIZE
            return INVALID_BLOCK_TRANS_SEQ_ERROR;
        }

        int result = super.isValid(asDeal, flags);
        if (result != Transaction.VALIDATE_OK) return result;

		/*
		//Block block1 = Controller.getInstance().getBlockByHeight(db, height);
		byte[] b = db.getHeightMap().getBlockByHeight(height);
		if (b == null )
			return INVALID_BLOCK_HEIGHT_ERROR;

		Block block = db.getBlocksHeadMap().get(b);
		if (block == null)
			return INVALID_BLOCK_HEIGHT_ERROR;
		Transaction tx = block.getTransaction(seq);
		if (tx == null )
			return INVALID_BLOCK_TRANS_SEQ_ERROR;
		 */
        if (!this.dcSet.getTransactionFinalMap().contains(new Tuple2<Integer, Integer>(this.vouchHeight, this.vouchSeqNo))) {
            return INVALID_BLOCK_TRANS_SEQ_ERROR;
        }

        return Transaction.VALIDATE_OK;

    }


    @Override
    public void process(Block block, int asDeal) {

        super.process(block, asDeal);

        if (block == null)
            return;

        // make key for vouching record
        Tuple2<Integer, Integer> recordKey = new Tuple2<Integer, Integer>(this.vouchHeight, this.vouchSeqNo);
        // find value
        Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> value = this.dcSet.getVouchRecordMap().get(recordKey);

        // update value
        Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> valueNew;
        BigDecimal amount = this.creator.getBalanceUSE(Transaction.RIGHTS_KEY, this.dcSet);
        List<Tuple2<Integer, Integer>> listNew;
        if (value == null) {
            listNew = new ArrayList<Tuple2<Integer, Integer>>();
        } else {
            listNew = value.b;
            amount = amount.add(value.a);
        }

        listNew.add(new Tuple2<Integer, Integer>(this.getBlockHeightByParent(this.dcSet), this.getSeqNo(this.dcSet)));
        // for test only!!
        //listNew.add(new Tuple2<Integer, Integer>(2, 2));

        valueNew =
                new Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>(
                        amount,
                        listNew
                );
        this.dcSet.getVouchRecordMap().set(recordKey, valueNew);

    }

    @Override
    public void orphan(int asDeal) {

        super.orphan(asDeal);

        // make key for vouching record
        Tuple2<Integer, Integer> recordKey = new Tuple2<Integer, Integer>(this.vouchHeight, this.vouchSeqNo);
        // find value
        Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> value = this.dcSet.getVouchRecordMap().get(recordKey);
        // update value
        List<Tuple2<Integer, Integer>> listNew = value.b;

        listNew.remove(new Tuple2<Integer, Integer>(this.getBlockHeight(this.dcSet), this.getSeqNo(this.dcSet)));
        // for test ONLY !!!
        //listNew.remove(new Tuple2<Integer, Integer>(2, 2));

        Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> valueNew =
                new Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>(
                        value.a.subtract(this.creator.getBalanceUSE(Transaction.RIGHTS_KEY, this.dcSet)),
                        listNew
                );
        this.dcSet.getVouchRecordMap().set(recordKey, valueNew);

    }


    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.creator);
        accounts.addAll(this.getRecipientAccounts());
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {

        HashSet<Account> accounts = new HashSet<Account>();

        Transaction record = dcSet.getTransactionFinalMap().getTransaction(vouchHeight, vouchSeqNo);
        if (record == null) {
            LOGGER.debug("core.transaction.R_Vouch.getRecipientAccounts() not found record: " + vouchHeight + "-" + vouchSeqNo);
            return accounts;
        }
        accounts.addAll(record.getInvolvedAccounts());

        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {
        String address = account.getAddress();
        if (address.equals(creator.getAddress())) return true;

        for (Account recipient : this.getRecipientAccounts()) {
            if (address.equals(recipient.getAddress()))
                return true;
        }

        return false;
    }

}