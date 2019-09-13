package org.erachain.datachain;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import org.apache.flink.api.java.tuple.Tuple2;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableTuple3StringLongInteger;
import org.erachain.dbs.rocksDB.integration.DBMapDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.erachain.utils.ObserverMessage;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

/**
 * Храним неподтвержденные транзакции - memory pool for unconfirmed transaction.
 * Signature (as Long) -> Transaction
 * <hr>
 * Здесь вторичные индексы создаются по несколько для одной записи путем создания массива ключей,
 * см. typeKey и recipientKey. Они используются для API RPC block explorer.
 * Нужно огрничивать размер выдаваемого списка чтобы не перегружать ноду.
 * <br>
 * Так же вторичный индекс по времени, который используется в ГУИ TIMESTAMP_INDEX = 0 (default)
 * - он оргнизыется внутри DCMap в списке индексов для сортировок в ГУИ
 *
 * Также хранит инфо каким пирам мы уже разослали транзакцию неподтвержденную так что бы при подключении делать автоматически broadcast
 *
 *  <hr>
 *  (!!!) для создания уникальных ключей НЕ нужно добавлять + val.viewTimestamp(), и так работант, а почему в Ордерах не работало?
 *  <br>в БИНДЕ внутри уникальные ключи создаются добавлением основного ключа
 */
public class TransactionRocksDBMap extends org.erachain.dbs.rocksDB.DCMap<Long, Transaction> implements TransactionMap {

    static Logger logger = LoggerFactory.getLogger(TransactionMap.class.getSimpleName());

    public int totalDeleted = 0;

    public static final int TIMESTAMP_INDEX = 1;

    private final String NAME_TABLE = "TRANSACTIONS_UNCONFIRMED_TABLE";
    private final String senderUnconfirmedTransactionIndexName = "sender_unc_txs";
    private final String recipientUnconfirmedTransactionIndexName = "recipient_unc_txs";
    private final String addressTypeUnconfirmedTransactionIndexName = "address_type_unc_txs";

    //private List<IndexDB> indexes;
    private IndexByteableTuple3StringLongInteger indexByteableTuple3StringLongInteger;
    private SimpleIndexDB<Long, Transaction, Tuple2<String, Long>> senderUnconfirmedTransactionIndex;


    public TransactionRocksDBMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
        if (databaseSet.isWithObserver()) {
            observableData.put(NOTIFY_RESET, ObserverMessage.RESET_UNC_TRANSACTION_TYPE);
            observableData.put(NOTIFY_LIST, ObserverMessage.LIST_UNC_TRANSACTION_TYPE);
            observableData.put(NOTIFY_ADD, ObserverMessage.ADD_UNC_TRANSACTION_TYPE);
            observableData.put(NOTIFY_REMOVE, ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE);
        }
    }

    public TransactionRocksDBMap(TransactionRocksDBMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    protected void createIndexes() {
        senderUnconfirmedTransactionIndex = new SimpleIndexDB<>(senderUnconfirmedTransactionIndexName, new BiFunction<Long, Transaction, org.apache.flink.api.java.tuple.Tuple2<String, Long>>() {
            @Override
            public org.apache.flink.api.java.tuple.Tuple2<String, Long> apply(Long aLong, Transaction transaction) {
                Account account = transaction.getCreator();
                return new Tuple2<>(account == null ? "genesis" : account.getAddress(), transaction.getTimestamp());
            }
        }, (result, key) -> org.bouncycastle.util.Arrays.concatenate(
                new ByteableString().toBytesObject(result.f0),
                new ByteableLong().toBytesObject(result.f1)));


        ArrayIndexDB<Long, Transaction, String> recipientsUnconfirmedTransactionIndex = new ArrayIndexDB<>(recipientUnconfirmedTransactionIndexName,
                (aLong, transaction) -> transaction.getRecipientAccounts().stream().map(Account::getAddress).toArray(String[]::new),
                (result, key) -> new ByteableString().toBytesObject(result));
        indexByteableTuple3StringLongInteger = new IndexByteableTuple3StringLongInteger();
        ListIndexDB<Long, Transaction, Fun.Tuple3<String, Long, Integer>> addressTypeUnconfirmedTransactionIndex
                = new ListIndexDB<>(addressTypeUnconfirmedTransactionIndexName,
                (aLong, transaction) -> {
                    Integer type = transaction.getType();
                    return transaction.getInvolvedAccounts().stream().map(
                            (account) -> (new Fun.Tuple3<>(account.getAddress(), transaction.getTimestamp(), type))).collect(Collectors.toList());
                }, indexByteableTuple3StringLongInteger);
        indexes = new ArrayList<>();
        indexes.add(senderUnconfirmedTransactionIndex);
        indexes.add(recipientsUnconfirmedTransactionIndex);
        indexes.add(addressTypeUnconfirmedTransactionIndex);
    }

    public void setObservableData(int index, Integer data) {
        this.observableData.put(index, data);
    }

    @Override
    protected void getMap(DB database) {
        tableDB = new DBRocksDBTable<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7,64,32,
                        256,10,
                        1,256,32,false),
                ROCKS_DB_FOLDER);
    }

    @Override
    protected void getMemoryMap() {
        tableDB = new DBMapDB<>(new HashMap<>());
    }


    public Iterator<Long> getTimestampIterator() {
        return getIndexIterator(senderUnconfirmedTransactionIndex, false);
    }

    Iterable receiveIndexKeys(sender, type, timestamp, senderKeys, senderUnconfirmedTransactionIndexName);
    recipientKeys = receiveIndexKeys(recipient, type, timestamp, recipientKeys, recipientUnconfirmedTransactionIndexName);


    /**
     * Find all unconfirmed transaction by address, sender or recipient.
     * Need set only one parameter(address, sender,recipient)
     *
     * @param address   - address
     * @param sender    - sender
     * @param recipient - recipient
     * @param type      - type transaction
     * @param desc      - order by transaction
     * @param offset    -
     * @param limit     - count transaction
     * @return Key transactions
     */
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
                        rocksDBTable.receiveIndexByName(addressTypeUnconfirmedTransactionIndexName));
            } else {
                recipientKeys = rocksDBTable.filterAppropriateValuesAsKeys(recipient.getBytes(), rocksDBTable.receiveIndexByName(recipientUnconfirmedTransactionIndexName));
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
        Set<Long> senderKeys = rocksDBTable.filterAppropriateValuesAsKeys(address.getBytes(), rocksDBTable.receiveIndexByName(senderUnconfirmedTransactionIndexName));
        List<Long> senderKeysLimit = senderKeys.stream().limit(limitSize).collect(Collectors.toList());
        Set<Long> recipientKeys = rocksDBTable.filterAppropriateValuesAsKeys(address.getBytes(), rocksDBTable.receiveIndexByName(recipientUnconfirmedTransactionIndexName));
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
}
