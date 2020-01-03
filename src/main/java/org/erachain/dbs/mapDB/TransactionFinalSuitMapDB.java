package org.erachain.dbs.mapDB;

//04/01 +- 

import com.google.common.collect.*;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.DCSet;
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
    private NavigableSet senderKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet recipientKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet addressTypeKey;

    @SuppressWarnings("rawtypes")
    private NavigableSet titleKey;

    //@SuppressWarnings("rawtypes")
    //private NavigableSet block_Key;
    // private NavigableSet <Tuple2<String,Tuple2<Integer,
    // Integer>>>signature_key;

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

        this.senderKey = database.createTreeSet("sender_txs").comparator(Fun.COMPARATOR).makeOrGet();

        // в БИНЕ внутри уникальные ключи создаются добавлением основного ключа
        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.senderKey, new Function2<String, Long, Transaction>() {
            @Override
            public String run(Long key, Transaction val) {
                Account account = val.getCreator();
                if (account == null)
                    return "";
                // make UNIQUE key??  + val.viewTimestamp()
                return account.getAddress();
            }
        });

        this.recipientKey = database.createTreeSet("recipient_txs").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.recipientKey,
                new Function2<String[], Long, Transaction>() {
                    @Override
                    public String[] run(Long key, Transaction val) {
                        List<String> recps = new ArrayList<String>();

                        // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                        val.setDC((DCSet) databaseSet);

                        for (Account acc : val.getRecipientAccounts()) {
                            // make UNIQUE key??  + val.viewTimestamp()
                            recps.add(acc.getAddress());
                        }
                        String[] ret = new String[recps.size()];
                        ret = recps.toArray(ret);
                        return ret;
                    }
                });

        this.addressTypeKey = database.createTreeSet("address_type_txs").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.addressTypeKey,
                new Function2<Tuple2<String, Integer>[], Long, Transaction>() {
                    @Override
                    public Tuple2<String, Integer>[] run(Long key, Transaction transaction) {
                        List<Tuple2<String, Integer>> accounts = new ArrayList<Tuple2<String, Integer>>();
                        Integer type = transaction.getType();
                        for (Account acc : transaction.getInvolvedAccounts()) {
                            // TODO: make unique key??  + transaction.viewTimestamp()
                            accounts.add(new Tuple2<String, Integer>(acc.getAddress(), type));
                        }

                        Tuple2<String, Integer>[] result = (Tuple2<String, Integer>[])
                                Array.newInstance(Tuple2.class, accounts.size());
                        result = accounts.toArray(result);
                        return result;
                    }
                });

        this.titleKey = database.createTreeSet("title_type_txs").comparator(Fun.COMPARATOR).makeOrGet();

        // в БИНЕ внутри уникальные ключи создаются добавлением основного ключа
        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.titleKey,
                new Function2<Tuple2<String, Integer>[], Long, Transaction>() {
                    @Override
                    public Tuple2<String, Integer>[] run(Long key, Transaction val) {
                        String title = val.getTitle();
                        if (title == null || title.isEmpty() || title.equals(""))
                            return null;

                        // see https://regexr.com/
                        String[] tokens = title.toLowerCase().split(DCSet.SPLIT_CHARS);
                        Tuple2<String, Integer>[] keys = new Tuple2[tokens.length];
                        for (int i = 0; i < tokens.length; ++i) {
                            if (tokens[i].length() > CUT_NAME_INDEX) {
                                tokens[i] = tokens[i].substring(0, CUT_NAME_INDEX);
                            }
                            keys[i] = new Tuple2<String, Integer>(tokens[i], val.getType());
                        }

                        return keys;
                    }
                });

        // BI-DIrectional INDEX - for blockexplorer
        NavigableSet<Integer> addressBiIndex = database.createTreeSet("address_txs")
                .comparator(Fun.TUPLE2_COMPARATOR)
                .counterEnable()
                .makeOrGet();

        NavigableSet<Integer> descendingaddressBiIndex = database.createTreeSet("address_txs_descending")
                .comparator(new ReverseComparator(Fun.TUPLE2_COMPARATOR))
                .makeOrGet();

        createIndex(BIDIRECTION_ADDRESS_INDEX, addressBiIndex, descendingaddressBiIndex,
                new Fun.Function2<Tuple2<Long, Long>[],
                Long, Transaction>() {
            @Override
            public Tuple2<Long, Long>[] run(Long key, Transaction transaction) {
                List<Tuple2<Long, Long>> accounts = new ArrayList<Tuple2<Long, Long>>();
                for (Account acc : transaction.getInvolvedAccounts()) {
                    // TODO: make unique key??  + val.viewTimestamp()
                    accounts.add(new Tuple2<Long, Long>(Longs.fromByteArray(transaction.getCreator().getShortAddressBytes()),
                            transaction.getDBRef()));
                }

                Tuple2<Long, Long>[] result = (Tuple2<Long, Long>[])
                        Array.newInstance(Tuple2.class, accounts.size());
                result = accounts.toArray(result);
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
         return new IteratorCloseableImpl(((BTreeMap<Long, Transaction>) map)
                .subMap(Transaction.makeDBRef(height, 0),
                        Transaction.makeDBRef(height, Integer.MAX_VALUE)).keySet().iterator());

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByRecipient(String address) {
        Iterable keys = Fun.filter(this.recipientKey, address);
        Iterator iter = keys.iterator();
        return new IteratorCloseableImpl(iter);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorBySender(String address) {
        Iterable keys = Fun.filter(this.senderKey, address);
        Iterator iter = keys.iterator();
        return new IteratorCloseableImpl(iter);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public IteratorCloseable<Long> getIteratorByAddressAndType(String address, Integer type) {
        Iterable keys = Fun.filter(this.addressTypeKey, new Tuple2<String, Integer>(address, type));
        Iterator iter = keys.iterator();
        return new IteratorCloseableImpl(iter);
    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndTypeFrom(String address, Integer type, Long fromID) {

        return IteratorCloseableImpl.make(((BTreeMap<Fun.Tuple3, Long>) this.addressTypeKey).subMap(
                Fun.t3(address, type, fromID),
                Fun.t3(address, type, Fun.HI())).values().iterator());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public IteratorCloseable<Long> getIteratorByTitleAndType(String filter, boolean asFilter, Integer type) {

        String filterLower = filter.toLowerCase();
        Iterable keys = Fun.filter(this.titleKey,
                new Tuple2<String, Integer>(filterLower,
                        type==0?0:type), true,
                new Tuple2<String, Integer>(asFilter?
                        filterLower + new String(new byte[]{(byte)255}) : filterLower,
                        type==0?Integer.MAX_VALUE:type), true);

        Iterator iter = keys.iterator();
        return new IteratorCloseableImpl(iter);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    // скорость сортировки в том или ином случае может быть разная - нужны ТЕСТЫ на 3 варианта работы
    // TODO need benchmark tests
    public IteratorCloseable<Long> getIteratorByAddress(String address) {

        if (true) {
            Iterable senderKeys = Fun.filter(this.senderKey, address);
            Iterable recipientKeys = Fun.filter(this.recipientKey, address);

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

            Iterable senderKeys = Fun.filter(this.senderKey, address);
            Iterator<Long> senderKeysIterator = senderKeys.iterator();
            Iterable recipientKeys = Fun.filter(this.recipientKey, address);
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
     * @param address
     * @param fromSeqNo
     * @param descending
     * @return
     */
    @Override
    public IteratorCloseable<Long> getBiDirectionAddressIterator(String address, Long fromSeqNo, boolean descending) {
        Long addressKey = Longs.fromByteArray(Crypto.getInstance().getShortBytesFromAddress(address));

        if (descending)
            return IteratorCloseableImpl.make(((NavigableMap)getIndex(BIDIRECTION_ADDRESS_INDEX, descending))
                    .subMap(new Tuple2<>(addressKey, null), new Tuple2<>(addressKey, fromSeqNo)).values().iterator());

        return IteratorCloseableImpl.make(((NavigableMap)getIndex(BIDIRECTION_ADDRESS_INDEX, descending))
                .subMap(new Tuple2<>(addressKey, fromSeqNo), new Tuple2<>(addressKey, Fun.HI())).values().iterator());
    }

}
