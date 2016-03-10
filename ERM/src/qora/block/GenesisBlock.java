package qora.block;

import java.util.logging.Logger;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.assets.Asset;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.transaction.GenesisTransaction;
import qora.transaction.GenesisIssueAssetTransaction;
import qora.transaction.GenesisTransferAssetTransaction;
import qora.transaction.Transaction;
import settings.Settings;
import utils.Pair;

public class GenesisBlock extends Block{
	
	private static int genesisVersion = 1;
	private static byte[] genesisReference =  new byte[]{1,1,1,1,1,1,1,1};
	private static long genesisGeneratingBalance = 10000000L;
	private static PublicKeyAccount genesisGenerator = new PublicKeyAccount(new byte[]{1,3,1,3,1,3,1,3});

	private String testnetInfo; 
	
	public GenesisBlock()
	{
		//SET HEADER
		super(genesisVersion, genesisReference, Settings.getInstance().getGenesisStamp(), genesisGeneratingBalance, genesisGenerator, generateHash(), null, 0);
		
		long genesisTimestamp = Settings.getInstance().getGenesisStamp();
		
		if(genesisTimestamp != Settings.DEFAULT_MAINNET_STAMP) {
			this.testnetInfo = ""; 
			
			//ADD TESTNET GENESIS TRANSACTIONS
			this.testnetInfo += "\ngenesisStamp: " + String.valueOf(genesisTimestamp);	

			byte[] seed = Crypto.getInstance().digest(Longs.toByteArray(genesisTimestamp));

			this.testnetInfo += "\ngenesisSeed: " + Base58.encode(seed);
			
			for(int nonce=0; nonce<10; nonce++)
		    {
				byte[] accountSeed = generateAccountSeed(seed, nonce);
				
				Pair<byte[], byte[]> keyPair = Crypto.getInstance().createKeyPair(accountSeed);
				byte[] publicKey = keyPair.getB();
				String address = Crypto.getInstance().getAddress(publicKey);

				this.addTransaction(new GenesisTransaction(new Account(address), new BigDecimal(10000000000L/10).setScale(8), genesisTimestamp));
				
				this.testnetInfo += "\ngenesisAccount(" + String.valueOf(nonce) + "): " + address +  " / POST addresses " + Base58.encode(accountSeed);
		    }
			this.testnetInfo += "\nStart the other nodes with command:";
			this.testnetInfo += "\njava -Xms512m -Xmx1024m -jar Qora.jar -testnet=" + genesisTimestamp;

			
			//GENERATE AND VALIDATE TRANSACTIONSSIGNATURE
			this.setTransactionsSignature(generateHash());
		} else {
			Account recipient;
			BigDecimal bdAmount = new BigDecimal("1111111111.").setScale(8);
			List<String> recipients = Arrays.asList(					
					"QStUHLofuyCBy3UR2Rr8WRNnPc56WZYzWu","QRqBjBJshFJig97ABKiPJ9ar86KbWEZ7Hc","QYgYu43QEMv2cf1QC8nq5PwVRQrNVk81MM",
					"Qj1vEeuz7iJADzV2qrxguSFGzamZiYZVUP","QiZSovPpdyAhLW66P2KkF5UynR9RtVsLPN","QYMA8MopsHnWx4B28zUFArAsCmZoPx3ooG",
					"QXuzwBv17fmDQD3y5Emhu7qiFoRYCDE8jS","QVcP2HUjxrGrb6ARWmu6h6x1fCTxatFw2H","QLdMWd4QAhLuAtq3G1WCrHd6WTJ7GV4jdk");
			//ADD MAINNET GENESIS TRANSACTIONS
			for(String address: recipients)
			{
				recipient = new Account(address);
				this.addTransaction(new GenesisTransaction(recipient, bdAmount, genesisTimestamp));
			}
						
			Asset asset1;
			byte[] signature;
			//CREATE JOB ASSET
			asset1 = makeOil(new byte[64]);
			signature = GenesisIssueAssetTransaction.generateSignature(asset1, genesisTimestamp);
			asset1 = makeOil(signature);
			//Logger.getGlobal().info("genesisGenerator " + genesisGenerator.getAddress());

			this.addTransaction(new GenesisIssueAssetTransaction(genesisGenerator, asset1, genesisTimestamp));
			
			//CREATE VOTE ASSET
			Asset asset2;
			asset2 = makeGem(new byte[64]);
			signature = GenesisIssueAssetTransaction.generateSignature(asset2, genesisTimestamp);
			asset2 = makeGem(signature);
			this.addTransaction(new GenesisIssueAssetTransaction(genesisGenerator, asset2, genesisTimestamp));
			
			//Logger.getGlobal().info("amount " + new BigDecimal(asset1.getQuantity()).multiply(new BigDecimal(11)));
			for(String address: recipients)
			{
				recipient = new Account(address);
				bdAmount = new BigDecimal(asset1.getQuantity()).divide(new BigDecimal(9));
				this.addTransaction(new GenesisTransferAssetTransaction(genesisGenerator, recipient, 1l, bdAmount, genesisTimestamp));
				
				bdAmount = new BigDecimal(asset2.getQuantity()).divide(new BigDecimal(9));
				this.addTransaction(new GenesisTransferAssetTransaction(genesisGenerator, recipient, 2l, bdAmount, genesisTimestamp));
			}

			//GENERATE AND VALIDATE TRANSACTIONSSIGNATURE
			this.setTransactionsSignature(generateHash());
		}
	}

	// make assets
	public Asset makeERM(byte[] reference) 
	{
		Asset asset = new Asset(genesisGenerator, "ERM", "Main unit", 10000000000L, (byte) 6, true, reference);
		return asset;
	}
	public Asset makeOil(byte[] reference) 
	{
		Asset asset = new Asset(genesisGenerator, "oil", "Fees oil", 99999999L, (byte) 8, true, reference);
		return asset;
	}
	public Asset makeGem(byte[] reference) 
	{
		Asset asset = new Asset(genesisGenerator, "GEM", "Votes gem", 999999999999999999L, (byte) 0, false, reference);
		return asset;
	}

	public String getTestNetInfo() 
	{
		return this.testnetInfo;
	}
	
	//GETTERS
	
	@Override
	public Block getParent()
	{
		//PARENT DOES NOT EXIST
		return null;
	}
	
	//SIGNATURE

	public static byte[] generateHash()
	{
		byte[] data = new byte[0];
		
		//WRITE VERSION
		byte[] versionBytes = Longs.toByteArray(genesisVersion);
		versionBytes = Bytes.ensureCapacity(versionBytes, 4, 0);
		data = Bytes.concat(data, versionBytes);
		
		//WRITE REFERENCE
		byte[] referenceBytes = Bytes.ensureCapacity(genesisReference, 64, 0);
		data = Bytes.concat(data, referenceBytes);
		
		//WRITE GENERATING BALANCE
		byte[] generatingBalanceBytes = Longs.toByteArray(genesisGeneratingBalance);
		generatingBalanceBytes = Bytes.ensureCapacity(generatingBalanceBytes, 8, 0);
		data = Bytes.concat(data, generatingBalanceBytes);
		
		//WRITE GENERATOR
		byte[] generatorBytes = Bytes.ensureCapacity(genesisGenerator.getPublicKey(), 32, 0);
		data = Bytes.concat(data, generatorBytes);
		
		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);		
		digest = Bytes.concat(digest, digest);
		
		return digest;
	}
	
	//VALIDATE
	
	@Override
	public boolean isSignatureValid()
	{
		byte[] data = new byte[0];
		
		//WRITE VERSION
		byte[] versionBytes = Longs.toByteArray(genesisVersion);
		versionBytes = Bytes.ensureCapacity(versionBytes, 4, 0);
		data = Bytes.concat(data, versionBytes);
				
		//WRITE REFERENCE
		byte[] referenceBytes = Bytes.ensureCapacity(genesisReference, 64, 0);
		data = Bytes.concat(data, referenceBytes);
				
		//WRITE GENERATING BALANCE
		byte[] generatingBalanceBytes = Longs.toByteArray(genesisGeneratingBalance);
		generatingBalanceBytes = Bytes.ensureCapacity(generatingBalanceBytes, 8, 0);
		data = Bytes.concat(data, generatingBalanceBytes);
				
		//WRITE GENERATOR
		byte[] generatorBytes = Bytes.ensureCapacity(genesisGenerator.getPublicKey(), 32, 0);
		data = Bytes.concat(data, generatorBytes);
				
		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);		
		digest = Bytes.concat(digest, digest);
						
		//VALIDATE BLOCK SIGNATURE
		if(!Arrays.equals(digest, this.generatorSignature))
		{
			return false;
		}
		
		//VALIDATE TRANSACTIONS SIGNATURE
		if(!Arrays.equals(digest, this.transactionsSignature))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isValid(DBSet db)
	{
		//CHECK IF NO OTHER BLOCK IN DB
		if(db.getBlockMap().getLastBlock() != null)
		{
			return false;
		}
		
		//VALIDATE TRANSACTIONS
		for(Transaction transaction: this.getTransactions())
		{
			if(transaction.isValid(db) != Transaction.VALIDATE_OK)
			{
				return false;
			}
		}
		
		return true;
	}
	
	private static byte[] generateAccountSeed(byte[] seed, int nonce) 
	{		
		byte[] nonceBytes = Ints.toByteArray(nonce);
		byte[] accountSeed = Bytes.concat(nonceBytes, seed, nonceBytes);
		return Crypto.getInstance().doubleDigest(accountSeed);		
	}	
}
