package org.erachain.dbs.rocksDB;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.datachain.TransactionFinalMapImpl;
import org.erachain.datachain.TransactionFinalSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorParent;
import org.erachain.dbs.MergedOR_IteratorsNoDuplicates;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDB;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.mapdb.Fun;
import org.rocksdb.WriteOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class TransactionFinalSuitRocksDBFork extends DBMapSuitFork<Long, Transaction> implements TransactionFinalSuit {

    private final String senderTransactionsIndexName = "senderTxs";
    private final String addressTypeTransactionsIndexName = "addressTypeTxs";

    SimpleIndexDB<Long, Transaction, byte[]> creatorTxs;
    /**
     * С учетом Создатель или Получатель (1 или 0)
     */
    ListIndexDB<Long, Transaction, byte[]> addressTypeTxs;

    public TransactionFinalSuitRocksDBFork(TransactionFinalMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, false, null);
    }

    @Override
    public void openMap() {

        map = new DBRocksDBTableDB<>(new ByteableLong(), new ByteableTransaction(), indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false), sizeEnable);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {

        // USE counter index
        indexes = new ArrayList<>();

        // теперь это протокольный для множественных выплат
        creatorTxs = new SimpleIndexDB<>(senderTransactionsIndexName,
                (aLong, transaction) -> {
                    Account account = transaction.getCreator();
                    if (account == null)
                        return null;
                    byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
                    System.arraycopy(account.getShortAddressBytes(), 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                    return addressKey;
                }, (result) -> result);
        indexes.add(creatorTxs);

        // теперь это протокольный для множественных выплат
        addressTypeTxs = new ListIndexDB<>(addressTypeTransactionsIndexName,
                (aLong, transaction) -> {

                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    if (transaction.noDCSet()) {
                        transaction.setDC((DCSet) databaseSet, true);
                    }

                    Integer type = transaction.getType();
                    List<byte[]> addressesTypes = new ArrayList<>();
                    for (Account account : transaction.getInvolvedAccounts()) {
                        byte[] key = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 2];
                        System.arraycopy(account.getShortAddressBytes(), 0, key, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                        key[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte) (int) type;
                        key[TransactionFinalMap.ADDRESS_KEY_LEN + 1] = (byte) (account.equals(transaction.getCreator()) ? 1 : 0);
                        addressesTypes.add(key);
                    }
                    return addressesTypes;
                },
                (result) -> result);
        indexes.add(addressTypeTxs);

    }

    @Override
    public void deleteForBlock(Integer height) {
        try (IteratorCloseable<Long> iterator = getOneBlockIterator(height, true)) {
            while (iterator.hasNext()) {
                map.remove(iterator.next());
            }
        } catch (IOException e) {
        }
    }

    @Override
    public IteratorCloseable<Long> getOneBlockIterator(Integer height, boolean descending) {

        // берем из родителя
        IteratorCloseable<Long> parentIterator = ((TransactionFinalMapImpl) parent).getOneBlockIterator(height, descending);

        // берем свои - форкнутые
        IteratorCloseable<Long> iteratorForked = map.getIndexIteratorFilter(Ints.toByteArray(height), descending, false);

        // создаем с учетом удаленных
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iteratorForked), Fun.COMPARATOR);

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        Iterator<Long> iterator = map.getIndexIteratorFilter(creatorTxs.getColumnFamilyHandle(), addressKey, descending, true);

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent).getIteratorByCreator(addressShort, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted),
                iterator),
                Fun.COMPARATOR);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo, boolean descending) {

        if (fromSeqNo == null) {
            byte[] fromKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
            System.arraycopy(addressShort, 0, fromKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
            return map.getIndexIteratorFilter(creatorTxs.getColumnFamilyHandle(), fromKey, descending, true);
        }

        byte[] fromKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + Long.BYTES];
        System.arraycopy(addressShort, 0, fromKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        System.arraycopy(Longs.toByteArray(fromSeqNo), 0, fromKey, TransactionFinalMap.ADDRESS_KEY_LEN, Long.BYTES);

        Iterator<Long> iterator = map.getIndexIteratorFilter(creatorTxs.getColumnFamilyHandle(), fromKey, null, descending, true);

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent).getIteratorByCreator(addressShort, fromSeqNo, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted),
                iterator),
                Fun.COMPARATOR);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo, Long toSeqNo, boolean descending) {

        byte[] fromKey;
        if (fromSeqNo == null) {
            fromKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
            System.arraycopy(addressShort, 0, fromKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        } else {
            fromKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + Long.BYTES];
            System.arraycopy(addressShort, 0, fromKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
            System.arraycopy(Longs.toByteArray(fromSeqNo), 0, fromKey, TransactionFinalMap.ADDRESS_KEY_LEN, Long.BYTES);
        }

        byte[] toKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + Long.BYTES];
        System.arraycopy(addressShort, 0, toKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        System.arraycopy(Longs.toByteArray(toSeqNo == null ? descending ? 0L : Long.MAX_VALUE : toSeqNo), 0, toKey, TransactionFinalMap.ADDRESS_KEY_LEN, Long.BYTES);

        Iterator<Long> iterator = map.getIndexIteratorFilter(creatorTxs.getColumnFamilyHandle(),
                fromKey, toKey, descending, true);

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent).getIteratorByCreator(addressShort, fromSeqNo, toSeqNo, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted),
                iterator),
                Fun.COMPARATOR);
    }

    @Override
    public IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort, boolean descending) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort, Long fromSeqNo, boolean descending) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort, Long fromSeqNo, Long toSeqNo, boolean descending) {
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, boolean descending) {
        byte[] key;
        if (type != null && type != 0) {
            if (isCreator != null) {
                key = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 2];
                System.arraycopy(addressShort, 0, key, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                key[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte) (int) type;
                key[TransactionFinalMap.ADDRESS_KEY_LEN + 1] = (byte) (isCreator ? 1 : 0);
            } else {
                key = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 1];
                System.arraycopy(addressShort, 0, key, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                key[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte) (int) type;
            }
        } else {
            key = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
            System.arraycopy(addressShort, 0, key, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        }
        Iterator<Long> iterator = map.getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(), key, descending, true);

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent).getIteratorByAddressAndType(addressShort, type, isCreator, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted),
                iterator),
                Fun.COMPARATOR);

    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, Long fromID, boolean descending) {
        if (fromID == null || isCreator == null || type == null) {
            return getIteratorByAddressAndType(addressShort, type, isCreator, descending);
        }

        byte[] keyFrom = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 2 + Long.BYTES];
        System.arraycopy(addressShort, 0, keyFrom, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        keyFrom[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte) (int) type;
        keyFrom[TransactionFinalMap.ADDRESS_KEY_LEN + 1] = (byte) (isCreator ? 1 : 0);
        System.arraycopy(Longs.toByteArray(fromID), 0, keyFrom, TransactionFinalMap.ADDRESS_KEY_LEN + 2, Long.BYTES);

        byte[] keyTo = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 2 + Long.BYTES];
        System.arraycopy(addressShort, 0, keyTo, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        keyTo[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte) (int) type;
        keyTo[TransactionFinalMap.ADDRESS_KEY_LEN + 1] = (byte) (isCreator ? 1 : 0);
        System.arraycopy(Longs.toByteArray(descending ? 0L : Long.MAX_VALUE),
                0, keyTo, TransactionFinalMap.ADDRESS_KEY_LEN + 2, Long.BYTES);

        Iterator<Long> iterator = map.getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(),
                keyFrom, keyTo, descending, true);

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent).getIteratorByAddressAndType(
                addressShort, type, isCreator, fromID, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted),
                iterator),
                Fun.COMPARATOR);

    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, Long fromID, Long toID, boolean descending) {
        if (fromID == null || isCreator == null || type == null) {
            return getIteratorByAddressAndType(addressShort, type, isCreator, descending);
        }

        byte[] keyFrom = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 2 + Long.BYTES];
        System.arraycopy(addressShort, 0, keyFrom, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        keyFrom[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte) (int) type;
        keyFrom[TransactionFinalMap.ADDRESS_KEY_LEN + 1] = (byte) (isCreator ? 1 : 0);
        System.arraycopy(Longs.toByteArray(fromID), 0, keyFrom, TransactionFinalMap.ADDRESS_KEY_LEN + 2, Long.BYTES);

        byte[] keyTo = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 2 + Long.BYTES];
        System.arraycopy(addressShort, 0, keyTo, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        keyTo[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte) (int) type;
        keyTo[TransactionFinalMap.ADDRESS_KEY_LEN + 1] = (byte) (isCreator ? 1 : 0);
        System.arraycopy(Longs.toByteArray(toID == null ? descending ? 0L : Long.MAX_VALUE : toID),
                0, keyTo, TransactionFinalMap.ADDRESS_KEY_LEN + 2, Long.BYTES);

        Iterator<Long> iterator = map.getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(),
                keyFrom, keyTo, descending, true);

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent).getIteratorByAddressAndType(
                addressShort, type, isCreator, fromID, toID, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted),
                iterator),
                Fun.COMPARATOR);

    }

    @Override
    public IteratorCloseable<Long> getIteratorByTitle(String filter, boolean asFilter, String fromWord, Long fromSeqNo, boolean descending) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getBiDirectionIterator_old(Long fromSeqNo, boolean descending) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getBiDirectionAddressIterator(byte[] addressShort, Long fromSeqNo, boolean descending) {
        return null;
    }

}
