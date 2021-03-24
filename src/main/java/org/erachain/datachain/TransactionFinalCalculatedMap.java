package org.erachain.datachain;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transCalculated.Calculated;
import org.erachain.database.serializer.CalculatedSerializer;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.dbs.MergedOR_IteratorsNoDuplicates;
import org.erachain.utils.BlExpUnit;
import org.erachain.utils.ObserverMessage;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

//04/01 +-
//import org.erachain.core.transaction.Calculated;

//import java.math.BigDecimal;

/**
 * Храним вычисленные транзакции - для отображения в отчетах - пока нигде не используется - на будущее
 *
 * Ключ: ссылка на запись Родитель + Номер Актива - хотя наверное по Активу это во вторичные ключи
 * Значение: Сама Вычисленная транзакция
 * block.id + tx.ID in this block -> transaction
 *
 * Вторичные ключи по:
 * ++ sender_txs
 * ++ recipient_txs
 * ++ address_type_txs
 */
public class TransactionFinalCalculatedMap extends DCUMap<Tuple3<Integer, Integer, Long>, Calculated> {

    @SuppressWarnings("rawtypes")
    private NavigableSet senderKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet recipientKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet typeKey;

    //@SuppressWarnings("rawtypes")
    //private NavigableSet block_Key;
    // private NavigableSet <Tuple2<String,Tuple2<Integer,
    // Integer>>>signature_key;

    public TransactionFinalCalculatedMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_CLACULATED_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_CLACULATED_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_CLACULATED_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_CLACULATED_TYPE);
        }
    }

    public TransactionFinalCalculatedMap(TransactionFinalCalculatedMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @SuppressWarnings("unchecked")
    private Map<Tuple3<Integer, Integer, Long>, Calculated> openMap(DB database) {

        BTreeMap<Tuple3<Integer, Integer, Long>, Calculated> map = database.createTreeMap("height_seq_calculated")
                .keySerializer(BTreeKeySerializer.TUPLE3).valueSerializer(new CalculatedSerializer())
                .makeOrGet();

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return map;

        this.senderKey = database.createTreeSet("sender_txs").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryKey(map, this.senderKey, new Fun.Function2<String, Tuple3<Integer, Integer, Long>, Calculated>() {
            @Override
            public String run(Tuple3<Integer, Integer, Long> key, Calculated val) {
                Account account = val.getSender();
                return account == null ? "genesis" : account.getAddress();
            }
        });

        //	this.block_Key = database.createTreeSet("Block_txs").comparator(Fun.COMPARATOR).makeOrGet();

        //	Bind.secondaryKey(map, this.block_Key, new Fun.Function2<Integer, Tuple2<Integer, Integer>, Calculated>() {
        //		@Override
        //		public Integer run(Tuple2<Integer, Integer> key, Calculated val) {
        //			return val.getBlockHeightByParentOrLast((DCSet)databaseSet);
        //		}
        //	});

        this.recipientKey = database.createTreeSet("recipient_calcs").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryKeys(map, this.recipientKey,
                new Fun.Function2<String[], Tuple3<Integer, Integer, Long>, Calculated>() {
                    @Override
                    public String[] run(Tuple3<Integer, Integer, Long> key, Calculated val) {
                        List<String> recps = new ArrayList<String>();
                        for (Account acc : val.getRecipientAccounts()) {
                            recps.add(acc.getAddress());
                        }
                        String[] ret = new String[recps.size()];
                        ret = recps.toArray(ret);
                        return ret;
                    }
                });

        this.typeKey = database.createTreeSet("address_type_calcs").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryKeys(map, this.typeKey,
                new Fun.Function2<Tuple2<String, Integer>[], Tuple3<Integer, Integer, Long>, Calculated>() {
                    @Override
                    public Tuple2<String, Integer>[] run(Tuple3<Integer, Integer, Long> key, Calculated val) {
                        List<Tuple2<String, Integer>> recps = new ArrayList<Tuple2<String, Integer>>();
                        Integer type = val.getType();
                        for (Account acc : val.getInvolvedAccounts()) {
                            recps.add(new Tuple2<String, Integer>(acc.getAddress(), type));

                        }
                        // Tuple2<Integer, String>[] ret = (Tuple2<Integer,
                        // String>[]) new Object[ recps.size() ];
                        Tuple2<String, Integer>[] ret = (Tuple2<String, Integer>[]) Array.newInstance(Fun.Tuple2.class,
                                recps.size());
                        ret = recps.toArray(ret);
                        return ret;
                    }
                });

        return map;

    }

    @Override
    public void openMap() {
        // OPEN MAP
        map = openMap(database);
    }

    @Override
    protected void getMemoryMap() {
        // OPEN MAP
        this.openMap();
    }

    // TODO сделать удаление по фильтру разом - как у RocksDB - deleteRange(final byte[] beginKey, final byte[] endKey)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Integer height) {
        BTreeMap map = (BTreeMap) this.map;
        // GET ALL CalculatedS THAT BELONG TO THAT ADDRESS
        Collection<Tuple3> keys = ((BTreeMap<Tuple3, Calculated>) map)
                .subMap(Fun.t3(height, null, null), Fun.t3(height, Integer.MAX_VALUE, Long.MAX_VALUE)).keySet();

        // DELETE CalculatedS
        for (Tuple3<Integer, Integer, Long> key : keys) {
            if (this.contains(key))
                this.delete(key);
        }
    }

    public Calculated getCalculated(Integer blockNo, Integer transNo, Long seq) {
        return this.get(new Tuple3<Integer, Integer, Long>(blockNo, transNo, seq));
    }

    public List<Calculated> getCalculatedsByRecipient(String address) {
        return getCalculatedsByRecipient(address, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Calculated> getCalculatedsByRecipient(String address, int limit) {

        List<Calculated> txs = new ArrayList<>();
        int counter = 0;
        try (IteratorCloseable iterator = IteratorCloseableImpl.make(Fun.filter(this.recipientKey, address).iterator())) {
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {
                txs.add(this.map.get(iterator.next()));
                counter++;
            }
        } catch (IOException e) {
        }
        return txs;
    }

    public Collection<Calculated> getCalculatedsByBlock(Integer block) {
        return getCalculatedsByBlock(block, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Calculated> getCalculatedsByBlock(Integer block, int limit) {
        //BTreeMap map = (BTreeMap) this.map;
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        Collection<Calculated> keys1 = ((BTreeMap) map)
                .subMap(Fun.t2(block, null), Fun.t2(block, Integer.MAX_VALUE)).values();


        List<Calculated> txs = new ArrayList<>();
        for (Calculated bb : keys1) {
            txs.add(bb);
        }
        return txs;

    }

    public List<Calculated> getCalculatedsBySender(String address) {
        return getCalculatedsBySender(address, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Calculated> getCalculatedsBySender(String address, int limit) {
        Iterator iterator = Fun.filter(this.senderKey, address).iterator();

        List<Calculated> txs = new ArrayList<>();
        int counter = 0;
        while (iterator.hasNext() && (limit == 0 || counter < limit)) {
            txs.add(this.map.get(iterator.next()));
            counter++;
        }
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Calculated> getCalculatedsByTypeAndAddress(String address, Integer type, int limit) {
        Iterator iterator = Fun.filter(this.typeKey, new Tuple2<String, Integer>(address, type)).iterator();
        List<Calculated> txs = new ArrayList<>();
        int counter = 0;
        while (iterator.hasNext() && (limit == 0 || counter < limit)) {
            txs.add(this.map.get(iterator.next()));
            counter++;
        }
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Set<BlExpUnit> getBlExpCalculatedsByAddress(String address) {
        Iterator senderKeys = Fun.filter(this.senderKey, address).iterator();
        Iterator recipientKeys = Fun.filter(this.recipientKey, address).iterator();

        //iterator = Iterators.concat(senderKeys, recipientKeys);
        Iterator<Tuple2<Integer, Integer>> iterator = new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(senderKeys, recipientKeys), Fun.COMPARATOR);

        Set<BlExpUnit> txs = new TreeSet<>();
        while (iterator.hasNext()) {
            Tuple2<Integer, Integer> request = (Tuple2<Integer, Integer>) iterator.next();
            txs.add(new BlExpUnit(request.a, request.b, this.map.get(request)));
        }
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Calculated> getCalculatedsByAddress(String address) {
        Iterator senderKeys = Fun.filter(this.senderKey, address).iterator();
        Iterator recipientKeys = Fun.filter(this.recipientKey, address).iterator();

        //iterator = Iterators.concat(senderKeys, recipientKeys);
        Iterator<Tuple2<Integer, Integer>> iterator = new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(senderKeys, recipientKeys), Fun.COMPARATOR);

        List<Calculated> txs = new ArrayList<>();
        while (iterator.hasNext()) {
            txs.add(this.map.get(iterator.next()));
        }
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public int getCalculatedsByAddressCount(String address) {
        Iterator senderKeys = Fun.filter(this.senderKey, address).iterator();
        Iterator recipientKeys = Fun.filter(this.recipientKey, address).iterator();

        //Set<Tuple2<Integer, Integer>> treeKeys = new TreeSet<>();

        //iterator = Iterators.concat(senderKeys, recipientKeys);
        return Iterators.size(new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of((Iterable) senderKeys, recipientKeys), Fun.COMPARATOR));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Tuple2<Integer, Integer> getCalculatedsAfterTimestamp(int startHeight, int numOfTx, String address) {
        Iterator iterator = Fun.filter(this.recipientKey, address).iterator();
        int prevKey = startHeight;
        while (iterator.hasNext()) {
            Tuple2<Integer, Integer> key = (Tuple2<Integer, Integer>) iterator.next();
            if (key.a >= startHeight) {
                if (key.a != prevKey) {
                    numOfTx = 0;
                }
                prevKey = key.a;
                if (key.b > numOfTx) {
                    return key;
                }
            }
        }
        return null;
    }

    public DBTab<Tuple3<Integer, Integer, Long>, Calculated> getParentMap() {
        return this.parent;
    }

    @SuppressWarnings("rawtypes")
    public List<Calculated> findCalculateds(String address, String sender, String recipient, final int minHeight,
                                              final int maxHeight, int type, int service, boolean desc, int offset, int limit) {
        Iterator iterator = findCalculatedsKeys(address, sender, recipient, minHeight, maxHeight, type, service, desc,
                offset, limit);

        List<Calculated> txs = new ArrayList<>();

        while (iterator.hasNext()) {
            txs.add(this.map.get(iterator.next()));
        }
        return txs;
    }

    @SuppressWarnings("rawtypes")
    public int findCalculatedsCount(String address, String sender, String recipient, final int minHeight,
                                     final int maxHeight, int type, int service, boolean desc, int offset, int limit) {
        Iterator keys = findCalculatedsKeys(address, sender, recipient, minHeight, maxHeight, type, service, desc,
                offset, limit);
        return Iterators.size(keys);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Iterator findCalculatedsKeys(String address, String sender, String recipient, final int minHeight,
                                         final int maxHeight, int type, final int service, boolean desc, int offset, int limit) {
        Iterator senderKeys = null;
        Iterator recipientKeys = null;
        Iterator<Tuple2<Integer, Integer>> iterator = new TreeSet<Tuple2<Integer, Integer>>().iterator();

        if (address != null) {
            sender = address;
            recipient = address;
        }

        if (sender == null && recipient == null) {
            return iterator;
        }

        if (sender != null) {
            if (type > 0) {
                senderKeys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(sender, type)).iterator();
            } else {
                senderKeys = Fun.filter(this.senderKey, sender).iterator();
            }
        }

        if (recipient != null) {
            if (type > 0) {
                recipientKeys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(recipient, type)).iterator();
            } else {
                recipientKeys = Fun.filter(this.recipientKey, recipient).iterator();
            }
        }

        if (address != null) {
            //iterator = Iterators.concat(senderKeys, recipientKeys);
            iterator = new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of((Iterable) senderKeys, recipientKeys), Fun.COMPARATOR);

        } else if (sender != null && recipient != null) {
            iterator = senderKeys;
            Iterators.retainAll(iterator, Lists.newArrayList(recipientKeys));
        } else if (sender != null) {
            iterator = senderKeys;
        } else if (recipient != null) {
            iterator = recipientKeys;
        }

        if (minHeight != 0 || maxHeight != 0) {
            iterator = Iterators.filter(iterator, new Predicate<Tuple2<Integer, Integer>>() {
                @Override
                public boolean apply(Tuple2<Integer, Integer> key) {
                    return (minHeight == 0 || key.a >= minHeight) && (maxHeight == 0 || key.a <= maxHeight);
                }
            });
        }

        if (desc) {
            //iterator = ((TreeSet) iterator).descendingSet();
            ///iterator = ((TreeSet) iterator).descendingIterator();
            iterator = Lists.reverse(Lists.newArrayList(iterator)).iterator();
        }

        limit = (limit == 0) ? Iterators.size(iterator) : limit;
        Iterators.advance(iterator, offset);

        return Iterators.limit(iterator, limit);
    }

}
