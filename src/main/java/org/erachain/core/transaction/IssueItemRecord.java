package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.erachain.utils.Pair;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public abstract class IssueItemRecord extends Transaction {

    static Logger LOGGER = LoggerFactory.getLogger(IssueItemRecord.class.getName());

    //private static final int BASE_LENGTH = Transaction.BASE_LENGTH;

    public static final long START_KEY = 0; // << 20;

    protected ItemCls item;

    public IssueItemRecord(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
        this.item = item;
    }

    public IssueItemRecord(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(typeBytes, NAME_ID, creator, item, feePow, timestamp, reference);
        this.signature = signature;
        if (item.getReference() == null) item.setReference(signature); // set reference
        //item.resolveKey(DLSet.getInstance());
        //if (timestamp > 1000 ) this.calcFee(); // not asPaack
    }

    public IssueItemRecord(byte[] typeBytes, String NAME_ID, PublicKeyAccount creator, ItemCls item, byte[] signature) {
        this(typeBytes, NAME_ID, creator, item, (byte) 0, 0l, null);
        this.signature = signature;
        if (this.item.getReference() == null) this.item.setReference(signature);
        //item.resolveKey(DLSet.getInstance());
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

    public String getItemDescription() {
        return item.getDescription();
    }

    // RETURN START KEY in tot GEMESIS
    public abstract long getStartKey(int height);

    @Override
    public boolean hasPublicText() {
        return true;
    }

    //@Override
    @Override
    public void sign(PrivateKeyAccount creator, int forDeal) {
        super.sign(creator, forDeal);
        // in IMPRINT reference already setted before sign
        if (this.item.getReference() == null) this.item.setReference(this.signature);
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
        data = Bytes.concat(data, this.item.toBytes(false, false));

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

        return base_len + this.item.getDataLength(false);

    }

    //VALIDATE

    //@Override
    @Override
    public int isValid(int asDeal, long flags) {

        //CHECK NAME LENGTH
        String name = this.item.getName();
        // TEST ONLY CHARS
        int nameLen = name.length();
        if (nameLen < item.getMinNameLen()
                //&& !BlockChain.DEVELOP_USE
                && this.getBlockHeightByParentOrLast(this.dcSet) > 114000
                ) {
            // IF already in DB
            Pair<Integer, byte[]> pair = BlockChain.NOVA_ASSETS.get(name);
            if (pair == null
                    || this.item.getKey(this.dcSet) > 0
                    || !this.getCreator().equals(pair.getB())) {
                return INVALID_NAME_LENGTH;
            }
        }

        // TEST ALL BYTES for database FIELD
        if (name.getBytes(StandardCharsets.UTF_8).length > ItemCls.MAX_NAME_LENGTH) {
            return INVALID_NAME_LENGTH;
        }

        //CHECK ICON LENGTH
        int iconLength = this.item.getIcon().length;
        if (iconLength < 0 || iconLength > ItemCls.MAX_ICON_LENGTH) {
            return INVALID_ICON_LENGTH;
        }

        //CHECK IMAGE LENGTH
        int imageLength = this.item.getImage().length;
        if (imageLength < 0 || imageLength > ItemCls.MAX_IMAGE_LENGTH) {
            return INVALID_IMAGE_LENGTH;
        }

        //CHECK DESCRIPTION LENGTH
        int descriptionLength = this.item.getDescription().getBytes(StandardCharsets.UTF_8).length;
        if (descriptionLength > BlockChain.MAX_REC_DATA_BYTES) {
            return INVALID_DESCRIPTION_LENGTH;
        }

        return super.isValid(asDeal, flags);

    }

    //PROCESS/ORPHAN
    //@Override
    @Override
    public void process(Block block, int asDeal) {
        //UPDATE CREATOR
        super.process(block, asDeal);

        // SET REFERENCE if not setted before (in Imprint it setted)
        if (this.item.getReference() == null)
            this.item.setReference(this.signature);

        //INSERT INTO DATABASE
        this.item.insertToMap(this.dcSet, this.getStartKey(this.height));

    }

    //@Override
    @Override
    public void orphan(Block block, int asDeal) {
        //UPDATE CREATOR
        super.orphan(block, asDeal);

        //LOGGER.debug("<<<<< org.erachain.core.transaction.IssueItemRecord.orphan 1");
        //DELETE FROM DATABASE
        long key = this.item.removeFromMap(this.dcSet, START_KEY);
        //LOGGER.debug("<<<<< org.erachain.core.transaction.IssueItemRecord.orphan 2");
    }

    @Override
    public HashSet<Account> getInvolvedAccounts() {
        HashSet<Account> accounts = this.getRecipientAccounts();
        accounts.add(this.creator);
        return accounts;
    }

    @Override
    public HashSet<Account> getRecipientAccounts() {
        HashSet<Account> accounts = new HashSet<>(3, 1);
        if (!this.item.getOwner().equals(this.creator)) {
            accounts.add(this.item.getOwner());
        }
        return accounts;
    }

    @Override
    public boolean isInvolved(Account account) {

        String address = account.getAddress();

        if (address.equals(this.creator.getAddress())) {
            return true;
        } else if (address.equals(this.item.getOwner().getAddress())) {
            return true;
        }

        return false;
    }

}
