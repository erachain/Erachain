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
import core.block.Block;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.statuses.StatusCls;
import core.item.ItemCls;
import core.item.notes.NoteCls;
import core.item.notes.NoteFactory;
import core.item.persons.PersonCls;
import core.item.persons.PersonFactory;
//import core.item.statuses.StatusCls;
import database.ItemAssetBalanceMap;
import ntp.NTP;
import database.DBSet;
import utils.Converter;
import utils.DateTimeFormat;

// if person has not ALIVE status - add it
// end_day = this.add_day + this.timestanp(days)
// typeBytes[1] - version =0 - not need sign by person;
// 		 =1 - need sign by person
// typeBytes[2] - size of personalized accounts
public class R_SertifyPubKeys extends Transaction {

	private static final byte TYPE_ID = (byte)Transaction.CERTIFY_PUB_KEYS_TRANSACTION;
	private static final String NAME_ID = "Sertify Person";
	private static final int USER_ADDRESS_LENGTH = Transaction.CREATOR_LENGTH;
	private static final int DATE_DAY_LENGTH = 4; // one year + 256 days max

	// need RIGHTS for PERSON account
	private static final BigDecimal MIN_ERM_BALANCE = BigDecimal.valueOf(1000).setScale(8);
	// need RIGHTS for non PERSON account
	private static final BigDecimal GENERAL_ERM_BALANCE = BigDecimal.valueOf(100000).setScale(8);

	// how many FEE gift
	public static final BigDecimal GIFTED_FEE_AMOUNT = BigDecimal.valueOf(0.00005).setScale(8);
	public static final int DEFAULT_DURATION = 2 * 356;

	protected Long key; // PERSON KEY
	protected Integer add_day; // in days
	protected List<PublicKeyAccount> sertifiedPublicKeys;
	protected List<byte[]> sertifiedSignatures;

	private static final int SELF_LENGTH = DATE_DAY_LENGTH + KEY_LENGTH;
	
	protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + SELF_LENGTH;
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + SELF_LENGTH;

	public R_SertifyPubKeys(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key,
			List<PublicKeyAccount> sertifiedPublicKeys,
			int add_day, long timestamp, Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);		

		this.key = key;
		this.sertifiedPublicKeys = sertifiedPublicKeys;
		if (add_day == 0)
			// set to_date to default
			add_day = DEFAULT_DURATION;
		this.add_day = add_day;
	}

	public R_SertifyPubKeys(int version, PublicKeyAccount creator, byte feePow, long key,
			List<PublicKeyAccount> sertifiedPublicKeys,
			int add_day, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, (byte)version, (byte)sertifiedPublicKeys.size(), 0}, creator, feePow, key,
				sertifiedPublicKeys,
				add_day, timestamp, reference);
	}
	// set default date
	public R_SertifyPubKeys(int version, PublicKeyAccount creator, byte feePow, long key,
			List<PublicKeyAccount> sertifiedPublicKeys,
			long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, (byte)version, (byte)sertifiedPublicKeys.size(), 0}, creator, feePow, key,
				sertifiedPublicKeys,
				0, timestamp, reference);
	}
	public R_SertifyPubKeys(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key,
			List<PublicKeyAccount> sertifiedPublicKeys,
			int add_day, long timestamp, Long reference, byte[] signature,
			List<byte[]> sertifiedSignatures) {
		this(typeBytes, creator, feePow, key,
				sertifiedPublicKeys,
				add_day, timestamp, reference);
		this.signature = signature;
		this.sertifiedSignatures = sertifiedSignatures;
		this.calcFee();
	}
	// as pack
	public R_SertifyPubKeys(byte[] typeBytes, PublicKeyAccount creator, long key,
			List<PublicKeyAccount> sertifiedPublicKeys,
			int add_day, byte[] signature,
			List<byte[]> sertifiedSignatures) {
		this(typeBytes, creator, (byte)0, key,
				sertifiedPublicKeys,
				add_day, 0l, null);
		this.signature = signature;
		this.sertifiedSignatures = sertifiedSignatures;
	}
	public R_SertifyPubKeys(int version, PublicKeyAccount creator, byte feePow, long key,
			List<PublicKeyAccount> sertifiedPublicKeys,
			int add_day, long timestamp, Long reference, byte[] signature,
			byte[] userSignature1, byte[] userSignature2, byte[] userSignature3) {
		this(new byte[]{TYPE_ID, (byte)version, (byte)sertifiedPublicKeys.size(), 0}, creator, feePow, key,
				sertifiedPublicKeys,
				add_day, timestamp, reference);
	}

	// as pack
	public R_SertifyPubKeys(int version, PublicKeyAccount creator, long key,
			List<PublicKeyAccount> sertifiedPublicKeys,
			int add_day, byte[] signature,
			byte[] userSignature1, byte[] userSignature2, byte[] userSignature3) {
		this(new byte[]{TYPE_ID, (byte)version, (byte)sertifiedPublicKeys.size(), 0}, creator, (byte)0, key,
				sertifiedPublicKeys,
				add_day, 0l, null);
	}
	
	//GETTERS/SETTERS

	//public static String getName() { return "Send"; }

	public long getKey()
	{
		return this.key;
	}

	public List<PublicKeyAccount> getSertifiedPublicKeys() 
	{
		return this.sertifiedPublicKeys;
	}
	public List<String> getSertifiedPublicKeysB58() 
	{
		List<String> pbKeys = new ArrayList<String>();
		for (PublicKeyAccount key: this.sertifiedPublicKeys)
		{
			pbKeys.add(Base58.encode(key.getPublicKey()));
		};
		return pbKeys;
	}

	public List<byte[]> getSertifiedSignatures() 
	{
		return this.sertifiedSignatures;
	}
	public List<String> getSertifiedSignaturesB58() 
	{
		List<String> items = new ArrayList<String>();
		for (byte[] item: this.sertifiedSignatures)
		{
			items.add(Base58.encode(item));
		};
		return items;
	}
	
	public int getAddDay() 
	{
		return this.add_day;
	}
	
	public int getPublicKeysSize()
	{
		return this.typeBytes[2];
	}
	public static int getPublicKeysSize(byte[] typeBytes)
	{
		return typeBytes[2];
	}
			
	//////// VIEWS
	@Override
	public String viewAmount(String address) {
		return add_day>0?"+" + add_day: "" + add_day;
	}

	@Override
	public String viewRecipient() {
		return Base58.encode( this.sertifiedPublicKeys.get(0).getPublicKey());
	}

	//////////////
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();

		//ADD CREATOR/SERVICE/DATA
		transaction.put("key", this.key);
		List<String> pbKeys = new ArrayList<String>();
		transaction.put("sertified_public_keys", this.getSertifiedPublicKeysB58());
		transaction.put("sertified_signatures", this.getSertifiedSignaturesB58());
		transaction.put("add_day", this.add_day);
		
		return transaction;	
	}
	
	public void signUserAccounts(List<PrivateKeyAccount> userPrivateAccounts)
	{
		byte[] data;
		// use this.reference in any case
		data = this.toBytes( false, null );
		if ( data == null ) return;

		// all test a not valid for main test
		// all other network must be invalid here!
		int port = Controller.getInstance().getNetworkPort();
		data = Bytes.concat(data, Ints.toByteArray(port));

		if (this.sertifiedSignatures == null) this.sertifiedSignatures = new ArrayList<byte[]>();
		
		byte[] publicKey;
		for ( PublicKeyAccount publicAccount: this.sertifiedPublicKeys)
		{
			for ( PrivateKeyAccount privateAccount: userPrivateAccounts)
			{
				publicKey = privateAccount.getPublicKey();
				if (Arrays.equals((publicKey), publicAccount.getPublicKey()))
				{
					this.sertifiedSignatures.add(Crypto.getInstance().sign(privateAccount, data));
					break;
				}
			}
		}
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

		//READ PERSON KEY
		byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
		long key = Longs.fromByteArray(keyBytes);	
		position += KEY_LENGTH;

		//byte[] item;
		List<PublicKeyAccount> sertifiedPublicKeys = new ArrayList<PublicKeyAccount>();
		List<byte[]> sertifiedSignatures = new ArrayList<byte[]>();
		for (int i=0; i< getPublicKeysSize(typeBytes); i++)
		{
			//READ USER ACCOUNT
			sertifiedPublicKeys.add(new PublicKeyAccount(Arrays.copyOfRange(data, position, position + USER_ADDRESS_LENGTH)));
			position += USER_ADDRESS_LENGTH;			

			if (getVersion(typeBytes)==1)
			{
				//READ USER SIGNATURE
				sertifiedSignatures.add( Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH));
				position += SIGNATURE_LENGTH;
			}
		}

		// READ DURATION
		int add_day = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + DATE_DAY_LENGTH));
		position += DATE_DAY_LENGTH;

		if (!asPack) {
			return new R_SertifyPubKeys(typeBytes, creator, feePow, key,
					sertifiedPublicKeys,
					add_day, timestamp, reference, signature,
					sertifiedSignatures);
		} else {
			return new R_SertifyPubKeys(typeBytes, creator, key,
					sertifiedPublicKeys,
					add_day, signature,
					sertifiedSignatures);
		}

	}

	//@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) {

		byte[] data = super.toBytes(withSign, releaserReference);

		//WRITE PERSON KEY
		byte[] keyBytes = Longs.toByteArray(this.key);
		keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE USER PUBLIC KEYS
		int i = 0;
		for ( PublicKeyAccount publicAccount: this.sertifiedPublicKeys)
		{
			data = Bytes.concat(data, publicAccount.getPublicKey());
			
			if (withSign & this.getVersion()==1)
			{
				data = Bytes.concat(data, this.sertifiedSignatures.get(i++));
			}
		}
		
		//WRITE DURATION
		data = Bytes.concat(data, Ints.toByteArray(this.add_day));

		return data;	
	}

	@Override
	public int getDataLength(boolean asPack)
	{
		// not include note reference
		int len = asPack? BASE_LENGTH_AS_PACK : BASE_LENGTH;
		int accountsSize = this.sertifiedPublicKeys.size(); 
		len += accountsSize * PublicKeyAccount.PUBLIC_KEY_LENGTH;
		return this.typeBytes[1] == 1? len + Transaction.SIGNATURE_LENGTH * accountsSize: len;
	}

	//VALIDATE

	@Override
	public boolean isSignatureValid() {

		if ( this.signature == null || this.signature.length != 64 || this.signature == new byte[64] )
			return false;

		int pAccountsSize = 0;
		if (this.getVersion() == 1)
		{
			pAccountsSize = this.sertifiedPublicKeys.size();
			if (pAccountsSize > this.sertifiedSignatures.size())
				return false;
			
			byte[] singItem;
			for (int i = 0; i < pAccountsSize; i++)
			{
				//if (this.sertifiedSignatures.e(i);
				singItem = this.sertifiedSignatures.get(i);
				if (singItem == null || singItem.length != 64 || singItem == new byte[64])
				{
					return false;
				}
			}
		}
		
		byte[] data = this.toBytes( false, null );
		if ( data == null ) return false;

		// all test a not valid for main test
		// all other network must be invalid here!
		int port = Controller.getInstance().getNetworkPort();
		data = Bytes.concat(data, Ints.toByteArray(port));

		Crypto crypto = Crypto.getInstance();
		if (!crypto.verify(creator.getPublicKey(), signature, data))
				return false;

		// if use signs from person
		if (this.getVersion() == 1)
		{
			for (int i = 0; i < pAccountsSize; i++)
			{
				if (!crypto.verify(this.sertifiedPublicKeys.get(i).getPublicKey(), this.sertifiedSignatures.get(i), data))
					return false;
			}
		}

		return true;
	}

	//
	public int isValid(DBSet db, Long releaserReference) {
		
		int result = super.isValid(db, releaserReference);
		if (result != Transaction.VALIDATE_OK) return result; 

		//int transactionIndex = block.getTransactionIndex(signature);

		for (PublicKeyAccount publicAccount: this.sertifiedPublicKeys)
		{
			//CHECK IF PERSON PUBLIC KEY IS VALID
			if(!publicAccount.isValid())
			{
				return INVALID_PUBLIC_KEY;
			} else if (publicAccount.hasPerson(db) != null) {
				LOGGER.error("ACCOUNT_ALREADY_PERSONALIZED " + publicAccount.hasPerson(db));
				return ACCOUNT_ALREADY_PERSONALIZED;
			}
		}

		if ( !db.getItemPersonMap().contains(this.key) )
		{
			return Transaction.ITEM_PERSON_NOT_EXIST;
		}

		BigDecimal balERM = this.creator.getConfirmedBalance(RIGHTS_KEY, db);
		if ( balERM.compareTo(GENERAL_ERM_BALANCE)<0 )
			if ( this.creator.isPerson(db) )
			{
				if ( balERM.compareTo(MIN_ERM_BALANCE)<0 )
					return Transaction.NOT_ENOUGH_RIGHTS;
			} else {
				return Transaction.ACCOUNT_NOT_PERSONALIZED;
			}
		
		return Transaction.VALIDATE_OK;
	}

	//PROCESS/ORPHAN
	
	public void process(DBSet db, boolean asPack) {

		//UPDATE SENDER
		super.process(db, asPack);

		// Controller.getInstance().getHeight()
		// TODO нужно сделать запись что данная транзакция принадлежит данному блоку чтобы в нем найти её номер
		int transactionIndex = -1;
		int blockIndex = -1;
		Block block = this.getParent(db);// == null (((
		if (block == null) {
			blockIndex = db.getBlockMap().getLastBlock().getHeight(db);
		} else {
			blockIndex = block.getHeight(db);
			if (blockIndex < 1 ) {
				// if block not is confirmed - get last block + 1
				blockIndex = db.getBlockMap().getLastBlock().getHeight(db) + 1;
			} else {
				transactionIndex = block.getTransactionIndex(signature);
			}			
		}

		PublicKeyAccount pkAccount = this.sertifiedPublicKeys.get(0);
		// send GIFT FEE_KEY
		this.creator.setConfirmedBalance(FEE_KEY, this.creator.getConfirmedBalance(FEE_KEY, db).subtract(GIFTED_FEE_AMOUNT), db);						
		pkAccount.setConfirmedBalance(Transaction.FEE_KEY, 
				pkAccount.getConfirmedBalance(Transaction.FEE_KEY, db).add(GIFTED_FEE_AMOUNT), db);
		
		int add_day = this.add_day;
		// set to time stamp of record
		int start_day = (int)(this.timestamp / 86400000); 
		int end_day = start_day + add_day;
		
		Tuple3<Integer, Integer, Integer> itemP = new Tuple3<Integer, Integer, Integer>(end_day,
				//Controller.getInstance().getHeight(), this.signature);
				blockIndex, transactionIndex);
		Tuple4<Long, Integer, Integer, Integer> itemA = new Tuple4<Long, Integer, Integer, Integer>(this.key, end_day,
				blockIndex, transactionIndex);
		
		if (db.getPersonStatusMap().getItem(key, StatusCls.ALIVE_KEY) == null) {
			// ADD ALIVE STATUS to PERSON for permanent TO_DATE
			PersonCls person = (PersonCls)db.getItemPersonMap().get(key);
			db.getPersonStatusMap().addItem(key, StatusCls.ALIVE_KEY,
					new Tuple4<Long, Long, Integer, Integer>(
							person.getBirthday(), Long.MAX_VALUE,
							blockIndex, transactionIndex));
		}

		// SET PERSON ADDRESS
		String address;
		for (PublicKeyAccount publicAccount: this.sertifiedPublicKeys)
		{
			address = publicAccount.getAddress();
			db.getAddressPersonMap().addItem(address, itemA);
			db.getPersonAddressMap().addItem(this.key, address, itemP);			
		}
				
		if (!asPack) {

			//UPDATE REFERENCE OF RECIPIENT - for first accept FEE need
			if(pkAccount.getLastReference(db) == null)
			{
				pkAccount.setLastReference(this.timestamp, db);
			}
		}

	}

	public void orphan(DBSet db, boolean asPack) {

		//UPDATE SENDER
		super.orphan(db, asPack);
		
		PublicKeyAccount pkAccount = this.sertifiedPublicKeys.get(0);
		// BACK GIFT FEE_KEY
		this.creator.setConfirmedBalance(Transaction.FEE_KEY, this.creator.getConfirmedBalance(Transaction.FEE_KEY, db).add(GIFTED_FEE_AMOUNT), db);						
		pkAccount.setConfirmedBalance(Transaction.FEE_KEY, pkAccount.getConfirmedBalance(Transaction.FEE_KEY, db).subtract(GIFTED_FEE_AMOUNT), db);
						
		//UPDATE RECIPIENT
		String address;
		for (PublicKeyAccount publicAccount: this.sertifiedPublicKeys)
		{
			address = publicAccount.getAddress();
			db.getAddressPersonMap().removeItem(address);
			db.getPersonAddressMap().removeItem(this.key, address);
		}
		
		if (!asPack) {
			
			//UPDATE REFERENCE OF RECIPIENT
			if(pkAccount.getLastReference(db).equals(this.timestamp))
			{
				pkAccount.removeReference(db);
			}	
		}
	}

	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.add(this.creator);
		accounts.addAll(this.getRecipientAccounts());
		return accounts;
	}

	@Override
	public HashSet<Account> getRecipientAccounts() {
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.addAll(this.sertifiedPublicKeys);
		
		return accounts;
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		if(address.equals(creator.getAddress())) return true;
		
		for (PublicKeyAccount publicAccount: this.sertifiedPublicKeys)
		{
			if (address.equals(publicAccount.getAddress()))
					return true;
		}
				
		return false;
	}

}