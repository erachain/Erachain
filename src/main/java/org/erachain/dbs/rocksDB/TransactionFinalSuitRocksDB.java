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

    private static final int CUT_NAME_INDEX = 12;
    private final String NAME_TABLE = "TRANSACTION_FINAL_TABLE";
    private final String senderTransactionsIndexName = "senderTxs";
    private final String recipientTransactionsIndexName = "recipientTxs";
    private final String addressTypeTransactionsIndexName = "addressTypeTxs";
    private final String titleTypeTransactionsIndexName = "titleTypeTxs";


    SimpleIndexDB<Long, Transaction, String> senderTxs;
    ListIndexDB<Long, Transaction, String> recipientTxs;
    ListIndexDB<Long, Transaction, Fun.Tuple2<String, Integer>> addressTypeTxs;
    ArrayIndexDB<Long, Transaction, Fun.Tuple2<String, Integer>> titleTypeTxs;

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

        senderTxs = new SimpleIndexDB<>(senderTransactionsIndexName,
                (aLong, transaction) -> {
                    Account account = transaction.getCreator();
                    return (account == null ? "genesis" : account.getAddress());
                }, (result) -> result.getBytes());

        recipientTxs = new ListIndexDB<>(recipientTransactionsIndexName,
                (Long aLong, Transaction transaction) -> {
                    List<String> recipients = new ArrayList<>();

                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    transaction.setDC((DCSet) databaseSet);

                    for (Account account : transaction.getRecipientAccounts()) {
                        recipients.add(account.getAddress());
                    }
                    return recipients;
                }, (result) -> result.getBytes());

        addressTypeTxs = new ListIndexDB<>(addressTypeTransactionsIndexName,
                (aLong, transaction) -> {

                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    transaction.setDC((DCSet) databaseSet);

                    Integer type = transaction.getType();
                    List<Tuple2<String, Integer>> addressesTypes = new ArrayList<>();
                    for (Account account : transaction.getInvolvedAccounts()) {
                        addressesTypes.add(new Tuple2<>(account.getAddress(), type));
                    }
                    return addressesTypes;
                },
                (result) -> {
                    if (result == null) {
                        return null;
                    }
                    return org.bouncycastle.util.Arrays.concatenate(result.a.getBytes(), Ints.toByteArray(result.b));
                }
        );

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
                        if (tokens[i].length() > CUT_NAME_INDEX) {
                            tokens[i] = tokens[i].substring(0, CUT_NAME_INDEX);
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

        indexes.add(senderTxs);
        indexes.add(recipientTxs);
        indexes.add(addressTypeTxs);
        indexes.add(titleTypeTxs);
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
    public IteratorCloseable<Long> getIteratorBySender(String address) {
        //return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(senderTxs.getColumnFamilyHandle(), address.getBytes(), false);
        return map.getIndexIteratorFilter(senderTxs.getColumnFamilyHandle(), address.getBytes(), false, true);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByRecipient(String address) {
        return map.getIndexIteratorFilter(recipientTxs.getColumnFamilyHandle(), address.getBytes(), false, true);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public IteratorCloseable<Long> getIteratorByAddressAndType(String address, Integer type) {
        ///return (Iterator) ((DBRocksDBTable) map).getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(), Arrays.concatenate(address.getBytes(), Ints.toByteArray(type)), false);
        return map.getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(), Arrays.concatenate(address.getBytes(), Ints.toByteArray(type)), false, true);
    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndTypeFrom(String address, Integer type, Long fromID) {
        if (fromID != null)
            return map.getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(),
                    Arrays.concatenate(address.getBytes(), Ints.toByteArray(type), Longs.toByteArray(fromID)),
                    Arrays.concatenate(address.getBytes(), Ints.toByteArray(type), Longs.toByteArray(Long.MAX_VALUE)), false, true);
        return map.getIndexIteratorFilter(addressTypeTxs.getColumnFamilyHandle(),
                Arrays.concatenate(address.getBytes(), Ints.toByteArray(type)), null, false, true);

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public IteratorCloseable<Long> getIteratorByTitleAndType(String filter, boolean asFilter, Integer type) {

        String filterLower = filter.toLowerCase();
        //Iterable keys = Fun.filter(this.titleKey,
        //        new Tuple2<String, Integer>(filterLower,
        //                type==0?0:type), true,
        //        new Tuple2<String, Integer>(asFilter?
        //                filterLower + new String(new byte[]{(byte)255}) : filterLower,
        //                type==0?Integer.MAX_VALUE:type), true);

        return map.getIndexIteratorFilter(titleTypeTxs.getColumnFamilyHandle(), type == 0 ? filter.getBytes()
                        : Arrays.concatenate(filter.getBytes(), Ints.toByteArray(type))
                , false, true);

    }

    // TODO сделать просто итератор складной - без создания списков и дубляжей в итераторе
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByAddress(String address) {
        Iterator senderKeys = map.getIndexIteratorFilter(senderTxs.getColumnFamilyHandle(), address.getBytes(), false, true);
        Iterator recipientKeys = map.getIndexIteratorFilter(recipientTxs.getColumnFamilyHandle(), address.getBytes(), false, true);
        //return Iterators.concat(senderKeys, recipientKeys);

        // тут нельзя обратный КОМПАРАТОР REVERSE_COMPARATOR использоваьт ак как все перемешается
        Iterator<Long> mergedIterator = new MergedIteratorNoDuplicates((Iterable) ImmutableList.of(senderKeys, recipientKeys), Fun.COMPARATOR);

        // а тут уже оьбратный порядок дать
        return new IteratorCloseableImpl(Lists.reverse(Lists.newArrayList(mergedIterator)).iterator());

    }

}
