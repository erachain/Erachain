package org.erachain.dbs.rocksDB;

import com.google.common.collect.Iterables;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.TransactionSuit;
import org.erachain.dbs.rocksDB.common.DBIterator;
import org.erachain.dbs.rocksDB.common.RocksDB;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableLong;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableTuple3StringLongInteger;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class TransactionSuitRocksDB extends DBMapSuit<Long, Transaction> implements TransactionSuit
{

    static Logger logger = LoggerFactory.getLogger(TransactionSuitRocksDB.class.getSimpleName());

    private final String NAME_TABLE = "TRANSACTIONS_UNCONFIRMED_TABLE";
    private final String timestampIndexName = "timestamp_unc_txs";
    private final String senderIndexName = "sender_unc_txs";
    private final String recipientsIndexName = "recipient_unc_txs";
    private final String addressTypeIndexName = "address_type_unc_txs";

    private SimpleIndexDB<Long, Transaction, Long> timestampIndex;
    //private IndexByteableTuple3StringLongInteger indexByteableTuple3StringLongInteger;
    private SimpleIndexDB<Long, Transaction, Fun.Tuple2<String, Long>> senderIndex;
    private ArrayIndexDB<Long, Transaction, String> recipientsIndex;
    ListIndexDB<Long, Transaction, Fun.Tuple3<String, Long, Integer>> addressTypeIndex;

    public TransactionSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    protected void getMap() {

        timestampIndex = new SimpleIndexDB<>(timestampIndexName,
                new BiFunction<Long, Transaction, Long>() {
                    @Override
                    public Long apply(Long aLong, Transaction transaction) {
                        return transaction.getTimestamp();
                    }
                //}, (result, key) ->new ByteableLong().toBytesObject(result)); // создает Класс на лету и переопределяет его метод
                }, new IndexByteableLong()); // а тут мы уже создали заранее Класс

        senderIndex = new SimpleIndexDB<>(senderIndexName,
                new BiFunction<Long, Transaction, Fun.Tuple2<String, Long>>() {
                    @Override
                    public Fun.Tuple2<String, Long> apply(Long aLong, Transaction transaction) {
                        Account account = transaction.getCreator();
                        return new Fun.Tuple2<>(account == null ? "genesis" : account.getAddress(), transaction.getTimestamp());
                    }
                }, (result, key) -> org.bouncycastle.util.Arrays.concatenate(
                new ByteableString().toBytesObject(result.a),
                new ByteableLong().toBytesObject(result.b)));

        recipientsIndex = new ArrayIndexDB<>(recipientsIndexName,
                (aLong, transaction) -> transaction.getRecipientAccounts().stream().map(Account::getAddress).toArray(String[]::new),
                (result, key) -> new ByteableString().toBytesObject(result));

        //indexByteableTuple3StringLongInteger = new IndexByteableTuple3StringLongInteger();
        addressTypeIndex
                = new ListIndexDB<>(addressTypeIndexName,
                (aLong, transaction) -> {
                    Integer type = transaction.getType();
                    return transaction.getInvolvedAccounts().stream().map(
                            (account) -> (new Fun.Tuple3<>(account.getAddress(), transaction.getTimestamp(), type))).collect(Collectors.toList());
                }, new IndexByteableTuple3StringLongInteger());

        List indexes = new ArrayList<>();
        indexes.add(timestampIndex);
        indexes.add(addressTypeIndex);
        indexes.add(senderIndex);
        indexes.add(recipientsIndex);

        map = new DBRocksDBTable<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                databaseSet);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
    }

    @Override
    public Transaction getDefaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    public Iterable typeKeys(String sender, Long timestamp, Integer type) {
        return ((RocksDB)map).filterAppropriateValuesAsKeys(
                toBytesStringLongInteger.toBytes(sender, timestamp, type),
                addressTypeIndex.getColumnFamilyHandle());
    }

    @Override
    public Iterable senderKeys(String sender) {
        return ((RocksDB)map).filterAppropriateValuesAsKeys(sender.getBytes(),
                senderIndex.getColumnFamilyHandle());
    }

    @Override
    public Iterable recipientKeys(String recipient) {
        return ((RocksDB)map).filterAppropriateValuesAsKeys(recipient.getBytes(),
                recipientsIndex.getColumnFamilyHandle());
    }

    @Override
    public Iterator<Long> getTimestampIterator() {
        return map.getIndexIterator(timestampIndex.getColumnFamilyHandle(), false);
    }

    @Override
    public Collection<Long> getFromToKeys(long fromKey, long toKey) {
        Iterable iterable = (Iterable) map.getIndexIterator(timestampIndex.getColumnFamilyHandle(), true);
        Iterable iterableLimit = Iterables.limit(Iterables.skip(iterable, (int) fromKey), (int) (toKey - fromKey));

        List<Long> treeKeys = new ArrayList<Long>();

        Iterator<Fun.Tuple2<Long, Long>> iterator = iterableLimit.iterator();
        while (iterator.hasNext()) {
            treeKeys.add(iterator.next().b);
        }

        return treeKeys;

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
