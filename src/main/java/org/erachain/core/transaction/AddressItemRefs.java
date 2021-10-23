package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public abstract class AddressItemRefs extends Transaction {

    private ItemCls item;

    public static final long START_KEY = 1000L; // << 20;

    public AddressItemRefs(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte feePow, long timestamp, long reference) {
        super(typeBytes, NAME_ID, creator, null, null, feePow, timestamp, reference);
        this.item = item;
    }

    public AddressItemRefs(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte[] signature, long dbRef) {
        this(typeBytes, NAME_ID, creator, item, (byte) 0, 0L, 0L);
        this.signature = signature;
        this.item.setReference(signature, dbRef);
    }

    //GETTERS/SETTERS
    //public static String getName() { return "Issue Item"; }

    public ItemCls getItem() {
        return this.item;
    }

    @Override
    public String viewItemName() {
        return item.toString();
    }

    //@Override
    @Override
    public void sign(PrivateKeyAccount creator, int forDeal) {
        super.sign(creator, forDeal);
    }

    //PARSE CONVERT


    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = this.getJsonBase();

        //ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
        transaction.put("item", this.item.toJson());

        return transaction;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean withSignature) {
        byte[] data = super.toBytes(forDeal, withSignature);

        // without reference
        data = Bytes.concat(data, this.item.toBytes(forDeal, false, false));

        return data;
    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        int base_len = super.getDataLength(forDeal, withSignature);

        // not include item reference
        return base_len + this.item.getDataLength(false);
    }

    //VALIDATE

    //@Override
    @Override
    public int isValid(int forDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        //CHECK NAME LENGTH
        int nameLength = this.item.getName().getBytes(StandardCharsets.UTF_8).length;
        if (nameLength < item.getMinNameLen()) {
            return INVALID_NAME_LENGTH_MIN;
        }
        if (nameLength > ItemCls.MAX_NAME_LENGTH) {
            return INVALID_NAME_LENGTH_MAX;
        }

        //CHECK DESCRIPTION LENGTH
        int descriptionLength = this.item.getDescription().getBytes(StandardCharsets.UTF_8).length;
        if (descriptionLength > BlockChain.MAX_REC_DATA_BYTES) {
            return INVALID_DESCRIPTION_LENGTH_MAX;
        }

        return super.isValid(forDeal, flags);

    }

    //PROCESS/ORPHAN

    //@Override
    @Override
    public void processBody(Block block, int forDeal) {
        //UPDATE CREATOR
        super.processBody(block, forDeal);

        this.item.setReference(this.signature, dbRef);

        //INSERT INTO DATABASE
        this.item.insertToMap(this.dcSet, START_KEY);

    }

    //@Override
    @Override
    public void orphanBody(Block block, int forDeal) {
        //UPDATE CREATOR
        super.orphanBody(block, forDeal);

        //DELETE FROM DATABASE
        long key = this.item.deleteFromMap(this.dcSet, START_KEY);
    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        return this.getRecipientAccounts();
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<>();
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {
        if (account.equals(this.creator)) {
            return true;
        }

        return false;
    }

    @Override
    public long calcBaseFee(boolean withFreeProtocol) {

        long long_fee = super.calcBaseFee(withFreeProtocol);
        if (long_fee == 0)
            return 0L;

        long len = this.getItem().getName().length();
        if (len < 10) {
            long_fee += BlockChain.FEE_PER_BYTE * (500 + 3 ^ (10 - len) * 100);
        }

        return long_fee;
    }
}
