package test;

import static org.junit.Assert.*;

 import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.mapdb.Fun.Tuple3;

import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.item.persons.PersonCls;
import core.item.persons.PersonHuman;
import core.item.statuses.StatusCls;
import core.transaction.GenesisCertifyPersonRecord;
import core.transaction.GenesisIssuePersonRecord;
import core.transaction.IssueAssetTransaction;
import core.transaction.IssuePersonRecord;
import core.transaction.R_SertifyPubKeys;
import core.transaction.Transaction;
import core.wallet.Wallet;
import database.AddressPersonMap;
import database.DBSet;
import ntp.NTP;
import database.ItemAssetMap;
import database.KKPersonStatusMap;
import database.PersonAddressMap;


public class DatabaseTests {

	byte[] assetReference = new byte[Crypto.SIGNATURE_LENGTH];

	static Logger LOGGER = Logger.getLogger(DatabaseTests.class.getName());

	Long releaserReference = null;

	BigDecimal BG_ZERO = BigDecimal.ZERO.setScale(8);
	long ERM_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	long ALIVE_KEY = StatusCls.ALIVE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] personReference = new byte[Crypto.SIGNATURE_LENGTH];
	long timestamp = NTP.getTime();
	
	private byte[] icon = new byte[0]; // default value
	private byte[] image = new byte[0]; // default value

	//CREATE EMPTY MEMORY DATABASE
	private DBSet dbSet;
	private GenesisBlock gb;
	Long last_ref;
	boolean asPack = false;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    	
	PersonCls personGeneral;
	PersonCls person;
	long personKey = -1;
	IssuePersonRecord issuePersonTransaction;
	R_SertifyPubKeys r_SertifyPubKeys;

	//int version = 0; // without signs of person
	int version = 1; // with signs of person
	
	// INIT PERSONS
	private void init() {
		
		dbSet = DBSet.createEmptyDatabaseSet();

		gb = new GenesisBlock();
		gb.process(dbSet);
		
		last_ref = gb.getTimestamp(dbSet);
		
		// GET RIGHTS TO CERTIFIER
		byte gender = 1;
		long birthDay = timestamp - 12345678;
		personGeneral = new PersonHuman(maker, "Ermolaev Dmitrii Sergeevich as sertifier", birthDay, birthDay - 1,
				gender, "Slav", (float)28.12345, (float)133.7777,
				"white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей");
				
		GenesisIssuePersonRecord genesis_issue_person = new GenesisIssuePersonRecord(personGeneral);
		genesis_issue_person.process(dbSet, false);
		GenesisCertifyPersonRecord genesis_certify = new GenesisCertifyPersonRecord(maker, 0L);
		genesis_certify.process(dbSet, false);
		
		maker.setLastReference(last_ref, dbSet);
		maker.setBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), dbSet);
		maker.setBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), dbSet);
		
		person = new PersonHuman(maker, "Ermolaev Dmitrii Sergeevich", birthDay, birthDay - 2,
				gender, "Slav", (float)28.12345, (float)133.7777,
				"white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей");

		//CREATE ISSUE PERSON TRANSACTION
		issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp, maker.getLastReference(dbSet));

	}

	@Test
	public void databaseFork()
	{
		
		init();
		
		issuePersonTransaction.sign(maker, asPack);
		issuePersonTransaction.process(dbSet, asPack);

		issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp++, maker.getLastReference(dbSet));
		issuePersonTransaction.sign(maker, asPack);
		issuePersonTransaction.process(dbSet, asPack);

		issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp++, maker.getLastReference(dbSet));
		issuePersonTransaction.sign(maker, asPack);
		issuePersonTransaction.process(dbSet, asPack);
		

		//assertEquals(dbSet.getItemPersonMap().getKeys().toString(), "");
		//assertEquals(dbSet.getItemPersonMap().getValues().toString(), "");
		//CREATE FORK
		DBSet fork = dbSet.fork();

		issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp++, maker.getLastReference(fork));
		issuePersonTransaction.sign(maker, asPack);
		issuePersonTransaction.process(fork, asPack);

		issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp++, maker.getLastReference(fork));
		issuePersonTransaction.sign(maker, asPack);
		issuePersonTransaction.process(fork, asPack);

		//assertEquals(PersonCls.getItem(fork, ItemCls.PERSON_TYPE, 1).getDBMap(fork).getKeys().toString(), "");
		
		//SET BALANCE
		dbSet.getAssetBalanceMap().set("test", 1L, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE));
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, dbSet.getAssetBalanceMap().get("test", 1L));
		
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.ONE, fork.getAssetBalanceMap().get("test", 1L));
		
		//SET BALANCE IN FORK
		fork.getAssetBalanceMap().set("test", 1L, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN));
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, dbSet.getAssetBalanceMap().get("test", 1L));
				
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.TEN, fork.getAssetBalanceMap().get("test", 1L));
		
		//CREATE SECOND FORK
		DBSet fork2 = fork.fork();
		
		//SET BALANCE IN FORK2
		fork2.getAssetBalanceMap().set("test", 1L, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, dbSet.getAssetBalanceMap().get("test", 1L));
						
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.TEN, fork.getAssetBalanceMap().get("test", 1L));
		
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.ZERO, fork2.getAssetBalanceMap().get("test", 1L));
	}
	
	@Test
	public void databaseAssets()
	{
		
		DBSet.reCreateDatabase();
				
		GenesisBlock gb = new GenesisBlock();
		gb.process(dbSet);
		
		ItemAssetMap dbMap = dbSet.getItemAssetMap();
		Collection<ItemCls> assets = dbMap.getValues();
		for (ItemCls asset:assets) {
			//Asset asset = DBSet.getInstance().getAssetMap().get(key);
			AssetCls aa = (AssetCls) asset;
			LOGGER.info("ASSET - " + asset.getKey(dbSet) + " : " + asset.getName()
				+ " : " + aa.getQuantity()	
				+ " - " + aa.getReference().length	
				+ ": " + aa.getReference());	
			//db.add(asset);
		}
				
		dbMap.add(dbMap.get(1l));
		LOGGER.info("keys " + dbMap.getKeys());

		//Collection<Asset> issues = DBSet.getInstance().getIssueAssetMap.getValues();
		
		//long key = db.);

	}

	@Test
	public void databaseAssetsAddGet()
	{

		init();
						
		AssetCls asset = new AssetVenture(maker, "test", icon, image, "strontje", 50000l, (byte) 2, false);
		Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastReference(dbSet));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(dbSet, false);
		//LOGGER.info(asset.toString() + " getQuantity " + asset.getQuantity());
		
		long key = asset.getKey(dbSet);
		
		ItemAssetMap assetDB = dbSet.getItemAssetMap();
		Collection<ItemCls> assets = assetDB.getValues();
		for (ItemCls asset_2:assets) {
			AssetCls aa = (AssetCls) asset_2;
			LOGGER.info(aa.toString() + " getQuantity " + aa.getQuantity());
		}
		
	}
}
