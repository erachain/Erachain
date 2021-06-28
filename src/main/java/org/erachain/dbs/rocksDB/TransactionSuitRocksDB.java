package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableLong;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableTuple3StringLongInteger;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.erachain.dbs.rocksDB.transformation.toBytesStringLongInteger;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
public class TransactionSuitRocksDB extends DBMapSuit<Long, Transaction> implements TransactionSuit
{

    private final String NAME_TABLE = "TRANSACTIONS_UNCONFIRMED_TABLE";
    private final String timestampIndexName = "timestamp_unc_txs";
    private final String senderIndexName = "sender_unc_txs";
    private final String addressTypeIndexName = "address_type_unc_txs";

    private SimpleIndexDB<Long, Transaction, Long> timestampIndex;
    //private IndexByteableTuple3StringLongInteger indexByteableTuple3StringLongInteger;
    private SimpleIndexDB<Long, Transaction, Fun.Tuple2<String, Long>> senderIndex;
    private ArrayIndexDB<Long, Transaction, String> recipientsIndex;
    ListIndexDB<Long, Transaction, Fun.Tuple3<String, Long, Integer>> addressTypeIndex;

    public TransactionSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, true);
    }

    @Override
    public void openMap() {

        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(),
                databaseSet, sizeEnable);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {

        // USE counter index
        indexes = new ArrayList<>();

        timestampIndex = new SimpleIndexDB<>(timestampIndexName,
                new BiFunction<Long, Transaction, Long>() {
                    @Override
                    public Long apply(Long aLong, Transaction transaction) {
                        return transaction.getTimestamp();
                    }
                    //}, (result, key) ->new ByteableLong().toBytesObject(result)); // создает Класс на лету и переопределяет его метод
                }, new IndexByteableLong()); // а тут мы уже создали заранее Класс

        indexes.add(timestampIndex);


        if (Controller.getInstance().onlyProtocolIndexing) {
            return;
        }

        senderIndex = new SimpleIndexDB<>(senderIndexName,
                new BiFunction<Long, Transaction, Fun.Tuple2<String, Long>>() {
                    @Override
                    public Fun.Tuple2<String, Long> apply(Long aLong, Transaction transaction) {
                        Account account = transaction.getCreator();
                        return new Fun.Tuple2<>(account == null ? "genesis" : account.getAddress(), transaction.getTimestamp());
                    }
                }, (result) -> org.bouncycastle.util.Arrays.concatenate(
                new ByteableString().toBytesObject(result.a),
                new ByteableLong().toBytesObject(result.b)));

        String recipientsIndexName = "recipient_unc_txs";
        recipientsIndex = new ArrayIndexDB<>(recipientsIndexName,
                (aLong, transaction) -> {
                    if (transaction.noDCSet()) {
                        transaction.setDC((DCSet) databaseSet, true);
                    }
                    return transaction.getRecipientAccounts().stream().map(Account::getAddress).toArray(String[]::new);
                },
                (result) -> new ByteableString().toBytesObject(result));

        //indexByteableTuple3StringLongInteger = new IndexByteableTuple3StringLongInteger();
        addressTypeIndex
                = new ListIndexDB<>(addressTypeIndexName,
                (aLong, transaction) -> {
                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    if (transaction.noDCSet()) {
                        transaction.setDC((DCSet) databaseSet, true);
                    }
                    Integer type = transaction.getType();
                    return transaction.getInvolvedAccounts().stream().map(
                            (account) -> (new Fun.Tuple3<>(account.getAddress(), transaction.getTimestamp(), type))).collect(Collectors.toList());
                }, new IndexByteableTuple3StringLongInteger());

        indexes.add(addressTypeIndex);
        indexes.add(senderIndex);
        indexes.add(recipientsIndex);

    }

    @Override
    public IteratorCloseable<Long> typeIterator(String sender, Long timestamp, Integer type) {
        return map.getIndexIteratorFilter(addressTypeIndex.getColumnFamilyHandle(),
                toBytesStringLongInteger.toBytes(sender, timestamp, type), false, true);
    }

    @Override
    public IteratorCloseable<Long> senderIterator(String sender) {
        return map.getIndexIteratorFilter(senderIndex.getColumnFamilyHandle(), sender.getBytes(), false, true);
    }

    @Override
    public IteratorCloseable<Long> recipientIterator(String recipient) {
        return map.getIndexIteratorFilter(recipientsIndex.getColumnFamilyHandle(), recipient.getBytes(), false, true);
    }

    @Override
    public IteratorCloseable<Long> getTimestampIterator(boolean descending) {
        return map.getIndexIterator(timestampIndex.getColumnFamilyHandle(), descending, true);
    }


    /*********************** GLEB

     public Collection<Long> getFromToKeys(long fromKey, long toKey) {
     List<Long> result = new ArrayList<>();
     for (long key = fromKey; key < toKey; key++) {
     if (rocksDBTable.containsKey(key)) {
     result.add(key);
     }
     }
     return result;

     }

     @SuppressWarnings({"rawtypes", "unchecked"})

     public Iterable findTransactionsKeys(String address, String sender, String recipient,
     int type, boolean desc, int offset, int limit, long timestamp) {
     Iterable senderKeys = null;
     Iterable recipientKeys = null;
     TreeSet<Object> treeKeys = new TreeSet<>();

     if (address != null) {
     sender = address;
     recipient = address;
     }

     if (sender == null && recipient == null) {
     return treeKeys;
     }
     //  timestamp = null;
     senderKeys = receiveIndexKeys(sender, type, timestamp, senderKeys, senderUnconfirmedTransactionIndexName);
     recipientKeys = receiveIndexKeys(recipient, type, timestamp, recipientKeys, recipientUnconfirmedTransactionIndexName);
     if (address != null) {
     treeKeys.addAll(Sets.newTreeSet(senderKeys));
     treeKeys.addAll(Sets.newTreeSet(recipientKeys));
     } else if (sender != null && recipient != null) {
     treeKeys.addAll(Sets.newTreeSet(senderKeys));
     treeKeys.retainAll(Sets.newTreeSet(recipientKeys));
     } else if (sender != null) {
     treeKeys.addAll(Sets.newTreeSet(senderKeys));
     } else {
     treeKeys.addAll(Sets.newTreeSet(recipientKeys));
     }

     Iterable keys;
     if (desc) {
     keys = ((TreeSet) treeKeys).descendingSet();
     } else {
     keys = treeKeys;
     }
     limit = (limit == 0) ? Iterables.size(keys) : limit;
     return Iterables.limit(Iterables.skip(keys, offset), limit);

     }

     private Iterable receiveIndexKeys(String recipient, int type, long timestamp, Iterable recipientKeys, String recipientUnconfirmedTransactionIndexName) {
     if (recipient != null) {
     if (type > 0) {
     recipientKeys = rocksDBTable.filterAppropriateValuesAsKeys(
     indexByteableTuple3StringLongInteger.toBytes(new Tuple3<>(recipient, timestamp, type), null),
     rocksDBTable.getIndexByName(addressTypeUnconfirmedTransactionIndexName));
     } else {
     recipientKeys = rocksDBTable.filterAppropriateValuesAsKeys(recipient.getBytes(), rocksDBTable.getIndexByName(recipientUnconfirmedTransactionIndexName));
     }
     }
     return recipientKeys;
     }

     public List<Transaction> findTransactions(String address, String sender, String recipient,
     int type, boolean desc, int offset, int limit, long timestamp) {

     Iterable keys = findTransactionsKeys(address, sender, recipient,
     type, desc, offset, limit, timestamp);
     return getUnconfirmedTransaction(keys);

     }


     public List<Transaction> getUnconfirmedTransaction(Iterable keys) {
     Iterator iter = keys.iterator();
     List<Transaction> transactions = new ArrayList<>();
     Transaction item;
     Long key;
     while (iter.hasNext()) {
     key = (Long) iter.next();
     item = this.rocksDBTable.get(key);
     transactions.add(item);
     }
     return transactions;
     }

     public List<Transaction> getTransactionsByAddressFast100(String address, int limitSize) {
     HashSet<Long> treeKeys = new HashSet<>();
     Set<Long> senderKeys = rocksDBTable.filterAppropriateValuesAsKeys(address.getBytes(), rocksDBTable.getIndexByName(senderUnconfirmedTransactionIndexName));
     List<Long> senderKeysLimit = senderKeys.stream().limit(limitSize).collect(Collectors.toList());
     Set<Long> recipientKeys = rocksDBTable.filterAppropriateValuesAsKeys(address.getBytes(), rocksDBTable.getIndexByName(recipientUnconfirmedTransactionIndexName));
     List<Long> recipientKeysLimit = recipientKeys.stream().limit(limitSize).collect(Collectors.toList());
     treeKeys.addAll(senderKeysLimit);
     treeKeys.addAll(recipientKeysLimit);
     return getUnconfirmedTransaction(Iterables.limit(treeKeys, limitSize));

     }

     // slow?? without index
     public List<Transaction> getTransactionsByAddress(String address) {
     ArrayList<Transaction> result = new ArrayList<Transaction>();
     Iterator<Long> iterator = getIterator(false);
     Account account = new Account(address);

     Transaction transaction;
     boolean ok;

     int i = 0;
     int n = 100;
     while (iterator.hasNext()) {
     transaction = rocksDBTable.get(iterator.next());
     ok = transaction.getCreator().getAddress().equals(address);
     if (!ok) {
     HashSet<Account> recipients = transaction.getRecipientAccounts();
     if (recipients == null || recipients.isEmpty() || !recipients.contains(account)) {
     continue;
     }
     }
     // SET LIMIT
     if (++i > n) {
     break;
     }
     result.add(transaction);
     }
     return result;
     }

     public List<Transaction> getTransactions(boolean descending) {
     ArrayList<Transaction> result = new ArrayList<Transaction>();
     logger.debug("get ITERATOR");
     Iterator<Long> iterator = getIterator(descending);
     logger.debug("get ITERATOR - DONE");

     Transaction transaction;
     while (iterator.hasNext()) {
     transaction = get(iterator.next());
     result.add(transaction);
     }
     return result;
     }

     public List<Transaction> getIncomedTransactions(String address, int type, long timestamp, int count, boolean descending) {
     ArrayList<Transaction> result = new ArrayList<>();
     Iterator<Long> iterator = getIterator(descending);
     Account account = new Account(address);
     int i = 0;
     Transaction transaction;
     while (iterator.hasNext()) {
     transaction = rocksDBTable.get(iterator.next());
     if (type != 0 && type != transaction.getType()) {
     continue;
     }
     HashSet<Account> recipients = transaction.getRecipientAccounts();
     if (recipients == null || recipients.isEmpty()) {
     continue;
     }
     if (recipients.contains(account) && transaction.getTimestamp() >= timestamp) {
     result.add(transaction);
     i++;
     if (count > 0 && i > count) {
     break;
     }
     }
     }
     return result;
     }
     */
}
