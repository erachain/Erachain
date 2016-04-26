package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ntp.NTP;

import org.apache.log4j.Logger;
import org.junit.Test;

import core.BlockGenerator;
import core.Synchronizer;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.transaction.Transaction;
import core.transaction.GenesisTransferAssetTransaction;
import database.DBSet;

public class SynchronizerTests {

	static Logger LOGGER = Logger.getLogger(SynchronizerTests.class.getName());

	long ERM_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	byte FEE_POWER = (byte)0;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();

	@Test
	public void synchronizeNoCommonBlock()
	{		
		//GENERATE 5 BLOCKS FROM ACCOUNT 1
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), databaseSet);
		
		//GENERATE 5 NEXT BLOCKS
		Block lastBlock = genesisBlock;
		BlockGenerator blockGenerator = new BlockGenerator();
		List<Block> firstBlocks = new ArrayList<Block>();
		for(int i=0; i<5; i++)
		{	
			//GENERATE NEXT BLOCK
			Block newBlock = blockGenerator.generateNextBlock(databaseSet, generator, lastBlock);
			
			//ADD TRANSACTION SIGNATURE
			byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getGeneratorSignature());
			newBlock.setTransactionsSignature(transactionsSignature);
			
			//PROCESS NEW BLOCK
			newBlock.process(databaseSet);
			
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
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), databaseSet);

		
		//FORK
		DBSet fork = databaseSet.fork();	
		
		//GENERATE NEXT 5 BLOCKS
		List<Block> newBlocks = new ArrayList<Block>();
		for(int i=0; i<5; i++)
		{	
			//GENERATE NEXT BLOCK
			Block newBlock = blockGenerator.generateNextBlock(fork, generator, lastBlock);
			
			//ADD TRANSACTION SIGNATURE
			byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getGeneratorSignature());
			newBlock.setTransactionsSignature(transactionsSignature);
			
			//PROCESS NEW BLOCK
			newBlock.process(fork);
			
			//ADD TO LIST
			newBlocks.add(newBlock);
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}		
		
		//SYNCHRONIZE DB FROM ACCOUNT 1 WITH NEXT 5 BLOCKS OF ACCOUNT 2
		Synchronizer synchronizer = new Synchronizer();
		
		try
		{
			synchronizer.synchronize(databaseSet, null, newBlocks);
			
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
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		DBSet databaseSet2 = DBSet.createEmptyDatabaseSet();
		
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
		genesisBlock.process(databaseSet2);
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		//transaction.process(databaseSet2, false);
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), databaseSet);
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), databaseSet2);

		
		//CREATE KNOWN ACCOUNT 2
		byte[] seed2 = Crypto.getInstance().digest("test2".getBytes());
		byte[] privateKey2 = Crypto.getInstance().createKeyPair(seed2).getA();
		PrivateKeyAccount generator2 = new PrivateKeyAccount(privateKey2);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR2 HAS FUNDS
		//transaction = new GenesisTransaction(generator2, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		GenesisTransferAssetTransaction transaction = new GenesisTransferAssetTransaction(generator2, ERM_KEY, BigDecimal.valueOf(1000).setScale(8));
		transaction.process(databaseSet, false);
		transaction.process(databaseSet2, false);
		//generator2.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10).setScale(8), databaseSet);
		//generator2.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10).setScale(8), databaseSet2);
		
		
		//GENERATE 5 NEXT BLOCKS
		Block lastBlock = genesisBlock;
		BlockGenerator blockGenerator = new BlockGenerator();
		for(int i=0; i<5; i++)
		{	
			//GENERATE NEXT BLOCK
			Block newBlock = blockGenerator.generateNextBlock(databaseSet, generator, lastBlock);
			
			//ADD TRANSACTION SIGNATURE
			byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getGeneratorSignature());
			newBlock.setTransactionsSignature(transactionsSignature);
			
			//PROCESS NEW BLOCK
			newBlock.process(databaseSet);
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}

		//GENERATE NEXT 10 BLOCKS
		lastBlock = genesisBlock;
		List<Block> newBlocks = new ArrayList<Block>();
		for(int i=0; i<10; i++)
		{	
			//GENERATE NEXT BLOCK
			Block newBlock = blockGenerator.generateNextBlock(databaseSet2, generator2, lastBlock);
			
			//ADD TRANSACTION SIGNATURE
			byte[] transactionsSignature = Crypto.getInstance().sign(generator2, newBlock.getGeneratorSignature());
			newBlock.setTransactionsSignature(transactionsSignature);
			
			//PROCESS NEW BLOCK
			newBlock.process(databaseSet2);
			
			//ADD TO LIST
			newBlocks.add(newBlock);
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}		
		
		//SYNCHRONIZE DB FROM ACCOUNT 1 WITH NEXT 5 BLOCKS OF ACCOUNT 2
		Synchronizer synchronizer = new Synchronizer();
		
		try
		{
			synchronizer.synchronize(databaseSet, genesisBlock, newBlocks);
			
			//CHECK BLOCKS
			lastBlock = databaseSet.getBlockMap().getLastBlock();
			for(int i=9; i>=0; i--)
			{
				//CHECK LAST BLOCK
				assertEquals(true, Arrays.equals(newBlocks.get(i).getSignature(), lastBlock.getSignature()));
				lastBlock = lastBlock.getParent(databaseSet);
			}
			
			//CHECK LAST BLOCK
			assertEquals(true, Arrays.equals(lastBlock.getSignature(), genesisBlock.getSignature()));
			
			//CHECK HEIGHT
			assertEquals(11, databaseSet.getBlockMap().getLastBlock().getHeight(databaseSet));
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(),e);
			fail("Exception during synchronize");
		}	
	}	
}
