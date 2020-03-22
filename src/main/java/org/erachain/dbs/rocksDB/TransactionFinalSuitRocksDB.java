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
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private final String titleIndexName = "titleTypeTxs";

    SimpleIndexDB<Long, Transaction, byte[]> creatorTxs;
    ListIndexDB<Long, Transaction, byte[]> recipientTxs;
    ListIndexDB<Long, Transaction, byte[]> addressTypeTxs;
    ArrayIndexDB<Long, Transaction, byte[]> titleIndex;
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

        titleIndex = new ArrayIndexDB<>(titleIndexName,
                (aLong, transaction) -> {

                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    transaction.setDC((DCSet) databaseSet);

                    String[] tokens = transaction.getTags();
                    byte[][] keys = new byte[tokens.length][];
                    for (int i = 0; i < tokens.length; ++i) {
                        byte[] key = new byte[TransactionFinalMap.CUT_NAME_INDEX];
                        byte[] bytes = tokens[i].getBytes(StandardCharsets.UTF_8);
                        int keyLength = bytes.length;
                        if (keyLength >= TransactionFinalMap.CUT_NAME_INDEX) {
                            System.arraycopy(bytes, 0, key, 0, TransactionFinalMap.CUT_NAME_INDEX);
                        } else {
                            System.arraycopy(bytes, 0, key, 0, keyLength);
                        }
                        keys[i] = key;
                    }

                    return keys;
                }, (result) -> result);

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
        indexes.add(titleIndex);
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
                    ///key, null, false, true);
                    key, false, true); // as filter
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
    public IteratorCloseable<Long> getIteratorByTitle(String filter, boolean asFilter, String fromWord, Long fromSeqNo, boolean descending) {

        byte[] filterLower = filter.toLowerCase().getBytes(StandardCharsets.UTF_8);
        int filterLowerLength = Math.min(filterLower.length, TransactionFinalMap.CUT_NAME_INDEX);

        byte[] fromKey;
        byte[] toKey;

        if (fromWord == null) {
            // ищем все с самого начала для данного адреса
            //fromKey = new byte[filterLowerLength];
            //System.arraycopy(filterLower, 0, fromKey, 0, filterLowerLength);

            if (descending) {
                // тут нужно взять кранее верхнее значени и найти нижнее первое
                // см. https://github.com/facebook/rocksdb/wiki/SeekForPrev
                byte[] prevFilter;
                if (asFilter) {
                    // тут берем вообще все варианты
                    prevFilter = new byte[filterLowerLength + 1];
                    System.arraycopy(filterLower, 0, prevFilter, 0, filterLowerLength);
                    prevFilter[filterLowerLength] = (byte) 255;
                } else {
                    // тут берем только варианты обрезанные но со всеми номерами
                    prevFilter = new byte[TransactionFinalMap.CUT_NAME_INDEX + Long.BYTES];
                    System.arraycopy(filterLower, 0, prevFilter, 0, filterLowerLength);
                    System.arraycopy(Longs.toByteArray(Long.MAX_VALUE), 0, prevFilter, TransactionFinalMap.CUT_NAME_INDEX, Long.BYTES);
                    //prevFilter[filterLowerLength] = (byte) 255; // все что меньше не берем
                }

                return map.getIndexIteratorFilter(titleIndex.getColumnFamilyHandle(),
                        prevFilter, filterLower, descending, true);

            } else {

                if (asFilter) {
                    toKey = new byte[filterLowerLength + 1];
                    System.arraycopy(filterLower, 0, toKey, 0, filterLowerLength);
                    toKey[filterLowerLength] = (byte) 255;
                } else {
                    // тут берем только варианты обрезанные но со всеми номерами
                    toKey = new byte[TransactionFinalMap.CUT_NAME_INDEX + Long.BYTES];
                    System.arraycopy(filterLower, 0, toKey, 0, filterLowerLength);
                    System.arraycopy(Longs.toByteArray(Long.MAX_VALUE), 0, toKey, TransactionFinalMap.CUT_NAME_INDEX, Long.BYTES);
                }
                return map.getIndexIteratorFilter(titleIndex.getColumnFamilyHandle(),
                        filterLower, toKey, descending, true);

            }

        } else {

            // поиск ключа первого у всех одинаковый
            byte[] startFrom = fromWord.toLowerCase().getBytes(StandardCharsets.UTF_8);
            int startFromLength = Math.min(startFrom.length, TransactionFinalMap.CUT_NAME_INDEX);
            // используем полный ключ для начального поиска
            // значит и стартовое слово надо использовать
            fromKey = new byte[TransactionFinalMap.CUT_NAME_INDEX + Long.BYTES];
            System.arraycopy(startFrom, 0, fromKey, 0, startFromLength);

            if (descending) {

                // тут нужно взять кранее верхнее значени и найти нижнее первое
                // см. https://github.com/facebook/rocksdb/wiki/SeekForPrev
                /// учет на то что начальный ключ будет взят предыдущий от поиска + 1
                System.arraycopy(Longs.toByteArray(fromSeqNo // + 1L
                ), 0, fromKey, TransactionFinalMap.CUT_NAME_INDEX, Long.BYTES);

                if (asFilter) {
                    // тут берем вообще все варианты
                    toKey = filterLower;
                } else {
                    // тут берем только варианты обрезанные но со всеми номерами
                    toKey = new byte[TransactionFinalMap.CUT_NAME_INDEX + Long.BYTES];
                    System.arraycopy(filterLower, 0, toKey, 0, filterLowerLength);
                    System.arraycopy(Longs.toByteArray(0L), 0, toKey, TransactionFinalMap.CUT_NAME_INDEX, Long.BYTES);
                    toKey[filterLowerLength] = (byte) 255; // все что меньше не берем
                }

                return map.getIndexIteratorFilter(titleIndex.getColumnFamilyHandle(),
                        fromKey, toKey, descending, true);
            } else {

                System.arraycopy(Longs.toByteArray(fromSeqNo), 0, fromKey, TransactionFinalMap.CUT_NAME_INDEX, Long.BYTES);

                if (asFilter) {
                    // диаппазон заданим если у нас фильтр - значение начальное увеличим в нути ключа
                    toKey = new byte[filterLowerLength + 1];
                    System.arraycopy(filterLower, 0, toKey, 0, filterLowerLength);
                    toKey[filterLowerLength] = (byte) 255;
                } else {
                    toKey = new byte[TransactionFinalMap.CUT_NAME_INDEX + Long.BYTES];
                    System.arraycopy(filterLower, 0, toKey, 0, filterLowerLength);
                    System.arraycopy(Longs.toByteArray(Long.MAX_VALUE), 0, toKey, TransactionFinalMap.CUT_NAME_INDEX, Long.BYTES);
                }
                return map.getIndexIteratorFilter(titleIndex.getColumnFamilyHandle(),
                        fromKey, toKey, descending, true);
            }
        }
    }

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
    public IteratorCloseable<Long> getBiDirectionIterator(Long fromSeqNo, boolean descending) {
        byte[] fromKey;

        if (fromSeqNo == null || fromSeqNo == 0) {
            //fromKey = new byte[1]{descending ? Byte.MAX_VALUE : Byte.MIN_VALUE};
            fromKey = null;
        } else {
            // используем полный ключ для начального поиска
            fromKey = Longs.toByteArray(fromSeqNo);
        }
        return map.getIndexIteratorFilter(fromKey, null, descending, false);

    }

    @Override
    public IteratorCloseable<Long> getBiDirectionAddressIterator(byte[] addressShort, Long fromSeqNo, boolean descending) {

        if (addressShort == null)
            return getBiDirectionIterator(fromSeqNo, descending);

        byte[] fromKey;

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

    }

}
