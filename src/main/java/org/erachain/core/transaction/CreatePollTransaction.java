package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.voting.Poll;
import org.erachain.core.voting.PollOption;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * old create poll from QORA
 * @deprecated
 */
public class CreatePollTransaction extends Transaction {
    private static final int TYPE_ID = Transaction.CREATE_POLL_TRANSACTION;
    private static final String NAME_ID = "Create Poll";

    private PublicKeyAccount creator;
    private Poll poll;

    public CreatePollTransaction(byte[] typeBytes, PublicKeyAccount creator, Poll poll, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, null, null, feePow, timestamp, reference);

        this.creator = creator;
        this.poll = poll;
    }

    public CreatePollTransaction(byte[] typeBytes, PublicKeyAccount creator, Poll poll, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, poll, feePow, timestamp, reference);

        this.signature = signature;
        //this.calcFee();
    }

    public CreatePollTransaction(byte[] typeBytes, PublicKeyAccount creator, Poll poll, byte feePow, long timestamp,
                                 Long reference, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, poll, feePow, timestamp, reference);

        this.signature = signature;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
    }

    public CreatePollTransaction(PublicKeyAccount creator, Poll poll, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, feePow, timestamp, reference, signature);
    }

    public CreatePollTransaction(PublicKeyAccount creator, Poll poll, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, feePow, timestamp, reference);
    }

    //GETTERS/SETTERS

    @Override
    public boolean isWiped() {
        return true;
    }

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

        //READ FLAGS
        byte[] flagsBytes = Arrays.copyOfRange(data, position, position + FLAGS_LENGTH);
         long flagsTX = Longs.fromByteArray(flagsBytes);
        position += FLAGS_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        //READ POLL
        Poll poll = Poll.parse(Arrays.copyOfRange(data, position, data.length));
        position += poll.getDataLength();

        byte feePow = 0;
        if (forDeal > Transaction.FOR_PACK) {
            // READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);

        long feeLong = 0;
        long seqNo = 0;
        if (forDeal == FOR_DB_RECORD) {
            position += SIGNATURE_LENGTH;

            //READ SEQ_NO
            byte[] seqNoBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            seqNo = Longs.fromByteArray(seqNoBytes);
            position += TIMESTAMP_LENGTH;

            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

        return new CreatePollTransaction(typeBytes, creator, poll, feePow, timestamp, flagsTX, signatureBytes, seqNo, feeLong);

    }

    public Poll getPoll() {
        return this.poll;
    }

    //PARSE CONVERT

    @Override
    public boolean hasPublicText() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        //ADD CREATOR/NAME/DESCRIPTION/OPTIONS
        transaction.put("name", this.poll.getName());
        transaction.put("description", this.poll.getDescription());

        JSONArray options = new JSONArray();
        for (PollOption option : this.poll.getOptions()) {
            options.add(option.getName());
        }

        transaction.put("options", options);

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = new byte[0];

        //WRITE TYPE
        data = Bytes.concat(data, this.typeBytes);

        if (forDeal > FOR_MYPACK) {
            // WRITE TIMESTAMP
            byte[] timestampBytes = Longs.toByteArray(this.timestamp);
            timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
            data = Bytes.concat(data, timestampBytes);
        }

        //WRITE FLAGS
        byte[] flagsBytes = Longs.toByteArray(this.flags);
        data = Bytes.concat(data, flagsBytes);

        //WRITE CREATOR
        data = Bytes.concat(data, this.creator.getPublicKey());

        //WRITE POLL
        data = Bytes.concat(data, this.poll.toBytes());

        if (forDeal > FOR_PACK) {
            // WRITE FEE POWER
            byte[] feePowBytes = new byte[1];
            feePowBytes[0] = this.feePow;
            data = Bytes.concat(data, feePowBytes);
        }

        //SIGNATURE
        if (withSignature)
            data = Bytes.concat(data, this.signature);

        if (forDeal == FOR_DB_RECORD) {
            // WRITE DBREF
            byte[] dbRefBytes = Longs.toByteArray(this.dbRef);
            data = Bytes.concat(data, dbRefBytes);

            // WRITE FEE
            byte[] feeBytes = Longs.toByteArray(this.fee.unscaledValue().longValue());
            data = Bytes.concat(data, feeBytes);
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

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        return base_len + this.poll.getDataLength();
    }

    //VALIDATE

    @Override
    public int isValid(int forDeal, long flags) {

        if (this.height > BlockChain.ITEM_POLL_FROM)
            return INVALID_TRANSACTION_TYPE;

        return super.isValid(forDeal, flags);
    }

    //PROCESS/ORPHAN

    //@Override
    @Override
    public void processBody(Block block, int forDeal) {

        //UPDATE CREATOR
        super.processBody(block, forDeal);

    }


    //@Override
    @Override
    public void orphanBody(Block block, int forDeal) {

        //UPDATE CREATOR
        super.orphanBody(block, forDeal);

    }


    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<>();
        accounts.add(this.creator);
        accounts.add(this.poll.getCreator());
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        return new HashSet<>();
    }

    @Override
    public boolean isInvolved(Account account) {

        if (account.equals(this.creator) || account.equals(this.poll.getCreator())) {
            return true;
        }

        return false;
    }

    //@Override
    @Override
    public BigDecimal getAmount(Account account) {
        if (account.getAddress().equals(this.creator.getAddress())) {
            return BigDecimal.ZERO.subtract(this.fee);
        }

        return BigDecimal.ZERO;
    }

    //@Override
    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        return subAssetAmount(null, this.creator.getAddress(), FEE_KEY, this.fee);
    }

}
