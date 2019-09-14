package org.erachain.dbs.rocksDB;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.dbs.mapDB.DBMapSuit;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableTuple3StringLongInteger;
import org.erachain.dbs.rocksDB.integration.DBMapDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.InnerDBTable;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

public class TransactionRocksDBMap extends DBMapSuit<Long, Transaction>
{

    static Logger logger = LoggerFactory.getLogger(TransactionRocksDBMap.class.getSimpleName());

    private final String NAME_TABLE = "TRANSACTIONS_UNCONFIRMED_TABLE";
    private final String senderUnconfirmedTransactionIndexName = "sender_unc_txs";
    private final String recipientUnconfirmedTransactionIndexName = "recipient_unc_txs";
    private final String addressTypeUnconfirmedTransactionIndexName = "address_type_unc_txs";

    private InnerDBTable<Long, Transaction> map;
    private List<IndexDB> indexes;
    private IndexByteableTuple3StringLongInteger indexByteableTuple3StringLongInteger;
    private SimpleIndexDB<Long, Transaction, Fun.Tuple2<String, Long>> senderUnconfirmedTransactionIndex;


    public TransactionRocksDBMap(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    protected void getMap() {
        map = new DBRocksDBTable<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                ROCKS_DB_FOLDER);
    }

    @Override
    protected void createIndexes() {
        senderUnconfirmedTransactionIndex = new SimpleIndexDB<>(senderUnconfirmedTransactionIndexName,
                new BiFunction<Long, Transaction, Fun.Tuple2<String, Long>>() {
                    @Override
                    public Fun.Tuple2<String, Long> apply(Long aLong, Transaction transaction) {
                        Account account = transaction.getCreator();
                        return new Fun.Tuple2<>(account == null ? "genesis" : account.getAddress(), transaction.getTimestamp());
                    }
                }, (result, key) -> org.bouncycastle.util.Arrays.concatenate(
                new ByteableString().toBytesObject(result.a),
                new ByteableLong().toBytesObject(result.b)));


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

    protected Transaction getDefaultValue() {
        return null;
    }

    /**
     * @param descending true if need descending sort
     * @return
     */
    public Iterator<Long> getIndexIterator(IndexDB indexDB, boolean descending) {
        return map.getIndexIterator(descending, indexDB);
    }

    public Iterator<Long> getIterator(boolean descending) {
        return map.getIterator(descending);
    }

    @Override
    public Iterator<Long> getIterator(int index, boolean descending) {
        return map.getIndexIterator(descending, index);
    }

    public Iterator<Long> getTimestampIterator() {
        return getIndexIterator(senderUnconfirmedTransactionIndex, false);
    }

    public Iterator<Long> getCeatorIterator() {
        return null;
    }


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
                if (map instanceof DBRocksDBTable) {
                    recipientKeys = ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(
                            indexByteableTuple3StringLongInteger.toBytes(new Fun.Tuple3<>(recipient, timestamp, type), null),
                            ((DBRocksDBTable) map).receiveIndexByName(addressTypeUnconfirmedTransactionIndexName));
                } else {
                    recipientKeys = (Iterable)((DBMapDB) map).getIterator(false);
                }
            } else {
                if (map instanceof DBRocksDBTable) {
                    recipientKeys = ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(recipient.getBytes(),
                            ((DBRocksDBTable) map).receiveIndexByName(recipientUnconfirmedTransactionIndexName));
                } else {
                    recipientKeys = (Iterable)((DBMapDB) map).getIterator(false);
                }
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
            item = map.get(key);
            transactions.add(item);
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByAddressFast100(String address, int limitSize) {
        HashSet<Long> treeKeys = new HashSet<>();
        Set<Long> senderKeys = ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(address.getBytes(),
                ((DBRocksDBTable) map).receiveIndexByName(senderUnconfirmedTransactionIndexName));
        List<Long> senderKeysLimit = senderKeys.stream().limit(limitSize).collect(Collectors.toList());
        Set<Long> recipientKeys = ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(address.getBytes(),
                ((DBRocksDBTable) map).receiveIndexByName(recipientUnconfirmedTransactionIndexName));
        List<Long> recipientKeysLimit = recipientKeys.stream().limit(limitSize).collect(Collectors.toList());
        treeKeys.addAll(senderKeysLimit);
        treeKeys.addAll(recipientKeysLimit);
        return getUnconfirmedTransaction(Iterables.limit(treeKeys, limitSize));

    }

    public void clearByDeadTimeAndLimit(long timestamp, boolean cutDeadTime) {
         Iterator<Long> iterator = getIterator(false); // getTimestampIterator();
    }

    // TODO выдает ошибку на шаге treeKeys.addAll(Sets.newTreeSet(senderKeys));
    public List<Transaction> getTransactionsByAddressFast100(String address) {
        return getTransactionsByAddressFast100(address, 100);

    }

}
