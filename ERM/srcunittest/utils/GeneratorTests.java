package utils;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import core.BlockGenerator;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.transaction.R_Send;
import core.transaction.Transaction;
import database.DBSet;
//import ntp.NTP;

public class GeneratorTests {

	byte FEE_KEY = 1;
	byte FEE_POWER = (byte)0;
	
	@Test
	public void addManyTransactionsWithDifferentFees()
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
		//Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(10000000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet, false);
		generator.setLastReference(genesisBlock.getTimestamp(), databaseSet);
		generator.setConfirmedBalance(Transaction.FEE_KEY, BigDecimal.valueOf(10000000).setScale(8), databaseSet);
				
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
		//ADD 10 UNCONFIRMED VALID TRANSACTIONS	
		Account recipient = new Account("QcA6u3ejXLHKH4Km2wH9rXNGJAp2e4iFeA");
		DBSet snapshot = databaseSet.fork();
		for(int i=0; i<1000; i++)
		{
			long timestamp = newBlock.getTimestamp() + i - 10000;
						 				
			//CREATE VALID PAYMENT
			Transaction payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(snapshot));
			payment.sign(generator, false);
		
			//PROCESS IN DB
			payment.process(snapshot, false);
			
			//ADD TO UNCONFIRMED TRANSACTIONS
			blockGenerator.addUnconfirmedTransaction(databaseSet, payment, false);
		}
		
		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		blockGenerator.addUnconfirmedTransactions(databaseSet, newBlock);
		
		//CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
		assertEquals(1000, newBlock.getTransactionCount());
		
		//CHECK IF BLOCK IS VALID
		assertEquals(true, newBlock.isValid(databaseSet));
	}
	//TODO CALCULATETRANSACTIONSIGNATURE
}
