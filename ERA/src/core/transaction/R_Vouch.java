package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Base58;
import core.crypto.Crypto;
import datachain.DCSet;
import datachain.ItemAssetBalanceMap;
import lang.Lang;
import utils.Converter;


// TODO
// ver =1 - vouching incommed transfers - assets etc.
//   ++ FEE = 0, no TIMESTAMP??, max importance for including in block 
public class R_Vouch extends Transaction {

	private static final byte TYPE_ID = (byte)Transaction.VOUCH_TRANSACTION;
	private static final String NAME_ID = "Vouch";

	protected int height;
	protected int seq;
	
	protected static final int BASE_LENGTH = Transaction.BASE_LENGTH + HEIGHT_LENGTH + SEQ_LENGTH;

	static Logger LOGGER = Logger.getLogger(R_Vouch.class.getName());

	public R_Vouch(byte[] typeBytes, PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp, Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);

		this.height = height;
		this.seq = seq;
	}
	public R_Vouch(byte[] typeBytes, PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, feePow, height, seq, timestamp, reference);
		this.signature = signature;
		//this.calcFee();
	}
	// as pack
	public R_Vouch(byte[] typeBytes, PublicKeyAccount creator, int height, int seq, Long reference, byte[] signature) {
		this(typeBytes, creator, (byte)0, height, seq, 0l, reference);
		this.signature = signature;
	}
	public R_Vouch(PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, height, seq, timestamp, reference);
	}
	public R_Vouch(PublicKeyAccount creator, byte feePow, int height, int seq, long timestamp, Long reference, byte[] signature) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, feePow, height, seq, timestamp, reference, signature);
	}
	// as pack
	public R_Vouch(PublicKeyAccount creator, int height, int seq, Long reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, (byte)0, height, seq, 0l, reference);
	}

	
	//GETTERS/SETTERS

	//public static String getName() { return "Send"; }

	public int getVouchHeight() 
	{
		return this.height;
	}
	
	public int getVouchSeq() 
	{
		return this.seq;
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

		transaction.put("height", this.height);
		transaction.put("seq", this.seq);
		
		return transaction;	
	}

	//PARSE/CONVERT
	
	public static Transaction Parse(byte[] data, Long releaserReference) throws Exception{

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

		//READ HEIGHT
		byte[] heightBytes = Arrays.copyOfRange(data, position, position + HEIGHT_LENGTH);
		int height = Ints.fromByteArray(heightBytes);	
		position += HEIGHT_LENGTH;
				
		//READ SEQ
		byte[] seqBytes = Arrays.copyOfRange(data, position, position + SEQ_LENGTH);
		int seq = Ints.fromByteArray(seqBytes);	
		position += SEQ_LENGTH;
		
		if (asPack) {
			return new R_Vouch(typeBytes, creator, height, seq, reference, signatureBytes);
		} else {
			return new R_Vouch(typeBytes, creator, feePow, height, seq, timestamp, reference, signatureBytes);
		}

	}

	@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) {

		byte[] data = super.toBytes(withSign, releaserReference);

		//WRITE HEIGHT
		byte[] heightBytes = Ints.toByteArray(this.height);
		heightBytes = Bytes.ensureCapacity(heightBytes, HEIGHT_LENGTH, 0);
		data = Bytes.concat(data, heightBytes);

		//SEQ HEIGHT
		byte[] seqBytes = Ints.toByteArray(this.seq);
		seqBytes = Bytes.ensureCapacity(seqBytes, SEQ_LENGTH, 0);
		data = Bytes.concat(data, seqBytes);

		return data;	
	}

	@Override
	public int getDataLength(boolean asPack) {
		// not include item reference
		if (asPack) {
			return BASE_LENGTH_AS_PACK;
		} else {
			return BASE_LENGTH;
		}
	}

	//@Override
	public int isValid(DCSet db, Long releaserReference) {
		
		if (this.height < 2 ) {
			//CHECK HEIGHT - not 0 and NOT GENESIS
			return INVALID_BLOCK_HEIGHT;
		}

		if (this.seq < 0 ) {
			//CHECK DATA SIZE
			return INVALID_BLOCK_TRANS_SEQ_ERROR;
		}
			
		int result = super.isValid(db, releaserReference);
		if (result != Transaction.VALIDATE_OK) return result;

		/*
		//Block block1 = Controller.getInstance().getBlockByHeight(db, height);
		byte[] b = db.getHeightMap().getBlockByHeight(height);
		if (b == null )
			return INVALID_BLOCK_HEIGHT_ERROR;

		Block block = db.getBlockMap().get(b);
		if (block == null)
			return INVALID_BLOCK_HEIGHT_ERROR;		
		Transaction tx = block.getTransaction(seq);
		if (tx == null )
			return INVALID_BLOCK_TRANS_SEQ_ERROR;
			*/
		Transaction tx = db.getTransactionFinalMap().getTransaction(height, seq);
		if (tx == null ) {
			if (height == 104841 && seq == 1) {
				// "32tebyLDxbucXod4N4TAZZGCMqLJdXtDQuY4o1P4gxDBcBKkdCi41LdAxVD9Xzy3rmPQ41yHXtFJvhD6SPkrfaa3
			} else {
				return INVALID_BLOCK_TRANS_SEQ_ERROR;
			}
		}

		return Transaction.VALIDATE_OK;
		
	}

	
	
	public void process(Block block, boolean asPack) {

		super.process(block, asPack);
		
		if (block == null)
			return;
		
		// make key for vouching record
		Tuple2<Integer, Integer> recordKey = new Tuple2<Integer, Integer>(this.height, this.seq);
		// find value
		Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> value =  db.getVouchRecordMap().get(recordKey);

		// update value
		Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> valueNew;
		BigDecimal amount = this.creator.getBalanceUSE(Transaction.RIGHTS_KEY, db);
		List<Tuple2<Integer, Integer>> listNew;
		if (value == null) {
			listNew = new ArrayList<Tuple2<Integer, Integer>>();
		} else {
			listNew = value.b;
			amount = amount.add(value.a);
		}
		
		listNew.add(new Tuple2<Integer, Integer>(this.getBlockHeightByParent(db), this.getSeqNo(db)));
		// for test only!!
		//listNew.add(new Tuple2<Integer, Integer>(2, 2));
		
		valueNew =
				new Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>(
					amount,
					listNew
					);			
		db.getVouchRecordMap().set(recordKey, valueNew);
		
	}

	public void orphan(boolean asPack) {

		super.orphan(asPack);
		
		// make key for vouching record
		Tuple2<Integer, Integer> recordKey = new Tuple2<Integer, Integer>(this.height, this.seq);
		// find value
		Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> value =  db.getVouchRecordMap().get(recordKey);
		// update value
		List<Tuple2<Integer, Integer>> listNew = value.b;
		
		listNew.remove(new Tuple2<Integer, Integer>(this.getBlockHeight(db), this.getSeqNo(db)));
		// for test ONLY !!!
		//listNew.remove(new Tuple2<Integer, Integer>(2, 2));
		
		Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> valueNew =
				new Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>(
					value.a.subtract(this.creator.getBalanceUSE(Transaction.RIGHTS_KEY, db)),
					listNew
					);
		db.getVouchRecordMap().set(recordKey, valueNew);

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

		Transaction record = dcSet.getTransactionFinalMap().getTransaction(height, seq);
		if (record == null) {
			LOGGER.debug("core.transaction.R_Vouch.getRecipientAccounts() not found record: " + height + "-" + seq);
			return accounts;
		}
		accounts.addAll(record.getInvolvedAccounts());
				
		return accounts;
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		if(address.equals(creator.getAddress())) return true;
		
		for (Account recipient: this.getRecipientAccounts())
		{
			if (address.equals(recipient.getAddress()))
					return true;
		}

		return false;
	}

}