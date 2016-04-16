package qora.transaction;

import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import qora.account.Account;
//import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Crypto;
import qora.naming.Name;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;

public class UpdateNameTransaction extends Transaction 
{
	private static final byte TYPE_ID = (byte)UPDATE_NAME_TRANSACTION;
	private static final String NAME_ID = "OLD: Update Name";
	private static final int BASE_LENGTH = TransactionAmount.BASE_LENGTH;

	private Name name;
	
	public UpdateNameTransaction(byte[] typeBytes, PublicKeyAccount creator, Name name, byte feePow, long timestamp, byte[] reference) 
	{
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
		
		this.creator = creator;
		this.name = name;
	}
	public UpdateNameTransaction(byte[] typeBytes, PublicKeyAccount creator, Name name, byte feePow, long timestamp, byte[] reference, byte[] signature) 
	{
		this(typeBytes, creator, name, feePow, timestamp, reference);		
		this.signature = signature;
		this.calcFee();
	}
	public UpdateNameTransaction(PublicKeyAccount creator, Name name, byte feePow, long timestamp, byte[] reference) 
	{
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, name, feePow, timestamp, reference);		
	}

	//GETTERS/SETTERS

	//public static String getName() { return "OLD: Update Name"; }

	public Name getName()
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
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;
		
		//READ CREATOR
		byte[] registrantBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(registrantBytes);
		position += CREATOR_LENGTH;
		
		//READ NAME
		Name name = Name.Parse(Arrays.copyOfRange(data, position, data.length));
		position += name.getDataLength();
		
		//READ FEE POWER
		byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
		byte feePow = feePowBytes[0];
		position += 1;
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		
		return new UpdateNameTransaction(typeBytes, creator, name, feePow, timestamp, reference, signatureBytes);
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
				
		//ADD REGISTRANT/NAME/VALUE
		transaction.put("creator", this.creator.getAddress());
		transaction.put("newOwner", this.name.getOwner().getAddress());
		transaction.put("name", this.name.getName());
		transaction.put("newValue", this.name.getValue());
				
		return transaction;	
	}
	
	@Override
	public byte[] toBytes(boolean withSign, byte[] releaserReference) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		data = Bytes.concat(data, this.typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		//WRITE NAME
		data = Bytes.concat(data , this.name.toBytes());
		
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
		return BASE_LENGTH + this.name.getDataLength();
	}
	
	//VALIDATE
		
	@Override
	public int isValid(DBSet db, byte[] releaserReference) 
	{
		//CHECK NAME LENGTH
		int nameLength = this.name.getName().getBytes(StandardCharsets.UTF_8).length;
		if(nameLength > 400 || nameLength < 1)
		{
			return INVALID_NAME_LENGTH;
		}
				
		//CHECK VALUE LENGTH
		int valueLength = this.name.getValue().getBytes(StandardCharsets.UTF_8).length;
		if(valueLength > 4000 || valueLength < 1)
		{
			return INVALID_VALUE_LENGTH;
		}
		
		//CHECK CREATOR
		if(!Crypto.getInstance().isValidAddress(this.name.getOwner().getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK IF NAME EXISTS
		if(!db.getNameMap().contains(this.name))
		{
			return NAME_DOES_NOT_EXIST;
		}
		
		//CHECK IF NAMESALE EXISTS
		if(db.getNameExchangeMap().contains(this.name.getName()))
		{
			return NAME_ALREADY_FOR_SALE;
		}
		
		//CHECK IF CREATOR IS CREATOR
		if(!db.getNameMap().get(this.name.getName()).getOwner().getAddress().equals(this.creator.getAddress()))
		{
			return INVALID_NAME_CREATOR;
		}
		
		//CHECK IF SENDER HAS ENOUGH FEE BALANCE
		if(this.creator.getConfirmedBalance(OIL_KEY, db).compareTo(this.fee) == -1)
		{
			return NOT_ENOUGH_FEE;
		}
		
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(releaserReference==null ? this.creator.getLastReference(db) : releaserReference, this.reference))
		{
			return INVALID_REFERENCE;
		}		
		
		return VALIDATE_OK;
	}
	
	//PROCESS/ORPHAN

	//@Override
	public void process(DBSet db, boolean asPack)
	{
		//UPDATE CREATOR
		super.process(db, asPack);
							
		//SET ORPHAN DATA
		Name oldName = db.getNameMap().get(this.name.getName());
		db.getUpdateNameMap().set(this, oldName);
		
		//INSERT INTO DATABASE
		db.getNameMap().add(this.name);
	}

	//@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		super.orphan(db, asPack);

		//RESTORE ORPHAN DATA
		Name oldName = db.getUpdateNameMap().get(this);
		db.getNameMap().add(oldName);
		
		//DELETE ORPHAN DATA
		db.getUpdateNameMap().delete(this);
	}


	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.add(this.creator);
		accounts.add(this.name.getOwner());
		return accounts;
	}

	@Override
	public HashSet<Account> getRecipientAccounts() {
		return new HashSet<Account>();
	}

	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()) || address.equals(this.name.getOwner().getAddress()))
		{
			return true;
		}
		
		return false;
	}

	//@Override
	public BigDecimal viewAmount(Account account) 
	{
		if(account.getAddress().equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		return BigDecimal.ZERO;
	}

	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		return subAssetAmount(null, this.creator.getAddress(), BalanceMap.FEE_KEY, this.fee);
	}

	public int calcBaseFee() {
		return calcCommonFee();
	}
}
