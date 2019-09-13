package org.erachain.datachain;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private SimpleIndexDB<Long, Transaction, Fun.Tuple2<String, Long>> senderUnconfirmedTransactionIndex;


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

    public Integer deleteObservableData(int index) {
        return this.observableData.remove(index);
    }

    public Integer setObservableData(int index, Integer data) {
        return this.observableData.put(index, data);
    }

    @Override
    protected void getMap() {
        tableDB = new DBRocksDBTable<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                ROCKS_DB_FOLDER);
    }

    @Override
    protected void getMemoryMap() {
        tableDB = new DBMapDB<>(new HashMap<>());
    }

    /**
     * @param descending true if need descending sort
     * @return
     */
    public Iterator<Long> getIndexIterator(IndexDB indexDB, boolean descending) {
        return tableDB.getIndexIterator(descending, indexDB);
    }

    public Iterator<Long> getIterator(boolean descending) {
        return tableDB.getIterator(descending);
    }

    @Override
    public Iterator<Long> getIterator(int index, boolean descending) {
        return tableDB.getIndexIterator(descending, index);
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
                if (tableDB instanceof DBRocksDBTable) {
                    recipientKeys = ((DBRocksDBTable) tableDB).filterAppropriateValuesAsKeys(
                            indexByteableTuple3StringLongInteger.toBytes(new Fun.Tuple3<>(recipient, timestamp, type), null),
                            ((DBRocksDBTable) tableDB).receiveIndexByName(addressTypeUnconfirmedTransactionIndexName));
                } else {
                    recipientKeys = (Iterable)((DBMapDB) tableDB).getIterator(false);
                }
            } else {
                if (tableDB instanceof DBRocksDBTable) {
                    recipientKeys = ((DBRocksDBTable) tableDB).filterAppropriateValuesAsKeys(recipient.getBytes(),
                            ((DBRocksDBTable) tableDB).receiveIndexByName(recipientUnconfirmedTransactionIndexName));
                } else {
                    recipientKeys = (Iterable)((DBMapDB) tableDB).getIterator(false);
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
            item = tableDB.get(key);
            transactions.add(item);
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByAddressFast100(String address, int limitSize) {
        HashSet<Long> treeKeys = new HashSet<>();
        Set<Long> senderKeys = ((DBRocksDBTable) tableDB).filterAppropriateValuesAsKeys(address.getBytes(),
                ((DBRocksDBTable) tableDB).receiveIndexByName(senderUnconfirmedTransactionIndexName));
        List<Long> senderKeysLimit = senderKeys.stream().limit(limitSize).collect(Collectors.toList());
        Set<Long> recipientKeys = ((DBRocksDBTable) tableDB).filterAppropriateValuesAsKeys(address.getBytes(),
                ((DBRocksDBTable) tableDB).receiveIndexByName(recipientUnconfirmedTransactionIndexName));
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
            transaction = tableDB.get(iterator.next());
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
            transaction = tableDB.get(iterator.next());
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


    /**
     * Используется для получения транзакций для сборки блока
     * Поидее нужно братьв се что есть без учета времени протухания для сборки блока своего
     *
     * @param timestamp
     * @param notSetDCSet
     * @param cutDeadTime true is need filter by Dead Time
     * @return
     */
    public List<Transaction> getSubSet(long timestamp, boolean notSetDCSet, boolean cutDeadTime) {

        List<Transaction> values = new ArrayList<Transaction>();

        Iterator<Long> iterator = getTimestampIterator();

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
                transaction.setDC((DCSet) databaseSet);

            values.add(transaction);

        }

        return values;
    }

    public void setTotalDeleted(int value) {
        totalDeleted = value;
    }

    public int getTotalDeleted() {
        return totalDeleted;
    }

    private static long MAX_DEADTIME = 1000 * 60 * 60 * 1;

    private boolean clearProcessed = false;

    private synchronized boolean isClearProcessedAndSet() {

        if (clearProcessed)
            return true;

        clearProcessed = true;

        return false;
    }

    /**
     * очищает  только по признаку протухания и ограничения на размер списка - без учета валидности
     * С учетом валидности очистка идет в Генераторе после каждого запоминания блока
     *
     * @param timestamp
     * @param cutDeadTime
     */
    protected long pointClear;

    public void clearByDeadTimeAndLimit(long timestamp, boolean cutDeadTime) {

        // займем просецц или установим флаг
        if (isClearProcessedAndSet())
            return;

        long keepTime = BlockChain.VERS_30SEC_TIME < timestamp ? 600000 : 240000;
        try {
            long realTime = System.currentTimeMillis();

            if (realTime - pointClear < keepTime) {
                return;
            }

            int count = 0;
            long tickerIter = realTime;

            timestamp -= (keepTime >> 1) + (keepTime << (5 - Controller.HARD_WORK >> 1));

            /**
             * по несколько секунд итератор берется - при том что таблица пустая -
             * - дале COMPACT не помогает
             */
            Iterator<Long> iterator = getIterator(false); // getTimestampIterator();
            tickerIter = System.currentTimeMillis() - tickerIter;
            if (tickerIter > 10) {
                LOGGER.debug("TAKE ITERATOR: " + tickerIter + " ms");
            }

            Transaction transaction;

            tickerIter = System.currentTimeMillis();
            long size = this.size();
            tickerIter = System.currentTimeMillis() - tickerIter;
            if (tickerIter > 10) {
                LOGGER.debug("TAKE ITERATOR.SIZE: " + tickerIter + " ms");
            }
            while (iterator.hasNext()) {
                Long key = iterator.next();
                transaction = this.map.get(key);
                if (transaction == null) {
                    // такая ошибка уже было
                    break;
                }

                long deadline = transaction.getDeadline();
                if (realTime - deadline > 86400000 // позде на день удаляем в любом случае
                        || ((Controller.HARD_WORK > 3
                        || cutDeadTime)
                        && deadline < timestamp)
                        || Controller.HARD_WORK <= 3
                        && deadline + MAX_DEADTIME < timestamp // через сутки удалять в любом случае
                        || size - count > BlockChain.MAX_UNCONFIGMED_MAP_SIZE) {
                    this.delete(key);
                    count++;
                } else {
                    break;
                }
            }

            long ticker = System.currentTimeMillis() - realTime;
            if (ticker > 1000 || count > 0) {
                LOGGER.debug("------ CLEAR DEAD UTXs: " + ticker + " ms, for deleted: " + count);
            }

        } finally {
            // освободим процесс
            pointClear = System.currentTimeMillis();
            clearProcessed = false;
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


    /**
     * synchronized - потому что почемуто вызывало ошибку в unconfirmedMap.delete(transactionSignature) в процессе блока.
     * Head Zero - data corrupted
     *
     * @param key
     * @return
     */
    public /* synchronized */ Transaction delete(Long key) {
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

        Iterator<Long> iterator = getIterator(false); //getTimestampIterator();
        while (iterator.hasNext()) {
            treeKeys.add(iterator.next());
        }

        return treeKeys;

    }


    // TODO выдает ошибку на шаге treeKeys.addAll(Sets.newTreeSet(senderKeys));
    public List<Transaction> getTransactionsByAddressFast100(String address) {
        return getTransactionsByAddressFast100(address, 100);

    }

    public List<Transaction> getTransactions(int count, boolean descending) {

        ArrayList<Transaction> values = new ArrayList<Transaction>();

        LOGGER.debug("get ITERATOR");
        Iterator<Long> iterator = getIterator(false); // getTimestampIterator();
        LOGGER.debug("get ITERATOR - DONE"); // for merge

        Transaction transaction;
        for (int i = 0; i < count; i++) {
            if (!iterator.hasNext())
                break;

            transaction = this.get(iterator.next());
            transaction.setDC((DCSet)databaseSet);
            values.add(transaction);
        }
        return values;
    }

}
