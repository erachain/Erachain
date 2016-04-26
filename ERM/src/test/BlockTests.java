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
import core.transaction.GenesisTransferAssetTransaction;
import core.transaction.PaymentTransaction;
import core.transaction.Transaction;
import core.transaction.TransferAssetTransaction;
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
	private DBSet db;
	private GenesisBlock gb;

	static Logger LOGGER = Logger.getLogger(BlockTests.class.getName());

	@Test
	public void validateSignatureGenesisBlock()
	{
		
		//CREATE EMPTY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();

		Block genesisBlock = new GenesisBlock();
		//genesisBlock.process(databaseSet);
		
		//CHECK IF SIGNATURE VALID  this.transactionsSignature [B@5ecddf8f [B@6c629d6e
		// [B@5ecddf8f [B@6c629d6e
		LOGGER.info("getGeneratorSignature " + genesisBlock.getGeneratorSignature().length
				+ " : " + genesisBlock.getGeneratorSignature());

		LOGGER.info("getGeneratorSignature " + genesisBlock.getGeneratorSignature().length
				+ " : " + genesisBlock.getGeneratorSignature());

		assertEquals(true, genesisBlock.isSignatureValid());
		
		//ADD TRANSACTION SIGNATURE
		LOGGER.info("getGeneratorSignature " + genesisBlock.getGeneratorSignature());
		//newBlock.setTransactionsSignature(transactionsSignature);

	}
	
	@Test
	public void validateGenesisBlock()
	{
		//CREATE EMPTY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE GENESIS BLOCK
		Block genesisBlock = new GenesisBlock();
		
		//CHECK IF VALID
		assertEquals(true, genesisBlock.isValid(databaseSet));
		
		//ADD INVALID GENESIS TRANSACTION
		//Transaction transaction = new GenesisTransaction(new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g"), BigDecimal.valueOf(-1000).setScale(8), NTP.getTime());
		genesisBlock.addTransaction(new GenesisIssueAssetTransaction(GenesisBlock.makeAssetVenture(Transaction.RIGHTS_KEY)));
		
		//CHECK IF INVALID
		assertEquals(false, genesisBlock.isValid(databaseSet));
		
		//CREATE NEW BLOCK
		genesisBlock = new GenesisBlock();
		//databaseSet = DBSet.createEmptyDatabaseSet();
		//genesisBlock.process(databaseSet);
		
		//CHECK IF VALID
		assertEquals(true, genesisBlock.isValid(databaseSet));
		
		//PROCESS
		genesisBlock.process(databaseSet);
		
		//CHECK IF INVALID
		assertEquals(false, genesisBlock.isValid(databaseSet));
	}
	
	@Test
	public void parseGenesisBlock()
	{
		//CREATE VALID BLOCK
		Block genesisBlock = new GenesisBlock();
		//genesisBlock.process();
				
		//CONVERT TO BYTES
		byte[] rawBlock = genesisBlock.toBytes();
				
		try 
		{	
			//PARSE FROM BYTES
			Block parsedBlock = BlockFactory.getInstance().parse(rawBlock);		
					
			//CHECK length
			assertEquals(rawBlock.length, parsedBlock.getDataLength());

			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(genesisBlock.getSignature(), parsedBlock.getSignature()));
					
			//CHECK BASE TARGET
			assertEquals(genesisBlock.getGeneratingBalance(), parsedBlock.getGeneratingBalance());	
			
			//CHECK FEE
			assertEquals(genesisBlock.getTotalFee(), parsedBlock.getTotalFee());	

			//CHECK TIMESTAMP
			assertEquals(genesisBlock.getTimestamp(), parsedBlock.getTimestamp());

			//CHECK TRANSACTION COUNT
			assertEquals(genesisBlock.getTransactionCount(), parsedBlock.getTransactionCount());

			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(genesisBlock.getReference(), parsedBlock.getReference()));			

			//CHECK GENERATOR
			assertEquals(genesisBlock.getGenerator().getAddress(), parsedBlock.getGenerator().getAddress());	
					
			//CHECK INSTANCE
			assertEquals(true, parsedBlock instanceof GenesisBlock);

		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction." + e);
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
	public void validateSignatureBlock()
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
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10000).setScale(8), databaseSet);

		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
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
		newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);	
		
		//ADD TRANSACTION
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = newBlock.getTimestamp();
		Transaction payment = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(100).setScale(8), (byte)0, timestamp, generator.getLastReference(databaseSet));
		payment.sign(generator, false);
		newBlock.addTransaction(payment);
		
		//ADD TRANSACTION SIGNATURE
		transactionsSignature = blockGenerator.calculateTransactionsSignature(newBlock, generator);
		newBlock.setTransactionsSignature(transactionsSignature);
		
		//CHECK VALID TRANSACTION SIGNATURE
		assertEquals(true, newBlock.isSignatureValid());	
		
		//INVALID TRANSACTION SIGNATURE
		newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);	
		
		//ADD TRANSACTION
		payment = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(200).setScale(8), (byte)0, NTP.getTime(), generator.getLastReference(databaseSet), payment.getSignature());
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
				0l, BigDecimal.valueOf(1000).setScale(8));
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
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = newBlock.getTimestamp();
		Transaction payment = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(-100).setScale(8), (byte)0, timestamp, generator.getLastReference(databaseSet));
		payment.sign(generator, false);
		invalidBlock.addTransaction(payment);		
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(databaseSet));
		
		//ADD GENESIS TRANSACTION
		invalidBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
		//transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), newBlock.getTimestamp());
		transaction = new GenesisIssueAssetTransaction(GenesisBlock.makeAssetVenture(Transaction.RIGHTS_KEY));
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
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10000).setScale(8), databaseSet);

								
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block block = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
						
		//FORK
		DBSet fork = databaseSet.fork();
				
		//GENERATE PAYMENT 1
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = block.getTimestamp();
		Transaction payment1 = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(100).setScale(8), (byte)0, timestamp, generator.getLastReference(databaseSet));
		payment1.sign(generator, false);
		
		//payment1.process(fork);
		block.addTransaction(payment1);	
				
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("XLPYYfxKEiDcybCkFA7jXcxSdePMMoyZLt");
		Transaction payment2 = new PaymentTransaction(generator, recipient2, BigDecimal.valueOf(100).setScale(8), (byte)0, timestamp, generator.getLastReference(fork));
		payment2.sign(generator, false);
		
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
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10000).setScale(8), databaseSet);
								
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block block = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
		//FORK
		DBSet fork = databaseSet.fork();
		
		//GENERATE PAYMENT 1
		Account recipient = new Account("QaEx7otgPh61k5zg4f4zCRXXfEiVnXXMGM");
		long timestamp = block.getTimestamp();
		Transaction payment1 = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(100).setScale(8), (byte)0, timestamp, generator.getLastReference(databaseSet));
		payment1.sign(generator, false);
		
		payment1.process(fork, false);
		block.addTransaction(payment1);	
		
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("Qc14p8iokvDwCchc8CmUyBiJuj8wc4X63a");
		Transaction payment2 = new TransferAssetTransaction(generator, recipient2, FEE_KEY, BigDecimal.valueOf(1).setScale(8), (byte)0, timestamp, generator.getLastReference(fork));
		payment2.sign(generator, false);
		block.addTransaction(payment2);	
		
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = blockGenerator.calculateTransactionsSignature(block, generator);
		block.setTransactionsSignature(transactionsSignature);
		
		//CHECK VALID
		assertEquals(true, block.isSignatureValid());
		assertEquals(true, block.isValid(databaseSet)); // comment if(this.timestamp - 500 > NTP.getTime() || this.timestamp < this.getParent(db).timestamp)
		
		//PROCESS BLOCK
		block.process(databaseSet);
		
		//CHECK BALANCE GENERATOR
		assertEquals(generator.getConfirmedBalance(databaseSet), BigDecimal.valueOf(900.00000620).setScale(8));
		
		//CHECK LAST REFERENCE GENERATOR
		assertEquals(true, Arrays.equals(generator.getLastReference(databaseSet), payment2.getSignature()));
		
		//CHECK BALANCE RECIPIENT
		assertEquals(recipient.getConfirmedBalance(databaseSet), BigDecimal.valueOf(100).setScale(8));
		
		//CHECK LAST REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(recipient.getLastReference(databaseSet), payment1.getSignature()));
		
		//CHECK BALANCE RECIPIENT2
		assertEquals(recipient2.getConfirmedBalance(databaseSet), BigDecimal.valueOf(0).setScale(8));
				
		//CHECK LAST REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(recipient2.getLastReference(databaseSet), payment2.getSignature()));
		
		//CHECK TOTAL FEE
		assertEquals(block.getTotalFee(), BigDecimal.valueOf(0.0000062).setScale(8));
		
		//CHECK TOTAL TRANSACTIONS
		assertEquals(2, block.getTransactionCount());
		
		//CHECK LAST BLOCK
		assertEquals(true, Arrays.equals(block.getSignature(), databaseSet.getBlockMap().getLastBlock().getSignature()));
	}
	
	@Test
	public void orphanBlock()
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
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10000).setScale(8), databaseSet);

		// OIL FUND
		generator.setLastReference(genesisBlock.getGeneratorSignature(), databaseSet);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), databaseSet);
								
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block block = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
		//FORK
		DBSet fork = databaseSet.fork();
		
		//GENERATE PAYMENT 1
		Account recipient = new Account("QRqBjBJshFJig97ABKiPJ9ar86KbWEZ7Hc");
		long timestamp = block.getTimestamp();
		recipient.setLastReference(genesisBlock.getGeneratorSignature(), fork);
		recipient.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), fork);
		
		Transaction payment1 = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(100).setScale(8), (byte)0, timestamp, generator.getLastReference(databaseSet));
		payment1.sign(generator, false);
		
		payment1.process(fork, false);
		block.addTransaction(payment1);	
		
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("QQ7YLV7hcmAjcoYyosycsdEuhbN42M6TW1");
		recipient2.setLastReference(genesisBlock.getGeneratorSignature(), fork);
		recipient2.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), fork);
		
		Transaction payment2 = new PaymentTransaction(generator, recipient2, BigDecimal.valueOf(100).setScale(8), (byte)0, timestamp, generator.getLastReference(fork));
		payment2.sign(generator, false);
		
		block.addTransaction(payment2);	
		
		//ADD TRANSACTION SIGNATURE
		byte[] transactionsSignature = blockGenerator.calculateTransactionsSignature(block, generator);
		block.setTransactionsSignature(transactionsSignature);
		
		//CHECK VALID
		assertEquals(true, block.isSignatureValid());
		assertEquals(true, block.isValid(databaseSet));
		
		//PROCESS BLOCK
		block.process(databaseSet);
		
		//ORPHAN BLOCK
		block.orphan(databaseSet);
		
		//CHECK BALANCE GENERATOR
		assertEquals(true, generator.getConfirmedBalance(databaseSet).compareTo(BigDecimal.valueOf(1000)) == 0);
		
		//CHECK LAST REFERENCE GENERATOR
		assertEquals(true, Arrays.equals(generator.getLastReference(databaseSet), genesisBlock.getGeneratorSignature()));
		
		//CHECK BALANCE RECIPIENT
		assertEquals(true, recipient.getConfirmedBalance(databaseSet).compareTo(BigDecimal.valueOf(1000)) == 0);
		
		//CHECK LAST REFERENCE RECIPIENT
		assertEquals(false, Arrays.equals(recipient.getLastReference(databaseSet), payment1.getSignature()));
		
		//CHECK BALANCE RECIPIENT2
		assertEquals(true, recipient2.getConfirmedBalance(databaseSet).compareTo(BigDecimal.valueOf(0)) == 0);
				
		//CHECK LAST REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(recipient2.getLastReference(databaseSet), new byte[0]));
		
		//CHECK LAST BLOCK
		assertEquals(true, Arrays.equals(genesisBlock.getSignature(), databaseSet.getBlockMap().getLastBlock().getSignature()));
	}
}
