package datachain;

//04/01 +- 

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import core.account.Account;
import core.crypto.Base58;
import core.transaction.ArbitraryTransaction;
import core.transaction.Transaction;
import database.DBMap;
import database.serializer.TransactionSerializer;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import utils.BlExpUnit;
import utils.ObserverMessage;

import java.lang.reflect.Array;
import java.util.*;

//import java.math.BigDecimal;

// block.id + tx.ID in this block -> transaction
// ++ sender_txs
// ++ recipient_txs
// ++ address_type_txs
public class TransactionFinalMap extends DCMap<Tuple2<Integer, Integer>, Transaction> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

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

    public TransactionFinalMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (false && databaseSet.isWithObserver()) {
            if (databaseSet.isDynamicGUI()) {
                this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_TRANSACTION_TYPE);
                this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_TRANSACTION_TYPE);
                this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRANSACTION_TYPE);
                this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_TRANSACTION_TYPE);
            } else {
                this.observableData.put(DBMap.NOTIFY_COUNT, ObserverMessage.COUNT_TRANSACTION_TYPE);
            }
        }
    }

    public TransactionFinalMap(TransactionFinalMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    protected void createIndexes(DB database) {
    }

    @SuppressWarnings("unchecked")
    private Map<Tuple2<Integer, Integer>, Transaction> openMap(DB database) {

        BTreeMap<Tuple2<Integer, Integer>, Transaction> map = database.createTreeMap("height_seq_transactions")
                .keySerializer(BTreeKeySerializer.TUPLE2).valueSerializer(new TransactionSerializer())
                .counterEnable()
                .makeOrGet();

        this.senderKey = database.createTreeSet("sender_txs").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryKey(map, this.senderKey, new Fun.Function2<String, Tuple2<Integer, Integer>, Transaction>() {
            @Override
            public String run(Tuple2<Integer, Integer> key, Transaction val) {
                Account account = val.getCreator();
                return account == null ? "genesis" : account.getAddress();
            }
        });

        //	this.block_Key = database.createTreeSet("Block_txs").comparator(Fun.COMPARATOR).makeOrGet();

        //	Bind.secondaryKey(map, this.block_Key, new Fun.Function2<Integer, Tuple2<Integer, Integer>, Transaction>() {
        //		@Override
        //		public Integer run(Tuple2<Integer, Integer> key, Transaction val) {
        //			return val.getBlockHeightByParentOrLast(getDCSet());
        //		}
        //	});

        this.recipientKey = database.createTreeSet("recipient_txs").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryKeys(map, this.recipientKey,
                new Fun.Function2<String[], Tuple2<Integer, Integer>, Transaction>() {
                    @Override
                    public String[] run(Tuple2<Integer, Integer> key, Transaction val) {
                        List<String> recps = new ArrayList<String>();
                        val.setDC(getDCSet(), false);
                        for (Account acc : val.getRecipientAccounts()) {
                            recps.add(acc.getAddress());
                        }
                        String[] ret = new String[recps.size()];
                        ret = recps.toArray(ret);
                        return ret;
                    }
                });

        this.typeKey = database.createTreeSet("address_type_txs").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryKeys(map, this.typeKey,
                new Fun.Function2<Tuple2<String, Integer>[], Tuple2<Integer, Integer>, Transaction>() {
                    @Override
                    public Tuple2<String, Integer>[] run(Tuple2<Integer, Integer> key, Transaction val) {
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
    protected Map<Tuple2<Integer, Integer>, Transaction> getMap(DB database) {
        // OPEN MAP
        return openMap(database);
    }

    @Override
    protected Map<Tuple2<Integer, Integer>, Transaction> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        // OPEN MAP
        return this.getMap(database);
    }

    @Override
    protected Transaction getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Integer height) {
        BTreeMap map = (BTreeMap) this.map;
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        Collection<Tuple2> keys = ((BTreeMap<Tuple2, Transaction>) map)
                .subMap(Fun.t2(height, null), Fun.t2(height, Fun.HI())).keySet();

        // DELETE TRANSACTIONS
        for (Tuple2<Integer, Integer> key : keys) {
            if (this.contains(key))
                this.delete(key);
        }
        keys = null;
    }

    public void delete(Integer height, Integer seq) {
        this.delete(new Tuple2<Integer, Integer>(height, seq));
    }

    public boolean add(Integer height, Integer seq, Transaction transaction) {
        return this.set(new Tuple2<Integer, Integer>(height, seq), transaction);
    }

    public Transaction getTransaction(Integer height, Integer seq) {
        return this.get(new Tuple2<Integer, Integer>(height, seq));
    }

    public List<Transaction> getTransactionsByRecipient(String address) {
        return getTransactionsByRecipient(address, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsByRecipient(String address, int limit) {
        Iterable keys = Fun.filter(this.recipientKey, address);
        Iterator iter = keys.iterator();
        keys = null;
        List<Transaction> txs = new ArrayList<>();
        int counter = 0;
        while (iter.hasNext() && (limit == 0 || counter < limit)) {
            txs.add(this.map.get(iter.next()));
            counter++;
        }
        iter = null;
        return txs;
    }

    public Collection<Transaction> getTransactionsByBlock(Integer block) {
        return getTransactionsByBlock(block, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsByBlock(Integer block, int limit) {
		/*
		Iterable keys = Fun.filter(this.block_Key, block);
		Iterator iter = keys.iterator();
		keys = null;
		List<Transaction> txs = new ArrayList<>();
		int counter = 0;
		while (iter.hasNext() && (limit == 0 || counter < limit)) {
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		iter = null;
		*/
        //BTreeMap map = (BTreeMap) this.map;
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        Collection<Transaction> keys1 = ((BTreeMap) map)
                .subMap(Fun.t2(block, null), Fun.t2(block, Fun.HI())).values();


        List<Transaction> txs = new ArrayList<>();
        for (Transaction bb : keys1) {
            txs.add(bb);
            bb = null;
        }
        keys1 = null;
        return txs;

    }

    public List<Transaction> getTransactionsBySender(String address) {
        return getTransactionsBySender(address, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsBySender(String address, int limit) {
        Iterable keys = Fun.filter(this.senderKey, address);
        Iterator iter = keys.iterator();
        keys = null;
        List<Transaction> txs = new ArrayList<>();
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
    public List<Transaction> getTransactionsByTypeAndAddress(String address, Integer type, int limit) {
        Iterable keys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(address, type));
        Iterator iter = keys.iterator();
        keys = null;
        List<Transaction> txs = new ArrayList<>();
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
    public Set<BlExpUnit> getBlExpTransactionsByAddress(String address) {
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
    public List<Transaction> getTransactionsByAddress(String address) {
        Iterable senderKeys = Fun.filter(this.senderKey, address);
        Iterable recipientKeys = Fun.filter(this.recipientKey, address);

        Set<Tuple2<Integer, Integer>> treeKeys = new TreeSet<>();

        treeKeys.addAll(Sets.newTreeSet(senderKeys));
        treeKeys.addAll(Sets.newTreeSet(recipientKeys));

        Iterator iter = treeKeys.iterator();
        treeKeys = null;
        recipientKeys = null;
        senderKeys = null;
        List<Transaction> txs = new ArrayList<>();
        while (iter.hasNext()) {
            txs.add(this.map.get(iter.next()));
        }
        treeKeys = null;
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public int getTransactionsByAddressCount(String address) {
        Iterable senderKeys = Fun.filter(this.senderKey, address);
        Iterable recipientKeys = Fun.filter(this.recipientKey, address);

        Set<Tuple2<Integer, Integer>> treeKeys = new TreeSet<>();

        treeKeys.addAll(Sets.newTreeSet(senderKeys));
        treeKeys.addAll(Sets.newTreeSet(recipientKeys));

        return treeKeys.size();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Tuple2<Integer, Integer> getTransactionsAfterTimestamp(int startHeight, int numOfTx, String address) {
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

    public DCMap<Tuple2<Integer, Integer>, Transaction> getParentMap() {
        return this.parent;
    }

    @SuppressWarnings("rawtypes")
    public List<Transaction> findTransactions(String address, String sender, String recipient, final int minHeight,
                                              final int maxHeight, int type, int service, boolean desc, int offset, int limit) {
        Iterable keys = findTransactionsKeys(address, sender, recipient, minHeight, maxHeight, type, service, desc,
                offset, limit);

        Iterator iter = keys.iterator();
        keys = null;
        List<Transaction> txs = new ArrayList<>();

        while (iter.hasNext()) {
            txs.add(this.map.get(iter.next()));
        }
        iter = null;
        return txs;
    }

    @SuppressWarnings("rawtypes")
    public int findTransactionsCount(String address, String sender, String recipient, final int minHeight,
                                     final int maxHeight, int type, int service, boolean desc, int offset, int limit) {
        Iterable keys = findTransactionsKeys(address, sender, recipient, minHeight, maxHeight, type, service, desc,
                offset, limit);
        return Iterables.size(keys);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Iterable findTransactionsKeys(String address, String sender, String recipient, final int minHeight,
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

        if (type == Transaction.ARBITRARY_TRANSACTION && service > -1) {
            treeKeys = Sets.filter(treeKeys, new Predicate<Tuple2<Integer, Integer>>() {
                @Override
                public boolean apply(Tuple2<Integer, Integer> key) {
                    ArbitraryTransaction tx = (ArbitraryTransaction) map.get(key);
                    return tx.getService() == service;
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    public byte[] getSignature(int hight, int seg) {

        return this.get(new Tuple2(hight, seg)).getSignature();

    }

    public Transaction getTransaction(byte[] signature) {
        return this.get(getDCSet().getTransactionFinalMapSigns().get(signature));

    }

    public Transaction getRecord(DCSet db, String refStr) {
        try {
            String[] strA = refStr.split("\\-");
            int height = Integer.parseInt(strA[0]);
            int seq = Integer.parseInt(strA[1]);

            return db.getTransactionFinalMap().getTransaction(height, seq);
        } catch (Exception e1) {
            try {
                return db.getTransactionFinalMap().getTransaction(Base58.decode(refStr));
            } catch (Exception e2) {
                return null;
            }
        }
    }

}
