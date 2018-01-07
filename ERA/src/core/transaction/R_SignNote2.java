package core.transaction;

import java.math.BigDecimal;
//import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.Charset;
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

import core.BlockChain;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Base58;
import core.item.ItemCls;
import core.item.statements.StatementCls;
import datachain.DCSet;
import datachain.Issue_ItemMap;
import datachain.Item_Map;



public class R_SignNote2 extends Issue_ItemRecord { //Transaction {

	private static final byte TYPE_ID = (byte) SIGN_NOTE2_TRANSACTION;
	private static final String NAME_ID = "Issue Note 2";

	protected static final byte HAS_TEMPLATE_MASK = (byte)(1 << 7);
	/*
	PROPERTIES:
	[0] - type
	[1] - version 
	******* prop 1
	[2] bits [0] - has HEAD
	[2] bits [1] - has PLATE and .key > 0
	[2] bits [2] - has DATA
	[2] bits [3] - has encryptedDATA
	[2] bits [6,7] - signers: 0 - none; 1..3 = 1..3; 4 = LIST -> 1 byte for LIST.len + 3 

	******* prop2
	[3] bits [0,1] - hashes: 0 - none; 1..3 = 1..3; 4 = LIST -> 1 byte for LIST.len + 3 
	[3] bits [2,3] - files: 0 - none; 1..3 = 1..3; 4 = LIST -> 1 byte for LIST.len + 3 
	[3] bits [4,5] - parents: 0 - none; 1..3 = 1..3; 4 = LIST -> 1 byte for LIST.len + 3
	
	******* DATA or encryptedData Size < 0 - it is not TEXT
	******* 
		*/
	
	protected String head;
	protected long key; // key for Template
	protected byte[] publicData;
	protected boolean publicData_isText;
	protected byte[] encryptedData;
	protected boolean encryptedData_isText;
	protected Long[] parents; // parent records
	protected byte[][] hashes;
	protected byte[][] files;
	
	protected PublicKeyAccount[] signers; // for all it need encrypt
	protected byte[][] signatures; // - multi sign
	
	public R_SignNote2(byte[] typeBytes, PublicKeyAccount creator, StatementCls statement, byte feePow, long templateKey, byte[] publicData, boolean pubData_isText,
			byte[] encryptedData, boolean encData_isText, long timestamp, Long reference) {
		
		super(typeBytes, NAME_ID, creator, statement, feePow, timestamp, reference);

		this.key = templateKey;
		this.publicData = publicData;
		this.publicData_isText = pubData_isText;
		this.encryptedData = encryptedData;
		this.encryptedData_isText = encData_isText;
	}
	public R_SignNote2(byte[] typeBytes, PublicKeyAccount creator, StatementCls statement, byte feePow, long templateKey, byte[] publicData, boolean pubData_isText,
			byte[] encryptedData, boolean encData_isText, Long[] parents, byte[][] hashes, byte[][] files,
			long timestamp, Long reference) {
		
		super(typeBytes, NAME_ID, creator, statement, feePow, timestamp, reference);

		this.key = templateKey;
		this.publicData = publicData;
		this.publicData_isText = pubData_isText;
		this.encryptedData = encryptedData;
		this.encryptedData_isText = encData_isText;
		this.parents = parents;
		this.hashes = hashes;
		this.files = files;
	}
	
	public R_SignNote2(byte[] typeBytes, PublicKeyAccount creator, StatementCls statement, byte feePow, long templateKey, byte[] publicData, boolean pubData_isText,
			byte[] encryptedData, boolean encData_isText, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, statement, feePow, templateKey, publicData, pubData_isText, encryptedData, encData_isText, timestamp, reference);
		this.signature = signature;
		//this.calcFee();
	}
	public R_SignNote2(PublicKeyAccount creator, StatementCls statement, byte feePow, long templateKey, byte[] publicData, boolean pubData_isText,
			byte[] encryptedData, boolean encData_isText, Long[] parents, byte[][] hashes, byte[][] files, long timestamp, Long reference) {

		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, statement, feePow, templateKey, 
				publicData, pubData_isText,
				encryptedData, encData_isText, parents, hashes, files,
				timestamp, reference);
		// set props
		this.setTypeBytes();
	}
	public R_SignNote2(PublicKeyAccount creator, StatementCls statement, byte feePow, long templateKey, byte[] publicData, boolean pubData_isText,
			byte[] encryptedData, boolean encData_isText, Long[] parents, byte[][] hashes, byte[][] files, long timestamp, Long reference, byte[] signature) {

		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, statement, feePow, templateKey, 
				publicData, pubData_isText,
				encryptedData, encData_isText, parents, hashes, files,
				timestamp, reference);
		this.signature = signature;
		// set props
		this.setTypeBytes();
	}
	public R_SignNote2(byte prop1, byte prop2, byte prop3, PublicKeyAccount creator, StatementCls statement, byte feePow,  long templateKey, byte[] publicData, boolean pubData_isText,
			byte[] encryptedData, boolean encData_isText, Long[] parents, byte[][] hashes, byte[][] files, long timestamp, Long reference)
	{
		this(new byte[]{TYPE_ID, prop1, prop2, prop3}, creator, statement, feePow, templateKey, publicData, pubData_isText, encryptedData, encData_isText, timestamp, reference);
	}

	//GETTERS/SETTERS

	// NOT GENESIS ISSUE START FRON NUM
	protected long getStartKey() {
		return 0l;
	}

	public void setSidnerSignature(int index, byte[] signature) {
		if (signatures == null)
			signatures = new byte[signers.length][];
		
		signatures[index] = signature;
		
	}
	
	public static boolean hasTemplate(byte[] typeBytes) {
		if (typeBytes[2] < 0 ) return true;
		return false;
	}
	protected boolean hasTemplate() {
		return hasTemplate(this.typeBytes);
	}
	public static int getSignersLength(byte[] typeBytes) {
		byte mask = ~HAS_TEMPLATE_MASK;
		return typeBytes[2] & mask;
	}
	
	protected void setTypeBytes() {

		byte vers = 0;
		byte[] bytesLen = null;
		
		byte prop1 = 0;
		if (this.signers != null && this.signers.length > 0) {
			int len = this.signers.length; 
			if (len < 4) {
				prop1 = (byte)len;
			} else {
				prop1 = (byte)4;
			}
		}
		// set has TEMPLATE byte
		if (this.key > 0) prop1 = (byte) (HAS_TEMPLATE_MASK | prop1);
			
		byte prop2 = 0;
		if (publicData != null && publicData.length > 0) {
			prop2 = (byte)(prop2 | (byte)-128);
		}
		if (publicData != null && publicData.length > 0) {
			prop2 = (byte)(prop2 | (byte)-128);
		}

		if (this.typeBytes == null) {
			this.typeBytes  = new byte[]{TYPE_ID, vers, prop1, prop2};
		} else {
			this.typeBytes[2] = prop1; // property 1
			this.typeBytes[3] = prop2; // property 2
		}
		
		if (this.hashes != null && this.hashes.length > 0) {
			bytesLen = Ints.toByteArray(this.hashes.length); 
			//this.typeBytes[2] = bytesLen[2];
			//this.typeBytes[3] = bytesLen[3];
		}

	}

	//public static String getName() { return "Statement"; }

	public long getKey() 
	{
		return this.key;
	}
	
	public byte[] getData() 
	{
		return this.publicData;
	}
	
	public boolean isText()
	{
		if (publicData == null || publicData.length == 0) return false;
		return this.publicData_isText;
	}
	
	public boolean isEncrypted()
	{
		if (publicData == null || publicData.length == 0) return false;
		return (Arrays.equals(this.encryptedData, new byte[1]))?false:true;
	}

	public PublicKeyAccount[] getSigners() 
	{
		return this.signers;
	}
	public String[] getSignersB58() 
	{
		String[] pbKeys = new String[0];
		int i = 0;
		for (PublicKeyAccount key: this.signers)
		{
			pbKeys[i++] = Base58.encode(key.getPublicKey());
		};
		return pbKeys;
	}

	public byte[][] getSignersSignatures() 
	{
		return this.signatures;
	}
	public String[] getSignersSignaturesB58() 
	{
		String[] items = new String[0];
		int i = 0;
		for (byte[] item: this.signatures)
		{
			items[i++] = Base58.encode(item);
		};
		return items;
	}
	
	public boolean hasPublicText() {
		if (publicData == null || publicData.length == 0)
			return false;
		if (!Arrays.equals(this.encryptedData,new byte[1]))
			return false;
		
		return true;
	}


	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();

		//ADD CREATOR/SERVICE/DATA
		if (publicData != null && publicData.length > 0) {

			//ADD CREATOR/SERVICE/DATA
			if ( this.isText() && !this.isEncrypted() )
			{
				transaction.put("data", new String(this.publicData, Charset.forName("UTF-8")));
			}
			else
			{
				transaction.put("data", Base58.encode(this.publicData));
			}
			transaction.put("encrypted", this.isEncrypted());
			transaction.put("isText", this.isText());
		}

		if (this.key > 0)
			transaction.put("template", this.key);

		if (signers != null && signers.length >0) {
			transaction.put("singers", this.getSignersB58());
			transaction.put("signatures", this.getSignersSignaturesB58());
		}
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
		
		long key = 0l;
		if (hasTemplate(typeBytes)) 
		{
			//READ KEY
			byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
			key = Longs.fromByteArray(keyBytes);	
			position += KEY_LENGTH;
		}

		// DATA +++ - from core.transaction.R_Send.Parse(byte[], Long)
		byte[] arbitraryData = null;
		byte[] encryptedByte = null;
		byte[] isTextByte = null;
		if (typeBytes[3] < 0) {
			// IF here is DATA

			//READ DATA SIZE
			byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
			int dataSize = Ints.fromByteArray(dataSizeBytes);	
			position += DATA_SIZE_LENGTH;
	
			//READ DATA
			arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
			position += dataSize;
			
			encryptedByte = Arrays.copyOfRange(data, position, position + ENCRYPTED_LENGTH);
			position += ENCRYPTED_LENGTH;
			
			isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
			position += IS_TEXT_LENGTH;
		}

		int signersLen = getSignersLength(typeBytes);
		PublicKeyAccount[] signers = null;
		byte[][] signatures = null;
		if (signersLen > 0) {
			if (signersLen == 4) {
				//READ ONE BITE for len
				byte[] signersLenBytes = Arrays.copyOfRange(data, position, position + 1);
				signersLen = Byte.toUnsignedInt(signersLenBytes[0]) + 4;
				position ++;
			}
			signers = new PublicKeyAccount[signersLen];
			signatures = new byte[signersLen][];
			for (int i = 0; i < signersLen ; i++) {
				signers[i] = new PublicKeyAccount(Arrays.copyOfRange(data, position, position + CREATOR_LENGTH));
				position += CREATOR_LENGTH;
				signatures[i] = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
				position += SIGNATURE_LENGTH;
			}
		}
		
		if (signersLen == 0) {
			if (!asPack) {
				return null;
				//new R_SignNote2(typeBytes, creator, feePow, key, arbitraryData, isTextByte, encryptedByte, timestamp, reference, signatureBytes);
			} else {
				return null;
				//new R_SignNote2(typeBytes, creator, key, arbitraryData, isTextByte, encryptedByte, reference, signatureBytes);
			}
		} else {
			if (!asPack) {
				return null;
				//new R_SignNote2(typeBytes, creator, feePow, key, arbitraryData, isTextByte, encryptedByte, signers, signatures, timestamp, reference, signatureBytes);
			} else {
				return null;
				//new R_SignNote2(typeBytes, creator, key, arbitraryData, isTextByte, encryptedByte, signers, signatures, reference, signatureBytes);
			}
			
		}

	}

	//@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) {

		byte[] data = super.toBytes(withSign, releaserReference);

		if (this.key > 0 ) {
			//WRITE KEY
			byte[] keyBytes = Longs.toByteArray(this.key);
			keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
			data = Bytes.concat(data, keyBytes);
			
		}
		if (this.publicData != null ) {
			
			//WRITE DATA SIZE
			byte[] dataSizeBytes = Ints.toByteArray(this.publicData.length);
			data = Bytes.concat(data, dataSizeBytes);
	
			//WRITE DATA
			data = Bytes.concat(data, this.publicData);
			
			//WRITE ENCRYPTED
			data = Bytes.concat(data, this.encryptedData);
			
			//WRITE ISTEXT
			data = Bytes.concat(data, this.publicData_isText? new byte[]{1}: new byte[]{0});
		}

		return data;	
	}

	@Override
	public int getDataLength(boolean asPack) {
		int add_len = 0;
		if (this.publicData != null && this.publicData.length > 0)
			add_len += IS_TEXT_LENGTH + ENCRYPTED_LENGTH + DATA_SIZE_LENGTH + this.publicData.length;
		if (this.key > 0)
			add_len += KEY_LENGTH;
		
		if (asPack) {
			return BASE_LENGTH_AS_PACK + add_len;
		} else {
			return BASE_LENGTH + add_len;
		}
	}

	//@Override
	public int isValid(DCSet db, Long releaserReference) {
		
		//CHECK DATA SIZE
		if (publicData == null && key <= 0)
			return INVALID_DATA_LENGTH;
		
		if(publicData != null && publicData.length > BlockChain.MAX_REC_DATA_BYTES)
		{
			return INVALID_DATA_LENGTH;
		}
	

		int result = super.isValid(db, releaserReference);
		if (result != Transaction.VALIDATE_OK) return result; 
		
		// ITEM EXIST? - for assets transfer not need - amount expect instead
		if (this.key > 0 && !db.getItemTemplateMap().contains(this.key))
			return Transaction.ITEM_DOES_NOT_EXIST;

		return Transaction.VALIDATE_OK;

	}
	
	//PROCESS/ORPHAN
	
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
