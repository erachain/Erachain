package core.item.assets;

import java.math.BigDecimal;
//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
 import org.apache.log4j.Logger;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;

import com.google.common.primitives.Bytes;
//import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.transaction.Transaction;

public class AssetVenture extends AssetCls {
	
	protected static final int QUANTITY_LENGTH = 8;
	protected static final int SCALE_LENGTH = 1;
	protected static final int DIVISIBLE_LENGTH = 1;

	private static final int TYPE_ID = AssetCls.VENTURE;

	protected long quantity = 0;
	protected byte scale;
	protected boolean divisible;

	public AssetVenture(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, long quantity, byte scale, boolean divisible)
	{
		super(typeBytes, owner, name, icon, image, description);
		this.quantity = quantity;
		this.divisible = divisible;
		this.scale = scale;
	}
	public AssetVenture(int props, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, long quantity, byte scale, boolean divisible)
	{
		this(new byte[]{(byte)TYPE_ID, (byte)props}, owner, name, icon, image, description, quantity, scale, divisible);
	}
	public AssetVenture(PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, boolean movable, long quantity, byte scale, boolean divisible)
	{
		this(new byte[]{(byte)TYPE_ID, movable?(byte)1:(byte)0}, owner, name, icon, image, description, quantity, scale, divisible);
	}

	//GETTERS/SETTERS
	
	public String getItemSubType() { return "venture"; }

	public Long getQuantity() {		
		return this.quantity;
	}

	public Long getTotalQuantity() {
		
		if (this.quantity == 0) {
			// IF UNLIMIT QIUNTITY
			Tuple3<BigDecimal, BigDecimal, BigDecimal> bals = this.getOwner().getBalance(this.getKey());
			long bal = -bals.a.longValue();
			if (bal == 0) {
				bal = 1l;
			}
			return bal;
		} else {
			return this.quantity;
		}
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
	public static AssetVenture parse(byte[] data, boolean includeReference) throws Exception
	{	

		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;
		
		//READ CREATOR
		byte[] ownerBytes = Arrays.copyOfRange(data, position, position + OWNER_LENGTH);
		PublicKeyAccount owner = new PublicKeyAccount(ownerBytes);
		position += OWNER_LENGTH;
		
		//READ NAME
		//byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		//int nameLength = Ints.fromByteArray(nameLengthBytes);
		//position += NAME_SIZE_LENGTH;
		int nameLength = Byte.toUnsignedInt(data[position]);
		position ++;
		
		if(nameLength < 1 || nameLength > MAX_NAME_LENGTH)
		{
			throw new Exception("Invalid name length: " + nameLength);
		}
		
		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String name = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;
				
		//READ ICON
		byte[] iconLengthBytes = Arrays.copyOfRange(data, position, position + ICON_SIZE_LENGTH);
		int iconLength = Ints.fromBytes( (byte)0, (byte)0, iconLengthBytes[0], iconLengthBytes[1]);
		position += ICON_SIZE_LENGTH;
		
		if(iconLength > MAX_ICON_LENGTH)
		{
			throw new Exception("Invalid icon length");
		}
		
		byte[] icon = Arrays.copyOfRange(data, position, position + iconLength);
		position += iconLength;

		//READ IMAGE
		byte[] imageLengthBytes = Arrays.copyOfRange(data, position, position + IMAGE_SIZE_LENGTH);
		int imageLength = Ints.fromByteArray(imageLengthBytes);
		position += IMAGE_SIZE_LENGTH;
		
		if(imageLength > MAX_IMAGE_LENGTH)
		{
			throw new Exception("Invalid image length");
		}
		
		byte[] image = Arrays.copyOfRange(data, position, position + imageLength);
		position += imageLength;

		//READ DESCRIPTION
		byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
		int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
		position += DESCRIPTION_SIZE_LENGTH;
		
		if(descriptionLength > BlockChain.MAX_REC_DATA_BYTES)
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
			position += REFERENCE_LENGTH;
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
		AssetVenture venture = new AssetVenture(typeBytes, owner, name, icon, image, description, quantity, scale, divisable);
		if (includeReference)
		{
			venture.setReference(reference);
		}

		return venture;
	}
	public byte[] toBytes(boolean includeReference, boolean onlyBody)
	{
		byte[] data = super.toBytes(includeReference, onlyBody);
		
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
