package org.erachain.dbs.rocksDB;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalSuit;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;
import org.spongycastle.util.Arrays;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

@Slf4j
public class TransactionFinalSuitRocksDB extends DBMapSuit<Long, Transaction> implements TransactionFinalSuit
{

    private static final int CUT_NAME_INDEX = 12;
    private final String NAME_TABLE = "TRANSACTION_FINAL_TABLE";
    private final String senderTransactionsIndexName = "senderTxs";
    private final String recipientTransactionsIndexName = "recipientTxs";
    private final String addressTypeTransactionsIndexName = "addressTypeTxs";
    private final String titleTypeTransactionsIndexName = "titleTypeTxs";


    SimpleIndexDB<Long, Transaction, String> senderTxs;
    ListIndexDB<Long, Transaction, String> recipientTxs;
    ListIndexDB<Long, Transaction, Fun.Tuple2<String, Integer>> addressTypeTxs;
    ArrayIndexDB<Long, Transaction, Fun.Tuple2<String, Integer>> titleTypeTxs;

    public TransactionFinalSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, null);
    }

    @Override
    protected void getMap() {

        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(),
                databaseSet);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {

        // USE counter index
        indexes = new ArrayList<>();

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

                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    transaction.setDC((DCSet) databaseSet);

                    for (Account account : transaction.getRecipientAccounts()) {
                        recipients.add(account.getAddress());
                    }
                    return recipients;
                }, (result, key) -> result.getBytes());

        addressTypeTxs = new ListIndexDB<>(addressTypeTransactionsIndexName,
                (aLong, transaction) -> {

                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    transaction.setDC((DCSet) databaseSet);

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

                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    transaction.setDC((DCSet) databaseSet);

                    String title = transaction.getTitle();
                    if (title == null || title.isEmpty() || title.equals(""))
                        return null;

                    // see https://regexr.com/
                    String[] tokens = title.toLowerCase().split(DCSet.SPLIT_CHARS);
                    Tuple2<String, Integer>[] keys = new Tuple2[tokens.length];
                    for (int i = 0; i < tokens.length; ++i) {
                        if (tokens[i].length() > CUT_NAME_INDEX) {
                            tokens[i] = tokens[i].substring(0, CUT_NAME_INDEX);
                        }
                        //keys[i] = tokens[i];
                        keys[i] = new Tuple2<String, Integer>(tokens[i], transaction.getType());
                    }

                    return keys;
                }, (result, key) -> {
            if (result == null) {
                return null;
            }
            return org.bouncycastle.util.Arrays.concatenate(result.a.getBytes(), Ints.toByteArray(result.b));
        }
        );

        indexes.add(senderTxs);
        indexes.add(recipientTxs);
        indexes.add(addressTypeTxs);
        indexes.add(titleTypeTxs);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getBlockIterator(Integer height) {
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(Ints.toByteArray(height), false);

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getIteratorBySender(String address) {
        return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(senderTxs.getColumnFamilyHandle(), address.getBytes(), false);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getIteratorByRecipient(String address) {
        return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(recipientTxs.getColumnFamilyHandle(), address.getBytes(), false);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Iterator<Long> getIteratorByAddressAndType(String address, Integer type) {
        return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(), Arrays.concatenate(address.getBytes(), Ints.toByteArray(type)), false
        );
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Iterator<Long> getIteratorByTitleAndType(String filter, boolean asFilter, Integer type) {

        String filterLower = filter.toLowerCase();
        //Iterable keys = Fun.filter(this.titleKey,
        //        new Tuple2<String, Integer>(filterLower,
        //                type==0?0:type), true,
        //        new Tuple2<String, Integer>(asFilter?
        //                filterLower + new String(new byte[]{(byte)255}) : filterLower,
        //                type==0?Integer.MAX_VALUE:type), true);

        return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(titleTypeTxs.getColumnFamilyHandle(), type == 0 ? filter.getBytes()
                        : Arrays.concatenate(filter.getBytes(), Ints.toByteArray(type))
                , false);

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Iterator<Long> getIteratorByAddress(String address) {
        Iterator senderKeys = ((DBRocksDBTable) map).getIndexIteratorFilter(senderTxs.getColumnFamilyHandle(), address.getBytes(), false);
        Iterator recipientKeys = ((DBRocksDBTable) map).getIndexIteratorFilter(recipientTxs.getColumnFamilyHandle(), address.getBytes(), false);

        return Iterators.concat(senderKeys, recipientKeys);
    }

    /**
     * Пока это не используется - на верхнем уровне своя сборка общая от получаемых Итераторов с этого класса.
     * Возможно потом с более конкретным проходом по DESCENDING + OFFSET & LIMIT буджет реализация у каждой СУБД своя?
     * Хотя нет - просто в Iterator перебор по индексаю таблицы у СУБД уже свой реализован
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
                //senderKeys = Fun.filter(this.addressTypeKey, new Tuple2<String, Integer>(sender, type)).iterator();
                senderKeys = ((DBRocksDBTable) map).getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(), Arrays.concatenate(sender.getBytes(), Ints.toByteArray(type))
                        , false);
            } else {
                senderKeys = ((DBRocksDBTable) map).getIndexIteratorFilter(senderTxs.getColumnFamilyHandle(), address.getBytes(), false);
            }
        }

        if (recipient != null) {
            if (type != 0) {
                //recipientKeys = Fun.filter(this.addressTypeKey, new Tuple2<String, Integer>(recipient, type)).iterator();
                senderKeys = ((DBRocksDBTable) map).getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(), Arrays.concatenate(recipient.getBytes(), Ints.toByteArray(type))
                        , false);
            } else {
                recipientKeys = ((DBRocksDBTable) map).getIndexIteratorFilter(recipientTxs.getColumnFamilyHandle(), address.getBytes(), false);
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

}
