package org.erachain.dbs.rocksDB;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.datachain.TransactionFinalSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.dbs.MergedIteratorNoDuplicates;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class TransactionFinalSuitRocksDB extends DBMapSuit<Long, Transaction> implements TransactionFinalSuit
{

    private final String NAME_TABLE = "TRANSACTION_FINAL_TABLE";
    private final String senderTransactionsIndexName = "senderTxs";
    private final String recipientTransactionsIndexName = "recipientTxs";
    private final String addressTypeTransactionsIndexName = "addressTypeTxs";
    private final String titleTypeTransactionsIndexName = "titleTypeTxs";

    SimpleIndexDB<Long, Transaction, byte[]> creatorTxs;
    ListIndexDB<Long, Transaction, byte[]> recipientTxs;
    ListIndexDB<Long, Transaction, byte[]> addressTypeTxs;
    ArrayIndexDB<Long, Transaction, Fun.Tuple2<String, Integer>> titleTypeTxs;
    ListIndexDB<Long, Transaction, byte[]> addressBiDirectionTxs;

    public TransactionFinalSuitRocksDB(DBASet databaseSet, DB database, boolean sizeEnable) {
        super(databaseSet, database, logger, sizeEnable);
    }

    @Override
    public void openMap() {

        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(),
                databaseSet, sizeEnable);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {

        // USE counter index
        indexes = new ArrayList<>();

        if (Controller.getInstance().onlyProtocolIndexing) {
            return;
        }

        creatorTxs = new SimpleIndexDB<>(senderTransactionsIndexName,
                (aLong, transaction) -> {
                    Account account = transaction.getCreator();
                    if (account == null)
                        return null;
                    byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
                    System.arraycopy(account.getShortAddressBytes(), 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                    return addressKey;
                }, (result) -> result);

        recipientTxs = new ListIndexDB<>(recipientTransactionsIndexName,
                (Long aLong, Transaction transaction) -> {
                    List<byte[]> recipients = new ArrayList<>();

                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    transaction.setDC((DCSet) databaseSet);

                    for (Account account : transaction.getRecipientAccounts()) {
                        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
                        System.arraycopy(account.getShortAddressBytes(), 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

                        recipients.add(addressKey);
                    }
                    return recipients;
                }, (result) -> result);

        addressTypeTxs = new ListIndexDB<>(addressTypeTransactionsIndexName,
                (aLong, transaction) -> {

                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    transaction.setDC((DCSet) databaseSet);

                    Integer type = transaction.getType();
                    List<byte[]> addressesTypes = new ArrayList<>();
                    for (Account account : transaction.getInvolvedAccounts()) {
                        byte[] key = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 1];
                        System.arraycopy(account.getShortAddressBytes(), 0, key, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                        key[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte) (int) type;
                        addressesTypes.add(key);
                    }
                    return addressesTypes;
                },
                (result) -> result);

        titleTypeTxs = new ArrayIndexDB<>(titleTypeTransactionsIndexName,
                (aLong, transaction) -> {

                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    transaction.setDC((DCSet) databaseSet);

                    String title = transaction.getTitle();
                    if (title == null || title.isEmpty() || title.equals(""))
                        return null;

                    // see https://regexr.com/
                    String[] tokens = title.toLowerCase().split(DCSet.SPLIT_CHARS);
                    Tuple2<String, Integer>[] keys = new Tuple2[tokens.length];
                    for (int i = 0; i < tokens.length; ++i) {
                        if (tokens[i].length() > TransactionFinalMap.CUT_NAME_INDEX) {
                            tokens[i] = tokens[i].substring(0, TransactionFinalMap.CUT_NAME_INDEX);
                        }
                        //keys[i] = tokens[i];
                        keys[i] = new Tuple2<String, Integer>(tokens[i], transaction.getType());
                    }

                    return keys;
                }, (result) -> {
            if (result == null) {
                return null;
            }
            return org.bouncycastle.util.Arrays.concatenate(result.a.getBytes(), Ints.toByteArray(result.b));
        }
        );

        addressBiDirectionTxs = new ListIndexDB<>("addressBiDirectionTXIndex",
                (aLong, transaction) -> {

                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    transaction.setDC((DCSet) databaseSet);

                    List<byte[]> secondaryKeys = new ArrayList<>();
                    for (Account account : transaction.getInvolvedAccounts()) {
                        byte[] secondaryKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
                        System.arraycopy(account.getShortAddressBytes(), 0, secondaryKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                        ////System.arraycopy(transaction.getDBRefAsBytes(), 0, secondaryKey, ADDRESS_KEY_LEN, 8);

                        secondaryKeys.add(secondaryKey);
                    }
                    return secondaryKeys;
                },
                (result) -> result
        );

        indexes.add(creatorTxs);
        indexes.add(recipientTxs);
        indexes.add(addressTypeTxs);
        indexes.add(titleTypeTxs);
        indexes.add(addressBiDirectionTxs);

    }

    // TODO  dbCore.deleteRange(beg, end);
    @Override
    public void deleteForBlock(Integer height) {
        try (IteratorCloseable<Long> iterator = getBlockIterator(height)) {
            while (iterator.hasNext()) {
                map.remove(iterator.next());
            }
        } catch (IOException e) {
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getBlockIterator(Integer height) {
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        //map.getIterator();
        return map.getIndexIteratorFilter(Ints.toByteArray(height), false, false);

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        return map.getIndexIteratorFilter(creatorTxs.getColumnFamilyHandle(), addressKey, false, true);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo) {

        if (fromSeqNo == null) {
            byte[] fromKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
            System.arraycopy(addressShort, 0, fromKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
            return map.getIndexIteratorFilter(creatorTxs.getColumnFamilyHandle(), fromKey, false, true);
        }

        byte[] fromKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + Long.BYTES];
        System.arraycopy(addressShort, 0, fromKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        System.arraycopy(Longs.toByteArray(fromSeqNo), 0, fromKey, TransactionFinalMap.ADDRESS_KEY_LEN, Long.BYTES);

        return map.getIndexIteratorFilter(creatorTxs.getColumnFamilyHandle(), fromKey, null, false, true);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        return map.getIndexIteratorFilter(recipientTxs.getColumnFamilyHandle(), addressKey, false, true);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type) {
        byte[] key = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 1];
        System.arraycopy(addressShort, 0, key, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        key[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte)(int) type;
        return map.getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(), key, false, true);
    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndTypeFrom(byte[] addressShort, Integer type, Long fromID) {
        if (fromID == null) {
            byte[] key = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 1];
            System.arraycopy(addressShort, 0, key, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
            key[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte) (int) type;
            return map.getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(),
                    key, null, false, true);
        }

        byte[] keyFrom = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 1 + Long.BYTES];
        System.arraycopy(addressShort, 0, keyFrom, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        keyFrom[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte) (int) type;
        System.arraycopy(Longs.toByteArray(fromID), 0, keyFrom, TransactionFinalMap.ADDRESS_KEY_LEN + 1, Long.BYTES);

        byte[] keyTo = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + 1 + Long.BYTES];
        System.arraycopy(addressShort, 0, keyFrom, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
        keyFrom[TransactionFinalMap.ADDRESS_KEY_LEN] = (byte) (int) type;
        System.arraycopy(Longs.toByteArray(Long.MAX_VALUE), 0, keyFrom, TransactionFinalMap.ADDRESS_KEY_LEN + 1, Long.BYTES);

        return map.getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(),
                keyFrom, keyTo, false, true);

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public IteratorCloseable<Long> getIteratorByTitle(String filter, boolean asFilter, Long fromSeqNo, boolean descending) {

        String filterLower = filter.toLowerCase();
        //Iterable keys = Fun.filter(this.titleKey,
        //        new Tuple2<String, Integer>(filterLower,
        //                type==0?0:type), true,
        //        new Tuple2<String, Integer>(asFilter?
        //                filterLower + new String(new byte[]{(byte)255}) : filterLower,
        //                type==0?Integer.MAX_VALUE:type), true);

        return map.getIndexIteratorFilter(titleTypeTxs.getColumnFamilyHandle(), fromSeqNo == null || fromSeqNo == 0 ? filterLower.getBytes()
                        : Arrays.concatenate(filterLower.getBytes(), Longs.toByteArray(fromSeqNo)),
                descending, true);

    }

    // TODO сделать просто итератор складной - без создания списков и дубляжей в итераторе
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByAddress(byte[] addressShort) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        Iterator senderKeys = map.getIndexIteratorFilter(creatorTxs.getColumnFamilyHandle(), addressKey, false, true);
        Iterator recipientKeys = map.getIndexIteratorFilter(recipientTxs.getColumnFamilyHandle(), addressKey, false, true);

        // тут нельзя обратный КОМПАРАТОР REVERSE_COMPARATOR использоваьт ак как все перемешается
        Iterator<Long> mergedIterator = new MergedIteratorNoDuplicates((Iterable) ImmutableList.of(senderKeys, recipientKeys), Fun.COMPARATOR);

        // а тут уже оьбратный порядок дать
        return new IteratorCloseableImpl(Lists.reverse(Lists.newArrayList(mergedIterator)).iterator());

    }

    @Override
    public IteratorCloseable<Long> getBiDirectionAddressIterator(byte[] addressShort, Long fromSeqNo, boolean descending) {
        byte[] fromKey;
        if (addressShort != null) {
            if (fromSeqNo == null || fromSeqNo == 0) {
                // ищем все с самого начала для данного адреса
                fromKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
                System.arraycopy(addressShort, 0, fromKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
            } else {
                // используем полный ключ для начального поиска
                fromKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN + Long.BYTES];
                System.arraycopy(addressShort, 0, fromKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                System.arraycopy(Longs.toByteArray(fromSeqNo), 0, fromKey, TransactionFinalMap.ADDRESS_KEY_LEN, Long.BYTES);
            }

            byte[] toKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
            System.arraycopy(addressShort, 0, toKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

            if (descending && fromSeqNo == null) {
                // тут нужно взять кранее верхнее значени и найти нижнее первое
                // см. https://github.com/facebook/rocksdb/wiki/SeekForPrev
                int length = fromKey.length;
                byte[] prevFilter = new byte[length + 1];
                System.arraycopy(fromKey, 0, prevFilter, 0, length);
                prevFilter[length] = (byte) 255;

                //toKey =
                return map.getIndexIteratorFilter(addressBiDirectionTxs.getColumnFamilyHandle(),
                        prevFilter, toKey, descending, true);

            } else {
                return map.getIndexIteratorFilter(addressBiDirectionTxs.getColumnFamilyHandle(),
                        fromKey, toKey, descending, true);

            }

        } else {
            return map.getIndexIterator(addressBiDirectionTxs.getColumnFamilyHandle(), descending, true);
        }


    }

}
