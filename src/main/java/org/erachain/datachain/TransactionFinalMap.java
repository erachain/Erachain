package org.erachain.datachain;

import org.erachain.core.transaction.Transaction;
import org.erachain.database.FilteredByStringArray;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.utils.Pair;

import java.util.Collection;
import java.util.List;

public interface TransactionFinalMap extends DBTab<Long, Transaction>, FilteredByStringArray {
    @SuppressWarnings({"unchecked", "rawtypes"})
    void delete(Integer height);

    void delete(Integer height, Integer seq);

    void add(Integer height, Integer seq, Transaction transaction);

    Transaction get(Integer height, Integer seq);

    List<Transaction> getTransactionsByRecipient(String address);

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Transaction> getTransactionsByRecipient(String address, int limit);

    Collection<Transaction> getTransactionsByBlock(Integer block);

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Transaction> getTransactionsByBlock(Integer block, int offset, int limit);

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Transaction> getTransactionsBySender(String address, int limit, int offset);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    List<Transaction> getTransactionsByAddressAndType(String address, Integer type, int limit, int offset);

    List<Long> getTransactionsByAddressAndType(String address, Integer type, Long fromID, int limit, int offset);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    List<Transaction> getTransactionsByTitleAndType(String filter, Integer type, int limit, boolean descending);

    @SuppressWarnings({"unchecked", "rawtypes"})
    IteratorCloseable<Long> getKeysByTitleAndType(String filter, Integer type, int offset, int limit);

    Pair<Integer, IteratorCloseable<Long>> getKeysByFilterAsArrayRecurse(int step, String[] filterArray);

    @SuppressWarnings({"unchecked", "rawtypes"})
    Pair<String, IteratorCloseable<Long>> getKeysIteratorByFilterAsArray(String filter, int offset, int limit);

    // get list items in name substring str
    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Transaction> getByFilterAsArray(String filter, int offset, int limit);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    IteratorCloseable<Long> getIteratorByAddress(String address);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    List<Transaction> getTransactionsByAddressLimit(String address, int limit, boolean noForge);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    int getTransactionsByAddressCount(String address);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    Long getTransactionsAfterTimestamp(int startHeight, int numOfTx, String address);

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

    List<Transaction> getTransactionsByAddressFromID(String address, Long fromSeqNo, int offset, int limit, boolean noForge, boolean fillFullPage);

    byte[] getSignature(int hight, int seg);

    Transaction getRecord(String refStr);

    Transaction get(byte[] signature);

    void put(Transaction transaction);
}
