package org.erachain.dbs.mapDB;

//04/01 +- 

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalSuit;
import org.mapdb.BTreeKeySerializer.BasicKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;

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
public class TransactionFinalSuitMapDB extends DBMapSuit<Long, Transaction> implements TransactionFinalSuit {

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

    public TransactionFinalSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    protected void createIndexes() {
    }

    @Override
    protected void getMap() {
        // OPEN MAP
        // TREE MAP for sortable search
        map = database.createTreeMap("height_seq_transactions")
                .keySerializer(BasicKeySerializer.BASIC)
                .valueSerializer(new TransactionSerializer())
                .counterEnable()
                .makeOrGet();

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
                    public Tuple2<String, Integer>[] run(Long key, Transaction val) {
                        List<Tuple2<String, Integer>> recps = new ArrayList<Tuple2<String, Integer>>();
                        Integer type = val.getType();
                        for (Account acc : val.getInvolvedAccounts()) {
                            // TODO: make unique key??  + val.viewTimestamp()
                            recps.add(new Tuple2<String, Integer>(acc.getAddress(), type));
                        }

                        // Tuple2<Integer, String>[] ret = (Tuple2<Integer,
                        // String>[]) new Object[ recps.size() ];
                        Tuple2<String, Integer>[] ret = (Tuple2<String, Integer>[])
                                Array.newInstance(Tuple2.class, recps.size());
                        ret = recps.toArray(ret);
                        return ret;
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
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getBlockIterator(Integer height) {
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
         return  ((BTreeMap<Long, Transaction>) map)
                .subMap(Transaction.makeDBRef(height, 0),
                        Transaction.makeDBRef(height, Integer.MAX_VALUE)).keySet().iterator();

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getIteratorByRecipient(String address) {
        Iterable keys = Fun.filter(this.recipientKey, address);
        Iterator iter = keys.iterator();
        return iter;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<Long> getIteratorBySender(String address) {
        Iterable keys = Fun.filter(this.senderKey, address);
        Iterator iter = keys.iterator();
        return iter;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Iterator<Long> getIteratorByAddressAndType(String address, Integer type) {
        Iterable keys = Fun.filter(this.addressTypeKey, new Tuple2<String, Integer>(address, type));
        Iterator iter = keys.iterator();
        return iter;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Iterator<Long> getIteratorByTitleAndType(String filter, boolean asFilter, Integer type) {

        String filterLower = filter.toLowerCase();
        Iterable keys = Fun.filter(this.titleKey,
                new Tuple2<String, Integer>(filterLower,
                        type==0?0:type), true,
                new Tuple2<String, Integer>(asFilter?
                        filterLower + new String(new byte[]{(byte)255}) : filterLower,
                        type==0?Integer.MAX_VALUE:type), true);

        Iterator iter = keys.iterator();
        return iter;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Iterator<Long> getIteratorByAddress(String address) {
        Iterator<Long> senderKeys = Fun.filter(this.senderKey, address).iterator();
        Iterator<Long> recipientKeys = Fun.filter(this.recipientKey, address).iterator();

        Iterator<Long> treeKeys = new TreeSet<Long>().iterator();

        treeKeys = Iterators.concat(senderKeys, recipientKeys);

        return treeKeys; //((TreeSet<Long>) treeKeys).descendingIterator();
    }

    /**
     * Пока это не используется - на верхнем уровне своя сборка общая от получаемых Итераторов с этого класса.
     * Возможно потом с более конкретным проходом по DESCENDING + OFFSET & LIMIT буджет реализация у каждой СУБД своя?
     * Хотя нет - просто в Iterator перебор по индексаю таблицы у СУБД уже свой реализован
     * @param address
     * @param sender
     * @param recipient
     * @param minHeight
     * @param maxHeight
     * @param type
     * @param service
     * @param desc
     * @param offset
     * @param limit
     * @return
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Iterator findTransactionsKeys(String address, String sender, String recipient, final int minHeight,
                                         final int maxHeight, int type, final int service, boolean desc, int offset, int limit) {
        Iterator senderKeys = null;
        Iterator recipientKeys = null;
        Iterator iterator = new TreeSet<>().iterator();

        if (address != null) {
            sender = address;
            recipient = address;
        }

        if (sender == null && recipient == null) {
            return iterator;
        }

        if (sender != null) {
            if (type != 0) {
                senderKeys = Fun.filter(this.addressTypeKey, new Tuple2<String, Integer>(sender, type)).iterator();
            } else {
                senderKeys = Fun.filter(this.senderKey, sender).iterator();
            }
        }

        if (recipient != null) {
            if (type != 0) {
                recipientKeys = Fun.filter(this.addressTypeKey, new Tuple2<String, Integer>(recipient, type)).iterator();
            } else {
                recipientKeys = Fun.filter(this.recipientKey, recipient).iterator();
            }
        }

        if (address != null) {
            iterator = Iterators.concat(senderKeys, recipientKeys);
        } else if (sender != null && recipient != null) {
            iterator = senderKeys;
            Iterators.retainAll(iterator, Lists.newArrayList(recipientKeys));
        } else if (sender != null) {
            iterator = senderKeys;
        } else if (recipient != null) {
            iterator = recipientKeys;
        }

        if (minHeight != 0 || maxHeight != 0) {
            iterator = Iterators.filter(iterator, new Predicate<Long>() {
                @Override
                public boolean apply(Long key) {
                    Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                    return (minHeight == 0 || pair.a >= minHeight) && (maxHeight == 0 || pair.a <= maxHeight);
                }
            });
        }

        if (false && type == Transaction.ARBITRARY_TRANSACTION && service != 0) {
            iterator = Iterators.filter(iterator, new Predicate<Long>() {
                @Override
                public boolean apply(Long key) {
                    ArbitraryTransaction tx = (ArbitraryTransaction) map.get(key);
                    return tx.getService() == service;
                }
            });
        }

        if (desc) {
            //keys = ((TreeSet) iterator).descendingSet();
            iterator = Lists.reverse(Lists.newArrayList(iterator)).iterator();
        }

        //limit = (limit == 0) ? Iterables.size(keys) : limit;
        limit = (limit == 0) ? Iterators.size(iterator) : limit;

        Iterators.advance(iterator, offset);
        iterator = Iterators.limit(iterator, limit);


        return iterator;
    }

}
