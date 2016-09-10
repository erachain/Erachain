package core.transaction;

import java.math.BigDecimal;
//import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
//import java.util.List;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import org.apache.log4j.Logger;
import java.util.List;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple5;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.item.ItemCls;
//import database.BalanceMap;
import database.DBSet;



public class R_Hashes extends Transaction {

	private static final byte TYPE_ID = (byte) HASHES_RECORD;
	private static final String NAME_ID = "Hashes Record";

	private static final int URL_SIZE_LENGTH = 1;
	public static final int MAX_URL_LENGTH = (int) Math.pow(256, URL_SIZE_LENGTH) - 1;
	private static final int HASH_LENGTH = 32;

	protected byte[] url; // url
	protected byte[] data;
	protected byte[][] hashes;
	
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + URL_SIZE_LENGTH + DATA_SIZE_LENGTH;
	
	public R_Hashes(byte[] typeBytes, PublicKeyAccount creator, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference) {
		
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

		this.url = url;
		this.data = data;
		this.hashes = hashes;
		
	}
	public R_Hashes(byte[] typeBytes, PublicKeyAccount creator, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, feePow, url, data, hashes, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	// asPack
	public R_Hashes(byte[] typeBytes, PublicKeyAccount creator, byte[] url, byte[] data, byte[][] hashes, Long reference, byte[] signature) {
		this(typeBytes, creator, (byte)0, url, data, hashes, 0l, reference);
		this.signature = signature;
		// not need this.calcFee();
	}
	
	public R_Hashes(PublicKeyAccount creator, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference, byte[] signature) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, url, data, hashes, timestamp, reference, signature);
		// set props
		this.setTypeBytes();
	}
	public R_Hashes(PublicKeyAccount creator, byte feePow, byte[] url, byte[] data, byte[][] hashes, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, url, data, hashes, timestamp, reference);
		// set props
		this.setTypeBytes();
	}
	
	public static int getHashesLength(byte[] typeBytes) {
		return Ints.fromBytes((byte)0, (byte)0, typeBytes[2], typeBytes[3]);
	}
	
	protected void setTypeBytes() {
		
		byte[] bytesLen = Ints.toByteArray(this.hashes.length); 

		this.typeBytes[2] = bytesLen[2];
		this.typeBytes[3] = bytesLen[3];

	}

	//public static String getName() { return "Statement"; }

	public byte[] getURL()
	{
		return this.url;
	}
	
	public byte[] getData()
	{
		return this.data;
	}
	
	public String[] getHashesB58() 
	{
		String[] strHashes = new String[R_Hashes.getHashesLength(this.typeBytes)];
		int i = 0;
		for (byte[] hash: this.hashes)
		{
			strHashes[i++] = Base58.encode(hash);
		};
		return strHashes;
	}

	public byte[][] getHashes() 
	{
		return this.hashes;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();

		//ADD CREATOR/SERVICE/DATA
		if (data != null && data.length > 0) {
			transaction.put("data", new String(this.data, Charset.forName("UTF-8")));
			//transaction.put("data", Base58.encode(this.data));
		}
		if (url != null && url.length > 0) {
			//transaction.put("url", new String(this.url, Charset.forName("UTF-8")));
			transaction.put("url", new String(this.url, StandardCharsets.UTF_8));
			//transaction.put("data", Base58.encode(this.data));
		}

			transaction.put("hashes", this.getHashesB58());
			
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
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;

		//////// local parameters
		
		//READ NAME
		//byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		//int nameLength = Ints.fromByteArray(nameLengthBytes);
		//position += NAME_SIZE_LENGTH;
		int urlLength = Byte.toUnsignedInt(data[position]);
		position ++;
		
		if(urlLength > MAX_URL_LENGTH)
		{
			throw new Exception("Invalid URL length");
		}
		
		byte[] url = Arrays.copyOfRange(data, position, position + urlLength);
		position += urlLength;

		//READ DATA SIZE
		byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
		int dataSize = Ints.fromByteArray(dataSizeBytes);	
		position += DATA_SIZE_LENGTH;

		//READ DATA
		byte[] arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
		position += dataSize;
		/*
		encryptedByte = Arrays.copyOfRange(data, position, position + ENCRYPTED_LENGTH);
		position += ENCRYPTED_LENGTH;
		
		isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
		position += IS_TEXT_LENGTH;
		*/

		int hashesLen = getHashesLength(typeBytes);
		byte[][] hashes = new byte[hashesLen][];
		for (int i = 0; i < hashesLen ; i++) {
			hashes[i] = Arrays.copyOfRange(data, position, position + HASH_LENGTH);
			position += HASH_LENGTH;
		}
		
		if (!asPack) {
			return new R_Hashes(typeBytes, creator, feePow, url, arbitraryData, hashes, timestamp, reference, signatureBytes);
		} else {
			return new R_Hashes(typeBytes, creator, url, arbitraryData, hashes, reference, signatureBytes);
		}

	}

	//@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) {

		byte[] data = super.toBytes(withSign, releaserReference);

		//WRITE URL SIZE
		data = Bytes.concat(data, new byte[]{(byte)this.url.length});
		
		//WRITE URL
		data = Bytes.concat(data, this.url);
	
		//WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
		data = Bytes.concat(data, dataSizeBytes);

		//WRITE DATA
		data = Bytes.concat(data, this.data);

		/*
		//WRITE ENCRYPTED
		data = Bytes.concat(data, this.encrypted);
		
		//WRITE ISTEXT
		data = Bytes.concat(data, this.isText);
		*/
		
		for (int i=0; i< this.hashes.length; i++)
			data = Bytes.concat(data, this.hashes[i]);
			

		return data;	
	}

	@Override
	public int getDataLength(boolean asPack) {

		int add_len = this.url.length
				+ this.data.length
				+ this.hashes.length * HASH_LENGTH;
		
		if (asPack) {
			return BASE_LENGTH_AS_PACK + add_len;
		} else {
			return BASE_LENGTH + add_len;
		}
	}

	//@Override
	public int isValid(DBSet db, Long releaserReference) {
		
		//CHECK DATA SIZE
		if(url.length > MAX_URL_LENGTH)
		{
			return INVALID_URL_LENGTH;
		}
		
		if(data.length > 2 * Short.MAX_VALUE - 1)
		{
			return INVALID_DATA_LENGTH;
		}
		
		if(hashes.length > 2 * Short.MAX_VALUE - 1)
		{
			return INVALID_PARAMS_LENGTH;
		}
	

		int result = super.isValid(db, releaserReference);
		if (result != Transaction.VALIDATE_OK) return result; 
		
		return Transaction.VALIDATE_OK;

	}
	
	//PROCESS/ORPHAN
	
	/*
	public void process(DBSet db, boolean asPack) {

		//UPDATE SENDER
		super.process(db, asPack);
		
		// it in any time is unconfirmed! byte[] ref = this.getDBRef(db);
		db.getAddressStatement_Refs().set(this.creator.getAddress(), this.key, this.signature);

	}

	public void orphan(DBSet db, boolean asPack) {

		//UPDATE SENDER
		super.orphan(db, asPack);
						
		db.getAddressStatement_Refs().delete(this.creator.getAddress(), this.key);

	}
	*/

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

	public int calcBaseFee() {
		return calcCommonFee();
	}
}
