package org.erachain.datachain;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Observer;

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
public interface TransactionMap extends DBTab<Long, Transaction> {

    Integer deleteObservableData(int index);

    Integer setObservableData(int index, Integer data);

    IteratorCloseable<Long> getTimestampIterator(boolean descending);

    int clearByDeadTime(long heightTimestamp);

    void put(Transaction transaction);

    void delete(Transaction transaction);
    void delete(byte[] signature);

    boolean contains(byte[] signature);
    boolean contains(Transaction transaction);

    Transaction get(byte[] signature);

    Collection<Long> getFromToKeys(Long fromKey, int limit);

    @SuppressWarnings({"rawtypes", "unchecked"})
    IteratorCloseable findTransactionsKeys(String address, String sender, String recipient,
                                           int type, boolean desc, int offset, int limit, long timestamp);

    List<Transaction> findTransactions(String address, String sender, String recipient,
                                       int type, boolean desc, int offset, int limit, long timestamp);

    List<Transaction> getUnconfirmedTransaction(Iterator keys);

    // TODO выдает ошибку на шаге treeKeys.addAll(Sets.newTreeSet(senderKeys));
    List<Transaction> getTransactionsByAddressFast100(String address);

    // slow?? without index
    List<Transaction> getTransactionsByAddress(String address);

    List<Transaction> getTransactions(int count, boolean descending);

    List<Transaction> getIncomedTransactions(String address, int type, long timestamp, int count, boolean descending);

    void setTotalDeleted(int value);
    int getTotalDeleted();

    int size();

    Collection<Transaction> values();

    void addObserver(Observer o);
    void deleteObserver(Observer o);


}
