package org.erachain.datachain;

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
import org.erachain.dbs.nativeMemMap.nativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.TransactionSuitRocksDB;
import org.erachain.dbs.rocksDB.TransactionSuitRocksDBFork;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

import java.util.*;

import static org.erachain.database.IDB.*;

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
class TransactionTabImpl extends DBTabImpl<Long, Transaction>
        implements TransactionTab
{

    //public int TIMESTAMP_INDEX = 1;

    public int totalDeleted = 0;

    public TransactionTabImpl(int dbsUsed, DCSet databaseSet, DB database) {
        super(dbsUsed, databaseSet, database);

        DEFAULT_INDEX = TransactionSuit.TIMESTAMP_INDEX;

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_UNC_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_UNC_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_UNC_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE);
        }

    }

    public TransactionTabImpl(int dbsUsed, TransactionTab parent, DCSet databaseSet) {
        super(dbsUsed, parent, databaseSet);
    }

    @Override
    protected void getMap()
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
                case DBS_MAP_DB:
                    map = new TransactionSuitMapDBFork((TransactionTab) parent, databaseSet);
                    break;
                case DBS_ROCK_DB:
                    map = new TransactionSuitRocksDBFork((TransactionTab) parent, databaseSet);
                    break;
                default:
                    map = new nativeMapTreeMapFork(parent, databaseSet, ItemAssetBalanceSuit.DEFAULT_VALUE);
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
     * @param timestamp
     * @param cutDeadTime
     */
    protected long pointClear;
    public void clearByDeadTimeAndLimit(long timestamp, boolean cutDeadTime) {

        // займем просецц или установим флаг
        if (isClearProcessedAndSet())
            return;

        long keepTime = BlockChain.VERS_30SEC_TIME < timestamp? 600000 : 240000;
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
            //Iterator<Long> iterator = this.getIterator(TIMESTAMP_INDEX, false);
            //Iterator<Tuple2<?, Long>> iterator = map.getIterator(TIMESTAMP_INDEX, false);
            Iterator<Long> iterator = ((TransactionSuit)map).getTimestampIterator(false);
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
                    this.remove(key);
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
        return this.remove(Longs.fromByteArray(signature));
    }


    /**
     * synchronized - потому что почемуто вызывало ошибку в unconfirmedMap.delete(transactionSignature) в процессе блока.
     * Head Zero - data corrupted
     * @param key
     * @return
     */
    public /* synchronized */ Transaction remove(Long key) {
        Transaction transaction = super.remove(key);
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

    /**
     * Из-за того что для проверки валидности в core.TransactionCreator
     * задаются номер блоки и позиция даже когда она неподтвержденная
     * То надо сбрасывать эти значения при получении из базы - иначе в КЭШе этот объект с установленными занчениями
     * @param key
     * @return
     */
    public Transaction get(long key) {
        Transaction transaction = super.get(key);
        transaction.setHeightSeq(0, 0);
        return transaction;
    }
    public Transaction get(Long key) {
        Transaction transaction = super.get(key);
        transaction.setHeightSeq(0, 0);
        return transaction;
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
        //TreeSet<Object> treeKeys = new TreeSet<>();
        Iterator treeKeys = new TreeSet<>().iterator();

        if (address != null) {
            sender = address;
            recipient = address;
        }

        if (sender == null && recipient == null) {
            return treeKeys;
        }
        //  timestamp = null;
        if (sender != null) {
            if (type > 0) {
                senderKeys = ((TransactionSuit)map).typeIterator(sender, timestamp, type);
            } else {
                senderKeys = ((TransactionSuit)map).senderIterator(sender);
            }
        }

        if (recipient != null) {
            if (type > 0) {
                //recipientKeys = Fun.filter(this.typeKey, new Fun.Tuple3<String, Long, Integer>(recipient, timestamp, type));
                recipientKeys = ((TransactionSuit)map).typeIterator(recipient, timestamp, type);
            } else {
                //recipientKeys = Fun.filter(this.recipientKey, recipient);
                recipientKeys = ((TransactionSuit)map).recipientIterator(recipient);
            }
        }

        if (address != null) {
            //treeKeys.addAll(Sets.newTreeSet(senderKeys));
            Iterators.concat(treeKeys, senderKeys);
            //treeKeys.addAll(Sets.newTreeSet(recipientKeys));
            Iterators.concat(treeKeys, recipientKeys);
        } else if (sender != null && recipient != null) {
            //treeKeys.addAll(Sets.newTreeSet(senderKeys));
            Iterators.concat(treeKeys, senderKeys);
            //treeKeys.retainAll(Sets.newTreeSet(recipientKeys));
            Iterators.retainAll(treeKeys, Lists.newArrayList(recipientKeys));
        } else if (sender != null) {
            //treeKeys.addAll(Sets.newTreeSet(senderKeys));
            Iterators.concat(treeKeys, senderKeys);
        } else if (recipient != null) {
            //treeKeys.addAll(Sets.newTreeSet(recipientKeys));
            Iterators.concat(treeKeys, recipientKeys);
        }

        Iterator keys;
        if (desc) {
            //keys = ((TreeSet) treeKeys).descendingSet();
            keys = ((TreeSet) treeKeys).descendingIterator();
        } else {
            keys = treeKeys;
        }

        if (offset > 0) {
            Iterators.advance(keys, offset);
        }

        if (limit > 0) {
            keys = Iterators.limit(keys, limit);
        }

        return  keys;
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

        Iterator<Long> treeKeys = new TreeSet<Long>().iterator();

        Iterator<Long> senderKeys = ((TransactionSuit)map).senderIterator(address);
        Iterator<Long> recipientKeys = ((TransactionSuit)map).recipientIterator(address);

        Iterators.advance(senderKeys, 100);
        Iterators.advance(recipientKeys, 100);

        treeKeys  = Iterators.concat(senderKeys, recipientKeys);
        Iterators.advance(treeKeys, 100);

        return getUnconfirmedTransaction(treeKeys);

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
