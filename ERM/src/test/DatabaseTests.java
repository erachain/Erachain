package test;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Test;

import database.DBSet;
import database.AssetMap;
//import database.IssueAssetMap;
import qora.assets.Asset;
import qora.block.GenesisBlock;


public class DatabaseTests {

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
			+ " : " + asset.getReference());	
			//db.add(asset);
		}
		db.add(db.get(1l));
		Logger.getGlobal().info("keys " + db.getKeys());

		//Collection<Asset> issues = DBSet.getInstance().getIssueAssetMap.getValues();
	}

}
