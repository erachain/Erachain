package test.blocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.BlockChain;
import core.BlockGenerator;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.BlockFactory;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.GenesisTransferAssetTransaction;
import core.transaction.R_Send;
import core.transaction.Transaction;
import datachain.DCSet;
import ntp.NTP;

public class BlockTests
{
	long ERM_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	byte FEE_POWER = (byte)0;
	byte[] assetReference = new byte[Crypto.SIGNATURE_LENGTH];
	long timestamp = NTP.getTime();
	
	long flags = 0l;

	boolean forDB = true;
	//CREATE EMPTY MEMORY DATABASE
	private DCSet db = DCSet.createEmptyDatabaseSet();
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

		Controller.getInstance().initBlockChain(db);
		gb = Controller.getInstance().getBlockChain().getGenesisBlock();

		gbTransactions = gb.getTransactions();

		generator.setLastTimestamp(gb.getTimestamp(db), db);
		generator.changeBalance(db, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		generator.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false); // need for payments
	}

	private void initTrans(List<Transaction> transactions, long timestamp) {
		payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, generator.getLastTimestamp(db));
		payment.sign(generator, false);
		transactions.add(payment);

	}

	@Test
	public void validateSignatureGenesisBlock()
	{

		//CHECK IF SIGNATURE VALID
		LOGGER.info("getGeneratorSignature " + gb.getSignature().length
				+ " : " + gb.getSignature());

		assertEquals(true, gb.isSignatureValid());

		//ADD TRANSACTION SIGNATURE
		LOGGER.info("getGeneratorSignature " + gb.getSignature());

		//ADD a GENESIS TRANSACTION for invalid SIGNATURE
		List<Transaction> transactions = gb.getTransactions();
		transactions.add( new GenesisTransferAssetTransaction(
				new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"), 1l, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
		gb.setTransactions(transactions);

		// SIGNATURE invalid
		assertEquals(false, gb.isSignatureValid());

		assertEquals(true, gb.isValid(db, false));

	}

	@Test
	public void validateGenesisBlock()
	{

		//CHECK IF VALID
		assertEquals(true, gb.isValid(db, false));

		//ADD INVALID GENESIS TRANSACTION
		List<Transaction> transactions = gb.getTransactions();
		transactions.add( new GenesisTransferAssetTransaction(
				new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ"), 1l, BigDecimal.valueOf(-1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
		gb.setTransactions(transactions);

		//CHECK IF INVALID
		assertEquals(false, gb.isValid(db, false));

		//CREATE NEW BLOCK
		gb = new GenesisBlock();

		//CHECK IF VALID
		assertEquals(true, gb.isValid(db, false));

		//PROCESS
		try {
			gb.process(db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//CHECK IF INVALID
		assertEquals(false, gb.isValid(db, false));
	}

	@Test
	public void parseGenesisBlock()
	{
		//gb.process();

		//CONVERT TO BYTES
		byte[] rawBlock = gb.toBytes(true, forDB);
		//CHECK length
		assertEquals(rawBlock.length, gb.getDataLength(forDB));

		Block parsedBlock = null;
		try
		{
			//PARSE FROM BYTES
			parsedBlock = BlockFactory.getInstance().parse(rawBlock, forDB);

		}
		catch (Exception e)
		{
			fail("Exception while parsing transaction." + e);
		}

		//CHECK length
		assertEquals(rawBlock.length, parsedBlock.getDataLength(forDB));

		//CHECK SIGNATURE
		assertEquals(true, Arrays.equals(gb.getSignature(), parsedBlock.getSignature()));

		//CHECK BASE TARGET
		assertEquals(gb.getForgingValue(), parsedBlock.getForgingValue());

		//CHECK FEE
		assertEquals(gb.getTotalFee(), parsedBlock.getTotalFee());

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
			BlockFactory.getInstance().parse(rawBlock, forDB);

			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e)
		{
			//EXCEPTION IS THROWN OK
		}
	}

	@Test
	public void processGenesisBlock()
	{

		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();

		//CHECK VALID
		assertEquals(true, genesisBlock.isSignatureValid());
		assertEquals(true, genesisBlock.isValid(db, false));

		//PROCESS BLOCK
		try {
			genesisBlock.process(db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Account recipient1 = new Account("73CcZe3PhwvqMvWxDznLAzZBrkeTZHvNzo");
		Account recipient2 = new Account("7FUUEjDSo9J4CYon4tsokMCPmfP4YggPnd");

		//CHECK LAST REFERENCE GENERATOR
		assertEquals((long)recipient1.getLastTimestamp(db), 0);
		assertEquals((long)recipient2.getLastTimestamp(db), 0);

		//CHECK BALANCE RECIPIENT 1
		assertEquals(0, recipient1.getBalanceUSE(ERM_KEY, db).compareTo(BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
		assertEquals(0, recipient1.getBalanceUSE(FEE_KEY, db).compareTo(BigDecimal.valueOf(0.0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));

		//CHECK BALANCE RECIPIENT2
		assertEquals(0, recipient2.getBalanceUSE(ERM_KEY, db).compareTo(BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
		assertEquals(0, recipient2.getBalanceUSE(FEE_KEY, db).compareTo(BigDecimal.valueOf(0.0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));

		int height = genesisBlock.getHeight(db) + 1;
		Tuple2<Integer, Integer> forgingData = recipient1.getForgingData(db, height);
		assertEquals(-1, (int)forgingData.a);

		forgingData = recipient2.getForgingData(db, height);
		assertEquals(-1, (int)forgingData.a);

		//ORPHAN BLOCK
		try {
			genesisBlock.orphan(db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(true, recipient1.getLastTimestamp(db) == 0l);
		assertEquals(true, recipient2.getLastTimestamp(db) == 0l);

		//CHECK BALANCE RECIPIENT 1
		assertEquals(recipient1.getBalanceUSE(ERM_KEY, db), BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));
		assertEquals(recipient1.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));
		//CHECK BALANCE RECIPIENT 2
		assertEquals(true, recipient2.getBalanceUSE(ERM_KEY, db).compareTo(BigDecimal.valueOf(0)) == 0);
		assertEquals(true, recipient2.getBalanceUSE(FEE_KEY, db).compareTo(BigDecimal.valueOf(0)) == 0);

	}

	////////////////
	@Test
	public void validateSignatureBlock()
	{

		init();
		try {
			gb.process(db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), NTP.getTime());
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
		Transaction payment = new R_Send(generator, FEE_POWER, generator, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, generator.getLastTimestamp(db));
		payment.sign(generator, false);
		transactions.add(payment);

		// SET TRANSACTIONS to BLOCK
		newBlock.setTransactions(transactions);

		//CHECK IF SIGNATURE INVALID
		assertEquals(false, newBlock.isSignatureValid());

		//INVALID GENERATOR SIGNATURE
		newBlock = BlockFactory.getInstance().create(newBlock.getVersion(), newBlock.getReference(), generator, new byte[Crypto.HASH_LENGTH], new byte[0]);
		newBlock.sign(generator);
		newBlock.setTransactions(transactions);

		///CHECK IF SIGNATURE INVALID
		assertEquals(false, newBlock.isSignatureValid());

		//VALID TRANSACTION SIGNATURE
		newBlock = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);

		//ADD TRANSACTION
		Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
		long timestamp = newBlock.getTimestamp(db);
		payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, generator.getLastTimestamp(db));
		payment.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment.isValid(null, flags));
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
		payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(200).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), NTP.getTime(), generator.getLastTimestamp(db), payment.getSignature());
		transactions = new ArrayList<Transaction>();
		transactions.add(payment);

		//ADD TRANSACTION SIGNATURE
		newBlock.setTransactions(transactions);
		newBlock.sign(generator);

		//CHECK INVALID TRANSACTION SIGNATURE
		assertEquals(false, newBlock.isValid(db, false));
		// BUT valid HERE
		assertEquals(true, newBlock.isSignatureValid());
	}

	@Test
	public void validateBlock()
	{
		init();
		try {
			gb.process(db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);

		Transaction transaction;

		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		/*
		transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), NTP.getTime());
		transaction.process(databaseSet);
		 */

		// (issuer, recipient, 0l, bdAmount, timestamp)
		// need add VOLUME for generating new block - 0l asset!
		transaction = new GenesisTransferAssetTransaction(generator,
				ERM_KEY, BigDecimal.valueOf(100000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));
		transaction.process(gb, false);
		transaction = new GenesisTransferAssetTransaction(generator,
				FEE_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));
		transaction.process(gb, false);

		//GENERATE NEXT BLOCK
		//BigDecimal genBal = generator.getGeneratingBalance(db);
		BlockGenerator blockGenerator = new BlockGenerator(false);
		Block newBlock = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);

		//ADD TRANSACTION SIGNATURE
		//byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
		newBlock.makeTransactionsHash();

		//CHECK IF VALID
		assertEquals(true, newBlock.isValid(db, false));

		//CHANGE REFERENCE
		Block invalidBlock = BlockFactory.getInstance().create(newBlock.getVersion(), new byte[128], newBlock.getCreator(), transactionsHash, atBytes);
		invalidBlock.sign(generator);

		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(db, false));

		//ADD INVALID TRANSACTION
		invalidBlock = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);
		Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
		long timestamp = newBlock.getTimestamp(db);
		Transaction payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(-100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, generator.getLastTimestamp(db));
		payment.sign(generator, false);

		transactions = new ArrayList<Transaction>();
		transactions.add(payment);

		//ADD TRANSACTION SIGNATURE
		invalidBlock.setTransactions(transactions);
		invalidBlock.sign(generator);

		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(db, false));

		//ADD GENESIS TRANSACTION
		invalidBlock = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);

		//transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), newBlock.getTimestamp());
		transaction = new GenesisIssueAssetTransaction(GenesisBlock.makeAsset(Transaction.RIGHTS_KEY));
		transactions.add(transaction);

		//ADD TRANSACTION SIGNATURE
		invalidBlock.setTransactions(transactions);
		invalidBlock.sign(generator);

		//CHECK IF INVALID
		assertEquals(false, invalidBlock.isValid(db, false));
	}

	@Test
	public void parseBlock()
	{
		init();
		try {
			gb.process(db);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);

		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastTimestamp(gb.getTimestamp(db), db);
		generator.changeBalance(db, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		generator.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);


		//GENERATE NEXT BLOCK
		Block block = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);

		//FORK
		DCSet fork = db.fork();

		//GENERATE PAYMENT 1
		Account recipient = new Account("7F9cZPE1hbzMT21g96U8E1EfMimovJyyJ7");
		long timestamp = block.getTimestamp(db);
		Transaction payment1 = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, generator.getLastTimestamp(db));
		payment1.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment1.isValid(null, flags));

		//payment1.process(fork);
		transactions = new ArrayList<Transaction>();
		transactions.add(payment1);


		//GENERATE PAYMENT 2
		Account recipient2 = new Account("7AfGz1FJ6tUnxxKSAHfcjroFEm8jSyVm7r");
		Transaction payment2 = new R_Send(generator, FEE_POWER, recipient2, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, generator.getLastTimestamp(fork));
		payment2.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment2.isValid(null, flags));

		transactions.add(payment2);

		//ADD TRANSACTION SIGNATURE
		block.setTransactions(transactions);
		block.sign(generator);

		//CONVERT TO BYTES
		byte[] rawBlock = block.toBytes(true, forDB);

		Block parsedBlock = null;
		try
		{
			//PARSE FROM BYTES
			parsedBlock = BlockFactory.getInstance().parse(rawBlock, forDB);
		}
		catch (Exception e)
		{
			fail("Exception while parsing transaction.");
		}

		//CHECK INSTANCE
		assertEquals(false, parsedBlock instanceof GenesisBlock);

		//CHECK SIGNATURE
		assertEquals(true, Arrays.equals(block.getSignature(), parsedBlock.getSignature()));

		//CHECK GENERATOR
		assertEquals(block.getCreator().getAddress(), parsedBlock.getCreator().getAddress());

		//CHECK BASE TARGET
		assertEquals(block.getForgingValue(), parsedBlock.getForgingValue());

		//CHECK FEE
		assertEquals(block.getTotalFee(db), parsedBlock.getTotalFee(db));

		//CHECK REFERENCE
		assertEquals(true, Arrays.equals(block.getReference(), parsedBlock.getReference()));

		//CHECK TIMESTAMP
		assertEquals(block.getTimestamp(db), parsedBlock.getTimestamp(db));

		//CHECK TRANSACTIONS COUNT
		assertEquals(block.getTransactionCount(), parsedBlock.getTransactionCount());

		//PARSE TRANSACTION FROM WRONG BYTES
		rawBlock = new byte[50];

		try
		{
			//PARSE FROM BYTES
			BlockFactory.getInstance().parse(rawBlock, forDB);

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

		init();
		// already processed gb.process(db);

		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);

		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS for generate
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastTimestamp(gb.getTimestamp(db), db);
		generator.changeBalance(db, false, ERM_KEY, BigDecimal.valueOf(100000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		generator.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);

		//GENERATE NEXT BLOCK
		Block block = BlockGenerator.generateNextBlock(db, generator, gb, transactionsHash);

		//FORK
		DCSet fork = db.fork();

		//GENERATE PAYMENT 1
		Account recipient1 = new Account("7JU8UTuREAJG2yht5ASn7o1Ur34P1nvTk5");
		// TIMESTAMP for records make lower
		long timestamp = block.getTimestamp(db) - 1000;
		Transaction payment1 = new R_Send(generator, FEE_POWER, recipient1, FEE_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp++, generator.getLastTimestamp(fork));
		payment1.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment1.isValid(null, flags));

		payment1.process(block, false);

		transactions.add(payment1);

		//GENERATE PAYMENT 2
		Account recipient2 = new Account("7G1G45RX4td59daBv6PoN84nAJA49NZ47i");
		Transaction payment2 = new R_Send(generator, FEE_POWER, recipient2, ERM_KEY,
				BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp++, generator.getLastTimestamp(fork));
		payment2.sign(generator, false);
		assertEquals(Transaction.VALIDATE_OK, payment2.isValid(null, flags));

		transactions.add(payment2);

		//ADD TRANSACTION SIGNATURE
		block.setTransactions(transactions);

		////generator.setLastForgingData(db, block.getHeightByParent(db));
		generator.setForgingData(db, block.getHeightByParent(db), payment2.getAmount().intValue());
		//block.setCalcGeneratingBalance(db);
		block.sign(generator);

		//CHECK VALID
		assertEquals(true, block.isSignatureValid());
		assertEquals(true, block.isValid(db, false));

		//PROCESS BLOCK
		try {
			block.process(db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//CHECK BALANCE GENERATOR
		assertEquals(generator.getBalanceUSE(ERM_KEY, db), BigDecimal.valueOf(100990).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));
		//assertEquals(generator.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(900.00009482).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));
		assertEquals(generator.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(1900.0000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));

		//CHECK LAST REFERENCE GENERATOR
		assertEquals((long)generator.getLastTimestamp(db), (long)payment2.getTimestamp());

		//CHECK BALANCE RECIPIENT 1
		assertEquals(recipient1.getBalanceUSE(ERM_KEY, db), BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));
		assertEquals(recipient1.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));

		//CHECK LAST REFERENCE RECIPIENT 1
		//assertEquals((long)recipient1.getLastReference(db), (long)payment1.getTimestamp());
		assertEquals((long)recipient1.getLastTimestamp(db), 0l);

		//CHECK BALANCE RECIPIENT2
		assertEquals(recipient2.getBalanceUSE(ERM_KEY, db), BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));
		assertEquals(recipient2.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));

		//CHECK LAST REFERENCE RECIPIENT 2
		assertNotEquals(recipient2.getLastTimestamp(db), payment2.getTimestamp());

		//CHECK TOTAL FEE
		assertEquals(block.getTotalFee(), BigDecimal.valueOf(0.00065536).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));

		//CHECK TOTAL TRANSACTIONS
		assertEquals(2, block.getTransactionCount());

		//CHECK LAST BLOCK
		assertEquals(true, Arrays.equals(block.getSignature(), db.getBlockMap().last().getSignature()));


		////////////////////////////////////
		//ORPHAN BLOCK
		//////////////////////////////////
		try {
			block.orphan(db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//CHECK BALANCE GENERATOR
		assertEquals(generator.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(2000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));

		//CHECK LAST REFERENCE GENERATOR
		assertEquals((long)generator.getLastTimestamp(db), gb.getTimestamp(db));

		//CHECK BALANCE RECIPIENT 1
		assertEquals(recipient1.getBalanceUSE(FEE_KEY, db), BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE));

		//CHECK LAST REFERENCE RECIPIENT 1
		assertNotEquals(recipient1.getLastTimestamp(db), payment1.getTimestamp());

		//CHECK BALANCE RECIPIENT 2
		assertEquals(true, recipient2.getBalanceUSE(FEE_KEY, db).compareTo(BigDecimal.valueOf(0)) == 0);

		//CHECK LAST REFERENCE RECIPIENT 2
		assertEquals((long)recipient2.getLastTimestamp(db), (long)0);

		//CHECK LAST BLOCK
		assertEquals(true, Arrays.equals(gb.getSignature(), db.getBlockMap().last().getSignature()));
	}
}
