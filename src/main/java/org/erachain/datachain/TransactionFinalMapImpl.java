package org.erachain.datachain;

//04/01 +- 

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.RCalculated;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.TransactionFinalSuitMapDB;
import org.erachain.dbs.mapDB.TransactionFinalSuitMapDBFork;
import org.erachain.dbs.nativeMemMap.NativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.TransactionFinalSuitRocksDB;
import org.erachain.dbs.rocksDB.TransactionFinalSuitRocksDBFork;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.util.*;

import static org.erachain.database.IDB.DBS_MAP_DB;
import static org.erachain.database.IDB.DBS_ROCK_DB;

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
 * Потому что там создавался "руками" вторичный индекс и биндился, а тут встроенной MapDB штучкой с реверсными индексами
 * и там внутри цепляется Основной Ключ -
 * в БИНДЕ внутри уникальные ключи создаются добавлением основного ключа
 */
@Slf4j
public class TransactionFinalMapImpl extends DBTabImpl<Long, Transaction> implements TransactionFinalMap {

    private static int CUT_NAME_INDEX = 12;

    public TransactionFinalMapImpl(int dbsUsed, DCSet databaseSet, DB database) {
        super(dbsUsed, databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_TRANSACTION_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRANSACTION_TYPE);
        }
    }

    public TransactionFinalMapImpl(int dbsUsed, TransactionFinalMap parent, DCSet dcSet) {
        super(dbsUsed, parent, dcSet);
    }

    @Override
    public void openMap() {
        // OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new TransactionFinalSuitRocksDB(databaseSet, database, sizeEnable);
                    break;
                default:
                    map = new TransactionFinalSuitMapDB(databaseSet, database, sizeEnable);
            }
        } else {
            switch (dbsUsed) {
                case DBS_MAP_DB:
                    map = new TransactionFinalSuitMapDBFork((TransactionFinalMap) parent, databaseSet, sizeEnable);
                    break;
                case DBS_ROCK_DB:
                    map = new TransactionFinalSuitRocksDBFork((TransactionFinalMap) parent, databaseSet, sizeEnable);
                    break;
                default:
                    /// НЕЛЬЗЯ HashMap !!!  так как удаляем по фильтру блока тут в delete(Integer height)
                    // map = new NativeMapHashMapFork(parent, databaseSet, null);
                    /// - тоже нельзя так как удаление по номеру блока не получится
                    // map = new NativeMapTreeMapFork(parent, databaseSet, null, null);
                    map = new TransactionFinalSuitMapDBFork((TransactionFinalMap) parent, databaseSet, sizeEnable);
            }
        }
    }

    @Override
    public int size() {
        return ((DCSet) this.databaseSet).getTransactionFinalMapSigns().size();
    }
    /**
     * Это протокольный вызов - поэтому в форке он тоже бывает
     *
     * @param height
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Integer height) {

        if (BlockChain.CHECK_BUGS > 2 && height == 652627) {
            int tt = 1;
        }

        // TODO сделать удаление по фильтру разом - как у RocksDB - deleteRange(final byte[] beginKey, final byte[] endKey)
        if (map instanceof TransactionFinalSuit) {
            ((TransactionFinalSuit) map).deleteForBlock(height);
        } else if (map instanceof NativeMapTreeMapFork) {
            Iterator<Long> iterator = map.getIterator();
            while (iterator.hasNext()) {
                Long key = iterator.next();
                if (Transaction.parseDBRef(key).a.equals(height)) {
                    map.delete(key);
                }
            }
        } else {
            Long error = null;
            ++error;
        }

    }

    @Override
    public void delete(Integer height, Integer seq) {
        this.delete(Transaction.makeDBRef(height, seq));
    }

    @Override
    public void add(Integer height, Integer seq, Transaction transaction) {
        this.put(Transaction.makeDBRef(height, seq), transaction);
    }

    @Override
    public Transaction get(Integer height, Integer seq) {
        return this.get(Transaction.makeDBRef(height, seq));
    }

    @Override
    public List<Transaction> getTransactionsByRecipient(String address) {
        return getTransactionsByRecipient(address, 0);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsByRecipient(String address, int limit) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        //Iterable keys = Fun.filter(this.recipientKey, address);
        //Iterator iterator = keys.iterator();
        Iterator iterator = ((TransactionFinalSuit)map).getIteratorByRecipient(address);
        List<Transaction> txs = new ArrayList<>();
        int counter = 0;
        Transaction item;
        Long key;
        while (iterator.hasNext() && (limit == 0 || counter < limit)) {

            key = (Long) iterator.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            item = this.map.get(key);
            item.setDC((DCSet)databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

            txs.add(item);
            counter++;
        }
        return txs;
    }

    @Override
    public Collection<Transaction> getTransactionsByBlock(Integer block) {
        return getTransactionsByBlock(block, 0, 0);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsByBlock(Integer block, int offset, int limit) {

        if (parent != null) {
            return null;
        }

        Iterator<Long> iterator = ((TransactionFinalSuit)map).getBlockIterator(block);

        if (offset > 0)
            Iterators.advance(iterator, offset);

        if (limit > 0)
            iterator = Iterators.limit(iterator, limit);

        List<Transaction> txs = new ArrayList<>();
        while (iterator.hasNext()) {
            txs.add(map.get(iterator.next()));
        }
        return txs;

    }

    @Override
    public List<Transaction> getTransactionsBySender(String address) {
        return getTransactionsBySender(address, 0);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsBySender(String address, int limit) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        //Iterable keys = Fun.filter(this.senderKey, address);
        //Iterator iterator = keys.iterator();
        Iterator iterator = ((TransactionFinalSuit)map).getIteratorBySender(address);
        List<Transaction> txs = new ArrayList<>();
        int counter = 0;
        Transaction item;
        Long key;
        while (iterator.hasNext() && (limit == 0 || counter < limit)) {
            key = (Long) iterator.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            item = this.map.get(key);
            item.setDC((DCSet)databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

            txs.add(item);
            counter++;
        }
        return txs;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Transaction> getTransactionsByAddressAndType(String address, Integer type, int limit) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        //Iterable keys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(address, type));
        //Iterator iterator = keys.iterator();
        Iterator iterator = ((TransactionFinalSuit)map).getIteratorByAddressAndType(address, type);
        List<Transaction> txs = new ArrayList<>();
        int counter = 0;
        Transaction item;
        Long key;
        while (iterator.hasNext() && (limit == 0 || counter < limit)) {
            key = (Long) iterator.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            item = this.map.get(key);
            item.setDC((DCSet)databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

            txs.add(item); // 628853-1
            counter++;
        }
        return txs;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Transaction> getTransactionsByTitleAndType(String filter, Integer type, int limit, boolean descending) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        //Iterable keys = Fun.filter(this.titleKey, new Tuple2<String, Integer>(filter, type), true,
        //        new Tuple2<String, Integer>(filter + "я", //new String(new byte[]{(byte)254}),
        //                type), true);

        //Iterator iter = keys.iterator();
        Iterator iter = ((TransactionFinalSuit)map).getIteratorByTitleAndType(filter, true, type);

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
        return txs;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator getKeysByTitleAndType(String filter, Integer type, int offset, int limit) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        //String filtrLower = filter.toLowerCase();

        //Iterable keys = Fun.filter(this.titleKey,
        //        new Tuple2<String, Integer>(filtrLower,
        //                type==0?0:type), true,
        //        new Tuple2<String, Integer>(filtrLower + new String(new byte[]{(byte)254}),
        //                type==0?Integer.MAX_VALUE:type), true);

        Iterator iterator = ((TransactionFinalSuit) map).getIteratorByTitleAndType(filter, true, type);

        if (offset > 0)
            Iterators.advance(iterator, offset);

        if (limit > 0)
            iterator = Iterators.limit(iterator, limit);

        return iterator;

    }

    @Override
    public Pair<Integer, Iterator<Long>> getKeysByFilterAsArrayRecurse(int step, String[] filterArray) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        Iterator iterator;

        String stepFilter = filterArray[step];
        if (!stepFilter.endsWith("!")) {
            // это сокращение для диаппазона
            if (stepFilter.length() < 5) {
                // ошибка - ищем как полное слово
                //keys = Fun.filter(this.titleKey,
                //        new Tuple2<String, Integer>(stepFilter, 0), true,
                //        new Tuple2<String, Integer>(stepFilter, Integer.MAX_VALUE), true);
                iterator = ((TransactionFinalSuit)map).getIteratorByTitleAndType(stepFilter, false, 0);

            } else {

                if (stepFilter.length() > CUT_NAME_INDEX) {
                    stepFilter = stepFilter.substring(0, CUT_NAME_INDEX);
                }

                // поиск диаппазона
                //keys = Fun.filter(this.titleKey,
                //        new Tuple2<String, Integer>(stepFilter, 0), true,
                //        new Tuple2<String, Integer>(stepFilter + new String(new byte[]{(byte) 254}), Integer.MAX_VALUE), true);
                iterator = ((TransactionFinalSuit)map).getIteratorByTitleAndType(stepFilter, true, 0);

            }

        } else {
            // поиск целиком

            stepFilter = stepFilter.substring(0, stepFilter.length() -1);

            if (stepFilter.length() > CUT_NAME_INDEX) {
                stepFilter = stepFilter.substring(0, CUT_NAME_INDEX);
            }

            //keys = Fun.filter(this.titleKey,
            //        new Tuple2<String, Integer>(stepFilter, 0), true,
            //        new Tuple2<String, Integer>(stepFilter, Integer.MAX_VALUE), true);
            iterator = ((TransactionFinalSuit)map).getIteratorByTitleAndType(stepFilter, false, 0);
        }

        if (step > 0) {

            // погнали в РЕКУРСИЮ
            Pair<Integer, Iterator<Long>> result = getKeysByFilterAsArrayRecurse(--step, filterArray);

            if (result.getA() > 0) {
                return result;
            }

            // в рекурсии все хорошо - соберем ключи
            ///Iterator<Long> rescurseIterator = result.getB();
            ///iterator = Iterators.concat(iterator, rescurseIterator);
            Iterable<Long> mergedIterable = Iterables.mergeSorted((Iterable) ImmutableList.of(iterator, result.getB()), Fun.COMPARATOR);
            iterator = mergedIterable.iterator();

            return new Pair<>(0, iterator);

        } else {

            return new Pair<Integer, Iterator<Long>>(0, iterator);

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
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Pair<String, Iterator> getKeysIteratorByFilterAsArray(String filter, int offset, int limit) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        String[] filterArray = filter.toLowerCase().split(DCSet.SPLIT_CHARS);

        Pair<Integer, Iterator<Long>> result = getKeysByFilterAsArrayRecurse(filterArray.length - 1, filterArray);
        if (result.getA() > 0) {
            return new Pair<>("Error: filter key at " + (result.getA() - 1000) + "pos has length < 5", null);
        }

        Iterator<Long> iterator = result.getB();

        if (offset > 0)
            Iterators.advance(iterator, offset);

        if (limit > 0)
            iterator = Iterators.limit(iterator, limit);

        return new Pair<>(null, iterator);

    }

    // get list items in name substring str
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Long> getKeysByFilterAsArray(String filter, int offset, int limit) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        if (filter == null || filter.isEmpty()){
            return new ArrayList<>();
        }

        Pair<String, Iterator> resultKeys = getKeysIteratorByFilterAsArray(filter, offset, limit);
        if (resultKeys.getA() != null) {
            return new ArrayList<>();
        }

        List<Long> result = new ArrayList<>();

        Iterator<Long> iterator = resultKeys.getB();

        while (iterator.hasNext()) {
            Long key = iterator.next();
            Transaction item = get(key);
            if (item != null)
                result.add(key);
        }

        return result;
    }

    // get list items in name substring str
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getByFilterAsArray(String filter, int offset, int limit) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        if (filter == null || filter.isEmpty()){
            return new ArrayList<>();
        }

        Pair<String, Iterator> resultKeys = getKeysIteratorByFilterAsArray(filter, offset, limit);
        if (resultKeys.getA() != null) {
            return new ArrayList<>();
        }

        List<Transaction> result = new ArrayList<>();

        Iterator<Long> iterator = resultKeys.getB();

        while (iterator.hasNext()) {
            Transaction item = get(iterator.next());
            result.add(item);
        }

        return result;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Iterator getIteratorByAddress(String address) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        return ((TransactionFinalSuit)map).getIteratorByAddress(address);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Transaction> getTransactionsByAddressLimit(String address, int limit, boolean noForge) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

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

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public int getTransactionsByAddressCount(String address) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return 0;
        }

        return Iterators.size(getIteratorByAddress(address));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Long getTransactionsAfterTimestamp(int startHeight, int numOfTx, String address) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        //Iterable keys = Fun.filter(this.recipientKey, address);
        //Iterator iter = keys.iterator();
        Iterator iter = ((TransactionFinalSuit)map).getIteratorByRecipient(address);
        int prevKey = startHeight;
        while (iter.hasNext()) {
            Long key = (Long) iter.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            if (pair.a >= startHeight) {
                if (pair.a != prevKey) {
                    numOfTx = 0;
                }
                prevKey = pair.a;
                if (pair.b > numOfTx) {
                    return key;
                }
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<Transaction> findTransactions(String address, String sender, String recipient, final int minHeight,
                                              final int maxHeight, int type, int service, boolean desc, int offset, int limit) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        Iterator iterator = findTransactionsKeys(address, sender, recipient, minHeight, maxHeight, type, service, desc,
                offset, limit);

        List<Transaction> txs = new ArrayList<>();
        Transaction item;
        Long key;

        while (iterator.hasNext()) {
            key = (Long) iterator.next();
            Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            item = this.map.get(key);
            item.setDC((DCSet)databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);
            txs.add(item);
        }
        return txs;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public int findTransactionsCount(String address, String sender, String recipient, final int minHeight,
                                     final int maxHeight, int type, int service, boolean desc, int offset, int limit) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return 0;
        }
        Iterator keys = findTransactionsKeys(address, sender, recipient, minHeight, maxHeight, type, service, desc,
                offset, limit);
        return Iterators.size(keys);
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
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Iterator<Long> findTransactionsKeys(String address, String sender, String recipient, final int minHeight,
                                               final int maxHeight, int type, final int service, boolean desc, int offset, int limit) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }
        Iterator<Long> senderKeys = null;
        Iterator<Long> recipientKeys = null;
        Iterator<Long> iterator;

        if (address != null) {
            sender = address;
            recipient = address;
        }

        if (sender == null && recipient == null) {
            return new TreeSet<Long>().iterator();
        }

        if (sender != null) {
            if (type != 0) {
                //senderKeys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(sender, type));
                senderKeys = ((TransactionFinalSuit)map).getIteratorByAddressAndType(sender, type);
            } else {
                //senderKeys = Fun.filter(this.senderKey, sender);
                senderKeys = ((TransactionFinalSuit)map).getIteratorBySender(sender);
            }
        }

        if (recipient != null) {
            if (type != 0) {
                //recipientKeys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(recipient, type));
                recipientKeys = ((TransactionFinalSuit)map).getIteratorByAddressAndType(recipient, type);
            } else {
                //recipientKeys = Fun.filter(this.recipientKey, recipient);
                recipientKeys = ((TransactionFinalSuit)map).getIteratorByRecipient(recipient);
            }
        }

        if (address != null) {
            //iterator = Iterators.concat(senderKeys, recipientKeys);
            Iterable<Long> mergedIterable = Iterables.mergeSorted((Iterable) ImmutableList.of(senderKeys, recipientKeys), Fun.COMPARATOR);
            iterator = mergedIterable.iterator();
        } else if (sender != null && recipient != null) {
            iterator = senderKeys;
            Iterators.retainAll(iterator, Lists.newArrayList(recipientKeys));
        } else if (sender != null) {
            iterator = senderKeys;
        } else if (recipient != null) {
            iterator = recipientKeys;
        } else {
            iterator = new TreeSet<Long>().iterator();
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
            //iterator = ((TreeSet) iterator).descendingIterator();
            iterator = Lists.reverse(Lists.newArrayList(iterator)).iterator();
        }

        limit = (limit == 0) ? Iterators.size(iterator) : limit;
        Iterators.advance(iterator, offset);

        return Iterators.limit(iterator, limit);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public byte[] getSignature(int hight, int seg) {

        return this.get(Transaction.makeDBRef(hight, seg)).getSignature();

    }

    @Override
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

    @Override
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

    @Override
    public void put(Transaction transaction) {
        super.put(transaction.getDBRef(), transaction);
    }

}
