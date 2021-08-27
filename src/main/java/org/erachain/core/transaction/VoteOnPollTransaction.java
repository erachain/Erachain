package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * old poll transaction from QORA
 * @deprecated
 */
public class VoteOnPollTransaction extends Transaction {
    private static final byte TYPE_ID = (byte) VOTE_ON_POLL_TRANSACTION;
    private static final String NAME_ID = "Vote on Poll";
    private static final int POLL_SIZE_LENGTH = 4;
    private static final int OPTION_SIZE_LENGTH = 4;

    private static final int LOAD_LENGTH = POLL_SIZE_LENGTH + OPTION_SIZE_LENGTH;
    private static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK + LOAD_LENGTH;
    private static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + LOAD_LENGTH;
    private static final int BASE_LENGTH = Transaction.BASE_LENGTH + LOAD_LENGTH;
    private static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD + LOAD_LENGTH;

    public int option;
    private String poll;

    public VoteOnPollTransaction(byte[] typeBytes, PublicKeyAccount creator, String poll, int option, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, null, null, feePow, timestamp, reference);

        this.creator = creator;
        this.poll = poll;
        this.option = option;
    }

    public VoteOnPollTransaction(byte[] typeBytes, PublicKeyAccount creator, String poll, int option, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, poll, option, feePow, timestamp, reference);
        this.signature = signature;
    }

    public VoteOnPollTransaction(byte[] typeBytes, PublicKeyAccount creator, String poll, int option, byte feePow,
                                 long timestamp, Long reference, byte[] signature, long seqNo, long feeLong) {
        this(typeBytes, creator, poll, option, feePow, timestamp, reference);
        this.signature = signature;
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    // as pack
    public VoteOnPollTransaction(byte[] typeBytes, PublicKeyAccount creator, String poll, int option, Long reference, byte[] signature) {
        this(typeBytes, creator, poll, option, (byte) 0, 0l, reference);
        this.signature = signature;
    }

    public VoteOnPollTransaction(PublicKeyAccount creator, String poll, int option, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, option, feePow, timestamp, reference, signature);
    }

    public VoteOnPollTransaction(PublicKeyAccount creator, String poll, int option, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, option, feePow, timestamp, reference);
    }

    public VoteOnPollTransaction(PublicKeyAccount creator, String poll, int option, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, option, (byte) 0, 0l, reference);
    }

    //GETTERS/SETTERS

    //public static String getName() { return "Vote on Poll"; }

    @Override
    public boolean isWiped() {
        return true;
    }

    public static Transaction Parse(byte[] data, int forDeal) throws Exception {

        int test_len = BASE_LENGTH;
        if (forDeal == Transaction.FOR_MYPACK) {
            test_len -= Transaction.TIMESTAMP_LENGTH + Transaction.FEE_POWER_LENGTH;
        } else if (forDeal == Transaction.FOR_PACK) {
            test_len -= Transaction.TIMESTAMP_LENGTH;
        } else if (forDeal == Transaction.FOR_DB_RECORD) {
            test_len += Transaction.FEE_POWER_LENGTH;
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

        /////
        //READ POLL SIZE
        byte[] pollLengthBytes = Arrays.copyOfRange(data, position, position + POLL_SIZE_LENGTH);
        int pollLength = Ints.fromByteArray(pollLengthBytes);
        position += POLL_SIZE_LENGTH;

        if (pollLength < 1 || pollLength > 400) {
            throw new Exception("Invalid poll length");
        }

        //READ POLL
        byte[] pollBytes = Arrays.copyOfRange(data, position, position + pollLength);
        String poll = new String(pollBytes, StandardCharsets.UTF_8);
        position += pollLength;

        //READ OPTION
        byte[] optionBytes = Arrays.copyOfRange(data, position, position + OPTION_SIZE_LENGTH);
        int option = Ints.fromByteArray(optionBytes);
        position += OPTION_SIZE_LENGTH;

        if (forDeal > Transaction.FOR_MYPACK) {
            return new VoteOnPollTransaction(typeBytes, creator, poll, option, feePow, timestamp, reference,
                    signatureBytes, seqNo, feeLong);
        } else {
            return new VoteOnPollTransaction(typeBytes, creator, poll, option, reference, signatureBytes);
        }
    }

    public String getPoll() {
        return this.poll;
    }

    public int getOption() {
        return this.option;
    }

    //PARSE CONVERT

    @Override
    public boolean hasPublicText() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        //ADD CREATOR/NAME/DESCRIPTION/OPTIONS
        transaction.put("creator", this.creator.getAddress());
        transaction.put("poll", this.poll);
        transaction.put("option", this.option);

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {

        byte[] data = super.toBytes(forDeal, withSignature);

        //WRITE POLL SIZE
        byte[] pollBytes = this.poll.getBytes(StandardCharsets.UTF_8);
        int pollLength = pollBytes.length;
        byte[] pollLengthBytes = Ints.toByteArray(pollLength);
        data = Bytes.concat(data, pollLengthBytes);

        //WRITE NAME
        data = Bytes.concat(data, pollBytes);

        //WRITE OPTION
        byte[] optionBytes = Ints.toByteArray(this.option);
        optionBytes = Bytes.ensureCapacity(optionBytes, OPTION_SIZE_LENGTH, 0);
        data = Bytes.concat(data, optionBytes);

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

        return base_len + this.poll.getBytes(StandardCharsets.UTF_8).length;
    }

    //VALIDATE

    //@Override
    @Override
    public int isValid(int forDeal, long flags) {

        if (this.height > BlockChain.ITEM_POLL_FROM)
            return INVALID_TRANSACTION_TYPE;

        return super.isValid(forDeal, flags);

    }

    //PROCESS/ORPHAN

    //@Override
    @Override
    public void process(Block block, int forDeal) {

        //UPDATE CREATOR
        super.process(block, forDeal);

    }


    //@Override
    @Override
    public void orphan(Block block, int forDeal) {

        //UPDATE CREATOR
        super.orphan(block, forDeal);

    }


    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        return new HashSet<Account>();
    }

    @Override
    public boolean isInvolved(Account account) {

        if (account.equals(this.creator)) {
            return true;
        }

        return false;
    }


    //@Override
    @Override
    public BigDecimal getAmount(Account account) {
        if (account.equals(this.creator)) {
            return BigDecimal.ZERO.subtract(this.fee);
        }

        return BigDecimal.ZERO;
    }

    //@Override
    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        return subAssetAmount(null, this.creator.getAddress(), FEE_KEY, this.fee);
    }

}
