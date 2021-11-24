package org.erachain.dbs.mapDB;

//04/01 +- 

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.SignedBytes;
import lombok.extern.slf4j.Slf4j;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.*;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.dbs.IteratorParent;
import org.erachain.dbs.MergedOR_IteratorsNoDuplicates;
import org.mapdb.BTreeKeySerializer.BasicKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.Fun;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;

//import java.math.BigDecimal;

/**
 * Транзакции занесенные в цепочку
 * <p>
 * block.id + tx.ID in this block -> transaction
 * * <hr>
 * Здесь вторичные индексы создаются по несколько для одной записи путем создания массива ключей,
 * см. typeKey и recipientKey. Они используются для API RPC block explorer.
 * Нужно огрничивать размер выдаваемого списка чтобы не перегружать ноду.
 * <br>
 * Вторичные ключи:
 * ++ senderKey
 * ++ recipientKey
 * ++ typeKey
 * <hr>
 * (!!!) для создания уникальных ключей НЕ нужно добавлять + val.viewTimestamp(), и так работант, а почему в Ордерах не работало?
 * <br>в БИНДЕ внутри уникальные ключи создаются добавлением основного ключа
 */
@Slf4j
public class TransactionFinalSuitMapDBFork extends DBMapSuitFork<Long, Transaction>
        implements TransactionFinalSuit {

    @SuppressWarnings("rawtypes")
    private NavigableSet creatorKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet addressTypeKey;

    public TransactionFinalSuitMapDBFork(TransactionFinalMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, false, null);
    }

    @Override
    public void openMap() {

        // OPEN MAP
        // TREE MAP for sortable search
        map = database.createTreeMap("height_seq_transactions")
                .keySerializer(BasicKeySerializer.BASIC)
                //.keySerializer(BTreeKeySerializer.ZERO_OR_POSITIVE_LONG)
                .valueSerializer(new TransactionSerializer())
                .makeOrGet();

        // теперь это протокольный для множественных выплат
        Fun.Tuple2Comparator<byte[], Long> comparatorAddressT2 = new Fun.Tuple2Comparator<byte[], Long>(
                SignedBytes.lexicographicalComparator(),
                Fun.COMPARATOR);

        this.creatorKey = database.createTreeSet("creator_txs")
                .comparator(comparatorAddressT2) // - for Tuple2 String
                //.comparator(SignedBytes.lexicographicalComparator())
                .makeOrGet();

        // в БИНЕ внутри уникальные ключи создаются добавлением основного ключа
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.creatorKey, new Fun.Function2<byte[], Long, Transaction>() {
            @Override
            public byte[] run(Long key, Transaction transaction) {
                Account account = transaction.getCreator();
                if (account == null) {
                    /// так как вторичный ключ тут даже с Null будет создан как Tuple2(null, primaryKey) и
                    /// SignedBytes.lexicographicalComparator тогда вызывает ошибку - поэтому выдаем не пустой массив
                    return new byte[0];
                }

                byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];

                System.arraycopy(account.getShortAddressBytes(), 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                return addressKey;
            }
        });

        // теперь это протокольный для множественных выплат
        Fun.Tuple2Comparator<Fun.Tuple3Comparator<byte[], Integer, Boolean>, Long> comparatorAddressType
                = new Fun.Tuple2Comparator<Fun.Tuple3Comparator<byte[], Integer, Boolean>, Long>(
                new Fun.Tuple3Comparator(
                        SignedBytes.lexicographicalComparator(),
                        Fun.COMPARATOR,
                        Fun.COMPARATOR),
                Fun.COMPARATOR);
        this.addressTypeKey = database.createTreeSet("address_type_txs")
                .comparator(comparatorAddressType)
                .makeOrGet();

        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.addressTypeKey,
                new Fun.Function2<Fun.Tuple3<byte[], Integer, Boolean>[], Long, Transaction>() {
                    @Override
                    public Fun.Tuple3<byte[], Integer, Boolean>[] run(Long key, Transaction transaction) {
                        // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                        if (transaction.noDCSet()) {
                            transaction.setDC((DCSet) databaseSet, true);
                        }
                        List<Fun.Tuple3<byte[], Integer, Boolean>> accounts = new ArrayList<Fun.Tuple3<byte[], Integer, Boolean>>();
                        Integer type = transaction.getType();
                        for (Account account : transaction.getInvolvedAccounts()) {
                            byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
                            System.arraycopy(account.getShortAddressBytes(), 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                            accounts.add(new Fun.Tuple3<byte[], Integer, Boolean>(addressKey, type, account.equals(transaction.getCreator())));
                        }

                        Fun.Tuple3<byte[], Integer, Boolean>[] result = (Fun.Tuple3<byte[], Integer, Boolean>[])
                                Array.newInstance(Fun.Tuple3.class, accounts.size());
                        result = accounts.toArray(result);
                        return result;
                    }
                });

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
        IteratorCloseable<Long> iteratorForked;
        if (descending) {
            iteratorForked = new IteratorCloseableImpl(((BTreeMap<Long, Transaction>) map)
                    .subMap(Transaction.makeDBRef(height, 0),
                            Transaction.makeDBRef(height, Integer.MAX_VALUE)).keySet().descendingIterator());
        } else {
            iteratorForked = new IteratorCloseableImpl(((BTreeMap<Long, Transaction>) map)
                    .subMap(Transaction.makeDBRef(height, 0),
                            Transaction.makeDBRef(height, Integer.MAX_VALUE)).keySet().iterator());
        }

        // создаем с учетом удаленных
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iteratorForked),
                descending ? Fun.REVERSE_COMPARATOR : Fun.COMPARATOR);


    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        Iterator<Long> iterator = IteratorCloseableImpl.make(
                Fun.filter(descending ? this.creatorKey.descendingSet() : this.creatorKey, addressKey).iterator());

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent).getIteratorByCreator(addressShort, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iterator),
                descending ? Fun.REVERSE_COMPARATOR : Fun.COMPARATOR);

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        if (fromSeqNo == null) {
            fromSeqNo = descending ? Long.MAX_VALUE : Long.MIN_VALUE;
        }

        Iterator<Long> iterator = IteratorCloseableImpl.make(new IndexIterator((descending ? this.creatorKey.descendingSet() : this.creatorKey)
                .subSet(Fun.t2(addressKey, fromSeqNo),
                        Fun.t2(addressKey, descending ? Long.MIN_VALUE : Long.MAX_VALUE)).iterator()));

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent).getIteratorByCreator(addressShort, fromSeqNo, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iterator),
                descending ? Fun.REVERSE_COMPARATOR : Fun.COMPARATOR);

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo, Long toSeqNo, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        if (fromSeqNo == null) {
            fromSeqNo = descending ? Long.MAX_VALUE : Long.MIN_VALUE;
        }

        if (toSeqNo == null) {
            toSeqNo = descending ? Long.MIN_VALUE : Long.MAX_VALUE;
        }

        Iterator<Long> iterator = IteratorCloseableImpl.make(new IndexIterator((descending ? this.creatorKey.descendingSet() : this.creatorKey)
                .subSet(Fun.t2(addressKey, fromSeqNo),
                        Fun.t2(addressKey, toSeqNo)).iterator()));

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent).getIteratorByCreator(addressShort, fromSeqNo, toSeqNo, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iterator),
                descending ? Fun.REVERSE_COMPARATOR : Fun.COMPARATOR);

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
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        Iterator<Long> iterator = IteratorCloseableImpl.make(new IndexIterator((descending ? this.addressTypeKey.descendingSet() : this.addressTypeKey).subSet(
                Fun.t2(Fun.t3(addressKey, type, isCreator), descending ? Long.MAX_VALUE : Long.MIN_VALUE),
                Fun.t2(Fun.t3(addressKey,
                        type == 0 ? descending ? Integer.MIN_VALUE : Integer.MAX_VALUE : type,
                        isCreator == null ? descending ? Boolean.FALSE : Boolean.TRUE : isCreator
                ), descending ? Long.MIN_VALUE : Long.MAX_VALUE)).iterator()));

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent)
                .getIteratorByAddressAndType(addressShort, type, isCreator, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iterator),
                descending ? Fun.REVERSE_COMPARATOR : Fun.COMPARATOR);

    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, Long fromID, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        if (fromID == null) {
            fromID = descending ? Long.MAX_VALUE : Long.MIN_VALUE;
        }

        Iterator<Long> iterator = IteratorCloseableImpl.make(new IndexIterator((descending ? this.addressTypeKey.descendingSet() : this.addressTypeKey).subSet(
                Fun.t2(Fun.t3(addressKey, type, isCreator), fromID),
                Fun.t2(Fun.t3(addressKey,
                        type == 0 ? descending ? Integer.MIN_VALUE : Integer.MAX_VALUE : type,
                        isCreator == null ? descending ? Boolean.FALSE : Boolean.TRUE : isCreator
                ), descending ? Long.MIN_VALUE : Long.MAX_VALUE)).iterator()));

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent)
                .getIteratorByAddressAndType(addressShort, type, isCreator, fromID, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iterator),
                descending ? Fun.REVERSE_COMPARATOR : Fun.COMPARATOR);

    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, Long fromID, Long toID, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        if (fromID == null) {
            fromID = descending ? Long.MAX_VALUE : Long.MIN_VALUE;
        }

        if (toID == null) {
            toID = descending ? Long.MIN_VALUE : Long.MAX_VALUE;
        }

        Iterator<Long> iterator = IteratorCloseableImpl.make(new IndexIterator((descending ? this.addressTypeKey.descendingSet() : this.addressTypeKey).subSet(
                Fun.t2(Fun.t3(addressKey, type, isCreator), fromID),
                Fun.t2(Fun.t3(addressKey,
                        type == 0 ? descending ? Integer.MIN_VALUE : Integer.MAX_VALUE : type,
                        isCreator == null ? descending ? Boolean.FALSE : Boolean.TRUE : isCreator
                ), toID)).iterator()));

        IteratorCloseable<Long> parentIterator = ((TransactionFinalMap) parent)
                .getIteratorByAddressAndType(addressShort, type, isCreator, fromID, toID, descending);
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(parentIterator, deleted), iterator),
                descending ? Fun.REVERSE_COMPARATOR : Fun.COMPARATOR);

    }

    @Override
    public IteratorCloseable<Long> getIteratorByTitle(String filter, boolean asFilter, String fromWord, Long fromSeqNo, boolean descending) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> getAddressesIterator(byte[] addressShort, Long fromSeqNo, boolean descending) {
        return null;
    }
}
