package org.erachain.core;

import com.google.common.primitives.Bytes;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
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
import java.util.concurrent.atomic.AtomicInteger;


/**
 * класс для создания транзакций в текущем кошельке
 * Использует форкнутую базу цепочки
 *
 * TODO: - поидее его надо перенести в модуль Кошелек - ибо без кошелька он не будет пахать.
 */
@Slf4j
public class TransactionCreator {
    private DCSet fork;
    private Block lastBlock;
    private int blockHeight;
    private AtomicInteger seqNo = new AtomicInteger();

    static private final byte[] itemAppData = null;
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
        if (this.fork != null) {
            // закроем сам файл базы - закрывать DCSet.fork - не нужно - он сам очистится
            this.fork.close();
        }

        // создаем в памяти базу - так как она на 1 блок только нужна - а значит много памяти не возьмет
        DB database = DCSet.makeDBinMemory();
        this.fork = DCSet.getInstance().fork(database, "updateFork");

        //UPDATE LAST BLOCK
        this.lastBlock = Controller.getInstance().getLastBlock();
        this.blockHeight = this.fork.getBlockMap().size() + 1;
        this.seqNo.set(0); // reset sequence number

        //SCAN UNCONFIRMED TRANSACTIONS FOR TRANSACTIONS WHERE ACCOUNT IS CREATOR OF
        ///List<Transaction> transactions = (List<Transaction>)this.fork.getTransactionMap().getValuesAll();
        TransactionMap transactionTab = this.fork.getTransactionTab();
        List<Transaction> accountTransactions = new ArrayList<Transaction>();
        Transaction transaction;

        // здесь нужен протокольный итератор!
        try (IteratorCloseable<Long> iterator = transactionTab.getIterator()) {
            List<Account> accountMap = Controller.getInstance().getWalletAccounts();

            while (iterator.hasNext()) {
                transaction = transactionTab.get(iterator.next());

                if (accountMap.contains(transaction.getCreator())) {
                    accountTransactions.add(transaction);
                }
            }
        } catch (IOException e) {
        }

        //SORT THEM BY TIMESTAMP
        Collections.sort(accountTransactions, new TransactionTimestampComparator());

        //VALIDATE AND PROCESS THOSE TRANSACTIONS IN FORK for recalc last reference
        for (Transaction transactionAccount : accountTransactions) {

            try {
                transactionAccount.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());
                if (!transactionAccount.isSignatureValid(this.fork)) {
                    //THE TRANSACTION BECAME INVALID LET
                    this.fork.getTransactionTab().delete(transactionAccount);
                } else {
                    if (transactionAccount.isValid(Transaction.FOR_NETWORK, 0L) == Transaction.VALIDATE_OK) {
                        transactionAccount.process(null, Transaction.FOR_NETWORK);
                    } else {
                        //THE TRANSACTION BECAME INVALID LET
                        this.fork.getTransactionTab().delete(transactionAccount);
                    }
                    // CLEAR for HEAP
                    transactionAccount.resetDCSet();
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

    public Transaction createPollCreation(PrivateKeyAccount creator, Poll poll, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE POLL CREATION
        CreatePollTransaction pollCreation = new CreatePollTransaction(creator, poll, (byte) feePow, time, 0l);
        pollCreation.sign(creator, Transaction.FOR_NETWORK);
        pollCreation.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

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
        pollVote.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

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
        pollVote.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(pollVote, this.afterCreate(pollVote, Transaction.FOR_NETWORK, false, false));
    }


    public Pair<Transaction, Integer> createArbitraryTransaction(PrivateKeyAccount creator, List<Payment> payments, int service, byte[] data, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        Transaction arbitraryTransaction;
        arbitraryTransaction = new ArbitraryTransactionV3(creator, payments, service, data, (byte) feePow, time, 0l);
        arbitraryTransaction.sign(creator, Transaction.FOR_NETWORK);
        arbitraryTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(arbitraryTransaction, this.afterCreate(arbitraryTransaction, Transaction.FOR_NETWORK, false, false));
    }


    public Transaction createIssueAssetTransaction(PrivateKeyAccount creator, ExLink linkTo, AssetCls asset, int feePow) {
        //CHECK FOR UPDATES
        // all unconfirmed org.erachain.records insert in FORK for calc last account REFERENCE
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        asset.setKey(this.fork.getItemAssetMap().getLastKey() + 1l);

        //CREATE ISSUE ASSET TRANSACTION
        IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(creator, linkTo, asset, (byte) feePow, time, 0L);
        issueAssetTransaction.sign(creator, Transaction.FOR_NETWORK);
        issueAssetTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        return issueAssetTransaction;
    }

    public Transaction createIssueAssetTransaction(PrivateKeyAccount creator, ExLink linkTo, String name, String description,
                                                   byte[] icon, byte[] image,
                                                   int scale, int asset_type, long quantity, int feePow) {
        AssetCls asset = new AssetVenture(itemAppData, creator, name, icon, image, description, asset_type, scale, quantity);
        return createIssueAssetTransaction(creator, linkTo, asset, feePow);
    }

    public Pair<Transaction, Integer> createIssueImprintTransaction(PrivateKeyAccount creator, ExLink linkTo, String name, String description,
                                                                    byte[] icon, byte[] image,
                                                                    int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        ImprintCls imprint = new Imprint(itemAppData, creator, name, icon, image, description);

        //CREATE ISSUE IMPRINT TRANSACTION
        IssueImprintRecord issueImprintRecord = new IssueImprintRecord(creator, linkTo, imprint, (byte) feePow, time);
        issueImprintRecord.sign(creator, Transaction.FOR_NETWORK);
        issueImprintRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(issueImprintRecord, this.afterCreate(issueImprintRecord, Transaction.FOR_NETWORK, false, false));
    }

    public Transaction createIssueImprintTransaction1(PrivateKeyAccount creator, ExLink linkTo, String name, String description,
                                                      byte[] icon, byte[] image,
                                                      int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        ImprintCls imprint = new Imprint(itemAppData, creator, name, icon, image, description);

        //CREATE ISSUE IMPRINT TRANSACTION
        IssueImprintRecord issueImprintRecord = new IssueImprintRecord(creator, linkTo, imprint, (byte) feePow, time);
        issueImprintRecord.sign(creator, Transaction.FOR_NETWORK);
        issueImprintRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

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

    public Pair<Transaction, Integer> createIssuePersonTransaction(
            boolean forIssue,
            PrivateKeyAccount creator, ExLink linkTo, String fullName, int feePow, long birthday, long deathday,
            byte gender, String race, float birthLatitude, float birthLongitude,
            String skinColor, String eyeColor, String hairСolor, int height,
            byte[] icon, byte[] image, String description,
            PublicKeyAccount maker, byte[] makerSignature) {

        this.checkUpdate();

        //CHECK FOR UPDATES
        if (false // теперь ниже проверяем в форке тут на ДУБЛЬ
                && forIssue) {
            // IF has not DUPLICATE in UNCONFIRMED RECORDS
            TransactionMap unconfirmedMap = this.fork.getTransactionTab();
            try {
                for (Transaction record : unconfirmedMap.values()) {
                    if (record.getType() == Transaction.ISSUE_PERSON_TRANSACTION) {
                        if (record instanceof IssuePersonRecord) {
                            IssuePersonRecord issuePerson = (IssuePersonRecord) record;
                            if (issuePerson.getItem().getName().equals(fullName)) {
                                record.setErrorValue("equal to " + fullName);
                                return new Pair<Transaction, Integer>(record, Transaction.ITEM_DUPLICATE);
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

        PersonHuman person = new PersonHuman(itemAppData, maker, fullName, birthday, deathday,
                gender, race, birthLatitude, birthLongitude,
                skinColor, eyeColor, hairСolor, height, icon, image, description, makerSignature);

        long lastReference;
        if (forIssue) {
            lastReference = 0L;
        } else {
            lastReference = time - 1000L;
        }

        //CREATE ISSUE PLATE TRANSACTION
        IssuePersonRecord issuePersonRecord = new IssuePersonRecord(creator, linkTo, person, (byte) feePow, time, lastReference);
        issuePersonRecord.sign(creator, Transaction.FOR_NETWORK);
        issuePersonRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        //VALIDATE AND PROCESS
        if (forIssue) {
            if (person.getMakerSignature() != null && this.fork.getTransactionFinalMapSigns().contains(person.getMakerSignature())) {
                issuePersonRecord.setErrorValue("equal to OwnerSignature " + Base58.encode(person.getMakerSignature()));
                return new Pair<Transaction, Integer>(issuePersonRecord, Transaction.ITEM_DUPLICATE);
            }

            return new Pair<Transaction, Integer>(issuePersonRecord, 1);
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

    public Pair<Transaction, Integer> createIssuePersonTransaction(
            PrivateKeyAccount creator, ExLink linkTo, int feePow, PersonCls person) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        long lastReference;
        lastReference = 0L;

        //CREATE ISSUE PLATE TRANSACTION
        IssuePersonRecord issuePersonRecord = new IssuePersonRecord(creator, linkTo, person, (byte) feePow, time, lastReference);
        issuePersonRecord.sign(creator, Transaction.FOR_NETWORK);
        issuePersonRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        byte[] makerSign = ((PersonHuman) issuePersonRecord.getItem()).getMakerSignature();
        if (makerSign != null && this.fork.getTransactionFinalMapSigns().contains(makerSign)) {
            issuePersonRecord.setErrorValue("equal to OwnerSignature " + Base58.encode(makerSign));
            return new Pair<Transaction, Integer>(issuePersonRecord, Transaction.ITEM_DUPLICATE);
        }

        //VALIDATE AND PROCESS
        boolean asPack = false;
        return new Pair<Transaction, Integer>(issuePersonRecord, Transaction.VALIDATE_OK);

    }

    public Transaction createIssuePollTransaction(PrivateKeyAccount creator, ExLink linkTo, int feePow, PollCls poll) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE ISSUE PLATE TRANSACTION
        IssuePollRecord issueStatusRecord = new IssuePollRecord(creator, linkTo, poll, (byte) feePow, time, 0L);
        issueStatusRecord.sign(creator, Transaction.FOR_NETWORK);
        issueStatusRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        return issueStatusRecord;
    }

    public Transaction createIssuePollTransaction(PrivateKeyAccount creator, ExLink linkTo, String name, String description,
                                                  byte[] icon, byte[] image,
                                                  List<String> options, int feePow) {
        PollCls poll = new org.erachain.core.item.polls.Poll(itemAppData, creator, name, icon, image, description, options);
        return createIssuePollTransaction(creator, linkTo, feePow, poll);
    }

    public Transaction createIssueStatusTransaction(PrivateKeyAccount creator, ExLink linkTo, int feePow, StatusCls status) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE ISSUE PLATE TRANSACTION
        IssueStatusRecord issueStatusRecord = new IssueStatusRecord(creator, linkTo, status, (byte) feePow, time, 0L);
        issueStatusRecord.sign(creator, Transaction.FOR_NETWORK);
        issueStatusRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        return issueStatusRecord;
    }

    public Transaction createIssueStatusTransaction(PrivateKeyAccount creator, ExLink linkTo, String name, String description,
                                                    byte[] icon, byte[] image,
                                                    boolean unique, int feePow) {
        StatusCls status = new Status(itemAppData, creator, name, icon, image, description, unique);
        return createIssueStatusTransaction(creator, linkTo, feePow, status);
    }

    public Transaction createIssueTemplateTransaction(PrivateKeyAccount creator, ExLink linkTo, int feePow, TemplateCls template) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE ISSUE PLATE TRANSACTION
        IssueTemplateRecord issueTemplateRecord = new IssueTemplateRecord(creator, linkTo, template, (byte) feePow, time, 0L);
        issueTemplateRecord.sign(creator, Transaction.FOR_NETWORK);
        issueTemplateRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        //VALIDATE AND PROCESS
        return issueTemplateRecord;
    }

    public Transaction createIssueTemplateTransaction(PrivateKeyAccount creator, ExLink linkTo, String name, String description,
                                                      byte[] icon, byte[] image,
                                                      int feePow) {
        TemplateCls template = new Template(itemAppData, creator, name, icon, image, description);
        return createIssueTemplateTransaction(creator, linkTo, feePow, template);
    }

    public Transaction createIssueUnionTransaction(PrivateKeyAccount creator, ExLink linkTo, String name, long birthday, long parent, String description,
                                                   byte[] icon, byte[] image,
                                                   int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        UnionCls union = new Union(itemAppData, creator, name, birthday, parent, icon, image, description);

        //CREATE ISSUE PLATE TRANSACTION
        IssueUnionRecord issueUnionRecord = new IssueUnionRecord(creator, linkTo, union, (byte) feePow, time, 0l);
        issueUnionRecord.sign(creator, Transaction.FOR_NETWORK);
        issueUnionRecord.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

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
        createOrderTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

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
        cancelOrderTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

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
        cancelOrderTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

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
        cancelOrderTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(cancelOrderTransaction, this.afterCreate(cancelOrderTransaction, Transaction.FOR_NETWORK, false, false));
    }

    public Transaction createCancelOrderTransaction1(PrivateKeyAccount creator, byte[] orderID, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE PRDER TRANSACTION
        CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(creator, orderID, (byte) feePow, time, 0l);
        cancelOrderTransaction.sign(creator, Transaction.FOR_NETWORK);
        cancelOrderTransaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        //VALIDATE AND PROCESS
        return cancelOrderTransaction;
    }

    public Pair<Transaction, Integer> sendMultiPayment(PrivateKeyAccount creator, List<Payment> payments, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //CREATE MULTI PAYMENTS
        MultiPaymentTransaction multiPayment = new MultiPaymentTransaction(creator, payments, (byte) feePow, time, 0l);
        multiPayment.sign(creator, Transaction.FOR_NETWORK);
        multiPayment.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        //VALIDATE AND PROCESS
        return new Pair<Transaction, Integer>(multiPayment, this.afterCreate(multiPayment, Transaction.FOR_NETWORK, false, false));
    }

    public Pair<Transaction, Integer> deployATTransaction(PrivateKeyAccount creator, String name, String description, String type, String tags, byte[] creationBytes, BigDecimal amount, int feePow) {
        //CHECK FOR UPDATES
        this.checkUpdate();

        //TIME
        long time = NTP.getTime();

        //DEPLOY AT
        DeployATTransaction deployAT = new DeployATTransaction(creator, name, description, type, tags, creationBytes, amount, (byte) feePow, time, 0l);
        deployAT.sign(creator, Transaction.FOR_NETWORK);
        deployAT.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        return new Pair<Transaction, Integer>(deployAT, this.afterCreate(deployAT, Transaction.FOR_NETWORK, false, false));

    }

    //public Pair<Transaction, Integer> r_Send(PrivateKeyAccount creator,

    public Transaction r_Send(PrivateKeyAccount creator,
                              ExLink linkTo, Account recipient, long key, BigDecimal amount, int feePow, String title, byte[] message, byte[] isText,
                              byte[] encryptMessage, long timestamp_in) {

        this.checkUpdate();

        Transaction messageTx;

        long timestamp = timestamp_in > 0 ? timestamp_in : NTP.getTime();

        //CREATE MESSAGE TRANSACTION
        //messageTx = new RSend(creator, (byte)feePow, recipient, key, amount, head, message, isText, encryptMessage, timestamp, 0l);
        messageTx = new RSend(creator, linkTo, (byte) feePow, recipient, key, amount, title, message, isText, encryptMessage, timestamp, 0L);
        messageTx.sign(creator, Transaction.FOR_NETWORK);
        messageTx.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        return messageTx;// new Pair<Transaction, Integer>(messageTx, afterCreate(messageTx, false));
    }

    public Transaction r_Send(byte version, byte property1, byte property2,
                              PrivateKeyAccount creator,
                              Account recipient, long key, BigDecimal amount, ExLink linkTo, int feePow, String title,
                              byte[] message, byte[] isText, byte[] encryptMessage) {

        this.checkUpdate();

        Transaction messageTx;

        long timestamp = NTP.getTime();

        //CREATE MESSAGE TRANSACTION
        messageTx = new RSend(version, property1, property2, creator, linkTo, (byte) feePow, recipient, key, amount, title,
                message, isText, encryptMessage, timestamp, 0L);
        messageTx.sign(creator, Transaction.FOR_NETWORK);
        messageTx.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        return messageTx;
    }

    public Transaction r_SignNote(byte version, byte property1, byte property2,
                                  PrivateKeyAccount creator,
                                  int feePow, long key, byte[] message) {

        this.checkUpdate();

        Transaction recordNoteTx;

        long timestamp = NTP.getTime();

        //CREATE MESSAGE TRANSACTION
        recordNoteTx = new RSignNote(version, property1, property1,
                creator, (byte) feePow, key, message, timestamp, 0L);
        recordNoteTx.sign(creator, Transaction.FOR_NETWORK);
        recordNoteTx.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet(), false);

        return recordNoteTx;

    }

    public Transaction r_CertifyPubKeysPerson(int version, int forDeal,
                                              PrivateKeyAccount creator, int feePow, long key,
                                              List<PublicKeyAccount> userAccounts,
                                              int add_day) {

        this.checkUpdate();

        Transaction record;

        long timestamp = NTP.getTime();

        //CREATE SERTIFY PERSON TRANSACTION
        //int version = 5; // without user sign
        record = new RCertifyPubKeys(version, creator, (byte) feePow, key,
                userAccounts,
                add_day, timestamp, 0L);
        record.sign(creator, forDeal);
        record.setDC(this.fork, forDeal, this.blockHeight, this.seqNo.incrementAndGet(), false);

        return record;
    }

    public Transaction r_CertifyPubKeysPerson(int version, PrivateKeyAccount creator, ExLink linkTo, int feePow, long key,
                                              PublicKeyAccount pubKey, int add_day) {

        this.checkUpdate();

        Transaction record;

        long timestamp = NTP.getTime();

        List<PublicKeyAccount> pubKeys = new ArrayList<>();
        pubKeys.add(pubKey);

        //CREATE SERTIFY PERSON TRANSACTION
        record = new RCertifyPubKeys(version, creator, linkTo, (byte) feePow, key,
                pubKeys, add_day, timestamp, 0L);
        record.sign(creator, Transaction.FOR_NETWORK);
        record.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet(), false);

        return record;
    }

    public Transaction r_Vouch(int version, int forDeal,
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
        record.sign(creator, forDeal);
        record.setDC(this.fork, forDeal, this.blockHeight, this.seqNo.incrementAndGet(), false);

        return record;
    }

    public Transaction r_Hashes(PrivateKeyAccount creator, int feePow,
                                String urlStr, String dataStr, String[] hashes58) {

        this.checkUpdate();

        Transaction transaction;

        long timestamp = NTP.getTime();

        byte[] url = urlStr.getBytes(StandardCharsets.UTF_8);
        byte[] data = dataStr.getBytes(StandardCharsets.UTF_8);

        byte[][] hashes = new byte[hashes58.length][32];
        for (int i = 0; i < hashes58.length; i++) {
            hashes[i] = Bytes.ensureCapacity(Base58.decode(hashes58[i]), 32, 0);
        }

        //CREATE MESSAGE TRANSACTION
        transaction = new RHashes(creator, (byte) feePow, url, data, hashes, timestamp, 0l);
        transaction.sign(creator, Transaction.FOR_NETWORK);
        transaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        return transaction;
    }

    public Transaction r_Hashes(PrivateKeyAccount creator, int feePow,
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

        Transaction transaction;

        long timestamp = NTP.getTime();

        //CREATE SERTIFY PERSON TRANSACTION
        //int version = 5; // without user sign
        transaction = new RSetStatusToItem(creator, (byte) feePow, key, item.getItemType(), item.getKey(),
                beg_date, end_date, value_1, value_2, data_1, data_2, refParent, descr,
                timestamp, 0l);
        transaction.sign(creator, Transaction.FOR_NETWORK);
        transaction.setDC(this.fork, Transaction.FOR_NETWORK, this.blockHeight, this.seqNo.incrementAndGet());

        return transaction;
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
        return new Pair<Transaction, Integer>(transaction, this.afterCreate(transaction, Transaction.FOR_NETWORK, false, false));
    }

    public Integer afterCreate(Transaction transaction, int forDeal, boolean tryFree, boolean notRelease) {
        //CHECK IF PAYMENT VALID

        if (false && // теперь не проверяем так как ключ сделал длинный dbs.rocksDB.TransactionFinalSignsSuitRocksDB.KEY_LEN
                this.fork.getTransactionTab().contains(transaction.getSignature())) {
            // если случилась коллизия по подписи усеченной
            // в базе неподтвержденных транзакций -то выдадим ошибку
            return Transaction.KEY_COLLISION;
        }

        if (!this.fork.equals(transaction.getDCSet())) {
            transaction.setDC(this.fork, forDeal, this.blockHeight, this.seqNo.incrementAndGet());
        }

        int valid = transaction.isValid(forDeal, tryFree ? Transaction.NOT_VALIDATE_FLAG_FEE : 0L);

        if (valid == Transaction.VALIDATE_OK) {

            if (forDeal > Transaction.FOR_PACK) {
                //PROCESS IN FORK
                transaction.process(null, forDeal);

                // if it ISSUE - reset key
                // не надо теперь сбрасывать - Так как дальше скелет передается в onTransactionCreate
                if (false && transaction instanceof IssueItemRecord) {
                    IssueItemRecord issueItem = (IssueItemRecord) transaction;
                    issueItem.getItem().resetKey();
                }

                if (!notRelease)
                    Controller.getInstance().onTransactionCreate(transaction);
            }
        } else {
            this.seqNo.decrementAndGet();
        }

        //RETURN
        return valid;
    }

    public Integer afterCreateRaw(Transaction transaction, int forDeal, long flags) {
        this.checkUpdate();
        return this.afterCreate(transaction, forDeal, false, false);
    }

}
