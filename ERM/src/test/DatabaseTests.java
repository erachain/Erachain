package test;

import static org.junit.Assert.*;

 import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Test;

import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.transaction.IssueAssetTransaction;
import core.transaction.Transaction;
import database.DBSet;
import ntp.NTP;
import database.ItemAssetMap;


public class DatabaseTests {

	long OIL_KEY = 1l;
	byte FEE_POWER = (byte)1;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();

	static Logger LOGGER = Logger.getLogger(DatabaseTests.class.getName());

	@Test
	public void databaseFork() 
	{
		//CREATE DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE FORK
		DBSet fork = databaseSet.fork();
		
		//SET BALANCE
		databaseSet.getBalanceMap().set("test", BigDecimal.ONE);
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, databaseSet.getBalanceMap().get("test"));
		
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.ONE, fork.getBalanceMap().get("test"));
		
		//SET BALANCE IN FORK
		fork.getBalanceMap().set("test", BigDecimal.TEN);
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, databaseSet.getBalanceMap().get("test"));
				
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.TEN, fork.getBalanceMap().get("test"));
		
		//CREATE SECOND FORK
		DBSet fork2 = fork.fork();
		
		//SET BALANCE IN FORK2
		fork2.getBalanceMap().set("test", BigDecimal.ZERO);
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, databaseSet.getBalanceMap().get("test"));
						
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.TEN, fork.getBalanceMap().get("test"));
		
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.ZERO, fork2.getBalanceMap().get("test"));
	}
	
	@Test
	public void databaseAssets()
	{
		
		DBSet.reCreateDatabase();
		GenesisBlock gb = new GenesisBlock();
		gb.process();
		
		ItemAssetMap db = DBSet.getInstance().getAssetMap();
		Collection<ItemCls> assets = db.getValues();
		for (ItemCls asset:assets) {
			//Asset asset = DBSet.getInstance().getAssetMap().get(key);
			AssetCls aa = (AssetCls) asset;
			LOGGER.info("ASSET - " + asset.getKey() + " : " + asset.getName()
				+ " : " + aa.getQuantity()	
				+ " - " + aa.getReference().length	
				+ ": " + aa.getReference());	
			//db.add(asset);
		}
				
		db.add(db.get(1l));
		LOGGER.info("keys " + db.getKeys());

		//Collection<Asset> issues = DBSet.getInstance().getIssueAssetMap.getValues();
		
		//long key = db.);

	}

	@Test
	public void databaseAssetsAddGet()
	{


		DBSet.reCreateDatabase();
		
		DBSet db = DBSet.getInstance();
		GenesisBlock gb = new GenesisBlock();
		gb.process();

		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
		
		// OIL FUND
		//maker.setLastReference(gb.getGeneratorSignature(), db);
		//maker.setConfirmedBalance(OIL_KEY, BigDecimal.valueOf(1).setScale(8), db);
		
		AssetCls asset = new AssetVenture(maker, "test", "strontje", 50000l, (byte) 2, false);
		Transaction issueAssetTransaction = new IssueAssetTransaction(null, maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(false);
		//LOGGER.info(asset.toString() + " getQuantity " + asset.getQuantity());
		
		long key = asset.getKey(db);
		
		ItemAssetMap assetDB = db.getAssetMap();
		Collection<ItemCls> assets = assetDB.getValues();
		for (ItemCls asset_2:assets) {
			AssetCls aa = (AssetCls) asset_2;
			LOGGER.info(aa.toString() + " getQuantity " + aa.getQuantity());
		}
		
	}
}
