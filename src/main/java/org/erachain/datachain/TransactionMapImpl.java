package org.erachain.datachain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.TransactionsPool;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.*;
import org.erachain.dbs.mapDB.TransactionSuitMapDB;
import org.erachain.dbs.mapDB.TransactionSuitMapDBFork;
import org.erachain.dbs.mapDB.TransactionSuitMapDBinMem;
import org.erachain.dbs.rocksDB.TransactionSuitRocksDB;
import org.erachain.ntp.NTP;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.io.IOException;
import java.util.*;

import static org.erachain.database.IDB.DBS_MAP_DB_IN_MEM;
import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Храним неподтвержденные транзакции - memory pool for unconfirmed transaction.
 * Signature (as Long) -> Transaction
 * <hr>
 * Здесь вторичные индексы создаются по несколько для одной записи путем создания массива ключей,
 * см. typeKey и recipientKey. Они используются для API RPC block explorer.
 * Нужно ограничивать размер выдаваемого списка чтобы не перегружать ноду.
 * <br>
 * Так же вторичный индекс по времени, который используется в ГУИ TIMESTAMP_INDEX = 0 (default)
 * - он организуется внутри DCMap в списке индексов для сортировок в ГУИ
 * <p>
 * Также хранит инфо каким пирам мы уже разослали транзакцию неподтвержденную так что бы при подключении делать автоматически broadcast
 *
 * <hr>
 * (!!!) для создания уникальных ключей НЕ нужно добавлять + val.viewTimestamp(), и так работает, а почему в Ордерах не работало?
 * <br>в БИНДЕ внутри уникальные ключи создаются добавлением основного ключа
 */
@Slf4j
public class TransactionMapImpl extends DBTabImpl<Long, Transaction>
        implements TransactionMap {

    TransactionsPool pool;

    public int totalDeleted = 0;

    public TransactionMapImpl(int dbsUsed, DCSet databaseSet, DB database) {
        super(dbsUsed, databaseSet, database);

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
            if (Controller.getInstance().uTxInMemory) {
                map = new TransactionSuitMapDBinMem(databaseSet, database);
            } else {
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
            }
        } else {
            switch (dbsUsed) {
                //case DBS_MAP_DB:
                case DBS_MAP_DB_IN_MEM:
                    //map = new TransactionSuitMapDBFork((TransactionMap) parent, databaseSet);
                    //break;
                case DBS_ROCK_DB:
                    //map = new TransactionSuitRocksDBFork((TransactionMap) parent, ((TransactionSuitRocksDB) parent).map, databaseSet);
                    //break;
                default:
                    //map = new NativeMapHashMapFork(parent, databaseSet, null);
                    map = new TransactionSuitMapDBFork((TransactionMap) parent, databaseSet);
            }
        }
    }

    public void setPool(TransactionsPool pool) {
        this.pool = pool;
    }

    public int getTotalDeleted() {
        return totalDeleted;
    }

    public void setTotalDeleted(int value) {
        totalDeleted = value;
    }

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
     *
     * @param timestamp
     * @param cutMaximum - обрезать список только по максимальному размеру, иначе обрезать список и по времени протухания
     */
    protected long pointClear;
    public int clearByDeadTimeAndLimit(long keepTime, boolean cutMaximum) {

        if (isClearProcessedAndSet())
            return 0;

        try { // для освобождения ресурса

            long realTime = NTP.getTime();

            if (realTime - pointClear < 10000) {
                return 0;
            }

            try { // для запоминания времени точки

                long tickerIter = realTime;
                int deletions = 0;

                /**
                 * по несколько секунд итератор берется - при том что таблица пустая -
                 * - дале COMPACT не помогает
                 */
                try (IteratorCloseable<Long> iterator = ((TransactionSuit) map).getTimestampIterator(false)) {
                    tickerIter = NTP.getTime() - tickerIter;
                    if (tickerIter > 10) {
                        LOGGER.debug("TAKE ITERATOR: {} ms", tickerIter);
                    }

                    Transaction transaction;

                    tickerIter = NTP.getTime();
                    long size = this.map.size();
                    tickerIter = NTP.getTime() - tickerIter;
                    if (tickerIter > 10 && logger.isDebugEnabled()) {
                        LOGGER.debug("TAKE ITERATOR.SIZE: {} ms", tickerIter);
                    }
                    while (iterator.hasNext()) {
                        Long key = iterator.next();
                        transaction = this.map.get(key);
                        if (transaction == null) {
                            // такая ошибка уже была
                            continue;
                        }

                        long deadline = transaction.getDeadline();
                        if (deadline < keepTime
                                || size - deletions >
                                (cutMaximum ? BlockChain.MAX_UNCONFIGMED_MAP_SIZE >> 3
                                        : BlockChain.MAX_UNCONFIGMED_MAP_SIZE)) {
                            // обязательно прямая чиста из таблицы иначе опять сюда в очередь прилетит и не сработает
                            this.deleteDirect(key);
                            LOGGER.debug("deleteDirect: {}", Transaction.viewDBRef(key));
                            deletions++;
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }

                long ticker = NTP.getTime() - realTime;
                if (ticker > 100 || deletions > 0) {
                    LOGGER.info("------ CLEAR DEAD UTXs: {} ms, for deleted: {}", ticker, deletions);
                }

                return deletions;

            } finally {
                // учтем новое время в точке
                pointClear = NTP.getTime();
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

    /**
     * Нужно вносить через очередь так как там может быть очистка таблицы с закрыванием запущена, см issue #1246
     * Так же чтобы при закрытии в dbs.mapDB.DBMapSuitFork.writeToParent(DBMapSuitFork.java:371) не вылетала ошибка что таблица закрыта
     *
     * @param key
     * @param transaction
     */
    @Override
    public void put(Long key, Transaction transaction) {
        if (pool == null)
            return;

        pool.offerMessage(transaction);
    }

    @Override
    public void delete(Transaction transaction) {
        this.delete(Longs.fromByteArray(transaction.getSignature()));
    }

    @Override
    public void delete(byte[] signature) {
        this.delete(Longs.fromByteArray(signature));
    }

    public void deleteDirect(byte[] signature) {
        super.delete(Longs.fromByteArray(signature));
    }

    public void putDirect(Transaction transaction) {
        super.put(Longs.fromByteArray(transaction.getSignature()), transaction);
    }

    public void deleteDirect(Long key) {
        super.delete(key);
    }


    /**
     * Нужно удалять через очередь так как там может быть очистка таблицы с закрыванием запущена, см issue #1246
     * Так же чтобы при закрытии в dbs.mapDB.DBMapSuitFork.writeToParent(DBMapSuitFork.java:371) не вылетала ошибка что таблица закрыта
     * <hr>
     * ////// synchronized - потому что почемуто вызывало ошибку в unconfirmedMap.delete(transactionSignature) в процессе блока.
     * ////// Head Zero - data corrupted
     *
     * @param key
     * @return
     */
    @Override
    public Transaction remove(Long key) {
        if (pool == null)
            return null;

        try {

            Transaction transactionOld = null;
            if (contains(key)) {
                totalDeleted++;
                transactionOld = get(key);
            }

            // delete by key
            pool.offerMessage(key);

            return transactionOld;
        } catch (Exception e) {
        }

        return null;

    }

    /**
     * Нужно удалять через очередь так как там может быть очистка таблицы с закрыванием запущена, см issue #1246
     * Так же чтобы при закрытии в dbs.mapDB.DBMapSuitFork.writeToParent(DBMapSuitFork.java:371) не вылетала ошибка что таблица закрыта
     *
     * @param key
     */
    @Override
    public void delete(Long key) {
        if (pool == null)
            return;

        pool.offerMessage(key);
        totalDeleted++;
    }

    public boolean contains(Long key) {
        try {
            return super.contains(key);
        } catch (Exception e) {

        }
        return false;
    }

    @Override
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
                transaction.resetSeqNo();
            }
            return transaction;
        } catch (Exception e) {

        }
        return null;
    }

    public Collection<Long> getFromToKeys(Long fromKey, int limit) {

        List<Long> treeKeys = new ArrayList<Long>();

        // DESCENDING + 1000
        try (IteratorCloseable<Long> iterator = ((TransactionSuit) map).getTimestampIterator(true)) {

            //Iterators.advance(iterator, (int) fromKey);

            // тут не нужно уже делать Закрываемый Итератор - так достаточно того что внутренний Итератор закроется
            Iterator<Long> iteratorLimited = Iterators.limit(iterator, limit);

            while (iteratorLimited.hasNext()) {
                treeKeys.add(iteratorLimited.next());
            }
        } catch (IOException e) {
        }

        return treeKeys;

    }

    @Override
    public IteratorCloseable<Long> getTimestampIterator(boolean descending) {
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

    public IteratorCloseable findTransactionsKeys(String address, String sender, String recipient,
                                                  int type, boolean desc, int offset, int limit, long timestamp) {
        IteratorCloseable iteratorSender = null;
        IteratorCloseable iteratorRecipient = null;

        if (address != null) {
            sender = address;
            recipient = address;
        }

        if (sender == null && recipient == null) {
            return new IteratorCloseableImpl(new TreeSet<>().iterator());
        }
        //  timestamp = null;
        if (sender != null) {
            if (type > 0 || timestamp > 0) {
                iteratorSender = ((TransactionSuit)map).typeIterator(sender, timestamp, type);
            } else {
                iteratorSender = ((TransactionSuit)map).senderIterator(sender);
            }
        }

        if (recipient != null) {
            if (type > 0 || timestamp > 0) {
                //recipientKeys = Fun.filter(this.typeKey, new Fun.Tuple3<String, Long, Integer>(recipient, timestamp, type));
                iteratorRecipient = ((TransactionSuit)map).typeIterator(recipient, timestamp, type);
            } else {
                //recipientKeys = Fun.filter(this.recipientKey, recipient);
                iteratorRecipient = ((TransactionSuit)map).recipientIterator(recipient);
            }
        }

        IteratorCloseable iteratorMerged;
        if (address != null || sender != null && recipient != null) {
            // а этот Итератор.mergeSorted - он дублирует повторяющиеся значения индекса (( и делает пересортировку асинхронно - то есть тоже не ахти то что нужно
            iteratorMerged = new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(iteratorSender, iteratorRecipient),
                    Fun.COMPARATOR, desc);
        } else if (sender != null) {
            iteratorMerged = iteratorSender;
        } else if (recipient != null) {
            iteratorMerged = iteratorRecipient;
        } else {
            iteratorMerged = new IteratorCloseableImpl(new TreeSet<>().iterator());
        }

        if (desc) {
            //keys = ((TreeSet) iterator).descendingSet();
            /// закроем по выходу итератор так как тут новый делается
            try (IteratorCloseable mergedOld = iteratorMerged) {
                iteratorMerged = new IteratorCloseableImpl(Lists.reverse(Lists.newArrayList(iteratorMerged)).iterator());
            } catch (IOException e) {
            }
        }

        if (offset > 0) {
            Iterators.advance(iteratorMerged, offset);
        }

        if (limit > 0) {
            iteratorMerged = IteratorCloseableImpl.limit(iteratorMerged, limit);
        }

        return iteratorMerged;
    }

    public List<Transaction> findTransactions(String address, String sender, String recipient,
                                              int type, boolean desc, int offset, int limit, long timestamp) {

        try (IteratorCloseable keys = findTransactionsKeys(address, sender, recipient,
                type, desc, offset, limit, timestamp)) {
            return getUnconfirmedTransaction(keys);
        } catch (IOException e) {
        }

        return null;
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

        // здесь не нужно обрамления с try (=) - так как они оба потом закроются в объединенном итераторе
        IteratorCloseable<Long> iteratorSender = ((TransactionSuit) map).senderIterator(address);
        IteratorCloseable<Long> iteratorRecipient = ((TransactionSuit) map).recipientIterator(address);

        // а этот Итератор.mergeSorted - он дублирует повторяющиеся значения индекса (( и делает пересортировку асинхронно - то есть тоже не ахти то что нужно
        /// берем свой итератор
        try (IteratorCloseable<Long> iterator = new MergedOR_IteratorsNoDuplicates(ImmutableList.of(iteratorSender, iteratorRecipient),
                Fun.COMPARATOR, false)) {
            return getUnconfirmedTransaction(IteratorCloseableImpl.limit(iterator, 200));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    // slow?? without index
    public List<Transaction> getTransactionsByAddress(String address) {

        ArrayList<Transaction> values = new ArrayList<Transaction>();

        try (IteratorCloseable<Long> iterator = ((TransactionSuit) map).getTimestampIterator(false)) {
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
                    transaction.setDC((DCSet) databaseSet, true);
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
        } catch (IOException e) {
        }

        return values;
    }

    public List<Transaction> getTransactions(int count, boolean descending) {

        ArrayList<Transaction> values = new ArrayList<Transaction>();

        //LOGGER.debug("get ITERATOR");
        try (IteratorCloseable<Long> iterator = this.getTimestampIterator(descending)) {
            //LOGGER.debug("get ITERATOR - DONE"); / for merge

            Transaction transaction;
            for (int i = 0; i < count; i++) {
                if (!iterator.hasNext())
                    break;

                transaction = this.get(iterator.next());
                transaction.setDC((DCSet) databaseSet, true);
                values.add(transaction);
            }
        } catch (IOException e) {
        }

        return values;
    }

    /**
     * @param account    may be Null for ALL
     * @param type
     * @param timestamp
     * @param count
     * @param descending
     * @return
     */
    public List<Transaction> getTransactions(Account account, int type, long timestamp, int count, boolean descending) {

        ArrayList<Transaction> values = new ArrayList<>();
        try (IteratorCloseable<Long> iterator = this.getTimestampIterator(descending)) {

            int i = 0;
            Transaction transaction;
            while (iterator.hasNext()) {
                transaction = map.get(iterator.next());
                if (type != 0 && type != transaction.getType()
                        || transaction.getTimestamp() < timestamp)
                    continue;

                transaction.setDC((DCSet) databaseSet, true);

                if ((account == null || transaction.isInvolved(account))) {
                    values.add(transaction);
                    i++;
                    if (count > 0 && i > count)
                        break;
                }
            }
        } catch (IOException e) {
        }

        return values;
    }

    public List<Transaction> getIncomedTransactions(String address, int type, long timestamp, int count, boolean descending) {

        ArrayList<Transaction> values = new ArrayList<>();
        try (IteratorCloseable<Long> iterator = this.getTimestampIterator(descending)) {
            Account account = new Account(address);

            int i = 0;
            Transaction transaction;
            while (iterator.hasNext()) {
                transaction = map.get(iterator.next());
                if (type != 0 && type != transaction.getType())
                    continue;

                transaction.setDC((DCSet) databaseSet, true);
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
        } catch (IOException e) {
        }

        return values;
    }

}
