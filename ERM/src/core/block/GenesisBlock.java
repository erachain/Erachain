package core.block;

// import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.item.notes.Note;
import core.item.notes.NoteCls;
import core.item.statuses.Status;
import core.item.statuses.StatusCls;
import core.transaction.Transaction;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.GenesisIssueNoteTransaction;
import core.transaction.GenesisIssueStatusTransaction;
import core.transaction.GenesisTransaction;
import core.transaction.GenesisTransferAssetTransaction;
import core.transaction.GenesisTransferStatusTransaction;
//import core.transaction.Transaction;
import database.DBSet;
import settings.Settings;
import utils.Pair;

public class GenesisBlock extends Block{
	
	private static int genesisVersion = 1;
	private static byte[] genesisReference =  new byte[]{19,66,8,21,0,0,0,0};
	private static long genesisGeneratingBalance = 12000000L; // starting max volume for generating	
	private static PublicKeyAccount genesisGenerator = new PublicKeyAccount(new byte[]{0,1,2,3,4,13,31,13,31,13,31});

	private String testnetInfo; 
	
	public GenesisBlock()
	{
		//SET HEADER
		super(genesisVersion, genesisReference, Settings.getInstance().getGenesisStamp(), genesisGeneratingBalance / 5, genesisGenerator, generateHash(null), null, 0);
		
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
			this.testnetInfo += "\nStart the other nodes with command" + ":";
			this.testnetInfo += "\njava -Xms512m -Xmx1024m -jar ERM.jar -testnet=" + genesisTimestamp;

			
			//GENERATE AND VALIDATE TRANSACTIONSSIGNATURE
			this.setTransactionsSignature(generateHash(this.getTransactions()));
		} else {
			Account recipient;
			Long timestamp = genesisTimestamp;
			BigDecimal bdAmount;
			List<String> recipients = Arrays.asList(					
					"7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ","7EpDngzSLXrqnRBJ5x9YKTU395VEpsz5Mz",//"7Dwjk4TUB74CqW6PqfDQF1siXquK48HSPB",
					"7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC","77QnJnSbS9EeGBa2LPZFZKVwjPwzeAxjmy",//"77QnJnSbS9EeGBa2LPZFZKVwjPwzeAxjmy",
					"78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5","7AfGz1FJ6tUnxxKSAHfcjroFEm8jSyVm7r"//,"7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7"
					);

			List<String> minoreRecipients = Arrays.asList(					
					"7FUUEjDSo9J4CYon4tsokMCPmfP4YggPnd","78T3Eof2c4EyhuHc3qCunJ3Wk3TCYyQTnb","7NavQiMdL4nSsDMppnVoc6gtoCnREZXrEC",
					"753BpHWMyKxKVjsSUiceBab1mydcVEoKDD","7QGXujqsuJeb9YeW5L83vaEu3SsWXsRtXc","7RLwEuNLN6tJaksaKH1CuBmDGPnmLNDwri",
					"73CcZe3PhwvqMvWxDznLAzZBrkeTZHvNzo");
			
			PublicKeyAccount issuer = new PublicKeyAccount(new byte[32]);

			// NOTES
			//CREATE MY NOTE
			this.addTransaction(new GenesisIssueNoteTransaction(issuer, makeAssetNote(0), timestamp++));
			//CREATE PERSONALIZE NOTE
			this.addTransaction(new GenesisIssueNoteTransaction(issuer, makeAssetNote(1), timestamp++));
			//CREATE ESTABLISH NOTE
			this.addTransaction(new GenesisIssueNoteTransaction(issuer, makeAssetNote(2), timestamp++));

			// STATUSES
			// ALIVE
			this.addTransaction(new GenesisIssueStatusTransaction(issuer, makeStatus(0), timestamp++));
			// DEAD
			this.addTransaction(new GenesisIssueStatusTransaction(issuer, makeStatus(1), timestamp++));
			// CITIZEN
			this.addTransaction(new GenesisIssueStatusTransaction(issuer, makeStatus(2), timestamp++));
			// MEMBER
			this.addTransaction(new GenesisIssueStatusTransaction(issuer, makeStatus(3), timestamp++));
			
			AssetVenture asset0;
			//CREATE ERM ASSET
			asset0 = makeAssetVenture(Transaction.RIGHTS_KEY);
			this.addTransaction(new GenesisIssueAssetTransaction(issuer, asset0, timestamp++));
			//CREATE JOB ASSET
			AssetVenture asset1 = makeAssetVenture(Transaction.FEE_KEY);
			this.addTransaction(new GenesisIssueAssetTransaction(issuer, asset1, timestamp++));
			
			// TRANSFERS
			float koeff = (float)0.10;
			for(String address: recipients)
			{
				recipient = new Account(address);
				
				bdAmount = new BigDecimal(asset0.getQuantity() * koeff).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(issuer, recipient, 0l, bdAmount, timestamp++));

				bdAmount = new BigDecimal(asset1.getQuantity() * koeff).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(issuer, recipient, 1l, bdAmount, timestamp++));
				
				// STATUS ALIVE
				this.addTransaction(new GenesisTransferStatusTransaction(issuer, recipient, 0l, timestamp++));
				
			}
			koeff = (float)0.00001;
			for(String address: minoreRecipients)
			{
				recipient = new Account(address);
				
				bdAmount = new BigDecimal(asset0.getQuantity() * koeff).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(issuer, recipient, 0l, bdAmount, timestamp++));

				bdAmount = new BigDecimal(asset1.getQuantity() * koeff).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(issuer, recipient, 1l, bdAmount, timestamp++));
				
				// STATUS ALIVE
				this.addTransaction(new GenesisTransferStatusTransaction(issuer, recipient, 0l, timestamp++));

			}
			
			//GENERATE AND VALIDATE TRANSACTIONSSIGNATURE
			this.setTransactionsSignature(generateHash(this.getTransactions()));
		}
	}

	// make assets
	public static AssetVenture makeAssetVenture(long key) 
	{
		switch((int)key)
		{
		case (int)Transaction.FEE_KEY:
			return new AssetVenture(genesisGenerator, "LIEV", "It is an drops of life used for deals", 99999999L, (byte)8, true);
		}
		return new AssetVenture(genesisGenerator, "ERMO", "It is the basic unit of Environment Real Management Objects", genesisGeneratingBalance, (byte)0, true);
	}
	// make notes
	public static Note makeAssetNote(int key) 
	{
		switch(key)
		{
		case (int)NoteCls.PERSONALIZE_KEY:
			return new Note(genesisGenerator, "Introduce Myself", "I, %First Name% %Middle Name% %Last Name%, date of birth \"%date of Birth%\", place of birth \"%Place of Birth%\", race \"%Race%\", height \"%height%\", color \"%Color%\", eye color \"Eye Color\", hair color \"%Hair Color%\", I confirm that I have single-handedly account \"\" and I beg to acknowledge the data signed by this account as my own's handmade signature.");
		case (int)NoteCls.ESTABLISH_UNION_KEY:
			return new Note(genesisGenerator, "Establish the Company", "Company name \"%Company Name%\" in country \"%Country%\"");
		}
		return new Note(genesisGenerator, "Introduce Me", "I, Dmitry Ermolaev, date of birth \"1966.08.21\", place of birth \"Vladivostok, Primorsky Krai, Russia\", race \"Slav\", height \"188\", eye color \"light grey\", color \"white\", hair color \"dark brown\", I confirm that I have single-handedly account \"\" and I beg to acknowledge the data signed by this account as my own's handmade signature.");
	}
	// make notes
	public static Status makeStatus(int key) 
	{
		switch(key)
		{
		case (int)StatusCls.DEAD_KEY:
			return new Status(genesisGenerator, "dead", "Person is dead");
		case (int)StatusCls.CITIZEN_KEY:
			return new Status(genesisGenerator, "Сitizen", "I am citizen of %country%");
		case (int)StatusCls.MEMBER_KEY:
			return new Status(genesisGenerator, "Member", "I am member of %union%");
		}
		return new Status(genesisGenerator, "Alive", "I am alive.");
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
	
	public static byte[] generateHash(List<Transaction> transactions)
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
		
		if ( transactions != null )
		{
			//WRITE TRANSACTION SIGNATURE
			for(Transaction transaction: transactions)
			{
				data = Bytes.concat(data, transaction.getSignature());
			}
		}
		
		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);		
		digest = Bytes.concat(digest, digest);
		
		return digest;
	}
	
	//VALIDATE
	
	@Override
	public boolean isSignatureValid()
	{
		byte[] digest = generateHash(this.getTransactions());
						
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
	public BigDecimal getTotalFee()
	{
		return BigDecimal.ZERO.setScale(8);
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
			if(transaction.isValid(db, null) != Transaction.VALIDATE_OK)
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
