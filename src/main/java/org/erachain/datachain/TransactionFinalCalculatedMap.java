package org.erachain.datachain;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transCalculated.Calculated;
import org.erachain.dbs.DBMap;
import org.erachain.database.serializer.CalculatedSerializer;
import org.erachain.utils.BlExpUnit;
import org.erachain.utils.ObserverMessage;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

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
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_CLACULATED_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_CLACULATED_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_CLACULATED_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_CLACULATED_TYPE);
        }
    }

    public TransactionFinalCalculatedMap(TransactionFinalCalculatedMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    protected void createIndexes() {
    }

    @SuppressWarnings("unchecked")
    private Map<Tuple3<Integer, Integer, Long>, Calculated> openMap(DB database) {

        BTreeMap<Tuple3<Integer, Integer, Long>, Calculated> map = database.createTreeMap("height_seq_calculated")
                .keySerializer(BTreeKeySerializer.TUPLE3).valueSerializer(new CalculatedSerializer())
                .counterEnable()
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
    protected void getMap() {
        // OPEN MAP
        openMap(database);
    }

    @Override
    protected void getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        // OPEN MAP
        this.getMap();
    }

    @Override
    protected Calculated getDefaultValue() {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Integer height) {
        BTreeMap map = (BTreeMap) this.map;
        // GET ALL CalculatedS THAT BELONG TO THAT ADDRESS
        Collection<Tuple3> keys = ((BTreeMap<Tuple3, Calculated>) map)
                .subMap(Fun.t3(height, null, null), Fun.t3(height, Fun.HI(), Fun.HI())).keySet();

        // DELETE CalculatedS
        for (Tuple3<Integer, Integer, Long> key : keys) {
            if (this.contains(key))
                this.delete(key);
        }
        keys = null;
    }

    public void delete(Integer blockNo, Integer transNo, Long seq) {
        this.delete(new Tuple3<Integer, Integer, Long>(blockNo, transNo, seq));
    }

    public boolean add(Integer blockNo, Integer transNo, Long seq, Calculated calculated) {
        return this.set(new Tuple3<Integer, Integer, Long>(blockNo, transNo, seq), calculated);
    }

    public Calculated getCalculated(Integer blockNo, Integer transNo, Long seq) {
        return this.get(new Tuple3<Integer, Integer, Long>(blockNo, transNo, seq));
    }

    public List<Calculated> getCalculatedsByRecipient(String address) {
        return getCalculatedsByRecipient(address, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Calculated> getCalculatedsByRecipient(String address, int limit) {
        Iterable keys = Fun.filter(this.recipientKey, address);
        Iterator iter = keys.iterator();
        keys = null;
        List<Calculated> txs = new ArrayList<>();
        int counter = 0;
        while (iter.hasNext() && (limit == 0 || counter < limit)) {
            txs.add(this.map.get(iter.next()));
            counter++;
        }
        iter = null;
        return txs;
    }

    public Collection<Calculated> getCalculatedsByBlock(Integer block) {
        return getCalculatedsByBlock(block, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Calculated> getCalculatedsByBlock(Integer block, int limit) {
		/*
		Iterable keys = Fun.filter(this.block_Key, block);
		Iterator iter = keys.iterator();
		keys = null;
		List<Calculated> txs = new ArrayList<>();
		int counter = 0;
		while (iter.hasNext() && (limit == 0 || counter < limit)) {
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		iter = null;
		*/
        //BTreeMap map = (BTreeMap) this.map;
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        Collection<Calculated> keys1 = ((BTreeMap) map)
                .subMap(Fun.t2(block, null), Fun.t2(block, Fun.HI())).values();


        List<Calculated> txs = new ArrayList<>();
        for (Calculated bb : keys1) {
            txs.add(bb);
            bb = null;
        }
        keys1 = null;
        return txs;

    }

    public List<Calculated> getCalculatedsBySender(String address) {
        return getCalculatedsBySender(address, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Calculated> getCalculatedsBySender(String address, int limit) {
        Iterable keys = Fun.filter(this.senderKey, address);
        Iterator iter = keys.iterator();
        keys = null;
        List<Calculated> txs = new ArrayList<>();
        int counter = 0;
        while (iter.hasNext() && (limit == 0 || counter < limit)) {
            txs.add(this.map.get(iter.next()));
            counter++;
        }
        iter = null;
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Calculated> getCalculatedsByTypeAndAddress(String address, Integer type, int limit) {
        Iterable keys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(address, type));
        Iterator iter = keys.iterator();
        keys = null;
        List<Calculated> txs = new ArrayList<>();
        int counter = 0;
        while (iter.hasNext() && (limit == 0 || counter < limit)) {
            txs.add(this.map.get(iter.next()));
            counter++;
        }
        iter = null;
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Set<BlExpUnit> getBlExpCalculatedsByAddress(String address) {
        Iterable senderKeys = Fun.filter(this.senderKey, address);
        Iterable recipientKeys = Fun.filter(this.recipientKey, address);

        Set<Tuple2<Integer, Integer>> treeKeys = new TreeSet<>();

        treeKeys.addAll(Sets.newTreeSet(senderKeys));
        treeKeys.addAll(Sets.newTreeSet(recipientKeys));

        Iterator iter = treeKeys.iterator();
        treeKeys = null;
        recipientKeys = null;
        senderKeys = null;
        Set<BlExpUnit> txs = new TreeSet<>();
        while (iter.hasNext()) {
            Tuple2<Integer, Integer> request = (Tuple2<Integer, Integer>) iter.next();
            txs.add(new BlExpUnit(request.a, request.b, this.map.get(request)));
        }
        iter = null;
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Calculated> getCalculatedsByAddress(String address) {
        Iterable senderKeys = Fun.filter(this.senderKey, address);
        Iterable recipientKeys = Fun.filter(this.recipientKey, address);

        Set<Tuple2<Integer, Integer>> treeKeys = new TreeSet<>();

        treeKeys.addAll(Sets.newTreeSet(senderKeys));
        treeKeys.addAll(Sets.newTreeSet(recipientKeys));

        Iterator iter = treeKeys.iterator();
        treeKeys = null;
        recipientKeys = null;
        senderKeys = null;
        List<Calculated> txs = new ArrayList<>();
        while (iter.hasNext()) {
            txs.add(this.map.get(iter.next()));
        }
        treeKeys = null;
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public int getCalculatedsByAddressCount(String address) {
        Iterable senderKeys = Fun.filter(this.senderKey, address);
        Iterable recipientKeys = Fun.filter(this.recipientKey, address);

        Set<Tuple2<Integer, Integer>> treeKeys = new TreeSet<>();

        treeKeys.addAll(Sets.newTreeSet(senderKeys));
        treeKeys.addAll(Sets.newTreeSet(recipientKeys));

        return treeKeys.size();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Tuple2<Integer, Integer> getCalculatedsAfterTimestamp(int startHeight, int numOfTx, String address) {
        Iterable keys = Fun.filter(this.recipientKey, address);
        Iterator iter = keys.iterator();
        int prevKey = startHeight;
        keys = null;
        while (iter.hasNext()) {
            Tuple2<Integer, Integer> key = (Tuple2<Integer, Integer>) iter.next();
            if (key.a >= startHeight) {
                if (key.a != prevKey) {
                    numOfTx = 0;
                }
                prevKey = key.a;
                if (key.b > numOfTx)
                    iter = null;
                return key;
            }
        }
        iter = null;
        return null;
    }

    public DBMap<Tuple3<Integer, Integer, Long>, Calculated> getParentMap() {
        return this.parent;
    }

    @SuppressWarnings("rawtypes")
    public List<Calculated> findCalculateds(String address, String sender, String recipient, final int minHeight,
                                              final int maxHeight, int type, int service, boolean desc, int offset, int limit) {
        Iterable keys = findCalculatedsKeys(address, sender, recipient, minHeight, maxHeight, type, service, desc,
                offset, limit);

        Iterator iter = keys.iterator();
        keys = null;
        List<Calculated> txs = new ArrayList<>();

        while (iter.hasNext()) {
            txs.add(this.map.get(iter.next()));
        }
        iter = null;
        return txs;
    }

    @SuppressWarnings("rawtypes")
    public int findCalculatedsCount(String address, String sender, String recipient, final int minHeight,
                                     final int maxHeight, int type, int service, boolean desc, int offset, int limit) {
        Iterable keys = findCalculatedsKeys(address, sender, recipient, minHeight, maxHeight, type, service, desc,
                offset, limit);
        return Iterables.size(keys);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Iterable findCalculatedsKeys(String address, String sender, String recipient, final int minHeight,
                                         final int maxHeight, int type, final int service, boolean desc, int offset, int limit) {
        Iterable senderKeys = null;
        Iterable recipientKeys = null;
        Set<Tuple2<Integer, Integer>> treeKeys = new TreeSet<>();

        if (address != null) {
            sender = address;
            recipient = address;
        }

        if (sender == null && recipient == null) {
            return treeKeys;
        }

        if (sender != null) {
            if (type > 0) {
                senderKeys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(sender, type));
            } else {
                senderKeys = Fun.filter(this.senderKey, sender);
            }
        }

        if (recipient != null) {
            if (type > 0) {
                recipientKeys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(recipient, type));
            } else {
                recipientKeys = Fun.filter(this.recipientKey, recipient);
            }
        }

        if (address != null) {
            treeKeys.addAll(Sets.newTreeSet(senderKeys));
            treeKeys.addAll(Sets.newTreeSet(recipientKeys));
        } else if (sender != null && recipient != null) {
            treeKeys.addAll(Sets.newTreeSet(senderKeys));
            treeKeys.retainAll(Sets.newTreeSet(recipientKeys));
        } else if (sender != null) {
            treeKeys.addAll(Sets.newTreeSet(senderKeys));
        } else if (recipient != null) {
            treeKeys.addAll(Sets.newTreeSet(recipientKeys));
        }

        if (minHeight != 0 || maxHeight != 0) {
            treeKeys = Sets.filter(treeKeys, new Predicate<Tuple2<Integer, Integer>>() {
                @Override
                public boolean apply(Tuple2<Integer, Integer> key) {
                    return (minHeight == 0 || key.a >= minHeight) && (maxHeight == 0 || key.a <= maxHeight);
                }
            });
        }

        Iterable keys;
        if (desc) {
            keys = ((TreeSet) treeKeys).descendingSet();
        } else {
            keys = treeKeys;
        }

        limit = (limit == 0) ? Iterables.size(keys) : limit;

        return Iterables.limit(Iterables.skip(keys, offset), limit);
    }

}
