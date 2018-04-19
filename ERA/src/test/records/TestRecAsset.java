package test.records;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.assets.AssetUnique;
import core.item.assets.AssetVenture;
import core.transaction.IssueAssetTransaction;
import core.transaction.R_Send;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import datachain.DCSet;
import ntp.NTP;

public class TestRecAsset {

	static Logger LOGGER = Logger.getLogger(TestRecAsset.class.getName());

	Long releaserReference = null;

	long FEE_KEY = AssetCls.FEE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();

	private byte[] icon = new byte[]{1,3,4,5,6,9}; // default value
	private byte[] image = new byte[]{4,11,32,23,45,122,11,-45}; // default value

	//CREATE EMPTY MEMORY DATABASE
	private DCSet db;
	private GenesisBlock gb;
	private BlockChain bchain;
	Controller cntrl;

	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("tes213sdffsdft".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);

	byte[] seed_1 = Crypto.getInstance().digest("tes213sdffsdft_1".getBytes());
	byte[] privateKey_1 = Crypto.getInstance().createKeyPair(seed_1).getA();
	PrivateKeyAccount maker_1 = new PrivateKeyAccount(privateKey_1);

	AssetCls asset;
	AssetCls assetMovable;
	long key = 0;

	R_Send rsend;
	Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance5;

	// INIT ASSETS
	private void init() {

		db = DCSet.createEmptyDatabaseSet();
		cntrl = Controller.getInstance();
		cntrl.initBlockChain(db);
		bchain = cntrl.getBlockChain();
		gb = bchain.getGenesisBlock();
		//gb.process(db);

		// FEE FUND
		maker.setLastTimestamp(gb.getTimestamp(db), db);
		maker.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);

		maker_1.setLastTimestamp(gb.getTimestamp(db), db);

		asset = new AssetVenture(maker, "aasdasd", icon, image, "asdasda", 0, 8, 50000l);
		//key = asset.getKey();

		assetMovable = new AssetVenture(maker, "movable", icon, image, "...", 0, 8, 50000l);

	}

	@Test
	public void testScale()
	{

		init();

		int scalse_in = 5;
		int scalse_asset = 1;
		int scale_default = 8;
		BigDecimal amount_in = BigDecimal.valueOf(12345.123).setScale(scalse_in, BigDecimal.ROUND_HALF_DOWN);
		BigDecimal amount_asset = amount_in.setScale(scalse_asset, BigDecimal.ROUND_HALF_DOWN);
		// TO BASE SCALE
		BigDecimal amount_tx = amount_asset.scaleByPowerOfTen(amount_asset.scale() - scale_default);
		// FROM BASE SCALE to ASSET SCALE
		BigDecimal amount_asset_out = amount_tx.scaleByPowerOfTen(amount_tx.scale() - scalse_asset);


		//CREATE ASSET
		AssetVenture asset = new AssetVenture(maker, "test", icon, image, "strontje", 0, scalse_asset, 10000l);

		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, 0l);
		issueAssetTransaction.setDC(db, false);
		issueAssetTransaction.process(gb, false);
		asset.insertToMap(db, BlockChain.AMOUNT_SCALE_FROM);

		long assetKey = asset.getKey(db);

		long timestamp = NTP.getTime();

		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

		//CREATE VALID ASSET TRANSFER
		R_Send assetTransfer = new R_Send(maker, FEE_POWER, recipient, assetKey, amount_asset, timestamp, 0l);
		assetTransfer.sign(maker, false);

		//CONVERT TO BYTES
		byte[] rawAssetTransfer = assetTransfer.toBytes(true, null);

		//CHECK DATALENGTH
		assertEquals(rawAssetTransfer.length, assetTransfer.getDataLength(false));

		try
		{
			//PARSE FROM BYTES
			R_Send parsedAssetTransfer = (R_Send) TransactionFactory.getInstance().parse(rawAssetTransfer, releaserReference);

			//CHECK INSTANCE
			assertEquals(true, parsedAssetTransfer instanceof R_Send);

			BigDecimal ammountParsed = parsedAssetTransfer.getAmount();
			parsedAssetTransfer.setDC(db, false);
			BigDecimal ammountParsed_inDC = parsedAssetTransfer.getAmount();

			assertEquals(ammountParsed_inDC, amount_asset);


		}
		catch (Exception e)
		{
			fail("Exception while parsing transaction.");
		}

	}

	//ISSUE ASSET TRANSACTION

	@Test
	public void validateSignatureIssueAssetTransaction()
	{

		init();

		//CREATE ASSET
		AssetUnique asset = new AssetUnique(maker, "test", icon, image, "strontje", 0, 8);

		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, 0l);
		issueAssetTransaction.sign(maker, false);

		//CHECK IF ISSUE ASSET TRANSACTION IS VALID
		assertEquals(true, issueAssetTransaction.isSignatureValid(db));

		//INVALID SIGNATURE
		issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db), new byte[64]);

		//CHECK IF ISSUE ASSET IS INVALID
		assertEquals(false, issueAssetTransaction.isSignatureValid(db));
	}

	@Test
	public void parseIssueAssetTransaction()
	{

		init();

		//CREATE SIGNATURE
		AssetUnique assetUni = new AssetUnique(maker, "test", icon, image, "strontje", 0, 8);
		LOGGER.info("asset: " + assetUni.getType()[0] + ", " + assetUni.getType()[1]);
		byte [] rawUni = assetUni.toBytes(false, false);
		assertEquals(rawUni.length, assetUni.getDataLength(false));
		assetUni.setReference(new byte[64]);
		rawUni = assetUni.toBytes(true, false);
		assertEquals(rawUni.length, assetUni.getDataLength(true));

		//CREATE SIGNATURE
		AssetVenture asset = new AssetVenture(maker, "test", icon, image, "strontje", 0, 8, 1000l);
		LOGGER.info("asset: " + asset.getType()[0] + ", " + asset.getType()[1]);
		byte [] raw = asset.toBytes(false, false);
		assertEquals(raw.length, asset.getDataLength(false));
		asset.setReference(new byte[64]);
		raw = asset.toBytes(true, false);
		assertEquals(raw.length, asset.getDataLength(true));

		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, 0l);
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.setDC(db, false);
		issueAssetTransaction.process(gb, false);

		//CONVERT TO BYTES
		byte[] rawIssueAssetTransaction = issueAssetTransaction.toBytes(true, null);

		//CHECK DATA LENGTH
		assertEquals(rawIssueAssetTransaction.length, issueAssetTransaction.getDataLength(false));

		try
		{
			//PARSE FROM BYTES
			IssueAssetTransaction parsedIssueAssetTransaction = (IssueAssetTransaction) TransactionFactory.getInstance().parse(rawIssueAssetTransaction, releaserReference);

			//CHECK INSTANCE
			assertEquals(true, parsedIssueAssetTransaction instanceof IssueAssetTransaction);

			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), parsedIssueAssetTransaction.getSignature()));

			//CHECK ISSUER
			assertEquals(issueAssetTransaction.getCreator().getAddress(), parsedIssueAssetTransaction.getCreator().getAddress());

			//CHECK OWNER
			assertEquals(issueAssetTransaction.getItem().getOwner().getAddress(), parsedIssueAssetTransaction.getItem().getOwner().getAddress());

			//CHECK NAME
			assertEquals(issueAssetTransaction.getItem().getName(), parsedIssueAssetTransaction.getItem().getName());

			//CHECK DESCRIPTION
			assertEquals(issueAssetTransaction.getItem().getDescription(), parsedIssueAssetTransaction.getItem().getDescription());

			//CHECK QUANTITY
			assertEquals(((AssetCls)issueAssetTransaction.getItem()).getQuantity(), ((AssetCls)parsedIssueAssetTransaction.getItem()).getQuantity());

			//SCALE
			assertEquals(((AssetCls)issueAssetTransaction.getItem()).getScale(), ((AssetCls)parsedIssueAssetTransaction.getItem()).getScale());

			//ASSET TYPE
			assertEquals(((AssetCls)issueAssetTransaction.getItem()).getAssetType(), ((AssetCls)parsedIssueAssetTransaction.getItem()).getAssetType());

			//CHECK REFERENCE
			//assertEquals((long)issueAssetTransaction.getReference(), (long)parsedIssueAssetTransaction.getReference());

			//CHECK TIMESTAMP
			assertEquals(issueAssetTransaction.getTimestamp(), parsedIssueAssetTransaction.getTimestamp());
		}
		catch (Exception e)
		{
			fail("Exception while parsing transaction.");
		}

		//PARSE TRANSACTION FROM WRONG BYTES
		rawIssueAssetTransaction = new byte[issueAssetTransaction.getDataLength(false)];

		try
		{
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawIssueAssetTransaction, releaserReference);

			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e)
		{
			//EXCEPTION IS THROWN OK
		}
	}


	@Test
	public void processIssueAssetTransaction()
	{

		init();

		AssetUnique asset = new AssetUnique(maker, "test", icon, image, "strontje", 0, 8);

		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db));
		issueAssetTransaction.sign(maker, false);

		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(releaserReference));

		issueAssetTransaction.process(gb, false);

		LOGGER.info("asset KEY: " + asset.getKey(db));

		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(asset.getKey(db), db));

		//CHECK ASSET EXISTS SENDER
		long key = db.getIssueAssetMap().get(issueAssetTransaction);
		assertEquals(true, db.getItemAssetMap().contains(key));

		//CHECK ASSET IS CORRECT
		assertEquals(true, Arrays.equals(db.getItemAssetMap().get(key).toBytes(true, false), asset.toBytes(true, false)));

		//CHECK ASSET BALANCE SENDER
		assertEquals(true, db.getAssetBalanceMap().get(maker.getAddress(), key).a.b.compareTo(new BigDecimal(asset.getQuantity())) == 0);

		//CHECK REFERENCE SENDER
		assertEquals((long)issueAssetTransaction.getTimestamp(), (long)maker.getLastTimestamp(db));
	}


	@Test
	public void orphanIssueAssetTransaction()
	{

		init();

		AssetUnique asset = new AssetUnique(maker, "test", icon, image, "strontje", 0, 8);

		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(gb, false);
		long key = db.getIssueAssetMap().get(issueAssetTransaction);
		assertEquals(new BigDecimal(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key,db));
		assertEquals((long)issueAssetTransaction.getTimestamp(), (long)maker.getLastTimestamp(db));

		issueAssetTransaction.orphan(false);

		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key,db));

		//CHECK ASSET EXISTS SENDER
		assertEquals(false, db.getItemAssetMap().contains(key));

		//CHECK ASSET BALANCE SENDER
		assertEquals(0, db.getAssetBalanceMap().get(maker.getAddress(), key).a.b.longValue());

		//CHECK REFERENCE SENDER
		//assertEquals(issueAssetTransaction.getReference(), maker.getLastReference(db));
	}


	//TRANSFER ASSET

	@Test
	public void validateSignatureR_Send()
	{

		init();

		AssetUnique asset = new AssetUnique(maker, "test", icon, image, "strontje", 0, 8);

		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(gb, false);
		long key = db.getIssueAssetMap().get(issueAssetTransaction);

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

		//CREATE ASSET TRANSFER
		Transaction assetTransfer = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db));
		assetTransfer.sign(maker, false);

		//CHECK IF ASSET TRANSFER SIGNATURE IS VALID
		assertEquals(true, assetTransfer.isSignatureValid(db));

		//INVALID SIGNATURE
		assetTransfer = new R_Send(maker, FEE_POWER, recipient, 0, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db));
		assetTransfer.sign(maker, false);
		assetTransfer = new R_Send(maker, FEE_POWER, recipient, 0, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp+1, maker.getLastTimestamp(db), assetTransfer.getSignature());

		//CHECK IF ASSET TRANSFER SIGNATURE IS INVALID
		assertEquals(false, assetTransfer.isSignatureValid(db));
	}

	@Test
	public void validateR_Send()
	{

		init();

		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db));
		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(releaserReference));

		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(gb, false);
		long key = asset.getKey(db);
		//assertEquals(asset.getQuantity(), maker.getConfirmedBalance(FEE_KEY, db));
		assertEquals(new BigDecimal(asset.getQuantity()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

		//CREATE VALID ASSET TRANSFER
		Transaction assetTransfer = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp+100, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(releaserReference));

		assetTransfer.sign(maker, false);
		assetTransfer.process(gb, false);

		//CREATE VALID ASSET TRANSFER
		//maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), db);
		assetTransfer = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp+200, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
		assetTransfer = new R_Send(maker, FEE_POWER, new Account("test"), key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
		assetTransfer = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(-100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		assetTransfer = new R_Send(maker, FEE_POWER, recipient, 0, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db));
		//assetTransfer.sign(maker, false);
		//assetTransfer.process(db, false);

		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER WRONG REFERENCE
		assetTransfer = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, -123L);

		//CHECK IF ASSET TRANSFER IS INVALID
		assertNotEquals(Transaction.VALIDATE_OK, assetTransfer.isValid(releaserReference));
	}

	@Test
	public void parseR_Send()
	{

		init();

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();

		//CREATE VALID ASSET TRANSFER
		R_Send assetTransfer = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db));
		assetTransfer.sign(maker, false);

		//CONVERT TO BYTES
		byte[] rawAssetTransfer = assetTransfer.toBytes(true, releaserReference);

		//CHECK DATALENGTH
		assertEquals(rawAssetTransfer.length, assetTransfer.getDataLength(false));

		try
		{
			//PARSE FROM BYTES
			R_Send parsedAssetTransfer = (R_Send) TransactionFactory.getInstance().parse(rawAssetTransfer, releaserReference);

			//CHECK INSTANCE
			assertEquals(true, parsedAssetTransfer instanceof R_Send);

			//CHECK TYPEBYTES
			assertEquals(true, Arrays.equals(assetTransfer.getTypeBytes(), parsedAssetTransfer.getTypeBytes()));

			//CHECK TIMESTAMP
			assertEquals(assetTransfer.getTimestamp(), parsedAssetTransfer.getTimestamp());

			//CHECK REFERENCE
			//assertEquals(assetTransfer.getReference(), parsedAssetTransfer.getReference());

			//CHECK CREATOR
			assertEquals(assetTransfer.getCreator().getAddress(), parsedAssetTransfer.getCreator().getAddress());

			//CHECK FEE POWER
			assertEquals(assetTransfer.getFee(), parsedAssetTransfer.getFee());

			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(assetTransfer.getSignature(), parsedAssetTransfer.getSignature()));

			//CHECK KEY
			assertEquals(assetTransfer.getKey(), parsedAssetTransfer.getKey());

			//CHECK AMOUNT
			assertEquals(assetTransfer.getAmount(maker), parsedAssetTransfer.getAmount(maker));

			//CHECK AMOUNT RECIPIENT
			assertEquals(assetTransfer.getAmount(recipient), parsedAssetTransfer.getAmount(recipient));

		}
		catch (Exception e)
		{
			fail("Exception while parsing transaction." + e);
		}

		//PARSE TRANSACTION FROM WRONG BYTES
		rawAssetTransfer = new byte[assetTransfer.getDataLength(false)];

		try
		{
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawAssetTransfer, releaserReference);

			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e)
		{
			//EXCEPTION IS THROWN OK
		}
	}

	@Test
	public void processR_Send()
	{

		init();

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();

		//CREATE ASSET TRANSFER
		maker.changeBalance(db, false, key, BigDecimal.valueOf(200).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		Transaction assetTransfer = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db));
		assetTransfer.sign(maker, false);
		assetTransfer.isValid(releaserReference);
		assetTransfer.process(gb, false);

		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

		//CHECK REFERENCE SENDER
		assertEquals(assetTransfer.getTimestamp(), maker.getLastTimestamp(db));

		//CHECK REFERENCE RECIPIENT
		assertNotEquals(assetTransfer.getTimestamp(), recipient.getLastTimestamp(db));
	}

	@Test
	public void orphanR_Send()
	{

		init();

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();

		//CREATE ASSET TRANSFER
		long key = 1l;
		maker.changeBalance(db, false, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		Transaction assetTransfer = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), timestamp, maker.getLastTimestamp(db));
		assetTransfer.sign(maker, false);
		assetTransfer.setDC(db, false);
		assetTransfer.process(gb, false);
		assetTransfer.orphan(false);

		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

		//CHECK REFERENCE SENDER
		//assertEquals(assetTransfer.getReference(), maker.getLastReference(db));

		//CHECK REFERENCE RECIPIENT
		assertNotEquals(assetTransfer.getTimestamp(), recipient.getLastTimestamp(db));
	}



	//MESSAGE ASSET

	@Test
	public void validateSignatureMessageTransaction()
	{

		init();

		//AssetUnique asset = new AssetUnique(maker, "test", "strontje");

		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp, maker.getLastTimestamp(db));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(gb, false);
		long key = db.getIssueAssetMap().get(issueAssetTransaction);

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

		//CREATE ASSET TRANSFER
		Transaction messageTransaction = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastTimestamp(db));
		messageTransaction.sign(maker, false);

		//CHECK IF ASSET TRANSFER SIGNATURE IS VALID
		assertEquals(true, messageTransaction.isSignatureValid(db));

		//INVALID SIGNATURE
		messageTransaction = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastTimestamp(db));
		messageTransaction.sign(maker, false);
		messageTransaction = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp+1, maker.getLastTimestamp(db), messageTransaction.getSignature());

		//CHECK IF ASSET TRANSFER SIGNATURE IS INVALID
		assertEquals(false, messageTransaction.isSignatureValid(db));
	}

	@Test
	public void validateMessageTransaction()
	{

		init();

		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueMessageTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp++, maker.getLastTimestamp(db));
		assertEquals(Transaction.VALIDATE_OK, issueMessageTransaction.isValid(releaserReference));

		issueMessageTransaction.sign(maker, false);
		issueMessageTransaction.process(gb, false);
		long key = asset.getKey(db);
		//assertEquals(asset.getQuantity(), maker.getConfirmedBalance(FEE_KEY, db));
		assertEquals(new BigDecimal(asset.getQuantity()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

		//timestamp += 100;
		//CREATE VALID ASSET TRANSFER
		Transaction messageTransaction = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(releaserReference));

		messageTransaction.sign(maker, false);
		messageTransaction.process(gb, false);
		timestamp ++;

		//CREATE VALID ASSET TRANSFER
		//maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), db);
		messageTransaction = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER INVALID RECIPIENT ADDRESS
		messageTransaction = new R_Send(maker, FEE_POWER, new Account("test"), key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS INVALID
		assertEquals(Transaction.INVALID_ADDRESS, messageTransaction.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
		messageTransaction = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(-100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS INVALID
		assertEquals(Transaction.NOT_MOVABLE_ASSET, messageTransaction.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		messageTransaction = new R_Send(maker, FEE_POWER, recipient, 99, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS INVALID
		assertEquals(Transaction.ITEM_ASSET_NOT_EXIST, messageTransaction.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		messageTransaction = new R_Send(maker, FEE_POWER, recipient, key - 1, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS INVALID
		assertEquals(Transaction.NO_BALANCE, messageTransaction.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER WRONG REFERENCE
		messageTransaction = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, -123L);

		//CHECK IF ASSET TRANSFER IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, messageTransaction.isValid(releaserReference));

		// NOT DIVISIBLE
		asset = new AssetVenture(maker, "not divisible", icon, image, "asdasda", 0, 8, 0l);
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp++, maker.getLastTimestamp(db));
		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(releaserReference));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(gb, false);
		Long key_1 = issueAssetTransaction.getAssetKey(db);
		assertEquals(key+1, (long)key_1);
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key_1, db));

		BigDecimal amo = BigDecimal.TEN.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		//CREATE INVALID ASSET TRANSFER WRONG REFERENCE
		messageTransaction = new R_Send(maker, FEE_POWER, recipient, key_1,
				amo,
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));
		assertEquals(Transaction.VALIDATE_OK, messageTransaction.isValid(releaserReference));
		messageTransaction.process(gb, false);

		//CHECK IF UNLIMITED ASSET TRANSFERED with no balance
		assertEquals(BigDecimal.ZERO.subtract(amo), maker.getBalanceUSE(key_1, db));

		// TRY INVALID SEND FRON NOT CREATOR

		messageTransaction = new R_Send(maker_1, FEE_POWER, recipient, key_1,
				amo,
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker_1.getLastTimestamp(db));
		assertEquals(Transaction.NO_BALANCE, messageTransaction.isValid(releaserReference));

		//CHECK IF UNLIMITED ASSET TRANSFERED with no balance
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker_1.getBalanceUSE(key_1, db));

	}

	@Test
	public void parseMessageTransaction()
	{

		init();

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();

		//CREATE VALID ASSET TRANSFER
		R_Send r_Send = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastTimestamp(db));
		r_Send.sign(maker, false);

		//CONVERT TO BYTES
		byte[] rawAssetTransfer = r_Send.toBytes(true, releaserReference);

		//CHECK DATALENGTH
		assertEquals(rawAssetTransfer.length, r_Send.getDataLength(false));

		try
		{
			//PARSE FROM BYTES
			R_Send parsedAssetTransfer = (R_Send) TransactionFactory.getInstance().parse(rawAssetTransfer, releaserReference);

			//CHECK INSTANCE
			assertEquals(true, parsedAssetTransfer instanceof R_Send);

			//CHECK TYPEBYTES
			assertEquals(true, Arrays.equals(r_Send.getTypeBytes(), parsedAssetTransfer.getTypeBytes()));

			//CHECK TIMESTAMP
			assertEquals(r_Send.getTimestamp(), parsedAssetTransfer.getTimestamp());

			//CHECK REFERENCE
			//assertEquals(r_Send.getReference(), parsedAssetTransfer.getReference());

			//CHECK CREATOR
			assertEquals(r_Send.getCreator().getAddress(), parsedAssetTransfer.getCreator().getAddress());

			//CHECK FEE POWER
			assertEquals(r_Send.getFee(), parsedAssetTransfer.getFee());

			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(r_Send.getSignature(), parsedAssetTransfer.getSignature()));

			//CHECK KEY
			assertEquals(r_Send.getKey(), parsedAssetTransfer.getKey());

			//CHECK AMOUNT
			assertEquals(r_Send.getAmount(maker), parsedAssetTransfer.getAmount(maker));

			//CHECK AMOUNT RECIPIENT
			assertEquals(r_Send.getAmount(recipient), parsedAssetTransfer.getAmount(recipient));

		}
		catch (Exception e)
		{
			fail("Exception while parsing transaction." + e);
		}

		//PARSE TRANSACTION FROM WRONG BYTES
		rawAssetTransfer = new byte[r_Send.getDataLength(false)];

		try
		{
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawAssetTransfer, releaserReference);

			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e)
		{
			//EXCEPTION IS THROWN OK
		}
	}

	@Test
	public void processMessageTransaction()
	{

		init();

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();

		//CREATE ASSET TRANSFER
		maker.changeBalance(db, false, key, BigDecimal.valueOf(200).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		Transaction messageTransaction = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastTimestamp(db));
		messageTransaction.sign(maker, false);
		messageTransaction.process(gb, false);

		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

		//CHECK REFERENCE SENDER
		assertEquals(messageTransaction.getTimestamp(), maker.getLastTimestamp(db));

		//CHECK REFERENCE RECIPIENT
		assertNotEquals(messageTransaction.getTimestamp(), recipient.getLastTimestamp(db));
	}

	@Test
	public void orphanMessageTransaction()
	{

		init();

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();

		//CREATE ASSET TRANSFER
		long key = 2l;
		maker.changeBalance(db, false, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);
		Transaction messageTransaction = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp, maker.getLastTimestamp(db));
		messageTransaction.sign(maker, false);
		messageTransaction.setDC(db, false);
		messageTransaction.process(gb, false);
		messageTransaction.orphan(false);

		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

		//CHECK REFERENCE SENDER
		//assertEquals(messageTransaction.getReference(), maker.getLastReference(db));

		//CHECK REFERENCE RECIPIENT
		assertNotEquals(messageTransaction.getTimestamp(), recipient.getLastTimestamp(db));
	}

	/////////////////////////////////////////////
	////////////
	@Test
	public void validate_R_Send_Movable_Asset()
	{

		init();

		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueMessageTransaction = new IssueAssetTransaction(maker, assetMovable, FEE_POWER, timestamp++, maker.getLastTimestamp(db));
		assertEquals(Transaction.VALIDATE_OK, issueMessageTransaction.isValid(releaserReference));

		issueMessageTransaction.sign(maker, false);
		issueMessageTransaction.process(gb, false);
		long key = assetMovable.getKey(db);

		//assertEquals(asset.getQuantity(), maker.getConfirmedBalance(FEE_KEY, db));
		assertEquals(new BigDecimal(assetMovable.getQuantity()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

		//CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
		rsend = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(-100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS INVALID
		assertEquals(Transaction.NO_HOLD_BALANCE, rsend.isValid(releaserReference));

		//CREATE VALID ASSET TRANSFER
		rsend = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, rsend.isValid(releaserReference));

		rsend.sign(maker, false);
		rsend.process(gb, false);

		//NOW IT WILL BE vaLID
		rsend = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(-100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS INVALID
		assertEquals(Transaction.VALIDATE_OK, rsend.isValid(releaserReference));

		timestamp ++;

		//CREATE VALID ASSET TRANSFER
		//maker.setConfirmedBalance(key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), db);
		rsend = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS VALID
		assertEquals(Transaction.VALIDATE_OK, rsend.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER NEGATIVE AMOUNT
		rsend = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(-100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS INVALID
		assertEquals(Transaction.NO_HOLD_BALANCE, rsend.isValid(releaserReference));



		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		rsend = new R_Send(maker, FEE_POWER, recipient, 99, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS INVALID
		assertEquals(Transaction.ITEM_ASSET_NOT_EXIST, rsend.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER NOT ENOUGH ASSET BALANCE
		rsend = new R_Send(maker, FEE_POWER, recipient, key - 1, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));

		//CHECK IF ASSET TRANSFER IS INVALID
		assertEquals(Transaction.NO_BALANCE, rsend.isValid(releaserReference));

		//CREATE INVALID ASSET TRANSFER WRONG REFERENCE
		rsend = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, -123L);

		//CHECK IF ASSET TRANSFER IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, rsend.isValid(releaserReference));

		// NOT DIVISIBLE
		asset = new AssetVenture(maker, "not divisible", icon, image, "asdasda", 0, 8, 0l);
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, asset, FEE_POWER, timestamp++, maker.getLastTimestamp(db));
		assertEquals(Transaction.VALIDATE_OK, issueAssetTransaction.isValid(releaserReference));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(gb, false);
		Long key_1 = issueAssetTransaction.getAssetKey(db);
		assertEquals(key+1, (long)key_1);
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key_1, db));

		BigDecimal amo = BigDecimal.TEN.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
		//CREATE INVALID ASSET TRANSFER WRONG REFERENCE
		rsend = new R_Send(maker, FEE_POWER, recipient, key_1,
				amo,
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));
		assertEquals(Transaction.VALIDATE_OK, rsend.isValid(releaserReference));
		rsend.process(gb, false);

		//CHECK IF UNLIMITED ASSET TRANSFERED with no balance
		assertEquals(BigDecimal.ZERO.subtract(amo), maker.getBalanceUSE(key_1, db));

		// TRY INVALID SEND FRON NOT CREATOR

		rsend = new R_Send(maker_1, FEE_POWER, recipient, key_1,
				amo,
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker_1.getLastTimestamp(db));
		assertEquals(Transaction.NO_BALANCE, rsend.isValid(releaserReference));

		//CHECK IF UNLIMITED ASSET TRANSFERED with no balance
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker_1.getBalanceUSE(key_1, db));

	}

	@Test
	public void process_Movable_Asset()
	{

		init();
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(maker, assetMovable, FEE_POWER, timestamp++, maker.getLastTimestamp(db));
		issueAssetTransaction.sign(maker, false);
		issueAssetTransaction.process(gb, false);

		key = assetMovable.getKey(db);

		//CREATE SIGNATURE
		Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
		long timestamp = NTP.getTime();

		// SET BALANCES
		db.getAssetBalanceMap().set(maker.getAddress(), key, new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>(
				new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.valueOf(20).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)),
				new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)),
				new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)),
				new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)),
				new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.valueOf(0).setScale(BlockChain.AMOUNT_DEDAULT_SCALE))
				)
				);

		//CREATE ASSET TRANSFER
		rsend = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));
		assertEquals(rsend.isValid(releaserReference), Transaction.NO_BALANCE);

		rsend = new R_Send(maker, FEE_POWER, recipient, key, BigDecimal.valueOf(25).setScale(BlockChain.AMOUNT_DEDAULT_SCALE),
				"headdd", "wqeszcssd234".getBytes(), new byte[]{1}, new byte[]{1},
				timestamp++, maker.getLastTimestamp(db));
		assertEquals(rsend.isValid(releaserReference), Transaction.VALIDATE_OK);


		rsend.sign(maker, false);
		rsend.process(gb, false);

		balance5 = maker.getBalance(db, key);

		//CHECK BALANCE SENDER

		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), balance5.c);
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

		//CHECK REFERENCE SENDER
		assertEquals(rsend.getTimestamp(), maker.getLastTimestamp(db));

		//CHECK REFERENCE RECIPIENT
		assertNotEquals(rsend.getTimestamp(), recipient.getLastTimestamp(db));

		//////////////////////////////////////////////////
		/// ORPHAN
		/////////////////////////////////////////////////
		rsend.orphan(false);

		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(key, db));

		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(FEE_KEY, db));
		assertEquals(BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(key, db));

		//CHECK REFERENCE SENDER
		//assertEquals(rsend.getReference(), maker.getLastReference(db));

		//CHECK REFERENCE RECIPIENT
		assertNotEquals(rsend.getTimestamp(), recipient.getLastTimestamp(db));
	}


}
