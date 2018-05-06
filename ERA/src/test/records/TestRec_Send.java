package test.records;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.payment.Payment;
import core.transaction.ArbitraryTransactionV3;
import core.transaction.R_Send;
import core.transaction.Transaction;
import core.transaction.TransactionAmount;
import datachain.DCSet;
import ntp.NTP;



public class TestRec_Send {

	static Logger LOGGER = Logger.getLogger(TestRec_Send.class.getName());

	Long releaserReference = null;

	long ERM_KEY = 3;
	long FEE_KEY = AssetCls.FEE_KEY;
	byte FEE_POWER = (byte)0;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();

	long flags = 0l;

	private byte[] icon = new byte[]{1,3,4,5,6,9}; // default value
	private byte[] image = new byte[]{4,11,32,23,45,122,11,-45}; // default value

	//CREATE EMPTY MEMORY DATABASE
	private DCSet db;
	private GenesisBlock gb;

	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);

	Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
	BigDecimal amount = BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

	String head = "headdd";
	byte[] data = "test123!".getBytes();
	byte[] isText = new byte[] { 1 };
	byte[] encrypted = new byte[] { 0 };

	// INIT ASSETS
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

		// FEE FUND
		maker.setLastTimestamp(gb.getTimestamp(db), db);
		maker.changeBalance(db, false, ERM_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		maker.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);

	}

	@Test
	public void scaleTest()
	{

		Integer bbb = 31;
		assertEquals("11111", Integer.toBinaryString(bbb));

		assertEquals("10000000", Integer.toBinaryString(128));

		byte noData = (byte)128;
		//assertEquals((byte)-1, (byte)128);
		assertEquals((byte)128, (byte)-128);
		//assertEquals(core.transaction.R_Send.NO_DATA_MASK));
		
		BigDecimal amountTest = new BigDecimal("1234567812345678");
		BigDecimal amountBase;
		BigDecimal amount1;
		BigDecimal amount2;

		//int shift = 64;
		int scale;
		int scaleBase;

		for (int i = 0; i < TransactionAmount.SCALE_MASK; i++) {

			amount1 = amountTest.scaleByPowerOfTen(-TransactionAmount.SCALE_MASK_HALF - BlockChain.AMOUNT_DEDAULT_SCALE + i);

			scale = amount1.scale();

			// TO BASE
			scaleBase = scale - BlockChain.AMOUNT_DEDAULT_SCALE;

			// to DEFAUTL base 8 decimals
			amountBase = amount1.scaleByPowerOfTen(scaleBase);

			if (scaleBase < 0)
				scaleBase += TransactionAmount.SCALE_MASK + 1;


			// CHECK ACCURACY of AMOUNT
			int accuracy = scaleBase & TransactionAmount.SCALE_MASK;
			String sss = Integer.toBinaryString(accuracy);


			if (accuracy > 0) {
				if (accuracy > TransactionAmount.SCALE_MASK_HALF + 1) {
					accuracy -= TransactionAmount.SCALE_MASK + 1;
				}
				// RESCALE AMOUNT
				amount2 = amountBase.scaleByPowerOfTen(-accuracy);
			} else {
				amount2 = amountBase;
			}


			assertEquals(amount1, amount2);

		}
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
				ERM_KEY,
				amount,
				head, data,
				isText,
				encrypted,
				timestamp, maker.getLastTimestamp(db)
				);
		r_SendV3.sign(maker, false);
		r_SendV3.setDC(db, false);

		assertEquals(r_SendV3.isValid(releaserReference, flags), 25); //Transaction.VALIDATE_OK);

		assertEquals((long)maker.getLastTimestamp(db), gb.getTimestamp(db));
		r_SendV3.process(gb, false);
		assertEquals((long)maker.getLastTimestamp(db), timestamp);

		//assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(90).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(ERM_KEY, db));
		assertEquals(BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(ERM_KEY, db));

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

		assertEquals(r_SendV3.isSignatureValid(db), true);
		assertEquals(messageTransactionV3_2.isSignatureValid(db), true);

		//// MESSAGE ONLY
		r_SendV3.orphan(false);
		assertEquals((long)maker.getLastTimestamp(db), gb.getTimestamp(db));

		r_SendV3 = new R_Send(
				maker, FEE_POWER,
				recipient,
				ERM_KEY,
				null,
				head, data,
				isText,
				encrypted,
				timestamp, maker.getLastTimestamp(db)
				);
		r_SendV3.sign(maker, false);
		r_SendV3.setDC(db, false);

		assertEquals(r_SendV3.isValid(releaserReference, flags), 25); //Transaction.VALIDATE_OK);

		r_SendV3.process(gb, false);
		assertEquals((long)maker.getLastTimestamp(db), timestamp);

		//assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(ERM_KEY, db));
		assertEquals(BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(ERM_KEY, db));

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
		assertEquals(0, messageTransactionV3_2.getKey());
		assertEquals(r_SendV3.getAmount(), messageTransactionV3_2.getAmount());
		assertEquals(r_SendV3.isEncrypted(), messageTransactionV3_2.isEncrypted());
		assertEquals(r_SendV3.isText(), messageTransactionV3_2.isText());

		assertEquals(r_SendV3.isSignatureValid(db), true);
		assertEquals(messageTransactionV3_2.isSignatureValid(db), true);


		//// AMOUNT ONLY
		r_SendV3.orphan(false);
		assertEquals((long)maker.getLastTimestamp(db), gb.getTimestamp(db));

		r_SendV3 = new R_Send(
				maker, FEE_POWER,
				recipient,
				ERM_KEY,
				amount,
				"", null,
				null,
				null,
				timestamp, maker.getLastTimestamp(db)
				);
		r_SendV3.sign(maker, false);
		r_SendV3.setDC(db, false);

		assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.VALIDATE_OK);

		r_SendV3.process(gb, false);

		//assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(90).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(ERM_KEY, db));
		assertEquals(BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(ERM_KEY, db));

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

		assertEquals(r_SendV3.isSignatureValid(db), true);
		assertEquals(messageTransactionV3_2.isSignatureValid(db), true);

		//// EMPTY - NOT AMOUNT and NOT TEXT
		r_SendV3.orphan(false);

		r_SendV3 = new R_Send(
				maker, FEE_POWER,
				recipient,
				ERM_KEY,
				null,
				null, null,
				null,
				null,
				timestamp, maker.getLastTimestamp(db)
				);
		r_SendV3.sign(maker, false);
		r_SendV3.setDC(db, false);

		assertEquals(r_SendV3.isValid(releaserReference, flags), Transaction.VALIDATE_OK);

		r_SendV3.process(gb, false);

		//assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(ERM_KEY, db));
		assertEquals(BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(ERM_KEY, db));

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
		assertEquals(0, messageTransactionV3_2.getKey());
		assertEquals(r_SendV3.getAmount(), messageTransactionV3_2.getAmount());

		assertEquals(r_SendV3.isSignatureValid(db), true);
		assertEquals(messageTransactionV3_2.isSignatureValid(db), true);

		// NEGATE for test HOLD ///////////////////
		amount = amount.negate();
		recipient.changeBalance(db, false, -ERM_KEY, amount.negate(), false);
		/// MESSAGE + AMOUNT
		r_SendV3 = new R_Send(
				maker, FEE_POWER,
				recipient,
				-ERM_KEY,
				amount,
				head, data,
				isText,
				encrypted,
				++timestamp, maker.getLastTimestamp(db)
				);
		r_SendV3.sign(maker, false);
		r_SendV3.setDC(db, false);

		assertEquals(r_SendV3.isValid(releaserReference, flags), 25); //ransaction.VALIDATE_OK);

		r_SendV3.process(gb, false);

		//assertEquals(BigDecimal.valueOf(1).subtract(r_SendV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(ERM_KEY, db));
		assertEquals(BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(ERM_KEY, db));

		rawMessageTransactionV3 = r_SendV3.toBytes(true, null);
		dd = r_SendV3.getDataLength(false);
		assertEquals(rawMessageTransactionV3.length, r_SendV3.getDataLength(false));

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

		assertEquals(r_SendV3.isSignatureValid(db), true);
		assertEquals(messageTransactionV3_2.isSignatureValid(db), true);

	}


	@Test
	public void validateArbitraryTransactionV3()
	{

		init();

		//ADD ERM ASSET
		AssetCls aTFundingAsset = new AssetVenture(new GenesisBlock().getCreator(), "ATFunding", icon, image, "This asset represents the funding of AT team for the integration of a Turing complete virtual machine into ERM.",
				0, 8, 250000000l);
		aTFundingAsset.setReference(assetReference);
		db.getItemAssetMap().set(61l, aTFundingAsset);

		GenesisBlock genesisBlock = gb; //new GenesisBlock();
		/*
		try {
			genesisBlock.process(db);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/

		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();


		Account recipient1 = new Account("79MXwfzHPDGWoQUgyPXRf2fxKuzY1osNsg");
		Account recipient2 = new Account("76abzpJK61F4TAZFkqev2EY5duHVUvycZX");
		Account recipient3 = new Account("7JU8UTuREAJG2yht5ASn7o1Ur34P1nvTk5");

		long timestamp = NTP.getTime();

		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS

		maker.changeBalance(db, false, 61l, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);

		List<Payment> payments = new ArrayList<Payment>();
		payments.add(new Payment(recipient1, 61l, BigDecimal.valueOf(110).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
		payments.add(new Payment(recipient2, 61l, BigDecimal.valueOf(120).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
		payments.add(new Payment(recipient3, 61l, BigDecimal.valueOf(201).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));

		ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
				maker, payments, 111,
				data,
				FEE_POWER,
				++timestamp, maker.getLastTimestamp(db)
				);
		arbitraryTransactionV3.sign(maker, false);
		arbitraryTransactionV3.setDC(db, false);

		//if (NTP.getTime() < Transaction.getARBITRARY_TRANSACTIONS_RELEASE() || arbitraryTransactionV3.getTimestamp() < Transaction.getPOWFIX_RELEASE())
		if (false)
		{
			assertEquals(arbitraryTransactionV3.isValid(releaserReference, flags), Transaction.NOT_YET_RELEASED);
		}
		else
		{
			assertEquals(arbitraryTransactionV3.isValid(releaserReference, flags), Transaction.VALIDATE_OK);
		}

		arbitraryTransactionV3.process(gb, false);

		assertEquals(BigDecimal.valueOf(1).subtract(arbitraryTransactionV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(1000-110-120-201).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(61l, db));
		assertEquals(BigDecimal.valueOf(110).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient1.getBalanceUSE(61l, db));
		assertEquals(BigDecimal.valueOf(120).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient2.getBalanceUSE(61l, db));
		assertEquals(BigDecimal.valueOf(201).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient3.getBalanceUSE(61l, db));

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

		assertEquals(arbitraryTransactionV3.isSignatureValid(db), true);
		assertEquals(arbitraryTransactionV3_2.isSignatureValid(db), true);
	}

	@Test
	public void validateArbitraryTransactionV3withoutPayments()
	{

		init();

		AssetCls aTFundingAsset = new AssetVenture(gb.getCreator(), "ATFunding", icon, image, "This asset represents the funding of AT team for the integration of a Turing complete virtual machine into ERM.",
				0, 8, 250000000l);
		aTFundingAsset.setReference(gb.getSignature());
		db.getItemAssetMap().set(61l, aTFundingAsset);

		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();

		byte[] data = "test123!".getBytes();

		long timestamp = NTP.getTime();

		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS

		maker.changeBalance(db, false, 61l, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);

		List<Payment> payments = new ArrayList<Payment>();

		ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
				maker, payments, 111,
				data,
				FEE_POWER,
				timestamp, maker.getLastTimestamp(db)
				);
		arbitraryTransactionV3.sign(maker, false);
		arbitraryTransactionV3.setDC(db, false);

		//if (NTP.getTime() < Transaction.getARBITRARY_TRANSACTIONS_RELEASE() || arbitraryTransactionV3.getTimestamp() < Transaction.getPOWFIX_RELEASE())
		if (false)
		{
			assertEquals(arbitraryTransactionV3.isValid(releaserReference, flags), Transaction.NOT_YET_RELEASED);
		}
		else
		{
			assertEquals(arbitraryTransactionV3.isValid(releaserReference, flags), Transaction.VALIDATE_OK);
		}

		arbitraryTransactionV3.process(gb, false);

		assertEquals(BigDecimal.valueOf(1).subtract(arbitraryTransactionV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(61l, db));


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

		assertEquals(arbitraryTransactionV3.isSignatureValid(db), true);
		assertEquals(arbitraryTransactionV3_2.isSignatureValid(db), true);
	}

	@Test
	public void makeMessageTransactionV3_DISCREDIR_ADDRESSES()
	{

		// HPftF6gmSH3mn9dKSAwSEoaxW2Lb6SVoguhKyHXbyjr7 -
		PublicKeyAccount maker = new PublicKeyAccount(Transaction.DISCREDIR_ADDRESSES[0]);
		Account recipient = new Account("7R2WUFaS7DF2As6NKz13Pgn9ij4sFw6ymZ");
		BigDecimal amount = BigDecimal.valueOf(49800).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

		long era_key = 1l;
		/// DISCREDIR_ADDRESSES
		R_Send r_Send = new R_Send(maker, FEE_POWER, recipient, era_key, amount, "", null, isText, encrypted, timestamp, 1l);

		byte[] data = r_Send.toBytes(false, null);
		int port = Controller.getInstance().getNetworkPort();
		data = Bytes.concat(data, Ints.toByteArray(port));
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);

		R_Send r_SendSigned = new R_Send(maker, FEE_POWER, recipient, era_key, amount, "", null, isText, encrypted, timestamp, 1l, digest);
		String raw = Base58.encode(r_SendSigned.toBytes(true, null));
		System.out.print(raw);

		//DCSet dcSet = DCSet.getInstance();
		//assertEquals(r_SendSigned.isSignatureValid(dcSet), true);
		//r_SendSigned.setDC(dcSet, false);
		//assertEquals(r_SendSigned.isValid(dcSet, null), Transaction.VALIDATE_OK);
		//Controller.getInstance().broadcastTransaction(r_SendSigned);

	}


}
