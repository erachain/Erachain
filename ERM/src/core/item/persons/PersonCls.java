package core.item.persons;

//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
//import java.util.Arrays;
// import org.apache.log4j.Logger;

import org.json.simple.JSONObject;
import utils.ByteArrayUtils;

import com.google.common.primitives.Bytes;
//import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.item.ItemCls;
import core.naming.Name;
import database.DBSet;
import database.Issue_ItemMap;
import database.Item_Map;
import database.NameMap;
import database.ItemPersonMap;
import utils.DateTimeFormat;
import utils.NameUtils;
import utils.Pair;
import utils.NameUtils.NameResult;

//birthLatitude -90..90; birthLongitude -180..180
public abstract class PersonCls extends ItemCls {

	public static final int HUMAN = 1;
	public static final int DOG = 2;
	public static final int CAT = 3;

	protected static final int BIRTHDAY_LENGTH = ItemCls.TIMESTAMP_LENGTH;
	protected static final int DEATHDAY_LENGTH = ItemCls.TIMESTAMP_LENGTH;
	public static final int GENDER_LENGTH = 1;
	protected static final int RACE_SIZE_LENGTH = 1;
	public static final int MAX_RACE_LENGTH = 256 * RACE_SIZE_LENGTH;
	protected static final int LATITUDE_LENGTH = 4;
	protected static final int SKIN_COLOR_SIZE_LENGTH = 1;
	public static final int MAX_SKIN_COLOR_LENGTH = 256 * SKIN_COLOR_SIZE_LENGTH;
	protected static final int EYE_COLOR_SIZE_LENGTH = 1;
	public static final int MAX_EYE_COLOR_LENGTH = 256 * EYE_COLOR_SIZE_LENGTH;
	protected static final int HAIR_COLOR_SIZE_LENGTH = 1;
	public static final int MAX_HAIR_COLOR_LENGTH = 256 * HAIR_COLOR_SIZE_LENGTH;
	public static final int HEIGHT_LENGTH = 1;
	protected static final int BASE_LENGTH = ItemCls.BASE_LENGTH
			+ BIRTHDAY_LENGTH + DEATHDAY_LENGTH + GENDER_LENGTH + RACE_SIZE_LENGTH + LATITUDE_LENGTH * 2
			+ SKIN_COLOR_SIZE_LENGTH + EYE_COLOR_SIZE_LENGTH + HAIR_COLOR_SIZE_LENGTH
			+ HEIGHT_LENGTH;
	
	// already exist in super - protected String name; // First Name|Middle Name|Last Name
	protected long birthday; // timestamp
	protected long deathday; // timestamp
	protected byte gender; // 
	protected String race; 
	protected float birthLatitude;
	protected float birthLongitude;
	protected String skinColor; // First Name|Middle Name|Last Name
	protected String eyeColor; // First Name|Middle Name|Last Name
	protected String hairСolor; // First Name|Middle Name|Last Name	
	protected byte height;
	
	public PersonCls(byte[] typeBytes, Account creator, String name, long birthday, long deathday,
			byte gender, String race, float birthLatitude, float birthLongitude,
			String skinColor, String eyeColor, String hairСolor, byte height, String description)
	{
		super(typeBytes, creator, name, description);
		this.birthday = birthday;
		this.deathday = deathday;
		this.gender = gender;
		this.race = race;
		this.birthLatitude = birthLatitude;
		this.birthLongitude = birthLongitude;
		this.skinColor = skinColor;
		this.eyeColor = eyeColor;
		this.hairСolor = hairСolor;
		this.height = height;
	}
	
	public PersonCls(byte[] typeBytes, Account creator, String name, String birthday, String deathday,
			byte gender, String race, float birthLatitude, float birthLongitude,
			String skinColor, String eyeColor, String hairСolor, byte height, String description)
	{
		this(typeBytes, creator, name, 0, 0,
				gender, race, birthLatitude, birthLongitude,
				skinColor, eyeColor, hairСolor, (byte)height, description);
		this.birthday = Timestamp.valueOf(birthday).getTime();
		this.deathday = deathday==null?Long.MIN_VALUE:Timestamp.valueOf(deathday).getTime();
	}
	
	public PersonCls(int type, Account creator, String name, long birthday, long deathday,
			byte gender, String race, float birthLatitude, float birthLongitude,
			String skinColor, String eyeColor, String hairСolor, byte height, String description)
	{
		this(new byte[]{(byte)type}, creator, name, birthday, deathday,
				gender, race, birthLatitude, birthLongitude,
				skinColor, eyeColor, hairСolor, height, description);
	}

	//GETTERS/SETTERS
	
	public int getItemTypeInt() { return ItemCls.PERSON_TYPE; }
	public String getItemTypeStr() { return "person"; }

	public long getBirthday() {
		return this.birthday;
	}
	public long getDeathday() {
		return this.deathday;
	}
	
	public byte getGender() {
		return this.gender;
	}
	public String getRace() {
		return this.race;
	}
	public float getBirthLatitude() {
		return this.birthLatitude;
	}
	public float getBirthLongitude() {
		return this.birthLongitude;
	}
	public String getSkinColor() {
		return this.skinColor;
	}
	public String getEyeColor() {
		return this.eyeColor;
	}
	public String getHairСolor() {
		return this.hairСolor;
	}
	public int getHeight() {
		return Byte.toUnsignedInt(this.height);
	}	

	// DB
	public Item_Map getDBMap(DBSet db)
	{
		return db.getItemPersonMap();
	}
	public Issue_ItemMap getDBIssueMap(DBSet db)
	{
		return db.getIssuePersonMap();
	}

	// PARSE
	public byte[] toBytes(boolean includeReference)
	{
		
		byte[] data = super.toBytes(includeReference);
				
		// WRITE BIRTHDAY
		byte[] birthdayBytes = Longs.toByteArray(this.birthday);
		birthdayBytes = Bytes.ensureCapacity(birthdayBytes, BIRTHDAY_LENGTH, 0);
		data = Bytes.concat(data, birthdayBytes);

		// WRITE DEATHDAY
		byte[] deathdayBytes = Longs.toByteArray(this.deathday);
		deathdayBytes = Bytes.ensureCapacity(deathdayBytes, DEATHDAY_LENGTH, 0);
		data = Bytes.concat(data, deathdayBytes);

		// WRITE GENDER
		data = Bytes.concat(data, new byte[]{gender});
		
		// WRITE RACE
		byte[] raceBytes = this.race.getBytes(StandardCharsets.UTF_8);
		data = Bytes.concat(data, new byte[]{(byte)raceBytes.length});
		
		//WRITE RACE
		data = Bytes.concat(data, raceBytes);
		
		//WRITE BIRTH_LATITUDE
		byte[] birthLatitudeBytes = ByteArrayUtils.float2ByteArray(this.birthLatitude);
		//birthdayBytes = Bytes.ensureCapacity(birthdayBytes, LATITUDE_LENGTH, 0);
		data = Bytes.concat(data, birthLatitudeBytes);
		
		//WRITE BIRTH_LONGITUDE
		byte[] birthLongitudeBytes = ByteArrayUtils.float2ByteArray(this.birthLongitude);
		//birthdayBytes = Bytes.ensureCapacity(birthdayBytes, LATITUDE_LENGTH, 0);
		data = Bytes.concat(data, birthLongitudeBytes);

		//WRITE SKIN COLOR SIZE
		byte[] skinColorBytes = this.skinColor.getBytes(StandardCharsets.UTF_8);
		data = Bytes.concat(data, new byte[]{(byte)skinColorBytes.length});
		
		//WRITE SKIN COLOR
		data = Bytes.concat(data, skinColorBytes);

		//WRITE EYE COLOR SIZE
		byte[] eyeColorBytes = this.eyeColor.getBytes(StandardCharsets.UTF_8);
		data = Bytes.concat(data, new byte[]{(byte)eyeColorBytes.length});
		
		//WRITE EYE COLOR
		data = Bytes.concat(data, eyeColorBytes);

		//WRITE HAIR COLOR SIZE
		byte[] hairColorBytes = this.hairСolor.getBytes(StandardCharsets.UTF_8);
		data = Bytes.concat(data, new byte[]{(byte)hairColorBytes.length});
		
		//WRITE HAIR COLOR
		data = Bytes.concat(data, hairColorBytes);

		//WRITE HEIGHT
		data = Bytes.concat(data, new byte[]{this.height});

		return data;
	}

	@Override
	public int getDataLength(boolean includeReference) 
	{
		return BASE_LENGTH
				+ this.name.getBytes(StandardCharsets.UTF_8).length
				+ this.description.getBytes(StandardCharsets.UTF_8).length
				+ this.race.getBytes(StandardCharsets.UTF_8).length
				+ this.skinColor.getBytes(StandardCharsets.UTF_8).length
				+ this.eyeColor.getBytes(StandardCharsets.UTF_8).length
				+ this.hairСolor.getBytes(StandardCharsets.UTF_8).length
				+ (includeReference? REFERENCE_LENGTH: 0);
	}	
	
	//OTHER

	@Override
	public String toString(DBSet db)
	{
		long key = this.getKey(db);
		return (key<0?"?":key) + "." + this.typeBytes[0] + " " + this.name + " "
				+ DateTimeFormat.timestamptoString(birthday, "dd-MM-YY","") ;
	}
	
	@Override
	public String getShort(DBSet db)
	{
		long key = this.getKey(db);
		return (key<0?"?":key) + "." + this.typeBytes[0] + " "
				+ this.name.substring(0, Math.min(this.name.length(), 20)) + " "
				+ DateTimeFormat.timestamptoString(birthday, "dd-MM-YY","") ;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONObject personJSON = super.toJson();

		// ADD DATA
		personJSON.put("birthday", this.birthday);
		personJSON.put("deathday", this.deathday);
		personJSON.put("gender",this.gender);
		personJSON.put("race", this.race);
		personJSON.put("birthLatitude", this.birthLatitude);
		personJSON.put("birthLongitude", this.birthLongitude);
		personJSON.put("skinColor", this.skinColor);
		personJSON.put("eyeColor", this.eyeColor);
		personJSON.put("hairСolor", this.hairСolor);
		personJSON.put("height", this.height);
				
		return personJSON;
	}
	
}
