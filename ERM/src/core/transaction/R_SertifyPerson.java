package core.transaction;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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
import core.item.notes.NoteCls;
import core.item.notes.NoteFactory;
import core.item.persons.PersonCls;
import core.item.persons.PersonFactory;
import core.item.statuses.StatusCls;
import database.ItemAssetBalanceMap;
import ntp.NTP;
import database.DBSet;
import utils.Converter;

// this.end_date = 0 (ALIVE PERMANENT), = -1 (ENDED), = Integer - different
public class R_SertifyPerson extends Transaction {

	private static final byte TYPE_ID = (byte)Transaction.CERTIFY_PERSON_TRANSACTION;
	private static final String NAME_ID = "Sertify Person";
	private static final int USER_ADDRESS_LENGTH = Transaction.CREATOR_LENGTH;
	private static final int DURATION_LENGTH = 4; // one year + 256 days max
	private static final BigDecimal MIN_ERM_BALANCE = BigDecimal.valueOf(1000).setScale(8);

	// how many OIL gift
	public static final BigDecimal GIFTED_FEE_AMOUNT = BigDecimal.valueOf(0.00005).setScale(8);
	public static final int DEFAULT_DURATION = 3 * 356;

	protected Long key; // PERSON KEY
	protected Integer end_date; // in days
	protected PublicKeyAccount personAddress1;
	protected PublicKeyAccount personAddress2;
	protected PublicKeyAccount personAddress3;
	protected byte[] userSignature1;
	protected byte[] userSignature2;
	protected byte[] userSignature3;
	private static final int SELF_LENGTH = 3 * (USER_ADDRESS_LENGTH + SIGNATURE_LENGTH) + DURATION_LENGTH
			+ KEY_LENGTH;
	
	protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK + SELF_LENGTH;
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + SELF_LENGTH;

	public R_SertifyPerson(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key,
			PublicKeyAccount personAddress1, PublicKeyAccount personAddress2, PublicKeyAccount personAddress3,
			int end_date, long timestamp, byte[] reference) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);		

		this.TYPE_NAME = NAME_ID;
		this.key = key;
		this.personAddress1 = personAddress1;
		this.personAddress2 = personAddress2;
		this.personAddress3 = personAddress3;
		this.end_date = end_date;			
	}
	public R_SertifyPerson(PublicKeyAccount creator, byte feePow, long key,
			PublicKeyAccount userAccount1, PublicKeyAccount userAccount2, PublicKeyAccount userAccount3,
			int end_date, long timestamp, byte[] reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, key,
				userAccount1, userAccount2, userAccount3,
				end_date, timestamp, reference);
	}
	// set default date
	public R_SertifyPerson(PublicKeyAccount creator, byte feePow, long key,
			PublicKeyAccount userAccount1, PublicKeyAccount userAccount2, PublicKeyAccount userAccount3,
			long timestamp, byte[] reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, key,
				userAccount1, userAccount2, userAccount3,
				0, timestamp, reference);
		
		this.end_date = DEFAULT_DURATION + (int)(NTP.getTime() / 86400);
	}
	public R_SertifyPerson(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long key,
			PublicKeyAccount userAccount1, PublicKeyAccount userAccount2, PublicKeyAccount userAccount3,
			int end_date, long timestamp, byte[] reference, byte[] signature,
			 byte[] userSignature1,  byte[] userSignature2,  byte[] userSignature3) {
		this(typeBytes, creator, feePow, key,
				userAccount1, userAccount2, userAccount3,
				end_date, timestamp, reference);
		this.signature = signature;
		this.userSignature1 = userSignature1;
		this.userSignature2 = userSignature2;
		this.userSignature3 = userSignature3;
		this.calcFee();
	}
	// as pack
	public R_SertifyPerson(byte[] typeBytes, PublicKeyAccount creator, long key,
			PublicKeyAccount userAccount1, PublicKeyAccount userAccount2, PublicKeyAccount userAccount3,
			int end_date, byte[] signature,
			 byte[] userSignature1,  byte[] userSignature2,  byte[] userSignature3) {
		this(typeBytes, creator, (byte)0, key,
				userAccount1, userAccount2, userAccount3,
				end_date, 0l, null);
		this.signature = signature;
		this.userSignature1 = userSignature1;
		this.userSignature2 = userSignature2;
		this.userSignature3 = userSignature3;
	}
	public R_SertifyPerson(PublicKeyAccount creator, byte feePow, long key,
			PublicKeyAccount userAccount1, PublicKeyAccount userAccount2, PublicKeyAccount userAccount3,
			int end_date, long timestamp, byte[] reference, byte[] signature,
			byte[] userSignature1, byte[] userSignature2, byte[] userSignature3) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, key,
				userAccount1, userAccount2, userAccount3,
				end_date, timestamp, reference);
	}
	// as pack
	public R_SertifyPerson(PublicKeyAccount creator, long key,
			PublicKeyAccount userAccount1, PublicKeyAccount userAccount2, PublicKeyAccount userAccount3,
			int end_date, byte[] signature,
			byte[] userSignature1, byte[] userSignature2, byte[] userSignature3) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, (byte)0, key,
				userAccount1, userAccount2, userAccount3,
				end_date, 0l, null);
	}
	
	//GETTERS/SETTERS

	//public static String getName() { return "Send"; }

	public long getKey()
	{
		return this.key;
	}

	public PublicKeyAccount getUserAccount1() 
	{
		return this.personAddress1;
	}
	public PublicKeyAccount getUserAccount2() 
	{
		return this.personAddress2;
	}
	public PublicKeyAccount getUserAccount3() 
	{
		return this.personAddress3;
	}
	
	public byte[] getUserSignature1() 
	{
		return this.userSignature1;
	}
	public byte[] getUserSignature2() 
	{
		return this.userSignature2;
	}
	public byte[] getUserSignature3() 
	{
		return this.userSignature3;
	}
	public int getDuration() 
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
		transaction.put("userAccount1", this.personAddress1.getAddress());
		transaction.put("userAccount2", this.personAddress2.getAddress());
		transaction.put("userAccount3", this.personAddress3.getAddress());
		transaction.put("end_date", this.end_date);
		
		return transaction;	
	}

	public void signUserAccounts(PrivateKeyAccount userAccount1, PrivateKeyAccount userAccount2, PrivateKeyAccount userAccount3)
	{
		byte[] data;
		// use this.reference in any case
		data = this.toBytes( false, null );
		if ( data == null ) return;

		this.userSignature1 = Crypto.getInstance().sign(userAccount1, data);
		this.userSignature2 = Crypto.getInstance().sign(userAccount2, data);
		this.userSignature3 = Crypto.getInstance().sign(userAccount3, data);
		//this.calcFee(); // need for recal!
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

		/////
		//READ PERSON
		// Person parse without reference - if is = signature
		//PersonCls person = PersonFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
		//position += person.getDataLength(false);

		//READ KEY
		byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
		long key = Longs.fromByteArray(keyBytes);	
		position += KEY_LENGTH;

		//READ USER ACCOUNT1
		byte[] userAccount1Bytes = Arrays.copyOfRange(data, position, position + USER_ADDRESS_LENGTH);
		PublicKeyAccount userAccount1 = new PublicKeyAccount(userAccount1Bytes);
		position += USER_ADDRESS_LENGTH;

		//READ USER ACCOUNT2
		byte[] userAccount2Bytes = Arrays.copyOfRange(data, position, position + USER_ADDRESS_LENGTH);
		PublicKeyAccount userAccount2 = new PublicKeyAccount(userAccount2Bytes);
		position += USER_ADDRESS_LENGTH;

		//READ USER ACCOUNT1
		byte[] userAccount3Bytes = Arrays.copyOfRange(data, position, position + USER_ADDRESS_LENGTH);
		PublicKeyAccount userAccount3 = new PublicKeyAccount(userAccount3Bytes);
		position += USER_ADDRESS_LENGTH;

		// READ DURATION
		int end_date = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + DURATION_LENGTH));
		position += DURATION_LENGTH;
				
		//READ USER1 SIGNATURE
		byte[] userSignature1 = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;

		//READ USER2 SIGNATURE
		byte[] userSignature2 = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;

		//READ USER3 SIGNATURE
		byte[] userSignature3 = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;

		if (!asPack) {
			return new R_SertifyPerson(typeBytes, creator, feePow, key,
					userAccount1, userAccount2, userAccount3,
					end_date, timestamp, reference, signature,
					 userSignature1,  userSignature2,  userSignature3);
		} else {
			return new R_SertifyPerson(typeBytes, creator, key,
					userAccount1, userAccount2, userAccount3,
					end_date, signature,
					 userSignature1,  userSignature2,  userSignature3);
		}

	}

	//@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) {

		byte[] data = super.toBytes(withSign, releaserReference);

		//WRITE PERSON KEY
		byte[] keyBytes = Longs.toByteArray(this.key);
		keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE USER ACCOUNT1
		data = Bytes.concat(data, this.personAddress1.getPublicKey());
		
		//WRITE USER ACCOUNT2
		if (this.personAddress2 == null) {
			data = new byte[USER_ADDRESS_LENGTH];
		} else {
			data = Bytes.concat(data, this.personAddress2.getPublicKey());
		}
		
		//WRITE USER ACCOUNT3
		if (this.personAddress3 == null) {
			data = new byte[USER_ADDRESS_LENGTH];
		} else {
			data = Bytes.concat(data, this.personAddress3.getPublicKey());
		}

		//WRITE DURATION
		data = Bytes.concat(data, Ints.toByteArray(this.end_date));

		//USER SIGNATUREs
		if (withSign) {
			data = Bytes.concat(data, this.userSignature1);
			data = Bytes.concat(data, this.userSignature2 == null? new byte[SIGNATURE_LENGTH]: this.userSignature2);
			data = Bytes.concat(data, this.userSignature3 == null? new byte[SIGNATURE_LENGTH]: this.userSignature3);
		}

		return data;	
	}

	@Override
	public int getDataLength(boolean asPack)
	{
		// not include note reference
		return asPack? BASE_LENGTH_AS_PACK : BASE_LENGTH;
	}

	//VALIDATE

	@Override
	public boolean isSignatureValid() {

		if ( this.signature == null | this.signature.length != 64 | this.signature == new byte[64]
			| this.userSignature1 == null | this.userSignature1.length != 64 | this.userSignature1 == new byte[64]
					)
			return false;
		
		byte[] data = this.toBytes( false, null );
		if ( data == null ) return false;

		Crypto crypto = Crypto.getInstance();
		return crypto.verify(creator.getPublicKey(), signature, data)
				& crypto.verify(this.personAddress1.getPublicKey(), this.userSignature1, data)
				& (this.personAddress2 == null
						|| crypto.verify(this.personAddress2.getPublicKey(), this.userSignature2, data))
				& (this.personAddress3 == null
						|| crypto.verify(this.personAddress3.getPublicKey(), this.userSignature3, data));
	}

	//
	public int isValid(DBSet db, byte[] releaserReference) {
		
		//CHECK DURATION
		if(end_date < 0)
		{
			return INVALID_DURATION;
		}
	
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(this.personAddress1 == null || !Crypto.getInstance().isValidAddress(this.personAddress1.getAddress())
			//| this.personAddress2 == null || !Crypto.getInstance().isValidAddress(this.personAddress2.getAddress())
			//| this.personAddress3 == null || !Crypto.getInstance().isValidAddress(this.personAddress3.getAddress())
				)
		{
			return INVALID_ADDRESS;
		}

		int result = super.isValid(db, releaserReference);
		if (result != Transaction.VALIDATE_OK) return result; 
		
		BigDecimal balERM = this.creator.getConfirmedBalance(RIGHTS_KEY, db);
		if ( balERM.compareTo(MIN_ERM_BALANCE)<0 )
		{
			return Transaction.NOT_ENOUGH_RIGHTS;
		}

		
		if ( !db.getPersonMap().contains(this.key) )
		{
			return Transaction.ITEM_PERSON_NOT_EXIST;
		}

		if ( !this.creator.isPerson(db) )
		{
			return Transaction.ACCOUNT_NOT_PERSONALIZED;
		}
		
		// ITEM EXIST?
		if (!db.getPersonMap().contains(this.key))
			return Transaction.ITEM_DOES_NOT_EXIST;

		return Transaction.VALIDATE_OK;
	}

	//PROCESS/ORPHAN
	
	public void process(DBSet db, boolean asPack) {

		//UPDATE SENDER
		super.process(db, asPack);

		// send GIFT FEE_KEY
		this.creator.setConfirmedBalance(FEE_KEY, this.creator.getConfirmedBalance(FEE_KEY, db).subtract(GIFTED_FEE_AMOUNT), db);						
		this.personAddress1.setConfirmedBalance(Transaction.FEE_KEY, this.personAddress1.getConfirmedBalance(Transaction.FEE_KEY, db).add(GIFTED_FEE_AMOUNT), db);
		
		Tuple3<Integer, Integer, byte[]> itemP = new Tuple3<Integer, Integer, byte[]>(this.end_date,
				Controller.getInstance().getHeight(), this.signature);
		Tuple4<Long, Integer, Integer, byte[]> itemA = new Tuple4<Long, Integer, Integer, byte[]>(this.key, this.end_date,
				Controller.getInstance().getHeight(), this.signature);
		// SET ALIVE PERSON for DURATION
		db.getPersonStatusMap().addItem(this.key, itemP);

		// TODO need MAP List<address> for ONE PERSON - Tuple2<Long, List<String>>
		// SET PERSON ADDRESS
		db.getAddressPersonMap().addItem(this.personAddress1.getAddress(), itemA);
		db.getPersonAddressMap().addItem(this.key, this.personAddress1.getAddress(), itemP);
		
		if (this.personAddress2 !=null) {
			db.getAddressPersonMap().addItem(this.personAddress2.getAddress(), itemA);
			db.getPersonAddressMap().addItem(this.key, this.personAddress2.getAddress(), itemP);
		}
		if (this.personAddress3 !=null) {
			db.getAddressPersonMap().addItem(this.personAddress3.getAddress(), itemA);
			db.getPersonAddressMap().addItem(this.key, this.personAddress3.getAddress(), itemP);
		}
		
		if (!asPack) {

			//UPDATE REFERENCE OF RECIPIENT - for first accept OIL need
			if(Arrays.equals(this.personAddress1.getLastReference(db), new byte[0]))
			{
				this.personAddress1.setLastReference(this.signature, db);
			}
		}

	}

	public void orphan(DBSet db, boolean asPack) {

		//UPDATE SENDER
		super.orphan(db, asPack);
		
		// BACK GIFT FEE_KEY
		this.creator.setConfirmedBalance(Transaction.FEE_KEY, this.creator.getConfirmedBalance(Transaction.FEE_KEY, db).add(GIFTED_FEE_AMOUNT), db);						
		this.personAddress1.setConfirmedBalance(Transaction.FEE_KEY, this.personAddress1.getConfirmedBalance(Transaction.FEE_KEY, db).subtract(GIFTED_FEE_AMOUNT), db);
						
		// UNDO ALIVE PERSON for DURATION
		db.getPersonStatusMap().removeItem(this.key, StatusCls.ALIVE_KEY);

		//UPDATE RECIPIENT
		db.getAddressPersonMap().removeItem(this.personAddress1.getAddress());
		db.getPersonAddressMap().removeItem(this.key, this.personAddress1.getAddress());
		if (this.personAddress2 !=null) {
			db.getAddressPersonMap().removeItem(this.personAddress2.getAddress());
			db.getPersonAddressMap().removeItem(this.key, this.personAddress2.getAddress());
		}
		if (this.personAddress3 !=null) {
			db.getAddressPersonMap().removeItem(this.personAddress3.getAddress());
			db.getPersonAddressMap().removeItem(this.key, this.personAddress3.getAddress());
		}

		
		if (!asPack) {
			
			//UPDATE REFERENCE OF RECIPIENT
			if(Arrays.equals(this.personAddress1.getLastReference(db), this.signature))
			{
				this.personAddress1.removeReference(db);
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
		
		accounts.add(this.personAddress1);
		accounts.add(this.personAddress2);
		accounts.add(this.personAddress3);
		
		return accounts;
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(creator.getAddress())
				|| address.equals(personAddress1.getAddress())
				|| address.equals(personAddress2.getAddress())
				|| address.equals(personAddress3.getAddress())
				)
		{
			return true;
		}
		
		return false;
	}

	public int calcBaseFee() {
		return calcCommonFee();
	}

}