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
import core.item.assets.AssetCls;
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
import core.transaction.GenesisIssueNoteRecord;
import core.transaction.GenesisIssuePersonRecord;
import core.transaction.GenesisIssueStatusRecord;
import core.transaction.GenesisTransferAssetTransaction;
import database.DBSet;
import gui.Gui;
import lang.Lang;
import settings.Settings;
import utils.Pair;
import utils.SysTray;

public class GenesisBlock extends Block{
	
	private static int genesisVersion = 1;
	private static byte[] genesisReference = Bytes.ensureCapacity(new byte[]{19,66,8,21,0,0,0,0}, 128, 0);
	private static long genesisGeneratingBalance = 12000000L; // starting max volume for generating	
	private final static PublicKeyAccount genesisGenerator = new PublicKeyAccount(Bytes.ensureCapacity(new byte[]{0,1,2,3,4,13,31,13,31,13,31}, PublicKeyAccount.PUBLIC_KEY_LENGTH, 0));

	private String testnetInfo; 
	
	AssetVenture asset0;
	AssetVenture asset1;

	public GenesisBlock()
	{
		//SET HEADER
		super(genesisVersion, genesisReference, Settings.getInstance().getGenesisStamp(), genesisGeneratingBalance / 5, genesisGenerator, generateHeadHash());
		
		long genesisTimestamp = Settings.getInstance().getGenesisStamp();
		Account recipient;
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
						"UNKNOWN", "1966-08-21 0:10:10.0", null, (byte)1, "-", (float)0.1330, (float)1.9224,
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
			this.setTransactionsSignature(this.generateHash());
			
		} else {
			/////////// GENEGAL
			List<List<Object>> generalGenesisUsers = Arrays.asList(
					Arrays.asList(1, new PersonHuman(new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"),
							"Ермолаев, Дмитрий Сергеевич", "1966-08-21", null, 
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "школа: г.Уссурийск №6, институт: г.Владивосток ДВПИ")),
					Arrays.asList(1, new PersonHuman(new Account("7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC"),
							"Ермолаев, Александр Сергеевич", "1966-08-21", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "светло-коричневый", (int) 188, "школа: г.Уссурийск №6, институт: г.Владивосток ДВПИ")),
					Arrays.asList(1, new PersonHuman(new Account("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5"),
							"Скорняков, Александр Викторович", "1963-08-21", null,
							(byte)1, "европеец-славянин", (float)1.1330, (float)13.9224,
							"белый", "серо-зеленый", "светло-коричневый", (int) 188, "-"))
				);
			/////////// MAJOR
			List<List<Object>> majorGenesisUsers = Arrays.asList(
					Arrays.asList(1000, new PersonHuman(new Account("7FoC1wAtbR9Z5iwtcw4Ju1u2DnLBQ1TNS7"),
							"Симанков, Дмитрий", "1966-08-21", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("78A24nTM2PPdpjLF2JWbghPDUhPK1zQ51Y"),
							"Добрышкин, Сергей", "1966-08-21", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("76GJujhki7z2BeX1bnp4KL5Qp22NsakWeT"),
							"Бородин, Олег", "1966-08-21", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "")),
					Arrays.asList(1000, new PersonHuman(new Account("7RhYgcBSLNLKURXzv85BRuzp4DBb2bpCag"),
							"Попилин, Максим Александрович", "1984-08-10", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7"),
							"Кузьмин, Павел Иванович", "1970-12-08", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("7JWNnyeiti3X7MYo83kDJVw15PLR7VqUjb"),
							"Рабчевский, Павел Александрович", "1979-09-08", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("7EDf4NPP6wRTmTtZcszo7ivNYhWrP2X44P"),
							"Стриженок, Арсений Сергеевич", "1991-02-05", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("7McpCLj5a27mnSpo9UGHCcDr2CysC382VJ"),
							"Скорняков, Александр Викторович", "1956-02-01", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("7L4erwEVLbGfY6hw4o3GKMjdi8KsJjPdCt"),
							"Симонов, Олег Вадимович", "1967-02-11", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-"))
				);
			////////// MINOR
			List<List<Object>> minorGenesisUsers = Arrays.asList(
					Arrays.asList(100, new PersonHuman(new Account("73CcZe3PhwvqMvWxDznLAzZBrkeTZHvNzo"),
							"неизвестный участник", "1966-08-21",  null,
							(byte)1, "европеец-славянин", (float)0.0, (float)0.0,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, "-"))
					);
			List<PersonCls> personGenesisUsers = Arrays.asList(
					new PersonHuman(genesisGenerator,
							"Менделеев, Дмитрий Иванович", "1834-02-08", "1907-02-02",
							(byte)1, "европеец-славянин", (float)58.195278, (float)68.258056,
							"белый", "серо-зеленый", "серо-коричневый", (int) 180, "русский учёный-энциклопедист: химик, физикохимик, физик, метролог, экономист, технолог, геолог, метеоролог, нефтяник, педагог, воздухоплаватель, приборостроитель. Профессор Санкт-Петербургского университета; член-корреспондент по разряду «физический» Императорской Санкт-Петербургской Академии наук. Среди наиболее известных открытий — периодический закон химических элементов, один из фундаментальных законов мироздания, неотъемлемый для всего естествознания. Автор классического труда «Основы химии».")
					);

			////////// INVESTORS
			List<List<Object>> genesisInvestors = Arrays.asList(
					Arrays.asList(100, "7FUUEjDSo9J4CYon4tsokMCPmfP4YggPnd"),
					Arrays.asList(100, "78T3Eof2c4EyhuHc3qCunJ3Wk3TCYyQTnb"),
					Arrays.asList(100, "7NavQiMdL4nSsDMppnVoc6gtoCnREZXrEC"),
					Arrays.asList(100, "753BpHWMyKxKVjsSUiceBab1mydcVEoKDD"),
					Arrays.asList(100, "7QGXujqsuJeb9YeW5L83vaEu3SsWXsRtXc"),
					Arrays.asList(100, "7RLwEuNLN6tJaksaKH1CuBmDGPnmLNDwri"),
					Arrays.asList(100, "73CcZe3PhwvqMvWxDznLAzZBrkeTZHvNzo")
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
			long investorPicked = 0;
			for(List<Object> item: genesisInvestors)
			{
				investorPicked += (int)item.get(0);
			}

			// 10% 1%
			float majorPick = (float)0.1;
			float minorPick = (float)0.001;
			float investorPick = (float)0.005;
			double generalKoeff0 = (1.0 - majorPick - minorPick) * asset0.getQuantity() / generalPicked;
			double generalKoeff1 = asset1.getQuantity() / generalPicked;
			double majorKoeff = majorPick * asset0.getQuantity() / majorPicked;
			double minorKoeff = minorPick * asset0.getQuantity() / minorPicked;
			double investorKoeff = investorPick * asset0.getQuantity() / investorPicked;
			
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
				//this.addTransaction(new GenesisIssuePersonRecord(user, recipient));

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
				//this.addTransaction(new GenesisIssuePersonRecord(user, recipient));

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
				//this.addTransaction(new GenesisIssuePersonRecord(user, recipient));

				// CERTIFY PERSON
				//this.addTransaction(new GenesisCertifyPersonRecord(recipient, i++));

			}

			// NOT PERSONALIZE INVESTORS
			for(List<Object> item: genesisInvestors)
			{
				
				pick = (int)item.get(0);				
				recipient = new Account((String)item.get(1));
				
				bdAmount0 = new BigDecimal(Math.round(pick * investorKoeff)).setScale(8);
				//bal0 = bal0.add(bdAmount0).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(recipient, 0l, bdAmount0));

				bdAmount1 = new BigDecimal("0.01").setScale(8);
				//bal1 = bal1.add(bdAmount1).setScale(8);
				this.addTransaction(new GenesisTransferAssetTransaction(recipient, 1l, bdAmount1));

			}

			// PERSONALIZED USERS
			for(PersonCls person: personGenesisUsers)
			{				
				//CREATE ISSUE PERSON TRANSACTION
				GenesisIssuePersonRecord tr = new GenesisIssuePersonRecord(person);
				if (Transaction.VALIDATE_OK != tr.isValid(null))
				{
					//throw new Exception(Lang.getInstance().translate("Both gui and rpc cannot be disabled!"));
					LOGGER.error(Lang.getInstance().translate("Genesis person error"));
				}

				//this.addTransaction(new GenesisIssuePersonRecord(person));
			}

			//GENERATE AND VALIDATE TRANSACTIONSSIGNATURE
			this.setTransactionsSignature(this.generateHash());
			
		}
	}
	
	private void initItems()
	{
		
		///// ASSETS
		//CREATE ERM ASSET
		asset0 = makeAsset(AssetCls.ERMO_KEY);
		this.addTransaction(new GenesisIssueAssetTransaction(asset0));
		//CREATE JOB ASSET
		asset1 = makeAsset(AssetCls.FEE_KEY);
		this.addTransaction(new GenesisIssueAssetTransaction(asset1));
		// ASSET OTHER
		for (int i = (int)AssetCls.FEE_KEY + 1; i <= AssetCls.DEAL_KEY; i++) 
			this.addTransaction(new GenesisIssueAssetTransaction(makeAsset(i)));

		///// NOTES
		for (int i = 0; i <= NoteCls.HIRING_KEY; i++) 
			this.addTransaction(new GenesisIssueNoteRecord(makeNote(i)));

		///// STATUSES
		for (int i = 0; i <= StatusCls.EXPIRED_KEY; i++) 
			this.addTransaction(new GenesisIssueStatusRecord(makeStatus(i)));		
	}
	
	// make assets
	public static AssetVenture makeAsset(long key) 
	{
		switch((int)key)
		{
		case (int)AssetCls.FEE_KEY:
			return new AssetVenture(genesisGenerator, AssetCls.FEE_NAME, AssetCls.FEE_DESCR, 99999999L, (byte)8, true);
		case (int)AssetCls.TRUST_KEY:
			return new AssetVenture(genesisGenerator, AssetCls.TRUST_NAME, AssetCls.TRUST_DESCR, 0L, (byte)8, true);
		case (int)AssetCls.REAL_KEY:
			return new AssetVenture(genesisGenerator, AssetCls.REAL_NAME, AssetCls.REAL_DESCR, 0L, (byte)8, true);
		case (int)AssetCls.DEAL_KEY:
			return new AssetVenture(genesisGenerator, AssetCls.DEAL_NAME, AssetCls.DEAL_DESCR, 0L, (byte)8, true);
		}
		return new AssetVenture(genesisGenerator, AssetCls.ERMO_NAME, AssetCls.ERMO_DESCR, genesisGeneratingBalance, (byte)0, true);
	}
	// make notes
	public static Note makeNote(int key) 
	{
		switch(key)
		{
		case (int)NoteCls.EMPTY_KEY:
			return new Note(genesisGenerator, "empty", "empty");
		case (int)NoteCls.ESTABLISH_UNION_KEY:
			return new Note(genesisGenerator, "Establish the Union", "Union name \"%Company Name%\" in country \"%Country%\"");
		case (int)NoteCls.MARRIAGE_KEY:
			return new Note(genesisGenerator, "Marriage", "%person1% marries  %person2%");
		case (int)NoteCls.HIRING_KEY:
			return new Note(genesisGenerator, "Hiring", "Hiring to %union%");
		}
		return new Note(genesisGenerator, "I", "I, Dmitry Ermolaev, date of birth \"1966.08.21\", place of birth \"Vladivostok, Primorsky Krai, Russia\", race \"Slav\", height \"188\", eye color \"light grey\", color \"white\", hair color \"dark brown\", I confirm that I have single-handedly account \"\" and I beg to acknowledge the data signed by this account as my own's handmade signature.");
	}
	// make notes
	public static Status makeStatus(int key) 
	{
		if (key == StatusCls.DEAD_KEY) return new Status(genesisGenerator, "dead", "Person is dead");
		else if (key == StatusCls.CITIZEN_KEY) return new Status(genesisGenerator, "Сitizen", "I am citizen of %country%");
		else if (key == StatusCls.MEMBER_KEY) return new Status(genesisGenerator, "Member", "I am member of %union%");
		else if (key == StatusCls.SPOUSE_KEY) return new Status(genesisGenerator, "Spouse", "I am spouse on %spouse%");

		else if (key == StatusCls.GENERAL_KEY) return new Status(genesisGenerator, "General", "");
		else if (key == StatusCls.MAJOR_KEY) return new Status(genesisGenerator, "Major", "");
		else if (key == StatusCls.ADMIN_KEY) return new Status(genesisGenerator, "Admin", "");
		else if (key == StatusCls.MANAGER_KEY) return new Status(genesisGenerator, "Manager", "");
		else if (key == StatusCls.WORKER_KEY) return new Status(genesisGenerator, "Worker", "");
		else if (key == StatusCls.CREATOR_KEY) return new Status(genesisGenerator, "Creator", "");
		else if (key == StatusCls.PRESIDENT_KEY) return new Status(genesisGenerator, "President", "");
		else if (key == StatusCls.DIRECTOR_KEY) return new Status(genesisGenerator, "Director", "");
		else if (key == StatusCls.SENATOR_KEY) return new Status(genesisGenerator, "Senator", "");
		else if (key == StatusCls.DEPUTATE_KEY) return new Status(genesisGenerator, "Deputy", "");
		else if (key == StatusCls.OBSERVER_KEY) return new Status(genesisGenerator, "Observer", "");

		else if (key == StatusCls.CERTIFIED_KEY) return new Status(genesisGenerator, "Certified", "");
		else if (key == StatusCls.CONFIRMED_KEY) return new Status(genesisGenerator, "Confirmed", "");
		else if (key == StatusCls.EXPIRED_KEY) return new Status(genesisGenerator, "Expired", "");

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
	
	public static byte[] generateHeadHash()
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
	
	public byte[] generateHash()
	{
		byte[] data = new byte[0];
		
		/// icreator insert
		//WRITE TRANSACTION SIGNATURE
		for(Transaction transaction: this.getTransactions())
		{
			data = Bytes.concat(data, transaction.getSignature());
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
		
		//VALIDATE BLOCK SIGNATURE
		byte[] digest = generateHeadHash();				
		if(!Arrays.equals(digest, this.generatorSignature))
		{
			return false;
		}
		
		//VALIDATE TRANSACTIONS SIGNATURE
		digest = this.generateHash();
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
