package test;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Test;

import database.DBSet;
import ntp.NTP;
import database.AssetMap;
import qora.account.PrivateKeyAccount;
//import database.IssueAssetMap;
import qora.assets.Asset;
import qora.assets.Venture;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.Transaction;


public class DatabaseTests {

	long OIL_KEY = 1l;
	byte FEE_POWER = (byte)1;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();

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
		//GenesisBlock gb = new GenesisBlock();
		//gb.process();
		
		AssetMap db = DBSet.getInstance().getAssetMap();
		
		Collection<Asset> assets = db.getValues();
		for (Asset asset:assets) {
			//Asset asset = DBSet.getInstance().getAssetMap().get(key);
			Logger.getGlobal().info("ASSET - " + asset.getKey() + " : " + asset.getName()
			+ " : " + asset.getQuantity()	
			+ " - " + asset.getReference().length	
			+ ": " + asset.getReference());	
			//db.add(asset);
		}
		
		db.add(db.get(1l));
		Logger.getGlobal().info("keys " + db.getKeys());

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
		
		Asset asset = new Venture(maker, "test", "strontje", 50000l, (byte) 2, false);
		Transaction issueAssetTransaction = new IssueAssetTransaction(null, maker, asset, FEE_POWER, timestamp, maker.getLastReference(db));
		issueAssetTransaction.sign(maker);
		issueAssetTransaction.process();
		//Logger.getGlobal().info(asset.toString() + " getQuantity " + asset.getQuantity());
		
		long key = asset.getKey(db);
		
		AssetMap assetDB = db.getAssetMap();
		Collection<Asset> assets = assetDB.getValues();
		for (Asset asset_2:assets) {
			Logger.getGlobal().info(asset_2.toString() + " getQuantity " + asset_2.getQuantity());
		}
		
	}
}
