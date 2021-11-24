package org.erachain.dbs.mapDB;

//04/01 +- 

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
import org.mapdb.BTreeKeySerializer.BasicKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple3;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
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
public class TransactionFinalSuitMapDB extends DBMapSuit<Long, Transaction> implements TransactionFinalSuit {

    private static int CUT_NAME_INDEX = 12;

    @SuppressWarnings("rawtypes")
    private NavigableSet creatorKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet addressTypeKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet recipientKey;

    @SuppressWarnings("rawtypes")
    private NavigableSet titleKey;

    public TransactionFinalSuitMapDB(DBASet databaseSet, DB database, boolean sizeEnable) {
        super(databaseSet, database, logger, sizeEnable, null);
    }

    @Override
    public void openMap() {

        HI = Long.MAX_VALUE;
        LO = 0L;

        // OPEN MAP
        // TREE MAP for sortable search
        DB.BTreeMapMaker mapConstruct = database.createTreeMap("height_seq_transactions")
                .keySerializer(BasicKeySerializer.BASIC)
                //.keySerializer(BTreeKeySerializer.ZERO_OR_POSITIVE_LONG)
                .valueSerializer(new TransactionSerializer());

        if (sizeEnable)
            mapConstruct = mapConstruct.counterEnable();

        map = mapConstruct.makeOrGet();

        // теперь это протокольный для множественных выплат
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
                new Function2<Tuple3<byte[], Integer, Boolean>[], Long, Transaction>() {
                    @Override
                    public Tuple3<byte[], Integer, Boolean>[] run(Long key, Transaction transaction) {
                        // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                        if (transaction.noDCSet()) {
                            transaction.setDC((DCSet) databaseSet, true);
                        }
                        List<Tuple3<byte[], Integer, Boolean>> accounts = new ArrayList<Tuple3<byte[], Integer, Boolean>>();
                        Integer type = transaction.getType();
                        for (Account account : transaction.getInvolvedAccounts()) {
                            byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
                            System.arraycopy(account.getShortAddressBytes(), 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                            accounts.add(new Tuple3<byte[], Integer, Boolean>(addressKey, type, account.equals(transaction.getCreator())));
                        }

                        Tuple3<byte[], Integer, Boolean>[] result = (Tuple3<byte[], Integer, Boolean>[])
                                Array.newInstance(Tuple3.class, accounts.size());
                        result = accounts.toArray(result);
                        return result;
                    }
                });

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        this.recipientKey = database.createTreeSet("recipient_txs")
                .comparator(comparatorAddressT2) // - for Tuple2 String
                //.comparator(SignedBytes.lexicographicalComparator())
                .makeOrGet();

        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.recipientKey,
                new Function2<byte[][], Long, Transaction>() {
                    @Override
                    public byte[][] run(Long key, Transaction transaction) {
                        // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                        if (transaction.noDCSet()) {
                            transaction.setDC((DCSet) databaseSet, true);
                        }

                        HashSet<Account> recipients = transaction.getRecipientAccounts();
                        int size = recipients.size();
                        byte[][] keys = new byte[size][];
                        int count = 0;
                        for (Account recipient : recipients) {
                            byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
                            System.arraycopy(recipient.getShortAddressBytes(), 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);
                            keys[count++] = addressKey;
                        }
                        return keys;
                    }
                });

        this.titleKey = database.createTreeSet("title_txs").comparator(Fun.COMPARATOR).makeOrGet();

        // в БИНЕ внутри уникальные ключи создаются добавлением основного ключа
        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.titleKey,
                new Function2<String[], Long, Transaction>() {
                    @Override
                    public String[] run(Long key, Transaction transaction) {

                        // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                        // При удалении - транзакция то берется из базы для создания индексов к удалению.
                        // И она скелет - нужно базу данных задать и водтянуть номера сущностей и все заново просчитать чтобы правильно удалить метки
                        if (transaction.noDCSet()) {
                            transaction.setDC((DCSet) databaseSet, true);
                        }

                        String[] tokens = transaction.getTags();
                        if (tokens == null || tokens.length == 0)
                            return null;

                        String[] keys = new String[tokens.length];
                        int count = 0;
                        for (String token : tokens) {
                            if (token.length() > CUT_NAME_INDEX) {
                                keys[count++] = token.substring(0, CUT_NAME_INDEX);
                            } else if (token.length() > 0) {
                                keys[count++] = token;
                            }
                        }

                        String[] keys2 = new String[count];
                        System.arraycopy(keys, 0, keys2, 0, keys2.length);
                        return keys2;
                    }
                });

    }

    @Override
    public void deleteForBlock(Integer height) {
        // Descending for correct remove tags see issue #1766
        try (IteratorCloseable<Long> iterator = getOneBlockIterator(height, true)) {
            while (iterator.hasNext()) {
                map.remove(iterator.next());
            }
        } catch (IOException e) {
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getOneBlockIterator(Integer height, boolean descending) {
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        if (descending) {
            return IteratorCloseableImpl.make(((BTreeMap<Long, Transaction>) map)
                    .subMap(Transaction.makeDBRef(height, 0),
                            Transaction.makeDBRef(height, Integer.MAX_VALUE)).keySet().descendingIterator());
        } else {
            return IteratorCloseableImpl.make(((BTreeMap<Long, Transaction>) map)
                    .subMap(Transaction.makeDBRef(height, 0),
                            Transaction.makeDBRef(height, Integer.MAX_VALUE)).keySet().iterator());
        }

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        Iterable keys = Fun.filter(descending ? this.creatorKey.descendingSet() : this.creatorKey, addressKey);
        return IteratorCloseableImpl.make(keys.iterator());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        if (fromSeqNo == null) {
            fromSeqNo = descending ? Long.MAX_VALUE : Long.MIN_VALUE;
        }

        return IteratorCloseableImpl.make(new IndexIterator((descending ? this.creatorKey.descendingSet() : this.creatorKey)
                .subSet(Fun.t2(addressKey, fromSeqNo),
                        Fun.t2(addressKey, descending ? Long.MIN_VALUE : Long.MAX_VALUE)).iterator()));
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

        return IteratorCloseableImpl.make(new IndexIterator((descending ? this.creatorKey.descendingSet() : this.creatorKey)
                .subSet(Fun.t2(addressKey, fromSeqNo),
                        Fun.t2(addressKey, toSeqNo)).iterator()));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        Iterable keys = Fun.filter(descending ? this.recipientKey.descendingSet() : this.recipientKey, addressKey);
        return IteratorCloseableImpl.make(keys.iterator());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort, Long fromSeqNo, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        return IteratorCloseableImpl.make(new IndexIterator((descending ? this.recipientKey.descendingSet() : this.recipientKey)
                .subSet(Fun.t2(addressKey, fromSeqNo),
                        Fun.t2(addressKey, descending ? Long.MIN_VALUE : Long.MAX_VALUE)).iterator()));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByRecipient(byte[] addressShort, Long fromSeqNo, Long toSeqNo, boolean descending) {
        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        if (toSeqNo == null) {
            toSeqNo = descending ? Long.MIN_VALUE : Long.MAX_VALUE;
        }

        return IteratorCloseableImpl.make(new IndexIterator((descending ? this.recipientKey.descendingSet() : this.recipientKey)
                .subSet(Fun.t2(addressKey, fromSeqNo),
                        Fun.t2(addressKey, toSeqNo)).iterator()));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, boolean descending) {

        if (type == null && isCreator != null) {
            return null;
        }

        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        return IteratorCloseableImpl.make(new IndexIterator((descending ? this.addressTypeKey.descendingSet() : this.addressTypeKey).subSet(
                Fun.t2(Fun.t3(addressKey,
                        type == null || type == 0 ? descending ? Integer.MAX_VALUE : Integer.MIN_VALUE : type,
                        isCreator == null ? descending ? Boolean.TRUE : Boolean.FALSE : isCreator
                ), descending ? Long.MAX_VALUE : Long.MIN_VALUE),
                Fun.t2(Fun.t3(addressKey,
                        type == null || type == 0 ? descending ? Integer.MIN_VALUE : Integer.MAX_VALUE : type,
                        isCreator == null ? descending ? Boolean.FALSE : Boolean.TRUE : isCreator
                ), descending ? Long.MIN_VALUE : Long.MAX_VALUE)).iterator()));

    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, Long fromID, boolean descending) {
        if (type == null && isCreator != null) {
            return null;
        }

        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        if (fromID == null) {
            fromID = descending ? Long.MAX_VALUE : Long.MIN_VALUE;
        }

        return IteratorCloseableImpl.make(new IndexIterator((descending ? this.addressTypeKey.descendingSet() : this.addressTypeKey).subSet(
                Fun.t2(Fun.t3(addressKey,
                        type == null || type == 0 ? descending ? Integer.MAX_VALUE : Integer.MIN_VALUE : type,
                        isCreator == null ? descending ? Boolean.TRUE : Boolean.FALSE : isCreator
                ), fromID),
                Fun.t2(Fun.t3(addressKey,
                        type == null || type == 0 ? descending ? Integer.MIN_VALUE : Integer.MAX_VALUE : type,
                        isCreator == null ? descending ? Boolean.FALSE : Boolean.TRUE : isCreator
                ), descending ? Long.MIN_VALUE : Long.MAX_VALUE)).iterator()));
    }

    @Override
    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, Long fromID, Long toID, boolean descending) {
        if (type == null && isCreator != null) {
            return null;
        }

        byte[] addressKey = new byte[TransactionFinalMap.ADDRESS_KEY_LEN];
        System.arraycopy(addressShort, 0, addressKey, 0, TransactionFinalMap.ADDRESS_KEY_LEN);

        if (fromID == null) {
            fromID = descending ? Long.MAX_VALUE : Long.MIN_VALUE;
        }

        if (toID == null) {
            toID = descending ? Long.MIN_VALUE : Long.MAX_VALUE;
        }

        return IteratorCloseableImpl.make(new IndexIterator((descending ? this.addressTypeKey.descendingSet() : this.addressTypeKey).subSet(
                Fun.t2(Fun.t3(addressKey,
                        type == null || type == 0 ? descending ? Integer.MAX_VALUE : Integer.MIN_VALUE : type,
                        isCreator == null ? descending ? Boolean.TRUE : Boolean.FALSE : isCreator
                ), fromID),
                Fun.t2(Fun.t3(addressKey,
                        type == null || type == 0 ? descending ? Integer.MIN_VALUE : Integer.MAX_VALUE : type,
                        isCreator == null ? descending ? Boolean.FALSE : Boolean.TRUE : isCreator
                ), toID)).iterator()));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getIteratorByTitle(String filter, boolean asFilter, String fromWord, Long fromSeqNo, boolean descending) {

        String filterLower = filter.toLowerCase();
        String startFrom = fromWord == null ? filterLower : fromWord.toLowerCase();
        String filterLowerEnd;

        if (descending) {

            if (asFilter && fromWord == null) {
                startFrom = filterLower + new String(new byte[]{(byte) 255});
            }
            return IteratorCloseableImpl.make(new IndexIterator((((NavigableSet) this.titleKey).descendingSet()
                    .subSet(
                            Fun.t2(startFrom, fromSeqNo == null || fromSeqNo == 0 ? Long.MAX_VALUE : fromSeqNo),// false,
                            Fun.t2(filterLower, 0L)//, true
                    )).iterator()));
        } else {
            if (asFilter) {
                filterLowerEnd = filterLower + new String(new byte[]{(byte) 255});
            } else {
                filterLowerEnd = filterLower;
            }
            return IteratorCloseableImpl.make(new IndexIterator(this.titleKey.subSet(
                    Fun.t2(startFrom, fromSeqNo == null || fromSeqNo == 0 ? 0L : fromSeqNo),
                    Fun.t2(filterLowerEnd, Long.MAX_VALUE)).iterator()));
        }

    }

    /**
     * @param addressShort
     * @param fromSeqNo
     * @param descending
     * @return
     */
    @Override
    public IteratorCloseable<Long> getAddressesIterator(byte[] addressShort, Long fromSeqNo, boolean descending) {

        if (addressShort == null)
            return getIterator(fromSeqNo, descending);

        return getIteratorByAddressAndType(addressShort, null, null, fromSeqNo, descending);
    }

    @Override
    public void put(Long key, Transaction transaction) {
        super.put(key, transaction);
    }

}
