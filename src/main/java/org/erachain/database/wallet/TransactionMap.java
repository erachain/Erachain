package org.erachain.database.wallet;
//09/03

import com.google.common.collect.Iterables;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.erachain.utils.ReverseComparator;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * Транзакции относящиеся к моим счетам. Сюда же записываться должны и неподтвержденные<br>
 * А когда они подтверждаются они будут перезаписываться поверх.
 * Тогда неподтвержденные будут показывать что они не сиполнились.
 * И их пользователь сможет сам удалить вручную или командой - удалить все неподтвержденные
 * <hr>
 * Ключ: счет + подпись<br>
 * Значение: транзакция
 */
public class TransactionMap extends DBMap<Tuple2<String, String>, Transaction> {

    BTreeMap AUTOKEY_INDEX;
    private Atomic.Long atomicKey;

    static final int KEY_LENGHT = 12;
    public static final int TIMESTAMP_INDEX = 1;
    public static final int ADDRESS_INDEX = 2;
    public static final int AMOUNT_INDEX = 3;

    static Logger LOGGER = LoggerFactory.getLogger(TransactionMap.class.getName());

    public TransactionMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        this.atomicKey = database.getAtomicLong("TransactionMap_atomicKey");

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.WALLET_RESET_TRANSACTION_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.WALLET_LIST_TRANSACTION_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.WALLET_ADD_TRANSACTION_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes(DB database) {
        //TIMESTAMP INDEX
        NavigableSet<Tuple2<Long, Tuple2<String, String>>> timestampIndex = database.createTreeSet("transactions_index_timestamp")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<Long, Tuple2<String, String>>> descendingTimestampIndex = database.createTreeSet("transactions_index_timestamp_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(TIMESTAMP_INDEX, timestampIndex, descendingTimestampIndex, new Fun.Function2<Long, Tuple2<String, String>, Transaction>() {
            @Override
            public Long run(Tuple2<String, String> key, Transaction value) {
                return value.getTimestamp();
            }
        });

        //ADDRESS INDEX
        NavigableSet<Tuple2<String, Tuple2<String, String>>> addressIndex = database.createTreeSet("transactions_index_address")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingAddressIndex = database.createTreeSet("transactions_index_address_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(ADDRESS_INDEX, addressIndex, descendingAddressIndex, new Fun.Function2<String, Tuple2<String, String>, Transaction>() {
            @Override
            public String run(Tuple2<String, String> key, Transaction value) {
                return key.a;
            }
        });

        //AMOUNT INDEX
        NavigableSet<Tuple2<BigDecimal, Tuple2<String, String>>> amountIndex = database.createTreeSet("transactions_index_amount")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<BigDecimal, Tuple2<String, String>>> descendingAmountIndex = database.createTreeSet("transactions_index_amount_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(AMOUNT_INDEX, amountIndex, descendingAmountIndex, new Fun.Function2<BigDecimal, Tuple2<String, String>, Transaction>() {
            @Override
            public BigDecimal run(Tuple2<String, String> key, Transaction value) {
                Account account = new Account(key.a);
                return value.getAmount(account);
            }
        });

    }

    @Override
    protected Map<Tuple2<String, String>, Transaction> getMap(DB database) {
        //OPEN MAP
        BTreeMap map = database.createTreeMap("transactions")
                .keySerializer(BTreeKeySerializer.TUPLE2)
                .valueSerializer(new TransactionSerializer())
                .counterEnable()
                .makeOrGet();

        this.AUTOKEY_INDEX = database.createTreeMap("dw_transactions_AUTOKEY_INDEX")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        //BIND
        Bind.secondaryKey(map, this.AUTOKEY_INDEX, new Fun.Function2<Long, Tuple2<String, String>, Transaction>() {
            @Override
            public Long run(Tuple2<String, String> key, Transaction value) {
                return -atomicKey.get();
            }
        });

        return map;
    }

    @Override
    protected Map<Tuple2<String, String>, Transaction> getMemoryMap() {
        return new TreeMap<Tuple2<String, String>, Transaction>(Fun.TUPLE2_COMPARATOR);
    }

    @Override
    protected Transaction getDefaultValue() {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> get(Account account, int limit) {
        List<Transaction> transactions = new ArrayList<Transaction>();

        try {
            //GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
			/*Map<Tuple2<String, String>, Transaction> accountTransactions = ((BTreeMap) this.map).subMap(
					Fun.t2(null, account.getAddress()),
					Fun.t2(Fun.HI(), account.getAddress()));*/

            Map<Tuple2<String, String>, Transaction> accountTransactions = ((BTreeMap) this.map).subMap(
                    Fun.t2(account.getAddress(), null),
                    Fun.t2(account.getAddress(), Fun.HI()));

            //GET ITERATOR
            Iterator<Transaction> iterator = accountTransactions.values().iterator();

            //RETURN {LIMIT} TRANSACTIONS
            int counter = 0;
            while (iterator.hasNext() && counter < limit) {
                transactions.add(iterator.next());
                counter++;
            }
        } catch (Exception e) {
            //ERROR
            LOGGER.error(e.getMessage(), e);
        }

        return transactions;
    }

    public List<Pair<Account, Transaction>> get(List<Account> accounts, int limit) {
        List<Pair<Account, Transaction>> transactions = new ArrayList<Pair<Account, Transaction>>();

        try {
            //FOR EACH ACCOUNTS
            synchronized (accounts) {
                for (Account account : accounts) {
                    List<Transaction> accountTransactions = get(account, limit);
                    for (Transaction transaction : accountTransactions) {
                        transactions.add(new Pair<Account, Transaction>(account, transaction));
                    }
                }
            }
        } catch (Exception e) {
            //ERROR
            LOGGER.error(e.getMessage(), e);
        }

        return transactions;
    }

    public Collection<Tuple2<String, String>> getFromToKeys(long fromKey, long toKey) {

        if (true) {
            // РАБОТАЕТ намного БЫСТРЕЕ
            return AUTOKEY_INDEX.subMap(fromKey, toKey).values();
        } else {

            // перебор по NEXT очень медленный
            List<Tuple2<String, String>> treeKeys = new ArrayList<Tuple2<String, String>>();

            // DESCENDING + 1000
            Iterable iterable = this.indexes.get(TIMESTAMP_INDEX + DESCENDING_SHIFT_INDEX);
            Iterable iterableLimit = Iterables.limit(Iterables.skip(iterable, (int) fromKey), (int) (toKey - fromKey));

            Iterator<Tuple2<Long, Tuple2<String, String>>> iterator = iterableLimit.iterator();
            while (iterator.hasNext()) {
                treeKeys.add(iterator.next().b);
            }

            return treeKeys;
        }

    }

    public Transaction delete(Tuple2<String, String> key) {
        if (this.atomicKey != null) {
            this.atomicKey.decrementAndGet();
        }
        return super.delete(key);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Account account) {
        //GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        Map<Tuple2<String, String>, Transaction> accountTransactions = ((BTreeMap) this.map).subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI()));

        //DELETE TRANSACTIONS
        for (Tuple2<String, String> key : accountTransactions.keySet()) {
            this.delete(key);
        }
    }

    public void delete(Account account, Transaction transaction) {
        this.delete(new Tuple2<String, String>(account.getAddress(), new String(transaction.getSignature()).substring(KEY_LENGHT)));
    }

    public void deleteAll(List<Account> accounts) {
        for (Account account : accounts) {
            this.delete(account);
        }
    }

    public boolean set(Tuple2<String, String> key, Transaction transaction) {
        if (this.atomicKey != null) {
            this.atomicKey.incrementAndGet();
        }
        return super.set(key, transaction);
    }

    public boolean add(Account account, Transaction transaction) {
        return this.set(new Tuple2<String, String>(account.getAddress(), new String(transaction.getSignature()).substring(KEY_LENGHT)), transaction);
    }

    public void addAll(Map<Account, List<Transaction>> transactions) {
        //FOR EACH ACCOUNT
        for (Account account : transactions.keySet()) {
            //FOR EACH TRANSACTION
            for (Transaction transaction : transactions.get(account)) {
                this.add(account, transaction);
            }
        }
    }
}
