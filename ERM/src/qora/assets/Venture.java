package qora.assets;

//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
//import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
//import database.DBSet;
import qora.account.Account;
import qora.crypto.Base58;
//import qora.crypto.Crypto;
import qora.transaction.Transaction;

public class Venture extends Asset {
	
	protected static final int QUANTITY_LENGTH = 8;
	protected static final int SCALE_LENGTH = 1;
	protected static final int DIVISIBLE_LENGTH = 1;

	private static final int TYPE_ID = Asset.VENTURE;

	protected long quantity;
	protected byte scale;
	protected boolean divisible;

	public Venture(Account creator, String name, String description, long quantity, byte scale, boolean divisible)
	{
		super(TYPE_ID, creator, name, description);
		this.quantity = quantity;
		this.divisible = divisible;
		this.scale = scale;
}

	//GETTERS/SETTERS
	public Long getQuantity() {
		return this.quantity;
	}

	@Override
	public boolean isDivisible() {
		return this.divisible;
	}
	@Override
	public int getScale() {
		return this.scale;
	}

	//PARSE
	// includeReference - TRUE only for store in local DB
	public static Venture parse(byte[] data, boolean includeReference) throws Exception
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
		
		byte[] reference = null;
		if (includeReference)
		{
			//READ REFERENCE
			reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		}

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
		Venture venture = new Venture(creator, name, description, quantity, scale, divisable);
		if (includeReference)
		{
			venture.setReference(reference);
		}

		return venture;
	}
	public byte[] toBytes(boolean includeReference)
	{
		byte[] data = super.toBytes(includeReference);
		
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
				
		return data;
	}

	public int getDataLength(boolean includeReference) 
	{
		return super.getDataLength(includeReference)
				+ SCALE_LENGTH + QUANTITY_LENGTH + DIVISIBLE_LENGTH;
	}	
	
	//OTHER
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONObject assetJSON = super.toJson();

		// ADD DATA
		assetJSON.put("quantity", this.getQuantity());
		assetJSON.put("scale", this.getScale());
		assetJSON.put("isDivisible", this.isDivisible());
		
		return assetJSON;
	}

}
