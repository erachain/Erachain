package qora.assets;

//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
import database.DBSet;
import qora.account.Account;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.transaction.Transaction;

public abstract class Asset {

	public static final int STATEMENT = 1;
	public static final int NAME = 2;
	public static final int VENTURE = 3;

	protected static final int TYPE_LENGTH = 4;
	protected static final int CREATOR_LENGTH = Account.ADDRESS_LENGTH;
	protected static final int NAME_SIZE_LENGTH = 4;
	protected static final int DESCRIPTION_SIZE_LENGTH = 4;
	protected static final int REFERENCE_LENGTH = Transaction.SIGNATURE_LENGTH;
	protected static final int BASE_LENGTH = TYPE_LENGTH + CREATOR_LENGTH + NAME_SIZE_LENGTH + DESCRIPTION_SIZE_LENGTH;
	
	protected int type;
	protected Account creator;
	protected String name;
	protected String description;
	protected long key = -1;
	protected byte[] reference; // this is signature of issued record
	
	public Asset(int type, Account creator, String name, String description)
	{
		this.type = type;
		this.creator = creator;
		this.name = name;
		this.description = description;
	}

	//GETTERS/SETTERS
	
	public int getType()
	{
		return this.type;
	}

	public Account getCreator() {
		return this.creator;
	}
	
	public String getName() {
		return this.name;
	}
	public long getKey() {
		return this.getKey(DBSet.getInstance());
	}
	public long getKey(DBSet db) {
		// TODO if ophran ?
		if (this.key <0) this.key = db.getIssueAssetMap().get(this.reference);
		return this.key;
	}
	
	public String getDescription() {
		return this.description;
	}
		
	public byte[] getReference() {
		return this.reference;
	}
	public void setReference(byte[] reference) {
		this.reference = reference;
	}
		
	public boolean isConfirmed() {
		return DBSet.getInstance().getIssueAssetMap().contains(this.reference);
	}
	
	public Long getQuantity() {
		return 1l;
	}

	public boolean isDivisible() {
		return true;
	}
	public int getScale() {
		return 8;
	}

	public byte[] toBytes(boolean includeReference)
	{
		byte[] data = new byte[0];

		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(type);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);

		//WRITE CREATOR
		try
		{
			data = Bytes.concat(data , Base58.decode(this.creator.getAddress()));
		}
		catch(Exception e)
		{
			//DECODE EXCEPTION
		}
		
		//WRITE NAME SIZE
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);
		
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);
		
		//WRITE DESCRIPTION SIZE
		byte[] descriptionBytes = this.description.getBytes(StandardCharsets.UTF_8);
		int descriptionLength = descriptionBytes.length;
		byte[] descriptionLengthBytes = Ints.toByteArray(descriptionLength);
		data = Bytes.concat(data, descriptionLengthBytes);
				
		//WRITE DESCRIPTION
		data = Bytes.concat(data, descriptionBytes);
		
		if(includeReference)
		{
			//WRITE REFERENCE
			data = Bytes.concat(data, this.reference);
		}
		else
		{
			//WRITE EMPTY REFERENCE
			// data = Bytes.concat(data, new byte[64]);
		}
		
		return data;
	}

	public int getDataLength(boolean includeReference) 
	{
		return BASE_LENGTH
				+ this.name.getBytes(StandardCharsets.UTF_8).length
				+ this.description.getBytes(StandardCharsets.UTF_8).length
				+ (includeReference? REFERENCE_LENGTH: 0);
	}	
	
	//OTHER
	
	public String toString()
	{		
		return "(" + this.getKey() + ":" + this.type + ") " + this.getName();
	}
	
	public String getShort()
	{
		return "(" + this.getKey() + ":" + this.type + ") " + this.getName().substring(0, Math.min(this.getName().length(), 4));
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONObject assetJSON = new JSONObject();

		// ADD DATA
		assetJSON.put("type", this.getType());
		assetJSON.put("key", this.getKey());
		assetJSON.put("name", this.getName());
		assetJSON.put("description", this.getDescription());
		assetJSON.put("creator", this.getCreator().getAddress());
		assetJSON.put("isConfirmed", this.isConfirmed());
		assetJSON.put("reference", Base58.encode(this.getReference()));
		
		Transaction txReference = Controller.getInstance().getTransaction(this.getReference());
		if(txReference != null)
		{
			assetJSON.put("timestamp", txReference.getTimestamp());
		}
		
		return assetJSON;
	}
}
