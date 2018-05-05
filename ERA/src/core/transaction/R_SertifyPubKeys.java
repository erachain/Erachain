package core.transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.ItemCls;
import datachain.DCSet;


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
		//this.calcFee();
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

	// PERSON KEY
	@Override
	public long getKey()
	{
		return this.key;
	}

	@Override
	public List<PublicKeyAccount> getPublicKeys()
	{
		return this.sertifiedPublicKeys;
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

	@Override
	public List<byte[]> getSignatures() {
		return sertifiedSignatures;
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

	// IT is only PERSONALITY record
	@Override
	public boolean hasPublicText() {
		return true;
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
		//List<String> pbKeys = new ArrayList<String>();
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
	@Override
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
		// not include reference
		int len = asPack? BASE_LENGTH_AS_PACK : BASE_LENGTH;
		int accountsSize = this.sertifiedPublicKeys.size();
		len += accountsSize * PublicKeyAccount.PUBLIC_KEY_LENGTH;
		return this.typeBytes[1] == 1? len + Transaction.SIGNATURE_LENGTH * accountsSize: len;
	}

	//VALIDATE

	@Override
	public boolean isSignatureValid(DCSet dcSet) {

		if ( this.signature == null || this.signature.length != Crypto.SIGNATURE_LENGTH
				|| this.signature == new byte[Crypto.SIGNATURE_LENGTH] )
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
				if (singItem == null || singItem.length != Crypto.SIGNATURE_LENGTH
						|| singItem == new byte[Crypto.SIGNATURE_LENGTH])
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
	@Override
	public int isValid(Long releaserReference, long flags) {

		boolean creator_admin = false;

		int result = super.isValid(releaserReference, flags);
		if (result == Transaction.CREATOR_NOT_PERSONALIZED) {
			long personsCount = dcSet.getItemPersonMap().getLastKey();
			if (personsCount < 20) {
				// FIRST Persons only by ME
				// FIRST Persons only by ADMINS
				for ( String admin: BlockChain.GENESIS_ADMINS) {
					if (this.creator.equals(admin)) {
						creator_admin = true;
						break;
					}
				}
			}
			if (!creator_admin)
				return result;
		}

		int height = this.getBlockHeightByParentOrLast(dcSet);

		for (PublicKeyAccount publicAccount: this.sertifiedPublicKeys)
		{
			//CHECK IF PERSON PUBLIC KEY IS VALID
			if(!publicAccount.isValid())
			{
				return INVALID_PUBLIC_KEY;
			} else if (publicAccount.getPerson(dcSet, height) != null) {
				LOGGER.error("ACCOUNT_ALREADY_PERSONALIZED " + publicAccount.getPerson(dcSet, height));
				return ACCOUNT_ALREADY_PERSONALIZED;
			}
		}

		if ( !dcSet.getItemPersonMap().contains(this.key) )
		{
			return Transaction.ITEM_PERSON_NOT_EXIST;
		}

		BigDecimal balERA = this.creator.getBalanceUSE(RIGHTS_KEY, dcSet);
		if ( balERA.compareTo(
				//BlockChain.MINOR_ERA_BALANCE_BD
				BlockChain.MIN_GENERATING_BALANCE_BD
				)<0
				)
			return Transaction.NOT_ENOUGH_RIGHTS;

		return Transaction.VALIDATE_OK;
	}

	//PROCESS/ORPHAN

	@Override
	public void process(Block block, boolean asPack) {

		//UPDATE SENDER
		super.process(block, asPack);

		DCSet db = this.dcSet;

		int transactionIndex = -1;
		int blockIndex = -1;
		//Block block = this.getBlock(db);// == null (((
		if (block == null) {
			blockIndex = db.getBlockMap().last().getHeight(db);
		} else {
			blockIndex = block.getHeight(db);
			if (blockIndex < 1 ) {
				// if block not is confirmed - get last block + 1
				blockIndex = db.getBlockMap().last().getHeight(db) + 1;
			}
			//transactionIndex = this.getSeqNo(db);
			transactionIndex = block.getTransactionSeq(signature);
		}


		boolean personalized = false;
		for (Account pkAccount: this.sertifiedPublicKeys) {
			Tuple4<Long, Integer, Integer, Integer> info = pkAccount.getPersonDuration(db);
			if (info!= null) {
				personalized = true;
				break;
			}
		}

		PublicKeyAccount pkAccount = this.sertifiedPublicKeys.get(0);
		if (!personalized) {
			// IT IS NOT VOUCHED PERSON
			// FIND person
			ItemCls person = db.getItemPersonMap().get(this.key);

			// FIND issue record
			Transaction transPersonIssue = db.getTransactionFinalMap().getTransaction(person.getReference());

			// GET FEE from that record
			transPersonIssue.setDC(db, false); // NEED to RECAL?? if from DB

			// ISSUE NEW COMPU in chain
			BigDecimal issued_FEE_BD = transPersonIssue.getFee();

			// BACK FEE FOR ISSUER without gift for this.CREATOR
			transPersonIssue.getCreator().changeBalance(db, false, FEE_KEY, issued_FEE_BD
					.subtract(BlockChain.GIFTED_COMPU_AMOUNT_BD), false);

			// GIVE GIFTs
			this.creator.changeBalance(db, false, FEE_KEY, BlockChain.GIFTED_COMPU_AMOUNT_BD, false);
			pkAccount.changeBalance(db, false, FEE_KEY, BlockChain.GIFTED_COMPU_AMOUNT_FOR_PERSON_BD, false);

			// ADD to EMISSION (with minus)
			GenesisBlock.CREATOR.changeBalance(db, true, FEE_KEY, issued_FEE_BD
					.add(BlockChain.GIFTED_COMPU_AMOUNT_FOR_PERSON_BD), false);

		}

		int add_day = this.add_day;
		// set to time stamp of record
		int start_day = (int)(this.timestamp / 86400000);
		int end_day = start_day + add_day;

		Tuple3<Integer, Integer, Integer> itemP = new Tuple3<Integer, Integer, Integer>(end_day,
				//Controller.getInstance().getHeight(), this.signature);
				blockIndex, transactionIndex);
		Tuple4<Long, Integer, Integer, Integer> itemA = new Tuple4<Long, Integer, Integer, Integer>(this.key, end_day,
				blockIndex, transactionIndex);

		/*
		Tuple5<Long, Long, byte[], Integer, Integer> psItem = db.getPersonStatusMap().getItem(this.key, StatusCls.ALIVE_KEY);
		if (psItem == null) {
			// ADD ALIVE STATUS to PERSON for permanent TO_DATE
			PersonCls person = (PersonCls)db.getItemPersonMap().get(this.key);
			db.getPersonStatusMap().addItem(this.key, StatusCls.ALIVE_KEY,
					new Tuple5<Long, Long, byte[], Integer, Integer>(
							person.getBirthday(), Long.MAX_VALUE,
							new byte[0],
							blockIndex, transactionIndex));
		}
		 */

		// SET PERSON ADDRESS
		String address;
		for (PublicKeyAccount publicAccount: this.sertifiedPublicKeys)
		{
			address = publicAccount.getAddress();
			db.getAddressPersonMap().addItem(address, itemA);
			db.getPersonAddressMap().addItem(this.key, address, itemP);

			if (!db.getAddressTime_SignatureMap().contains(address)) {
				db.getAddressTime_SignatureMap().set(address, this.signature);
			}

		}

	}

	@Override
	public void orphan(boolean asPack) {

		//UPDATE SENDER
		super.orphan(asPack);

		DCSet db = this.dcSet;
		//UPDATE RECIPIENT
		String address;
		for (PublicKeyAccount publicAccount: this.sertifiedPublicKeys)
		{
			address = publicAccount.getAddress();
			db.getAddressPersonMap().removeItem(address);
			db.getPersonAddressMap().removeItem(this.key, address);
		}

		boolean personalized = false;
		for (Account pkAccount: this.sertifiedPublicKeys) {
			if (pkAccount.getPersonDuration(db) != null) {
				personalized = true;
				break;
			}
		}

		PublicKeyAccount pkAccount = this.sertifiedPublicKeys.get(0);
		if (!personalized) {
			// IT IS NOT VOUCHED PERSON
			// FIND person
			ItemCls person = db.getItemPersonMap().get(this.key);
			// FIND issue record
			Transaction transPersonIssue = db.getTransactionFinalMap().getTransaction(person.getReference());
			// GET FEE from that record
			transPersonIssue.setDC(db, false); // NEED to RECAL?? if from DB
			//long issueFEE = transPersonIssue.getFeeLong() + BlockChain.GIFTED_COMPU_AMOUNT;
			//if (true || BlockChain.START_LEVEL == 1)
			//	issueFEE = issueFEE>>2;

			// ISSUE NEW COMPU in chain
			BigDecimal issued_FEE_BD = transPersonIssue.getFee();

			// BACK FEE FOR ISSUER without gift for this.CREATOR
			transPersonIssue.getCreator().changeBalance(db, true, FEE_KEY, issued_FEE_BD
					.subtract(BlockChain.GIFTED_COMPU_AMOUNT_BD), true);

			// GIVE GIFTs
			this.creator.changeBalance(db, true, FEE_KEY, BlockChain.GIFTED_COMPU_AMOUNT_BD, true);
			pkAccount.changeBalance(db, true, FEE_KEY, BlockChain.GIFTED_COMPU_AMOUNT_FOR_PERSON_BD, true);

			// ADD to EMISSION (with minus)
			GenesisBlock.CREATOR.changeBalance(db, false, FEE_KEY, issued_FEE_BD
					.add(BlockChain.GIFTED_COMPU_AMOUNT_FOR_PERSON_BD), true);

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