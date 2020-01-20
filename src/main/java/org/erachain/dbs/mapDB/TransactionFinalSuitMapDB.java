package org.erachain.dbs.mapDB;

//04/01 +- 

import com.google.common.collect.*;
import com.google.common.primitives.SignedBytes;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IndexIterator;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.datachain.TransactionFinalSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.dbs.MergedIteratorNoDuplicates;
import org.erachain.utils.ReverseComparator;
import org.mapdb.BTreeKeySerializer.BasicKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

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
public class TransactionFinalSuitMapDB extends DBMapSuit<Long, Transaction> implements TransactionFinalSuit {

    public static final int BIDIRECTION_ADDRESS_INDEX = 1;
    private static int CUT_NAME_INDEX = 12;

    @SuppressWarnings("rawtypes")
    private NavigableSet creatorKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet recipientKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet addressTypeKey;

    @SuppressWarnings("rawtypes")
    private NavigableSet titleKey;

    public TransactionFinalSuitMapDB(DBASet databaseSet, DB database, boolean sizeEnable) {
        super(databaseSet, database, logger, sizeEnable, null);
    }

    @Override
    public void openMap() {

        // OPEN MAP
        // TREE MAP for sortable search
        DB.BTreeMapMaker mapConstruct = database.createTreeMap("height_seq_transactions")
                .keySerializer(BasicKeySerializer.BASIC)
                //.keySerializer(BTreeKeySerializer.ZERO_OR_POSITIVE_LONG)
                .valueSerializer(new TransactionSerializer());

        if (sizeEnable)
            mapConstruct = mapConstruct.counterEnable();

        map = mapConstruct.makeOrGet();

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        Fun.Tuple2Comparator<byte[], Long> comparatorAddressT2 = new Fun.Tuple2Comparator<byte[], Long>(
                SignedBytes.lexicographicalComparator(),
                Fun.COMPARATOR);

        this.creatorKey = database.createTreeSet("creator_txs")
                .comparator(comparatorAddressT2) // - for Tuple2 String
                //.comparator(SignedBytes.lexicographicalComparator())
                .makeOrGet();

        // в БИНЕ внутри уникальные ключи создаются добавлением основного ключа
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.creatorKey, new Function2<byte[], Long, Transaction>() {
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

        this.recipientKey = database.createTreeSet("recipient_txs")
                .comparator(comparatorAddressT2) // - for Tuple2 String
                //.comparator(SignedBytes.lexicographicalComparator())
                .makeOrGet();

        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.recipientKey,
                new Function2<byte[][], Long, Transaction>() {
                    @Override
                    public byte[][] run(Long key, Transaction transaction) {
                        // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                        transaction.setDC((DCSet) databaseSet);

                        HashSet<Account> recipients = transaction.getRecipientAccounts();
                        int size = recipients.size();
                        byte[][] keys = new byte[size][];
                        int count = 0;
                        for (Account recipient: recipients) {
                            byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
                            System.arraycopy(recipient.getShortAddressBytes(), 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                            keys[count++] = addressKey;
                        }
                        return keys;
                    }
                });

        Fun.Tuple2Comparator<Fun.Tuple2Comparator<byte[], Integer>, Long> comparatorAddressType
                = new Fun.Tuple2Comparator<Fun.Tuple2Comparator<byte[], Integer>, Long>(
                    new Fun.Tuple2Comparator(
                        SignedBytes.lexicographicalComparator(),
                        Fun.COMPARATOR),
                Fun.COMPARATOR);

        this.addressTypeKey = database.createTreeSet("address_type_txs")
                .comparator(comparatorAddressType)
                .makeOrGet();

        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.addressTypeKey,
                new Function2<Tuple2<byte[], Integer>[], Long, Transaction>() {
                    @Override
                    public Tuple2<byte[], Integer>[] run(Long key, Transaction transaction) {
                        List<Tuple2<byte[], Integer>> accounts = new ArrayList<Tuple2<byte[], Integer>>();
                        Integer type = transaction.getType();
                        for (Account account : transaction.getInvolvedAccounts()) {
                            byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
                            System.arraycopy(account.getShortAddressBytes(), 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                            accounts.add(new Tuple2<byte[], Integer>(addressKey, type));
                        }

                        Tuple2<byte[], Integer>[] result = (Tuple2<byte[], Integer>[])
                                Array.newInstance(Tuple2.class, accounts.size());
                        result = accounts.toArray(result);
                        return result;
                    }
                });

        this.titleKey = database.createTreeSet("title_type_txs").comparator(Fun.COMPARATOR).makeOrGet();

        // в БИНЕ внутри уникальные ключи создаются добавлением основного ключа
        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.titleKey,
                new Function2<String[], Long, Transaction>() {
                    @Override
                    public String[] run(Long key, Transaction transaction) {
                        String title = transaction.getTitle();
                        if (title == null || title.isEmpty() || title.equals("")) {
                            // нужно возвращать не null что бы сработал Компаратор нормально
                            return new String[0];
                        }

                        // see https://regexr.com/
                        String[] tokens = title.toLowerCase().split(DCSet.SPLIT_CHARS);
                        String[] keys = new String[tokens.length];
                        for (int i = 0; i < tokens.length; ++i) {
                            if (tokens[i].length() > CUT_NAME_INDEX) {
                                tokens[i] = tokens[i].substring(0, CUT_NAME_INDEX);
                            }
                            keys[i] = tokens[i];
                        }

                        return keys;
                    }
                });

        // BI-DIrectional INDEX - for blockexplorer
        NavigableSet<Integer> addressBiIndex = database.createTreeSet("address_txs")
                .comparator(comparatorAddressT2)
                .counterEnable()
                .makeOrGet();

        NavigableSet<Integer> descendingaddressBiIndex = database.createTreeSet("address_txs_descending")
                .comparator(new ReverseComparator(comparatorAddressT2))
                .makeOrGet();

        createIndexes(BIDIRECTION_ADDRESS_INDEX, addressBiIndex, descendingaddressBiIndex,
                new Fun.Function2<byte[][],
                Long, Transaction>() {
            @Override
            public byte[][] run(Long key, Transaction transaction) {
                HashSet<Account> accounts = transaction.getInvolvedAccounts();
                int size = accounts.size();
                byte[][] result = new byte[size][];
                int count = 0;
                for (Account account : accounts) {
                    byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
                    System.arraycopy(account.getShortAddressBytes(), 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

                    result[count++] = addressKey;
                }

                return result;
            }
        });

    }

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
         return IteratorCloseableImpl.make(((BTreeMap<Long, Transaction>) map)
                 .subMap(Transaction.makeDBRef(height, 0),
                         Transaction.makeDBRef(height, Integer.MAX_VALUE)).keySet().iterator());

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        Iterable keys = Fun.filter(this.recipientKey, addressKey);
        Iterator iter = keys.iterator();
        return IteratorCloseableImpl.make(iter);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        Iterable keys = Fun.filter(this.creatorKey, addressKey);
        Iterator iter = keys.iterator();
        return IteratorCloseableImpl.make(iter);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        return IteratorCloseableImpl.make(new IndexIterator(this.creatorKey.subSet(Fun.t2(addressKey, fromSeqNo),
                Fun.t2(addressKey, Fun.HI())).iterator()));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        Iterable keys = Fun.filter(this.addressTypeKey, new Tuple2<byte[], Integer>(addressKey, type));
        return IteratorCloseableImpl.make(keys.iterator());
    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndTypeFrom(byte[] addressShort, Integer type, Long fromID) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        return IteratorCloseableImpl.make(new IndexIterator(this.addressTypeKey.subSet(
                Fun.t2(Fun.t2(addressKey, type), fromID),
                Fun.t2(Fun.t2(addressKey, type), Fun.HI())).iterator()));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public IteratorCloseable<Long> getIteratorByTitle(String filter, boolean asFilter, Long fromSeqNo, boolean descending) {

        String filterLower = filter.toLowerCase();
        String filterLowerEnd;
        if (asFilter) {
            filterLowerEnd = filterLower + new String(255);
        } else {
            filterLowerEnd = filterLower;
        }

        if (descending) {
            return IteratorCloseableImpl.make(new IndexIterator(((NavigableSet) this.titleKey.subSet(
                    Fun.t2(filterLower, fromSeqNo == null || fromSeqNo == 0 ? Long.MIN_VALUE : fromSeqNo),
                    Fun.t2(filterLowerEnd, fromSeqNo == null || fromSeqNo == 0 ? Long.MAX_VALUE : fromSeqNo))).descendingIterator()));
        } else {
            return IteratorCloseableImpl.make(new IndexIterator(this.titleKey.subSet(
                    Fun.t2(filterLower, fromSeqNo == null || fromSeqNo == 0 ? Long.MIN_VALUE : fromSeqNo),
                    Fun.t2(filterLowerEnd, fromSeqNo == null || fromSeqNo == 0 ? Long.MAX_VALUE : fromSeqNo)).iterator()));
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    // скорость сортировки в том или ином случае может быть разная - нужны ТЕСТЫ на 3 варианта работы
    // TODO need benchmark tests
    public IteratorCloseable<Long> getIteratorByAddress(byte[] addressShort) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        if (true) {
            Iterable senderKeys = Fun.filter(this.creatorKey, addressKey);
            Iterable recipientKeys = Fun.filter(this.recipientKey, addressKey);

            if (true) {
                Set<Long> treeKeys = new TreeSet<>();

                treeKeys.addAll(Sets.newTreeSet(senderKeys));
                treeKeys.addAll(Sets.newTreeSet(recipientKeys));

                return new IteratorCloseableImpl(((TreeSet<Long>) treeKeys).descendingIterator());
            } else {
                // тут нельзя обратный КОМПАРАТОР REVERSE_COMPARATOR использоваьт ак как все перемешается
                Iterable<Long> mergedIterable = Iterables.mergeSorted((Iterable) ImmutableList.of(senderKeys, recipientKeys), Fun.COMPARATOR);
                // не удаляет дубли индексов return Lists.newLinkedList(mergedIterable).descendingIterator();
                // удалит дубли индексов и отсортирует - но тогда нен ужны
                return new IteratorCloseableImpl(Sets.newTreeSet(mergedIterable).descendingIterator());

            }
        } else {

            // ТУТ СОРТИООВКА не в ту тсорону получается

            Iterable senderKeys = Fun.filter(this.creatorKey, addressKey);
            Iterator<Long> senderKeysIterator = senderKeys.iterator();
            Iterable recipientKeys = Fun.filter(this.recipientKey, addressKey);
            Iterators.removeAll(senderKeysIterator, (Collection) recipientKeys);

            // заново возьмем итератор а тот прошелся весь до конца уже
            senderKeysIterator = senderKeys.iterator();
            Iterator<Long> recipientKeysIterator = recipientKeys.iterator();

            // тут нельзя обратный КОМПАРАТОР REVERSE_COMPARATOR использоваьт ак как все перемешается
            Iterator<Long> mergedIterator = new MergedIteratorNoDuplicates((Iterable) ImmutableList.of(senderKeysIterator, recipientKeysIterator), Fun.COMPARATOR);
            return new IteratorCloseableImpl(Lists.reverse(Lists.newArrayList(mergedIterator)).iterator());

        }
    }

    /**
     * Нужно для пролистывания по адресу в обоих направлениях - для блокэксплорера
     * TODO: тут ключ по адресу обрезан до 8-ми байт и возможны совпадения - поидее нужно увеличить длинну
     * @param addressShort
     * @param fromSeqNo
     * @param descending
     * @return
     */
    @Override
    public IteratorCloseable<Long> getBiDirectionAddressIterator(byte[] addressShort, Long fromSeqNo, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        if (descending) {
            IteratorCloseable result =
                // делаем закрываемый Итератор
                IteratorCloseableImpl.make(
                    // только ключи берем из Tuple2
                    new IndexIterator<>(
                        // берем индекс с обратным отсчетом
                        getIndex(BIDIRECTION_ADDRESS_INDEX, descending)
                            // задаем границы, так как он обратный границы меняем местами
                            .subSet(Fun.t2(addressKey, fromSeqNo == null || fromSeqNo.equals(0L)? Long.MAX_VALUE : fromSeqNo),
                                    Fun.t2(addressKey, 0L)).iterator()));
            return result;
        }

        IteratorCloseable result =
                // делаем закрываемый Итератор
                IteratorCloseableImpl.make(
                        // только ключи берем из Tuple2
                        new IndexIterator<>(
                                getIndex(BIDIRECTION_ADDRESS_INDEX, descending)
                                    // задаем границы, так как он обратный границы меняем местами
                                    .subSet(Fun.t2(addressKey, fromSeqNo == null? 0L : fromSeqNo),
                                            Fun.t2(addressKey, Long.MAX_VALUE)).iterator()));

        return result;
    }

}
