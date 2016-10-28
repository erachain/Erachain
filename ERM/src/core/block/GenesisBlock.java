package core.block;

// import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.sql.Timestamp;
//import org.mapdb.Fun.Tuple3;
//import org.mapdb.Fun.Tuple11;
import java.util.TreeSet;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

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
	
	private static int genesisVersion = 0;
	private static byte[] genesisReference = Bytes.ensureCapacity(new byte[]{19,66,8,21,0,0,0,0}, Crypto.SIGNATURE_LENGTH, 0);
	public final static int GENESIS_GENERATING_BALANCE = Settings.GENESIS_ERMO_TOTAL; // starting max volume for generating	
	//public static final long MAX_GENERATING_BALANCE = GENESIS_GENERATING_BALANCE / 2;
	public static final int MIN_GENERATING_BALANCE = 100;
	public static final BigDecimal MIN_GENERATING_BALANCE_BD = new BigDecimal(MIN_GENERATING_BALANCE);
	//public static final int GENERATING_RETARGET = 10;
	public static final int GENERATING_MIN_BLOCK_TIME = 180;
	public static final int GENERATING_MAX_BLOCK_TIME = 600;
	public static final int MAX_BLOCK_BYTES = 4 * 1048576;

	private static byte[] icon = new byte[0];
	private static byte[] image = new byte[0];

	private String testnetInfo; 
	private long genesisTimestamp;
	
	AssetVenture asset0;
	AssetVenture asset1;
	List<Transaction> transactions = new ArrayList<Transaction>();
	private static PublicKeyAccount genesisGenerator = new PublicKeyAccount(new byte[PublicKeyAccount.PUBLIC_KEY_LENGTH]);


	public GenesisBlock()
	{
		//SET HEADER
		super(genesisVersion, genesisReference, genesisGenerator, new byte[0], new byte[0]);
		
		this.genesisTimestamp = Settings.getInstance().getGenesisStamp();
		this.generatingBalance = Settings.GENERAL_ERMO_BALANCE;
		
		Account recipient;
		BigDecimal bdAmount0;
		BigDecimal bdAmount1;
		//PublicKeyAccount issuer = new PublicKeyAccount(new byte[Crypto.HASH_LENGTH]);
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
						"UNKNOWN", "1966-08-21 0:10:10.0", null, (byte)1, "-", (float)0.0, (float)0.0,
						"-", "-", "-", (int) 188, icon, image, "-");
				

				//CREATE ISSUE PERSON TRANSACTION
				//this.addTransaction(new GenesisIssuePersonRecord(user));

				// CERTIFY PERSON
				transactions.add(new GenesisCertifyPersonRecord(recipient, nonce++));

				this.testnetInfo += "\ngenesisAccount(" + String.valueOf(nonce) + "): " + address +  " / POST addresses " + Base58.encode(accountSeed);

				// SEND GENESIS ASSETS
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERMO_KEY, bdAmount0));
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.FEE_KEY, bdAmount1));
		    }
			this.testnetInfo += "\nStart the other nodes with command" + ":";
			this.testnetInfo += "\njava -Xms512m -Xmx1024m -jar ERM.jar -testnet=" + genesisTimestamp;
			
		} else {

			List<Tuple2<Account, BigDecimal>> sends_toUsers = new ArrayList<Tuple2<Account, BigDecimal>>();
			/////////// GENEGAL
			List<List<Object>> generalGenesisUsers = Arrays.asList(
					Arrays.asList(11, new PersonHuman(new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"),
							"-", "1966-08-21", null, 
							(byte)10, "-", (float)0.0, (float)0.0,
							"-", "-", "-", (int) 188, icon, image, "-")),
					Arrays.asList(10, new PersonHuman(new Account("7EpDngzSLXrqnRBJ5x9YKTU395VEpsz5Mz"),
							"-", "1966-08-21", null, 
							(byte)1, "-", (float)0.0, (float)0.0,
							"-", "-", "-", (int) 188, icon, image, "-")),
					Arrays.asList(10, new PersonHuman(new Account("7Dwjk4TUB74CqW6PqfDQF1siXquK48HSPB"),
							"-", "1966-08-21", null, 
							(byte)1, "-", (float)0.0, (float)0.0,
							"-", "-", "-", (int) 188, icon, image, "-")),
					Arrays.asList(10, new PersonHuman(new Account("7GnLzZmiDkSzjKWxkQqjQs2KMUkz7kMFc8"),
							"-", "1966-08-21", null, 
							(byte)1, "-", (float)0.0, (float)0.0,
							"-", "-", "-", (int) 188, icon, image, "-")),

					Arrays.asList(10, new PersonHuman(new Account("76DXrQKfyQmvraQTVUp4rDrUUjwQQagBA8"),
							"-", "1966-08-21", null, 
							(byte)1, "-", (float)0.0, (float)0.0,
							"-", "-", "-", (int) 188, icon, image, "-")),
					Arrays.asList(10, new PersonHuman(new Account("7BvNfm966RwMq8Dna58475cQpvRd7XaWt7"),
							"-", "1966-08-21", null, 
							(byte)1, "-", (float)0.0, (float)0.0,
							"-", "-", "-", (int) 188, icon, image, "-")),
					Arrays.asList(10, new PersonHuman(new Account("7R4jwh5C83HLj7C1FiSbsGptMHqfAirr8R"),
							"-", "1966-08-21", null, 
							(byte)1, "-", (float)0.0, (float)0.0,
							"-", "-", "-", (int) 188, icon, image, "-")),
					Arrays.asList(10, new PersonHuman(new Account("75hXUtuRoKGCyhzps7LenhWnNtj9BeAF12"),
							"-", "1966-08-21", null, 
							(byte)1, "-", (float)0.0, (float)0.0,
							"-", "-", "-", (int) 188, icon, image, "-")),

					Arrays.asList(10, new PersonHuman(new Account("7D7GCcPQXMMT7HEDBQ93pTwDfrUWj4GjgE"),
							"-", "1966-08-21", null, 
							(byte)1, "-", (float)0.0, (float)0.0,
							"-", "-", "-", (int) 188, icon, image, "-")),
					Arrays.asList(10, new PersonHuman(new Account("7RAyLF4t35UudD3Ko1svUe9mTnpDj6hrM9"),
							"-", "1966-08-21", null, 
							(byte)1, "-", (float)0.0, (float)0.0,
							"-", "-", "-", (int) 188, icon, image, "-")),
					Arrays.asList(10, new PersonHuman(new Account("76ACGgH8c63VrrgEw1wQA4Dno1JuPLTsWe"),
							"-", "1966-08-21", null, 
							(byte)1, "-", (float)0.0, (float)0.0,
							"-", "-", "-", (int) 188, icon, image, "-")),

					////
					Arrays.asList(10, new PersonHuman(new Account("78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5"),
							"Ермолаев, Дмитрий Сергеевич", "1966-08-21", null, 
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "школа: г.Уссурийск №6, институт: г.Владивосток ДВПИ")),
					Arrays.asList(10, new PersonHuman(new Account("7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC"),
							"Ермолаев, Александр Сергеевич", "1966-08-21", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "светло-коричневый", (int) 188, icon, image, "школа: г.Уссурийск №6, институт: г.Владивосток ДВПИ")),
					Arrays.asList(10, new PersonHuman(new Account("7EpDngzSLXrqnRBJ5x9YKTU395VEpsz5Mz"),
							"Скорняков, Александр Викторович", "1963-08-21", null,
							(byte)1, "европеец-славянин", (float)1.1330, (float)13.9224,
							"белый", "серо-зеленый", "светло-коричневый", (int) 188, icon, image, "-"))
				);
			/////////// MAJOR
			List<List<Object>> majorGenesisUsers = Arrays.asList(
					Arrays.asList(1000, new PersonHuman(new Account("7FoC1wAtbR9Z5iwtcw4Ju1u2DnLBQ1TNS7"),
							"Симанков, Дмитрий", "1966-08-21", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("78A24nTM2PPdpjLF2JWbghPDUhPK1zQ51Y"),
							"Добрышкин, Сергей", "1966-08-21", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("76GJujhki7z2BeX1bnp4KL5Qp22NsakWeT"),
							"Бородин, Олег", "1966-08-21", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "")),
					Arrays.asList(1000, new PersonHuman(new Account("7RhYgcBSLNLKURXzv85BRuzp4DBb2bpCag"),
							"Попилин, Максим Александрович", "1984-08-10", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("7AfGz1FJ6tUnxxKSAHfcjroFEm8jSyVm7r"),
							"в аренду", "1970-12-08", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("7JWNnyeiti3X7MYo83kDJVw15PLR7VqUjb"),
							"Рабчевский, Павел Александрович", "1979-09-08", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("7EDf4NPP6wRTmTtZcszo7ivNYhWrP2X44P"),
							"Стриженок, Арсений Сергеевич", "1991-02-05", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("7McpCLj5a27mnSpo9UGHCcDr2CysC382VJ"),
							"Скорняков, Александр Викторович", "1956-02-01", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-")),
					Arrays.asList(1000, new PersonHuman(new Account("7L4erwEVLbGfY6hw4o3GKMjdi8KsJjPdCt"),
							"Симонов, Олег Вадимович", "1967-02-11", null,
							(byte)1, "европеец-славянин", (float)43.1330, (float)131.9224,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-"))
				);
			////////// MINOR
			List<List<Object>> minorGenesisUsers = Arrays.asList(
					Arrays.asList(100, new PersonHuman(new Account("73CcZe3PhwvqMvWxDznLAzZBrkeTZHvNzo"),
							"неизвестный участник", "1966-08-21",  null,
							(byte)1, "европеец-славянин", (float)0.0, (float)0.0,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-")),
					Arrays.asList(100, new PersonHuman(new Account("7FUUEjDSo9J4CYon4tsokMCPmfP4YggPnd"),
							"неизвестный участник", "1966-08-21",  null,
							(byte)1, "европеец-славянин", (float)0.0, (float)0.0,
							"белый", "серо-зеленый", "серо-коричневый", (int) 188, icon, image, "-"))
					);
			List<PersonCls> personGenesisUsers = Arrays.asList(
					new PersonHuman(genesisGenerator,
							"Менделеев, Дмитрий Иванович", "1834-02-08", "1907-02-02",
							(byte)1, "европеец-славянин", (float)58.195278, (float)68.258056,
							"белый", "серо-зеленый", "серо-коричневый", (int) 180, icon, image, "русский учёный-энциклопедист: химик, физикохимик, физик, метролог, экономист, технолог, геолог, метеоролог, нефтяник, педагог, воздухоплаватель, приборостроитель. Профессор Санкт-Петербургского университета; член-корреспондент по разряду «физический» Императорской Санкт-Петербургской Академии наук. Среди наиболее известных открытий — периодический закон химических элементов, один из фундаментальных законов мироздания, неотъемлемый для всего естествознания. Автор классического труда «Основы химии».")
					);

			////////// INVESTORS
			List<List<Object>> genesisInvestors = Arrays.asList(
					//Arrays.asList(100, "7FUUEjDSo9J4CYon4tsokMCPmfP4YggPnd"),
					//Arrays.asList(100, "73CcZe3PhwvqMvWxDznLAzZBrkeTZHvNzo")
					);

			// genesis users
			List<List<Object>> genesisDebtors = Arrays.asList(
					Arrays.asList(100, "7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7"),
					Arrays.asList(100, "7AfGz1FJ6tUnxxKSAHfcjroFEm8jSyVm7r"),
					Arrays.asList(100, "77QnJnSbS9EeGBa2LPZFZKVwjPwzeAxjmy"),
					Arrays.asList(100, "7RUytz6baxNV4MVJnpdz43YSvth19GLkTP"),
					Arrays.asList(100, "7RYEVPZg7wbu2bmz3tWnzrhPavjpyQ4tnp"),
					Arrays.asList(100, "7AjPSBEumyNkdeoRtLDciBJWrxgYe9o8po"),
					Arrays.asList(100, "78xTnRVFTkJ3pu2BrktxFkY7rKDofiActv"),
					Arrays.asList(100, "7PFRVswUdzWB7JYp9VJzfk9Qcnjh7eCVNY")					
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
			long debtorPicked = 0;
			for(List<Object> item: genesisDebtors)
			{
				debtorPicked += (int)item.get(0);
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
			double debtorKoeff = 0.8 * asset0.getQuantity() / debtorPicked;
			BigDecimal limitOwned = new BigDecimal( 0.001 * asset0.getQuantity()).setScale(8);
			
			//long i = 0;
			int pick;

			int nonce = 0;
			for(List<Object> item: generalGenesisUsers)
			{
				
				pick = (int)item.get(0);
				user = (PersonHuman)item.get(1);
				
				recipient = user.getCreator();
				
				//CREATE ISSUE PERSON TRANSACTION
				//this.addTransaction(new GenesisIssuePersonRecord(user));

				bdAmount0 = new BigDecimal(Math.round(pick * generalKoeff0) - nonce++).setScale(8);
				//bal0 = bal0.add(bdAmount0).setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERMO_KEY, bdAmount0));
				
				// buffer for CREDIT sends
				sends_toUsers.add(new Tuple2<Account, BigDecimal>(recipient, bdAmount0));

				//bdAmount1 = new BigDecimal(Math.round(pick * generalKoeff1)).setScale(8);
				bdAmount1 = BigDecimal.ONE.setScale(8);
				//bal1 = bal1.add(bdAmount1).setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.FEE_KEY, bdAmount1));


			}

			// IN RENT
			Account rentOwner = new Account("7EpDngzSLXrqnRBJ5x9YKTU395VEpsz5Mz");
			for(List<Object> item: majorGenesisUsers)
			{
				
				pick = (int)item.get(0);
				user = (PersonHuman)item.get(1);
				
				recipient = user.getCreator();
				
				//CREATE ISSUE PERSON TRANSACTION
				//this.addTransaction(new GenesisIssuePersonRecord(user));

				bdAmount0 = new BigDecimal(Math.round(pick * majorKoeff) - nonce++).setScale(8);
				//bal0 = bal0.add(bdAmount0).setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERMO_KEY, bdAmount0, rentOwner));

				// buffer for CREDIT sends
				sends_toUsers.add(new Tuple2<Account, BigDecimal>(recipient, bdAmount0));

				bdAmount1 = new BigDecimal("0.001").setScale(8);
				//bal1 = bal1.add(bdAmount1).setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.FEE_KEY, bdAmount1));

				// CERTIFY PERSON
				//this.addTransaction(new GenesisCertifyPersonRecord(recipient, i++));

			}

			for(List<Object> item: minorGenesisUsers)
			{
				
				pick = (int)item.get(0);
				user = (PersonHuman)item.get(1);
				
				recipient = user.getCreator();
				
				//CREATE ISSUE PERSON TRANSACTION
				//this.addTransaction(new GenesisIssuePersonRecord(user));

				bdAmount0 = new BigDecimal(Math.round(pick * minorKoeff)).setScale(8);
				//bal0 = bal0.add(bdAmount0).setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERMO_KEY, bdAmount0));

				// buffer for CREDIT sends
				sends_toUsers.add(new Tuple2<Account, BigDecimal>(recipient, bdAmount0));

				bdAmount1 = new BigDecimal("0.001").setScale(8);
				//bal1 = bal1.add(bdAmount1).setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.FEE_KEY, bdAmount1));

				// CERTIFY PERSON
				//this.addTransaction(new GenesisCertifyPersonRecord(recipient, i++));

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
				} else {
					//this.addTransaction(new GenesisIssuePersonRecord(person));
				}
			}

			// NOT PERSONALIZE INVESTORS
			for(List<Object> item: genesisInvestors)
			{
				
				pick = (int)item.get(0);				
				recipient = new Account((String)item.get(1));
				
				bdAmount0 = new BigDecimal(Math.round(pick * investorKoeff)).setScale(8);
				//bal0 = bal0.add(bdAmount0).setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.ERMO_KEY, bdAmount0));

				// buffer for CREDIT sends
				sends_toUsers.add(new Tuple2<Account, BigDecimal>(recipient, bdAmount0));

				bdAmount1 = new BigDecimal("0.001").setScale(8);
				//bal1 = bal1.add(bdAmount1).setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.FEE_KEY, bdAmount1));

			}			

			// FOR DEBROTS
			int i = 0;
			Account bufferCreditor = sends_toUsers.get(i).a;
			BigDecimal bufferAmount = sends_toUsers.get(i).b.subtract(limitOwned);
			for(List<Object> item: genesisDebtors)
			{
				
				pick = (int)item.get(0);				
				recipient = new Account((String)item.get(1));
				
				bdAmount0 = new BigDecimal(Math.round(pick * debtorKoeff)).setScale(8);

				do {
					if (bufferAmount.compareTo(bdAmount0) < 0) {
						transactions.add(new GenesisTransferAssetTransaction(recipient, -AssetCls.ERMO_KEY,
								bufferAmount, bufferCreditor));
						bdAmount0 = bdAmount0.subtract(bufferAmount);
						i++;
						bufferCreditor = sends_toUsers.get(i).a;
						bufferAmount = sends_toUsers.get(i).b;
						// TRY rest limit for self
						if (bufferAmount.compareTo(limitOwned) > 0) {
							bufferAmount = bufferAmount.subtract(limitOwned);
						}
						continue;
					} else {
						transactions.add(new GenesisTransferAssetTransaction(recipient, -AssetCls.ERMO_KEY,
								bdAmount0, bufferCreditor));
						bufferAmount = bufferAmount.subtract(bdAmount0);
						break;
					}
				} while (true);

				bdAmount1 = new BigDecimal("0.0001").setScale(8);
				transactions.add(new GenesisTransferAssetTransaction(recipient, AssetCls.FEE_KEY, bdAmount1));

			}			
		}
		
		//GENERATE AND VALIDATE TRANSACTIONS
		this.setTransactions(transactions);
		// SIGN simple as HASH
		this.signature = generateHeadHash();
	}
	
	private void initItems()
	{
		
		///// ASSETS
		//CREATE ERM ASSET
		asset0 = makeAsset(AssetCls.ERMO_KEY);
		transactions.add(new GenesisIssueAssetTransaction(asset0));
		//CREATE JOB ASSET
		asset1 = makeAsset(AssetCls.FEE_KEY);
		transactions.add(new GenesisIssueAssetTransaction(asset1));
		// ASSET OTHER
		for (int i = (int)AssetCls.FEE_KEY + 1; i <= AssetCls.DEAL_KEY; i++) 
			transactions.add(new GenesisIssueAssetTransaction(makeAsset(i)));

		///// NOTES
		for (int i = 1; i <= NoteCls.HIRING_KEY; i++) 
			transactions.add(new GenesisIssueNoteRecord(makeNote(i)));

		///// STATUSES
		for (int i = 1; i <= StatusCls.MARRIED_KEY; i++) 
			transactions.add(new GenesisIssueStatusRecord(makeStatus(i)));		
	}
	
	// make assets
	public static AssetVenture makeAsset(long key) 
	{
		switch((int)key)
		{
		case (int)AssetCls.FEE_KEY:
			return new AssetVenture(genesisGenerator, AssetCls.FEE_NAME, icon, image, AssetCls.FEE_DESCR, false, 1000L, (byte)8, true);
		case (int)AssetCls.TRUST_KEY:
			return new AssetVenture(genesisGenerator, AssetCls.TRUST_NAME, icon, image, AssetCls.TRUST_DESCR, false, 0L, (byte)8, true);
		case (int)AssetCls.REAL_KEY:
			return new AssetVenture(genesisGenerator, AssetCls.REAL_NAME, icon, image, AssetCls.REAL_DESCR, false, 0L, (byte)8, true);
		case (int)AssetCls.DEAL_KEY:
			return new AssetVenture(genesisGenerator, AssetCls.DEAL_NAME, icon, image, AssetCls.DEAL_DESCR, false, 0L, (byte)8, true);
		}
		return new AssetVenture(genesisGenerator, AssetCls.ERMO_NAME, icon, image, AssetCls.ERMO_DESCR, false, GENESIS_GENERATING_BALANCE, (byte)0, true);
	}
	// make notes
	public static Note makeNote(int key) 
	{
		switch(key)
		{
		case (int)NoteCls.EMPTY_KEY:
			return new Note(genesisGenerator, "empty", icon, image, "empty");
		case (int)NoteCls.ESTABLISH_UNION_KEY:
			return new Note(genesisGenerator, "Establish the Union", icon, image, "Union name \"%Company Name%\" in country \"%Country%\"");
		case (int)NoteCls.MARRIAGE_KEY:
			return new Note(genesisGenerator, "Marriage", icon, image, "%person1% marries  %person2%");
		case (int)NoteCls.HIRING_KEY:
			return new Note(genesisGenerator, "Hiring", icon, image, "Hiring to %union%");
		}
		return new Note(genesisGenerator, "I", icon, image, "I, Dmitry Ermolaev, date of birth \"1966.08.21\", place of birth \"Vladivostok, Primorsky Krai, Russia\", race \"Slav\", height \"188\", eye color \"light grey\", color \"white\", hair color \"dark brown\", I confirm that I have single-handedly account \"\" and I beg to acknowledge the data signed by this account as my own's handmade signature.");
	}
	// make notes
	public static Status makeStatus(int key)
	{
		if (key == StatusCls.MEMBER_KEY) return new Status(genesisGenerator, "Member", icon, image, "Director, Manager, Worker, Member, Holder");
		else if (key == StatusCls.ALIVE_KEY) return new Status(genesisGenerator, "Alive", icon, image, "Alive or Dead");
		else if (key == StatusCls.RANK_KEY) return new Status(genesisGenerator, "Rank", icon, image, "General, Major or Minor");
		else if (key == StatusCls.USER_KEY) return new Status(genesisGenerator, "User", icon, image, "Admin, User, Observer");
		else if (key == StatusCls.MAKER_KEY) return new Status(genesisGenerator, "Maker", icon, image, "Creator, Designer, Maker");
		else if (key == StatusCls.DELEGATE_KEY) return new Status(genesisGenerator, "Delegate", icon, image, "President, Senator, Deputy");
		else if (key == StatusCls.CERTIFIED_KEY) return new Status(genesisGenerator, "Certified", icon, image, "Certified, Notarized, Confirmed");
		else if (key == StatusCls.MARRIED_KEY) return new Status(genesisGenerator, "Married", icon, image, "Husband, Wife, Spouse");

		return new Status(genesisGenerator, "RIGHTs", icon, image, "Rights");		
	}
	
	
	//GETTERS

	@Override
	public long getTimestamp(DBSet db)
	{
		return this.genesisTimestamp;
	}
	
	public String getTestNetInfo() 
	{
		return this.testnetInfo;
	}
	
	@Override
	public Block getParent(DBSet db)
	{
		//PARENT DOES NOT EXIST
		return null;
	}
	/*
	@Override
	public int getGeneratingBalance()
	{
		return 0;
	}
	*/

	
	public byte[] generateHeadHash()
	{
		byte[] data = new byte[0];
		
		//WRITE VERSION
		byte[] versionBytes = Longs.toByteArray(genesisVersion);
		versionBytes = Bytes.ensureCapacity(versionBytes, 4, 0);
		data = Bytes.concat(data, versionBytes);
		
		//WRITE REFERENCE
		byte[] referenceBytes = Bytes.ensureCapacity(genesisReference, Crypto.SIGNATURE_LENGTH, 0);
		data = Bytes.concat(data, referenceBytes);
		
		
		//WRITE TIMESTAMP
		byte[] genesisTimestampBytes = Longs.toByteArray(this.genesisTimestamp);
		genesisTimestampBytes = Bytes.ensureCapacity(genesisTimestampBytes, 8, 0);
		data = Bytes.concat(data, genesisTimestampBytes);
		
		/*
		//WRITE GENERATING BALANCE
		byte[] generatingBalanceBytes = Longs.toByteArray(GENESIS_GENERATING_BALANCE);
		generatingBalanceBytes = Bytes.ensureCapacity(generatingBalanceBytes, 8, 0);
		data = Bytes.concat(data, generatingBalanceBytes);
		*/
	
		/*
		//WRITE GENERATOR
		byte[] generatorBytes = Bytes.ensureCapacity(genesisGenerator.getPublicKey(), Crypto.HASH_LENGTH, 0);
		data = Bytes.concat(data, generatorBytes);
		*/
		
		//DIGEST [32]
		byte[] digest = Crypto.getInstance().digest(data);
		
		//DIGEST + transactionsHash
		// = byte[64]
		digest = Bytes.concat(digest, transactionsHash);
		
		return digest;
	}
	
	//VALIDATE
	
	@Override
	public boolean isSignatureValid()
	{
		
		//VALIDATE BLOCK SIGNATURE
		byte[] digest = generateHeadHash();				
		if(!Arrays.equals(digest, this.signature))
		{
			return false;
		}
				
		return true;
	}
	
	/*
	@Override
	public BigDecimal getTotalFee()
	{
		return BigDecimal.ZERO.setScale(8);
	}
	*/
	
	@Override
	public boolean isValid(DBSet db)
	{
		//CHECK IF NO OTHER BLOCK IN DB
		if(db.getBlockMap().getLastBlock() != null)
		{
			return false;
		}
		
		//VALIDATE TRANSACTIONS
		byte[] transactionsSignatures = new byte[0];
		for(Transaction transaction: this.getTransactions())
		{
			if(transaction.isValid(db, null) != Transaction.VALIDATE_OK)
			{
				return false;
			}
			transactionsSignatures = Bytes.concat(transactionsSignatures, transaction.getSignature());

		}
		transactionsSignatures = Crypto.getInstance().digest(transactionsSignatures);
		if (!Arrays.equals(this.transactionsHash, transactionsSignatures)) {
			LOGGER.error("*** GenesisBlock.digest(transactionsSignatures) invalid");
			return false;
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
