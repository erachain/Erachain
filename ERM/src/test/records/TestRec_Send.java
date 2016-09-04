package test.records;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ntp.NTP;

import org.apache.log4j.Logger;
import org.junit.Test;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.payment.Payment;
import core.transaction.ArbitraryTransactionV3;
import core.transaction.R_Send;
import core.transaction.Transaction;
import database.DBSet;



public class TestRec_Send {

	static Logger LOGGER = Logger.getLogger(TestRec_Send.class.getName());

	Long releaserReference = null;

	long ERMO_KEY = 3;
	long FEE_KEY = AssetCls.FEE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();
	
	private byte[] icon = new byte[]{1,3,4,5,6,9}; // default value
	private byte[] image = new byte[]{4,11,32,23,45,122,11,-45}; // default value

	//CREATE EMPTY MEMORY DATABASE
	private DBSet db;
	private GenesisBlock gb;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);

	Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");		
	BigDecimal amount = BigDecimal.valueOf(10).setScale(8); 

	byte[] data = "test123!".getBytes();
	byte[] isText = new byte[] { 1 };
	byte[] encrypted = new byte[] { 0 };

	// INIT ASSETS
	private void init() {
		
		db = DBSet.createEmptyDatabaseSet();
		gb = new GenesisBlock();
		gb.process(db);
		
		// FEE FUND
		maker.setLastReference(gb.getTimestamp(db), db);
		maker.setBalance(ERMO_KEY, BigDecimal.valueOf(100).setScale(8), db);
		maker.setBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);

	}

	@Test
	public void validateMessageTransactionV3() 
	{
		
		//Integer bbb = -128;
		//assertEquals("1111", Integer.toBinaryString(bbb));
		
		init();

		/// MESSAGE + AMOUNT
		R_Send r_SendV3 = new R_Send(
				maker, FEE_POWER, 
				recipient, 
				ERMO_KEY, 
				amount,
				data,
				isText,
				encrypted,
				timestamp, maker.getLastReference(db)
				);
		r_SendV3.sign(maker, false);
		
		assertEquals(r_SendV3.isValid(db, releaserReference), Transaction.VALIDATE_OK);
		
		r_SendV3.process(db, false);
		
		assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(8), maker.getBalanceUSR(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(90).setScale(8), maker.getBalanceUSR(ERMO_KEY, db));
		assertEquals(BigDecimal.valueOf(10).setScale(8), recipient.getBalanceUSR(ERMO_KEY, db));
		
		byte[] rawMessageTransactionV3 = r_SendV3.toBytes(true, null);
		int dd = r_SendV3.getDataLength(false);
		assertEquals(rawMessageTransactionV3.length, r_SendV3.getDataLength(false));

		
		R_Send messageTransactionV3_2 = null;
		try {
			messageTransactionV3_2 = (R_Send) R_Send.Parse(rawMessageTransactionV3, releaserReference);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		assertEquals(new String(r_SendV3.getData()), new String(messageTransactionV3_2.getData()));
		assertEquals(r_SendV3.getCreator(), messageTransactionV3_2.getCreator());
		assertEquals(r_SendV3.getRecipient(), messageTransactionV3_2.getRecipient());
		assertEquals(r_SendV3.getKey(), messageTransactionV3_2.getKey());
		assertEquals(r_SendV3.getAmount(), messageTransactionV3_2.getAmount());
		assertEquals(r_SendV3.isEncrypted(), messageTransactionV3_2.isEncrypted());
		assertEquals(r_SendV3.isText(), messageTransactionV3_2.isText());
		
		assertEquals(r_SendV3.isSignatureValid(), true);
		assertEquals(messageTransactionV3_2.isSignatureValid(), true);		

		//// MESSAGE ONLY
		r_SendV3.orphan(db, false);
		
		r_SendV3 = new R_Send(
				maker, FEE_POWER, 
				recipient, 
				ERMO_KEY, 
				null,
				data,
				isText,
				encrypted,
				timestamp, maker.getLastReference(db)
				);
		r_SendV3.sign(maker, false);
		
		assertEquals(r_SendV3.isValid(db, releaserReference), Transaction.VALIDATE_OK);
		
		r_SendV3.process(db, false);
		
		assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(8), maker.getBalanceUSR(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(8), maker.getBalanceUSR(ERMO_KEY, db));
		assertEquals(BigDecimal.valueOf(0).setScale(8), recipient.getBalanceUSR(ERMO_KEY, db));
		
		rawMessageTransactionV3 = r_SendV3.toBytes(true, null);
		dd = r_SendV3.getDataLength(false);
		assertEquals(rawMessageTransactionV3.length, r_SendV3.getDataLength(false));

		
		messageTransactionV3_2 = null;
		try {
			messageTransactionV3_2 = (R_Send) R_Send.Parse(rawMessageTransactionV3, releaserReference);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		assertEquals(new String(r_SendV3.getData()), new String(messageTransactionV3_2.getData()));
		assertEquals(r_SendV3.getCreator(), messageTransactionV3_2.getCreator());
		assertEquals(r_SendV3.getRecipient(), messageTransactionV3_2.getRecipient());
		assertEquals(-1, messageTransactionV3_2.getKey());
		assertEquals(r_SendV3.getAmount(), messageTransactionV3_2.getAmount());
		assertEquals(r_SendV3.isEncrypted(), messageTransactionV3_2.isEncrypted());
		assertEquals(r_SendV3.isText(), messageTransactionV3_2.isText());
		
		assertEquals(r_SendV3.isSignatureValid(), true);
		assertEquals(messageTransactionV3_2.isSignatureValid(), true);		


		//// AMOUNT ONLY
		r_SendV3.orphan(db, false);
		
		r_SendV3 = new R_Send(
				maker, FEE_POWER, 
				recipient, 
				ERMO_KEY, 
				amount,
				null,
				null,
				null,
				timestamp, maker.getLastReference(db)
				);
		r_SendV3.sign(maker, false);
		
		assertEquals(r_SendV3.isValid(db, releaserReference), Transaction.VALIDATE_OK);
		
		r_SendV3.process(db, false);
		
		assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(8), maker.getBalanceUSR(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(90).setScale(8), maker.getBalanceUSR(ERMO_KEY, db));
		assertEquals(BigDecimal.valueOf(10).setScale(8), recipient.getBalanceUSR(ERMO_KEY, db));
		
		rawMessageTransactionV3 = r_SendV3.toBytes(true, null);
		dd = r_SendV3.getDataLength(false);
		assertEquals(rawMessageTransactionV3.length, r_SendV3.getDataLength(false));

		
		messageTransactionV3_2 = null;
		try {
			messageTransactionV3_2 = (R_Send) R_Send.Parse(rawMessageTransactionV3, releaserReference);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		assertEquals(null,r_SendV3.getData());
		assertEquals(null,messageTransactionV3_2.getData());
		assertEquals(r_SendV3.getCreator(), messageTransactionV3_2.getCreator());
		assertEquals(r_SendV3.getRecipient(), messageTransactionV3_2.getRecipient());
		assertEquals(r_SendV3.getKey(), messageTransactionV3_2.getKey());
		assertEquals(r_SendV3.getAmount(), messageTransactionV3_2.getAmount());
		
		assertEquals(r_SendV3.isSignatureValid(), true);
		assertEquals(messageTransactionV3_2.isSignatureValid(), true);		

		//// EMPTY - NOT AMOUNT and NOT TEXT
		r_SendV3.orphan(db, false);
		
		r_SendV3 = new R_Send(
				maker, FEE_POWER, 
				recipient, 
				ERMO_KEY, 
				null,
				null,
				null,
				null,
				timestamp, maker.getLastReference(db)
				);
		r_SendV3.sign(maker, false);
		
		assertEquals(r_SendV3.isValid(db, releaserReference), Transaction.VALIDATE_OK);
		
		r_SendV3.process(db, false);
		
		assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(8), maker.getBalanceUSR(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(8), maker.getBalanceUSR(ERMO_KEY, db));
		assertEquals(BigDecimal.valueOf(0).setScale(8), recipient.getBalanceUSR(ERMO_KEY, db));
		
		rawMessageTransactionV3 = r_SendV3.toBytes(true, null);
		dd = r_SendV3.getDataLength(false);
		assertEquals(rawMessageTransactionV3.length, r_SendV3.getDataLength(false));

		
		messageTransactionV3_2 = null;
		try {
			messageTransactionV3_2 = (R_Send) R_Send.Parse(rawMessageTransactionV3, releaserReference);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		assertEquals(null,r_SendV3.getData());
		assertEquals(null,messageTransactionV3_2.getData());
		assertEquals(r_SendV3.getCreator(), messageTransactionV3_2.getCreator());
		assertEquals(r_SendV3.getRecipient(), messageTransactionV3_2.getRecipient());
		assertEquals(-1, messageTransactionV3_2.getKey());
		assertEquals(r_SendV3.getAmount(), messageTransactionV3_2.getAmount());
		
		assertEquals(r_SendV3.isSignatureValid(), true);
		assertEquals(messageTransactionV3_2.isSignatureValid(), true);		

}
	
	
	@Test
	public void validateArbitraryTransactionV3() 
	{
		
		init();
		
		//ADD ERM ASSET
		AssetCls aTFundingAsset = new AssetVenture(new GenesisBlock().getCreator(), "ATFunding", icon, image, "This asset represents the funding of AT team for the integration of a Turing complete virtual machine into ERM.", false, 250000000L, (byte) 2, true);
		aTFundingAsset.setReference(assetReference);
		db.getItemAssetMap().set(61l, aTFundingAsset);
    	
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(db);
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		
		
		Account recipient1 = new Account("79MXwfzHPDGWoQUgyPXRf2fxKuzY1osNsg");		
		Account recipient2 = new Account("76abzpJK61F4TAZFkqev2EY5duHVUvycZX");		
		Account recipient3 = new Account("7JU8UTuREAJG2yht5ASn7o1Ur34P1nvTk5");		

		long timestamp = NTP.getTime();

		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		
		maker.setBalance(61l, BigDecimal.valueOf(1000).setScale(8), db);
		
		List<Payment> payments = new ArrayList<Payment>();
		payments.add(new Payment(recipient1, 61l, BigDecimal.valueOf(110).setScale(8)));
		payments.add(new Payment(recipient2, 61l, BigDecimal.valueOf(120).setScale(8)));
		payments.add(new Payment(recipient3, 61l, BigDecimal.valueOf(201).setScale(8)));
				
		ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
				maker, payments, 111,
				data, 
				FEE_POWER,
				++timestamp, maker.getLastReference(db)
				);
		arbitraryTransactionV3.sign(maker, false);
		
		//if (NTP.getTime() < Transaction.getARBITRARY_TRANSACTIONS_RELEASE() || arbitraryTransactionV3.getTimestamp() < Transaction.getPOWFIX_RELEASE())
		if (false)
		{
			assertEquals(arbitraryTransactionV3.isValid(db, releaserReference), Transaction.NOT_YET_RELEASED);
		}
		else
		{
			assertEquals(arbitraryTransactionV3.isValid(db, releaserReference), Transaction.VALIDATE_OK);
		}
		
		arbitraryTransactionV3.process(db, false);
		
		assertEquals(BigDecimal.valueOf(1).subtract(arbitraryTransactionV3.getFee()).setScale(8), maker.getBalanceUSR(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(1000-110-120-201).setScale(8), maker.getBalanceUSR(61l, db));
		assertEquals(BigDecimal.valueOf(110).setScale(8), recipient1.getBalanceUSR(61l, db));
		assertEquals(BigDecimal.valueOf(120).setScale(8), recipient2.getBalanceUSR(61l, db));
		assertEquals(BigDecimal.valueOf(201).setScale(8), recipient3.getBalanceUSR(61l, db));
		
		byte[] rawArbitraryTransactionV3 = arbitraryTransactionV3.toBytes(true, null);
		
		ArbitraryTransactionV3 arbitraryTransactionV3_2 = null;
		try {
			arbitraryTransactionV3_2 = (ArbitraryTransactionV3) ArbitraryTransactionV3.Parse(Arrays.copyOfRange(rawArbitraryTransactionV3, 0, rawArbitraryTransactionV3.length));
			// already SIGNED - arbitraryTransactionV3_2.sign(creator);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		assertEquals(new String(arbitraryTransactionV3.getData()), new String(arbitraryTransactionV3_2.getData()));
		assertEquals(	arbitraryTransactionV3.getPayments().get(0).toJson().toJSONString(), 
						arbitraryTransactionV3_2.getPayments().get(0).toJson().toJSONString());
		assertEquals(	arbitraryTransactionV3.getPayments().get(1).toJson().toJSONString(), 
						arbitraryTransactionV3_2.getPayments().get(1).toJson().toJSONString());
		assertEquals(	arbitraryTransactionV3.getPayments().get(2).toJson().toJSONString(), 
						arbitraryTransactionV3_2.getPayments().get(2).toJson().toJSONString());
		assertEquals( 	arbitraryTransactionV3.getPayments().size(), arbitraryTransactionV3.getPayments().size());  

		assertEquals(arbitraryTransactionV3.getService(), arbitraryTransactionV3_2.getService());
		assertEquals(arbitraryTransactionV3.getCreator(), arbitraryTransactionV3_2.getCreator());

		assertEquals(arbitraryTransactionV3.isSignatureValid(), true);
		assertEquals(arbitraryTransactionV3_2.isSignatureValid(), true);		
	}	
	
	@Test
	public void validateArbitraryTransactionV3withoutPayments() 
	{

		init();
		
		AssetCls aTFundingAsset = new AssetVenture(gb.getCreator(), "ATFunding", icon, image, "This asset represents the funding of AT team for the integration of a Turing complete virtual machine into ERM.", false, 250000000L, (byte) 2, true);
		aTFundingAsset.setReference(gb.getSignature());
		db.getItemAssetMap().set(61l, aTFundingAsset);

		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		
		byte[] data = "test123!".getBytes();
				
		long timestamp = NTP.getTime();

		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		
		maker.setBalance(61l, BigDecimal.valueOf(1000).setScale(8), db);
		
		List<Payment> payments = new ArrayList<Payment>();
				
		ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
				maker, payments, 111,
				data, 
				FEE_POWER,
				timestamp, maker.getLastReference(db)
				);
		arbitraryTransactionV3.sign(maker, false);
		
		//if (NTP.getTime() < Transaction.getARBITRARY_TRANSACTIONS_RELEASE() || arbitraryTransactionV3.getTimestamp() < Transaction.getPOWFIX_RELEASE())
		if (false)
		{
			assertEquals(arbitraryTransactionV3.isValid(db, releaserReference), Transaction.NOT_YET_RELEASED);
		}
		else
		{
			assertEquals(arbitraryTransactionV3.isValid(db, releaserReference), Transaction.VALIDATE_OK);
		}
		
		arbitraryTransactionV3.process(db, false);
		
		assertEquals(BigDecimal.valueOf(1).subtract(arbitraryTransactionV3.getFee()).setScale(8), maker.getBalanceUSR(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(1000).setScale(8), maker.getBalanceUSR(61l, db));

		
		byte[] rawArbitraryTransactionV3 = arbitraryTransactionV3.toBytes(true, null);
		
		ArbitraryTransactionV3 arbitraryTransactionV3_2 = null;
		try {
			arbitraryTransactionV3_2 = (ArbitraryTransactionV3) ArbitraryTransactionV3.Parse(Arrays.copyOfRange(rawArbitraryTransactionV3, 4, rawArbitraryTransactionV3.length));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		assertEquals(new String(arbitraryTransactionV3.getData()), new String(arbitraryTransactionV3_2.getData()));

		assertEquals( 	arbitraryTransactionV3.getPayments().size(), arbitraryTransactionV3.getPayments().size());  

		assertEquals(arbitraryTransactionV3.getService(), arbitraryTransactionV3_2.getService());
		assertEquals(arbitraryTransactionV3.getCreator(), arbitraryTransactionV3_2.getCreator());

		assertEquals(arbitraryTransactionV3.isSignatureValid(), true);
		assertEquals(arbitraryTransactionV3_2.isSignatureValid(), true);		
	}	
	
}
