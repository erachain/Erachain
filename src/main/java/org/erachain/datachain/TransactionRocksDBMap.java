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
import org.erachain.dbs.rocksDB.integration.InnerDBTable;
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
//public class TransactionRocksDBMap extends org.erachain.dbs.rocksDB.DCMap<Long, Transaction>
//    implements TransactionMap
public class TransactionRocksDBMap extends TransactionMapImpl
{

    static Logger logger = LoggerFactory.getLogger(TransactionRocksDBMap.class.getSimpleName());

    public TransactionRocksDBMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    protected void getMap() {
        map = new org.erachain.dbs.rocksDB.TransactionRocksDBMap(databaseSet, database);
    }

    @Override
    protected void createIndexes() {
    }

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
     * @param descending true if need descending sort
     * @return
     */
    public Iterator<Long> getIndexIterator(IndexDB indexDB, boolean descending) {
        return ((DBRocksDBTable) map).getIndexIterator(descending, indexDB);
    }

    public Iterator<Long> getIterator(boolean descending) {
        return map.getIterator(descending);
    }

    @Override
    public Iterator<Long> getIterator(int index, boolean descending) {
        return ((DBRocksDBTable) map).getIndexIterator(descending, index);
    }

    public Iterator<Long> getTimestampIterator() {
        return getIndexIterator(senderUnconfirmedTransactionIndex, false);
    }

    public Iterator<Long> getCeatorIterator() {
        return null;
    }

    //senderKeys = receiveIndexKeys(sender, type, timestamp, senderKeys, senderUnconfirmedTransactionIndexName);
    //recipientKeys = receiveIndexKeys(recipient, type, timestamp, recipientKeys, recipientUnconfirmedTransactionIndexName);

    Iterable recipientKeys(String recipient, long timestamp, int type) {
        return ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(
                indexByteableTuple3StringLongInteger.toBytes(new Fun.Tuple3<>(recipient, timestamp, type), null),
                ((DBRocksDBTable) map).list);
    }
    Iterable senderKeys(String recipient, long timestamp, int type) {
        return ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(
                indexByteableTuple3StringLongInteger.toBytes(new Fun.Tuple3<>(recipient, timestamp, type), null),
                ((org.erachain.dbs.rocksDB.TransactionRocksDBMap)map).getSenderIndex());
    }
    Iterable sendKeys(byte[] recipient) {
        return ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(recipient,
                ((org.erachain.dbs.rocksDB.TransactionRocksDBMap)map).getRecientIndex());
    }

    Iterable recipientKeys(String recipient) {
        return (Iterable) ((DBMapDB) map).getIterator(false);
    }


}
