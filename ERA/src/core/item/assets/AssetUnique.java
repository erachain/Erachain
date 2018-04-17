package core.item.assets;

//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

//import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import core.BlockChain;
import core.account.PublicKeyAccount;
import datachain.DCSet;

public class AssetUnique extends AssetCls {

	private static final int TYPE_ID = AssetCls.UNIQUE;

	public AssetUnique(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		super(typeBytes, owner, name, icon, image, description);
	}
	public AssetUnique(int props, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		this(new byte[]{(byte)TYPE_ID, (byte)props}, owner, name, icon, image, description);
	}
	public AssetUnique(PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		this(new byte[]{(byte)TYPE_ID, (byte)0}, owner, name, icon, image, description);
	}

	//GETTERS/SETTERS
	@Override
	public String getItemSubType() { return "unique"; }

	@Override
	public int getMinNameLen() { return 12; }

	@Override
	public Long getQuantity() {
		return 1L;
	}
	@Override
	public Long getTotalQuantity(DCSet dc) {
		return 1L;
	}

	//PARSE
	// includeReference - TRUE only for store in local DB
	public static AssetUnique parse(byte[] data, boolean includeReference) throws Exception
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
			throw new Exception("Invalid name length");
		}

		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String name = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;

		//READ ICON
		byte[] iconLengthBytes = Arrays.copyOfRange(data, position, position + ICON_SIZE_LENGTH);
		int iconLength = Ints.fromBytes( (byte)0, (byte)0, iconLengthBytes[0], iconLengthBytes[1]);
		position += ICON_SIZE_LENGTH;

		if(iconLength < 0 || iconLength > MAX_ICON_LENGTH)
		{
			throw new Exception("Invalid icon length");
		}

		byte[] icon = Arrays.copyOfRange(data, position, position + iconLength);
		position += iconLength;

		//READ IMAGE
		byte[] imageLengthBytes = Arrays.copyOfRange(data, position, position + IMAGE_SIZE_LENGTH);
		int imageLength = Ints.fromByteArray(imageLengthBytes);
		position += IMAGE_SIZE_LENGTH;

		if(imageLength < 0 || imageLength > MAX_IMAGE_LENGTH)
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

		//RETURN
		AssetUnique statement = new AssetUnique(typeBytes, owner, name, icon, image, description);

		if (includeReference)
		{
			//READ REFERENCE
			byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			statement.setReference(reference);
		}
		return statement;
	}

	//OTHER

}
