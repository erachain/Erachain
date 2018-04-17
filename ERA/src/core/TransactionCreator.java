package core;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
//import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.primitives.Bytes;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Base58;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.item.assets.Order;
import core.item.imprints.Imprint;
import core.item.imprints.ImprintCls;
import core.item.persons.PersonCls;
import core.item.persons.PersonHuman;
import core.item.statuses.Status;
import core.item.statuses.StatusCls;
import core.item.templates.Template;
import core.item.templates.TemplateCls;
import core.item.unions.Union;
import core.item.unions.UnionCls;
import core.naming.Name;
import core.naming.NameSale;
import core.payment.Payment;
import core.transaction.ArbitraryTransactionV3;
import core.transaction.BuyNameTransaction;
import core.transaction.CancelOrderTransaction;
import core.transaction.CancelSellNameTransaction;
import core.transaction.CreateOrderTransaction;
import core.transaction.CreatePollTransaction;
import core.transaction.DeployATTransaction;
import core.transaction.IssueAssetTransaction;
import core.transaction.IssueImprintRecord;
import core.transaction.IssuePersonRecord;
import core.transaction.IssueStatusRecord;
import core.transaction.IssueTemplateRecord;
import core.transaction.IssueUnionRecord;
import core.transaction.Issue_ItemRecord;
import core.transaction.MultiPaymentTransaction;
import core.transaction.R_Hashes;
import core.transaction.R_Send;
import core.transaction.R_SertifyPubKeys;
import core.transaction.R_SetStatusToItem;
import core.transaction.R_SignNote;
import core.transaction.R_Vouch;
import core.transaction.RegisterNameTransaction;
import core.transaction.SellNameTransaction;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import core.transaction.UpdateNameTransaction;
import core.transaction.VoteOnPollTransaction;
import core.voting.Poll;
import datachain.DCSet;
import datachain.TransactionMap;
import ntp.NTP;
//import settings.Settings;
import utils.Pair;
import utils.TransactionTimestampComparator;

/// icreator -
public class TransactionCreator
{
	private DCSet fork;
	private Block lastBlock;

	//private byte[] icon = new byte[0]; // default value
	//private byte[] image = new byte[0]; // default value

	private void checkUpdate()
	{
		//CHECK IF WE ALREADY HAVE A FORK
		if(this.lastBlock == null || this.fork == null)
		{
			updateFork();
		}
		else
		{
			//CHECK IF WE NEED A NEW FORK
			if(!Arrays.equals(this.lastBlock.getSignature(), Controller.getInstance().getLastBlock().getSignature()))
			{
				updateFork();
			}
		}
	}

	private void updateFork()
	{
		//CREATE NEW FORK
		if (this.fork != null) {
			this.fork.close();
		}
		this.fork = DCSet.getInstance().fork();

		//UPDATE LAST BLOCK
		this.lastBlock = Controller.getInstance().getLastBlock();

		//SCAN UNCONFIRMED TRANSACTIONS FOR TRANSACTIONS WHERE ACCOUNT IS CREATOR OF
		///List<Transaction> transactions = (List<Transaction>)this.fork.getTransactionMap().getValuesAll();
		TransactionMap transactionMap = this.fork.getTransactionMap();
		Iterator<byte[]> iterator = transactionMap.getIterator(0, false);
		Transaction transaction;
		List<Account> accountMap = Controller.getInstance().getAccounts();

		List<Transaction> accountTransactions = new ArrayList<Transaction>();

		while(iterator.hasNext())
		{
			transaction = transactionMap.get(iterator.next());
			if(accountMap.contains(transaction.getCreator()))
			{
				accountTransactions.add(transaction);
			}
		}

		//SORT THEM BY TIMESTAMP
		Collections.sort(accountTransactions, new TransactionTimestampComparator());

		//VALIDATE AND PROCESS THOSE TRANSACTIONS IN FORK for recalc last reference
		for(Transaction transactionAccount: accountTransactions)
		{
			if(!transactionAccount.isSignatureValid(this.fork)) {
				//THE TRANSACTION BECAME INVALID LET
				this.fork.getTransactionMap().delete(transactionAccount);
			} else {
				transactionAccount.setDC(this.fork, false);
				if(transactionAccount.isValid(this.fork, null) == Transaction.VALIDATE_OK)
				{
					transactionAccount.process(null, false);
				} else {
					//THE TRANSACTION BECAME INVALID LET
					this.fork.getTransactionMap().delete(transactionAccount);
				}
			}
		}
	}

	public long getReference(PublicKeyAccount creator)
	{
		this.checkUpdate();
		return 0l;
	}

	public Pair<Transaction, Integer> createNameRegistration(PrivateKeyAccount creator, Name name, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		//CREATE NAME REGISTRATION
		RegisterNameTransaction nameRegistration = new RegisterNameTransaction(creator, name, (byte)feePow, time, 0l);
		nameRegistration.sign(creator, false);
		nameRegistration.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		return new Pair<Transaction, Integer>(nameRegistration, this.afterCreate(nameRegistration, false));
	}

	public Pair<Transaction, Integer> createNameUpdate(PrivateKeyAccount creator, Name name, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		//CREATE NAME UPDATE
		UpdateNameTransaction nameUpdate = new UpdateNameTransaction(creator, name, (byte)feePow, time, 0l);
		nameUpdate.sign(creator, false);
		nameUpdate.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		return new Pair<Transaction, Integer>(nameUpdate, this.afterCreate(nameUpdate, false));
	}
	public Pair<Transaction, Integer> createNameSale(PrivateKeyAccount creator, NameSale nameSale, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		//CREATE NAME SALE
		SellNameTransaction nameSaleTransaction = new SellNameTransaction(creator, nameSale, (byte)feePow, time, 0l);
		nameSaleTransaction.sign(creator, false);
		nameSaleTransaction.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		return new Pair<Transaction, Integer>(nameSaleTransaction, this.afterCreate(nameSaleTransaction, false));
	}
	public Pair<Transaction, Integer> createCancelNameSale(PrivateKeyAccount creator, NameSale nameSale, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		//CREATE CANCEL NAME SALE
		CancelSellNameTransaction cancelNameSaleTransaction = new CancelSellNameTransaction(creator, nameSale.getKey(), (byte)feePow, time, 0l);
		cancelNameSaleTransaction.sign(creator, false);
		cancelNameSaleTransaction.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		return new Pair<Transaction, Integer>(cancelNameSaleTransaction, this.afterCreate(cancelNameSaleTransaction, false));
	}

	public Pair<Transaction, Integer> createNamePurchase(PrivateKeyAccount creator, NameSale nameSale, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		//CREATE NAME PURCHASE
		BuyNameTransaction namePurchase = new BuyNameTransaction(creator, nameSale, nameSale.getName().getOwner(), (byte)feePow, time, 0l);
		namePurchase.sign(creator, false);
		namePurchase.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		return new Pair<Transaction, Integer>(namePurchase, this.afterCreate(namePurchase, false));
	}

	public Transaction createPollCreation(PrivateKeyAccount creator, Poll poll, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		//CREATE POLL CREATION
		CreatePollTransaction pollCreation = new CreatePollTransaction(creator, poll, (byte)feePow, time, 0l);
		pollCreation.sign(creator, false);
		pollCreation.setDC(this.fork, false);

		return pollCreation;

	}


	public Pair<Transaction, Integer> createPollVote(PrivateKeyAccount creator, String poll, int optionIndex, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();


		//CREATE POLL VOTE
		VoteOnPollTransaction pollVote = new VoteOnPollTransaction(creator, poll, optionIndex, (byte)feePow, time, 0l);
		pollVote.sign(creator, false);
		pollVote.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		return new Pair<Transaction, Integer>(pollVote, this.afterCreate(pollVote, false));
	}


	public Pair<Transaction, Integer> createArbitraryTransaction(PrivateKeyAccount creator, List<Payment> payments, int service, byte[] data, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		Transaction arbitraryTransaction;
		arbitraryTransaction = new ArbitraryTransactionV3(creator, payments, service, data, (byte)feePow, time, 0l);
		arbitraryTransaction.sign(creator, false);
		arbitraryTransaction.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		return new Pair<Transaction, Integer>(arbitraryTransaction, this.afterCreate(arbitraryTransaction, false));
	}


	public Transaction createIssueAssetTransaction(PrivateKeyAccount creator, String name, String description,
			byte[] icon, byte[] image,
			int scale, int asset_type, long quantity, int feePow)
	{
		//CHECK FOR UPDATES
		// all unconfirmed records insert in FORK for calc last account REFERENCE
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		AssetCls asset = new AssetVenture(creator, name, icon, image, description, scale, asset_type, quantity);

		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(creator, asset, (byte)feePow, time, 0l);
		issueAssetTransaction.sign(creator, false);
		issueAssetTransaction.setDC(this.fork, false);

		return issueAssetTransaction;
	}

	public Pair<Transaction, Integer> createIssueImprintTransaction(PrivateKeyAccount creator, String name, String description,
			byte[] icon, byte[] image,
			int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		ImprintCls imprint = new Imprint(creator, name, icon, image, description);

		//CREATE ISSUE IMPRINT TRANSACTION
		IssueImprintRecord issueImprintRecord = new IssueImprintRecord(creator, imprint, (byte)feePow, time);
		issueImprintRecord.sign(creator, false);
		issueImprintRecord.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		return new Pair<Transaction, Integer>(issueImprintRecord, this.afterCreate(issueImprintRecord, false));
	}

	public Transaction createIssueTemplateTransaction(PrivateKeyAccount creator, String name, String description,
			byte[] icon, byte[] image,
			int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		TemplateCls template = new Template(creator, name, icon, image, description);

		//CREATE ISSUE PLATE TRANSACTION
		IssueTemplateRecord issueTemplateRecord = new IssueTemplateRecord(creator, template, (byte)feePow, time, 0l);
		issueTemplateRecord.sign(creator, false);
		issueTemplateRecord.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		return issueTemplateRecord;
	}

	public Pair<Transaction, Integer> createIssuePersonTransaction(
			boolean forIssue,
			PrivateKeyAccount creator, String fullName, int feePow, long birthday, long deathday,
			byte gender, String race, float birthLatitude, float birthLongitude,
			String skinColor, String eyeColor, String hairСolor, int height,
			byte[] icon, byte[] image, String description,
			PublicKeyAccount owner, byte[] ownerSignature)
	{

		this.checkUpdate();

		//CHECK FOR UPDATES
		if (forIssue) {

			// IF has not DUPLICATE in UNCONFIRMED RECORDS
			TransactionMap unconfirmedMap = DCSet.getInstance().getTransactionMap();
			for (Transaction record: unconfirmedMap.getValuesAll()) {
				if (record.getType() == Transaction.ISSUE_PERSON_TRANSACTION) {
					if (record instanceof IssuePersonRecord) {
						IssuePersonRecord issuePerson = (IssuePersonRecord) record;
						if (issuePerson.getItem().getName().equals(fullName)) {
							return new Pair<Transaction, Integer>(null, Transaction.ITEM_DUPLICATE);
						}
					}
				}
			}
		}

		//TIME
		long time = NTP.getTime();

		PersonCls person = new PersonHuman(owner, fullName, birthday, deathday,
				gender, race, birthLatitude, birthLongitude,
				skinColor, eyeColor, hairСolor, height, icon, image, description, ownerSignature);

		long lastReference;
		if (forIssue) {
			lastReference = 0l;

		} else {
			lastReference = time - 1000l;
		}
		//CREATE ISSUE PLATE TRANSACTION
		IssuePersonRecord issuePersonRecord = new IssuePersonRecord(creator, person, (byte)feePow, time, lastReference);
		issuePersonRecord.sign(creator, false);
		issuePersonRecord.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		if (forIssue) {
			boolean asPack = false;
			return new Pair<Transaction, Integer>(issuePersonRecord, 1);//this.afterCreate(issuePersonRecord, asPack));
		} else {
			// for COPY -
			int valid = issuePersonRecord.isValid(this.fork, lastReference);
			if (valid == Transaction.NOT_ENOUGH_FEE
					|| valid == Transaction.CREATOR_NOT_PERSONALIZED) {
				valid = Transaction.VALIDATE_OK;
			}

			return new Pair<Transaction, Integer>(issuePersonRecord, valid);
		}
	}

	public Pair<Transaction, Integer> createIssuePersonHumanTransaction(
			PrivateKeyAccount creator, int feePow, PersonHuman human)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		// IF has not DUPLICATE in UNCONFIRMED RECORDS
		TransactionMap unconfirmedMap = DCSet.getInstance().getTransactionMap();
		for (Transaction record: unconfirmedMap.getValuesAll()) {
			if (record.getType() == Transaction.ISSUE_PERSON_TRANSACTION) {
				if (record instanceof IssuePersonRecord) {
					IssuePersonRecord issuePerson = (IssuePersonRecord) record;
					if (issuePerson.getItem().getName().equals(human.getName())) {
						return new Pair<Transaction, Integer>(null, Transaction.ITEM_DUPLICATE);
					}
				}
			}
		}

		//TIME
		long time = NTP.getTime();

		long lastReference;
		lastReference = 0l;

		//CREATE ISSUE PLATE TRANSACTION
		IssuePersonRecord issuePersonRecord = new IssuePersonRecord(creator, human, (byte)feePow, time, lastReference);
		issuePersonRecord.sign(creator, false);
		issuePersonRecord.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		boolean asPack = false;
		return new Pair<Transaction, Integer>(issuePersonRecord, 1);

	}

	public Transaction createIssueStatusTransaction(PrivateKeyAccount creator, String name, String description,
			byte[] icon, byte[] image,
			boolean unique, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		StatusCls status = new Status(creator, name, icon, image, description, unique);

		//CREATE ISSUE PLATE TRANSACTION
		IssueStatusRecord issueStatusRecord = new IssueStatusRecord(creator, status, (byte)feePow, time, 0l);
		issueStatusRecord.sign(creator, false);
		issueStatusRecord.setDC(this.fork, false);

		return issueStatusRecord;
	}

	public Transaction createIssueUnionTransaction(PrivateKeyAccount creator, String name, long birthday, long parent, String description,
			byte[] icon, byte[] image,
			int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		UnionCls union = new Union(creator, name, birthday, parent, icon, image, description);

		//CREATE ISSUE PLATE TRANSACTION
		IssueUnionRecord issueUnionRecord = new IssueUnionRecord(creator, union, (byte)feePow, time, 0l);
		issueUnionRecord.sign(creator, false);
		issueUnionRecord.setDC(this.fork, false);

		return issueUnionRecord;

	}

	public Transaction createOrderTransaction(PrivateKeyAccount creator, AssetCls have, AssetCls want, BigDecimal amountHave, BigDecimal amounWant, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		//CREATE ORDER TRANSACTION
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(creator, have.getKey(), want.getKey(), amountHave, amounWant, (byte)feePow, time, 0l);

		/*
		int res = createOrderTransaction.isValid(this.fork, null);
		if (res != Transaction.VALIDATE_OK)
			return null;
		 */

		//VALIDATE AND PROCESS
		createOrderTransaction.sign(creator, false);
		createOrderTransaction.setDC(this.fork, false);

		return createOrderTransaction;
	}

	public Pair<Transaction, Integer> createCancelOrderTransaction(PrivateKeyAccount creator, Order order, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		//CREATE PRDER TRANSACTION
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(creator, order.getId(), (byte)feePow, time, 0l);
		cancelOrderTransaction.sign(creator, false);
		cancelOrderTransaction.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		return new Pair<Transaction, Integer>(cancelOrderTransaction, this.afterCreate(cancelOrderTransaction, false));
	}

	public Pair<Transaction, Integer> sendMultiPayment(PrivateKeyAccount creator, List<Payment> payments, int feePow)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		//CREATE MULTI PAYMENTS
		MultiPaymentTransaction multiPayment = new MultiPaymentTransaction(creator, payments, (byte)feePow, time, 0l);
		multiPayment.sign(creator, false);
		multiPayment.setDC(this.fork, false);

		//VALIDATE AND PROCESS
		return new Pair<Transaction, Integer>(multiPayment, this.afterCreate(multiPayment, false));
	}

	public Pair<Transaction, Integer> deployATTransaction(PrivateKeyAccount creator, String name, String description, String type, String tags, byte[] creationBytes, BigDecimal amount, int feePow )
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//TIME
		long time = NTP.getTime();

		//DEPLOY AT
		DeployATTransaction deployAT = new DeployATTransaction(creator, name, description, type, tags, creationBytes, amount, (byte)feePow, time, 0l);
		deployAT.sign(creator, false);
		deployAT.setDC(this.fork, false);

		return new Pair<Transaction, Integer>(deployAT, this.afterCreate(deployAT, false));

	}

	//public Pair<Transaction, Integer> r_Send(PrivateKeyAccount creator,

	public Transaction r_Send(PrivateKeyAccount creator,
			Account recipient, long key, BigDecimal amount, int feePow, String head, byte[] isText,
			byte[] message, byte[] encryptMessage) {

		this.checkUpdate();

		Transaction messageTx;

		long timestamp = NTP.getTime();

		//CREATE MESSAGE TRANSACTION
		//messageTx = new R_Send(creator, (byte)feePow, recipient, key, amount, head, message, isText, encryptMessage, timestamp, 0l);
		messageTx = new R_Send(creator, (byte)feePow, recipient, key, amount, head, message, isText, encryptMessage, timestamp, 0l);
		messageTx.sign(creator, false);
		messageTx.setDC(this.fork, false);

		return messageTx;// new Pair<Transaction, Integer>(messageTx, afterCreate(messageTx, false));
	}

	public Transaction r_Send(byte version, byte property1, byte property2,
			PrivateKeyAccount creator,
			Account recipient, long key, BigDecimal amount, int feePow, String head, byte[] isText,
			byte[] message, byte[] encryptMessage) {

		this.checkUpdate();

		Transaction messageTx;

		long timestamp = NTP.getTime();

		//CREATE MESSAGE TRANSACTION
		messageTx = new R_Send(version, property1, property2, creator, (byte)feePow, recipient, key, amount, head, message, isText, encryptMessage, timestamp, 0l);
		messageTx.sign(creator, false);
		messageTx.setDC(this.fork, false);

		return messageTx;
	}

	public Transaction r_SignNote(byte version, byte property1, byte property2,
			boolean asPack, PrivateKeyAccount creator,
			int feePow, long key, byte[] message, byte[] isText, byte[] encrypted) {

		this.checkUpdate();

		Transaction recordNoteTx;

		long timestamp = NTP.getTime();

		//CREATE MESSAGE TRANSACTION
		recordNoteTx = new R_SignNote(version, property1, property1,
				creator, (byte)feePow, key, message, isText, encrypted, timestamp, 0l);
		recordNoteTx.sign(creator, asPack);
		recordNoteTx.setDC(this.fork, asPack);

		return 	recordNoteTx;

	}

	public Transaction r_SertifyPerson(int version, boolean asPack,
			PrivateKeyAccount creator, int feePow, long key,
			List<PublicKeyAccount> userAccounts,
			int add_day) {

		this.checkUpdate();

		Transaction record;

		long timestamp = NTP.getTime();

		//CREATE SERTIFY PERSON TRANSACTION
		//int version = 5; // without user sign
		record = new R_SertifyPubKeys(version, creator, (byte)feePow, key,
				userAccounts,
				add_day, timestamp, 0l);
		record.sign(creator, asPack);
		record.setDC(this.fork, asPack);

		return record;
	}

	public Transaction r_Vouch(int version, boolean asPack,
			PrivateKeyAccount creator, int feePow,
			int height, int seq) {

		this.checkUpdate();

		Transaction record;

		long timestamp = NTP.getTime();

		//CREATE SERTIFY PERSON TRANSACTION
		//int version = 5; // without user sign
		record = new R_Vouch(creator, (byte)feePow,
				height, seq,
				timestamp, 0l);
		record.sign(creator, asPack);
		record.setDC(this.fork, asPack);

		return record;
	}

	public Pair<Transaction, Integer> r_Hashes(PrivateKeyAccount creator, int feePow,
			String urlStr, String dataStr, String[] hashes58) {

		this.checkUpdate();

		Transaction messageTx;

		long timestamp = NTP.getTime();

		byte[] url = urlStr.getBytes(StandardCharsets.UTF_8);
		byte[] data = dataStr.getBytes(StandardCharsets.UTF_8);

		byte[][] hashes = new byte[hashes58.length][32];
		for (int i=0; i < hashes58.length; i++ ) {
			hashes[i] = Bytes.ensureCapacity(Base58.decode(hashes58[i]), 32, 0);
		}

		//CREATE MESSAGE TRANSACTION
		messageTx = new R_Hashes(creator, (byte)feePow, url, data, hashes, timestamp, 0l);
		messageTx.sign(creator, false);
		messageTx.setDC(this.fork, false);

		return new Pair<Transaction, Integer>(messageTx, afterCreate(messageTx, false));
	}
	public Pair<Transaction, Integer> r_Hashes(PrivateKeyAccount creator, int feePow,
			String urlStr, String dataStr, String hashesStr) {

		String[] hashes58;
		if (hashesStr.length() > 0) {
			//hashes58 = hashesStr.split(" -");
			hashes58 = hashesStr.split(" ");
		} else {
			hashes58 = new String[0];
		}
		return r_Hashes(creator, feePow,
				urlStr, dataStr, hashes58);
	}


	/*
	// version 1
	public Pair<Transaction, Integer> r_SetStatusToItem(int version, boolean asPack,
			PrivateKeyAccount creator, int feePow, long key, ItemCls item,
			Long beg_date, Long end_date,
			int value_1, int value_2, byte[] data, long refParent
			) {

		this.checkUpdate();

		Transaction record;

		long timestamp = NTP.getTime();

		//CREATE SERTIFY PERSON TRANSACTION
		//int version = 5; // without user sign
		record = new R_SetStatusToItem(creator, (byte)feePow, key, item.getItemTypeInt(), item.getKey(),
				beg_date, end_date, value_1, value_2, data, refParent,
				timestamp, 0l);
		record.sign(creator, asPack);
		record.setDB(this.fork, asPack);

		return afterCreate(record, asPack);
	}
	 */

	// version 2
	public Transaction r_SetStatusToItem(int version, boolean asPack,
			PrivateKeyAccount creator, int feePow, long key, ItemCls item,
			Long beg_date, Long end_date,
			long value_1, long value_2, byte[] data_1, byte[] data_2, long refParent, byte[] descr
			) {

		this.checkUpdate();

		Transaction record;

		long timestamp = NTP.getTime();

		//CREATE SERTIFY PERSON TRANSACTION
		//int version = 5; // without user sign
		record = new R_SetStatusToItem(creator, (byte)feePow, key, item.getItemTypeInt(), item.getKey(),
				beg_date, end_date, value_1, value_2, data_1, data_2, refParent, descr,
				timestamp, 0l);
		record.sign(creator, asPack);
		record.setDC(this.fork, asPack);

		return  record;
	}

	/*
	public Pair<Transaction, Integer> createJson(PrivateKeyAccount creator,
			Account recipient, long key, BigDecimal amount, int feePow, byte[] isText,
			byte[] message, byte[] encryptMessage) {

		this.checkUpdate();

		Transaction messageTx;

		long timestamp = NTP.getTime();

		//CREATE MESSAGE TRANSACTION
		messageTx = new JsonTransaction(creator, recipient, key, amount, (byte)feePow, message, isText, encryptMessage, timestamp, 0l);
		messageTx.sign(creator);
		record.setDB(this.fork, asPack);

		return afterCreate(messageTx);
	}

	public Pair<Transaction, Integer> createAccounting(PrivateKeyAccount sender,
			Account recipient, long key, BigDecimal amount, int feePow, byte[] isText,
			byte[] message, byte[] encryptMessage) {

		this.checkUpdate();

		long timestamp = NTP.getTime();

		//CREATE ACCOunting TRANSACTION
		Transaction messageTx = new AccountingTransaction(sender, (byte)feePow, recipient, key, amount, message, isText, encryptMessage, timestamp, sender.getLastReference(this.fork));
		messageTx.sign(sender);
		record.setDB(this.fork, asPack);

		return afterCreate(messageTx);
	}

	public Pair<Transaction, Integer> createJson1(PrivateKeyAccount creator,
			Account recipient, long key, BigDecimal amount, int feePow, byte[] isText,
			byte[] message, byte[] encryptMessage) {

		this.checkUpdate();

		long timestamp = NTP.getTime();

		//CREATE MESSAGE TRANSACTION
		Transaction messageTx = new JsonTransaction(creator, recipient, key, amount, (byte)feePow, message, isText, encryptMessage, timestamp, 0l);
		messageTx.sign(creator);
		record.setDB(this.fork, asPack);

		return afterCreate(messageTx);
	}
	 */

	public Pair<Transaction, Integer> createTransactionFromRaw(byte[] rawData)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();

		//CREATE TRANSACTION FROM RAW
		Transaction transaction;
		try {
			transaction = TransactionFactory.getInstance().parse(rawData, null);
		} catch (Exception e) {
			return new Pair<Transaction, Integer>(null, Transaction.INVALID_RAW_DATA);
		}

		//VALIDATE AND PROCESS
		return new Pair<Transaction, Integer>(transaction, this.afterCreate(transaction, false));
	}

	public Integer afterCreate(Transaction transaction, boolean asPack)
	{
		//CHECK IF PAYMENT VALID
		transaction.setDC(this.fork, asPack);
		int valid = transaction.isValid(this.fork, null);

		if(valid == Transaction.VALIDATE_OK)
		{

			if (!asPack) {
				//PROCESS IN FORK
				transaction.process(null, asPack);

				// if it ISSUE - reset key
				if (transaction instanceof Issue_ItemRecord) {
					Issue_ItemRecord issueItem = (Issue_ItemRecord)transaction;
					issueItem.getItem().resetKey();
				}

				//CONTROLLER ONTRANSACTION
				Controller.getInstance().onTransactionCreate(transaction);
			}
		}

		//RETURN
		return valid;
	}

	public Integer afterCreateRaw(Transaction transaction, boolean asPack)
	{
		this.checkUpdate();
		return this.afterCreate(transaction, asPack);
	}

}
