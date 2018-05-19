package core.item.polls;

//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Ints;

import core.BlockChain;
import core.account.PublicKeyAccount;
import core.voting.PollOption;

public class Poll extends PollCls {
	
	private static final int TYPE_ID = PollCls.POLL;

	public Poll(PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, List<PollOption> options)
	{
		super(TYPE_ID, owner, name, icon, image, description, options);
	}
	public Poll(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, List<PollOption> options)
	{
		super(typeBytes, owner, name, icon, image, description, options);
	}

	//GETTERS/SETTERS
	public String getItemSubType() { return "poll"; }

	public int getMinNameLen() { return 12; }

	//PARSE
	// includeReference - TRUE only for store in local DB
	public static Poll parse(byte[] data, boolean includeReference) throws Exception
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
		
		if(descriptionLength < 1 || descriptionLength > BlockChain.MAX_REC_DATA_BYTES)
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
		
		//READ OPTIONS SIZE
		byte[] optionsLengthBytes = Arrays.copyOfRange(data, position, position + OPTIONS_SIZE_LENGTH);
		int optionsLength = Ints.fromByteArray(optionsLengthBytes);
		position += OPTIONS_SIZE_LENGTH;

		if(optionsLength < 1 || optionsLength > 100)
		{
			throw new Exception("Invalid options length");
		}

		//READ OPTIONS
		List<PollOption> options = new ArrayList<PollOption>();
		for(int i=0; i<optionsLength; i++)
		{
			PollOption option = PollOption.parse(Arrays.copyOfRange(data, position, data.length));
			position += option.getDataLength();
			options.add(option);
		}

		//RETURN
		Poll poll = new Poll(typeBytes, owner, name, icon, image, description, options);
		if (includeReference)
		{
			poll.setReference(reference);
		}

		return poll;
	}
	
}
