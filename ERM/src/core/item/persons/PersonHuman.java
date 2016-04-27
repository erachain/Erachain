package core.item.persons;

//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
 import org.apache.log4j.Logger;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.crypto.Base58;
import utils.ByteArrayUtils;

public class PersonHuman extends PersonCls {
	
	private static final int TYPE_ID = PersonCls.HUMAN;

	public PersonHuman(Account creator, String fullName, long birthday,
			byte gender, String race, float birthLatitude, float birthLongitude,
			String skinColor, String eyeColor, String hairСolor, int height, String description)
	{
		super(new byte[]{(byte)TYPE_ID, 0}, creator, fullName, birthday,
				gender, race, birthLatitude, birthLongitude,
				skinColor, eyeColor, hairСolor, (byte)height, description);
	}
	public PersonHuman(Account creator, String fullName, String birthday,
			byte gender, String race, float birthLatitude, float birthLongitude,
			String skinColor, String eyeColor, String hairСolor, int height, String description)
	{
		super(new byte[]{(byte)TYPE_ID, 0}, creator, fullName, birthday,
				gender, race, birthLatitude, birthLongitude,
				skinColor, eyeColor, hairСolor, (byte)height, description);
	}
	public PersonHuman(byte[] typeBytes, Account creator, String fullName, long birthday,
			byte gender, String race, float birthLatitude, float birthLongitude,
			String skinColor, String eyeColor, String hairСolor, int height, String description)
	{
		super(typeBytes, creator, fullName, birthday,
				gender, race, birthLatitude, birthLongitude,
				skinColor, eyeColor, hairСolor, (byte)height, description);
	}

	//GETTERS/SETTERS
	public String getItemSubType() { return "human"; }

	//PARSE
	// TODO - когда нулевая длдлинна и ошибка - но в ГУИ ошибка нне высветилась и создалась плоая запись и она развалила сеть
	// includeReference - TRUE only for store in local DB
	public static PersonHuman parse(byte[] data, boolean includeReference) throws Exception
	{	

		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		Account creator = new Account(Base58.encode(creatorBytes));
		position += CREATOR_LENGTH;
		
		//READ FULL NAME
		int fullNameLength = Byte.toUnsignedInt(data[position]);
		position ++;
		
		if(fullNameLength < 1 || fullNameLength > MAX_NAME_LENGTH)
		{
			throw new Exception("Invalid full name length");
		}
		
		byte[] fullNameBytes = Arrays.copyOfRange(data, position, position + fullNameLength);
		String fullName = new String(fullNameBytes, StandardCharsets.UTF_8);
		position += fullNameLength;
				
		//READ DESCRIPTION
		byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
		int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
		position += DESCRIPTION_SIZE_LENGTH;
		
		if(descriptionLength < 0 || descriptionLength > 4000)
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
				
		//READ BIRTDAY
		byte[] birthdayBytes = Arrays.copyOfRange(data, position, position + BIRTHDAY_LENGTH);
		long birthday = Longs.fromByteArray(birthdayBytes);	
		position += BIRTHDAY_LENGTH;

		//READ GENDER
		byte gender = data[position];
		position ++;
		
		//READ RACE
		int raceLength = Byte.toUnsignedInt(data[position]);
		position ++;
		
		if(raceLength < 0 || raceLength > MAX_RACE_LENGTH)
		{
			throw new Exception("Invalid race length");
		}
		
		byte[] raceBytes = Arrays.copyOfRange(data, position, position + raceLength);
		String race = new String(raceBytes, StandardCharsets.UTF_8);
		position += raceLength;

		//READ BIRTH LATITUDE
		float birthLatitude = ByteArrayUtils.ByteArray2float(Arrays.copyOfRange(data, position, position + LATITUDE_LENGTH));
		position += LATITUDE_LENGTH;

		//READ BIRTH LONGITUDE
		float birthLongitude = ByteArrayUtils.ByteArray2float(Arrays.copyOfRange(data, position, position + LATITUDE_LENGTH));
		position += LATITUDE_LENGTH;

		//READ SKIN COLOR LENGTH
		int skinColorLength = Byte.toUnsignedInt(data[position]);
		position ++;
		
		if(skinColorLength < 0 || skinColorLength > MAX_SKIN_COLOR_LENGTH)
		{
			throw new Exception("Invalid skin color length");
		}
		
		byte[] skinColorBytes = Arrays.copyOfRange(data, position, position + skinColorLength);
		String skinColor = new String(skinColorBytes, StandardCharsets.UTF_8);
		position += skinColorLength;

		//READ EYE COLOR LENGTH
		int eyeColorLength = Byte.toUnsignedInt(data[position]);
		position ++;
		
		if(eyeColorLength < 0 || eyeColorLength > MAX_EYE_COLOR_LENGTH)
		{
			throw new Exception("Invalid eye color length");
		}
		
		byte[] eyeColorBytes = Arrays.copyOfRange(data, position, position + eyeColorLength);
		String eyeColor = new String(eyeColorBytes, StandardCharsets.UTF_8);
		position += eyeColorLength;

		//READ HAIR COLOR LENGTH
		int hairСolorLength = Byte.toUnsignedInt(data[position]);
		position ++;
		
		if(hairСolorLength < 0 || hairСolorLength > MAX_HAIR_COLOR_LENGTH)
		{
			throw new Exception("Invalid hair color length");
		}
		
		byte[] hairСolorBytes = Arrays.copyOfRange(data, position, position + hairСolorLength);
		String hairСolor = new String(hairСolorBytes, StandardCharsets.UTF_8);
		position += hairСolorLength;

		//READ HEIGHT
		byte height = data[position];
		position ++;

		//RETURN
		PersonHuman personHuman = new PersonHuman(typeBytes, creator, fullName, birthday,
				gender, race, birthLatitude, birthLongitude,
				skinColor, eyeColor, hairСolor, height, description);
		if (includeReference)
		{
			personHuman.setReference(reference);
		}

		return personHuman;
	}
	
}
