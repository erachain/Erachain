package test.blocks;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
	byte[] assetReference = new byte[Crypto.SIGNATURE_LENGTH];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet db = DBSet.createEmptyDatabaseSet();
	private GenesisBlock gb = new GenesisBlock();

	List<Transaction> gbTransactions = gb.getTransactions();
	List<Transaction> transactions =  new ArrayList<Transaction>();
	byte[] transactionsHash =  new byte[Crypto.HASH_LENGTH];
	byte[] atBytes = new byte[0];

	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
	Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");

	Transaction payment;
	
	static Logger LOGGER = Logger.getLogger(BlockTests.class.getName());

	private void init() {
		//gb = new GenesisBlock();
		generator.setLastReference(gb.getTimestamp(), db);
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), db);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1000).setScale(8), db); // need for payments
	}
		
	private void initTrans(List<Transaction> transactions, long timestamp) {
		payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(8), timestamp, generator.getLastReference(db));
		payment.sign(generator, false);
		transactions.add(payment);

	}

	@Test
	public void validateSignatureGenesisBlock()
	{

		gb = new GenesisBlock();

		//CHECK IF SIGNATURE VALID
		LOGGER.info("getGeneratorSignature " + gb.getSignature().length
				+ " : " + gb.getSignature());

		assertEquals(true, gb.isSignatureValid());
		
		//ADD TRANSACTION SIGNATURE
		LOGGER.info("getGeneratorSignature " + gb.getSignature());

		//ADD a GENESIS TRANSACTION for invalid SIGNATURE
		List<Transaction> transactions = gb.getTransactions();
		transactions.add( new GenesisTransferAssetTransaction(
				new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"), 0l, BigDecimal.valueOf(1).setScale(8)));
		gb.setTransactions(transactions);
		
		// SIGNATURE invalid
		assertEquals(false, gb.isSignatureValid());		

		assertEquals(true, gb.isValid(db));

	}
	
	@Test
	public void validateGenesisBlock()
	{
				
		//CHECK IF VALID
		assertEquals(true, gb.isValid(db));
		
		//ADD INVALID GENESIS TRANSACTION
		List<Transaction> transactions = gb.getTransactions();
		transactions.add( new GenesisTransferAssetTransaction(
				new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"), 0l, BigDecimal.valueOf(-1000).setScale(8)));
		gb.setTransactions(transactions);
		
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
		byte[] rawBlock = gb.toBytes(true);
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
		assertEquals(gb.getCreator().getAddress(), parsedBlock.getCreator().getAddress());	
				
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
		
		init();
		gb.process(db);
				
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);

		//GENERATE NEXT BLOCK
		//BigDecimal genBal = generator.getGeneratingBalance(db);
		BlockGenerator blockGenerator = new BlockGenerator(false);
		Block newBlock = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);
		newBlock.sign(generator);
		
		////ADD TRANSACTION SIGNATURE
		///newBlock.makeTransactionsHash();
		
		//CHECK IF SIGNATURE VALID
		assertEquals(true, newBlock.isSignatureValid());

		//INVALID TRANSACTION HASH
		Transaction payment = new R_Send(generator, FEE_POWER, generator, FEE_KEY, BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(db));
		payment.sign(generator, false);
		transactions.add(payment);
		
		// SET TRANSACTIONS to BLOCK
		newBlock.setTransactions(transactions);
		
		//CHECK IF SIGNATURE INVALID
		assertEquals(false, newBlock.isSignatureValid());
		
		//INVALID GENERATOR SIGNATURE
		newBlock = BlockFactory.getInstance().create(newBlock.getVersion(), newBlock.getReference(), newBlock.getTimestamp(), generator, new byte[Crypto.HASH_LENGTH], new byte[0]);
		
		///CHECK IF SIGNATURE INVALID
		assertEquals(false, newBlock.isSignatureValid());
		
		//VALID TRANSACTION SIGNATURE
		newBlock = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);	
		
		//ADD TRANSACTION
		Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
		long timestamp = newBlock.getTimestamp();
		payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(8), timestamp, generator.getLastReference(db));
		payment.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment.isValid(db, null));
		transactions = new ArrayList<Transaction>();
		transactions.add(payment);
		
		//ADD TRANSACTION SIGNATURE
		newBlock.setTransactions(transactions);
		
		//CHECK VALID TRANSACTION SIGNATURE
		assertEquals(false, newBlock.isSignatureValid());

		newBlock.sign(generator);
		//CHECK VALID TRANSACTION SIGNATURE
		assertEquals(true, newBlock.isSignatureValid());
		
		//INVALID TRANSACTION SIGNATURE
		newBlock = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);	
		
		//ADD TRANSACTION
		payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(200).setScale(8), NTP.getTime(), generator.getLastReference(db), payment.getSignature());
		transactions = new ArrayList<Transaction>();
		transactions.add(payment);
				
		//ADD TRANSACTION SIGNATURE
		newBlock.setTransactions(transactions);
		newBlock.sign(generator);
		
		//CHECK INVALID TRANSACTION SIGNATURE
		assertEquals(false, newBlock.isValid(db));
		// BUT valid HERE
		assertEquals(true, newBlock.isSignatureValid());	
	}
	
	@Test
	public void validateBlock()
	{
		//CREATE EMPTY MEMORY DATABASE
		//DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(db);
						
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
				ERM_KEY, BigDecimal.valueOf(100000).setScale(8));
		transaction.process(db, false);
		transaction = new GenesisTransferAssetTransaction(generator,
				FEE_KEY, BigDecimal.valueOf(1000).setScale(8));
		transaction.process(db, false);
		
		//GENERATE NEXT BLOCK
		//BigDecimal genBal = generator.getGeneratingBalance(db);
		BlockGenerator blockGenerator = new BlockGenerator(false);
		Block newBlock = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);
				
		//ADD TRANSACTION SIGNATURE
		//byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
		newBlock.makeTransactionsHash();
		
		//CHECK IF VALID
		assertEquals(true, newBlock.isValid(db));
		
		//CHANGE REFERENCE
		Block invalidBlock = BlockFactory.getInstance().create(newBlock.getVersion(), new byte[128], newBlock.getTimestamp(), newBlock.getCreator(), transactionsHash, atBytes);
		invalidBlock.sign(generator);
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(db));
		
		//CHANGE TIMESTAMP
		invalidBlock = BlockFactory.getInstance().create(newBlock.getVersion(), newBlock.getReference(), 1L, newBlock.getCreator(), transactionsHash, atBytes);
		invalidBlock.sign(generator);
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(db));
				
		//ADD INVALID TRANSACTION
		invalidBlock = BlockGenerator.generateNextBlock(db, generator, genesisBlock, transactionsHash);
		Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
		long timestamp = newBlock.getTimestamp();
		Transaction payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(-100).setScale(8), timestamp, generator.getLastReference(db));
		payment.sign(generator, false);
		
		transactions = new ArrayList<Transaction>();
		transactions.add(payment);
				
		//ADD TRANSACTION SIGNATURE
		invalidBlock.setTransactions(transactions);
		invalidBlock.sign(generator);
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(db));
		
		//ADD GENESIS TRANSACTION
		invalidBlock = BlockGenerator.generateNextBlock(db, generator, genesisBlock, transactionsHash);
		
		//transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), newBlock.getTimestamp());
		transaction = new GenesisIssueAssetTransaction(GenesisBlock.makeAsset(Transaction.RIGHTS_KEY));
		transactions.add(transaction);
				
		//ADD TRANSACTION SIGNATURE
		invalidBlock.setTransactions(transactions);
		invalidBlock.sign(generator);
		
		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(db));
	}
	
	@Test
	public void parseBlock()
	{
		//CREATE EMPTY MEMORY DATABASE
		//DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(db);
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
										
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastReference(genesisBlock.getTimestamp(), db);
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), db);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1000).setScale(8), db);

								
		//GENERATE NEXT BLOCK
		Block block = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);
						
		//FORK
		DBSet fork = db.fork();
				
		//GENERATE PAYMENT 1
		Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
		long timestamp = block.getTimestamp();
		Transaction payment1 = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(8), timestamp, generator.getLastReference(db));
		payment1.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment1.isValid(fork, null));
		
		//payment1.process(fork);
		transactions = new ArrayList<Transaction>();
		transactions.add(payment1);

				
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("7AfGz1FJ6tUnxxKSAHfcjroFEm8jSyVm7r");
		Transaction payment2 = new R_Send(generator, FEE_POWER, recipient2, FEE_KEY, BigDecimal.valueOf(100).setScale(8), timestamp, generator.getLastReference(fork));
		payment2.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment2.isValid(fork, null));
		
		transactions.add(payment2);
						
		//ADD TRANSACTION SIGNATURE
		block.setTransactions(transactions);
		block.sign(generator);
				
		//CONVERT TO BYTES
		byte[] rawBlock = block.toBytes(true);
				
		try 
		{	
			//PARSE FROM BYTES
			Block parsedBlock = BlockFactory.getInstance().parse(rawBlock);
					
			//CHECK INSTANCE
			assertEquals(false, parsedBlock instanceof GenesisBlock);
					
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(block.getSignature(), parsedBlock.getSignature()));
					
			//CHECK GENERATOR
			assertEquals(block.getCreator().getAddress(), parsedBlock.getCreator().getAddress());	
					
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
										
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(db);
										
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
												
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS for generate
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastReference(genesisBlock.getTimestamp(), db);
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(1000).setScale(8), db);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1000).setScale(8), db);
								
		//GENERATE NEXT BLOCK
		Block block = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);
		
		//FORK
		DBSet fork = db.fork();
		
		//GENERATE PAYMENT 1
		Account recipient1 = new Account("7JU8UTuREAJG2yht5ASn7o1Ur34P1nvTk5");
		// TIMESTAMP for records make lower
		long timestamp = block.getTimestamp() - 1000;
		Transaction payment1 = new R_Send(generator, FEE_POWER, recipient1, FEE_KEY, BigDecimal.valueOf(100).setScale(8), timestamp++, generator.getLastReference(fork));
		payment1.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment1.isValid(fork, null));

		payment1.process(fork, false);
		
		transactions.add(payment1);
				
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("7G1G45RX4td59daBv6PoN84nAJA49NZ47i");
		Transaction payment2 = new R_Send(generator, FEE_POWER, recipient2, ERM_KEY, BigDecimal.valueOf(10).setScale(8),  timestamp++, generator.getLastReference(fork));
		payment2.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment2.isValid(fork, null));

		transactions.add(payment2);
		
		//ADD TRANSACTION SIGNATURE
		block.setTransactions(transactions);
		block.sign(generator);
		
		//CHECK VALID
		assertEquals(true, block.isSignatureValid());
		assertEquals(true, block.isValid(db));
		
		//PROCESS BLOCK
		block.process(db);
		
		//CHECK BALANCE GENERATOR
		assertEquals(generator.getConfirmedBalance(ERM_KEY, db), BigDecimal.valueOf(990).setScale(8));
		assertEquals(generator.getConfirmedBalance(FEE_KEY, db), BigDecimal.valueOf(900).setScale(8));
		
		//CHECK LAST REFERENCE GENERATOR
		assertEquals((long)generator.getLastReference(db), (long)payment2.getTimestamp());
		
		//CHECK BALANCE RECIPIENT 1
		assertEquals(recipient1.getConfirmedBalance(ERM_KEY, db), BigDecimal.valueOf(0).setScale(8));
		assertEquals(recipient1.getConfirmedBalance(FEE_KEY, db), BigDecimal.valueOf(100).setScale(8));
		
		//CHECK LAST REFERENCE RECIPIENT 1
		assertEquals((long)recipient1.getLastReference(db), (long)payment1.getTimestamp());
		
		//CHECK BALANCE RECIPIENT2
		assertEquals(recipient2.getConfirmedBalance(ERM_KEY, db), BigDecimal.valueOf(10).setScale(8));
		assertEquals(recipient2.getConfirmedBalance(FEE_KEY, db), BigDecimal.valueOf(0).setScale(8));
				
		//CHECK LAST REFERENCE RECIPIENT 2
		assertNotEquals(recipient2.getLastReference(db), payment2.getTimestamp());
		
		//CHECK TOTAL FEE
		assertEquals(block.getTotalFee(), BigDecimal.valueOf(0.00000518).setScale(8));
		
		//CHECK TOTAL TRANSACTIONS
		assertEquals(2, block.getTransactionCount());
		
		//CHECK LAST BLOCK
		assertEquals(true, Arrays.equals(block.getSignature(), db.getBlockMap().getLastBlock().getSignature()));
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
		generator.setLastReference(gb.getTimestamp(), db);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);
										
		//FORK
		DBSet fork = db.fork();
		
		//GENERATE NEXT BLOCK
		Block block = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);

		//GENERATE PAYMENT 1
		Account recipient1 = new Account("7JU8UTuREAJG2yht5ASn7o1Ur34P1nvTk5");
		long blockTimestamp = block.getTimestamp();
		recipient1.setLastReference(gb.getTimestamp(), fork);
		recipient1.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), fork);
		
		Transaction payment1 = new R_Send(generator, FEE_POWER, recipient1, FEE_KEY, BigDecimal.valueOf(0.1).setScale(8), blockTimestamp, generator.getLastReference(db));
		payment1.sign(generator, false);
		
		payment1.process(fork, false);
		
		transactions.add(payment1);
		
		//GENERATE PAYMENT 2
		Account recipient2 = new Account("7G1G45RX4td59daBv6PoN84nAJA49NZ47i");
		recipient2.setLastReference(gb.getTimestamp(), fork);
		recipient2.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), fork);
		
		Transaction payment2 = new R_Send(generator, FEE_POWER, recipient2, FEE_KEY, BigDecimal.valueOf(0.2).setScale(8), blockTimestamp, generator.getLastReference(fork));
		payment2.sign(generator, false);
		
		transactions.add(payment2);
		
		//ADD TRANSACTION SIGNATURE
		block.setTransactions(transactions);
		block.sign(generator);
		
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
		assertEquals((long)generator.getLastReference(db), gb.getTimestamp());
		
		//CHECK BALANCE RECIPIENT 1
		assertEquals(recipient1.getConfirmedBalance(FEE_KEY, db), BigDecimal.valueOf(0).setScale(8));
		
		//CHECK LAST REFERENCE RECIPIENT 1
		assertNotEquals(recipient1.getLastReference(db), payment1.getTimestamp());
		
		//CHECK BALANCE RECIPIENT 2
		assertEquals(true, recipient2.getConfirmedBalance(FEE_KEY, db).compareTo(BigDecimal.valueOf(0)) == 0);
				
		//CHECK LAST REFERENCE RECIPIENT 2
		assertEquals(recipient2.getLastReference(db), null);
		
		//CHECK LAST BLOCK
		assertEquals(true, Arrays.equals(gb.getSignature(), db.getBlockMap().getLastBlock().getSignature()));
	}
}
