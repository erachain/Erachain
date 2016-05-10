package core.transaction;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.EnumSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.statuses.StatusCls;
import core.item.ItemCls;
import ntp.NTP;
import database.DBSet;
import database.DBMap;

// this.end_date = 0 (ALIVE PERMANENT), = -1 (ENDED), = Integer - different
// typeBytes[1] - version =0 - not need sign by person;
// 		 =1 - need sign by person
// typeBytes[2] - size of personalized accounts
public class R_SetStatusToItem extends Transaction {

	private static final byte TYPE_ID = (byte)Transaction.SET_STATUS_TRANSACTION;
	private static final String NAME_ID = "Set Status";
	private static final int DATE_DAY_LENGTH = 4; // one year + 256 days max
	private static final BigDecimal MIN_ERM_BALANCE = BigDecimal.valueOf(1000).setScale(8);
	// need RIGHTS for non PERSON account
	private static final BigDecimal GENERAL_ERM_BALANCE = BigDecimal.valueOf(100000).setScale(8);

	protected Long key; // STATUS KEY
	protected ItemCls item; // ITEM
	protected Integer end_date = 0; // in days; 0 - permanent active
	private static final int SELF_LENGTH = DATE_DAY_LENGTH + KEY_LENGTH;
	
	protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + SELF_LENGTH;
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + SELF_LENGTH;

	public R_SetStatusToItem(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key, ItemCls item,
			int end_date, long timestamp, byte[] reference) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);		

		this.key = key;
		this.item = item;
		this.end_date = end_date;		
	}

	public R_SetStatusToItem(PublicKeyAccount creator, byte feePow, long key, ItemCls item,
			int end_date, long timestamp, byte[] reference) {
		this(new byte[]{TYPE_ID, (byte)0, 0, 0}, creator, feePow, key, item,
				end_date, timestamp, reference);
	}
	// set default date
	public R_SetStatusToItem(PublicKeyAccount creator, byte feePow, long key, ItemCls item,
			long timestamp, byte[] reference) {
		this(new byte[]{TYPE_ID, (byte)0, 0, 0}, creator, feePow, key, item,
				0, timestamp, reference);
	}
	public R_SetStatusToItem(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key, ItemCls item,
			int end_date, long timestamp, byte[] reference, byte[] signature) {
		this(typeBytes, creator, feePow, key, item,
				end_date, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	// as pack
	public R_SetStatusToItem(byte[] typeBytes, PublicKeyAccount creator, long key, ItemCls item,
			int end_date, byte[] signature) {
		this(typeBytes, creator, (byte)0, key, item,
				end_date, 0l, null);
		this.signature = signature;
	}
	public R_SetStatusToItem(PublicKeyAccount creator, byte feePow, long key, ItemCls item,
			int end_date, long timestamp, byte[] reference, byte[] signature) {
		this(new byte[]{TYPE_ID, (byte)0, 0, 0}, creator, feePow, key, item,
				end_date, timestamp, reference);
	}

	// as pack
	public R_SetStatusToItem(PublicKeyAccount creator, long key, ItemCls item,
			int end_date, byte[] signature) {
		this(new byte[]{TYPE_ID, (byte)0, (byte)0, 0}, creator, (byte)0, key, item,
				end_date, 0l, null);
	}
	
	//GETTERS/SETTERS

	//public static String getName() { return "Send"; }
	
	public long getKey()
	{
		return this.key;
	}

	public ItemCls getItem()
	{
		return this.item;
	}

	public int getEndDate() 
	{
		return this.end_date;
	}
				
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();

		//ADD CREATOR/SERVICE/DATA
		transaction.put("key", this.key);
		transaction.put("item", this.item.toJson());
		transaction.put("end_date", this.end_date);
		
		return transaction;	
	}

	// releaserReference = null - not a pack
	// releaserReference = reference for releaser account - it is as pack
	public static Transaction Parse(byte[] data, byte[] releaserReference) throws Exception
	{
		boolean asPack = releaserReference != null;
		
		//CHECK IF WE MATCH BLOCK LENGTH
		if (data.length < BASE_LENGTH_AS_PACK
				| !asPack & data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length " + data.length);
		}
		
		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;

		long timestamp = 0;
		if (!asPack) {
			//READ TIMESTAMP
			byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
			timestamp = Longs.fromByteArray(timestampBytes);	
			position += TIMESTAMP_LENGTH;
		}

		byte[] reference;
		if (!asPack) {
			//READ REFERENCE
			reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			position += REFERENCE_LENGTH;
		} else {
			reference = releaserReference;
		}
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		byte feePow = 0;
		if (!asPack) {
			//READ FEE POWER
			byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
			feePow = feePowBytes[0];
			position += 1;
		}
		
		//READ SIGNATURE
		byte[] signature = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;

		//READ STATUS KEY
		byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
		long key = Longs.fromByteArray(keyBytes);	
		position += KEY_LENGTH;

		//READ ITEM
		// ITEM TYPE
		Byte itemType = data[position];
		position ++;
		// ITEM KEY
		byte[] itemKeyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
		long itemKey = Longs.fromByteArray(itemKeyBytes);	
		position += KEY_LENGTH;
		ItemCls item = Controller.getInstance().getItem(itemType.intValue(), itemKey);		
		
		// READ DURATION
		int end_date = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + DATE_DAY_LENGTH));
		position += DATE_DAY_LENGTH;

		if (!asPack) {
			return new R_SetStatusToItem(typeBytes, creator, feePow, key, item,
					end_date, timestamp, reference, signature);
		} else {
			return new R_SetStatusToItem(typeBytes, creator, key, item,
					end_date, signature);
		}

	}

	//@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) {

		byte[] data = super.toBytes(withSign, releaserReference);

		//WRITE STATUS KEY
		byte[] keyBytes = Longs.toByteArray(this.key);
		keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE ITEM KEYS
		// TYPE
		data = Bytes.concat(data, Longs.toByteArray(this.item.getType()[0]));
		// KEY
		byte[] itemKeyBytes = Longs.toByteArray(this.item.getKey());
		keyBytes = Bytes.ensureCapacity(itemKeyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE DURATION
		data = Bytes.concat(data, Ints.toByteArray(this.end_date));

		return data;	
	}

	@Override
	public int getDataLength(boolean asPack)
	{
		// not include note reference
		int len = asPack? BASE_LENGTH_AS_PACK : BASE_LENGTH;
		return len;
	}

	//VALIDATE

	public int isValid(DBSet db, byte[] releaserReference) {
		
		int result = super.isValid(db, releaserReference);
		if (result != Transaction.VALIDATE_OK) return result; 

		//CHECK END_DAY
		if(end_date < 0)
		{
			return INVALID_DATE;
		}
	
		if ( !db.getItemStatusMap().contains(this.key) )
		{
			return Transaction.ITEM_STATUS_NOT_EXIST;
		}

		if ( this.item == null )
		{
			return Transaction.ITEM_DOES_NOT_EXIST;
		}
		
		if (item.getItemTypeInt() != ItemCls.PERSON_TYPE
				&& item.getItemTypeInt() != ItemCls.ASSET_TYPE
				&& item.getItemTypeInt() != ItemCls.UNION_TYPE)
			return ITEM_DOES_NOT_STATUSED;

		BigDecimal balERM = this.creator.getConfirmedBalance(RIGHTS_KEY, db);
		if ( balERM.compareTo(MIN_ERM_BALANCE)<0 )
		{
			return Transaction.NOT_ENOUGH_RIGHTS;
		}

		
		if ( !this.creator.isPerson(db) )
		{
			if ( balERM.compareTo(GENERAL_ERM_BALANCE)<0 )
				// if not enough RIGHT BALANCE as GENERAL
				return Transaction.ACCOUNT_NOT_PERSONALIZED;
		}
		
		return Transaction.VALIDATE_OK;
	}

	//PROCESS/ORPHAN
	
	public void process(DBSet db, boolean asPack) {

		//UPDATE SENDER
		super.process(db, asPack);
		
		Tuple3<Integer, Integer, byte[]> itemP = new Tuple3<Integer, Integer, byte[]>(this.end_date,
				Controller.getInstance().getHeight(), this.signature);

		// SET ALIVE PERSON for DURATION
		// TODO set STATUSES by reference of it record - not by key!
		/// or add MAP by reference as signature - as IssueAsset - for orphans delete
		if (item.getItemTypeInt() == ItemCls.PERSON_TYPE)
			db.getPersonStatusMap().addItem(item.getKey(), this.key, new Tuple3<Integer, Integer, byte[]>(this.end_date,
					Controller.getInstance().getHeight(), this.signature));
		else if (item.getItemTypeInt() == ItemCls.ASSET_TYPE)
			db.getAssetStatusMap().addItem(item.getKey(), this.key, new Tuple3<Integer, Integer, byte[]>(this.end_date,
					Controller.getInstance().getHeight(), this.signature));
		else if (item.getItemTypeInt() == ItemCls.UNION_TYPE)
			db.getUnionStatusMap().addItem(item.getKey(), this.key, new Tuple3<Integer, Integer, byte[]>(this.end_date,
					Controller.getInstance().getHeight(), this.signature));

	}

	public void orphan(DBSet db, boolean asPack) {

		//UPDATE SENDER
		super.orphan(db, asPack);
		
						
		// UNDO ALIVE PERSON for DURATION
		if (item.getItemTypeInt() == ItemCls.PERSON_TYPE)
			db.getPersonStatusMap().removeItem(this.item.getKey(), this.key);
		else if (item.getItemTypeInt() == ItemCls.ASSET_TYPE)
			db.getAssetStatusMap().removeItem(this.item.getKey(), this.key);
		else if (item.getItemTypeInt() == ItemCls.UNION_TYPE)
			db.getUnionStatusMap().removeItem(this.item.getKey(), this.key);

	}

	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.add(this.creator);
		return accounts;
	}
	
	@Override
	public HashSet<Account> getRecipientAccounts()
	{
		return new HashSet<>();
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator))
		{
			return true;
		}
		
		return false;
	}

}