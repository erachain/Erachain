package org.erachain.datachain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.TransactionSuitMapDB;
import org.erachain.dbs.mapDB.TransactionSuitMapDBFork;
import org.erachain.dbs.mapDB.TransactionSuitMapDBinMem;
import org.erachain.dbs.rocksDB.TransactionSuitRocksDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.util.*;

import static org.erachain.database.IDB.DBS_MAP_DB_IN_MEM;
import static org.erachain.database.IDB.DBS_ROCK_DB;

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
@Slf4j
public class TransactionMapImpl extends DBTabImpl<Long, Transaction>
        implements TransactionMap
{

    //public int TIMESTAMP_INDEX = 1;

    public int totalDeleted = 0;

    public TransactionMapImpl(int dbsUsed, DCSet databaseSet, DB database) {
        super(dbsUsed, databaseSet, database);

        DEFAULT_INDEX = TransactionSuit.TIMESTAMP_INDEX;

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_UNC_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_UNC_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_UNC_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE);
        }

    }

    public TransactionMapImpl(int dbsUsed, TransactionMap parent, DCSet databaseSet) {
        super(dbsUsed, parent, databaseSet);
    }

    @Override
    public void openMap()
    {
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new TransactionSuitRocksDB(databaseSet, database);
                    break;
                case DBS_MAP_DB_IN_MEM:
                    map = new TransactionSuitMapDBinMem(databaseSet, database);
                    break;
                default:
                    map = new TransactionSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                //case DBS_MAP_DB:
                case DBS_MAP_DB_IN_MEM:
                    map = new TransactionSuitMapDBFork((TransactionMap) parent, databaseSet);
                    break;
                case DBS_ROCK_DB:
                    //map = new TransactionSuitRocksDBFork((TransactionMap) parent, ((TransactionSuitRocksDB) parent).map, databaseSet);
                    //break;
                default:
                    //map = new NativeMapHashMapFork(parent, databaseSet, null);
                    map = new TransactionSuitMapDBFork((TransactionMap) parent, databaseSet);
            }
        }
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
        Iterator<Long> iterator = this.getTimestampIterator(false);
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

    public void setTotalDeleted(int value) { totalDeleted = value; }
    public int getTotalDeleted() { return totalDeleted; }

    //private static long MAX_DEADTIME = 1000 * 60 * 60 * 1;

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
     * @param timestamp
     * @param cutMaximum - образать список только по максимальному размеру, инаяе образать список и по времени протухания
     */
    protected long pointClear;
    public int clearByDeadTimeAndLimit(long timestamp, boolean cutMaximum) {

        // займем просецц или установим флаг
        if (isClearProcessedAndSet())
            return 0;

        try { // для освобождения ресурса

            long realTime = System.currentTimeMillis();

            if (realTime - pointClear < 10000) {
                return 0;
            }

            try { // для запоминания времени точки

                long tickerIter = realTime;
                int deletions = 0;
                long keepTime = BlockChain.GENERATING_MIN_BLOCK_TIME_MS(timestamp) << 3;

                timestamp -= (keepTime >> 1) + (keepTime << (5 - Controller.HARD_WORK >> 1));

                /**
                 * по несколько секунд итератор берется - при том что таблица пустая -
                 * - дале COMPACT не помогает
                 */
                //Iterator<Long> iterator = this.getIterator(TIMESTAMP_INDEX, false);
                //Iterator<Tuple2<?, Long>> iterator = map.getIterator(TIMESTAMP_INDEX, false);
                Iterator<Long> iterator = ((TransactionSuit) map).getTimestampIterator(false);
                tickerIter = System.currentTimeMillis() - tickerIter;
                if (tickerIter > 10) {
                    LOGGER.debug("TAKE ITERATOR: " + tickerIter + " ms");
                }

                Transaction transaction;

                tickerIter = System.currentTimeMillis();
                long size = this.map.size();
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
                    if (deadline < timestamp
                            || size - deletions >
                            (cutMaximum ? BlockChain.MAX_UNCONFIGMED_MAP_SIZE >> 3
                                    : BlockChain.MAX_UNCONFIGMED_MAP_SIZE)) {
                        this.delete(key);
                        deletions++;
                    } else {
                        break;
                    }
                }

                long ticker = System.currentTimeMillis() - realTime;
                if (ticker > 100 || deletions > 0) {
                    LOGGER.debug("------ CLEAR DEAD UTXs: " + ticker + " ms, for deleted: " + deletions);
                }

                return deletions;

            } finally {
                // учтем новое время в точке
                pointClear = System.currentTimeMillis();
            }
        } finally {
            databaseSet.clearCache();
            // освободим процесс
            clearProcessed = false;
        }
    }

    /**
     * Если таблица была закрыта для отчистки - то ошибку не пропускаем вверх
     * @param key
     * @param transaction
     * @return
     */
    @Override
    public boolean set(Long key, Transaction transaction) {
        try {
            return super.set(key, transaction);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(byte[] signature, Transaction transaction) {
        return this.set(Longs.fromByteArray(signature), transaction);

    }

    @Override
    public void put(Transaction transaction) {
        this.put(Longs.fromByteArray(transaction.getSignature()), transaction);
    }

    @Override
    public void delete(Transaction transaction) {
        this.delete(Longs.fromByteArray(transaction.getSignature()));
    }

    @Override
    public void delete(byte[] signature) {
        this.delete(Longs.fromByteArray(signature));
    }


    /**
     * synchronized - потому что почемуто вызывало ошибку в unconfirmedMap.delete(transactionSignature) в процессе блока.
     * Head Zero - data corrupted
     * @param key
     * @return
     */
    @Override
    public Transaction remove(Long key) {
        try {
            Transaction transaction = super.remove(key);
            if (transaction != null) {
                // DELETE only if DELETED
                totalDeleted++;
            }

            return transaction;
        } catch (Exception e) {

        }

        return null;

    }

    public void delete(Long key) {
        try {
            super.delete(key);
            totalDeleted++;
        } catch (Exception e) {

        }
    }

    public boolean contains(Long key) {
        try {
            return super.contains(key);
        } catch (Exception e) {

        }
        return false;
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

    /**
     * Из-за того что для проверки валидности в core.TransactionCreator
     * задаются номер блоки и позиция даже когда она неподтвержденная
     * То надо сбрасывать эти значения при получении из базы - иначе в КЭШе этот объект с установленными занчениями.
     * Так же чтобы не надететь на очистку карты из Пула - делаем отлов ошибок
     * @param key
     * @return
     */
    public Transaction get(Long key) {
        try {
            Transaction transaction = super.get(key);
            if (transaction != null) {
                transaction.setHeightSeq(0, 0);
            }
            return transaction;
        } catch (Exception e) {

        }
        return null;
    }

    public Collection<Long> getFromToKeys(long fromKey, long toKey) {

        List<Long> treeKeys = new ArrayList<Long>();

        // DESCENDING + 1000
        Iterator iterator =((TransactionSuit)map).getTimestampIterator(true);
        Iterators.advance(iterator, (int)fromKey);
        Iterator<Long> iteratorLimit = Iterators.limit(iterator, (int) (toKey - fromKey));

        while (iteratorLimit.hasNext()) {
            treeKeys.add(iteratorLimit.next());
        }

        return treeKeys;

    }

    @Override
    public Iterator<Long> getTimestampIterator(boolean descending) {
        return ((TransactionSuit)map).getTimestampIterator(descending);
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

    public Iterator findTransactionsKeys(String address, String sender, String recipient,
                                         int type, boolean desc, int offset, int limit, long timestamp) {
        Iterator senderKeys = null;
        Iterator recipientKeys = null;
        //TreeSet<Object> iterator = new TreeSet<>();
        Iterator iterator; // = new TreeSet<>().iterator();

        if (address != null) {
            sender = address;
            recipient = address;
        }

        if (sender == null && recipient == null) {
            return new TreeSet<>().iterator();
        }
        //  timestamp = null;
        if (sender != null) {
            if (type > 0 || timestamp > 0) {
                senderKeys = ((TransactionSuit)map).typeIterator(sender, timestamp, type);
            } else {
                senderKeys = ((TransactionSuit)map).senderIterator(sender);
            }
        }

        if (recipient != null) {
            if (type > 0 || timestamp > 0) {
                //recipientKeys = Fun.filter(this.typeKey, new Fun.Tuple3<String, Long, Integer>(recipient, timestamp, type));
                recipientKeys = ((TransactionSuit)map).typeIterator(recipient, timestamp, type);
            } else {
                //recipientKeys = Fun.filter(this.recipientKey, recipient);
                recipientKeys = ((TransactionSuit)map).recipientIterator(recipient);
            }
        }

        if (address != null) {
            //iterator.addAll(Sets.newTreeSet(senderKeys));
            //iterator = senderKeys;
            //iterator.addAll(Sets.newTreeSet(recipientKeys));
            // not sorted! Iterators.concat(iterator, recipientKeys);
            iterator = Iterators.mergeSorted(ImmutableList.of(senderKeys, recipientKeys), Fun.COMPARATOR);

        } else if (sender != null && recipient != null) {
            //iterator.addAll(Sets.newTreeSet(senderKeys));
            iterator = senderKeys;
            //iterator.retainAll(Sets.newTreeSet(recipientKeys));
            Iterators.retainAll(iterator, Lists.newArrayList(recipientKeys));
        } else if (sender != null) {
            //iterator.addAll(Sets.newTreeSet(senderKeys));
            iterator = senderKeys;
        } else if (recipient != null) {
            //iterator.addAll(Sets.newTreeSet(recipientKeys));
            iterator = recipientKeys;
        } else {
            iterator = new TreeSet<>().iterator();
        }

        if (desc) {
            //keys = ((TreeSet) iterator).descendingSet();
            iterator = Lists.reverse(Lists.newArrayList(iterator)).iterator();
        }

        if (offset > 0) {
            Iterators.advance(iterator, offset);
        }

        if (limit > 0) {
            iterator = Iterators.limit(iterator, limit);
        }

        return iterator;
    }

    public List<Transaction> findTransactions(String address, String sender, String recipient,
                                              int type, boolean desc, int offset, int limit, long timestamp) {

        Iterator keys = findTransactionsKeys(address, sender, recipient,
                type, desc, offset, limit, timestamp);
        return getUnconfirmedTransaction(keys);

    }


    public List<Transaction> getUnconfirmedTransaction(Iterator iter) {
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

        Iterator<Long> senderKeys = ((TransactionSuit)map).senderIterator(address);
        Iterator<Long> recipientKeys = ((TransactionSuit)map).recipientIterator(address);

        Iterators.advance(senderKeys, 100);
        Iterators.advance(recipientKeys, 100);

        //treeKeys  = Iterators.concat(senderKeys, recipientKeys);
        Iterator<Long> iterator = Iterators.mergeSorted(ImmutableList.of(senderKeys, recipientKeys), Fun.COMPARATOR);

        Iterators.advance(iterator, 100);

        return getUnconfirmedTransaction(iterator);

    }

    // slow?? without index
    public List<Transaction> getTransactionsByAddress(String address) {

        ArrayList<Transaction> values = new ArrayList<Transaction>();
        Iterator<Long> iterator = ((TransactionSuit)map).getTimestampIterator(false);
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

    public List<Transaction> getTransactions(int count, boolean descending) {

        ArrayList<Transaction> values = new ArrayList<Transaction>();

        //LOGGER.debug("get ITERATOR");
        Iterator<Long> iterator = this.getIterator(TransactionSuit.TIMESTAMP_INDEX, descending);
        //LOGGER.debug("get ITERATOR - DONE"); / for merge

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

    public List<Transaction> getIncomedTransactions(String address, int type, long timestamp, int count, boolean descending) {

        ArrayList<Transaction> values = new ArrayList<>();
        Iterator<Long> iterator = this.getIterator(TransactionSuit.TIMESTAMP_INDEX, descending);
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
