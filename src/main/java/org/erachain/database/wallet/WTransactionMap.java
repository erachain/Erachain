package org.erachain.database.wallet;

import org.erachain.core.account.Account;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.IndexIterator;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DCUMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;

/**
 * Ключ такой нужен для того чтобы сюда же заносить и неподтвержденные транзакции - уникальный по Время + Счет(Хэш)
 * Транзакции относящиеся к моим счетам. Сюда же записываться должны и неподтвержденные<br>
 * А когда они подтверждаются они будут перезаписываться поверх.
 * Тогда неподтвержденные будут показывать что они не исполнились.
 * И их пользователь сможет сам удалить вручную или командой - удалить все неподтвержденные.
 * <hr>
 * Ключ: время создания + первых 4 байта счета - так по времени сортируем. Вторичный ключ по первых 4 байта счета + первичный ключ - отображения по счетам.
 * Причем счет ищем как Involved - то есть и входящие тоже будут браться<br>
 * Значение: транзакция
 */
public class WTransactionMap extends DCUMapImpl<Tuple2<Long, Integer>, Transaction> {

    //public static final int TIMESTAMP_INDEX = 1;
    //public static final int ADDRESS_INDEX = 2;
    //public static final int AMOUNT_INDEX = 3;

    /**
     * Поиск по типу транзакции
     */
    NavigableSet<Tuple2<Byte, Tuple2<Long, Integer>>> typeKey;
    /**
     * Поиск по данному счету с сортировкой по времени
     */
    NavigableSet<Tuple2<Tuple2<Integer, Long>, Tuple2<Long, Integer>>> addressAssetKey;

    /**
     * Поиск по данному счету с сортировкой по Типу
     */
    NavigableSet<Tuple2<Tuple2<Integer, Byte>, Tuple2<Long, Integer>>> addressTypeKey;

    NavigableSet<Tuple2<Integer, Tuple2<Long, Integer>>> addressKey;

    /**
     * Те записи которые не были просмотрены
     */
    NavigableSet<Tuple2<Long, Integer>> unViewed;

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

        this.typeKey = database.createTreeSet("type_txs").comparator(Fun.COMPARATOR)
                .makeOrGet();
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.typeKey,
                new Fun.Function2<Byte, Tuple2<Long, Integer>, Transaction>() {
                    @Override
                    public Byte run(Tuple2<Long, Integer> key, Transaction value) {
                        return (byte) value.getType();
                    }
                });

        this.addressKey = database.createTreeSet("address_txs").comparator(Fun.COMPARATOR)
                .makeOrGet();
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.addressKey,
                new Fun.Function2<Integer, Tuple2<Long, Integer>, Transaction>() {
                    @Override
                    public Integer run(Tuple2<Long, Integer> key, Transaction value) {
                        return key.b.hashCode();
                    }
                });

        this.addressAssetKey = database.createTreeSet("address_asset_txs").comparator(Fun.TUPLE2_COMPARATOR)
                .makeOrGet();
        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.addressAssetKey,
                new Fun.Function2<Tuple2<Integer, Long>[], Tuple2<Long, Integer>, Transaction>() {
                    @Override
                    public Tuple2<Integer, Long>[] run(Tuple2<Long, Integer> key, Transaction value) {
                        value.setDC((DCSet) databaseSet, true);
                        Object[][] itemKeys = value.getItemsKeys();
                        if (itemKeys == null)
                            return null;

                        Tuple2<Integer, Long>[] keys = new Tuple2[itemKeys.length];
                        for (int i = 0; i < keys.length; i++) {
                            if (((int) itemKeys[i][0]) == ItemCls.ASSET_TYPE) {
                                keys[i] = new Tuple2<Integer, Long>()
                            }
                        }
                        return keys;
                    }
                });

        this.addressTypeKey = database.createTreeSet("address_type_txs").comparator(Fun.TUPLE2_COMPARATOR)
                .makeOrGet();
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.addressTypeKey,
                new Fun.Function2<Tuple2<Integer, Byte>, Tuple2<Long, Integer>, Transaction>() {
                    @Override
                    public Tuple2<Integer, Byte> run(Tuple2<Long, Integer> key, Transaction value) {
                        return new Tuple2<>(key.b.hashCode(), (byte) ((int) value.getType()));
                    }
                });

        this.unViewed = database.createTreeSet("un_viewed_txs").comparator(Fun.TUPLE2_COMPARATOR)
                .makeOrGet();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> get(Account account, int limit) {
        List<Transaction> transactions = new ArrayList<Transaction>();

        try {
            //GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
            Iterator<Tuple2<Long, Integer>> iterator = ((NavigableSet) this.addressKey).subSet(
                    Fun.t2(account.hashCode(), null),
                    Fun.t2(account.hashCode(), Fun.HI())).iterator();

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
    public Iterator<Tuple2<Long, Integer>> getUndeadIterator(boolean descending) {

        if (descending) {
            return unViewed.descendingSet().iterator();
        } else {
            return unViewed.iterator();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Tuple2<Long, Integer>> getTypeIterator(Byte type, boolean descending) {

        if (descending) {
            return IteratorCloseableImpl.make(new IndexIterator((NavigableSet) typeKey.descendingSet().subSet(
                    Fun.t2(type, Fun.HI()),
                    Fun.t2(type, null))));
        } else {
            return IteratorCloseableImpl.make(new IndexIterator((NavigableSet) typeKey.subSet(
                    Fun.t2(type, null),
                    Fun.t2(type, Fun.HI()))));
        }

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Tuple2<Long, Integer>> getAddressIterator(Account account, boolean descending) {

        if (account == null) {
            if (descending)
                return getDescendingIterator();
            return getIterator();
        }

        if (descending)
            return IteratorCloseableImpl.make(new IndexIterator((NavigableSet) this.addressKey.descendingSet().subSet(
                    Fun.t2(account.hashCode(), Fun.HI()),
                    Fun.t2(account.hashCode(), null))));

        return IteratorCloseableImpl.make(new IndexIterator((NavigableSet) this.addressKey.subSet(
                Fun.t2(account.hashCode(), null),
                Fun.t2(account.hashCode(), Fun.HI()))));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Tuple2<Long, Integer>> getAddressAssetIterator(Account account, Long assetKey, boolean descending) {

        if (assetKey == null || account == null)
            return getAddressIterator(account, descending);

        if (descending)
            return IteratorCloseableImpl.make(new IndexIterator((NavigableSet) this.addressAssetKey.descendingSet().subSet(
                    Fun.t2(Fun.t2(account == null ? null : account.hashCode(),
                            assetKey == null ? Long.MAX_VALUE : assetKey), Fun.HI()),
                    Fun.t2(Fun.t2(account == null ? null : account.hashCode(), assetKey), null))));

        return IteratorCloseableImpl.make(new IndexIterator((NavigableSet) this.addressAssetKey.subSet(
                Fun.t2(Fun.t2(account == null ? null : account.hashCode(), assetKey), null),
                Fun.t2(Fun.t2(account == null ? null : account.hashCode(),
                        assetKey == null ? Long.MAX_VALUE : assetKey), Fun.HI()))));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Tuple2<Long, Integer>> getAddressTypeIterator(Account account, Integer typeInt, boolean descending) {

        if (typeInt == null || account == null)
            return getAddressIterator(account, descending);

        Byte type = (byte) (int) typeInt;
        if (descending)
            return IteratorCloseableImpl.make(new IndexIterator((NavigableSet) this.addressTypeKey.descendingSet().subSet(
                    Fun.t2(Fun.t2(account == null ? null : account.hashCode(),
                            type == null ? Byte.MAX_VALUE : type), Fun.HI()),
                    Fun.t2(Fun.t2(account == null ? null : account.hashCode(), type), null))));

        return IteratorCloseableImpl.make(new IndexIterator((NavigableSet) this.addressTypeKey.subSet(
                Fun.t2(Fun.t2(account == null ? null : account.hashCode(), type), null),
                Fun.t2(Fun.t2(account == null ? null : account.hashCode(),
                        type == null ? Byte.MAX_VALUE : type), Fun.HI()))));
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

    public boolean isUnViewed(Transaction transaction) {
        if (transaction.getCreator() == null)
            return false;
        return unViewed.contains(new Tuple2<Long, Integer>(transaction.getTimestamp(), transaction.getCreator().hashCode()));
    }

    public void clearUnViewed(Transaction transaction) {
        if (transaction.getCreator() != null)
            unViewed.remove(new Tuple2<Long, Integer>(transaction.getTimestamp(), transaction.getCreator().hashCode()));
    }

    public void clearUnViewed() {
        unViewed.clear();
    }

    public boolean set(Tuple2<Long, Integer> key, Transaction transaction) {
        if (transaction.getCreator() != null)
            unViewed.add(new Tuple2<Long, Integer>(transaction.getTimestamp(), transaction.getCreator().hashCode()));

        return super.set(key, transaction);
    }

    public boolean set(Account account, Transaction transaction) {
        return this.set(new Tuple2<Long, Integer>(transaction.getTimestamp(), account.hashCode()), transaction);
    }

    public void put(Tuple2<Long, Integer> key, Transaction transaction) {
        super.put(key, transaction);
    }

    public void put(Account account, Transaction transaction) {
        this.put(new Tuple2<Long, Integer>(transaction.getTimestamp(), account.hashCode()), transaction);
    }

    public void delete(Tuple2<Long, Integer> key) {
        this.remove(key);
    }

    public void delete(Account account, Transaction transaction) {
        this.delete(new Tuple2<Long, Integer>(transaction.getTimestamp(), account.hashCode()));
    }

    public void delete(Transaction transaction) {
        this.delete(transaction.getCreator(), transaction);
    }

    public Transaction remove(Tuple2<Long, Integer> key) {
        Transaction transaction = super.remove(key);
        if (transaction != null && transaction.getCreator() != null)
            unViewed.remove(new Tuple2<Long, Integer>(transaction.getTimestamp(), transaction.getCreator().hashCode()));
        return transaction;
    }

}
