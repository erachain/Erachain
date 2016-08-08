package test.blocks;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ntp.NTP;

import org.junit.Test;

import core.BlockGenerator;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.transaction.GenesisTransferAssetTransaction;
//import core.transaction.GenesisTransaction;
import core.transaction.R_Send;
import core.transaction.Transaction;
import database.DBSet;

public class GeneratorTests {

	long ERM_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	byte FEE_POWER = (byte)0;
	byte[] assetReference = new byte[Crypto.SIGNATURE_LENGTH];
	long timestamp = NTP.getTime();

	List<Transaction> transactions =  new ArrayList<Transaction>();
	byte[] transactionsHash =  new byte[Crypto.HASH_LENGTH];

	@Test
	public void generateNewBlock() 
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
		// AND LAST REFERENCE
		// AND WIN_DATA
		Transaction transaction = new GenesisTransferAssetTransaction(generator, ERM_KEY, BigDecimal.valueOf(1000).setScale(8));
		transaction.process(databaseSet, false);
		transaction = new GenesisTransferAssetTransaction(generator, FEE_KEY, BigDecimal.valueOf(10).setScale(8));
		transaction.process(databaseSet, false);
		
		//GENERATE 2000 NEXT BLOCKS
		Block lastBlock = genesisBlock;
		for(int i=0; i<2000; i++)
		{	
			
			if ( NTP.getTime() - lastBlock.getTimestamp() < Block.GENERATING_MIN_BLOCK_TIME) {
				break;
			}
			//GENERATE NEXT BLOCK
			Block newBlock = BlockGenerator.generateNextBlock(databaseSet, generator, lastBlock, transactionsHash);
			
			//ADD TRANSACTION SIGNATURE
			//byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
			newBlock.makeTransactionsHash();
			
			//CHECK IF BLOCK SIGNATURE IS VALID
			assertEquals(true, newBlock.isSignatureValid());
			
			//CHECK IF BLOCK IS VALID
			if (!newBlock.isValid(databaseSet))
				assertEquals(true, newBlock.isValid(databaseSet));
			
			//PROCESS NEW BLOCK
			newBlock.process(databaseSet);
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}
	}
	
	@Test
	public void addTransactions()
	{
		// TEN
		
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
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(100000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastReference(genesisBlock.getTimestamp(), databaseSet);
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10000).setScale(8), databaseSet);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(10000).setScale(8), databaseSet);

		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator(false);
		Block newBlock = BlockGenerator.generateNextBlock(databaseSet, generator, genesisBlock, transactionsHash);

		// get timestamp for block
		long timestamp = newBlock.getTimestamp() - Block.GENERATING_MIN_BLOCK_TIME / 2;

		//ADD 10 UNCONFIRMED VALID TRANSACTIONS	
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		DBSet snapshot = databaseSet.fork();
		for(int i=0; i<10; i++)
		{
				
			//CREATE VALID PAYMENT
			Transaction payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(0.01).setScale(8), timestamp++, generator.getLastReference(snapshot));
			payment.sign(generator, false);
		
			//PROCESS IN DB
			payment.process(snapshot, false);
			
			//ADD TO UNCONFIRMED TRANSACTIONS
			blockGenerator.addUnconfirmedTransaction(databaseSet, payment, false);
						
		}
		
		transactions = BlockGenerator.getUnconfirmedTransactions(databaseSet, newBlock.getTimestamp() );
		// CALCULATE HASH for that transactions
		byte[] transactionsHash = Block.makeTransactionsHash(transactions);

		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		newBlock = BlockGenerator.generateNextBlock(databaseSet, generator, genesisBlock, transactionsHash);
		newBlock.setTransactions(transactions);
		
		//CHECK IF BLOCK IS VALID
		assertEquals(true, newBlock.isValid(databaseSet));

		newBlock.sign(generator);
		//CHECK IF BLOCK IS VALID
		assertEquals(true, newBlock.isSignatureValid());
}
	
	@Test
	public void addManyTransactions()
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
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(100000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastReference(genesisBlock.getTimestamp(), databaseSet);
		generator.setConfirmedBalance(ERM_KEY, BigDecimal.valueOf(10000).setScale(8), databaseSet);
		generator.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(100000).setScale(8), databaseSet);

				
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator(false);
		Block newBlock = BlockGenerator.generateNextBlock(databaseSet, generator, genesisBlock, transactionsHash);
		
		// get timestamp for block
		long timestampStart = newBlock.getTimestamp() - Block.GENERATING_MIN_BLOCK_TIME / 2;
		long timestamp = timestampStart;

		//ADD 10000 UNCONFIRMED VALID TRANSACTIONS	
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		DBSet snapshot = databaseSet.fork();
		int max_count = 2000;
		for(int i=0; i<max_count; i++)
		{
				
			//CREATE VALID PAYMENT
			Transaction payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(0.001).setScale(8),
					 new byte[3000], new byte[]{1}, new byte[]{0},
					 timestamp++, generator.getLastReference(snapshot));
			payment.sign(generator, false);
			assertEquals(payment.isValid(snapshot, null), Transaction.VALIDATE_OK);
		
			//PROCESS IN DB
			payment.process(snapshot, false);
			
			//ADD TO UNCONFIRMED TRANSACTIONS
			blockGenerator.addUnconfirmedTransaction(databaseSet, payment, false);
		}
		
		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		transactions = BlockGenerator.getUnconfirmedTransactions(databaseSet, newBlock.getTimestamp() );

		//CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
		assertEquals(true, max_count > transactions.size());
		
		// CALCULATE HASH for that transactions
		byte[] transactionsHash = Block.makeTransactionsHash(transactions);

		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		newBlock = BlockGenerator.generateNextBlock(databaseSet, generator, genesisBlock, transactionsHash);
		newBlock.setTransactions(transactions);
		
		//CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
		assertEquals(transactions.size(), newBlock.getTransactionCount());
		
		//CHECK IF BLOCK IS VALID
		assertEquals(true, newBlock.isValid(databaseSet));
	}
	
	//TODO CALCULATETRANSACTIONSIGNATURE
}
