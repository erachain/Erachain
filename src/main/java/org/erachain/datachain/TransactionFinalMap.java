package org.erachain.datachain;

//04/01 +- 

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.RCalculated;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.DBTab;
import org.erachain.database.FilteredByStringArray;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.BTreeKeySerializer.BasicKeySerializer;
import org.mapdb.*;
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
public class TransactionFinalMap extends DCUMap<Long, Transaction> implements FilteredByStringArray {

    private static int CUT_NAME_INDEX = 12;

    @SuppressWarnings("rawtypes")
    private NavigableSet senderKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet recipientKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet typeKey;

    @SuppressWarnings("rawtypes")
    private NavigableSet titleKey;

    //@SuppressWarnings("rawtypes")
    //private NavigableSet block_Key;
    // private NavigableSet <Tuple2<String,Tuple2<Integer,
    // Integer>>>signature_key;

    public TransactionFinalMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRANSACTION_TYPE);
        }
    }

    public TransactionFinalMap(TransactionFinalMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    protected void createIndexes() {
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Transaction> openMap(DB database) {

        // TREE MAP for sortable search
        BTreeMap<Long, Transaction> map = database.createTreeMap("height_seq_transactions")
                .keySerializer(BasicKeySerializer.BASIC)
                .valueSerializer(new TransactionSerializer())
                .counterEnable()
                .makeOrGet();

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return map;

        this.senderKey = database.createTreeSet("sender_txs").comparator(Fun.COMPARATOR).makeOrGet();

        // в БИНЕ внутри уникальные ключи создаются добавлением основного ключа
        Bind.secondaryKey(map, this.senderKey, new Function2<String, Long, Transaction>() {
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

        Bind.secondaryKeys(map, this.recipientKey,
                new Function2<String[], Long, Transaction>() {
                    @Override
                    public String[] run(Long key, Transaction val) {
                        List<String> recps = new ArrayList<String>();

                        val.setDC((DCSet)databaseSet);

                        for (Account acc : val.getRecipientAccounts()) {
                            // make UNIQUE key??  + val.viewTimestamp()
                            recps.add(acc.getAddress());
                        }
                        String[] ret = new String[recps.size()];
                        ret = recps.toArray(ret);
                        return ret;
                    }
                });

        this.typeKey = database.createTreeSet("address_type_txs").comparator(Fun.COMPARATOR).makeOrGet();

        Bind.secondaryKeys(map, this.typeKey,
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
                                Array.newInstance(Fun.Tuple2.class, recps.size());
                        ret = recps.toArray(ret);
                        return ret;
                    }
                });

        this.titleKey = database.createTreeSet("title_type_txs").comparator(Fun.COMPARATOR).makeOrGet();

        // в БИНЕ внутри уникальные ключи создаются добавлением основного ключа
        Bind.secondaryKeys(map, this.titleKey,
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

        return map;

    }

    @Override
    protected void getMap() {
        // OPEN MAP
        map = openMap(database);
    }

    @Override
    protected void getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        // OPEN MAP
        this.getMap();
    }

    @Override
    protected Transaction getDefaultValue() {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Integer height) {
        BTreeMap map = (BTreeMap) this.map;
        // GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        Collection<Long> keys = ((BTreeMap<Long, Transaction>) map)
                .subMap(Transaction.makeDBRef(height, 0),
                        Transaction.makeDBRef(height, Integer.MAX_VALUE)).keySet();

        // DELETE TRANSACTIONS
        for (Long key : keys) {
            if (this.contains(key))
                this.remove(key);
        }
        keys = null;
    }

    public void delete(Integer height, Integer seq) {
        this.remove(Transaction.makeDBRef(height, seq));
    }

    public boolean add(Integer height, Integer seq, Transaction transaction) {
        return this.set(Transaction.makeDBRef(height, seq), transaction);
    }

    public Transaction get(Integer height, Integer seq) {
        return this.get(Transaction.makeDBRef(height, seq));
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
        Transaction item;
        Long key;
        while (iter.hasNext() && (limit == 0 || counter < limit)) {

            key = (Long) iter.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            item = this.map.get(key);
            item.setDC((DCSet)databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

            txs.add(item);
            counter++;
        }
        iter = null;
        return txs;
    }

    public Collection<Transaction> getTransactionsByBlock(Integer block) {
        return getTransactionsByBlock(block, 0, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsByBlock(Integer block, int offset, int limit) {

        Collection<Long> keys = ((BTreeMap) map)
                .subMap(Transaction.makeDBRef(block, 0), Transaction.makeDBRef(block, Integer.MAX_VALUE)).keySet();

        if (offset > 0)
            keys = (Collection) Iterables.skip(keys, offset);

        if (limit > 0)
            keys = (Collection) Iterables.limit(keys, limit);

        List<Transaction> txs = new ArrayList<>();
        for (Long key : keys) {
            txs.add(map.get(key));
        }
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
        Transaction item;
        Long key;
        while (iter.hasNext() && (limit == 0 || counter < limit)) {
            key = (Long) iter.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            item = this.map.get(key);
            item.setDC((DCSet)databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

            txs.add(item);
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
        Transaction item;
        Long key;
        while (iter.hasNext() && (limit == 0 || counter < limit)) {
            key = (Long) iter.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            item = this.map.get(key);
            item.setDC((DCSet)databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

            txs.add(item);
            counter++;
        }
        iter = null;
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Transaction> getTransactionsByTitleAndType(String filter, Integer type, int limit, boolean descending) {

        Iterable keys = Fun.filter(this.titleKey, new Tuple2<String, Integer>(filter, type), true,
                new Tuple2<String, Integer>(filter + "я", //new String(new byte[]{(byte)254}),
                        type), true);

        Iterator iter = keys.iterator();

        List<Transaction> txs = new ArrayList<>();
        int counter = 0;
        Transaction item;
        Long key;
        while (iter.hasNext() && (limit == 0 || counter < limit)) {
            key = (Long) iter.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            item = this.map.get(key);
            item.setDC((DCSet)databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

            txs.add(item);
            counter++;
        }
        iter = null;
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterable getKeysByTitleAndType(String filter, Integer type,  int offset, int limit) {

        String filtrLower = filter.toLowerCase();

        Iterable keys = Fun.filter(this.titleKey,
                new Tuple2<String, Integer>(filtrLower,
                        type==0?0:type), true,
                new Tuple2<String, Integer>(filtrLower + new String(new byte[]{(byte)254}),
                        type==0?Integer.MAX_VALUE:type), true);

        if (offset > 0)
            keys = Iterables.skip(keys, offset);

        if (limit > 0)
            keys = Iterables.limit(keys, limit);

        return keys;

    }


    public Pair<Integer, HashSet<Long>> getKeysByFilterAsArrayRecurse(int step, String[] filterArray) {

        Iterable keys;

        String stepFilter = filterArray[step];
        if (!stepFilter.endsWith("!")) {
            // это сокращение для диаппазона
            if (stepFilter.length() < 5) {
                // ошибка - ищем как полное слово
                keys = Fun.filter(this.titleKey,
                        new Tuple2<String, Integer>(stepFilter, 0), true,
                        new Tuple2<String, Integer>(stepFilter, Integer.MAX_VALUE), true);
            } else {

                if (stepFilter.length() > CUT_NAME_INDEX) {
                    stepFilter = stepFilter.substring(0, CUT_NAME_INDEX);
                }

                // поиск диаппазона
                keys = Fun.filter(this.titleKey,
                        new Tuple2<String, Integer>(stepFilter, 0), true,
                        new Tuple2<String, Integer>(stepFilter + new String(new byte[]{(byte) 254}), Integer.MAX_VALUE), true);
            }

        } else {
            // поиск целиком

            stepFilter = stepFilter.substring(0, stepFilter.length() -1);

            if (stepFilter.length() > CUT_NAME_INDEX) {
                stepFilter = stepFilter.substring(0, CUT_NAME_INDEX);
            }

            keys = Fun.filter(this.titleKey,
                    new Tuple2<String, Integer>(stepFilter, 0), true,
                    new Tuple2<String, Integer>(stepFilter, Integer.MAX_VALUE), true);
        }

        if (step > 0) {

            // погнали в РЕКУРСИЮ
            Pair<Integer, HashSet<Long>> result = getKeysByFilterAsArrayRecurse(--step, filterArray);

            if (result.getA() > 0) {
                return result;
            }

            // в рекурсии все хорошо - соберем ключи
            Iterator iterator = keys.iterator();
            HashSet<Long> hashSet = result.getB();
            HashSet<Long> andHashSet = new HashSet<Long>();

            // берем только совпадающие в обоих списках
            while (iterator.hasNext()) {
                Long key = (Long) iterator.next();
                if (hashSet.contains(key)) {
                    andHashSet.add(key);
                }
            }

            return new Pair<>(0, andHashSet);

        } else {

            // последний шаг - просто все добавим
            Iterator iterator = keys.iterator();
            HashSet<Long> hashSet = new HashSet<>();
            while (iterator.hasNext()) {
                Long key = (Long) iterator.next();
                hashSet.add(key);
            }

            return new Pair<Integer, HashSet<Long>>(0, hashSet);

        }

    }

    /**
     * Делает поиск по нескольким ключам по Заголовкам и если ключ с ! - надо найти только это слово
     * а не как фильтр. Иначе слово принимаем как фильтр на диаппазон
     * и его длинна должна быть не мнее 5-ти символов. Например:
     * "Ермолаев Дмитр." - Найдет всех Ермолаев с Дмитр....
     * @param filter
     * @param offset
     * @param limit
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Pair<String, Iterable> getKeysIteratorByFilterAsArray(String filter, int offset, int limit) {

        String[] filterArray = filter.toLowerCase().split(DCSet.SPLIT_CHARS);

        Pair<Integer, HashSet<Long>> result = getKeysByFilterAsArrayRecurse(filterArray.length - 1, filterArray);
        if (result.getA() > 0) {
            return new Pair<>("Error: filter key at " + (result.getA() - 1000) + "pos has length < 5", null);
        }

        HashSet<Long> hashSet = result.getB();

        Iterable iterable;

        if (offset > 0)
            iterable = Iterables.skip(hashSet, offset);
        else
            iterable = hashSet;

        if (limit > 0)
            iterable = Iterables.limit(iterable, limit);

        return new Pair<>(null, iterable);

    }

    // get list items in name substring str
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Long> getKeysByFilterAsArray(String filter, int offset, int limit) {

        if (filter == null || filter.isEmpty()){
            return new ArrayList<>();
        }

        Pair<String, Iterable> resultKeys = getKeysIteratorByFilterAsArray(filter, offset, limit);
        if (resultKeys.getA() != null) {
            return new ArrayList<>();
        }

        List<Long> result = new ArrayList<>();

        Iterator<Long> iterator = resultKeys.getB().iterator();

        while (iterator.hasNext()) {
            Long key = iterator.next();
            Transaction item = get(key);
            if (item != null)
                result.add(key);
        }

        return result;
    }

    // get list items in name substring str
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getByFilterAsArray(String filter, int offset, int limit) {

        if (filter == null || filter.isEmpty()){
            return new ArrayList<>();
        }

        Pair<String, Iterable> resultKeys = getKeysIteratorByFilterAsArray(filter, offset, limit);
        if (resultKeys.getA() != null) {
            return new ArrayList<>();
        }

        List<Transaction> result = new ArrayList<>();

        Iterator<Long> iterator = resultKeys.getB().iterator();

        while (iterator.hasNext()) {
            Transaction item = get(iterator.next());
            result.add(item);
        }

        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Iterator getIteratorByAddress(String address) {
        Iterable senderKeys = Fun.filter(this.senderKey, address);
        Iterable recipientKeys = Fun.filter(this.recipientKey, address);

        Set<Long> treeKeys = new TreeSet<>();

        treeKeys.addAll(Sets.newTreeSet(senderKeys));
        treeKeys.addAll(Sets.newTreeSet(recipientKeys));

        return ((TreeSet<Long>) treeKeys).descendingIterator();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Transaction> getTransactionsByAddressLimit(String address, int limit, boolean noForge) {
        Iterator iterator = getIteratorByAddress(address);
        List<Transaction> txs = new ArrayList<>();
        Transaction item;
        Long key;
        while (iterator.hasNext() && (limit == -1 || limit > 0)) {
            key = (Long) iterator.next();
            item = this.map.get(key);
            if (noForge && item.getType() == Transaction.CALCULATED_TRANSACTION) {
                RCalculated tx = (RCalculated) item;
                String mess = tx.getMessage();
                if (mess != null && mess.equals("forging")) {
                    continue;
                }
            }

            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            item.setDC((DCSet)databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

            --limit;

            txs.add(item);
        }
        return txs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public int getTransactionsByAddressCount(String address) {
        Iterable senderKeys = Fun.filter(this.senderKey, address);
        Iterable recipientKeys = Fun.filter(this.recipientKey, address);

        Set<Long> treeKeys = new TreeSet<>();

        treeKeys.addAll(Sets.newTreeSet(senderKeys));
        treeKeys.addAll(Sets.newTreeSet(recipientKeys));

        return treeKeys.size();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Long getTransactionsAfterTimestamp(int startHeight, int numOfTx, String address) {
        Iterable keys = Fun.filter(this.recipientKey, address);
        Iterator iter = keys.iterator();
        int prevKey = startHeight;
        while (iter.hasNext()) {
            Long key = (Long) iter.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            if (pair.a >= startHeight) {
                if (pair.a != prevKey) {
                    numOfTx = 0;
                }
                prevKey = pair.a;
                if (pair.b > numOfTx)
                    iter = null;
                return key;
            }
        }
        return null;
    }

    public DBTab<Long, Transaction> getParentMap() {
        return this.parent;
    }

    @SuppressWarnings("rawtypes")
    public List<Transaction> findTransactions(String address, String sender, String recipient, final int minHeight,
                                              final int maxHeight, int type, int service, boolean desc, int offset, int limit) {
        Iterable keys = findTransactionsKeys(address, sender, recipient, minHeight, maxHeight, type, service, desc,
                offset, limit);

        Iterator iter = keys.iterator();
        List<Transaction> txs = new ArrayList<>();
        Transaction item;
        Long key;

        while (iter.hasNext()) {
            key = (Long) iter.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            item = this.map.get(key);
            item.setDC((DCSet)databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);
            txs.add(item);
        }
        return txs;
    }

    @SuppressWarnings("rawtypes")
    public int findTransactionsCount(String address, String sender, String recipient, final int minHeight,
                                     final int maxHeight, int type, int service, boolean desc, int offset, int limit) {
        Iterable keys = findTransactionsKeys(address, sender, recipient, minHeight, maxHeight, type, service, desc,
                offset, limit);
        return Iterables.size(keys);
    }

    /**
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Iterable findTransactionsKeys(String address, String sender, String recipient, final int minHeight,
                                         final int maxHeight, int type, final int service, boolean desc, int offset, int limit) {
        Iterable senderKeys = null;
        Iterable recipientKeys = null;
        Set<Long> treeKeys = new TreeSet<>();

        if (address != null) {
            sender = address;
            recipient = address;
        }

        if (sender == null && recipient == null) {
            return treeKeys;
        }

        if (sender != null) {
            if (type != 0) {
                senderKeys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(sender, type));
            } else {
                senderKeys = Fun.filter(this.senderKey, sender);
            }
        }

        if (recipient != null) {
            if (type != 0) {
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
            treeKeys = Sets.filter(treeKeys, new Predicate<Long>() {
                @Override
                public boolean apply(Long key) {
                    Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                    return (minHeight == 0 || pair.a >= minHeight) && (maxHeight == 0 || pair.a <= maxHeight);
                }
            });
        }

        if (false && type == Transaction.ARBITRARY_TRANSACTION && service != 0) {
            treeKeys = Sets.filter(treeKeys, new Predicate<Long>() {
                @Override
                public boolean apply(Long key) {
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

        return this.get(Transaction.makeDBRef(hight, seg)).getSignature();

    }

    public Transaction getRecord(String refStr) {
        try {
            String[] strA = refStr. split("\\-");
            int height = Integer.parseInt(strA[0]);
            int seq = Integer.parseInt(strA[1]);

            return this.get(height, seq);
        } catch (Exception e1) {
            try {
                return this.get(Base58.decode(refStr));
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public Transaction get(byte[] signature) {
        return this.get(((DCSet)databaseSet).getTransactionFinalMapSigns().get(signature));
    }

    public Transaction get(Long key) {
        // [167726]
        Transaction item = super.get(key);
        if (item == null)
            return null;

        Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
        item.setDC((DCSet)databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);
        return item;
    }

    public boolean set(Transaction transaction) {
        return super.set(transaction.getDBRef(), transaction);
    }

}
