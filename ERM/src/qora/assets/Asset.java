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

public class Asset {

	private static final int CREATOR_LENGTH = Account.ADDRESS_LENGTH;
	private static final int NAME_SIZE_LENGTH = 4;
	private static final int DESCRIPTION_SIZE_LENGTH = 4;
	private static final int QUANTITY_LENGTH = 8;
	private static final int SCALE_LENGTH = 1;
	private static final int DIVISIBLE_LENGTH = 1;
	private static final int REFERENCE_LENGTH = Transaction.SIGNATURE_LENGTH;
	
	private Account creator;
	private String name;
	private String description;
	private long quantity;
	private byte scale;
	private boolean divisible;
	//private long key;
	private byte[] reference; // this is signature of issued record
	
	public Asset(Account creator, String name, String description, long quantity, byte scale, boolean divisible)
	{
		this.creator = creator;
		this.name = name;
		this.description = description;
		this.quantity = quantity;
		this.divisible = divisible;
		this.scale = scale;
	}

	/*
	public Asset(Account creator, String name, String description, long quantity, byte scale, boolean divisible, byte[] reference)
	{		
		this(creator, name, description, quantity, scale, divisible);
		this.reference = reference; // as signature from transaction IssueAsset
	}
	*/
	//GETTERS/SETTERS
	
	public Account getCreator() {
		return this.creator;
	}
	
	public String getName() {
		return this.name;
	}
	public int getScale() {
		return this.scale;
	}
	public long getKey() {
		// -- return this.key;
		return DBSet.getInstance().getIssueAssetMap().get(this.reference);
	}
	/*
	public void setKey(long key) {
		this.key = key;
	}
	*/
	
	public String getDescription() {
		return this.description;
	}
	
	public Long getQuantity() {
		return this.quantity;
	}
	
	public boolean isDivisible() {
		return this.divisible;
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
	
	//PARSE
	// includeReference - TRUE only for store in local DB
	public static Asset parse(byte[] data, boolean includeReference) throws Exception
	{	

		int position = 0;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		Account creator = new Account(Base58.encode(creatorBytes));
		position += CREATOR_LENGTH;
		
		//READ NAME
		byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		int nameLength = Ints.fromByteArray(nameLengthBytes);
		position += NAME_SIZE_LENGTH;
		
		if(nameLength < 1 || nameLength > 400)
		{
			throw new Exception("Invalid name length");
		}
		
		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String name = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;
				
		//READ DESCRIPTION
		byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
		int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
		position += DESCRIPTION_SIZE_LENGTH;
		
		if(descriptionLength < 1 || descriptionLength > 4000)
		{
			throw new Exception("Invalid description length");
		}
		
		byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
		String description = new String(descriptionBytes, StandardCharsets.UTF_8);
		position += descriptionLength;
		
		//READ QUANTITY
		byte[] quantityBytes = Arrays.copyOfRange(data, position, position + QUANTITY_LENGTH);
		long quantity = Longs.fromByteArray(quantityBytes);	
		position += QUANTITY_LENGTH;
		
		//READ SCALE
		byte[] scaleBytes = Arrays.copyOfRange(data, position, position + SCALE_LENGTH);
		byte scale = scaleBytes[0];
		position += SCALE_LENGTH;

		//READ DIVISABLE
		byte[] divisibleBytes = Arrays.copyOfRange(data, position, position + DIVISIBLE_LENGTH);
		boolean divisable = divisibleBytes[0] == 1;
		position += DIVISIBLE_LENGTH;		
		
		//RETURN
		Asset asset = new Asset(creator, name, description, quantity, scale, divisable);

		if (includeReference)
		{
			//READ REFERENCE
			byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			asset.setReference(reference);
		}
		return asset;
	}
	
	public byte[] toBytes(boolean includeReference)
	{
		byte[] data = new byte[0];
		
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
		
		//WRITE QUANTITY
		byte[] quantityBytes = Longs.toByteArray(this.quantity);
		data = Bytes.concat(data, quantityBytes);
		
		//WRITE SCALE_LENGTH
		//byte[] scaleBytes = new byte[this.scale];
		byte[] scaleBytes = new byte[1];
		scaleBytes[0] = this.scale;
		data = Bytes.concat(data, scaleBytes);
		

		//WRITE DIVISIBLE
		byte[] divisibleBytes = new byte[1];
		divisibleBytes[0] = (byte) (this.divisible == true ? 1 : 0);
		data = Bytes.concat(data, divisibleBytes);
		
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
		return CREATOR_LENGTH + NAME_SIZE_LENGTH + this.name.getBytes(StandardCharsets.UTF_8).length + DESCRIPTION_SIZE_LENGTH + this.description.getBytes(StandardCharsets.UTF_8).length + SCALE_LENGTH + QUANTITY_LENGTH + DIVISIBLE_LENGTH
				+ (includeReference? REFERENCE_LENGTH: 0);
	}	
	
	//OTHER
	
	public String toString()
	{		
		return "(" + this.getKey() + ") " + this.getName();
	}
	
	public String getShort()
	{
		return "(" + this.getKey() + ")" + this.getName().substring(0, Math.min(this.getName().length(), 4));
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONObject assetJSON = new JSONObject();

		// ADD DATA
		assetJSON.put("key", this.getKey());
		assetJSON.put("name", this.getName());
		assetJSON.put("description", this.getDescription());
		assetJSON.put("creator", this.getCreator().getAddress());
		assetJSON.put("quantity", this.getQuantity());
		assetJSON.put("scale", this.getScale());
		assetJSON.put("isDivisible", this.isDivisible());
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
