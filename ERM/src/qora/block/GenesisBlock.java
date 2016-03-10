package qora.block;

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
import qora.payment.Payment;
import qora.transaction.GenesisTransaction;
//import qora.transaction.IssueAssetTransaction;
import qora.transaction.GenesisIssueAssetTransaction;
import qora.transaction.Transaction;
import settings.Settings;
import utils.Pair;

public class GenesisBlock extends Block{
	
	private static int genesisVersion = 1;
	private static byte[] genesisReference =  new byte[]{1,1,1,1,1,1,1,1};
	private static long genesisGeneratingBalance = 10000000L;
	private static PublicKeyAccount genesisGenerator = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});

	private String testnetInfo; 
	
	public GenesisBlock()
	{
		//SET HEADER
		super(genesisVersion, genesisReference, Settings.getInstance().getGenesisStamp() , genesisGeneratingBalance, genesisGenerator, generateHash());
		
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
				this.addTransaction(new GenesisTransaction(recipient, bdAmount , genesisTimestamp));
			}
			
			///////////
			byte[] seed = Crypto.getInstance().digest("Generic Assets".getBytes());
			byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
			PrivateKeyAccount issuer = new PrivateKeyAccount(privateKey);

			Asset asset;
			byte[] signature;

			//CREATE JOB ASSET
			asset = makeOil(new byte[64]);
			signature = asset.generateReference();
			asset = makeOil(signature);
			this.addTransaction(new GenesisIssueAssetTransaction(issuer, asset, genesisTimestamp));
			
			//CREATE VOTE ASSET
			asset = makeGem(new byte[64]);
			signature = asset.generateReference();
			asset = makeGem(signature);
			this.addTransaction(new GenesisIssueAssetTransaction(issuer, asset, genesisTimestamp));

			/*
			for(String address: recipients)
			{
				recipient = new Account(address);
				this.addTransaction(new GenesisIssueAssetTransaction(recipient, bdAmount , genesisTimestamp));
			}
			*/

			//GENERATE AND VALIDATE TRANSACTIONSSIGNATURE
			this.setTransactionsSignature(generateHash());
		}
	}

	// make assets
	public Asset makeERM(byte[] signature) 
	{
		return new Asset(genesisGenerator, "ERM", "Main unit", 10000000000L, (byte) 6, true, signature);
	}
	public Asset makeOil(byte[] signature) 
	{
		return new Asset(genesisGenerator, "oil", "Fee oil", 99999999L, (byte) 8, true, signature);
	}
	public Asset makeGem(byte[] signature) 
	{
		return new Asset(genesisGenerator, "GEM", "Vote gem", 999999999999999999L, (byte) 0, false, signature);
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
