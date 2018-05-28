package core.transaction;

//import java.math.BigInteger;

import com.google.common.primitives.Bytes;
import core.BlockChain;
import core.block.Block;
import core.item.ItemCls;
import datachain.DCSet;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;

public class GenesisIssue_ItemRecord extends Genesis_Record {

    private ItemCls item;

    public GenesisIssue_ItemRecord(byte type, String NAME_ID, ItemCls item) {
        super(type, NAME_ID);

        this.item = item;
        this.generateSignature();

    }

    //GETTERS/SETTERS

    public ItemCls getItem() {
        return this.item;
    }

    public long getAssetKey(DCSet db) {
        return this.getItem().getKey(db);
    }

    @Override
    public void generateSignature() {

        super.generateSignature();
        // NEED to set an reference
        this.item.setReference(this.signature);

    }


    @Override
    @SuppressWarnings("unchecked")
    //@Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = super.toJson();

        //ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
        transaction.put(this.item.getItemTypeStr(), this.item.toJson());

        return transaction;
    }

    //PARSE CONVERT
    //public abstract Transaction Parse(byte[] data);

    @Override
    public byte[] toBytes(boolean withSign, Long releaserReference) {

        byte[] data = super.toBytes(withSign, releaserReference);

        //WRITE ITEM
        // without reference
        data = Bytes.concat(data, this.item.toBytes(false, false));

        return data;
    }

    @Override
    public int getDataLength(boolean asPack) {
        // not include item REFERENCE
        return BASE_LENGTH + this.item.getDataLength(false);
    }

    //VALIDATE

    @Override
    public int isValid(Long releaserReference, long flags) {

        //CHECK NAME LENGTH
        int nameLength = this.item.getName().getBytes(StandardCharsets.UTF_8).length;
        if (nameLength > ItemCls.MAX_NAME_LENGTH || nameLength < 1) {
            return INVALID_NAME_LENGTH;
        }

        //CHECK DESCRIPTION LENGTH
        int descriptionLength = this.item.getDescription().getBytes(StandardCharsets.UTF_8).length;
        if (descriptionLength > BlockChain.MAX_REC_DATA_BYTES) {
            return INVALID_DESCRIPTION_LENGTH;
        }

        return VALIDATE_OK;
    }

    //PROCESS/ORPHAN

    @Override
    public void process(Block block, boolean asPack) {

        //INSERT INTO DATABASE
        this.item.insertToMap(this.dcSet, 0l);

    }


    @Override
    public void orphan(boolean asPack) {

        //DELETE FROM DATABASE
        this.item.removeFromMap(this.dcSet);

    }

	/*
	@Override
	public boolean isInvolved(Account account)
	{
		return true;
	}
	 */
}
