package core.transaction;

import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 import org.apache.log4j.Logger;

//import ntp.NTP;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.ItemFactory;
import database.Item_Map;
import utils.NumberAsString;
//import database.BalanceMap;
import database.DBSet;

public class GenesisIssue_ItemRecord extends Genesis_Record 
{
	
	private ItemCls item;	
	
	public GenesisIssue_ItemRecord(byte type, String NAME_ID, ItemCls item) 
	{
		super(type, NAME_ID);

		this.item = item;
		this.generateSignature();

	}

	//GETTERS/SETTERS
			
	public ItemCls getItem()
	{
		return this.item;
	}

	public long getAssetKey(DBSet db)
	{
		return this.getItem().getKey(db);
	}

	public void generateSignature() {
		
		super.generateSignature();
		// NEED to set an reference
		this.item.setReference(this.signature);

	}


	@SuppressWarnings("unchecked")
	//@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = super.toJson();
				
		//ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
		transaction.put(this.item.getItemTypeStr(), this.item.toJson());
				
		return transaction;	
	}

	//PARSE CONVERT
	//public abstract Transaction Parse(byte[] data);
	
	@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) 
	{
		
		byte[] data = super.toBytes(withSign, releaserReference);

		//WRITE ITEM
		// without reference
		data = Bytes.concat(data, this.item.toBytes(false));
				
		return data;
	}
	
	@Override
	public int getDataLength(boolean asPack)
	{
		// not include item REFERENCE
		return BASE_LENGTH + this.item.getDataLength(false);
	}
	
	//VALIDATE
		
	@Override
	public int isValid(DBSet db, Long releaserReference) 
	{
		
		//CHECK NAME LENGTH
		int nameLength = this.item.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > ItemCls.MAX_NAME_LENGTH || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK DESCRIPTION LENGTH
		int descriptionLength = this.item.getDescription().getBytes(StandardCharsets.UTF_8).length;
		if(descriptionLength > 4000)
		{
			return INVALID_DESCRIPTION_LENGTH;
		}
				
		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	@Override
	public void process(DBSet db, Block block, boolean asPack)
	{
		
		//INSERT INTO DATABASE
		this.item.insertToMap(db);

	}


	@Override
	public void orphan(DBSet db, boolean asPack) 
	{
																		
		//DELETE FROM DATABASE
		this.item.removeFromMap(db);

	}

	/*
	@Override
	public boolean isInvolved(Account account) 
	{
		return true;
	}
	*/
}
