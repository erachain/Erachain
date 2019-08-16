package org.erachain.datachain;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBMap;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.ReverseComparator;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;

import java.lang.reflect.Array;
import java.util.*;

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
public class TransactionMap extends DCMap<Long, Transaction> implements Observer {
    public static final int TIMESTAMP_INDEX = 1;

    //private static final Logger LOGGER = LoggerFactory.getLogger(TransactionMap.class);

    @SuppressWarnings("rawtypes")
    private NavigableSet senderKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet recipientKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet typeKey;

    public TransactionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_UNC_TRANSACTION_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_UNC_TRANSACTION_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_UNC_TRANSACTION_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE);
        }

    }

    public TransactionMap(TransactionMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes(DB database) {

        //////////// HERE PROTOCOL INDEX - for GENERATE BLOCL

        // TIMESTAMP INDEX
        Tuple2Comparator<Long, Long> comparator = new Fun.Tuple2Comparator<Long, Long>(Fun.COMPARATOR,
                //UnsignedBytes.lexicographicalComparator()
                Fun.COMPARATOR);
        NavigableSet<Tuple2<Long, Long>> heightIndex = database
                .createTreeSet("transactions_index_timestamp")
                .comparator(comparator)
                .makeOrGet();

        NavigableSet<Tuple2<Long, Long>> descendingHeightIndex = database
                .createTreeSet("transactions_index_timestamp_descending")
                .comparator(new ReverseComparator(comparator))
                .makeOrGet();

        createIndex(TIMESTAMP_INDEX, heightIndex, descendingHeightIndex,
                new Fun.Function2<Long, Long, Transaction>() {
                    @Override
                    public Long run(Long key, Transaction value) {
                        return value.getTimestamp();
                    }
                });

    }

    public Integer deleteObservableData(int index) {
        return this.observableData.remove(index);
    }

    public Integer setObservableData(int index, Integer data) {
        return this.observableData.put(index, data);
    }

    @Override
    protected Map<Long, Transaction> getMap(DB database) {

        // OPEN MAP
        BTreeMap<Long, Transaction> map = database.createTreeMap("transactions")
                .keySerializer(BTreeKeySerializer.BASIC)
                .valueSerializer(new TransactionSerializer())
                .counterEnable()
                .makeOrGet();


        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return map;

        this.senderKey = database.createTreeSet("sender_unc_txs").comparator(Fun.COMPARATOR).makeOrGet();
        Bind.secondaryKey(map, this.senderKey, new Fun.Function2<Tuple2<String, Long>, Long, Transaction>() {
            @Override
            public Tuple2<String, Long> run(Long key, Transaction val) {
                Account account = val.getCreator();
                return new Tuple2<String, Long>(account == null ? "genesis" : account.getAddress(), val.getTimestamp());
            }
        });

        this.recipientKey = database.createTreeSet("recipient_unc_txs").comparator(Fun.COMPARATOR).makeOrGet();
        Bind.secondaryKeys(map, this.recipientKey,
                new Fun.Function2<String[], Long, Transaction>() {
                    @Override
                    public String[] run(Long key, Transaction val) {
                        List<String> recps = new ArrayList<String>();

                        val.setDC((DCSet)databaseSet);

                        for (Account acc : val.getRecipientAccounts()) {
                            // recps.add(acc.getAddress() + val.viewTimestamp()); уникальнось внутри Бинда делается
                            recps.add(acc.getAddress());
                        }
                        String[] ret = new String[recps.size()];
                        ret = recps.toArray(ret);
                        return ret;
                    }
                });

        this.typeKey = database.createTreeSet("address_type_unc_txs").comparator(Fun.COMPARATOR).makeOrGet();
        Bind.secondaryKeys(map, this.typeKey,
                new Fun.Function2<Fun.Tuple3<String, Long, Integer>[], Long, Transaction>() {
                    @Override
                    public Fun.Tuple3<String, Long, Integer>[] run(Long key, Transaction val) {
                        List<Fun.Tuple3<String, Long, Integer>> recps = new ArrayList<Fun.Tuple3<String, Long, Integer>>();
                        Integer type = val.getType();

                        val.setDC((DCSet)databaseSet);

                        for (Account acc : val.getInvolvedAccounts()) {
                            recps.add(new Fun.Tuple3<String, Long, Integer>(acc.getAddress(), val.getTimestamp(), type));

                        }
                        // Tuple2<Integer, String>[] ret = (Tuple2<Integer,
                        // String>[]) new Object[ recps.size() ];
                        Fun.Tuple3<String, Long, Integer>[] ret = (Fun.Tuple3<String, Long, Integer>[])
                                Array.newInstance(Fun.Tuple3.class, recps.size());

                        ret = recps.toArray(ret);

                        return ret;
                    }
                });

        return map;
    }

    @Override
    protected Map<Long, Transaction> getMemoryMap() {
        return new TreeMap<Long, Transaction>(
                //UnsignedBytes.lexicographicalComparator()
        );
    }

    @Override
    protected Transaction getDefaultValue() {
        return null;
    }

    public Iterator<Long> getTimestampIterator() {

        Iterator<Long> iterator = this.getIterator(TIMESTAMP_INDEX, false);
        return iterator;
    }

    /**
     * Используется для получения транзакций для сборки блока
     * Поидее нужно братьв се что есть без учета времени протухания для сборки блока своего
     * @param timestamp
     * @param notSetDCSet
     * @param cutDeadTime true is need filter by Dead Time
     * @return
     */
    public List<Transaction> getSubSet(long timestamp, boolean notSetDCSet, boolean cutDeadTime) {

        List<Transaction> values = new ArrayList<Transaction>();
        Iterator<Long> iterator = this.getIterator(TIMESTAMP_INDEX, false);
        Transaction transaction;
        int count = 0;
        int bytesTotal = 0;
        Long key;
        while (iterator.hasNext()) {
            key = iterator.next();
            transaction = this.map.get(key);

            if (cutDeadTime && transaction.getDeadline() < timestamp)
                continue;
            if (transaction.getTimestamp() > timestamp)
                // мы используем отсортированный индекс, поэтому можно обрывать
                break;

            if (++count > BlockChain.MAX_BLOCK_SIZE_GEN)
                break;

            bytesTotal += transaction.getDataLength(Transaction.FOR_NETWORK, true);
            if (bytesTotal > BlockChain.MAX_BLOCK_SIZE_BYTES_GEN
                ///+ (BlockChain.MAX_BLOCK_SIZE_BYTE >> 3)
            ) {
                break;
            }

            if (!notSetDCSet)
                transaction.setDC((DCSet)databaseSet);

            values.add(transaction);

        }

        return values;
    }

    private static long MAX_DEADTIME = 1000 * 60 * 60 * 24;

    /**
     * очищает  только по признаку протухания и ограничения на размер списка - без учета валидности
     * С учетом валидности очистка идет в Генераторе после каждого запоминания блока
     * @param timestamp
     * @param cutDeadTime
     */
    protected long pointReset;
    public void clearByDeadTimeAndLimit(long timestamp, boolean cutDeadTime) {

        long realTime = System.currentTimeMillis();
        int count = 0;
        long tickerIter = System.currentTimeMillis() - realTime;

        timestamp -= (BlockChain.GENERATING_MIN_BLOCK_TIME_MS << 1) + BlockChain.GENERATING_MIN_BLOCK_TIME_MS << (5 - Controller.HARD_WORK >> 1);

        if (cutDeadTime) {

            timestamp -= BlockChain.GENERATING_MIN_BLOCK_TIME_MS;
            tickerIter = System.currentTimeMillis();
            SortedSet<Tuple2<?, Long>> subSet = this.indexes.get(TIMESTAMP_INDEX).headSet(new Tuple2<Long, Long>(
                    timestamp, null));
            tickerIter = System.currentTimeMillis() - tickerIter;
            if (tickerIter > 10) {
                LOGGER.debug("TAKE headSet: " + tickerIter + " ms subSet.size: " + subSet.size());
            }

            for (Tuple2<?, Long> key : subSet) {
                if (this.contains(key.b))
                    this.delete(key.b);
            }

        } else {
            /**
             * по несколько секунд итератор берется - при том что таблица пустая -
             * - дале COMPACT не помогает
             */
            //Iterator<Long> iterator = this.getIterator(TIMESTAMP_INDEX, false);
            Iterator<Tuple2<?, Long>> iterator = this.indexes.get(TIMESTAMP_INDEX).iterator();
            if (tickerIter > 10) {
                LOGGER.debug("TAKE ITERATOR: " + tickerIter + " ms");
            }

            Transaction transaction;

            while (iterator.hasNext()) {
                Long key = iterator.next().b;
                transaction = this.map.get(key);
                if (transaction == null) {
                    // такая ошибка уже было
                    return;
                }

                long deadline = transaction.getDeadline();
                if (realTime - deadline > 86400000 // позде на день удаляем в любом случае
                        || ((Controller.HARD_WORK > 3
                        || cutDeadTime)
                        && deadline < timestamp)
                        || Controller.HARD_WORK <= 3
                        && deadline + MAX_DEADTIME < timestamp // через сутки удалять в любом случае
                        || this.size() > BlockChain.MAX_UNCONFIGMED_MAP_SIZE) {
                    this.delete(key);
                    count++;
                } else {
                    break;
                }
            }
        }

        long ticker = System.currentTimeMillis() - realTime;
        if ( ticker > 1000 || count > 0) {
            LOGGER.debug("CLEAR dead UTXs: " + ticker + " ms, for deleted: " + count);
        }

    }

    @Override
    public void update(Observable o, Object arg) {

    }

    public boolean set(byte[] signature, Transaction transaction) {

        Long key = Longs.fromByteArray(signature);

        return this.set(key, transaction);

    }

    public boolean add(Transaction transaction) {

        return this.set(transaction.getSignature(), transaction);

    }

    public void delete(Transaction transaction) {
        this.delete(transaction.getSignature());
    }

    public Transaction delete(byte[] signature) {
        return this.delete(Longs.fromByteArray(signature));
    }


    public long totalDeleted = 0;
    public Transaction delete(Long key) {
        Transaction transaction = super.delete(key);
        if (transaction != null) {
            // DELETE only if DELETED
            totalDeleted++;
        }

        return transaction;

    }

    public boolean contains(byte[] signature) {
        return this.contains(Longs.fromByteArray(signature));
    }

    public boolean contains(Transaction transaction) {
        return this.contains(transaction.getSignature());
    }

    public Transaction get(byte[] signature) {
        return this.get(Longs.fromByteArray(signature));
    }

    public Collection<Long> getFromToKeys(long fromKey, long toKey) {

        List<Long> treeKeys = new ArrayList<Long>();

        //NavigableMap set = new NavigableMap<Long, Transaction>();
        // NodeIterator


        // DESCENDING + 1000
        Iterable iterable = this.indexes.get(TIMESTAMP_INDEX + DESCENDING_SHIFT_INDEX);
        Iterable iterableLimit = Iterables.limit(Iterables.skip(iterable, (int) fromKey), (int) (toKey - fromKey));

        Iterator<Tuple2<Long, Long>> iterator = iterableLimit.iterator();
        while (iterator.hasNext()) {
            treeKeys.add(iterator.next().b);
        }

        return treeKeys;

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
        if (sender != null) {
            if (type > 0)
                senderKeys = Fun.filter(this.typeKey, new Fun.Tuple3<String, Long, Integer>(sender, timestamp, type));
            else
                senderKeys = Fun.filter(this.senderKey, sender);
        }

        if (recipient != null) {
            if (type > 0)
                recipientKeys = Fun.filter(this.typeKey, new Fun.Tuple3<String, Long, Integer>(recipient, timestamp, type));
            else
                recipientKeys = Fun.filter(this.recipientKey, recipient);
        }

        if (address != null) {
            treeKeys.addAll(Sets.newTreeSet(senderKeys));
            treeKeys.addAll(Sets.newTreeSet(recipientKeys));
        } else if (sender != null && recipient != null) {
            treeKeys.addAll(Sets.newTreeSet(senderKeys));
            treeKeys.retainAll(Sets.newTreeSet(recipientKeys));
        } else if (sender != null) {
            treeKeys.addAll(Sets.newTreeSet(senderKeys));
        } else if (recipient != null) {
            treeKeys.addAll(Sets.newTreeSet(recipientKeys));
        }

        Iterable keys;
        if (desc) {
            keys = ((TreeSet) treeKeys).descendingSet();
        } else {
            keys = treeKeys;
        }

        limit = (limit == 0) ? Iterables.size(keys) : limit;

        // зачем это тут ?
        //// Iterable k = Iterables.limit(Iterables.skip(keys, offset), limit);
        //// getUnconfirmedTransaction(k);
        //// return Iterables.limit(Iterables.skip(keys, offset), limit);

        return Iterables.limit(Iterables.skip(keys, offset), limit);

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
            item = this.map.get(key);
            transactions.add(item);
        }
        return transactions;
    }

    // TODO выдает ошибку на шаге treeKeys.addAll(Sets.newTreeSet(senderKeys));
    public List<Transaction> getTransactionsByAddressFast100(String address) {

        Iterable senderKeys = null;
        Iterable recipientKeys = null;
        TreeSet<Object> treeKeys = new TreeSet<>();

        senderKeys = Iterables.limit(Fun.filter(this.senderKey, address), 100);
        recipientKeys = Iterables.limit(Fun.filter(this.recipientKey, address), 100);

        treeKeys.addAll(Sets.newTreeSet(senderKeys));
        treeKeys.addAll(Sets.newTreeSet(recipientKeys));

        return getUnconfirmedTransaction(Iterables.limit(treeKeys, 100));

    }

    // slow?? without index
    public List<Transaction> getTransactionsByAddress(String address) {

        ArrayList<Transaction> values = new ArrayList<Transaction>();
        Iterator<Long> iterator = this.getIterator(0, false);
        Account account = new Account(address);

        Transaction transaction;
        boolean ok = false;

        int i = 0;
        while (iterator.hasNext()) {

            transaction = map.get(iterator.next());
            if (transaction.getCreator().equals(address))
                ok = true;
            else
                ok = false;

            if (!ok) {
                transaction.setDC((DCSet)databaseSet);
                HashSet<Account> recipients = transaction.getRecipientAccounts();

                if (recipients == null || recipients.isEmpty() || !recipients.contains(account)) {
                    continue;
                }

            }

            // SET LIMIT
            if (++i > 100)
                break;

            values.add(transaction);

        }
        return values;
    }

    public List<Transaction> getTransactions(int indexID, int count, boolean descending) {

        ArrayList<Transaction> values = new ArrayList<Transaction>();

        //LOGGER.debug("get ITERATOR");
        Iterator<Long> iterator = this.getIterator(indexID, descending);
        //LOGGER.debug("get ITERATOR - DONE"); / for merge

        Transaction transaction;
        for (int i = 0; i < count; i++) {
            if (!iterator.hasNext())
                break;

            transaction = this.get(iterator.next());
            transaction.setDC((DCSet)databaseSet);
            values.add(transaction);
        }
        iterator = null;
        return values;
    }

    public List<Transaction> getIncomedTransactions(String address, int type, long timestamp, int count, boolean descending) {

        ArrayList<Transaction> values = new ArrayList<>();
        Iterator<Long> iterator = this.getIterator(0, descending);
        Account account = new Account(address);

        int i = 0;
        Transaction transaction;
        while (iterator.hasNext()) {
            transaction = map.get(iterator.next());
            if (type != 0 && type != transaction.getType())
                continue;

            transaction.setDC((DCSet)databaseSet);
            HashSet<Account> recipients = transaction.getRecipientAccounts();
            if (recipients == null || recipients.isEmpty())
                continue;
            if (recipients.contains(account) && transaction.getTimestamp() >= timestamp) {
                values.add(transaction);
                i++;
                if (count > 0 && i > count)
                    break;
            }
        }
        return values;
    }

}
