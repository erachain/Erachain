package org.erachain.datachain;

//04/01 +- 

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.RCalculated;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.*;
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

import java.io.IOException;
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

    public TransactionFinalMapImpl(int dbsUsed, DCSet databaseSet, DB database, boolean sizeEnable) {
        super(dbsUsed, databaseSet, database, sizeEnable, null, null);

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
                    map = new TransactionFinalSuitMapDBFork((TransactionFinalMap) parent, databaseSet);
                    break;
                case DBS_ROCK_DB:
                    map = new TransactionFinalSuitRocksDBFork((TransactionFinalMap) parent, databaseSet);
                    break;
                default:
                    /// НЕЛЬЗЯ HashMap !!!  так как удаляем по фильтру блока тут в delete(Integer height)
                    // map = new NativeMapHashMapFork(parent, databaseSet, null);
                    /// - тоже нельзя так как удаление по номеру блока не получится
                    // map = new NativeMapTreeMapFork(parent, databaseSet, null, null);
                    map = new TransactionFinalSuitMapDBFork((TransactionFinalMap) parent, databaseSet);
            }
        }
    }

    @Override
    // TODO кстати показало что скорость Получить данные очень медллоеный при просчете РАЗМЕРА в getTransactionFinalMapSigns - может для РоксДБ оставить тут счетчик?
    public int size() {
        if (sizeEnable)
            return map.size();

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
    public List<Transaction> getTransactionsByRecipient(byte[] addressShort) {
        return getTransactionsByRecipient(addressShort, 0);
    }

    public List<Transaction> getTransactionsByRecipient(String address) {
        return getTransactionsByRecipient(Account.makeShortBytes(address), 0);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsByRecipient(byte[] addressShort, int limit) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getIteratorByRecipient(addressShort)) {
            List<Transaction> txs = new ArrayList<>();
            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {

                key = iterator.next();
                Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                item = this.map.get(key);
                item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

                txs.add(item);
                counter++;
            }
            return txs;
        } catch (IOException e) {
            return null;
        }
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

        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getBlockIterator(block)) {

            if (offset > 0)
                Iterators.advance(iterator, offset);

            List<Transaction> txs = new ArrayList<>();
            int count = limit;
            while (iterator.hasNext()) {
                if (limit > 0 && --count < 0)
                    break;

                txs.add(map.get(iterator.next()));
            }
            return txs;
        } catch (IOException e) {
            return null;
        }

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsByCreator(byte[] addressShort, int limit, int offset) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        try (IteratorCloseable iterator = ((TransactionFinalSuit) map).getIteratorByCreator(addressShort)) {
            List<Transaction> txs = new ArrayList<>();
            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {
                if (offset > 0) {
                    offset--;
                    continue;
                }
                key = (Long) iterator.next();
                Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                item = this.map.get(key);
                item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

                txs.add(item);
                counter++;
            }
            return txs;
        } catch (IOException e) {
            return null;
        }
    }

    public List<Transaction> getTransactionsByCreator(String address, int limit, int offset) {
        return getTransactionsByCreator(Account.makeShortBytes(address), limit, offset);
    }

    public List<Transaction> getTransactionsByCreator(byte[] addressShort, Long fromID, int limit, int offset) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        try (IteratorCloseable iterator = ((TransactionFinalSuit) map).getIteratorByCreator(addressShort, fromID)) {
            List<Transaction> txs = new ArrayList<>();
            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {
                if (offset > 0) {
                    offset--;
                    continue;
                }
                key = (Long) iterator.next();
                Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                item = this.map.get(key);
                item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

                txs.add(item);
                counter++;
            }
            return txs;
        } catch (IOException e) {
            return null;
        }
    }

    public List<Transaction> getTransactionsByCreator(String address, Long fromID, int limit, int offset) {
        return getTransactionsByCreator(Account.makeShortBytes(address), fromID, limit, offset);
    }

    /**
     * Поиск активности данного счета по Созданным трнзакция за данный промежуток времени
     *
     * @param addressShort
     * @param fromSeqNo
     * @param toSeqNo
     * @return
     */
    public boolean isCreatorWasActive(byte[] addressShort, Long fromSeqNo, int typeTX, Long toSeqNo) {
        // на счете должна быть активность после fromSeqNo
        List<Transaction> txsFind;
        if (typeTX == 0) {
            txsFind = getTransactionsByCreator(addressShort, fromSeqNo, 1, 0);
        } else {
            txsFind = getTransactionsByAddressAndType(addressShort, typeTX, fromSeqNo, 1, 0, true);
        }

        if (txsFind.isEmpty())
            return false;
        // если полный диаппазон задан то проверим вхождение - он может быть и отрицательным
        if (fromSeqNo != null && txsFind.get(0).getDBRef() > toSeqNo) {
            return false;
        }

        return true;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Transaction> getTransactionsByAddressAndType(byte[] addressShort, Integer type, int limit, int offset) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Transaction> txs = new ArrayList<>();
        try (IteratorCloseable iterator = ((TransactionFinalSuit) map).getIteratorByAddressAndType(addressShort, type)) {
            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {
                key = (Long) iterator.next();

                if (offset > 0) {
                    offset--;
                    continue;
                }


                Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                item = this.map.get(key);
                item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

                txs.add(item); // 628853-1
                counter++;
            }
        } catch (IOException e) {
        }
        return txs;
    }
    public List<Transaction> getTransactionsByAddressAndType(String address, Integer type, int limit, int offset) {
        return getTransactionsByAddressAndType(Account.makeShortBytes(address), type, limit, offset);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Long> getKeysByAddressAndType(byte[] addressShort, Integer type, Long fromID, int limit, int offset) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Long> keys = new ArrayList<>();
        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getIteratorByAddressAndTypeFrom(addressShort, type, fromID)) {
            int counter = 0;
            //Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {
                key = iterator.next();

                if (offset > 0) {
                    offset--;
                    continue;
                }

                //Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                //item = this.map.get(key);
                //item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

                //txs.add(item);
                keys.add(key);
                counter++;
            }
        } catch (IOException e) {
        }
        return keys;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsByAddressAndType(byte[] addressShort, Integer type, Long fromID, int limit, int offset, boolean onlyCreator) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Transaction> transactions = new ArrayList<>();
        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getIteratorByAddressAndTypeFrom(addressShort, type, fromID)) {
            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {
                key = iterator.next();

                Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                item = this.map.get(key);
                if (onlyCreator && item.getCreator() != null && !item.getCreator().equals(addressShort)) {
                    // пропустим всех кто не создатель
                    continue;
                }

                if (offset > 0) {
                    offset--;
                    continue;
                }

                item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

                transactions.add(item);
                counter++;
            }
        } catch (IOException e) {
        }
        return transactions;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Transaction> getTransactionsByTitleAndType(String filter, Integer type, int limit, boolean descending) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Transaction> txs = new ArrayList<>();
        try (IteratorCloseable iterator = ((TransactionFinalSuit)map).getIteratorByTitleAndType(filter, true, type)) {

            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {
                key = (Long) iterator.next();
                Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                item = this.map.get(key);
                item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

                txs.add(item);
                counter++;
            }
        } catch (IOException e) {
        }
        return txs;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IteratorCloseable<Long> getKeysByTitleAndType(String filter, Integer type, int offset, int limit) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getIteratorByTitleAndType(filter, true, type);

        if (offset > 0)
            Iterators.advance(iterator, offset);

        if (limit > 0)
            iterator = IteratorCloseableImpl.limit(iterator, limit);

        return iterator;

    }

    @Override
    public Pair<Integer, IteratorCloseable<Long>> getKeysByFilterAsArrayRecurse(int step, String[] filterArray) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        IteratorCloseable iterator;

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
            Pair<Integer, IteratorCloseable<Long>> result = getKeysByFilterAsArrayRecurse(--step, filterArray);

            if (result.getA() > 0) {
                return result;
            }

            // в рекурсии все хорошо - соберем ключи
            ///Iterator<Long> rescurseIterator = result.getB();
            ///iterator = Iterators.concat(iterator, rescurseIterator);
            // а этот Итератор.mergeSorted - он дублирует повторяющиеся значения индекса (( и делает пересортировку асинхронно - то есть тоже не ахти то что нужно
            iterator = new MergedIteratorNoDuplicates((Iterable) ImmutableList.of(iterator, result.getB()), Fun.COMPARATOR);
            ////Iterable<Long> mergedIterable = Iterables.mergeSorted((Iterable) ImmutableList.of(iterator, result.getB()), Fun.COMPARATOR);
            ////iterator = mergedIterable.iterator();


            return new Pair<>(0, iterator);

        } else {

            return new Pair<Integer, IteratorCloseable<Long>>(0, iterator);

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
    public Pair<String, IteratorCloseable<Long>> getKeysIteratorByFilterAsArray(String filter, int offset, int limit) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        String[] filterArray = filter.toLowerCase().split(DCSet.SPLIT_CHARS);

        Pair<Integer, IteratorCloseable<Long>> result = getKeysByFilterAsArrayRecurse(filterArray.length - 1, filterArray);
        if (result.getA() > 0) {
            return new Pair<>("Error: filter key at " + (result.getA() - 1000) + "pos has length < 5", null);
        }

        IteratorCloseable<Long> iterator = result.getB();

        if (offset > 0)
            Iterators.advance(iterator, offset);

        if (limit > 0)
            iterator = IteratorCloseableImpl.limit(iterator, limit);

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

        Pair<String, IteratorCloseable<Long>> resultKeys = getKeysIteratorByFilterAsArray(filter, offset, limit);
        if (resultKeys.getA() != null) {
            return new ArrayList<>();
        }

        List<Long> result = new ArrayList<>();

        try (IteratorCloseable<Long> iterator = resultKeys.getB()) {

            while (iterator.hasNext()) {
                Long key = iterator.next();
                Transaction item = get(key);
                if (item != null)
                    result.add(key);
            }
        } catch (IOException e) {
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

        Pair<String, IteratorCloseable<Long>> resultKeys = getKeysIteratorByFilterAsArray(filter, offset, limit);
        if (resultKeys.getA() != null) {
            return new ArrayList<>();
        }

        List<Transaction> result = new ArrayList<>();

        try (IteratorCloseable<Long> iterator = resultKeys.getB()) {

            while (iterator.hasNext()) {
                Transaction item = get(iterator.next());
                result.add(item);
            }
        } catch (IOException e) {
        }

        return result;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public IteratorCloseable getIteratorByAddress(byte[] addressShort) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        return ((TransactionFinalSuit)map).getIteratorByAddress(addressShort);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Transaction> getTransactionsByAddressLimit(byte[] addressShort, int limit, boolean noForge) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Transaction> txs = new ArrayList<>();
        try (IteratorCloseable iterator = getIteratorByAddress(addressShort)) {
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
                item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

                --limit;

                txs.add(item);
            }
        } catch (IOException e) {
        }
        return txs;
    }

    public List<Transaction> getTransactionsByAddressLimit(String address, int limit, boolean noForge) {
        return getTransactionsByAddressLimit(Account.makeShortBytes(address), limit, noForge);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public int getTransactionsByAddressCount(byte[] addressShort) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return 0;
        }
        try (IteratorCloseable iterator = getIteratorByAddress(addressShort)) {
            return Iterators.size(iterator);
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public Long getTransactionsAfterTimestamp(int startHeight, int numOfTx, byte[] addressShort) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        //Iterable keys = Fun.filter(this.recipientKey, address);
        //Iterator iter = keys.iterator();
        try (IteratorCloseable iterator = ((TransactionFinalSuit)map).getIteratorByRecipient(addressShort)) {
            int prevKey = startHeight;
            while (iterator.hasNext()) {
                Long key = (Long) iterator.next();
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
        } catch (IOException e) {
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

        List<Transaction> txs = new ArrayList<>();
        try (IteratorCloseable iterator = findTransactionsKeys(address, sender, recipient, minHeight, maxHeight,
                type, service, desc, offset, limit)) {

            Transaction item;
            Long key;

            while (iterator.hasNext()) {
                key = (Long) iterator.next();
                Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                item = this.map.get(key);
                item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);
                txs.add(item);
            }
        } catch (IOException e) {
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
        try (IteratorCloseable iterator = findTransactionsKeys(address, sender, recipient, minHeight, maxHeight,
                type, service, desc, offset, limit)) {
            return Iterators.size(iterator);
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * @param address
     * @param creator
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
    public IteratorCloseable findTransactionsKeys(String address, String creator, String recipient, final int minHeight,
                                                  final int maxHeight, int type, final int service, boolean desc, int offset, int limit) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }
        IteratorCloseable<Long> creatorKeys = null;
        IteratorCloseable<Long> recipientKeys = null;

        if (address != null) {
            creator = address;
            recipient = address;
        }

        if (creator == null && recipient == null) {
            return IteratorCloseableImpl.make(new TreeSet<Long>().iterator());
        }

        if (creator != null) {
            if (type != 0) {
                //creatorKeys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(creator, type));
                creatorKeys = ((TransactionFinalSuit)map)
                        .getIteratorByAddressAndType(Crypto.getInstance().getShortBytesFromAddress(creator), type);
            } else {
                //creatorKeys = Fun.filter(this.senderKey, creator);
                //int sizeS = Iterators.size(((TransactionFinalSuit)map).getIteratorBySender(creator));

                creatorKeys = ((TransactionFinalSuit)map)
                        .getIteratorByCreator(Crypto.getInstance().getShortBytesFromAddress(creator));
            }
        }

        if (recipient != null) {
            if (type != 0) {
                //recipientKeys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(recipient, type));
                recipientKeys = ((TransactionFinalSuit)map)
                        .getIteratorByAddressAndType(Crypto.getInstance().getShortBytesFromAddress(recipient), type);
            } else {
                //int sizeR = Iterators.size(((TransactionFinalSuit)map).getIteratorByRecipient(recipient));
                //recipientKeys = Fun.filter(this.recipientKey, recipient);
                recipientKeys = ((TransactionFinalSuit)map).getIteratorByRecipient(Crypto.getInstance().getShortBytesFromAddress(recipient));
            }
        }

        IteratorCloseable<Long> iterator;
        if (address != null || creator != null && recipient != null) {
            // просто добавляет в конец iterator = Iterators.concat(creatorKeys, recipientKeys);
            // вызывает ошибку преобразования типов iterator = Iterables.mergeSorted((Iterable) ImmutableList.of(creatorKeys, recipientKeys), Fun.COMPARATOR).iterator();
            // а этот Итератор.mergeSorted - он дублирует повторяющиеся значения индекса (( и делает пересортировку асинхронно - то есть тоже не ахти то что нужно
            // поэтому нужно удалить дубли
            iterator = new MergedIteratorNoDuplicates(ImmutableList.of(creatorKeys, recipientKeys), Fun.COMPARATOR);

        } else if (creator != null) {
            iterator = creatorKeys;
        } else if (recipient != null) {
            iterator = recipientKeys;
        } else {
            iterator = IteratorCloseableImpl.make(new TreeSet<Long>().iterator());
        }

        if (minHeight != 0 || maxHeight != 0) {
            iterator = IteratorCloseableImpl.make(Iterators.filter(iterator, new Predicate<Long>() {
                @Override
                public boolean apply(Long key) {
                    Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                    return (minHeight == 0 || pair.a >= minHeight) && (maxHeight == 0 || pair.a <= maxHeight);
                }
            }));
        }

        if (false && type == Transaction.ARBITRARY_TRANSACTION && service != 0) {
            iterator = IteratorCloseableImpl.make(Iterators.filter(iterator, new Predicate<Long>() {
                @Override
                public boolean apply(Long key) {
                    ArbitraryTransaction tx = (ArbitraryTransaction) map.get(key);
                    return tx.getService() == service;
                }
            }));
        }

        if (desc) {
            // нужно старый Итератор закрыть и в переменную закатывать новый итератора уже
            // иначе память может не освободитсья в РоксДБ
            try (IteratorCloseable iteratorForClose = iterator) {
                iterator = IteratorCloseableImpl.make(Lists.reverse(Lists.newArrayList(iteratorForClose)).iterator());
            } catch (IOException e) {
            }
        }

        Iterators.advance(iterator, offset);

        return limit > 0 ? IteratorCloseableImpl.make(Iterators.limit(iterator, limit)) : iterator;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public IteratorCloseable<Long> getBiDirectionAddressIterator(String address, Long fromSeqNo, boolean descending, int offset, int limit) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map)
                .getBiDirectionAddressIterator(Crypto.getInstance().getShortBytesFromAddress(address), fromSeqNo, descending);
        Iterators.advance(iterator, offset);

        return limit > 0 ? IteratorCloseableImpl.make(Iterators.limit(iterator, limit)) : iterator;

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Transaction> getTransactionsByAddressFromID(byte[] addressShort, Long fromSeqNo, int offset, int limit,
                                                            boolean noForge, boolean fillFullPage) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Transaction> txs = new ArrayList<>();

        if (offset < 0 || limit < 0) {
            if (limit < 0)
                limit = -limit;

            // надо отмотать назад (вверх) - то есть нашли точку и в обратном направлении пропускаем
            // и по пути сосздаем список обратный что нашли по обратнму итератору
            int offsetHere = -(offset + limit);
            try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getBiDirectionAddressIterator(addressShort, fromSeqNo, false)) {
                Transaction item;
                Long key;
                int skipped = 0;
                int count = 0;
                while (iterator.hasNext() && (limit <= 0 || count < limit)) {
                    key = iterator.next();
                    item = this.map.get(key);
                    if (noForge && item.getType() == Transaction.CALCULATED_TRANSACTION) {
                        RCalculated tx = (RCalculated) item;
                        String mess = tx.getMessage();
                        if (mess != null && mess.equals("forging")) {
                            continue;
                        }
                    }

                    if (offsetHere > 0 && skipped++ < offsetHere) {
                        continue;
                    }

                    Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                    item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

                    count++;

                    // обратный отсчет в списке
                    txs.add(0, item);
                }

                if (fillFullPage && limit > 0 && count < limit) {
                    // сюда пришло значит не полный список - дополним его
                    for (Transaction transaction: getTransactionsByAddressFromID(addressShort,
                            fromSeqNo, 1, limit - count, noForge, false)) {
                        txs.add(transaction);
                    }
                }

            } catch (IOException e) {
            }

        } else {

            try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getBiDirectionAddressIterator(addressShort, fromSeqNo, true)) {
                Transaction item;
                Long key;
                int skipped = 0;
                while (iterator.hasNext() && (limit == -1 || limit > 0)) {
                    key = iterator.next();
                    item = this.map.get(key);
                    if (noForge && item.getType() == Transaction.CALCULATED_TRANSACTION) {
                        RCalculated tx = (RCalculated) item;
                        String mess = tx.getMessage();
                        if (mess != null && mess.equals("forging")) {
                            continue;
                        }
                    }

                    if (offset > 0 && skipped++ < offset) {
                        continue;
                    }

                    Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                    item.setDC((DCSet) databaseSet, Transaction.FOR_NETWORK, pair.a, pair.b);

                    --limit;

                    txs.add(item);
                }
            } catch (IOException e) {
            }
        }
        return txs;
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