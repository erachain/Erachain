package org.erachain.dbs.rocksDB;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.TransactionFinalSuit;
import org.erachain.datachain.TransactionSuit;
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
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class TransactionFinalSuitRocksDB extends DBMapSuit<Long, Transaction> implements TransactionFinalSuit
{

    static Logger logger = LoggerFactory.getLogger(TransactionFinalSuitRocksDB.class.getSimpleName());

    private static final int CUT_NAME_INDEX = 12;
    private final String NAME_TABLE = "TRANSACTION_FINAL_TABLE";
    private final String senderTransactionsIndexName = "senderTxs";
    private final String recipientTransactionsIndexName = "recipientTxs";
    private final String addressTypeTransactionsIndexName = "addressTypeTxs";
    private final String titleTypeTransactionsIndexName = "titleTypeTxs";


    SimpleIndexDB<Long, Transaction, String> senderTxs;
    ListIndexDB<Long, Transaction, String> recipientTxs;
    ListIndexDB<Long, Transaction, Fun.Tuple2<String, Integer>> addressTypeTxs;
    ArrayIndexDB<Long, Transaction, String> titleTypeTxs;

    public TransactionFinalSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    protected void getMap() {

        map = new DBRocksDBTable<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                databaseSet);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {

        if (Controller.getInstance().onlyProtocolIndexing) {
            return;
        }

        senderTxs = new SimpleIndexDB<>(senderTransactionsIndexName,
                (aLong, transaction) -> {
                    Account account = transaction.getCreator();
                    return (account == null ? "genesis" : account.getAddress());
                }, (result, key) -> result.getBytes());

        recipientTxs = new ListIndexDB<>(recipientTransactionsIndexName,
                (Long aLong, Transaction transaction) -> {
                    List<String> recipients = new ArrayList<>();
                    for (Account account : transaction.getRecipientAccounts()) {
                        recipients.add(account.getAddress());
                    }
                    return recipients;
                }, (result, key) -> result.getBytes());

        addressTypeTxs = new ListIndexDB<>(addressTypeTransactionsIndexName,
                (aLong, transaction) -> {
                    Integer type = transaction.getType();
                    List<Tuple2<String, Integer>> addressesTypes = new ArrayList<>();
                    for (Account account : transaction.getInvolvedAccounts()) {
                        addressesTypes.add(new Tuple2<>(account.getAddress(), type));
                    }
                    return addressesTypes;
                },
                (result, key) -> {
                    if (result == null) {
                        return null;
                    }
                    return org.bouncycastle.util.Arrays.concatenate(result.a.getBytes(), Ints.toByteArray(result.b));
                }
        );

        titleTypeTxs = new ArrayIndexDB<>(titleTypeTransactionsIndexName,
                (aLong, transaction) -> {
                    String title = transaction.getTitle();
                    if (title == null || title.isEmpty()) {
                        return new String[0];
                    }
                    String[] tokens = title.toLowerCase().split(" ");
                    String[] keys = new String[tokens.length];
                    for (int i = 0; i < tokens.length; i++) {
                        if (tokens[i].length() > CUT_NAME_INDEX) {
                            tokens[i] = tokens[i].substring(0, CUT_NAME_INDEX);
                        }
                        keys[i] = tokens[i];
                    }
                    return keys;
                }, (result, key) -> result.getBytes());

        indexes = new ArrayList<>();
        indexes.add(senderTxs);
        indexes.add(recipientTxs);
        indexes.add(addressTypeTxs);
        indexes.add(titleTypeTxs);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getBlockIterator(Integer height) {
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        return (Iterator) ((RocksDB)map).indexIteratorFilter(false, Ints.toByteArray(height));

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getIteratorBySender(String address) {
        return (Iterator) ((RocksDB)map).indexIteratorFilter(false, senderTxs.getColumnFamilyHandle(), address.getBytes());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getIteratorByRecipient(String address) {
        return (Iterator) ((RocksDB)map).indexIteratorFilter(false, recipientTxs.getColumnFamilyHandle(), address.getBytes());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Iterator<Long> getIteratorByAddressAndType(String address, Integer type) {
        return (Iterator) ((RocksDB)map).indexIteratorFilter(false, addressTypeTxs.getColumnFamilyHandle(),
                Arrays.concatenate(address.getBytes(), Ints.toByteArray(type));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Iterator<Long> getIteratorByTitleAndType(String filter, boolean asFilter, Integer type) {


        String filterLower = filter.toLowerCase();
        Iterable keys = Fun.filter(this.titleKey,
                new Tuple2<String, Integer>(filterLower,
                        type==0?0:type), true,
                new Tuple2<String, Integer>(asFilter?
                        filterLower + new String(new byte[]{(byte)255}) : filterLower,
                        type==0?Integer.MAX_VALUE:type), true);

        Iterator iter = keys.iterator();
        return iter;
    }

    /**
     *
     * @param filter
     * @param asFilter - use filter
     * @param type
     * @param offset
     * @param limit
     * @return
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator getIteratorByTitleAndType(String filter, boolean asFilter, Integer type, int offset, int limit) {

        String filterLower = filter.toLowerCase();

        Iterator iterator = Fun.filter(this.titleKey,
                new Tuple2<String, Integer>(filterLower,
                        type==0?0:type), true,
                new Tuple2<String, Integer>(asFilter?
                        filterLower + new String(new byte[]{(byte)255}) : filterLower,
                        type==0?Integer.MAX_VALUE:type), true).iterator();

        if (offset > 0)
            Iterators.advance(iterator, offset);

        if (limit > 0)
            iterator = Iterators.limit(iterator, limit);

        return iterator;

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Iterator<Long> getIteratorByAddress(String address) {
        Iterator<Long> senderKeys = (Iterator)((RocksDB)map).indexIteratorFilter(false, senderTxs.getColumnFamilyHandle(), address.getBytes());
        Iterator<Long> recipientKeys = (Iterator)((RocksDB)map).indexIteratorFilter(false, recipientTxs.getColumnFamilyHandle(), address.getBytes());

        return Iterators.concat(senderKeys, recipientKeys);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public int getTransactionsByAddressCount(String address) {
        Iterator senderKeys = Fun.filter(this.senderKey, address).iterator();
        Iterator recipientKeys = Fun.filter(this.recipientKey, address).iterator();

        Iterator<Long> treeKeys = new TreeSet<Long>().iterator();

        treeKeys = Iterators.concat(senderKeys, recipientKeys);

        return Iterators.size(treeKeys);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public int findTransactionsCount(String address, String sender, String recipient, final int minHeight,
                                     final int maxHeight, int type, int service, boolean desc, int offset, int limit) {
        Iterator keys = findTransactionsKeys(address, sender, recipient, minHeight, maxHeight, type, service, desc,
                offset, limit);
        return Iterators.size(keys);
    }

    /**
     * @param address
     * @param sender
     * @param recipient
     * @param minHeight
     * @param maxHeight
     * @param type
     * @param service
     * @param desc
     * @param offset
     * @param limit
     * @return
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Iterator findTransactionsKeys(String address, String sender, String recipient, final int minHeight,
                                         final int maxHeight, int type, final int service, boolean desc, int offset, int limit) {
        Iterator senderKeys = null;
        Iterator recipientKeys = null;
        Iterator iterator = new TreeSet<>().iterator();

        if (address != null) {
            sender = address;
            recipient = address;
        }

        if (sender == null && recipient == null) {
            return iterator;
        }

        if (sender != null) {
            if (type != 0) {
                senderKeys = Fun.filter(this.addressTypeKey, new Tuple2<String, Integer>(sender, type)).iterator();
            } else {
                senderKeys = Fun.filter(this.senderKey, sender).iterator();
            }
        }

        if (recipient != null) {
            if (type != 0) {
                recipientKeys = Fun.filter(this.addressTypeKey, new Tuple2<String, Integer>(recipient, type)).iterator();
            } else {
                recipientKeys = Fun.filter(this.recipientKey, recipient).iterator();
            }
        }

        if (address != null) {
            iterator = Iterators.concat(senderKeys, recipientKeys);
        } else if (sender != null && recipient != null) {
            iterator = senderKeys;
            Iterators.retainAll(iterator, Lists.newArrayList(recipientKeys));
        } else if (sender != null) {
            iterator = senderKeys;
        } else if (recipient != null) {
            iterator = recipientKeys;
        }

        if (minHeight != 0 || maxHeight != 0) {
            iterator = Iterators.filter(iterator, new Predicate<Long>() {
                @Override
                public boolean apply(Long key) {
                    Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                    return (minHeight == 0 || pair.a >= minHeight) && (maxHeight == 0 || pair.a <= maxHeight);
                }
            });
        }

        if (false && type == Transaction.ARBITRARY_TRANSACTION && service != 0) {
            iterator = Iterators.filter(iterator, new Predicate<Long>() {
                @Override
                public boolean apply(Long key) {
                    ArbitraryTransaction tx = (ArbitraryTransaction) map.get(key);
                    return tx.getService() == service;
                }
            });
        }

        if (desc) {
            //keys = ((TreeSet) iterator).descendingSet();
            iterator = Lists.reverse(Lists.newArrayList(iterator)).iterator();
        }

        //limit = (limit == 0) ? Iterables.size(keys) : limit;
        limit = (limit == 0) ? Iterators.size(iterator) : limit;

        Iterators.advance(iterator, offset);
        iterator = Iterators.limit(iterator, limit);


        return iterator;
    }

    ///////////
    //////////////////




    private Pair<Integer, Set<Long>> getKeysByFilterAsArrayRecurse(int step, String[] filterArray) {

        keys = rocksDBTable.filterAppropriateValuesAsKeys(stepFilter.getBytes(),
                rocksDBTable.receiveIndexByName(titleTypeTransactionsIndexName));

        Set<Long> keys;
        String stepFilter = filterArray[step];
        if (!stepFilter.endsWith("!")) {
            // это сокращение для диаппазона
            if (stepFilter.length() < 5) {
                // ошибка - ищем как полное слово
                keys = rocksDBTable.filterAppropriateValuesAsKeys(stepFilter.getBytes(),
                        rocksDBTable.receiveIndexByName(titleTypeTransactionsIndexName));
            } else {
                if (stepFilter.length() > CUT_NAME_INDEX) {
                    stepFilter = stepFilter.substring(0, CUT_NAME_INDEX);
                }
                keys = rocksDBTable.filterAppropriateValuesAsKeys(stepFilter.getBytes(),
                        rocksDBTable.receiveIndexByName(titleTypeTransactionsIndexName));
            }
        } else {
            // поиск целиком
            stepFilter = stepFilter.substring(0, stepFilter.length() - 1);
            if (stepFilter.length() > CUT_NAME_INDEX) {
                stepFilter = stepFilter.substring(0, CUT_NAME_INDEX);
            }
            keys = rocksDBTable.filterAppropriateValuesAsKeys(stepFilter.getBytes(),
                    rocksDBTable.receiveIndexByName(titleTypeTransactionsIndexName));
        }
        if (step > 0) {
            // погнали в РЕКУРСИЮ
            Pair<Integer, Set<Long>> result = getKeysByFilterAsArrayRecurse(--step, filterArray);

            if (result.getA() > 0) {
                return result;
            }

            // в рекурсии все хорошо - соберем ключи
            Iterator iterator = keys.iterator();
            Set<Long> hashSet = result.getB();
            Set<Long> andHashSet = new HashSet<Long>();

            // берем только совпадающие в обоих списках
            while (iterator.hasNext()) {
                Long key = (Long) iterator.next();
                if (hashSet.contains(key)) {
                    andHashSet.add(key);
                }
            }

            return new Pair<>(0, andHashSet);

        } else {

            // последний шаг - просто все добавим
            Iterator iterator = keys.iterator();
            HashSet<Long> hashSet = new HashSet<>();
            while (iterator.hasNext()) {
                Long key = (Long) iterator.next();
                hashSet.add(key);
            }

            return new Pair<>(0, hashSet);

        }

    }

    private List<Transaction> transformKeysIntoTransactions(Set<Long> keys, int limit) {
        Iterator<Long> iter = keys.iterator();
        List<Transaction> transactions = new ArrayList<>();
        int counter = 0;
        while (iter.hasNext() && (limit == 0 || counter < limit)) {
            Long key = iter.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            Transaction item = rocksDBTable.get(key);
            item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.f0, pair.f1);
            transactions.add(item);
            counter++;
        }
        return transactions;
    }

}
