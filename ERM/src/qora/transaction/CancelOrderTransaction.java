package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import qora.account.Account;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.assets.Order;
import qora.crypto.Base58;
import qora.crypto.Crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;

public class CancelOrderTransaction extends Transaction
{
	private static final int ORDER_LENGTH = 64;
	private static final int BASE_LENGTH = 1 + TIMESTAMP_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH + ORDER_LENGTH + SIGNATURE_LENGTH;
	
	private BigInteger order;
	
	public CancelOrderTransaction(PublicKeyAccount creator, BigInteger order, long timestamp, byte[] reference) {
		super(CANCEL_ORDER_TRANSACTION, creator, timestamp, reference);
		this.order = order;
	}
	public CancelOrderTransaction(PublicKeyAccount creator, BigInteger order, byte feePow, long timestamp, byte[] reference, byte[] signature) {
		this(creator, order, timestamp, reference);
		this.signature = signature;
		this.feePow = feePow;
		this.calcFee();
	}
	public CancelOrderTransaction(PublicKeyAccount creator, BigInteger order, byte feePow, long timestamp, byte[] reference) {
		this(creator, order, timestamp, reference);
		this.feePow = feePow;
		this.calcFee();
	}
	
	//GETTERS/SETTERS
	
	public BigInteger getOrder()
	{
		return this.order;
	}
	
	//PARSE CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception
	{	
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}
		
		int position = 0;
		
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
		
		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		//READ ORDER
		byte[] orderBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
		BigInteger order = new BigInteger(orderBytes);
		position += ORDER_LENGTH;
		
		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new CancelOrderTransaction(creator, order, feePow, timestamp, reference, signatureBytes);
	}	

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
								
		//ADD CREATOR/ORDER
		transaction.put("creator", this.creator.getAddress());
		transaction.put("order", Base58.encode(this.order.toByteArray()));
								
		return transaction;	
	}

	@Override
	public byte[] toBytes(boolean withSign) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(CANCEL_ORDER_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		//WRITE ORDER
		byte[] orderBytes = this.order.toByteArray();
		byte[] fill = new byte[ORDER_LENGTH - orderBytes.length];
		orderBytes = Bytes.concat(fill, orderBytes);
		data = Bytes.concat(data, orderBytes);
				
		//WRITE FEE POWER
		byte[] feePowBytes = new byte[1];
		feePowBytes[0] = this.feePow;
		data = Bytes.concat(data, feePowBytes);

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);
		
		return data;	
	}

	@Override
	public int getDataLength() 
	{
		return TYPE_LENGTH + BASE_LENGTH;
	}
	
	//VALIDATE

	@Override
	public int isValid(DBSet db) 
	{
		//CHECK IF ORDER EXISTS
		Order order = db.getOrderMap().get(this.order);
		if(order== null)
		{
			return ORDER_DOES_NOT_EXIST;
		}
		
		//CHECK CREATOR
		if(!Crypto.getInstance().isValidAddress(this.creator.getAddress()))
		{
			return INVALID_ADDRESS;
		}
				
		//CHECK IF CREATOR IS CREATOR
		if(!order.getCreator().getAddress().equals(this.creator.getAddress()))
		{
			return INVALID_ORDER_CREATOR;
		}
		
		//CHECK IF SENDER HAS ENOUGH FEE BALANCE
		if(this.creator.getConfirmedBalance(FEE_KEY, db).compareTo(this.fee) == -1)
		{
			return NOT_ENOUGH_FEE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
				
		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	@Override
	public void process(DBSet db) 
	{
		//UPDATE CREATOR
		process_fee(db);

		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.signature, db);
				
		//SET ORPHAN DATA
		Order order = db.getOrderMap().get(this.order);
		db.getCompletedOrderMap().add(order);
		
		//UPDATE BALANCE OF CREATOR
		this.creator.setConfirmedBalance(order.getHave(), this.creator.getConfirmedBalance(order.getHave(), db).add(order.getAmountLeft()), db);
		
		//DELETE FROM DATABASE
		db.getOrderMap().delete(this.order);	
	}

	@Override
	public void orphan(DBSet db) 
	{
		//UPDATE CREATOR
		orphan_fee(db);
												
		//UPDATE REFERENCE OF CREATOR
		this.creator.setLastReference(this.reference, db);
				
		//ADD TO DATABASE
		Order order = db.getCompletedOrderMap().get(this.order);
		db.getOrderMap().add(order);	
		
		//REMOVE BALANCE OF CREATOR
		this.creator.setConfirmedBalance(order.getHave(), this.creator.getConfirmedBalance(order.getHave(), db).subtract(order.getAmountLeft()), db);
		
		//DELETE ORPHAN DATA
		db.getCompletedOrderMap().delete(this.order);
	}

	@Override
	public List<Account> getInvolvedAccounts()
	{
		List<Account> accounts = new ArrayList<Account>();
		accounts.add(this.creator);
		return accounts;
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

	/*
	@Override
	public BigDecimal Amount(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8);
		}
		
		return BigDecimal.ZERO;
	}
	*/
	
	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);

		Order order;

		if(DBSet.getInstance().getCompletedOrderMap().contains(this.order))
		{
			order =  DBSet.getInstance().getCompletedOrderMap().get(this.order);
		}
		else
		{
			order =  DBSet.getInstance().getOrderMap().get(this.order);
		}	
		
		assetAmount = addAssetAmount(assetAmount, this.creator.getAddress(), order.getHave(), order.getAmountLeft());
		
		return assetAmount;
	}
	public BigDecimal calcBaseFee() {
		return calcCommonFee();
	}
}
