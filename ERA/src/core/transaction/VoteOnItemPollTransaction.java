package core.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.item.polls.PollCls;
import datachain.DCSet;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;

public class VoteOnItemPollTransaction extends Transaction {
    private static final byte TYPE_ID = (byte) VOTE_ON_ITEM_POLL_TRANSACTION;
    private static final String NAME_ID = "Vote on Item Poll";
    private static final int OPTION_SIZE_LENGTH = 4;
    private static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + KEY_LENGTH + OPTION_SIZE_LENGTH;
    private static final int BASE_LENGTH = Transaction.BASE_LENGTH + KEY_LENGTH + OPTION_SIZE_LENGTH;
    public int option;
    private long key;
    private PollCls poll;

    public VoteOnItemPollTransaction(byte[] typeBytes, PublicKeyAccount creator, long pollKey, int option, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

        this.key = pollKey;
        this.option = option;
    }

    public VoteOnItemPollTransaction(byte[] typeBytes, PublicKeyAccount creator, long pollKey, int option, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, creator, pollKey, option, feePow, timestamp, reference);
        this.signature = signature;
        //this.calcFee();
    }

    // as pack
    public VoteOnItemPollTransaction(byte[] typeBytes, PublicKeyAccount creator, long pollKey, int option, Long reference, byte[] signature) {
        this(typeBytes, creator, pollKey, option, (byte) 0, 0l, reference);
        this.signature = signature;
    }

    public VoteOnItemPollTransaction(PublicKeyAccount creator, long pollKey, int option, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, pollKey, option, feePow, timestamp, reference, signature);
    }

    public VoteOnItemPollTransaction(PublicKeyAccount creator, long pollKey, int option, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, pollKey, option, feePow, timestamp, reference);
    }

    public VoteOnItemPollTransaction(PublicKeyAccount creator, long pollKey, int option, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, pollKey, option, (byte) 0, 0l, reference);
    }

    //GETTERS/SETTERS

    //public static String getName() { return "Vote on Poll"; }

    public static Transaction Parse(byte[] data, Long releaserReference) throws Exception {
        boolean asPack = releaserReference != null;

        //CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < BASE_LENGTH_AS_PACK
                | !asPack & data.length < BASE_LENGTH) {
            throw new Exception("Data does not match block length " + data.length);
        }

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        long timestamp = 0;
        if (!asPack) {
            //READ TIMESTAMP
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            timestamp = Longs.fromByteArray(timestampBytes);
            position += TIMESTAMP_LENGTH;
        }

        Long reference;
        if (!asPack) {
            //READ REFERENCE
            byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
            reference = Longs.fromByteArray(referenceBytes);
            position += REFERENCE_LENGTH;
        } else {
            reference = releaserReference;
        }

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        byte feePow = 0;
        if (!asPack) {
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        /////
        //READ POLL
        byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
        long pollKey = Longs.fromByteArray(keyBytes);
        position += KEY_LENGTH;

        //READ OPTION
        byte[] optionBytes = Arrays.copyOfRange(data, position, position + OPTION_SIZE_LENGTH);
        int option = Ints.fromByteArray(optionBytes);
        position += OPTION_SIZE_LENGTH;

        if (!asPack) {
            return new VoteOnItemPollTransaction(typeBytes, creator, pollKey, option, feePow, timestamp, reference, signatureBytes);
        } else {
            return new VoteOnItemPollTransaction(typeBytes, creator, pollKey, option, reference, signatureBytes);
        }
    }

    @Override
    public void setDC(DCSet dcSet, boolean asPack) {
        super.setDC(dcSet, asPack);

        this.poll = (PollCls) this.dcSet.getItemPollMap().get(this.key);
    }

    public void setDC(DCSet dcSet, boolean asPack, int seqNo) {
        this.setDC(dcSet, asPack);
        this.seqNo = seqNo;
    }

    public long getKey() {
        return this.key;
    }

    public long getAbsKey() {
        if (this.key < 0)
            return -this.key;

        return this.key;
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

        transaction.put("key", this.key);
        transaction.put("option", this.option);

        return transaction;
    }

    @Override
    public byte[] toBytes(boolean withSign, Long releaserReference) {

        byte[] data = super.toBytes(withSign, releaserReference);

        //WRITE POLL KEY
        byte[] keyBytes = Longs.toByteArray(this.key);
        keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
        data = Bytes.concat(data, keyBytes);

        //WRITE OPTION
        byte[] optionBytes = Ints.toByteArray(this.option);
        optionBytes = Bytes.ensureCapacity(optionBytes, OPTION_SIZE_LENGTH, 0);
        data = Bytes.concat(data, optionBytes);

        return data;
    }

    @Override
    public int getDataLength(boolean asPack) {
        if (asPack) {
            return BASE_LENGTH_AS_PACK;
        } else {
            return BASE_LENGTH;
        }
    }

    //VALIDATE

    //@Override
    @Override
    public int isValid(Long releaserReference, long flags) {

        //CHECK POLL EXISTS
        if (poll == null) {
            return POLL_NOT_EXISTS;
        }

        //CHECK OPTION EXISTS
        if (poll.getOptions().size() - 1 < this.option || this.option < 0) {
            return POLL_OPTION_NOT_EXISTS;
        }

        return super.isValid(releaserReference, flags);

    }

    //PROCESS/ORPHAN

    public void process(Block block, boolean asPack) {
        //UPDATE CREATOR
        super.process(block, asPack);

        //ADD VOTE TO POLL
        this.dcSet.getVoteOnItemPollMap().addItem(this.key, this.option, new BigInteger(this.creator.getShortAddressBytes()),
                this.getHeightSeqNo());

    }


    //@Override
    @Override
    public void orphan(boolean asPack) {
        //UPDATE CREATOR
        super.orphan(asPack);

        //DELETE VOTE FROM POLL
        this.dcSet.getVoteOnItemPollMap().removeItem(this.key, this.option, new BigInteger(this.creator.getShortAddressBytes()));
    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = new HashSet<Account>();
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        return new HashSet<>();
    }

    @Override
    public boolean isInvolved(Account account) {
        String address = account.getAddress();

        if (address.equals(this.creator.getAddress())) {
            return true;
        }

        return false;
    }

    public int calcBaseFee() {
        // TODO: умножать комиссию на размер списка переголосваний (СТЕК)
        return calcCommonFee();
    }
}
