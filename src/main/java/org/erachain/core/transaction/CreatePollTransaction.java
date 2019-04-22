package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.voting.Poll;
import org.erachain.core.voting.PollOption;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
        super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

        this.creator = creator;
        this.poll = poll;
    }

    public CreatePollTransaction(byte[] typeBytes, PublicKeyAccount creator, Poll poll, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, poll, feePow, timestamp, reference);

        this.signature = signature;
        //this.calcFee();
    }
    public CreatePollTransaction(byte[] typeBytes, PublicKeyAccount creator, Poll poll, byte feePow, long timestamp,
                                 Long reference, byte[] signature, long feeLong) {
        this(typeBytes, creator, poll, feePow, timestamp, reference);

        this.signature = signature;
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.AMOUNT_DEDAULT_SCALE);
    }

    public CreatePollTransaction(PublicKeyAccount creator, Poll poll, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, feePow, timestamp, reference, signature);
    }

    public CreatePollTransaction(PublicKeyAccount creator, Poll poll, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, poll, feePow, timestamp, reference);
    }

    //GETTERS/SETTERS
    //public static String getName() { return "Create Poll"; }

    public static Transaction Parse(byte[] data, int asDeal) throws Exception {

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
        long reference = Longs.fromByteArray(referenceBytes);
        position += REFERENCE_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        //READ POLL
        Poll poll = Poll.parse(Arrays.copyOfRange(data, position, data.length));
        position += poll.getDataLength();

        byte feePow = 0;
        if (asDeal > Transaction.FOR_PACK) {
            // READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);

        long feeLong = 0;
        if (asDeal == FOR_DB_RECORD) {
            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

        return new CreatePollTransaction(typeBytes, creator, poll, feePow, timestamp, reference, signatureBytes, feeLong);
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
        transaction.put("creator", this.creator.getAddress());
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

        //WRITE REFERENCE
        byte[] referenceBytes = Longs.toByteArray(this.reference);
        referenceBytes = Bytes.ensureCapacity(referenceBytes, REFERENCE_LENGTH, 0);
        data = Bytes.concat(data, referenceBytes);

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

	/*
	public boolean isSignatureValid()
	{
		byte[] data = this.toBytes( false, null );
		if ( data == null ) return false;

		return Crypto.getInstance().verify(this.creator.getPublicKey(), this.signature, data);
	}
	 */

    @Override
    public int isValid(int asDeal, long flags) {

        if (this.height > BlockChain.ITEM_POLL_FROM)
            return INVALID_TRANSACTION_TYPE;

        //CHECK POLL NAME LENGTH
        int nameLength = this.poll.getName().getBytes(StandardCharsets.UTF_8).length;
        if (nameLength > 400 || nameLength < 1) {
            return INVALID_NAME_LENGTH_MAX;
        }

        //CHECK POLL NAME LOWERCASE
        if (!this.poll.getName().equals(this.poll.getName().toLowerCase())) {
            return NAME_NOT_LOWER_CASE;
        }

        //CHECK POLL DESCRIPTION LENGTH
        int descriptionLength = this.poll.getDescription().getBytes(StandardCharsets.UTF_8).length;
        if (descriptionLength > BlockChain.MAX_REC_DATA_BYTES || descriptionLength < 1) {
            return INVALID_DESCRIPTION_LENGTH_MAX;
        }

        //CHECK POLL DOES NOT EXIST ALREADY
        if (this.dcSet.getPollMap().contains(this.poll)) {
            return POLL_ALREADY_CREATED;
        }

        //CHECK IF POLL DOES NOT CONTAIN ANY VOTERS
        if (this.poll.hasVotes()) {
            return POLL_ALREADY_HAS_VOTES;
        }

        //CHECK POLL CREATOR VALID ADDRESS
        if (!Crypto.getInstance().isValidAddress(this.poll.getCreator().getAddress())) {
            return INVALID_ADDRESS;
        }

        //CHECK OPTIONS LENGTH
        int optionsLength = poll.getOptions().size();
        if (optionsLength > 100 || optionsLength < 1) {
            return INVALID_OPTIONS_LENGTH;
        }

        //CHECK OPTIONS
        List<String> options = new ArrayList<String>();
        for (PollOption option : this.poll.getOptions()) {
            //CHECK OPTION LENGTH
            int optionLength = option.getName().getBytes(StandardCharsets.UTF_8).length;
            if (optionLength > 400 || optionLength < 1) {
                return INVALID_OPTION_LENGTH;
            }

            //CHECK OPTION UNIQUE
            if (options.contains(option.getName())) {
                return DUPLICATE_OPTION;
            }

            options.add(option.getName());
        }

        return super.isValid(asDeal, flags);
    }

    //PROCESS/ORPHAN

    //@Override
    @Override
    public void process(Block block, int asDeal) {
        //UPDATE CREATOR
        super.process(block, asDeal);

        //INSERT INTO DATABASE
        this.dcSet.getPollMap().add(this.poll);
    }


    //@Override
    @Override
    public void orphan(Block block, int asDeal) {
        //UPDATE CREATOR
        super.orphan(block, asDeal);

        //DELETE FROM DATABASE
        this.dcSet.getPollMap().delete(this.poll);
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
        String address = account.getAddress();

        if (address.equals(this.creator.getAddress()) || address.equals(this.poll.getCreator().getAddress())) {
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

    @Override
    public long calcBaseFee() {
        return calcCommonFee();
    }
}
