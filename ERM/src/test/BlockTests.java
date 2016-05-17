package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import org.apache.log4j.Logger;

import ntp.NTP;

import org.junit.Test;

import core.BlockGenerator;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.BlockFactory;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.GenesisIssuePersonRecord;
import core.transaction.GenesisTransferAssetTransaction;
import core.transaction.Transaction;
import core.transaction.R_Send;
import core.web.blog.BlogEntry;
import database.DBSet;

public class BlockTests
{
	long ERM_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	byte FEE_POWER = (byte)0;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db = DBSet.createEmptyDatabaseSet();
	private GenesisBlock gb = new GenesisBlock();

	static Logger LOGGER = Logger.getLogger(BlockTests.class.getName());

	@Test
	public void validateSignatureGenesisBlock()
	{
		
		//CHECK IF SIGNATURE VALID
		LOGGER.info("getGeneratorSignature " + gb.getGeneratorSignature().length
				+ " : " + gb.getGeneratorSignature());

		LOGGER.info("getGeneratorSignature " + gb.getGeneratorSignature().length
				+ " : " + gb.getGeneratorSignature());

		assertEquals(true, gb.isSignatureValid());
		
		//ADD TRANSACTION SIGNATURE
		LOGGER.info("getGeneratorSignature " + gb.getGeneratorSignature());

		//ADD a GENESIS TRANSACTION for invalid SIGNATURE
		gb.addTransaction( new GenesisTransferAssetTransaction(
				new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"), 0l, BigDecimal.valueOf(1).setScale(8)));

		assertEquals(false, gb.isSignatureValid());

	}
	
	@Test
	public void validateGenesisBlock()
	{
				
		//CHECK IF VALID
		assertEquals(true, gb.isValid(db));
		
		//ADD INVALID GENESIS TRANSACTION
		gb.addTransaction( new GenesisTransferAssetTransaction(
				new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g"), 0l, BigDecimal.valueOf(-1000).setScale(8)));
		
		//CHECK IF INVALID
		assertEquals(false, gb.isValid(db));
		
		//CREATE NEW BLOCK
		gb = new GenesisBlock();
		
		//CHECK IF VALID
		assertEquals(true, gb.isValid(db));
		
		//PROCESS
		gb.process(db);
		
		//CHECK IF INVALID
		assertEquals(false, gb.isValid(db));
	}
	
	@Test
	public void parseGenesisBlock()
	{
		//gb.process();
				
		//CONVERT TO BYTES
		byte[] rawBlock = gb.toBytes();
		//CHECK length
		assertEquals(rawBlock.length, gb.getDataLength());
			
		Block parsedBlock = null;
		try 
		{	
			//PARSE FROM BYTES
			parsedBlock = BlockFactory.getInstance().parse(rawBlock);		
					
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
		}
		
		//CHECK length
		assertEquals(rawBlock.length, parsedBlock.getDataLength());

		//CHECK SIGNATURE
		assertEquals(true, Arrays.equals(gb.getSignature(), parsedBlock.getSignature()));
				
		//CHECK BASE TARGET
		assertEquals(gb.getGeneratingBalance(), parsedBlock.getGeneratingBalance());	
		
		//CHECK FEE
		assertEquals(gb.getTotalFee(), parsedBlock.getTotalFee());	

		//CHECK TIMESTAMP
		assertEquals(gb.getTimestamp(), parsedBlock.getTimestamp());

		//CHECK TRANSACTION COUNT
		assertEquals(gb.getTransactionCount(), parsedBlock.getTransactionCount());

		//CHECK REFERENCE
		assertEquals(true, Arrays.equals(gb.getReference(), parsedBlock.getReference()));			

		//CHECK GENERATOR
		assertEquals(gb.getGenerator().getAddress(), parsedBlock.getGenerator().getAddress());	
				
		//CHECK INSTANCE
		////assertEquals(true, parsedBlock instanceof GenesisBlock);
				
		//PARSE TRANSACTION FROM WRONG BYTES
		rawBlock = new byte[50];
		
		try 
		{	
			//PARSE FROM BYTES
			BlockFactory.getInstance().parse(rawBlock);
					
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void validateSignatureBlock()
	{
		gb.process(db);
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastReference(gb.getGeneratorSignature(), db);
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), db);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1000).setScale(8), db); // need for payments

		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block newBlock = blockGenerator.generateNextBlock(db, generator, gb);
		
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getGeneratorSignature());
		newBlock.setTransactionsSignature(transactionsSignature);
		
		//CHECK IF SIGNATURE VALID
		assertEquals(true, newBlock.isSignatureValid());

		//INVALID TRANSACTION SIGNATURE
		transactionsSignature = new byte[64];
		newBlock.setTransactionsSignature(transactionsSignature);
		
		//CHECK IF SIGNATURE INVALID
		assertEquals(false, newBlock.isSignatureValid());
		
		//INVALID GENERATOR SIGNATURE
		newBlock = BlockFactory.getInstance().create(newBlock.getVersion(), newBlock.getReference(), newBlock.getTimestamp(), newBlock.getGeneratingBalance(), generator, new byte[32], null, 0);
		transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getGeneratorSignature());
		newBlock.setTransactionsSignature(transactionsSignature);
		
		///CHECK IF SIGNATURE INVALID
		assertEquals(false, newBlock.isSignatureValid());
		
		//VALID TRANSACTION SIGNATURE
		newBlock = blockGenerator.generateNextBlock(db, generator, gb);	
		
		//ADD TRANSACTION
		Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
		long timestamp = newBlock.getTimestamp();
		Transaction payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(8), timestamp, generator.getLastReference(db));
		payment.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment.isValid(db, null));
		newBlock.addTransaction(payment);
		
		//ADD TRANSACTION SIGNATURE
		transactionsSignature = blockGenerator.calculateTransactionsSignature(newBlock, generator);
		newBlock.setTransactionsSignature(transactionsSignature);
		
		//CHECK VALID TRANSACTION SIGNATURE
		assertEquals(true, newBlock.isSignatureValid());	
		
		//INVALID TRANSACTION SIGNATURE
		newBlock = blockGenerator.generateNextBlock(db, generator, gb);	
		
		//ADD TRANSACTION
		payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(200).setScale(8), NTP.getTime(), generator.getLastReference(db), payment.getSignature());
		newBlock.addTransaction(payment);
				
		//ADD TRANSACTION SIGNATURE
		transactionsSignature = blockGenerator.calculateTransactionsSignature(newBlock, generator);
		newBlock.setTransactionsSignature(transactionsSignature);
		
		//CHECK INVALID TRANSACTION SIGNATURE
		assertEquals(false, newBlock.isSignatureValid());	
	}
	
	@Test
	public void validateBlock()
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
				
		Transaction transaction;
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		/*
		transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		*/
		
		// (issuer, recipient, 0l, bdAmount, timestamp)
		// need add VOLUME for generating new block - 0l asset!
		transaction = new GenesisTransferAssetTransaction(generator,
				ERM_KEY, BigDecimal.valueOf(1000).setScale(8));
		transaction.process(databaseSet, false);
		transaction = new GenesisTransferAssetTransaction(generator,
				ERM_KEY, BigDecimal.valueOf(1000).setScale(8));
		transaction.process(databaseSet, false);
		
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
				
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getGeneratorSignature());
		newBlock.setTransactionsSignature(transactionsSignature);
		
		//CHECK IF VALID
		/* !!! in core.block.Block.isValid(DBSet) - comment:
				//CHECK IF TIMESTAMP IS VALID -500 MS ERROR MARGIN TIME
				if(this.timestamp - 500 > NTP.getTime() || this.timestamp < this.getParent(db).timestamp)
		*/
		//assertEquals(true, newBlock.isValid(databaseSet));
		
		//CHANGE REFERENCE
		Block invalidBlock = BlockFactory.getInstance().create(newBlock.getVersion(), new byte[128], newBlock.getTimestamp(), newBlock.getGeneratingBalance(), newBlock.getGenerator(), newBlock.getGeneratorSignature(), null, 0);
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(databaseSet));
		
		//CHANGE TIMESTAMP
		invalidBlock = BlockFactory.getInstance().create(newBlock.getVersion(), newBlock.getReference(), 1L, newBlock.getGeneratingBalance(), newBlock.getGenerator(), newBlock.getGeneratorSignature(), null, 0);
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(databaseSet));
		
		//CHANGE BASETARGET
		invalidBlock = BlockFactory.getInstance().create(newBlock.getVersion(), newBlock.getReference(), newBlock.getTimestamp(), 1L, newBlock.getGenerator(), newBlock.getGeneratorSignature(), null, 0);
				
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(databaseSet));
		
		//ADD INVALID TRANSACTION
		invalidBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
		long timestamp = newBlock.getTimestamp();
		Transaction payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(-100).setScale(8), timestamp, generator.getLastReference(databaseSet));
		payment.sign(generator, false);
		invalidBlock.addTransaction(payment);		
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(databaseSet));
		
		//ADD GENESIS TRANSACTION
		invalidBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
		//transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), newBlock.getTimestamp());
		transaction = new GenesisIssueAssetTransaction(GenesisBlock.makeAsset(Transaction.RIGHTS_KEY));
		invalidBlock.addTransaction(transaction);	
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(databaseSet));
	}
	
	@Test
	public void parseBlock()
	{
		//CREATE EMPTY MEMORY DATABASE
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
		generator.setLastReference(genesisBlock.getGeneratorSignature(), databaseSet);
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), databaseSet);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1000).setScale(8), databaseSet);

								
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block block = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
						
		//FORK
		DBSet fork = databaseSet.fork();
				
		//GENERATE PAYMENT 1
		Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
		long timestamp = block.getTimestamp();
		Transaction payment1 = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(8), timestamp, generator.getLastReference(databaseSet));
		payment1.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment1.isValid(fork, null));
		
		//payment1.process(fork);
		block.addTransaction(payment1);	
				
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("7AfGz1FJ6tUnxxKSAHfcjroFEm8jSyVm7r");
		Transaction payment2 = new R_Send(generator, FEE_POWER, recipient2, FEE_KEY, BigDecimal.valueOf(100).setScale(8), timestamp, generator.getLastReference(fork));
		payment2.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment2.isValid(fork, null));
		
		block.addTransaction(payment2);	
						
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = Crypto.getInstance().sign(generator, block.getGeneratorSignature());
		block.setTransactionsSignature(transactionsSignature);
				
		//CONVERT TO BYTES
		byte[] rawBlock = block.toBytes();
				
		try 
		{	
			//PARSE FROM BYTES
			Block parsedBlock = BlockFactory.getInstance().parse(rawBlock);
					
			//CHECK INSTANCE
			assertEquals(false, parsedBlock instanceof GenesisBlock);
					
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(block.getSignature(), parsedBlock.getSignature()));
					
			//CHECK GENERATOR
			assertEquals(block.getGenerator().getAddress(), parsedBlock.getGenerator().getAddress());	
					
			//CHECK BASE TARGET
			assertEquals(block.getGeneratingBalance(), parsedBlock.getGeneratingBalance());	
			
			//CHECK FEE
			assertEquals(block.getTotalFee(), parsedBlock.getTotalFee());	
					
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(block.getReference(), parsedBlock.getReference()));	
					
			//CHECK TIMESTAMP
			assertEquals(block.getTimestamp(), parsedBlock.getTimestamp());		
			
			//CHECK TRANSACTIONS COUNT
			assertEquals(block.getTransactionCount(), parsedBlock.getTransactionCount());		
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
				
		//PARSE TRANSACTION FROM WRONG BYTES
		rawBlock = new byte[50];
		
		try 
		{	
			//PARSE FROM BYTES
			BlockFactory.getInstance().parse(rawBlock);
					
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}			
	}
	
	@Test
	public void processBlock()
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
										
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
										
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
												
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS for generate
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastReference(genesisBlock.getGeneratorSignature(), databaseSet);
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), databaseSet);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1000).setScale(8), databaseSet);
								
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block block = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
		//FORK
		DBSet fork = databaseSet.fork();
		
		//GENERATE PAYMENT 1
		Account recipient = new Account("7AfGz1FJ6tUnxxKSAHfcjroFEm8jSyVm7r");
		long timestamp = block.getTimestamp();
		Transaction payment1 = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(8), timestamp, generator.getLastReference(databaseSet));
		payment1.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment1.isValid(fork, null));

		payment1.process(fork, false);
		block.addTransaction(payment1);	
		
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("7Dwjk4TUB74CqW6PqfDQF1siXquK48HSPB");
		Transaction payment2 = new R_Send(generator, FEE_POWER, recipient2, ERM_KEY, BigDecimal.valueOf(10).setScale(8),  timestamp, generator.getLastReference(fork));
		payment2.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment2.isValid(fork, null));
		block.addTransaction(payment2);	
		
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = blockGenerator.calculateTransactionsSignature(block, generator);
		block.setTransactionsSignature(transactionsSignature);
		
		//CHECK VALID
		assertEquals(true, block.isSignatureValid());
		//assertEquals(true, block.isValid(databaseSet)); // comment if(this.timestamp - 500 > NTP.getTime() || this.timestamp < this.getParent(db).timestamp)
		
		//PROCESS BLOCK
		block.process(databaseSet);
		
		//CHECK BALANCE GENERATOR
		assertEquals(generator.getConfirmedBalance(ERM_KEY, databaseSet), BigDecimal.valueOf(990).setScale(8));
		assertEquals(generator.getConfirmedBalance(FEE_KEY, databaseSet), BigDecimal.valueOf(900).setScale(8));
		
		//CHECK LAST REFERENCE GENERATOR
		assertEquals(true, Arrays.equals(generator.getLastReference(databaseSet), payment2.getSignature()));
		
		//CHECK BALANCE RECIPIENT 1
		assertEquals(recipient.getConfirmedBalance(ERM_KEY, databaseSet), BigDecimal.valueOf(0).setScale(8));
		assertEquals(recipient.getConfirmedBalance(FEE_KEY, databaseSet), BigDecimal.valueOf(100).setScale(8));
		
		//CHECK LAST REFERENCE RECIPIENT 1
		assertEquals(true, Arrays.equals(recipient.getLastReference(databaseSet), payment1.getSignature()));
		
		//CHECK BALANCE RECIPIENT2
		assertEquals(recipient2.getConfirmedBalance(ERM_KEY, databaseSet), BigDecimal.valueOf(10).setScale(8));
		assertEquals(recipient2.getConfirmedBalance(FEE_KEY, databaseSet), BigDecimal.valueOf(0).setScale(8));
				
		//CHECK LAST REFERENCE RECIPIENT 2
		assertEquals(false, Arrays.equals(recipient2.getLastReference(databaseSet), payment2.getSignature()));
		
		//CHECK TOTAL FEE
		assertEquals(block.getTotalFee(), BigDecimal.valueOf(0.00000622).setScale(8));
		
		//CHECK TOTAL TRANSACTIONS
		assertEquals(2, block.getTransactionCount());
		
		//CHECK LAST BLOCK
		assertEquals(true, Arrays.equals(block.getSignature(), databaseSet.getBlockMap().getLastBlock().getSignature()));
	}
	
	@Test
	public void orphanBlock()
	{
										
		//PROCESS GENESISBLOCK
		gb.process(db);
										
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
												
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10000).setScale(8), db);

		// FEE FUND
		generator.setLastReference(gb.getGeneratorSignature(), db);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);
										
		//FORK
		DBSet fork = db.fork();
		
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block block = blockGenerator.generateNextBlock(db, generator, gb);

		//GENERATE PAYMENT 1
		Account recipient = new Account("7AfGz1FJ6tUnxxKSAHfcjroFEm8jSyVm7r");
		long blockTimestamp = block.getTimestamp();
		recipient.setLastReference(gb.getGeneratorSignature(), fork);
		recipient.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), fork);
		
		Transaction payment1 = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(0.1).setScale(8), blockTimestamp, generator.getLastReference(db));
		payment1.sign(generator, false);
		
		payment1.process(fork, false);
		block.addTransaction(payment1);	
		
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("7Dwjk4TUB74CqW6PqfDQF1siXquK48HSPB");
		recipient2.setLastReference(gb.getGeneratorSignature(), fork);
		recipient2.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), fork);
		
		Transaction payment2 = new R_Send(generator, FEE_POWER, recipient2, FEE_KEY, BigDecimal.valueOf(0.2).setScale(8), blockTimestamp, generator.getLastReference(fork));
		payment2.sign(generator, false);
		
		block.addTransaction(payment2);	
		
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = blockGenerator.calculateTransactionsSignature(block, generator);
		block.setTransactionsSignature(transactionsSignature);
		
		//CHECK VALID
		assertEquals(true, block.isSignatureValid());

		/*
		try {
			Thread.sleep(1000);
		}
		catch (Exception e) {}
		long diff = (long)blockTimestamp - (long)NTP.getTime();
		assertEquals(diff, 500); // тут нужен свежий блок - но генесиз давно сделан и блок тоже будет сделан давно этот
		//assertEquals(false, (long)block.getTimestamp() - 500 > (long)NTP.getTime());
		 */
		//assertEquals(true, block.isValid(db));
		
		//PROCESS BLOCK
		block.process(db);
		
		//ORPHAN BLOCK
		block.orphan(db);
		
		//CHECK BALANCE GENERATOR
		assertEquals(generator.getConfirmedBalance(FEE_KEY, db), BigDecimal.valueOf(1).setScale(8));
		
		//CHECK LAST REFERENCE GENERATOR
		assertEquals(true, Arrays.equals(generator.getLastReference(db), gb.getGeneratorSignature()));
		
		//CHECK BALANCE RECIPIENT
		assertEquals(recipient.getConfirmedBalance(FEE_KEY, db), BigDecimal.valueOf(0).setScale(8));
		
		//CHECK LAST REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(recipient.getLastReference(db), payment1.getSignature()));
		
		//CHECK BALANCE RECIPIENT2
		assertEquals(true, recipient2.getConfirmedBalance(FEE_KEY, db).compareTo(BigDecimal.valueOf(0)) == 0);
				
		//CHECK LAST REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(recipient2.getLastReference(db), new byte[0]));
		
		//CHECK LAST BLOCK
		assertEquals(true, Arrays.equals(gb.getSignature(), db.getBlockMap().getLastBlock().getSignature()));
	}
}
