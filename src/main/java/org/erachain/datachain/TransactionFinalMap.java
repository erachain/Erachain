package org.erachain.datachain;

import org.erachain.core.transaction.Transaction;
import org.erachain.database.FilteredByStringArray;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;

import java.util.Collection;
import java.util.List;

public interface TransactionFinalMap extends DBTab<Long, Transaction>, FilteredByStringArray {

    /**
     * Длинна слова до котрого слово рассматриваем как "ПОИСК ПОЛНОСТЬЮ"
     */
    int WHOLE_WORLD_LENGTH = 5;
    /**
     * ограничение поиска размера списка для слова
     */
    int LIMIT_FIND_TITLE = 200;

    int CUT_NAME_INDEX = 12;
    int ADDRESS_KEY_LEN = 10;

    @SuppressWarnings({"unchecked", "rawtypes"})
    void delete(Integer height);

    void delete(Integer height, Integer seq);

    void add(Integer height, Integer seq, Transaction transaction);

    Transaction get(Integer height, Integer seq);

    List<Transaction> getTransactionsByRecipient(byte[] addressShort);

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Transaction> getTransactionsByRecipient(byte[] addressShort, int limit);

    Collection<Transaction> getTransactionsByBlock(Integer block);

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Transaction> getTransactionsByBlock(Integer block, int offset, int limit);

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Transaction> getTransactionsByCreator(byte[] addressShort, int limit, int offset);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    List<Transaction> getTransactionsByAddressAndType(byte[] addressShort, Integer type, int limit, int offset);

    List<Long> getKeysByAddressAndType(byte[] addressShort, Integer type, Long fromID, int limit, int offset);

    List<Transaction> getTransactionsByAddressAndType(byte[] addressShort, Integer type, Long fromID, int limit, int offset, boolean onlyCreator);

    List<Transaction> getTransactionsByTitleAndType(String filter, Integer type, int offset, int limit);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    IteratorCloseable<Long> getIteratorByAddress(byte[] addressShort);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    List<Transaction> getTransactionsByAddressLimit(byte[] addressShort, int limit, boolean noForge);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    int getTransactionsByAddressCount(byte[] addressShort);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    Long getTransactionsAfterTimestamp(int startHeight, int numOfTx, byte[] addressShort);

    @SuppressWarnings("rawtypes")
    List<Transaction> findTransactions(String address, String sender, String recipient, int minHeight,
                                       int maxHeight, int type, int service, boolean desc, int offset, int limit);

    @SuppressWarnings("rawtypes")
    int findTransactionsCount(String address, String sender, String recipient, int minHeight,
                              int maxHeight, int type, int service, boolean desc, int offset, int limit);

    @SuppressWarnings({"rawtypes", "unchecked"})
    IteratorCloseable<Long> findTransactionsKeys(String address, String sender, String recipient, int minHeight,
                                                 int maxHeight, int type, int service, boolean desc, int offset, int limit);

    IteratorCloseable<Long> getBiDirectionAddressIterator(String address, Long fromSeqNo, boolean descending, int offset, int limit);

    List<Transaction> getTransactionsByAddressFromID(byte[] addressShort, Long fromSeqNo, int offset, int limit, boolean noForge, boolean fillFullPage);

    byte[] getSignature(int hight, int seg);

    Transaction getRecord(String refStr);

    Transaction get(byte[] signature);

    void put(Transaction transaction);
}
