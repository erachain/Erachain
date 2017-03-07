package core.item;

import java.nio.charset.Charset;
//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
// import org.apache.log4j.Logger;
import java.util.Arrays;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple6;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
//import com.google.common.primitives.Longs;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.transaction.Transaction;
import database.DBSet;
//import database.DBMap;
import database.Item_Map;
import database.wallet.FavoriteItem;
import lang.Lang;
import database.Issue_ItemMap;

import utils.Pair;

public abstract class ItemCls {

	public static final int ASSET_TYPE = 1;
	public static final int IMPRINT_TYPE = 2;
	public static final int NOTE_TYPE = 3;
	public static final int PERSON_TYPE = 4;
	public static final int STATUS_TYPE = 5;
	public static final int UNION_TYPE = 6;

	protected static final int TYPE_LENGTH = 2;
	protected static final int OWNER_LENGTH = PublicKeyAccount.PUBLIC_KEY_LENGTH;
	protected static final int NAME_SIZE_LENGTH = 1;
	public static final int MAX_NAME_LENGTH = (int) Math.pow(256, NAME_SIZE_LENGTH) - 1;
	protected static final int ICON_SIZE_LENGTH = 2;
	public static final int MAX_ICON_LENGTH = (int) Math.pow(256, ICON_SIZE_LENGTH) - 1;
	protected static final int IMAGE_SIZE_LENGTH = 4;
	public static final int MAX_IMAGE_LENGTH = (int) Math.pow(256, IMAGE_SIZE_LENGTH) - 1;
	protected static final int DESCRIPTION_SIZE_LENGTH = 4;
	protected static final int REFERENCE_LENGTH = Transaction.SIGNATURE_LENGTH;
	protected static final int BASE_LENGTH = TYPE_LENGTH + OWNER_LENGTH + NAME_SIZE_LENGTH + ICON_SIZE_LENGTH + IMAGE_SIZE_LENGTH + DESCRIPTION_SIZE_LENGTH;
		
	protected static final int TIMESTAMP_LENGTH = Transaction.TIMESTAMP_LENGTH;

	//protected DBMap dbMap;
	//protected DBMap dbIssueMap;
	
	protected String TYPE_NAME = "unknown";
	protected byte[] typeBytes;
	protected PublicKeyAccount owner;
	protected String name;
	protected String description;
	protected long key = 0;
	protected byte[] reference = null; // this is signature of issued record
	protected byte[] icon;
	protected byte[] image;
	
	public ItemCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		this.typeBytes = typeBytes;
		this.owner = owner;
		this.name = name;
		this.description = description;
		this.icon = icon == null? new byte[0]: icon;
		this.image = image == null? new byte[0]: image;
		
	}
	public ItemCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		this(new byte[TYPE_LENGTH], owner, name, icon, image, description);
		this.typeBytes[0] = (byte)type;
	}

	//GETTERS/SETTERS

	public abstract int getItemTypeInt();
	public abstract String getItemTypeStr();
	public abstract String getItemSubType();

	public abstract Item_Map getDBMap(DBSet db);
	public abstract Issue_ItemMap getDBIssueMap(DBSet db);
	//public abstract FavoriteItem getDBFavoriteMap();

	public static Pair<Integer, Long> resolveDateFromStr(String str, Long defaultVol)
	{
		if (str.length() == 0) return new Pair<Integer, Long>(0, defaultVol);
		else if (str.length() == 1)
		{
			if (str == "+")
				return new Pair<Integer, Long>(0, Long.MAX_VALUE);
			else if (str == "-")
				return new Pair<Integer, Long>(0, Long.MIN_VALUE);
			else
				return new Pair<Integer, Long>(0, defaultVol);
		}
		else {
			try {
				Long date = Long.parseLong(str);
				return new Pair<Integer, Long>(0, date);
			}
			catch(Exception e)			
			{
				return new Pair<Integer, Long>(-1, 0l);				
			}
		}
	}
	
	public static Pair<Integer, Integer> resolveEndDayFromStr(String str, Integer defaultVol)
	{
		if (str.length() == 0) return new Pair<Integer, Integer>(0, defaultVol);
		else if (str.length() == 1)
		{
			if (str == "+")
				return new Pair<Integer, Integer>(0, Integer.MAX_VALUE);
			else if (str == "-")
				return new Pair<Integer, Integer>(0, Integer.MIN_VALUE);
			else
				return new Pair<Integer, Integer>(0, defaultVol);
		}
		else {
			try {
				Integer date = Integer.parseInt(str);
				return new Pair<Integer, Integer>(0, date);
			}
			catch(Exception e)			
			{
				return new Pair<Integer, Integer>(-1, 0);				
			}
		}
	}

	public byte[] getType()
	{
		return this.typeBytes;
	}

	public byte getProps()
	{
		return this.typeBytes[1];
	}
	public void setProps(byte props)
	{
		this.typeBytes[1] = props;
	}

	public PublicKeyAccount getOwner() {
		return this.owner;
	}
	
	public String getName() {
		return this.name;
	}
	public byte[] getIcon() {
		return this.icon;
	}
	public byte[] getImage() {
		return this.image;
	}
	
	
	public long getKey() {
		return getKey(DBSet.getInstance());
	}
	public long getKey(DBSet db) {
		// resolve key in that DB
		resolveKey(db);
		return this.key;
	}
	public long resolveKey(DBSet db) {
		if (this.key == 0 // & this.reference != null
				) {
			if (this.getDBIssueMap(db).contains(this.reference)) {
				this.key = this.getDBIssueMap(db).get(this.reference);
			}
		}
		return this.key;
	}
	public void setKey(long key) {
		this.key = key;
	}
	public void resetKey() {
		this.key = 0;
	}
	
	public static ItemCls getItem(DBSet db, int type, long key) {
		//return Controller.getInstance().getItem(db, type, key);
		return db.getItem_Map(type).get(key);
	}
	
	public String getDescription() {
		return this.description;
	}
		
	public byte[] getReference() {
		return this.reference;
	}
	public void setReference(byte[] reference) {
		// TODO - if few itens issued in one recor - need reference to include nonce here
		this.reference = reference;

	}
		
	public boolean isConfirmed() {
		return isConfirmed(DBSet.getInstance());
	}
	
	public boolean isConfirmed(DBSet db) {
		return this.getDBIssueMap(db).contains(this.reference);
	}	

	public boolean isFavorite() {
		return Controller.getInstance().isItemFavorite(this);
	}

	// forOwnerSign - use only DATA needed for making signature
	public byte[] toBytes(boolean includeReference, boolean forOwnerSign)
	{

		byte[] data = new byte[0];
		boolean useAll = !forOwnerSign;
		
		if (useAll) {
			//WRITE TYPE
			data = Bytes.concat(data, this.typeBytes);
		}

		if (useAll) {
			//WRITE OWNER
			try
			{
				data = Bytes.concat(data, this.owner.getPublicKey());
			}
			catch(Exception e)
			{
				//DECODE EXCEPTION
			}
		}

		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		if (useAll) {
			//WRITE NAME SIZE
			data = Bytes.concat(data, new byte[]{(byte)nameBytes.length});
		}
		
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);

		if (useAll) {
			//WRITE ICON SIZE - 2 bytes = 64kB max
			int iconLength = this.icon.length;
			byte[] iconLengthBytes = Ints.toByteArray(iconLength);
			data = Bytes.concat(data, new byte[]{iconLengthBytes[2], iconLengthBytes[3]});
					
			//WRITE ICON
			data = Bytes.concat(data, this.icon);
		}

		if (useAll) {
			//WRITE IMAGE SIZE
			int imageLength = this.image.length;
			byte[] imageLengthBytes = Ints.toByteArray(imageLength);
			data = Bytes.concat(data, imageLengthBytes);
		}
				
		//WRITE IMAGE
		data = Bytes.concat(data, this.image);
		
		byte[] descriptionBytes = this.description.getBytes(StandardCharsets.UTF_8);
		if (useAll) {
			//WRITE DESCRIPTION SIZE
			int descriptionLength = descriptionBytes.length;
			byte[] descriptionLengthBytes = Ints.toByteArray(descriptionLength);
			data = Bytes.concat(data, descriptionLengthBytes);
		}
				
		//WRITE DESCRIPTION
		data = Bytes.concat(data, descriptionBytes);
		
		if(useAll && includeReference)
		{
			//WRITE REFERENCE
			data = Bytes.concat(data, this.reference);
		}
		
		return data;
	}

	public int getDataLength(boolean includeReference) 
	{
		return BASE_LENGTH
				+ this.name.getBytes(StandardCharsets.UTF_8).length
				+ this.icon.length
				+ this.image.length
				+ this.description.getBytes(StandardCharsets.UTF_8).length
				+ (includeReference? REFERENCE_LENGTH: 0);
	}

	//OTHER
	
	public String toString(DBSet db)
	{		
		long key = this.getKey(db);
		String creator = this.owner.getAddress().equals(Account.EMPTY_PUBLICK_ADDRESS)? "GENESIS": this.owner.getPersonAsString_01(false);
		return (key==0?"?:":key
				//+ "." + this.typeBytes[0]
				+ " ") + this.getName()  
				+ (creator.length()==0?"": " (" +creator + ")");
	}
	
	
	public String toString(DBSet db, byte[] data) {
		String str = this.toString(db);
		
		Tuple6<Long, Long, byte[], byte[], Long, byte[]> tuple = core.transaction.R_SetStatusToItem.unpackData(data);
		
		if (str.contains("%1") && tuple.a != null)
			str = str.replace("%1", tuple.a.toString());
		if (str.contains("%2") && tuple.b != null)
			str = str.replace("%2", tuple.b.toString());
		if (str.contains("%3") && tuple.c != null)
			str = str.replace("%3", new String(tuple.c, Charset.forName("UTF-8")));
		if (str.contains("%4") && tuple.d != null)
			str = str.replace("%4", new String(tuple.d, Charset.forName("UTF-8")));
		if (str.contains("%D") && tuple.f != null)
			str = str.replace("%D", new String(new String(tuple.f, Charset.forName("UTF-8"))));
		
		return str;
	}

	public String toString()
	{
		return toString(DBSet.getInstance());
	}
	
	public String getShort(DBSet db)
	{
		long key = this.getKey(db);
		String creator = this.owner.getAddress().equals(Account.EMPTY_PUBLICK_ADDRESS)? "GENESIS": this.owner.getPersonAsString_01(true);
		return (key<1?"? ":key + ": ") + this.name.substring(0, Math.min(this.name.length(), 30))
				+ (creator.length()==0?"": " (" +creator + ")");
	}
	public String getShort()
	{
		return getShort(DBSet.getInstance());
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONObject itemJSON = new JSONObject();

		// ADD DATA
		itemJSON.put("item_type", this.getItemTypeStr());
		itemJSON.put("item_type_sub", this.getItemSubType());
		itemJSON.put("type0", Byte.toUnsignedInt(this.typeBytes[0]));
		itemJSON.put("type1", Byte.toUnsignedInt(this.typeBytes[1]));
		itemJSON.put("key", this.key);
		itemJSON.put("name", this.name);
		itemJSON.put("description", this.description);
		itemJSON.put("creator", this.owner.getAddress());
		itemJSON.put("isConfirmed", this.isConfirmed());
		itemJSON.put("reference", Base58.encode(this.reference));
		
		Transaction txReference = Controller.getInstance().getTransaction(this.reference);
		if(txReference != null)
		{
			itemJSON.put("timestamp", txReference.getTimestamp());
		}
		
		return itemJSON;
	}
	@SuppressWarnings("unchecked")
	public JSONObject toJsonData() {
		
		JSONObject itemJSON = new JSONObject();

		// ADD DATA
		itemJSON.put("icon", Base58.encode(this.icon));
		itemJSON.put("image", Base58.encode(this.image));
		
		return itemJSON;
	}

	//
	public void insertToMap(DBSet db, long startKey)
	{
		//INSERT INTO DATABASE
		Item_Map dbMap = this.getDBMap(db);
		long key = dbMap.getSize();
		if (key < startKey) {
			// IF this not GENESIS issue - start from 1000
			dbMap.setSize(startKey);
		}
		key = dbMap.add(this);
		
		//SET ORPHAN DATA
		this.getDBIssueMap(db).set(this.reference, key);
		//this.key = key;
		
		//return key;
		
	}
	
	public long removeFromMap(DBSet db)
	{
		//DELETE FROM DATABASE
		Issue_ItemMap issueDB = this.getDBIssueMap(db);
		//long key = ;
		this.getDBMap(db).delete(this.getKey());	
				
		//DELETE ORPHAN DATA
		issueDB.delete(this.reference);
		
		return key;

	}

}
