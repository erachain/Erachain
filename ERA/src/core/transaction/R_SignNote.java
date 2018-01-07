package core.transaction;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
//import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
//import java.util.List;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import org.apache.log4j.Logger;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.item.ItemCls;
import database.wallet.FavoriteDocument;
import datachain.DCSet;



public class R_SignNote extends Transaction {

	private static final byte TYPE_ID = (byte) SIGN_NOTE_TRANSACTION;
	private static final String NAME_ID = "Sign Note";

	protected static final byte HAS_TEMPLATE_MASK = (byte)(1 << 7);
	/*
	PROPERTIES:
	[0] - type
	[1] - version 
	[2] bits[0] - =1 - has Template 
	[2] bits [6,7] - signers: 0 - none; 1..3 = 1..3; 4 = LIST -> 1 byte for LIST.len + 3 
	[3] - < 0 - has DATA
		*/
	protected long key; // key for Template
	protected byte[] data;
	protected byte[] encrypted;
	protected byte[] isText;
	protected PublicKeyAccount[] signers; // for all it need ecnrypt
	protected byte[][] signatures; // - multi sign
	
	public R_SignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference) {
		
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

		this.key = templateKey;
		this.data = data;
		this.encrypted = encrypted;
		this.isText = isText;
	}
	public R_SignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, feePow, templateKey, data, isText, encrypted, timestamp, reference);
		this.signature = signature;
		//this.calcFee();
	}
	// asPack
	public R_SignNote(byte[] typeBytes, PublicKeyAccount creator, long templateKey, byte[] data, byte[] isText, byte[] encrypted, Long reference, byte[] signature) {
		this(typeBytes, creator, (byte)0, templateKey, data, isText, encrypted, 0l, reference);
		this.signature = signature;
		// not need this.calcFee();
	}
	public R_SignNote(PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference, byte[] signature) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, templateKey, data, isText, encrypted, timestamp, reference, signature);
		// set props
		this.setTypeBytes();
	}
	public R_SignNote(PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, templateKey, data, isText, encrypted, timestamp, reference);
		// set props
		this.setTypeBytes();
	}
	public R_SignNote(byte version, byte ptoperty1, byte ptoperty2, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data, byte[] isText, byte[] encrypted, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, version, ptoperty1, ptoperty2}, creator, feePow, templateKey, data, isText, encrypted, timestamp, reference);
		// set props
		this.setTypeBytes();
	}
	public R_SignNote(byte[] typeBytes, PublicKeyAccount creator, byte feePow, long templateKey, byte[] data,
			byte[] isText, byte[] encrypted, PublicKeyAccount[] signers, byte[][] signatures, long timestamp, Long reference, byte[] signature)
	{
		this(typeBytes, creator, feePow, templateKey, data, isText, encrypted, timestamp, reference, signature);
		this.signers = signers;
		this.signatures = signatures;
		this.setTypeBytes();
	}
	// as Pack
	public R_SignNote(byte[] typeBytes, PublicKeyAccount creator, long templateKey, byte[] data,
			byte[] isText, byte[] encrypted, PublicKeyAccount[] signers, byte[][] signatures, Long reference, byte[] signature)
	{
		this(typeBytes, creator, templateKey, data, isText, encrypted, reference, signature);
		this.signers = signers;
		this.signatures = signatures;
		this.setTypeBytes();
	}

	//GETTERS/SETTERS
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
		if (data != null && data.length > 0) {
			prop2 = (byte)(prop2 | (byte)-128);
		}

		if (this.typeBytes == null) {
			this.typeBytes  = new byte[]{TYPE_ID, vers, prop1, prop2};
		} else {
			this.typeBytes[2] = prop1; // property 1
			this.typeBytes[3] = prop2; // property 2
		}
	}

	//public static String getName() { return "Statement"; }

	public long getKey() 
	{
		return this.key;
	}
	
	public byte[] getData() 
	{
		return this.data;
	}
	
	public boolean isText()
	{
		if (data == null || data.length == 0) return false;
		return (Arrays.equals(this.isText,new byte[1]))?false:true;
	}
	
	public boolean isEncrypted()
	{
		if (data == null || data.length == 0) return false;
		return (Arrays.equals(this.encrypted, new byte[1]))?false:true;
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
		if (data == null || data.length == 0)
			return false;
		if (!Arrays.equals(this.encrypted,new byte[1]))
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
		if (data != null && data.length > 0) {

			//ADD CREATOR/SERVICE/DATA
			if ( this.isText() && !this.isEncrypted() )
			{
				transaction.put("data", new String(this.data, Charset.forName("UTF-8")));
			}
			else
			{
				transaction.put("data", Base58.encode(this.data));
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
				return new R_SignNote(typeBytes, creator, feePow, key, arbitraryData, isTextByte, encryptedByte, timestamp, reference, signatureBytes);
			} else {
				return new R_SignNote(typeBytes, creator, key, arbitraryData, isTextByte, encryptedByte, reference, signatureBytes);
			}
		} else {
			if (!asPack) {
				return new R_SignNote(typeBytes, creator, feePow, key, arbitraryData, isTextByte, encryptedByte, signers, signatures, timestamp, reference, signatureBytes);
			} else {
				return new R_SignNote(typeBytes, creator, key, arbitraryData, isTextByte, encryptedByte, signers, signatures, reference, signatureBytes);
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
		if (this.data != null ) {
			
			//WRITE DATA SIZE
			byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
			data = Bytes.concat(data, dataSizeBytes);
	
			//WRITE DATA
			data = Bytes.concat(data, this.data);
			
			//WRITE ENCRYPTED
			data = Bytes.concat(data, this.encrypted);
			
			//WRITE ISTEXT
			data = Bytes.concat(data, this.isText);
		}

		return data;	
	}

	@Override
	public int getDataLength(boolean asPack) {
		int add_len = 0;
		if (this.data != null && this.data.length > 0)
			add_len += IS_TEXT_LENGTH + ENCRYPTED_LENGTH + DATA_SIZE_LENGTH + this.data.length;
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
		if (data == null && key <= 0)
			return INVALID_DATA_LENGTH;
		
		if(data != null && data.length > BlockChain.MAX_REC_DATA_BYTES)
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
	public Tuple3<String,String,JSONObject> parse_Data_V2_Without_Files() throws Exception{
		//Version, Title, JSON, Files	
			
			//CHECK IF WE MATCH BLOCK LENGTH
			if (data.length < Transaction.DATA_JSON_PART_LENGTH)
			{
				throw new Exception("Data does not match block length " + data.length);
			}
			int position = 0;
			
			// read version
			byte[] version_Byte = Arrays.copyOfRange(data, position , Transaction.DATA_VERSION_PART_LENGTH);
			position += Transaction.DATA_VERSION_PART_LENGTH;
			// read title
			byte[] titleSizeBytes = Arrays.copyOfRange(data, position, position + Transaction.DATA_TITLE_PART_LENGTH);
			int titleSize = Ints.fromByteArray(titleSizeBytes);
			position += Transaction.DATA_TITLE_PART_LENGTH;
			
			byte[] titleByte = Arrays.copyOfRange(data, position , position + titleSize);
			
			position +=titleSize;
			//READ Length JSON PART
			byte[] dataSizeBytes = Arrays.copyOfRange(data, position , position + Transaction.DATA_JSON_PART_LENGTH);
			int JSONSize = Ints.fromByteArray(dataSizeBytes);	
			
			position += Transaction.DATA_JSON_PART_LENGTH;
			//READ JSON
			byte[] arbitraryData = Arrays.copyOfRange(data, position, position + JSONSize);
			JSONObject json = (JSONObject) JSONValue.parseWithException(new String(arbitraryData, Charset.forName("UTF-8")));
			
			String title = new String(titleByte, Charset.forName("UTF-8"));
			String version = new String(version_Byte, Charset.forName("UTF-8"));
			
						
			 return new Tuple3(version,title,json);
		}
	
	public  Tuple4<String,String,JSONObject,HashMap <String,Tuple2<Boolean, byte[]>>> parse_Data_V2() throws Exception{
		
	return parse_Data_V2(this.data);	
		
	}
	public static  Tuple4<String,String,JSONObject,HashMap <String,Tuple2<Boolean, byte[]>>> parse_Data_V2(byte[] data) throws Exception{
	//Version, Title, JSON, Files	
		
		//CHECK IF WE MATCH BLOCK LENGTH
		if (data.length < Transaction.DATA_JSON_PART_LENGTH)
		{
			throw new Exception("Data does not match block length " + data.length);
		}
		int position = 0;
		
		// read version
		byte[] version_Byte = Arrays.copyOfRange(data, position , Transaction.DATA_VERSION_PART_LENGTH);
		position += Transaction.DATA_VERSION_PART_LENGTH;
		// read title
		byte[] titleSizeBytes = Arrays.copyOfRange(data, position, position + Transaction.DATA_TITLE_PART_LENGTH);
		int titleSize = Ints.fromByteArray(titleSizeBytes);
		position += Transaction.DATA_TITLE_PART_LENGTH;
		
		byte[] titleByte = Arrays.copyOfRange(data, position , position + titleSize);
		
		position +=titleSize;
		//READ Length JSON PART
		byte[] dataSizeBytes = Arrays.copyOfRange(data, position , position + Transaction.DATA_JSON_PART_LENGTH);
		int JSONSize = Ints.fromByteArray(dataSizeBytes);	
		
		position += Transaction.DATA_JSON_PART_LENGTH;
		//READ JSON
		byte[] arbitraryData = Arrays.copyOfRange(data, position, position + JSONSize);
		JSONObject json = (JSONObject) JSONValue.parseWithException(new String(arbitraryData, Charset.forName("UTF-8")));
		
		String title = new String(titleByte, Charset.forName("UTF-8"));
		String version = new String(version_Byte, Charset.forName("UTF-8"));
		position += JSONSize;
		HashMap<String,Tuple2<Boolean, byte[]>> out_Map = new HashMap<String,Tuple2<Boolean, byte[]>>();
		JSONObject files;
		Set files_key_Set;
		//v2.0
		if (json.containsKey("&*&*%$$%_files_#$@%%%")) { //return new Tuple4(version,title,json, null);
	
		
		
		files =(JSONObject) json.get("&*&*%$$%_files_#$@%%%");
		
		
		files_key_Set = files.keySet();
		for (int i = 0; i < files_key_Set.size(); i++) {
			JSONObject file = (JSONObject) files.get(i+"");
			
			
				String name = (String) file.get("File_Name"); // File_Name
				Boolean zip = new Boolean((String) file.get("ZIP")); // ZIP
				byte[] bb = Arrays.copyOfRange(data, position, position + new Integer((String) file.get("Size"))); //Size
				position = position + new Integer((String) file.get("Size")); //Size
				out_Map.put(name, new Tuple2(zip,bb));	
					
		}
		 return new Tuple4(version, title, json, out_Map);
		}
		// v 2.1
		if (json.containsKey("F")) { // return new Tuple4(version,title,json, null);
		
		
		
		files =(JSONObject) json.get("F");
		
		
		files_key_Set = files.keySet();
		for (int i = 0; i < files_key_Set.size(); i++) {
			JSONObject file = (JSONObject) files.get(i+"");
			
			
				String name = (String) file.get("FN"); // File_Name
				Boolean zip = new Boolean((String) file.get("ZP")); // ZIP
				byte[] bb = Arrays.copyOfRange(data, position, position + new Integer((String) file.get("SZ"))); //Size
				position = position + new Integer((String) file.get("SZ")); //Size
				out_Map.put(name, new Tuple2(zip,bb));	
					
		}
		
			
		 return new Tuple4(version, title, json, out_Map);
		}
		return new Tuple4(version, title, json, null);
	}
	
	public static  byte[]  Json_Files_to_Byte_V2(String title, JSONObject json, HashMap<String,Tuple2<Boolean,byte[]>> files) throws Exception {
	
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		outStream.write("v 2.00".getBytes(Charset.forName("UTF-8"))); // only 6 simbols!!!
		byte[] title_Bytes = "".getBytes(Charset.forName("UTF-8"));
		if (title !=null){
			title_Bytes = title.getBytes(Charset.forName("UTF-8"));
		}
		
		
		byte[] size_Title = ByteBuffer.allocate(Transaction.DATA_TITLE_PART_LENGTH).putInt(title_Bytes.length).array();
		
		outStream.write(size_Title);
		outStream.write(title_Bytes);
		
		if (json == null || json.equals("") ) return outStream.toByteArray();
		
		byte[] JSON_Bytes ;
		byte[] size_Json;
		
		if (files == null || files.isEmpty()){
			JSON_Bytes = json.toString().getBytes(Charset.forName("UTF-8"));
			// convert int to byte
			size_Json = ByteBuffer.allocate(Transaction.DATA_JSON_PART_LENGTH).putInt( JSON_Bytes.length).array();
			outStream.write(size_Json);
			outStream.write(JSON_Bytes);
			return outStream.toByteArray(); 
		}
		// if insert Files
		Iterator<Entry<String, Tuple2<Boolean, byte[]>>> it = files.entrySet().iterator();
		JSONObject files_Json = new JSONObject();
		int i = 0;
		 ArrayList<byte[]> out_files = new ArrayList<byte[]>();
		while(it.hasNext()){
			Entry<String, Tuple2<Boolean, byte[]>> file = it.next();
			JSONObject file_Json = new JSONObject();
			file_Json.put("FN", file.getKey()); //File_Name 
			file_Json.put("ZP", file.getValue().a.toString()); //ZIP
			file_Json.put("SZ", file.getValue().b.length+""); //Size
			files_Json.put(i+"", file_Json);
			out_files.add(i,file.getValue().b);
			i++;
		} 
		json.put("F",files_Json);
		JSON_Bytes = json.toString().getBytes(Charset.forName("UTF-8"));
		// convert int to byte
		size_Json = ByteBuffer.allocate(Transaction.DATA_JSON_PART_LENGTH).putInt( JSON_Bytes.length).array();
		outStream.write(size_Json);
		outStream.write(JSON_Bytes);
		for(i=0; i<out_files.size(); i++){
				outStream.write(out_files.get(i));	
		}
		return outStream.toByteArray();

	}
	public boolean isFavorite(){
			
		return Controller.getInstance().wallet.database.getDocumentFavoritesSet().contains(this);
		
		
		
	}
	

}
