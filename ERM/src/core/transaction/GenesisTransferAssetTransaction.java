package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import ntp.NTP;
import utils.NumberAsString;

import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.ItemCls;
import database.ItemAssetBalanceMap;
import database.AddressForging;
import database.DBSet;

public class GenesisTransferAssetTransaction extends Genesis_Record {

	private static final byte TYPE_ID = (byte)Transaction.GENESIS_SEND_ASSET_TRANSACTION;
	private static final String NAME_ID = "GENESIS Send Asset";
	private static final int RECIPIENT_LENGTH = TransactionAmount.RECIPIENT_LENGTH;
	private static final int OWNER_LENGTH = RECIPIENT_LENGTH;
	private static final int AMOUNT_LENGTH = TransactionAmount.AMOUNT_LENGTH;
	private static final int BASE_LENGTH = Genesis_Record.BASE_LENGTH + RECIPIENT_LENGTH + KEY_LENGTH + AMOUNT_LENGTH;

	private Account owner;
	private Account recipient;
	private BigDecimal amount;
	private long key;
	
	public GenesisTransferAssetTransaction(Account recipient, long key, BigDecimal amount) 
	{
		super(TYPE_ID, NAME_ID);
		this.recipient = recipient;
		this.amount = amount;
		this.key = key;
		if (key >= 0)
			this.generateSignature();
	}
	// RENT
	public GenesisTransferAssetTransaction(Account recipient, long key, BigDecimal amount, Account owner) 
	{
		this(recipient, key, amount);
		this.owner = owner;
		this.generateSignature();
	}
	
	//GETTERS/SETTERS
	//public static String getName() { return NAME; }

	public Account getOwner()
	{
		return this.owner;
	}
	public Account getRecipient()
	{
		return this.recipient;
	}
	
	public BigDecimal getAmount() 
	{
		return this.amount;
	}
	
	public long getKey()
	{
		return this.key;
	}
	public long getAssetKey()
	{
		return this.key;
	}
	
	@Override
	public BigDecimal getAmount(String address) {
		BigDecimal amount = BigDecimal.ZERO.setScale(8);
		
		if(address.equals(this.recipient.getAddress()))
		{
			//IF RECIPIENT
			amount = amount.add(this.amount);
		}

		return amount;
	}
	@Override
	public BigDecimal getAmount(Account account) {
		String address = account.getAddress();
		return getAmount(address);
	}

	@Override
	public String viewAmount(Account account) {
		String address = account.getAddress();
		return NumberAsString.getInstance().numberAsString(getAmount(address));
	}
	@Override
	public String viewAmount(String address) {
		return NumberAsString.getInstance().numberAsString(getAmount(address));
	}

	@Override
	public String viewRecipient() {
		return recipient.getPersonAsString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = super.toJson();
				
		//ADD CREATOR/RECIPIENT/AMOUNT/ASSET
		if (this.owner != null)
			transaction.put("owner", this.owner.getAddress());
		
		transaction.put("recipient", this.recipient.getAddress());
		transaction.put("asset", this.key);
		transaction.put("amount", this.amount.toPlainString());
				
		return transaction;	
	}

	//PARSE/CONVERT
	
	public static Transaction Parse(byte[] data) throws Exception{
		
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length: " + data.length + " in " + NAME_ID);
		}
		
		// READ TYPE
		//byte[] typeBytes = Arrays.copyOfRange(data, 0, SIMPLE_TYPE_LENGTH);
		int position = SIMPLE_TYPE_LENGTH;
		
		//READ RECIPIENT
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;
		
		//READ KEY
		byte[] keyBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
		long key = Longs.fromByteArray(keyBytes);	
		position += KEY_LENGTH;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;

		if (key < 0) {
			//READ OWNER
			byte[] ownerBytes = Arrays.copyOfRange(data, position, position + OWNER_LENGTH);
			Account owner = new Account(Base58.encode(ownerBytes));
			position += OWNER_LENGTH;
			return new GenesisTransferAssetTransaction(recipient, key, amount, owner);	
		} else {
			return new GenesisTransferAssetTransaction(recipient, key, amount);
		}

	}	
	
	@Override
	public byte[] toBytes(boolean withSign, Long releaserReference)
	{
		//byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] data = new byte[]{TYPE_ID};
		
		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));
		
		//WRITE KEY
		byte[] keyBytes = Longs.toByteArray(this.key);
		keyBytes = Bytes.ensureCapacity(keyBytes, KEY_LENGTH, 0);
		data = Bytes.concat(data, keyBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);

		if (key < 0) {
			//WRITE OWNER
			data = Bytes.concat(data, Base58.decode(this.owner.getAddress()));
		}

		return data;
	}

	@Override
	public int getDataLength(boolean asPack) 
	{
		return BASE_LENGTH + (this.key<0?OWNER_LENGTH:0);
	}


	//VALIDATE

	@Override
	public int isValid(DBSet db, Long releaserReference) 
	{
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			return INVALID_ADDRESS;
		}
						
		//CHECK IF AMOUNT IS DIVISIBLE
		// genesis assets not in DB yet and need take it from genesis maker
		if(!GenesisBlock.makeAsset((int)this.key).isDivisible())
		{
			//CHECK IF AMOUNT DOES NOT HAVE ANY DECIMALS
			if(this.getAmount().stripTrailingZeros().scale() > 0)
			{
				//AMOUNT HAS DECIMALS
				return INVALID_AMOUNT;
			}
		}
				
		//CHECK IF AMOUNT IS POSITIVE
		if(this.amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_AMOUNT;
		}
				
		return VALIDATE_OK;
	}

	//PROCESS/ORPHAN
	
	@Override
	public void process(DBSet db, Block block, boolean asPack) 
	{

		long key = this.key;

		//UPDATE RECIPIENT OWN or RENT
		this.recipient.changeBalance(db, false, key, this.amount);
		
		//UPDATE REFERENCE OF RECIPIENT
		this.recipient.setLastReference(this.timestamp, db);

		if (this.getAbsKey() == Transaction.RIGHTS_KEY) {
			// PROCESS FORGING DATA
			//// SKIP Genesis Block
			this.recipient.setForgingData(db, 1);
		}

		if (key < 0) {
			// THIS is CREDIT
			//this.owner.setBalance(key, this.owner.getBalance(db, key).subtract(this.amount), db);
			this.owner.changeBalance(db, true, key, this.amount);
			db.getCredit_AddressesMap().add(
					new Tuple3<String, Long, String>(this.owner.getAddress(), -key, this.recipient.getAddress()),
					this.amount);
		} else {
			// CREATOR update
			GenesisBlock.CREATOR.changeBalance(db, true, key, this.amount);			
		}
	}

	@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		
		/* IT CANNOT BE orphanED !!!
		 * 
		long key = this.key;
		//UPDATE RECIPIENT
		//this.recipient.setBalance(key, this.recipient.getBalance(db, key).subtract(this.amount), db);
		this.recipient.changeBalance(db, true, key, this.amount);
		
		//UPDATE REFERENCE OF RECIPIENT
		this.recipient.removeReference(db);

		if (this.getAbsKey() == Transaction.RIGHTS_KEY) {
			// ORPHAN FORGING DATA
			////this.recipient.setLastForgingData(db, -1);
			//this.recipient.delForgingData(db, 2);
		}

		if (key < 0) {
			//this.owner.setBalance(key, this.owner.getBalance(db, key).add(this.amount), db);
			this.owner.changeBalance(db, false, key, this.amount);
			db.getCredit_AddressesMap().sub(
					new Tuple3<String, Long, String>(this.owner.getAddress(), -key, this.recipient.getAddress()),
					this.amount);
		} else {
			// CREATOR update
			GenesisBlock.CREATOR.changeBalance(db, false, key, this.amount);			
		}
		*/

	}

	//REST
	

	@Override
	public HashSet<Account> getRecipientAccounts()
	{
		HashSet<Account> accounts = new HashSet<Account>();
		accounts.add(this.recipient);
		return accounts;
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(recipient.getAddress())
				|| owner != null && address.equals(owner.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
				
		assetAmount = addAssetAmount(assetAmount, this.recipient.getAddress(), this.key, this.amount);
		
		return assetAmount;
	}
}
