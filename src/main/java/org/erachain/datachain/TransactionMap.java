package org.erachain.datachain;

import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBMap;
import org.erachain.utils.ObserverMessage;
import org.mapdb.*;
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
abstract public class TransactionMap extends DCMap<Long, Transaction> {

    public static int TIMESTAMP_INDEX = 1;
    NavigableSet senderKey = null;
    NavigableSet recipientKey = null;
    NavigableSet typeKey = null;

    public TransactionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        DEFAULT_INDEX = TIMESTAMP_INDEX;

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

    public Integer deleteObservableData(int index) {
        return this.observableData.remove(index);
    }

    public Integer setObservableData(int index, Integer data) {
        return this.observableData.put(index, data);
    }

    abstract public Iterator<Long> getTimestampIterator();

    abstract public Iterator<Long> getCeatorIterator();

    /**
     * Используется для получения транзакций для сборки блока
     * Поидее нужно братьв се что есть без учета времени протухания для сборки блока своего
     * @param timestamp
     * @param notSetDCSet
     * @param cutDeadTime true is need filter by Dead Time
     * @return
     */
    abstract public List<Transaction> getSubSet(long timestamp, boolean notSetDCSet, boolean cutDeadTime);

    protected static long MAX_DEADTIME = 1000 * 60 * 60 * 1;

    protected boolean clearProcessed = false;
    protected synchronized boolean isClearProcessedAndSet() {

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
    long pointClear = 0;
    abstract public void clearByDeadTimeAndLimit(long timestamp, boolean cutDeadTime);

    abstract public void update(Observable o, Object arg);

    abstract public boolean set(byte[] key, Transaction transaction);
    abstract public boolean add(Transaction transaction);
    abstract public Transaction get(byte[] signature);
    abstract public boolean contains(byte[] signature);

    abstract public void delete(Transaction transaction);
    abstract public Transaction delete(byte[] signature);

    long totalDeleted = 0;

    abstract public boolean contains(Transaction transaction);

    abstract public Collection<Long> getFromToKeys(long fromKey, long toKey);

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
    abstract public Iterable findTransactionsKeys(String address, String sender, String recipient,
                                         int type, boolean desc, int offset, int limit, long timestamp);

    abstract public List<Transaction> findTransactions(String address, String sender, String recipient,
                                              int type, boolean desc, int offset, int limit, long timestamp);

    abstract public List<Transaction> getUnconfirmedTransaction(Iterable keys);

    abstract public List<Transaction> getTransactionsByAddressFast100(String address);

    abstract public List<Transaction> getTransactionsByAddress(String address);

    abstract public List<Transaction> getTransactions(int count, boolean descending);

    abstract public List<Transaction> getIncomedTransactions(String address, int type, long timestamp, int count, boolean descending);

}
