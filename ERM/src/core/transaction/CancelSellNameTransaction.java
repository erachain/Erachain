package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Crypto;
import core.naming.Name;
import core.naming.NameSale;
import database.ItemAssetBalanceMap;
import database.DBSet;

public class CancelSellNameTransaction extends Transaction
{
	private static final byte TYPE_ID = (byte)Transaction.CANCEL_SELL_NAME_TRANSACTION;
	private static final String NAME_ID = "OLD: Cancel Sell Name";
	private static final int NAME_SIZE_LENGTH = 4;
	private static final int BASE_LENGTH = Transaction.BASE_LENGTH + NAME_SIZE_LENGTH;
	
	//private PublicKeyAccount owner;
	private String name;
	
	public CancelSellNameTransaction(byte[] typeBytes, PublicKeyAccount creator, String name, byte feePow, long timestamp, Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);	
		this.name = name;
	}
	public CancelSellNameTransaction(byte[] typeBytes, PublicKeyAccount creator, String name, byte feePow, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, name, feePow, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	public CancelSellNameTransaction(PublicKeyAccount creator, String name, byte feePow, long timestamp, Long reference, byte[] signature) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, name, feePow, timestamp, reference, signature);
	}
	public CancelSellNameTransaction(PublicKeyAccount creator, String name, byte feePow, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, name, feePow, timestamp, reference);
	}
	
	//GETTERS/SETTERS
	// public static String getName() { return "OLD: Cancel Sell Name"; }

	
	public String getName()
	{
		return this.name;
	}
	
	//PARSE CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception
	{	
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}
				
		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;

		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
		
		//READ REFERENCE
		byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		long reference = Longs.fromByteArray(referenceBytes);	
		position += REFERENCE_LENGTH;
		
		//READ creator
		byte[] registrantBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(registrantBytes);
		position += CREATOR_LENGTH;
		
		//READ NAME
		byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		int nameLength = Ints.fromByteArray(nameLengthBytes);
		position += NAME_SIZE_LENGTH;
						
		if(nameLength < 1 || nameLength > 400)
		{
			throw new Exception("Invalid name length");
		}
						
		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String name = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;
		
		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new CancelSellNameTransaction(typeBytes, creator, name, feePow, timestamp, reference, signatureBytes);
	}	

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
								
		//ADD REGISTRANT/NAME/VALUE
		transaction.put("creator", this.creator.getAddress());
		transaction.put("name", this.name);
								
		return transaction;	
	}

	@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		//byte[] typeBytes = Ints.toByteArray(TYPE_ID);
		//typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, this.typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		byte[] referenceBytes = Longs.toByteArray(this.reference);
		referenceBytes = Bytes.ensureCapacity(referenceBytes, REFERENCE_LENGTH, 0);
		data = Bytes.concat(data, referenceBytes);
		
		//WRITE creator
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		//WRITE NAME SIZE
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);
				
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);
		
		//WRITE FEE POWER
		byte[] feePowBytes = new byte[1];
		feePowBytes[0] = this.feePow;
		data = Bytes.concat(data, feePowBytes);

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);
		
		return data;	
	}

	@Override
	public int getDataLength(boolean asPack) 
	{
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		
		return BASE_LENGTH + nameLength;
	}
	
	//VALIDATE

	//@Override
	public int isValid(DBSet db, Long releaserReference) 
	{
		//CHECK NAME LENGTH
		int nameLength = this.name.getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > 400 || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
		
		//CHECK IF NAME EXISTS
		Name name = db.getNameMap().get(this.name);
		if(name == null)
		{
			return NAME_DOES_NOT_EXIST;
		}
						
		//CHECK IF OWNER IS OWNER
		if(!name.getOwner().getAddress().equals(this.creator.getAddress()))
		{
			return INVALID_MAKER_ADDRESS;
		}
		
		//CHECK IF NAME FOR SALE ALREADY
		if(!db.getNameExchangeMap().contains(this.name))
		{
			return NAME_NOT_FOR_SALE;
		}
						
		return super.isValid(db, releaserReference);
	}
	
	//PROCESS/ORPHAN

	//@Override
	public void process(DBSet db, boolean asPack) 
	{
		//UPDATE creator
		super.process(db, asPack);

		//SET ORPHAN DATA
		NameSale nameSale = db.getNameExchangeMap().getNameSale(this.name);
		db.getCancelSellNameMap().set(this, nameSale.getAmount());
		
		//DELETE FROM DATABASE
		db.getNameExchangeMap().delete(this.name);
		
	}

	//@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		//UPDATE creator
		super.orphan(db, asPack);

		//ADD TO DATABASE
		BigDecimal amount = db.getCancelSellNameMap().get(this);
		NameSale nameSale = new NameSale(this.name, amount);
		db.getNameExchangeMap().add(nameSale);	
		
		//DELETE ORPHAN DATA
		db.getCancelSellNameMap().delete(this);
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

	//@Override
	public BigDecimal getAmount(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8);
		}
		
		return BigDecimal.ZERO;
	}
	
	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		return subAssetAmount(null, this.creator.getAddress(), FEE_KEY, this.fee);
	}
	
	public int calcBaseFee() {
		return calcCommonFee();
	}
}
