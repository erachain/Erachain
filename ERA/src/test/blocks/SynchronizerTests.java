package test.blocks;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ntp.NTP;

import org.apache.log4j.Logger;
import org.junit.Test;

import controller.Controller;
import core.BlockGenerator;
import core.Synchronizer;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.transaction.Transaction;
import datachain.DCSet;
import core.transaction.GenesisTransferAssetTransaction;

public class SynchronizerTests {

	static Logger LOGGER = Logger.getLogger(SynchronizerTests.class.getName());

	long ERM_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	byte FEE_POWER = (byte)0;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();
	DCSet databaseSet = DCSet.createEmptyDatabaseSet();
	GenesisBlock genesisBlock = new GenesisBlock();
	
	byte[] transactionsHash =  new byte[Crypto.HASH_LENGTH];

	@Test
	public void synchronizeNoCommonBlock()
	{		
		//GENERATE 5 BLOCKS FROM ACCOUNT 1
		
		//PROCESS GENESISBLOCK
		try {
			genesisBlock.process(databaseSet);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.changeBalance(databaseSet, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(8));
		
		//GENERATE 5 NEXT BLOCKS
		Block lastBlock = genesisBlock;
		BlockGenerator blockGenerator = new BlockGenerator(false);
		List<Block> firstBlocks = new ArrayList<Block>();
		for(int i=0; i<5; i++)
		{	
			//GENERATE NEXT BLOCK
			Block newBlock = BlockGenerator.generateNextBlock(databaseSet, generator, lastBlock, transactionsHash);
			
			//ADD TRANSACTION SIGNATURE
			//byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
			newBlock.makeTransactionsHash();
			
			//PROCESS NEW BLOCK
			try {
				newBlock.process(databaseSet);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//ADD TO LIST
			firstBlocks.add(newBlock);
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}

		//GENERATE NEXT 5 BLOCK FROM ACCOUNT 2 ON FORK
		seed = Crypto.getInstance().digest("test2".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		generator = new PrivateKeyAccount(privateKey);
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.changeBalance(databaseSet, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(8));

		//FORK
		DCSet fork = databaseSet.fork();	
		
		//GENERATE NEXT 5 BLOCKS
		List<Block> newBlocks = new ArrayList<Block>();
		for(int i=0; i<5; i++)
		{	
			//GENERATE NEXT BLOCK
			Block newBlock = BlockGenerator.generateNextBlock(fork, generator, lastBlock, transactionsHash);
			
			//ADD TRANSACTION SIGNATURE
			//byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
			newBlock.makeTransactionsHash();
			
			//PROCESS NEW BLOCK
			try {
				newBlock.process(fork);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//ADD TO LIST
			newBlocks.add(newBlock);
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}		
		
		//SYNCHRONIZE DB FROM ACCOUNT 1 WITH NEXT 5 BLOCKS OF ACCOUNT 2
		Synchronizer synchronizer = new Synchronizer();
		
		try
		{
			synchronizer.synchronize_blocks(databaseSet, null, newBlocks, null);
			
			//CHECK LAST 5 BLOCKS
			lastBlock = databaseSet.getBlockMap().getLastBlock();
			for(int i=4; i>=0; i--)
			{
				//CHECK LAST BLOCK
				assertEquals(true, Arrays.equals(newBlocks.get(i).getSignature(), lastBlock.getSignature()));
				lastBlock = lastBlock.getParent(databaseSet);
			}
			
			//CHECK LAST 5 BLOCKS
			for(int i=4; i>=0; i--)
			{
				//CHECK LAST BLOCK
				assertEquals(true, Arrays.equals(firstBlocks.get(i).getSignature(), lastBlock.getSignature()));
				lastBlock = lastBlock.getParent(databaseSet);
			}
			
			//CHECK LAST BLOCK
			assertEquals(true, Arrays.equals(lastBlock.getSignature(), genesisBlock.getSignature()));
			
			//CHECK HEIGHT
			assertEquals(11, databaseSet.getBlockMap().getLastBlock().getHeight(databaseSet));
		}
		catch(Exception e)
		{
			fail("Exception during synchronize");
		}	
	}
	
	@Test
	public void synchronizeCommonBlock()
	{	
		
		//GENERATE 5 BLOCKS FROM ACCOUNT 1
		DCSet databaseSet1 = DCSet.createEmptyDatabaseSet();
		DCSet databaseSet2 = DCSet.createEmptyDatabaseSet();
		
		GenesisBlock gb1;
		GenesisBlock gb2;
		
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		try {
			Controller.getInstance().initBlockChain(databaseSet1);
			gb1 = Controller.getInstance().getBlockChain().getGenesisBlock();

			//Controller.getInstance().initBlockChain(databaseSet2);
			gb2 = Controller.getInstance().getBlockChain().getGenesisBlock();

			genesisBlock.process(databaseSet1);
			//genesisBlock.process(databaseSet2);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		//transaction.process(databaseSet2, false);
		generator.changeBalance(databaseSet1, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(8));
		generator.changeBalance(databaseSet1, false, FEE_KEY, BigDecimal.valueOf(10).setScale(8));
		generator.changeBalance(databaseSet2, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(8));
		generator.changeBalance(databaseSet2, false, FEE_KEY, BigDecimal.valueOf(10).setScale(8));

		
		//CREATE KNOWN ACCOUNT 2
		byte[] seed2 = Crypto.getInstance().digest("test2".getBytes());
		byte[] privateKey2 = Crypto.getInstance().createKeyPair(seed2).getA();
		PrivateKeyAccount generator2 = new PrivateKeyAccount(privateKey2);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR2 HAS FUNDS
		//transaction = new GenesisTransaction(generator2, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//GenesisTransferAssetTransaction transaction = new GenesisTransferAssetTransaction(generator2, ERM_KEY, BigDecimal.valueOf(1000).setScale(8));
		//transaction.process(databaseSet, false);
		//transaction.process(databaseSet2, false);
		generator2.changeBalance(databaseSet1, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(8));
		generator2.changeBalance(databaseSet2, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(8));
		
		
		//GENERATE 5 NEXT BLOCKS
		Block lastBlock = gb1;
		BlockGenerator blockGenerator = new BlockGenerator(false);
		for(int i=0; i<5; i++)
		{	
			//GENERATE NEXT BLOCK
			Block newBlock = blockGenerator.generateNextBlock(databaseSet1, generator, lastBlock, transactionsHash);
			
			//ADD TRANSACTION SIGNATURE
			//byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
			newBlock.makeTransactionsHash();
			
			//PROCESS NEW BLOCK
			try {
				newBlock.process(databaseSet1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			}
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}

		//GENERATE NEXT 10 BLOCKS
		lastBlock = gb2;
		List<Block> newBlocks = new ArrayList<Block>();
		for(int i=0; i<10; i++)
		{	
			//GENERATE NEXT BLOCK
			Block newBlock = BlockGenerator.generateNextBlock(databaseSet2, generator2, lastBlock, transactionsHash);
			
			//ADD TRANSACTION SIGNATURE
			//byte[] transactionsSignature = Crypto.getInstance().sign(generator2, newBlock.getSignature());
			newBlock.makeTransactionsHash();
			
			//PROCESS NEW BLOCK
			try {
				//newBlock.process(databaseSet2);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail(e.getMessage());
			}
			
			//ADD TO LIST
			newBlocks.add(newBlock);
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}		
		
		//SYNCHRONIZE DB FROM ACCOUNT 1 WITH NEXT 5 BLOCKS OF ACCOUNT 2
		Synchronizer synchronizer = new Synchronizer();
		
		try
		{
			synchronizer.synchronize_blocks(databaseSet1, gb1, newBlocks, null);
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(),e);
			e.printStackTrace();
			fail("Exception during synchronize:" + e.getMessage());
		}	
			
		//CHECK BLOCKS
		lastBlock = databaseSet1.getBlockMap().getLastBlock();
		for(int i=9; i>=0; i--)
		{
			//CHECK LAST BLOCK
			assertEquals(true, Arrays.equals(newBlocks.get(i).getSignature(), lastBlock.getSignature()));
			lastBlock = lastBlock.getParent(databaseSet1);
		}
		
		//CHECK LAST BLOCK
		assertEquals(true, Arrays.equals(lastBlock.getSignature(), gb1.getSignature()));
		
		//CHECK HEIGHT
		assertEquals(11, databaseSet1.getBlockMap().getLastBlock().getHeight(databaseSet1));
	}	
}
