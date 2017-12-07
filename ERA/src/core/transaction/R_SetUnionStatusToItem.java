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
import org.mapdb.Fun.Tuple5;
import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.statuses.StatusCls;
import datachain.DCMap;
import datachain.DCSet;
import core.item.ItemCls;

// this.end_date == null -> MAX
public class R_SetUnionStatusToItem extends Transaction {

	private static final byte TYPE_ID = (byte)Transaction.SET_UNION_STATUS_TO_ITEM_TRANSACTION;
	private static final String NAME_ID = "Set Union Status to Unit";
	private static final int DATE_LENGTH = Transaction.TIMESTAMP_LENGTH; // one year + 256 days max

	protected Long key; // UNION KEY
	protected Long statusKey; // STATUS KEY
	protected int itemType; // ITEM TYPE (CAnnot read ITEMS on start DB - need reset ITEM after
	protected Long itemKey; // ITEM KEY
	protected long beg_date;
	protected long end_date = Long.MAX_VALUE;
	private static final int SELF_LENGTH = 2 * DATE_LENGTH + KEY_LENGTH + KEY_LENGTH + 1 + KEY_LENGTH;
	
	protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + SELF_LENGTH;
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + SELF_LENGTH;

	public R_SetUnionStatusToItem(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key, int itemType, long itemKey,
			Long beg_date, Long end_date, long timestamp, Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);		

		this.key = key;
		this.itemType = itemType;
		this.itemKey = itemKey;
		if (beg_date == null || beg_date == 0) beg_date = Long.MIN_VALUE;
		this.beg_date = beg_date;		
		if (end_date == null) end_date = Long.MAX_VALUE;
		this.end_date = end_date;		
	}

	public R_SetUnionStatusToItem(PublicKeyAccount creator, byte feePow, long key, int itemType, long itemKey,
			Long beg_date, Long end_date, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, (byte)0, 0, 0}, creator, feePow, key, itemType, itemKey,
				beg_date, end_date, timestamp, reference);
	}
	// set default date
	public R_SetUnionStatusToItem(PublicKeyAccount creator, byte feePow, long key, int itemType, long itemKey,
			long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, (byte)0, 0, 0}, creator, feePow, key, itemType, itemKey,
				 Long.MIN_VALUE, Long.MAX_VALUE, timestamp, reference);
	}
	public R_SetUnionStatusToItem(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key, int itemType, long itemKey,
			Long beg_date, Long end_date, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, feePow, key, itemType, itemKey,
				beg_date, end_date, timestamp, reference);
		this.signature = signature;
		//this.calcFee();
	}
	// as pack
	public R_SetUnionStatusToItem(byte[] typeBytes, PublicKeyAccount creator, long key, int itemType, long itemKey,
			Long beg_date, Long end_date, byte[] signature) {
		this(typeBytes, creator, (byte)0, key, itemType, itemKey,
				beg_date, end_date, 0l, null);
		this.signature = signature;
	}
	public R_SetUnionStatusToItem(PublicKeyAccount creator, byte feePow, long key, int itemType, long itemKey,
			Long beg_date, Long end_date, long timestamp, Long reference, byte[] signature) {
		this(new byte[]{TYPE_ID, (byte)0, 0, 0}, creator, feePow, key, itemType, itemKey,
				beg_date, end_date, timestamp, reference);
	}

	// as pack
	public R_SetUnionStatusToItem(PublicKeyAccount creator, long key, int itemType, long itemKey,
			Long beg_date, Long end_date, byte[] signature) {
		this(new byte[]{TYPE_ID, (byte)0, (byte)0, 0}, creator, (byte)0, key, itemType, itemKey,
				beg_date, end_date, 0l, null);
	}
	
	//GETTERS/SETTERS

	//public static String getName() { return "Send"; }
	
	public long getKey()
	{
		return this.key;
	}

	public int getItemType()
	{
		return this.itemType;
	}
	public long getItemKey()
	{
		return this.itemKey;
	}

	public Long getBeginDate() 
	{
		return this.beg_date;
	}
	public Long getEndDate() 
	{
		return this.end_date;
	}

	public boolean hasPublicText() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();

		//ADD CREATOR/SERVICE/DATA
		transaction.put("key", this.key);
		transaction.put("itemType", this.itemType);
		transaction.put("itemKey", this.itemKey);
		transaction.put("begin_date", this.beg_date);
		transaction.put("end_date", this.end_date);
		
		return transaction;	
	}

	// releaserReference = null - not a pack
	// releaserReference = reference for releaser account - it is as pack
	public static Transaction Parse(byte[] data, Long releaserReference) throws Exception
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

		Long reference = null;
		if (!asPack) {
			//READ REFERENCE
			byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			reference = Longs.fromByteArray(referenceBytes);	
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
		//ItemCls item = Controller.getInstance().getItem(itemType.intValue(), itemKey);	// error!	
		
		// READ BEGIN DATE
		byte[] beg_dateBytes = Arrays.copyOfRange(data, position, position + DATE_LENGTH);
		Long beg_date = Longs.fromByteArray(beg_dateBytes);	
		position += DATE_LENGTH;

		// READ END DATE
		byte[] end_dateBytes = Arrays.copyOfRange(data, position, position + DATE_LENGTH);
		Long end_date = Longs.fromByteArray(end_dateBytes);	
		position += DATE_LENGTH;

		if (!asPack) {
			return new R_SetUnionStatusToItem(typeBytes, creator, feePow, key, itemType, itemKey,
					beg_date, end_date, timestamp, reference, signature);
		} else {
			return new R_SetUnionStatusToItem(typeBytes, creator, key, itemType, itemKey,
					beg_date, end_date, signature);
		}

	}

	//@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) {

		byte[] data = super.toBytes(withSign, releaserReference);

		//WRITE STATUS KEY
		byte[] keyBytes = Longs.toByteArray(this.key);
		keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE ITEM KEYS
		// TYPE
		byte[] itemTypeKeyBytes = new byte[1];
		itemTypeKeyBytes[0] = (byte)this.itemType;
		data = Bytes.concat(data, itemTypeKeyBytes);
		// KEY
		byte[] itemKeyBytes = Longs.toByteArray(this.itemKey);
		keyBytes = Bytes.ensureCapacity(itemKeyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE BEGIN DATE
		data = Bytes.concat(data, Longs.toByteArray(this.beg_date));

		//WRITE END DATE
		data = Bytes.concat(data, Longs.toByteArray(this.end_date));

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

	public int isValid(DCSet db, Long releaserReference) {
		
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

		if (this.itemType != ItemCls.PERSON_TYPE
				&& this.itemType != ItemCls.ASSET_TYPE
				&& this.itemType != ItemCls.STATUS_TYPE)
			return ITEM_DOES_NOT_UNITED;

		ItemCls item = db.getItem_Map(this.itemType).get(this.itemKey);
		if ( item == null )
		{
			return Transaction.ITEM_DOES_NOT_EXIST;
		}
		
		BigDecimal balERA = this.creator.getBalanceUSE(RIGHTS_KEY, db);
		if (false && balERA.compareTo(BlockChain.MINOR_ERA_BALANCE_BD)<0 )
			return Transaction.NOT_ENOUGH_RIGHTS;
		
		return Transaction.VALIDATE_OK;
	}

	//PROCESS/ORPHAN
	
	public void process(DCSet db, Block block, boolean asPack) {

		//UPDATE SENDER
		super.process(db, block, asPack);

		// pack additional data
		byte[] a_data = new byte[0];//this.value1;

		//Block block = db.getBlockMap().getLastBlock();
		//int blockIndex = block.getHeight(db);
		//int transactionIndex = block.getTransactionIndex(signature);

		Tuple5<Long, Long, byte[], Integer, Integer> itemP = new Tuple5<Long, Long, byte[], Integer, Integer>
				(
					beg_date, end_date,
					a_data,
					this.getBlockHeightByParent(db), this.getSeqNo(db)
				);

		// SET UNION to ITEM for DURATION
		if (this.itemType == ItemCls.PERSON_TYPE)
			db.getPersonUnionMap().addItem(this.itemKey, this.key, itemP);
		else if (this.itemType == ItemCls.ASSET_TYPE)
			db.getAssetUnionMap().addItem(this.itemKey, this.key, itemP);
		else if (this.itemType == ItemCls.STATUS_TYPE)
			db.getStatusUnionMap().addItem(this.itemKey, this.key, itemP);

	}

	public void orphan(DCSet db, boolean asPack) {

		//UPDATE SENDER
		super.orphan(db, asPack);
						
		// UNDO ALIVE PERSON for DURATION
		if (this.itemType == ItemCls.PERSON_TYPE)
			db.getPersonUnionMap().removeItem(this.itemKey, this.key);
		else if (this.itemType == ItemCls.ASSET_TYPE)
			db.getAssetUnionMap().removeItem(this.itemKey, this.key);
		else if (this.itemType == ItemCls.STATUS_TYPE)
			db.getStatusUnionMap().removeItem(this.itemKey, this.key);

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
		
		if(address.equals(this.creator.getAddress()))
		{
			return true;
		}
		
		return false;
	}

}