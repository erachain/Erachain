package org.erachain.database.wallet;

import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.IndexIterator;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DCUMapImpl;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Транзакции относящиеся к моим счетам. Сюда же записываться должны и неподтвержденные<br>
 * А когда они подтверждаются они будут перезаписываться поверх.
 * Тогда неподтвержденные будут показывать что они не исполнились.
 * И их пользователь сможет сам удалить вручную или командой - удалить все неподтвержденные.
 * <hr>
 * Ключ: время создания + первых 8 байт счета - так по времени сортируем. Вторичный ключ по первых 8 байт счета + время создания - отображения по счетам.
 * Причем счет ищем как Involved - то есть и входящие тоже будут браться<br>
 * Значение: транзакция
 */
public class WTransactionMap extends DCUMapImpl<Tuple2<Long, Long>, Transaction> {

    //public static final int TIMESTAMP_INDEX = 1;
    //public static final int ADDRESS_INDEX = 2;
    //public static final int AMOUNT_INDEX = 3;

    NavigableSet typeKey;
    NavigableSet addressKey;

    static Logger LOGGER = LoggerFactory.getLogger(WTransactionMap.class.getName());

    public WTransactionMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.WALLET_RESET_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.WALLET_LIST_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.WALLET_ADD_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE);
        }

    }

    @Override
    public void openMap() {
        //OPEN MAP

        this.map = database.createTreeMap("transactions")
                .keySerializer(BTreeKeySerializer.TUPLE2)
                .valueSerializer(new TransactionSerializer())
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {

        /*

        //TIMESTAMP INDEX
        NavigableSet<Long> timestampIndex = database.createTreeSet("transactions_index_timestamp")
                .makeOrGet();

        NavigableSet<Long> descendingTimestampIndex = database.createTreeSet("transactions_index_timestamp_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(TIMESTAMP_INDEX, timestampIndex, descendingTimestampIndex, new Fun.Function2<Long, Tuple2<Long, Long>, Transaction>() {
            @Override
            public Long run(Tuple2<Long, Long> key, Transaction value) {
                return value.getTimestamp();
            }
        });

        //ADDRESS INDEX
        NavigableSet<Tuple2<String[], Tuple2<Long, Long>>> addressIndex = database.createTreeSet("transactions_index_address")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<String[], Tuple2<Long, Long>>> descendingAddressIndex = database.createTreeSet("transactions_index_address_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndexes(ADDRESS_INDEX, addressIndex, descendingAddressIndex, new Fun.Function2<String[], Tuple2<Long, Long>, Tuple2<Long, Transaction>>() {
            @Override
            public String[] run(Tuple2<Long, Long> key, Tuple2<Long, Transaction> value) {
                HashSet<Account> involved = value.b.getInvolvedAccounts();
                String[] keys = new String[involved.size()];
                Iterator<Account> keysIterator = involved.iterator();
                for (int i = 0; i < involved.size(); i++) {
                    keys[i] =keysIterator.next().getAddress();
                }
                return keys;
            }
        });
         */

        /* это вообще не информативнй индекс не нужен
        //AMOUNT INDEX
        NavigableSet<Tuple2<BigDecimal, Tuple2<Long, Long>>> amountIndex = database.createTreeSet("transactions_index_amount")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<BigDecimal, Tuple2<Long, Long>>> descendingAmountIndex = database.createTreeSet("transactions_index_amount_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(AMOUNT_INDEX, amountIndex, descendingAmountIndex, new Fun.Function2<BigDecimal, Tuple2<Long, Long>, Tuple2<Long, Transaction>>() {
            @Override
            public BigDecimal run(Tuple2<Long, Long> key, Tuple2<Long, Transaction> value) {
                Account account = new Account(key.a);
                return value.b.getAmount(account);
            }
        });
        */

        this.typeKey = database.createTreeSet("type_txs").comparator(Fun.TUPLE2_COMPARATOR)
                .makeOrGet();
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.typeKey,
                new Fun.Function2<Tuple2<Byte, Long>, Tuple2<Long, Long>, Transaction>() {
                    @Override
                    public Tuple2<Byte, Long> run(Tuple2<Long, Long> key, Transaction value) {
                        return new Tuple2<>((byte) value.getType(), key);
                    }
                });


        this.addressKey = database.createTreeSet("address_txs").comparator(Fun.TUPLE2_COMPARATOR)
                .makeOrGet();
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.addressKey,
                new Fun.Function2<Tuple2<Long, Long>, Long, Transaction>() {
                    @Override
                    public Tuple2<Long, Long> run(Long key, Transaction value) {
                        Account creator = value.getCreator();
                        Long addressKey = creator == null ? 0L : Longs.fromByteArray(value.getCreator().getShortAddressBytes());
                        return new Tuple2<>(addressKey, key);
                    }
                });

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> get(Account account, int limit) {
        List<Transaction> transactions = new ArrayList<Transaction>();

        try {
            //GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
            Set<Long> accountTransactions = ((NavigableSet) this.addressKey).subSet(
                    Fun.t2(Longs.fromByteArray(account.getShortAddressBytes()), null),
                    Fun.t2(Longs.fromByteArray(account.getShortAddressBytes()), Fun.HI()));

            //GET ITERATOR
            //Iterator<Long> iterator = accountTransactions.iterator();
            Iterator<Long> iterator = null;

            //RETURN {LIMIT} TRANSACTIONS
            int counter = 0;
            Transaction item;
            while (iterator.hasNext() && counter < limit) {
                item = this.get(iterator.next());
                transactions.add(item);
                counter++;
            }
        } catch (Exception e) {
            //ERROR
            LOGGER.error(e.getMessage(), e);
        }

        return transactions;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getTypeIterator(Byte type, boolean descending) {

        if (descending) {
            return new IndexIterator((NavigableSet) typeKey.descendingSet().subSet(
                    Fun.t2(new Tuple2(type, Fun.HI()), Fun.HI()),
                    Fun.t2(new Tuple2(type, null), null)));
        } else {
            return new IndexIterator((NavigableSet) typeKey.subSet(
                    Fun.t2(new Tuple2(type, null), null),
                    Fun.t2(new Tuple2(type, Fun.HI()), Fun.HI())));
        }

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getAddressIterator(Account account) {

        return new IndexIterator((NavigableSet) this.addressKey.subSet(
                Fun.t2(Fun.t2(Longs.fromByteArray(account.getShortAddressBytes()), null), null),
                Fun.t2(Fun.t2(Longs.fromByteArray(account.getShortAddressBytes()), Fun.HI()), Fun.HI())));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getAddressDescendingIterator(Account account) {

        return new IndexIterator((NavigableSet) this.addressKey.descendingSet().subSet(
                Fun.t2(Fun.t2(Longs.fromByteArray(account.getShortAddressBytes()), Fun.HI()), Fun.HI()),
                Fun.t2(Fun.t2(Longs.fromByteArray(account.getShortAddressBytes()), null), null)));

    }

    public List<Pair<Account, Transaction>> get(List<Account> accounts, int limit) {
        List<Pair<Account, Transaction>> transactions = new ArrayList<Pair<Account, Transaction>>();

        try {
            //FOR EACH ACCOUNTS
            synchronized (accounts) {
                for (Account account : accounts) {
                    List<Transaction> accountTransactions = get(account, limit);
                    for (Transaction transaction: accountTransactions) {
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
}
