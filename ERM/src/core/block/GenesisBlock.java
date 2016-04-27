package core.block;

// import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
//import java.sql.Timestamp;
//import org.mapdb.Fun.Tuple3;
//import org.mapdb.Fun.Tuple11;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
//import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.item.notes.NoteCls;
import core.item.notes.Note;
import core.item.persons.PersonCls;
import core.item.persons.PersonHuman;
import core.item.statuses.Status;
import core.item.statuses.StatusCls;
import core.transaction.Transaction;
import core.transaction.GenesisCertifyPersonRecord;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.GenesisIssueNoteTransaction;
import core.transaction.GenesisIssuePersonRecord;
import core.transaction.GenesisIssueStatusTransaction;
import core.transaction.GenesisTransferAssetTransaction;
import database.DBSet;
import settings.Settings;
import utils.Pair;

public class GenesisBlock extends Block{
	
	private static int genesisVersion = 1;
	private static byte[] genesisReference =  new byte[]{19,66,8,21,0,0,0,0};
	private static long genesisGeneratingBalance = 12000000L; // starting max volume for generating	
	private static PublicKeyAccount genesisGenerator = new PublicKeyAccount(new byte[]{0,1,2,3,4,13,31,13,31,13,31});

	private String testnetInfo; 
	
	AssetVenture asset0;
	AssetVenture asset1;

	public GenesisBlock()
	{
		//SET HEADER
		super(genesisVersion, genesisReference, Settings.getInstance().getGenesisStamp(), genesisGeneratingBalance / 5, genesisGenerator, generateHash(null), null, 0);
		
		long genesisTimestamp = Settings.getInstance().getGenesisStamp();
		Account recipient;
		Long timestamp = genesisTimestamp;
		BigDecimal bdAmount0;
		BigDecimal bdAmount1;
		//PublicKeyAccount issuer = new PublicKeyAccount(new byte[32]);
		PersonCls user;

		// ISSUE ITEMS
		this.initItems();
		
		if(genesisTimestamp != Settings.DEFAULT_MAINNET_STAMP) {
			this.testnetInfo = ""; 
			
			//ADD TESTNET GENESIS TRANSACTIONS
			this.testnetInfo += "\ngenesisStamp: " + String.valueOf(genesisTimestamp);	

			byte[] seed = Crypto.getInstance().digest(Longs.toByteArray(genesisTimestamp));

			this.testnetInfo += "\ngenesisSeed: " + Base58.encode(seed);
			
			bdAmount0 = new BigDecimal(asset0.getQuantity() * 0.1).setScale(8);
			bdAmount1 = new BigDecimal(asset1.getQuantity() * 0.1).setScale(8);
			for(int nonce=0; nonce<3; nonce++)
		    {
				byte[] accountSeed = generateAccountSeed(seed, nonce);
				
				Pair<byte[], byte[]> keyPair = Crypto.getInstance().createKeyPair(accountSeed);
				byte[] publicKey = keyPair.getB();
				String address = Crypto.getInstance().getAddress(publicKey);
				recipient = new Account(address);

				user = new PersonHuman(recipient,
						"UNRNOWN", "1966-08-21 10:10:10.0", (byte)1, "-", (float)0.1330, (float)1.9224,
						"-", "-", "-", (int) 188, "-");
				
				// SEND GENESIS ASSETS
				this.addTransaction(new GenesisTransferAssetTransaction(recipient, 0l, bdAmount0));
				this.addTransaction(new GenesisTransferAssetTransaction(recipient, 1l, bdAmount1));

				//CREATE ISSUE PERSON TRANSACTION
				this.addTransaction(new GenesisIssuePersonRecord(user));

				// CERTIFY PERSON
				this.addTransaction(new GenesisCertifyPersonRecord(recipient, nonce++));

				this.testnetInfo += "\ngenesisAccount(" + String.valueOf(nonce) + "): " + address +  " / POST addresses " + Base58.encode(accountSeed);
		    }
			this.testnetInfo += "\nStart the other nodes with command" + ":";
			this.testnetInfo += "\njava -Xms512m -Xmx1024m -jar ERM.jar -testnet=" + genesisTimestamp;

			
			//GENERATE AND VALIDATE TRANSACTIONSSIGNATURE
			this.setTransactionsSignature(generateHash(this.getTransactions()));
		} else {
			/////////// GENEGAL
			List<List<Object>> generalGenesisUsers = Arrays.asList(
					Arrays.asList(1, new PersonHuman(new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"),
							"Ермолаев Дмитрий Сергеевич", "1966-08-21 10:10:10.0", (byte)1, "Slav", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "школа: г.Уссурийск №6, институт: г.Владивосток ДВПИ")),
					Arrays.asList(1, new PersonHuman(new Account("7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC"),
							"Ермолаев Александр Сергеевич", "1966-08-21 10:10:10.0", (byte)1, "Slav", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "светло-коричневый", (int) 188, "школа: г.Уссурийск №6, институт: г.Владивосток ДВПИ")),
					Arrays.asList(1, new PersonHuman(new Account("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5"),
							"Скорняков Александр Викторович", "1963-08-21 10:10:10.0", (byte)1, "Slav", (float)1.1330, (float)13.9224,
							"белый", "серо-зеленый", "светло-коричневый", (int) 188, "-"))
				);
			/////////// MAJOR
			List<List<Object>> majorGenesisUsers = Arrays.asList(
					Arrays.asList(1000, new PersonHuman(new Account("7FoC1wAtbR9Z5iwtcw4Ju1u2DnLBQ1TNS7"),
							"Симанков Дмитрий", "1966-08-21 10:10:10.0", (byte)1, "Slav", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("76GJujhki7z2BeX1bnp4KL5Qp22NsakWeT"),
							"Бородин Олег", "1966-08-21 10:10:10.0", (byte)1, "Slav", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, ""))					
				);
			////////// MINOR
			List<List<Object>> minorGenesisUsers = Arrays.asList(
					Arrays.asList(100, new PersonHuman(new Account("7FUUEjDSo9J4CYon4tsokMCPmfP4YggPnd"),
							"неизвестный участник", "1966-08-21 10:10:10.0", (byte)1, "Slav", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(100, new PersonHuman(new Account("78T3Eof2c4EyhuHc3qCunJ3Wk3TCYyQTnb"),
							"неизвестный участник", "1966-08-21 10:10:10.0", (byte)1, "Slav", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(100, new PersonHuman(new Account("7NavQiMdL4nSsDMppnVoc6gtoCnREZXrEC"),
							"неизвестный участник", "1966-08-21 10:10:10.0", (byte)1, "Slav", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(100, new PersonHuman(new Account("753BpHWMyKxKVjsSUiceBab1mydcVEoKDD"),
							"неизвестный участник", "1966-08-21 10:10:10.0", (byte)1, "Slav", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "")),
					Arrays.asList(100, new PersonHuman(new Account("7QGXujqsuJeb9YeW5L83vaEu3SsWXsRtXc"),
							"неизвестный участник", "1966-08-21 10:10:10.0", (byte)1, "Slav", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(100, new PersonHuman(new Account("7RLwEuNLN6tJaksaKH1CuBmDGPnmLNDwri"),
							"неизвестный участник", "1966-08-21 10:10:10.0", (byte)1, "Slav", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(100, new PersonHuman(new Account("73CcZe3PhwvqMvWxDznLAzZBrkeTZHvNzo"),
							"неизвестный участник", "1966-08-21 10:10:10.0", (byte)1, "Slav", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-"))
					);

			// TRANSFERS

			// summ picks
			long generalPicked = 0;
			for(List<Object> item: generalGenesisUsers)
			{
				generalPicked += (int)item.get(0);
			}

			long majorPicked = 0;
			for(List<Object> item: majorGenesisUsers)
			{
				majorPicked += (int)item.get(0);
			}

			long minorPicked = 0;
			for(List<Object> item: minorGenesisUsers)
			{
				minorPicked += (int)item.get(0);
			}

			// 10% 1%
			float majorPick = (float)0.1;
			float minorPick = (float)0.001;
			double generalKoeff0 = (1.0 - majorPick - minorPick) * asset0.getQuantity() / generalPicked;
			double generalKoeff1 = asset1.getQuantity() / generalPicked;
			double majorKoeff = majorPick * asset0.getQuantity() / majorPicked;
			double minorKoeff = minorPick * asset0.getQuantity() / minorPicked;
			
			long i = 0;
			int pick;

			for(List<Object> item: generalGenesisUsers)
			{
				
				pick = (int)item.get(0);
				user = (PersonHuman)item.get(1);
				
				recipient = user.getCreator();
				
				bdAmount0 = new BigDecimal(Math.round(pick * generalKoeff0)).setScale(8);
				//bal0 = bal0.add(bdAmount0).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(recipient, 0l, bdAmount0));

				bdAmount1 = new BigDecimal(Math.round(pick * generalKoeff1)).setScale(8);
				//bal1 = bal1.add(bdAmount1).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(recipient, 1l, bdAmount1));

				//CREATE ISSUE PERSON TRANSACTION
				//this.addTransaction(new GenesisIssuePersonRecord(user));

				// CERTIFY PERSON
				//this.addTransaction(new GenesisCertifyPersonRecord(recipient, i++));

			}

			for(List<Object> item: majorGenesisUsers)
			{
				
				pick = (int)item.get(0);
				user = (PersonHuman)item.get(1);
				
				recipient = user.getCreator();
				
				bdAmount0 = new BigDecimal(Math.round(pick * majorKoeff)).setScale(8);
				//bal0 = bal0.add(bdAmount0).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(recipient, 0l, bdAmount0));

				bdAmount1 = new BigDecimal("0.001").setScale(8);
				//bal1 = bal1.add(bdAmount1).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(recipient, 1l, bdAmount1));

				//CREATE ISSUE PERSON TRANSACTION
				//this.addTransaction(new GenesisIssuePersonRecord(user));

				// CERTIFY PERSON
				//this.addTransaction(new GenesisCertifyPersonRecord(recipient, i++));

			}

			for(List<Object> item: minorGenesisUsers)
			{
				
				pick = (int)item.get(0);
				user = (PersonHuman)item.get(1);
				
				recipient = user.getCreator();
				
				bdAmount0 = new BigDecimal(Math.round(pick * minorKoeff)).setScale(8);
				//bal0 = bal0.add(bdAmount0).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(recipient, 0l, bdAmount0));

				bdAmount1 = new BigDecimal("0.0001").setScale(8);
				//bal1 = bal1.add(bdAmount1).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(recipient, 1l, bdAmount1));

				//CREATE ISSUE PERSON TRANSACTION
				///this.addTransaction(new GenesisIssuePersonRecord(user));

				// CERTIFY PERSON
				//this.addTransaction(new GenesisCertifyPersonRecord(recipient, i++));

			}
			
			//GENERATE AND VALIDATE TRANSACTIONSSIGNATURE
			this.setTransactionsSignature(generateHash(this.getTransactions()));
		}
	}
	
	private void initItems()
	{
		
		//CREATE ERM ASSET
		asset0 = makeAssetVenture(Transaction.RIGHTS_KEY);
		this.addTransaction(new GenesisIssueAssetTransaction(asset0));
		//CREATE JOB ASSET
		asset1 = makeAssetVenture(Transaction.FEE_KEY);
		this.addTransaction(new GenesisIssueAssetTransaction(asset1));

		// NOTES
		//CREATE MY NOTE
		this.addTransaction(new GenesisIssueNoteTransaction(makeAssetNote(0)));
		//CREATE PERSONALIZE NOTE
		this.addTransaction(new GenesisIssueNoteTransaction(makeAssetNote(1)));
		//CREATE ESTABLISH NOTE
		this.addTransaction(new GenesisIssueNoteTransaction(makeAssetNote(2)));

		// STATUSES
		// ALIVE
		this.addTransaction(new GenesisIssueStatusTransaction(makeStatus(0)));
		// DEAD
		this.addTransaction(new GenesisIssueStatusTransaction(makeStatus(1)));
		// CITIZEN
		this.addTransaction(new GenesisIssueStatusTransaction(makeStatus(2)));
		// MEMBER
		this.addTransaction(new GenesisIssueStatusTransaction(makeStatus(3)));
		
	}
	
	// make assets
	public static AssetVenture makeAssetVenture(long key) 
	{
		switch((int)key)
		{
		case (int)Transaction.FEE_KEY:
			return new AssetVenture(genesisGenerator, "LAEV", "It is an drops of life used for deals", 99999999L, (byte)8, true);
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
		if (key == StatusCls.DEAD_KEY) return new Status(genesisGenerator, "dead", "Person is dead");
		else if (key == StatusCls.CITIZEN_KEY) return new Status(genesisGenerator, "Сitizen", "I am citizen of %country%");
		else if (key == StatusCls.MEMBER_KEY) return new Status(genesisGenerator, "Member", "I am member of %union%");
		else return new Status(genesisGenerator, "Alive", "I am alive.");
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
