package test.records;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import controller.Controller;
import core.BlockChain;
import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.ItemFactory;
import core.item.polls.Poll;
import core.item.polls.PollCls;
import core.transaction.IssuePollRecord;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import core.voting.PollOption;
import core.wallet.Wallet;
import datachain.DCSet;
import ntp.NTP;

public class TestRecPoll {

	static Logger LOGGER = Logger.getLogger(TestRecPoll.class.getName());

	Long releaserReference = null;

	BigDecimal BG_ZERO = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
	long ERM_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	//long ALIVE_KEY = StatusCls.ALIVE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] pollReference = new byte[64];
	long timestamp = NTP.getTime();
	
	long flags = 4l;

	private byte[] icon = new byte[]{1,3,4,5,6,9}; // default value
	private byte[] image = new byte[]{4,11,32,23,45,122,11,-45}; // default value

	//CREATE EMPTY MEMORY DATABASE
	private DCSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount certifier = new PrivateKeyAccount(privateKey);

	//GENERATE ACCOUNT SEED
	int nonce = 1;
	//byte[] accountSeed;
	//core.wallet.Wallet.generateAccountSeed(byte[], int)
	byte[] accountSeed1 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount1 = new PrivateKeyAccount(accountSeed1);
    String userAddress1 = userAccount1.getAddress();
    byte[] accountSeed2 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount2 = new PrivateKeyAccount(accountSeed2);
    String userAddress2 = userAccount2.getAddress();
    byte[] accountSeed3 = Wallet.generateAccountSeed(seed, nonce++);
    PrivateKeyAccount userAccount3 = new PrivateKeyAccount(accountSeed3);
    String userAddress3 = userAccount3.getAddress();
    	
	PollCls pollGeneral;
	PollCls poll;
	long pollKey = -1;
	PollOption pollOption;
	List<String> options = new ArrayList<String>();;
	IssuePollRecord issuePollTransaction;

	//PollAddressMap dbPA;
	//AddressPollMap dbAP;

	int version = 0;
	long parent = -1;
	
	// INIT POLLS
	private void init() {
		
		db = DCSet.createEmptyDatabaseSet();
		Controller.getInstance().setDCSet(db);
		gb = new GenesisBlock();
		try {
			gb.process(db);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//dbPA = db.getPollAddressMap();
		//dbAP = db.getAddressPollMap();
		
		
		options.add("first ORTION");
		options.add("second ORTION");
		options.add("probe probe");

		// GET RIGHTS TO CERTIFIER
		pollGeneral = new Poll(certifier, "СССР", icon, image, "wqeqwe", options);
		//GenesisIssuePollRecord genesis_issue_poll = new GenesisIssuePollRecord(pollGeneral, certifier);
		//genesis_issue_poll.process(db, false);
		//GenesisCertifyPollRecord genesis_certify = new GenesisCertifyPollRecord(certifier, 0L);
		//genesis_certify.process(db, false);
		
		certifier.setLastTimestamp(gb.getTimestamp(db), db);
		certifier.changeBalance(db, false, ERM_KEY, BlockChain.MAJOR_ERA_BALANCE_BD, false);
		certifier.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		
		poll = new Poll(certifier, "РСФСР", icon, image, "Россия", options);

		//CREATE ISSUE POLL TRANSACTION
		issuePollTransaction = new IssuePollRecord(certifier, poll, FEE_POWER, timestamp, certifier.getLastTimestamp(db));
		issuePollTransaction.setDC(db, false);
		

	}
	
	public void initPollalize() {


		assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(releaserReference, flags));

		issuePollTransaction.sign(certifier, false);
		
		issuePollTransaction.process(gb, false);
		pollKey = poll.getKey(db);

		assertEquals( 1, pollKey);
		//assertEquals( null, dbPS.getItem(pollKey));
		
	}
	
	//ISSUE POLL TRANSACTION
	
	@Test
	public void validateSignatureIssuePollRecord() 
	{
		
		init();
						
		issuePollTransaction.sign(certifier, false);

		//CHECK IF ISSUE POLL TRANSACTION IS VALID
		assertEquals(true, issuePollTransaction.isSignatureValid(db));
		
		//INVALID SIGNATURE
		issuePollTransaction = new IssuePollRecord(certifier, poll, FEE_POWER, timestamp, certifier.getLastTimestamp(db), new byte[64]);		
		//CHECK IF ISSUE POLL IS INVALID
		assertEquals(false, issuePollTransaction.isSignatureValid(db));

	}
		
	
	@Test
	public void validateIssuePollRecord() 
	{

		init();
		
		issuePollTransaction.sign(certifier, false);

		//CHECK IF ISSUE POLL IS VALID
		assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(releaserReference, flags));

		
		//CREATE INVALID ISSUE POLL - INVALID POLLALIZE
		issuePollTransaction = new IssuePollRecord(userAccount1, poll, FEE_POWER, timestamp, 0l, new byte[64]);
		issuePollTransaction.setDC(db, false);
		if (!BlockChain.DEVELOP_USE)
			assertEquals(Transaction.NOT_ENOUGH_FEE, issuePollTransaction.isValid(releaserReference, flags));
		// ADD FEE
		userAccount1.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(releaserReference, flags));

		//CHECK IF ISSUE POLL IS VALID
		userAccount1.changeBalance(db, false, ERM_KEY, BlockChain.MINOR_ERA_BALANCE_BD, false);
		assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(releaserReference, flags));

		//CHECK 
		userAccount1.changeBalance(db, false, ERM_KEY, BlockChain.MAJOR_ERA_BALANCE_BD, false);
		assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(releaserReference, flags));

	}

	
	@Test
	public void parseIssuePollRecord() 
	{
		
		init();
		
		LOGGER.info("poll: " + poll.getType()[0] + ", " + poll.getType()[1]);

		// PARSE POLL
		
		byte [] rawPoll = poll.toBytes(false, false);
		assertEquals(rawPoll.length, poll.getDataLength(false));
		poll.setReference(new byte[64]);
		rawPoll = poll.toBytes(true, false);
		assertEquals(rawPoll.length, poll.getDataLength(true));
		
		rawPoll = poll.toBytes(false, false);
		PollCls parsedPoll = null;
		try 
		{	
			//PARSE FROM BYTES
			parsedPoll = (PollCls) ItemFactory.getInstance()
					.parse(ItemCls.POLL_TYPE, rawPoll, false);
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.  : " + e);
		}
		assertEquals(rawPoll.length, poll.getDataLength(false));
		assertEquals(poll.getOwner().getAddress(), parsedPoll.getOwner().getAddress());
		assertEquals(poll.getName(), parsedPoll.getName());
		assertEquals(poll.getDescription(), parsedPoll.getDescription());
		assertEquals(poll.getItemTypeStr(), parsedPoll.getItemTypeStr());
		
		// PARSE ISSEU POLL RECORD
		issuePollTransaction.sign(certifier, false);
		issuePollTransaction.setDC(db, false);
		issuePollTransaction.process(gb, false);
		
		//CONVERT TO BYTES
		byte[] rawIssuePollRecord = issuePollTransaction.toBytes(true, null);
		
		//CHECK DATA LENGTH
		assertEquals(rawIssuePollRecord.length, issuePollTransaction.getDataLength(false));
		
		IssuePollRecord parsedIssuePollRecord = null;
		try 
		{	
			//PARSE FROM BYTES
			parsedIssuePollRecord = (IssuePollRecord) TransactionFactory.getInstance().parse(rawIssuePollRecord, releaserReference);			

		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.  : " + e);
		}
		
		//CHECK INSTANCE
		assertEquals(true, parsedIssuePollRecord instanceof IssuePollRecord);
		
		//CHECK SIGNATURE
		assertEquals(true, Arrays.equals(issuePollTransaction.getSignature(), parsedIssuePollRecord.getSignature()));
		
		//CHECK ISSUER
		assertEquals(issuePollTransaction.getCreator().getAddress(), parsedIssuePollRecord.getCreator().getAddress());
		
		parsedPoll = (Poll)parsedIssuePollRecord.getItem();

		//CHECK OWNER
		assertEquals(poll.getOwner().getAddress(), parsedPoll.getOwner().getAddress());
		
		//CHECK NAME
		assertEquals(poll.getName(), parsedPoll.getName());
			
		//CHECK REFERENCE
		//assertEquals(issuePollTransaction.getReference(), parsedIssuePollRecord.getReference());	
		
		//CHECK TIMESTAMP
		assertEquals(issuePollTransaction.getTimestamp(), parsedIssuePollRecord.getTimestamp());				

		//CHECK DESCRIPTION
		assertEquals(poll.getDescription(), parsedPoll.getDescription());
						
		assertEquals(poll.getItemTypeStr(), parsedPoll.getItemTypeStr());

		assertEquals(poll.getOptions().size(), parsedPoll.getOptions().size());
		assertEquals(poll.getOptions().get(2), parsedPoll.getOptions().get(2));

		//PARSE TRANSACTION FROM WRONG BYTES
		rawIssuePollRecord = new byte[issuePollTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawIssuePollRecord, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}

	
	@Test
	public void processIssuePollRecord()
	{
		
		init();				
		
		assertEquals(Transaction.VALIDATE_OK, issuePollTransaction.isValid(releaserReference, flags));

		issuePollTransaction.sign(certifier, false);
		issuePollTransaction.setDC(db, false);
		issuePollTransaction.process(gb, false);
		
		LOGGER.info("poll KEY: " + poll.getKey(db));
		
		//CHECK BALANCE ISSUER
		//assertEquals(BlockChain.MAJOR_ERA_BALANCE_BD, certifier.getBalanceUSE(ERM_KEY, db));
		//assertEquals(issuePollTransaction.getFee().setScale(BlockChain.AMOUNT_DEDAULT_SCALE), certifier.getBalanceUSE(FEE_KEY, db));
		
		//CHECK POLL EXISTS DB AS CONFIRMED:  key > -1
		long key = db.getIssuePollMap().get(issuePollTransaction);
		assertEquals(1l, key);
		assertEquals(true, db.getItemPollMap().contains(key));
		
		//CHECK POLL IS CORRECT
		assertEquals(true, Arrays.equals(db.getItemPollMap().get(key).toBytes(true, false), poll.toBytes(true, false)));
						
		//CHECK REFERENCE SENDER
		assertEquals(issuePollTransaction.getTimestamp(), certifier.getLastTimestamp(db));

		//////// ORPHAN /////////
		issuePollTransaction.orphan(false);
		
		//CHECK BALANCE ISSUER
		if (!BlockChain.DEVELOP_USE)
			assertEquals(BlockChain.MAJOR_ERA_BALANCE_BD, certifier.getBalanceUSE(ERM_KEY, db));
		assertEquals(BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), certifier.getBalanceUSE(FEE_KEY, db));
		
		//CHECK POLL EXISTS ISSUER
		assertEquals(false, db.getItemPollMap().contains(pollKey));
						
		//CHECK REFERENCE ISSUER
		//assertEquals(issuePollTransaction.getReference(), certifier.getLastReference(db));
	}
	
	
}
