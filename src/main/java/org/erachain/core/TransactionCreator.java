package org.erachain.core;

import com.google.common.primitives.Bytes;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.imprints.Imprint;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.statuses.Status;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.Template;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.item.unions.Union;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.naming.Name;
import org.erachain.core.naming.NameSale;
import org.erachain.core.payment.Payment;
import org.erachain.core.transaction.*;
import org.erachain.core.voting.Poll;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.ntp.NTP;
import org.erachain.utils.Pair;
import org.erachain.utils.TransactionTimestampComparator;
import org.mapdb.DB;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * класс для создания транзакций в текущем кошельке
 * Использует форкнутую базу цепочки
 *
 * TODO: - поидее его надо перенести в модуль Кошелек - ибо без кошелька он не будет пахать.
 */
@Slf4j
public class TransactionCreator {
    private DCSet fork;
    DB database;
    private Block lastBlock;
    private int blockHeight;
    private int seqNo;

    //private byte[] icon = new byte[0]; // default value
    //private byte[] image = new byte[0]; // default value

    // must be a SYNCHRONIZED
    private synchronized void checkUpdate() {
        //CHECK IF WE ALREADY HAVE A FORK
        if (this.lastBlock == null || this.fork == null) {
            updateFork();
        } else {
            //CHECK IF WE NEED A NEW FORK
            if (!Arrays.equals(this.lastBlock.getSignature(), Controller.getInstance().getLastBlock().getSignature())) {
                updateFork();
            }
        }
    }

    private synchronized void updateFork() {
        //CREATE NEW FORK
        if (this.database != null) {
            // закроем сам файл базы - закрывать DCSet.fork - не нужно - он сам очистится
            this.database.close();
        }

        // создаем в памяти базу - так как она на 1 блок только нужна - а значит много памяти не возьмет
        this.database = DCSet.makeDBinMemory();
        this.fork = DCSet.getInstance().fork(database);

        //UPDATE LAST BLOCK
        this.lastBlock = Controller.getInstance().getLastBlock();
        this.blockHeight = this.fork.getBlockMap().size() + 1;
        this.seqNo = 0; // reset sequence number

        //SCAN UNCONFIRMED TRANSACTIONS FOR TRANSACTIONS WHERE ACCOUNT IS CREATOR OF
        ///List<Transaction> transactions = (List<Transaction>)this.fork.getTransactionMap().getValuesAll();
        TransactionMap transactionTab = this.fork.getTransactionTab();
        List<Transaction> accountTransactions = new ArrayList<Transaction>();
        Transaction transaction;

        if (false) {
            // У форка нет вторичных индексов поэтому этот вариант не покатит
            for (Account account: Controller.getInstance().getAccounts()) {
                try (IteratorCloseable<Long> iterator = transactionTab.findTransactionsKeys(account.getAddress(), null, null,
                        0, false, 0, 0, 0L)) {
                    while (iterator.hasNext()) {
                        transaction = transactionTab.get(iterator.next());
                        accountTransactions.add(transaction);
                    }
                } catch (java.lang.Throwable e) {
                    if (e instanceof java.lang.IllegalAccessError) {
                        // налетели на закрытую таблицу
                    } else {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

        } else {
            // здесь нужен протокольный итератор!

            try (IteratorCloseable<Long> iterator = transactionTab.getIterator()) {
                List<Account> accountMap = Controller.getInstance().getAccounts();

                while (iterator.hasNext()) {
                    transaction = transactionTab.get(iterator.next());
                    if (accountMap.contains(transaction.getCreator())) {
                        accountTransactions.add(transaction);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //SORT THEM BY TIMESTAMP
        Collections.sort(accountTransactions, new TransactionTimestampComparator());

        //VALIDATE AND PROCESS THOSE TRANSACTIONS IN FORK for recalc last reference
        for (Transaction transactionAccount : accountTransactions) {

            try {
                transactionAccount.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);
                if (!transactionAccount.isSignatureValid(this.fork)) {
                    //THE TRANSACTION BECAME INVALID LET
                    this.fork.getTransactionTab().delete(transactionAccount);
                } else {
                    if (transactionAccount.isValid(Transaction.FOR_NETWORK, 0l) == Transaction.VALIDATE_OK) {
                        transactionAccount.process(null, Transaction.FOR_NETWORK);
                    } else {
                        //THE TRANSACTION BECAME INVALID LET
                        this.fork.getTransactionTab().delete(transactionAccount);
                    }
                    // CLEAR for HEAP
                    transactionAccount.setDC(null);
                }
            } catch (java.lang.Throwable e) {
                if (e instanceof java.lang.IllegalAccessError) {
                    // налетели на закрытую таблицу
                } else {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public DCSet getFork() {
        if (this.fork == null) {
            updateFork();
        }
        return this.fork;
    }

    public long getReference(PublicKeyAccount creator) {
        this.checkUpdate();
        return 0l;
    }

    public Pair<Transaction, Integer> createNameRegistration(PrivateKeyAccount creator, Name name, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE NAME REGISTRATION
        RegisterNameTransaction nameRegistration = new RegisterNameTransaction(creator, name, (byte) feePow, time, 0l);
        nameRegistration.sign(creator, Transaction.FOR_NETWORK);
        nameRegistration.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(nameRegistration, this.afterCreate(nameRegistration, Transaction.FOR_NETWORK));
    }

    public Pair<Transaction, Integer> createNameUpdate(PrivateKeyAccount creator, Name name, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE NAME UPDATE
        UpdateNameTransaction nameUpdate = new UpdateNameTransaction(creator, name, (byte) feePow, time, 0l);
        nameUpdate.sign(creator, Transaction.FOR_NETWORK);
        nameUpdate.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(nameUpdate, this.afterCreate(nameUpdate, Transaction.FOR_NETWORK));
    }

    public Pair<Transaction, Integer> createNameSale(PrivateKeyAccount creator, NameSale nameSale, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE NAME SALE
        SellNameTransaction nameSaleTransaction = new SellNameTransaction(creator, nameSale, (byte) feePow, time, 0l);
        nameSaleTransaction.sign(creator, Transaction.FOR_NETWORK);
        nameSaleTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(nameSaleTransaction, this.afterCreate(nameSaleTransaction, Transaction.FOR_NETWORK));
    }

    public Pair<Transaction, Integer> createCancelNameSale(PrivateKeyAccount creator, NameSale nameSale, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE CANCEL NAME SALE
        CancelSellNameTransaction cancelNameSaleTransaction = new CancelSellNameTransaction(creator, nameSale.getKey(), (byte) feePow, time, 0l);
        cancelNameSaleTransaction.sign(creator, Transaction.FOR_NETWORK);
        cancelNameSaleTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(cancelNameSaleTransaction, this.afterCreate(cancelNameSaleTransaction, Transaction.FOR_NETWORK));
    }

    public Pair<Transaction, Integer> createNamePurchase(PrivateKeyAccount creator, NameSale nameSale, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE NAME PURCHASE
        BuyNameTransaction namePurchase = new BuyNameTransaction(creator, nameSale, nameSale.getName().getOwner(), (byte) feePow, time, 0l);
        namePurchase.sign(creator, Transaction.FOR_NETWORK);
        namePurchase.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(namePurchase, this.afterCreate(namePurchase, Transaction.FOR_NETWORK));
    }

    public Transaction createIssuePollRecord(PrivateKeyAccount creator, PollCls poll, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE POLL CREATION
        IssuePollRecord pollCreation = new IssuePollRecord(creator, poll, (byte) feePow, time, 0l);
        pollCreation.sign(creator, Transaction.FOR_NETWORK);
        pollCreation.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return pollCreation;

    }

    public Transaction createPollCreation(PrivateKeyAccount creator, Poll poll, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE POLL CREATION
        CreatePollTransaction pollCreation = new CreatePollTransaction(creator, poll, (byte) feePow, time, 0l);
        pollCreation.sign(creator, Transaction.FOR_NETWORK);
        pollCreation.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return pollCreation;

    }

    public Transaction createItemPollVote(PrivateKeyAccount creator, long pollKey, int optionIndex, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE POLL VOTE
        VoteOnItemPollTransaction pollVote = new VoteOnItemPollTransaction(creator, pollKey, optionIndex, (byte) feePow, time, 0l);
        pollVote.sign(creator, Transaction.FOR_NETWORK);
        pollVote.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return pollVote;
    }

    public Pair<Transaction, Integer> createPollVote(PrivateKeyAccount creator, String poll, int optionIndex, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();


        //CREATE POLL VOTE
        VoteOnPollTransaction pollVote = new VoteOnPollTransaction(creator, poll, optionIndex, (byte) feePow, time, 0l);
        pollVote.sign(creator, Transaction.FOR_NETWORK);
        pollVote.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(pollVote, this.afterCreate(pollVote, Transaction.FOR_NETWORK));
    }


    public Pair<Transaction, Integer> createArbitraryTransaction(PrivateKeyAccount creator, List<Payment> payments, int service, byte[] data, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        Transaction arbitraryTransaction;
        arbitraryTransaction = new ArbitraryTransactionV3(creator, payments, service, data, (byte) feePow, time, 0l);
        arbitraryTransaction.sign(creator, Transaction.FOR_NETWORK);
        arbitraryTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(arbitraryTransaction, this.afterCreate(arbitraryTransaction, Transaction.FOR_NETWORK));
    }


    public Transaction createIssueAssetTransaction(PrivateKeyAccount creator, String name, String description,
                                                   byte[] icon, byte[] image,
                                                   int scale, int asset_type, long quantity, int feePow) {
        //CHECK FOR UPDATES
        // all unconfirmed org.erachain.records insert in FORK for calc last account REFERENCE
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        AssetCls asset = new AssetVenture(creator, name, icon, image, description, asset_type, scale, quantity);
        asset.setKey(this.fork.getItemAssetMap().getLastKey() + 1l);

        //CREATE ISSUE ASSET TRANSACTION
        IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(creator, asset, (byte) feePow, time, 0l);
        issueAssetTransaction.sign(creator, Transaction.FOR_NETWORK);
        issueAssetTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return issueAssetTransaction;
    }

    public Pair<Transaction, Integer> createIssueImprintTransaction(PrivateKeyAccount creator, String name, String description,
                                                                    byte[] icon, byte[] image,
                                                                    int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        ImprintCls imprint = new Imprint(creator, name, icon, image, description);

        //CREATE ISSUE IMPRINT TRANSACTION
        IssueImprintRecord issueImprintRecord = new IssueImprintRecord(creator, imprint, (byte) feePow, time);
        issueImprintRecord.sign(creator, Transaction.FOR_NETWORK);
        issueImprintRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(issueImprintRecord, this.afterCreate(issueImprintRecord, Transaction.FOR_NETWORK));
    }

    public Transaction createIssueImprintTransaction1(PrivateKeyAccount creator, String name, String description,
                                                      byte[] icon, byte[] image,
                                                      int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        ImprintCls imprint = new Imprint(creator, name, icon, image, description);

        //CREATE ISSUE IMPRINT TRANSACTION
        IssueImprintRecord issueImprintRecord = new IssueImprintRecord(creator, imprint, (byte) feePow, time);
        issueImprintRecord.sign(creator, Transaction.FOR_NETWORK);
        issueImprintRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return issueImprintRecord;
    }
	
/*
	public Transaction createIssueImprintTransaction1(PrivateKeyAccount creator, String name, String description,
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
		return issueImprintRecord;
	}
*/

    public Transaction createIssueTemplateTransaction(PrivateKeyAccount creator, String name, String description,
                                                      byte[] icon, byte[] image,
                                                      int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        TemplateCls template = new Template(creator, name, icon, image, description);

        //CREATE ISSUE PLATE TRANSACTION
        IssueTemplateRecord issueTemplateRecord = new IssueTemplateRecord(creator, template, (byte) feePow, time, 0l);
        issueTemplateRecord.sign(creator, Transaction.FOR_NETWORK);
        issueTemplateRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return issueTemplateRecord;
    }

    public Pair<Transaction, Integer> createIssuePersonTransaction(
            boolean forIssue,
            PrivateKeyAccount creator, String fullName, int feePow, long birthday, long deathday,
            byte gender, String race, float birthLatitude, float birthLongitude,
            String skinColor, String eyeColor, String hairСolor, int height,
            byte[] icon, byte[] image, String description,
            PublicKeyAccount owner, byte[] ownerSignature) {

        this.checkUpdate();

        //CHECK FOR UPDATES
        if (forIssue) {

            // IF has not DUPLICATE in UNCONFIRMED RECORDS
            TransactionMap unconfirmedMap = DCSet.getInstance().getTransactionTab();
            try {
                for (Transaction record : unconfirmedMap.values()) {
                    if (record.getType() == Transaction.ISSUE_PERSON_TRANSACTION) {
                        if (record instanceof IssuePersonRecord) {
                            IssuePersonRecord issuePerson = (IssuePersonRecord) record;
                            if (issuePerson.getItem().getName().equals(fullName)) {
                                return new Pair<Transaction, Integer>(null, Transaction.ITEM_DUPLICATE);
                            }
                        }
                    }
                }
            } catch (java.lang.Throwable e) {
                if (e instanceof java.lang.IllegalAccessError) {
                    // налетели на закрытую таблицу
                } else {
                    logger.error(e.getMessage(), e);
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
        IssuePersonRecord issuePersonRecord = new IssuePersonRecord(creator, person, (byte) feePow, time, lastReference);
        issuePersonRecord.sign(creator, Transaction.FOR_NETWORK);
        issuePersonRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        if (forIssue) {
            boolean asPack = false;
            return new Pair<Transaction, Integer>(issuePersonRecord, 1);//this.afterCreate(issuePersonRecord, asPack));
        } else {
            // for COPY -
            int valid = issuePersonRecord.isValid(Transaction.FOR_NETWORK,
                    Transaction.NOT_VALIDATE_FLAG_FEE | Transaction.NOT_VALIDATE_FLAG_PUBLIC_TEXT);
            if (valid == Transaction.NOT_ENOUGH_FEE
                    || valid == Transaction.CREATOR_NOT_PERSONALIZED) {
                valid = Transaction.VALIDATE_OK;
            }

            return new Pair<Transaction, Integer>(issuePersonRecord, valid);
        }
    }

    public Pair<Transaction, Integer> createIssuePersonHumanTransaction(
            PrivateKeyAccount creator, int feePow, PersonHuman human) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        // IF has not DUPLICATE in UNCONFIRMED RECORDS
        TransactionMap unconfirmedMap = DCSet.getInstance().getTransactionTab();
        try {
            for (Transaction record : unconfirmedMap.values()) {
                if (record.getType() == Transaction.ISSUE_PERSON_TRANSACTION) {
                    if (record instanceof IssuePersonRecord) {
                        IssuePersonRecord issuePerson = (IssuePersonRecord) record;
                        if (issuePerson.getItem().getName().equals(human.getName())) {
                            return new Pair<Transaction, Integer>(null, Transaction.ITEM_DUPLICATE);
                        }
                    }
                }
            }
        } catch (java.lang.Throwable e) {
            if (e instanceof java.lang.IllegalAccessError) {
                // налетели на закрытую таблицу
            } else {
                logger.error(e.getMessage(), e);
            }
        }

        //TIME
        long time = NTP.getTime();

        long lastReference;
        lastReference = 0l;

        //CREATE ISSUE PLATE TRANSACTION
        IssuePersonRecord issuePersonRecord = new IssuePersonRecord(creator, human, (byte) feePow, time, lastReference);
        issuePersonRecord.sign(creator, Transaction.FOR_NETWORK);
        issuePersonRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        boolean asPack = false;
        return new Pair<Transaction, Integer>(issuePersonRecord, 1);

    }

    public Transaction createIssuePollTransaction(PrivateKeyAccount creator, String name, String description,
                                                  byte[] icon, byte[] image,
                                                  List<String> options, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        PollCls status = new org.erachain.core.item.polls.Poll(creator, name, icon, image, description, options);

        //CREATE ISSUE PLATE TRANSACTION
        IssuePollRecord issueStatusRecord = new IssuePollRecord(creator, status, (byte) feePow, time, 0l);
        issueStatusRecord.sign(creator, Transaction.FOR_NETWORK);
        issueStatusRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return issueStatusRecord;
    }

    public Transaction createIssueStatusTransaction(PrivateKeyAccount creator, String name, String description,
                                                    byte[] icon, byte[] image,
                                                    boolean unique, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        StatusCls status = new Status(creator, name, icon, image, description, unique);

        //CREATE ISSUE PLATE TRANSACTION
        IssueStatusRecord issueStatusRecord = new IssueStatusRecord(creator, status, (byte) feePow, time, 0l);
        issueStatusRecord.sign(creator, Transaction.FOR_NETWORK);
        issueStatusRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return issueStatusRecord;
    }

    public Transaction createIssueUnionTransaction(PrivateKeyAccount creator, String name, long birthday, long parent, String description,
                                                   byte[] icon, byte[] image,
                                                   int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        UnionCls union = new Union(creator, name, birthday, parent, icon, image, description);

        //CREATE ISSUE PLATE TRANSACTION
        IssueUnionRecord issueUnionRecord = new IssueUnionRecord(creator, union, (byte) feePow, time, 0l);
        issueUnionRecord.sign(creator, Transaction.FOR_NETWORK);
        issueUnionRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return issueUnionRecord;

    }

    public Transaction createOrderTransaction(PrivateKeyAccount creator, AssetCls have, AssetCls want, BigDecimal amountHave, BigDecimal amounWant, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE ORDER TRANSACTION
        CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(creator, have.getKey(), want.getKey(), amountHave, amounWant, (byte) feePow, time, 0l);

		/*
		int res = createOrderTransaction.isValid(this.fork, null);
		if (res != Transaction.VALIDATE_OK)
			return null;
		 */

        //VALIDATE AND PROCESS
        createOrderTransaction.sign(creator, Transaction.FOR_NETWORK);
        createOrderTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return createOrderTransaction;
    }

    public Transaction createCancelOrderTransaction2(PrivateKeyAccount creator, Long orderID, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        Transaction createOrder = this.fork.getTransactionFinalMap().get(orderID);
        if (createOrder == null)
            return null;

        byte[] orderSignature = createOrder.getSignature();

        //CREATE PRDER TRANSACTION
        CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(creator, orderSignature, (byte) feePow, time, 0l);
        cancelOrderTransaction.sign(creator, Transaction.FOR_NETWORK);
        cancelOrderTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return cancelOrderTransaction;

    }

    public Transaction createCancelOrderTransaction2(PrivateKeyAccount creator, byte[] orderSignature, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE PRDER TRANSACTION
        CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(creator, orderSignature, (byte) feePow, time, 0l);
        cancelOrderTransaction.sign(creator, Transaction.FOR_NETWORK);
        cancelOrderTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return cancelOrderTransaction;

    }

    public Pair<Transaction, Integer> createCancelOrderTransaction(PrivateKeyAccount creator, Order order, int feePow) {
        return createCancelOrderTransaction(creator, order, feePow);
    }

    public Pair<Transaction, Integer> createCancelOrderTransaction(PrivateKeyAccount creator, byte[] orderID, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE PRDER TRANSACTION
        CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(creator, orderID, (byte) feePow, time, 0l);
        cancelOrderTransaction.sign(creator, Transaction.FOR_NETWORK);
        cancelOrderTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(cancelOrderTransaction, this.afterCreate(cancelOrderTransaction, Transaction.FOR_NETWORK));
    }

    public Pair<Transaction, Integer> sendMultiPayment(PrivateKeyAccount creator, List<Payment> payments, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE MULTI PAYMENTS
        MultiPaymentTransaction multiPayment = new MultiPaymentTransaction(creator, payments, (byte) feePow, time, 0l);
        multiPayment.sign(creator, Transaction.FOR_NETWORK);
        multiPayment.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(multiPayment, this.afterCreate(multiPayment, Transaction.FOR_NETWORK));
    }

    public Pair<Transaction, Integer> deployATTransaction(PrivateKeyAccount creator, String name, String description, String type, String tags, byte[] creationBytes, BigDecimal amount, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //DEPLOY AT
        DeployATTransaction deployAT = new DeployATTransaction(creator, name, description, type, tags, creationBytes, amount, (byte) feePow, time, 0l);
        deployAT.sign(creator, Transaction.FOR_NETWORK);
        deployAT.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return new Pair<Transaction, Integer>(deployAT, this.afterCreate(deployAT, Transaction.FOR_NETWORK));

    }

    //public Pair<Transaction, Integer> r_Send(PrivateKeyAccount creator,

    public Transaction r_Send(PrivateKeyAccount creator,
                              Account recipient, long key, BigDecimal amount, int feePow, String title, byte[] message, byte[] isText,
                              byte[] encryptMessage) {

        this.checkUpdate();

        Transaction messageTx;

        long timestamp = NTP.getTime();

        //CREATE MESSAGE TRANSACTION
        //messageTx = new RSend(creator, (byte)feePow, recipient, key, amount, head, message, isText, encryptMessage, timestamp, 0l);
        messageTx = new RSend(creator, (byte) feePow, recipient, key, amount, title, message, isText, encryptMessage, timestamp, 0l);
        messageTx.sign(creator, Transaction.FOR_NETWORK);
        messageTx.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return messageTx;// new Pair<Transaction, Integer>(messageTx, afterCreate(messageTx, false));
    }

    public Transaction r_Send(byte version, byte property1, byte property2,
                              PrivateKeyAccount creator,
                              Account recipient, long key, BigDecimal amount, int feePow, String title,
                              byte[] message, byte[] isText, byte[] encryptMessage) {

        this.checkUpdate();

        Transaction messageTx;

        long timestamp = NTP.getTime();

        //CREATE MESSAGE TRANSACTION
        messageTx = new RSend(version, property1, property2, creator, (byte) feePow, recipient, key, amount, title,
                message, isText, encryptMessage, timestamp, 0l);
        messageTx.sign(creator, Transaction.FOR_NETWORK);
        messageTx.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return messageTx;
    }

    public Transaction r_SignNote(byte version, byte property1, byte property2,
                                  int asDeal, PrivateKeyAccount creator,
                                  int feePow, long key, byte[] message, byte[] isText, byte[] encrypted) {

        this.checkUpdate();

        Transaction recordNoteTx;

        long timestamp = NTP.getTime();

        //CREATE MESSAGE TRANSACTION
        recordNoteTx = new RSignNote(version, property1, property1,
                creator, (byte) feePow, key, message, isText, encrypted, timestamp, 0l);
        recordNoteTx.sign(creator, asDeal);
        recordNoteTx.setDC(this.fork, asDeal, this.blockHeight, ++this.seqNo);

        return recordNoteTx;

    }

    public Transaction r_SertifyPerson(int version, int asDeal,
                                       PrivateKeyAccount creator, int feePow, long key,
                                       List<PublicKeyAccount> userAccounts,
                                       int add_day) {

        this.checkUpdate();

        Transaction record;

        long timestamp = NTP.getTime();

        //CREATE SERTIFY PERSON TRANSACTION
        //int version = 5; // without user sign
        record = new RSertifyPubKeys(version, creator, (byte) feePow, key,
                userAccounts,
                add_day, timestamp, 0l);
        record.sign(creator, asDeal);
        record.setDC(this.fork, asDeal, this.blockHeight, ++this.seqNo);

        return record;
    }

    public Transaction r_Vouch(int version, int asDeal,
                               PrivateKeyAccount creator, int feePow,
                               int height, int seq) {

        this.checkUpdate();

        Transaction record;

        long timestamp = NTP.getTime();

        //CREATE SERTIFY PERSON TRANSACTION
        //int version = 5; // without user sign
        record = new RVouch(creator, (byte) feePow,
                height, seq,
                timestamp, 0l);
        record.sign(creator, asDeal);
        record.setDC(this.fork, asDeal, this.blockHeight, ++this.seqNo);

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
        for (int i = 0; i < hashes58.length; i++) {
            hashes[i] = Bytes.ensureCapacity(Base58.decode(hashes58[i]), 32, 0);
        }

        //CREATE MESSAGE TRANSACTION
        messageTx = new RHashes(creator, (byte) feePow, url, data, hashes, timestamp, 0l);
        messageTx.sign(creator, Transaction.FOR_NETWORK);
        messageTx.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return new Pair<Transaction, Integer>(messageTx, afterCreate(messageTx, Transaction.FOR_NETWORK));
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
		record = new RSetStatusToItem(creator, (byte)feePow, key, item.getItemType(), item.getKey(),
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
        record = new RSetStatusToItem(creator, (byte) feePow, key, item.getItemType(), item.getKey(),
                beg_date, end_date, value_1, value_2, data_1, data_2, refParent, descr,
                timestamp, 0l);
        record.sign(creator, Transaction.FOR_NETWORK);
        record.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, ++this.seqNo);

        return record;
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

    public Pair<Transaction, Integer> createTransactionFromRaw(byte[] rawData) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //CREATE TRANSACTION FROM RAW
        Transaction transaction;
        try {
            transaction = TransactionFactory.getInstance().parse(rawData, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            return new Pair<Transaction, Integer>(null, Transaction.INVALID_RAW_DATA);
        }

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(transaction, this.afterCreate(transaction, Transaction.FOR_NETWORK));
    }

    public Integer afterCreate(Transaction transaction, int asDeal) {
        //CHECK IF PAYMENT VALID

        if (this.fork.getTransactionTab().contains(transaction.getSignature())) {
            // если случилась коллизия по подписи усеченной
            // в базе неподтвержденных транзакций -то выдадим ошибку
            return Transaction.KEY_COLLISION;
        }

        transaction.setDC(this.fork, asDeal, this.blockHeight, ++this.seqNo);
        int valid = transaction.isValid(asDeal, 0l);

        if (valid == Transaction.VALIDATE_OK) {

            if (asDeal > Transaction.FOR_PACK) {
                //PROCESS IN FORK
                transaction.process(null, asDeal);

                // if it ISSUE - reset key
                if (transaction instanceof IssueItemRecord) {
                    IssueItemRecord issueItem = (IssueItemRecord) transaction;
                    issueItem.getItem().resetKey();
                }

                //CONTROLLER ONTRANSACTION
                Controller.getInstance().onTransactionCreate(transaction);
            }
        }

        //RETURN
        return valid;
    }

    public Integer afterCreateRaw(Transaction transaction, int asDeal, long flags) {
        this.checkUpdate();
        return this.afterCreate(transaction, asDeal);
    }

}
