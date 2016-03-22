package qora.transaction;

//import java.util.logging.Logger;
import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.math.MathContext;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//import javax.swing.JFrame;
//import javax.swing.JOptionPane;

import org.json.simple.JSONObject;

import controller.Controller;
import database.DBSet;
//import lang.Lang;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.block.Block;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import settings.Settings;

public abstract class Transaction {
	
	//VALIDATION CODE
	public static final int VALIDATE_OK = 1;
	public static final int INVALID_ADDRESS = 2;
	public static final int NEGATIVE_AMOUNT = 3;
	//public static final int NEGATIVE_FEE = 4;
	public static final int NOT_ENOUGH_FEE = 4;
	public static final int NO_BALANCE = 5;
	public static final int INVALID_REFERENCE = 6;
	
	public static final int INVALID_NAME_LENGTH = 7;
	public static final int INVALID_VALUE_LENGTH = 8;
	public static final int NAME_ALREADY_REGISTRED = 9;
	
	public static final int NAME_DOES_NOT_EXIST = 10;
	public static final int INVALID_NAME_CREATOR = 11;
	public static final int NAME_ALREADY_FOR_SALE = 12;
	public static final int NAME_NOT_FOR_SALE = 13;
	public static final int BUYER_ALREADY_OWNER = 14;
	public static final int INVALID_AMOUNT = 15;
	public static final int INVALID_SELLER = 16;
	
	public static final int NAME_NOT_LOWER_CASE = 17;
	
	public static final int INVALID_DESCRIPTION_LENGTH = 18;
	public static final int INVALID_OPTIONS_LENGTH = 19;
	public static final int INVALID_OPTION_LENGTH = 20;
	public static final int DUPLICATE_OPTION = 21;
	public static final int POLL_ALREADY_CREATED = 22;
	public static final int POLL_ALREADY_HAS_VOTES = 23;
	public static final int POLL_NO_EXISTS = 24;
	public static final int OPTION_NO_EXISTS = 25;
	public static final int ALREADY_VOTED_FOR_THAT_OPTION = 26;
	public static final int INVALID_DATA_LENGTH = 27;
	
	public static final int INVALID_QUANTITY = 28;
	public static final int ASSET_DOES_NOT_EXIST = 29;
	public static final int INVALID_RETURN = 30;
	public static final int HAVE_EQUALS_WANT = 31;
	public static final int ORDER_DOES_NOT_EXIST = 32;
	public static final int INVALID_ORDER_CREATOR = 33;
	public static final int INVALID_PAYMENTS_LENGTH = 34;
	public static final int NEGATIVE_PRICE = 35;
	public static final int INVALID_CREATION_BYTES = 36;
	public static final int AT_ERROR = 10000;
	public static final int INVALID_TAGS_LENGTH = 37;
	public static final int INVALID_TYPE_LENGTH = 38;
	
	public static final int FEE_LESS_REQUIRED = 40;
	
	public static final int INVALID_RAW_DATA = 41;
	
	public static final int NOT_YET_RELEASED = 1000;
	
	//TYPES *******
	public static final int EXTENDED = 0;
	public static final int GENESIS_TRANSACTION = 1;
	public static final int GENESIS_ISSUE_ASSET_TRANSACTION = 2;
	public static final int GENESIS_TRANSFER_ASSET_TRANSACTION = 3;
	public static final int REERVED_4 = 4;

	public static final int PAYMENT_TRANSACTION = 5;
	
	public static final int REGISTER_NAME_TRANSACTION = 6;
	public static final int UPDATE_NAME_TRANSACTION = 7;
	public static final int SELL_NAME_TRANSACTION = 8;
	public static final int CANCEL_SELL_NAME_TRANSACTION = 9;
	public static final int BUY_NAME_TRANSACTION = 10;
	
	public static final int CREATE_POLL_TRANSACTION =11;
	public static final int VOTE_ON_POLL_TRANSACTION = 12;
	public static final int STATEMENT_RECORD = 13;
	public static final int REERVED_14 = 14;
	
	public static final int ARBITRARY_TRANSACTION = 15;
	public static final int REERVED_16 = 16;
	
	public static final int ISSUE_ASSET_TRANSACTION = 17;
	public static final int TRANSFER_ASSET_TRANSACTION = 18;
	public static final int CREATE_ORDER_TRANSACTION = 19;
	public static final int CANCEL_ORDER_TRANSACTION = 20;

	public static final int MULTI_PAYMENT_TRANSACTION = 21;
	public static final int REERVED_22 = 22;

	public static final int DEPLOY_AT_TRANSACTION = 23;
	public static final int REERVED_24 = 24;
	
	public static final int MESSAGE_TRANSACTION = 25;
	public static final int ACCOUNTING_TRANSACTION = 26;
	public static final int JSON_TRANSACTION = 27;
	
	// FEE KEY = OIL
	public static final long FEE_KEY = 1l;
	public static final int FEE_PER_BYTE = 1;
	public static final float FEE_POW_BASE = (float)1.5;
	public static final int FEE_POW_MAX = 6;
		
	//RELEASES
	private static final long VOTING_RELEASE = 0l;
	private static final long ARBITRARY_TRANSACTIONS_RELEASE = 0l;
	private static final int AT_BLOCK_HEIGHT_RELEASE = 0;
	private static final int MESSAGE_BLOCK_HEIGHT_RELEASE = 0;
	//public static final long ASSETS_RELEASE = 1411308000000l;
	private static final long ASSETS_RELEASE = 0l;
	private static final long POWFIX_RELEASE = 0L; // Block Version 3 // 2016-02-25T19:00:00+00:00
								
	//public static String[] TYPES = new String[256];
	//public String TYPES[0] = "w";

	public static long getVOTING_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return Settings.getInstance().getGenesisStamp();
		}
		return VOTING_RELEASE;
	}

	public static long getARBITRARY_TRANSACTIONS_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return Settings.getInstance().getGenesisStamp();
		}
		return ARBITRARY_TRANSACTIONS_RELEASE;
	}

	public static int getAT_BLOCK_HEIGHT_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return 1;
		}
		return AT_BLOCK_HEIGHT_RELEASE;
	}
	
	public static int getMESSAGE_BLOCK_HEIGHT_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return 1;
		}
		return MESSAGE_BLOCK_HEIGHT_RELEASE;
	}
	
	public static long getASSETS_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return Settings.getInstance().getGenesisStamp();
		}
		return ASSETS_RELEASE;
	}
	
	public static long getPOWFIX_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return Settings.getInstance().getGenesisStamp();
		}
		return POWFIX_RELEASE;
	}
	
	//PROPERTIES LENGTH
	protected static final int TYPE_LENGTH = 4;
	//protected static final int PROP_LENGTH = 2; // properties
	protected static final int TIMESTAMP_LENGTH = 8;
	protected static final int REFERENCE_LENGTH = 64;
	protected static final int DATA_SIZE_LENGTH = 4;
	protected static final int ENCRYPTED_LENGTH = 1;
	protected static final int IS_TEXT_LENGTH = 1;
	protected static final int KEY_LENGTH = 8;
	//protected static final int HKEY_LENGTH = 20;
	protected static final int CREATOR_LENGTH = 32;
	// not need now protected static final int FEE_LENGTH = 8;
	public static final int SIGNATURE_LENGTH = 64;
	protected static final int BASE_LENGTH = TYPE_LENGTH + REFERENCE_LENGTH + TIMESTAMP_LENGTH + CREATOR_LENGTH + SIGNATURE_LENGTH;
		
	protected byte[] reference;
	protected BigDecimal fee  = BigDecimal.ZERO.setScale(8); // - for genesis transactions
	protected byte feePow = 0;
	protected int type;
	protected byte[] signature;
	protected long timestamp;
	protected PublicKeyAccount creator;
	
	// need for genesis
	protected Transaction(int type, long timestamp)
	{
		this.type = type;
		this.timestamp = timestamp;

	}
	protected Transaction(int type, PublicKeyAccount creator, byte feePow, long timestamp, byte[] reference)
	{
		this.type = type;
		this.creator = creator;
		//this.props = props;
		this.timestamp = timestamp;
		this.reference = reference;
		if (feePow < 0 ) feePow = 0;
		else if (feePow > FEE_POW_MAX ) feePow = FEE_POW_MAX;
		this.feePow = feePow;
	}
	protected Transaction(int type, PublicKeyAccount creator, byte feePow, long timestamp, byte[] reference, byte[] signature)
	{
		this(type, creator, feePow, timestamp, reference);
		this.signature = signature;
	}

	//GETTERS/SETTERS
	
	public int getType()
	{
		return this.type;
	}
	
	public PublicKeyAccount getCreator() 
	{
		return this.creator;
	}

	public long getTimestamp()
	{
		return this.timestamp;
	}
	
	public long getDeadline()
	{
		//24HOUR DEADLINE TO INCLUDE TRANSACTION IN BLOCK
		return this.timestamp + (1000*60*60*24);
	}
	
	public BigDecimal viewAmount() {
		return BigDecimal.ZERO;
	}

	public BigDecimal viewAmount(Account account)
	{
		return BigDecimal.ZERO;
	}
	
	public BigDecimal getFee()
	{
		return this.fee;
	}
	public String getStr()
	{
		return "trans";
	}
	
	
	public byte[] getSignature()
	{
		return this.signature;
	}
	
	public byte[] getReference()
	{
		return this.reference;
	}			

	
	public int calcCommonFee()
	{		
		int fee =  this.getDataLength() + 100 * FEE_PER_BYTE;
		if (this.getDataLength() > 1000) {
			// add overheat
			fee += (this.getDataLength() - 1000) * FEE_PER_BYTE;
		}
		
		return (int) fee;
	}
	
	// get personal fee
	public abstract int calcBaseFee();
	
	// calc FEE by recommended and feePOW
	public void calcFee()
	{	
		
		BigDecimal fee = new BigDecimal(calcBaseFee())
				.multiply(new BigDecimal("0.00000001"))
				.setScale(8, BigDecimal.ROUND_UP);

		if (this.feePow > 0) {
			this.fee = fee.multiply(new BigDecimal(FEE_POW_BASE).pow((int)this.feePow))
					.setScale(8, BigDecimal.ROUND_UP);
		} else {
			this.fee = fee;
		}
	}
	
	public Block getParent() {
		
		return DBSet.getInstance().getTransactionParentMap().getParent(this.signature);
	}

	//PARSE/CONVERT
	
	@SuppressWarnings("unchecked")
	protected JSONObject getJsonBase()
	{
		JSONObject transaction = new JSONObject();
		
		transaction.put("type", this.type);
		transaction.put("fee", this.fee.toPlainString());
		transaction.put("timestamp", this.timestamp);
		transaction.put("reference", Base58.encode(this.reference));
		transaction.put("signature", Base58.encode(this.signature));
		transaction.put("confirmations", this.getConfirmations());
		if (this.creator != null ) transaction.put("creator", this.creator.getAddress());
		
		return transaction;
	}
	
	public void sign(PrivateKeyAccount creator)
	{
		byte[] data = this.toBytes( false );
		if ( data == null ) return;

		this.signature = Crypto.getInstance().sign(creator, data);
		this.calcFee(); // need for recal!
	}

	public abstract JSONObject toJson();
	
	public abstract byte[] toBytes(boolean withSign);
	
	public abstract int getDataLength();
	
	//VALIDATE
	
	public boolean isSignatureValid() {

		if ( this.signature == null | this.signature.length != 64 | this.signature == new byte[64]) return false;
		
		byte[] data = this.toBytes( false );
		if ( data == null ) return false;

		return Crypto.getInstance().verify(this.creator.getPublicKey(),
				this.signature, data);
	}
	

	public int isValid()
	{
		return isValid(DBSet.getInstance());
	}
	
	public abstract int isValid(DBSet db);
	
	//PROCESS/ORPHAN
	
	public void process()
	{
		this.process(DBSet.getInstance());
	}
	//public abstract void process(DBSet db);
	public void process(DBSet db)
	{
		
		this.calcFee();

		if (this.fee != null & this.fee.compareTo(BigDecimal.ZERO) > 0)
			this.creator.setConfirmedBalance(FEE_KEY, this.creator.getConfirmedBalance(FEE_KEY, db)
					.subtract(this.fee), db);

	}

	public void orphan()
	{
		this.orphan(DBSet.getInstance());
	}
	//public abstract void orphan(DBSet db);
	public void orphan(DBSet db)
	{
		if (this.fee != null & this.fee.compareTo(BigDecimal.ZERO) > 0)
			this.creator.setConfirmedBalance(FEE_KEY, this.creator.getConfirmedBalance(FEE_KEY, db).add(this.fee), db);

	}

	
	//REST
		
	public abstract List<Account> getInvolvedAccounts();
		
	public abstract boolean isInvolved(Account account);	
	
	public int getSeq()
	{
		if(this.isConfirmed())
		{
			return this.getParent().getTransactionSeq(this.signature);
		}
		return -1;
	}

	@Override 
	public boolean equals(Object object)
	{
		if(object instanceof Transaction)
		{
			Transaction transaction = (Transaction) object;
			
			return Arrays.equals(this.getSignature(), transaction.getSignature());
		}
		
		return false;
	}

	public boolean isConfirmed()
	{
		return this.isConfirmed(DBSet.getInstance());
	}
	
	public boolean isConfirmed(DBSet db)
	{
		return DBSet.getInstance().getTransactionParentMap().contains(this.getSignature());
	}
	
	public int getConfirmations()
	{
		
		try
		{
		//CHECK IF IN TRANSACTIONDATABASE
		if(DBSet.getInstance().getTransactionMap().contains(this))
		{
			return 0;
		}
		
		//CALCULATE CONFIRMATIONS
		int lastBlockHeight = DBSet.getInstance().getHeightMap().get(DBSet.getInstance().getBlockMap().getLastBlockSignature());
		int transactionBlockHeight = DBSet.getInstance().getHeightMap().get(DBSet.getInstance().getTransactionParentMap().getParent(this.signature));
		
		//RETURN
		return 1 + lastBlockHeight - transactionBlockHeight;

		}catch(Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	public int getBlockVersion1()
	{
		// IF ALREADY IN THE BLOCK. CONFIRMED 
		if(this.isConfirmed())
		{
			return DBSet.getInstance().getTransactionParentMap().getParent(this.getSignature()).getVersion();
		}
		
		// IF UNCONFIRMED
		return Controller.getInstance().getLastBlock().getNextBlockVersion(DBSet.getInstance());	
	}

	public static Map<String, Map<Long, BigDecimal>> subAssetAmount(Map<String, Map<Long, BigDecimal>> allAssetAmount, String address, Long assetKey, BigDecimal amount) 
	{
		return addAssetAmount(allAssetAmount, address, assetKey, BigDecimal.ZERO.setScale(8).subtract(amount));
	}
	
	public static Map<String, Map<Long, BigDecimal>> addAssetAmount(Map<String, Map<Long, BigDecimal>> allAssetAmount, String address, Long assetKey, BigDecimal amount) 
	{
		Map<String, Map<Long, BigDecimal>> newAllAssetAmount;
		if(allAssetAmount != null) {
			newAllAssetAmount = new LinkedHashMap<String, Map<Long, BigDecimal>>(allAssetAmount);
		} else {
			newAllAssetAmount = new LinkedHashMap<String, Map<Long, BigDecimal>>();
		}

		Map<Long, BigDecimal> newAssetAmountOfAddress;
		
		if(!newAllAssetAmount.containsKey(address)){
			newAssetAmountOfAddress = new LinkedHashMap<Long, BigDecimal>();
			newAssetAmountOfAddress.put(assetKey, amount);
			
			newAllAssetAmount.put(address, newAssetAmountOfAddress);
		} else {
			if(!newAllAssetAmount.get(address).containsKey(assetKey)) {
				newAssetAmountOfAddress = new LinkedHashMap<Long, BigDecimal>(newAllAssetAmount.get(address));
				newAssetAmountOfAddress.put(assetKey, amount);

				newAllAssetAmount.put(address, newAssetAmountOfAddress);
			} else {
				newAssetAmountOfAddress = new LinkedHashMap<Long, BigDecimal>(newAllAssetAmount.get(address));
				BigDecimal newAmount = newAllAssetAmount.get(address).get(assetKey).add(amount);
				newAssetAmountOfAddress.put(assetKey, newAmount);
				
				newAllAssetAmount.put(address, newAssetAmountOfAddress);
			}
		}
		
		return newAllAssetAmount;
	}

}
