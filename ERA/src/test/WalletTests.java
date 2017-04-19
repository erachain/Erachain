package test;

import static org.junit.Assert.*;
import org.junit.Test;

import java.math.BigDecimal;

import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import database.wallet.WalletDatabase;
import database.wallet.SecureWalletDatabase;
import ntp.NTP;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import core.crypto.Crypto;
import core.wallet.Wallet;

public class WalletTests {
	
	byte[] releaserReference = null;

	boolean asPack = false;
	long FEE_KEY = AssetCls.FEE_KEY;
	long VOTE_KEY = AssetCls.ERM_KEY;
	byte FEE_POWER = (byte)1;
	byte[] noteReference = new byte[64];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private WalletDatabase database;
	private SecureWalletDatabase secureDatabase;
	private GenesisBlock gb;
	String password = "test";
	Wallet wallet;
	boolean create;
	
	// INIT NOTES
	private void init() {
		
		database = new WalletDatabase();
		secureDatabase = new SecureWalletDatabase(password);

		wallet = new Wallet();
		create = wallet.create(database, secureDatabase, Crypto.getInstance()
				.digest(password.getBytes()), 10, false);
	}

	@Test
	public void createWallet() throws Exception
	{
		//CREATE DATABASE
		//CREATE WALLET
		init();
	
		//CHECK CREATE
		assertEquals(true, create);
		
		//CHECK VERSION
		assertEquals(1, wallet.getVersion());
		
		//CHECK ADDRESSES
		assertEquals(10, wallet.getAccounts().size());
			
		//CHECK PRIVATE KEYS
		assertEquals(10, wallet.getprivateKeyAccounts().size());
		
		//CHECK LAST BLOCKS
		assertNotNull(wallet.getLastBlocks());
		
		//CHECK LAST TRANSACTIONS
		assertNotNull(wallet.getLastTransactions(100));
	}
	
	@Test
	public void lockUnlock()
	{
			
		//CREATE DATABASE
		//CREATE WALLET
		init();
		
		//CHECK UNLOCKED
		assertEquals(true, wallet.isUnlocked());
		
		//LOCK
		wallet.lock();
		
		//CHECK LOCKED
		assertEquals(false, wallet.isUnlocked());
		
		//CHECK ACCOUNTS
		assertEquals(null, wallet.getprivateKeyAccounts());
		
		//UNLOCK
		wallet.unlock(secureDatabase);
		
		//CHECK UNLOCKED
		assertEquals(true, wallet.isUnlocked());
		
		//CHECK ACCOUNTS
		assertEquals(10, wallet.getprivateKeyAccounts().size());
	}
	
	
}
