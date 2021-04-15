package org.erachain.datachain;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.GenesisRecord;
import org.erachain.core.transaction.RCalculated;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.PagedMap;
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

        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getIteratorByRecipient(addressShort, false)) {
            List<Transaction> txs = new ArrayList<>();
            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {

                item = get(iterator.next());
                item.setDC((DCSet) databaseSet, true);

                txs.add(item);
                counter++;
            }
            return txs;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public IteratorCloseable<Long> getIteratorByBlock(Integer block) {
        return ((TransactionFinalSuit) map).getBlockIterator(block);
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

                txs.add(get(iterator.next()));
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

        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getIteratorByCreator(addressShort, false)) {
            List<Transaction> txs = new ArrayList<>();
            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {
                if (offset > 0) {
                    offset--;
                    continue;
                }
                item = get(iterator.next());
                item.setDC((DCSet) databaseSet, true);

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

        if (BlockChain.TEST_DB > 0
            // теперь при множественных выплатах это протокольный индекс
            ///parent != null || Controller.getInstance().onlyProtocolIndexing
        ) {
            return null;
        }

        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getIteratorByCreator(addressShort, fromID, false)) {
            List<Transaction> txs = new ArrayList<>();
            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {
                if (offset > 0) {
                    offset--;
                    continue;
                }
                item = get(iterator.next());
                item.setDC((DCSet) databaseSet, true);

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

    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, boolean descending) {
        return ((TransactionFinalSuit) map).getIteratorByCreator(addressShort, descending);
    }

    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo, boolean descending) {
        return ((TransactionFinalSuit) map).getIteratorByCreator(addressShort, fromSeqNo, descending);
    }

    public IteratorCloseable<Long> getIteratorByCreator(byte[] addressShort, Long fromSeqNo, Long toSeqNo, boolean descending) {
        return ((TransactionFinalSuit) map).getIteratorByCreator(addressShort, fromSeqNo, toSeqNo, descending);
    }


    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer typeTX, Boolean isCreator, boolean descending) {
        return ((TransactionFinalSuit) map).getIteratorByAddressAndType(addressShort, typeTX, isCreator, descending);
    }

    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer typeTX, Boolean isCreator, Long fromID, boolean descending) {
        return ((TransactionFinalSuit) map).getIteratorByAddressAndType(addressShort, typeTX, isCreator, fromID, descending);
    }

    public IteratorCloseable<Long> getIteratorByAddressAndType(byte[] addressShort, Integer typeTX, Boolean isCreator, Long fromID, Long toID, boolean descending) {
        return ((TransactionFinalSuit) map).getIteratorByAddressAndType(addressShort, typeTX, isCreator, fromID, toID, descending);
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
        try (IteratorCloseable<Long> iterator =
                     typeTX == 0 ? getIteratorByCreator(addressShort, fromSeqNo, toSeqNo, false)
                             : getIteratorByAddressAndType(addressShort, typeTX, true, fromSeqNo, toSeqNo, false)) {
            if (!iterator.hasNext())
                return false;
            // если полный диаппазон задан то проверим вхождение - он может быть и отрицательным
            if (///fromSeqNo != null &&
                    toSeqNo != null && iterator.next() > toSeqNo) {
                return false;
            }
        } catch (IOException e) {
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
        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getIteratorByAddressAndType(addressShort, type, null, false)) {
            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {

                if (offset > 0) {
                    offset--;
                    continue;
                }

                item = get(iterator.next());
                item.setDC((DCSet) databaseSet, true);

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
    public List<Long> getKeysByAddressAndType(byte[] addressShort, Integer type, Boolean isCreator, Long fromID, int limit, int offset, boolean descending) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Long> keys = new ArrayList<>();
        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getIteratorByAddressAndType(addressShort, type, isCreator, fromID, false)) {
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
                //item = get(key);
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
    public List<Transaction> getTransactionsByAddressAndType(byte[] addressShort, Integer type, boolean onlyCreator, Long fromID, int limit, int offset) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Transaction> transactions = new ArrayList<>();
        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getIteratorByAddressAndType(addressShort, type, onlyCreator, fromID, false)) {
            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {

                item = get(iterator.next());
                if (onlyCreator && item.getCreator() != null && !item.getCreator().equals(addressShort)) {
                    // пропустим всех кто не создатель
                    continue;
                }

                if (offset > 0) {
                    offset--;
                    continue;
                }

                item.setDC((DCSet) databaseSet, true);

                transactions.add(item);
                counter++;
            }
        } catch (IOException e) {
        }
        return transactions;
    }

    /**
     * Поиск сразу по двум счетам - получателя и отправителя
     *
     * @param address_A_Short
     * @param address_B_Short
     * @param type
     * @param onlyCreator
     * @param fromID
     * @param limit
     * @param offset
     * @return
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsByAddressAndType(byte[] address_A_Short, Account address_B, Integer type,
                                                             boolean onlyCreator, Long fromID, int limit, int offset) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Transaction> transactions = new ArrayList<>();
        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getIteratorByAddressAndType(address_A_Short, type, onlyCreator, fromID, false)) {
            int counter = 0;
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == 0 || counter < limit)) {

                item = get(iterator.next());
                if (onlyCreator && item.getCreator() != null && !item.getCreator().equals(address_A_Short)) {
                    // пропустим всех кто не создатель
                    continue;
                }

                if (offset > 0) {
                    offset--;
                    continue;
                }

                item.setDC((DCSet) databaseSet, true);
                if (item.isInvolved(address_B)) {
                    transactions.add(item);
                    counter++;
                }
            }
        } catch (IOException e) {
        }
        return transactions;
    }

    /**
     * Если слово заканчивается на "!" - то поиск полностью слова
     * или если оно короче чем MIN_WORLD_INDEX, иначе поиск по началу
     *
     * @param words
     * @return
     */
    public Pair<String, Boolean>[] stepFilter(String[] words) {

        Pair[] result = new Pair[words.length];
        String word;
        for (int i = 0; i < words.length; i++) {
            word = words[i];
            if (word.endsWith("!")) {
                // принудительно поставили в конце "ПОИСК слова ПОЛНОСТЬЮ"
                word = word.substring(0, word.length() - 1);

                if (word.length() > CUT_NAME_INDEX) {
                    word = word.substring(0, CUT_NAME_INDEX);
                }
                result[i] = new Pair(word, false);

            } else {
                if (word.length() < WHOLE_WORLD_LENGTH) {
                    result[i] = new Pair<>(word, false);
                } else {
                    if (word.length() > CUT_NAME_INDEX) {
                        word = word.substring(0, CUT_NAME_INDEX);
                    }
                    result[i] = new Pair<>(word, true);
                }
            }
        }
        return result;
    }

    public int getTransactionsByTitleBetterIndex(Pair<String, Boolean>[] words, boolean descending) {

        // сперва выберем самый короткий набор
        // TODO нужно еще отсортировать по длинне слов - самые длинные сперва проверять - они короче список дадут поидее

        int betterSize = LIMIT_FIND_TITLE;
        int tmpSize;
        int betterIndex = 0;
        for (int i = 0; i < words.length; i++) {
            try (IteratorCloseable iterator = ((TransactionFinalSuit) map)
                    .getIteratorByTitle(words[i].getA(), words[i].getB(), null, null, descending)) {
                // ограничим максимальный перебор - иначе может затормозить
                tmpSize = Iterators.size(Iterators.limit(iterator, LIMIT_FIND_TITLE));
                if (tmpSize < betterSize) {
                    betterSize = tmpSize;
                    betterIndex = i;
                }
            } catch (IOException e) {
            }
        }

        return betterIndex;
    }

    /**
     * Делает поиск по нескольким ключам по Заголовкам и если ключ с ! - надо найти только это слово
     * а не как фильтр. Иначе слово принимаем как фильтр на диаппазон
     * и его длинна должна быть не мнее 5-ти символов. Например:
     * "Ермолаев Дмитр." - Найдет всех Ермолаев с Дмитр....
     *
     * @param filter     string of words
     * @param fromWord
     * @param fromSeqNo  transaction Type = 0 for all
     * @param offset
     * @param limit
     * @param descending
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsByTitle(String filter, String fromWord, Long fromSeqNo, int offset, int limit, boolean descending) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        if (filter == null || filter.isEmpty()) {
            return new ArrayList<>();
        }

        List<Transaction> result = new ArrayList<>();

        String[] filterArray = filter.toLowerCase().split(Transaction.SPLIT_CHARS);
        Pair<String, Boolean>[] words = stepFilter(filterArray);

        // сперва выберем самый короткий набор
        int betterIndex = getTransactionsByTitleBetterIndex(words, descending);

        return getTransactionsByTitleFromBetter(words, betterIndex, fromWord, fromSeqNo, offset, limit, descending);
    }

    /**
     * Короче тут для корректного поиска по ключам с совпадением по началу слова ни с движением по номерам записям
     * нужно обязательно передавать текущее слово в индексе на котром остановились и к нему прибавляем номер
     * Иначе не будет искать корректно и всегда будет на начало прыгать
     *
     * @param words
     * @param betterIndex
     * @param fromWord
     * @param fromSeqNo
     * @param offset
     * @param limit
     * @param descending
     * @return
     */
    public List<Transaction> getTransactionsByTitleFromBetter(Pair<String, Boolean>[] words, int betterIndex,
                                                              String fromWord, Long fromSeqNo, int offset, int limit, boolean descending) {

        List<Transaction> result = new ArrayList<>();

        try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map)
                .getIteratorByTitle(words[betterIndex].getA(), words[betterIndex].getB(), fromWord, fromSeqNo, descending)) {

            Long key;
            Transaction transaction;
            boolean txChecked;
            boolean wordChecked;
            DCSet dcSet = (DCSet) databaseSet;
            while (iterator.hasNext()) {
                key = iterator.next();
                transaction = get(key);
                if (transaction == null)
                    continue;

                // теперь проверим все слова в Заголовке
                transaction.setDC(dcSet);
                String[] titleArray = transaction.getTags();

                if (titleArray == null || titleArray.length < words.length)
                    continue;

                Pair<String, Boolean>[] txWords = stepFilter(titleArray);
                txChecked = true;
                for (int i = 0; i < words.length; i++) {
                    if (i == betterIndex) {
                        // это слово уже проверено - так как по нему индекс уже построен и мы по нему идем
                        continue;
                    }

                    wordChecked = false;
                    for (int k = 0; k < txWords.length; k++) {
                        if (txWords[k].getA().startsWith(words[i].getA())) {
                            wordChecked = true;
                            break;
                        }
                    }
                    if (!wordChecked) {
                        txChecked = false;
                        break;
                    }
                }

                if (!txChecked)
                    continue;

                if (offset > 0) {
                    offset--;
                    continue;
                }

                result.add(transaction);

                if (limit > 0) {
                    if (--limit == 0)
                        break;
                }

            }
        } catch (IOException e) {
        }

        return result;
    }

    public List<Transaction> getByFilterAsArray(String filter, Long fromSeqNo, int offset, int limit, boolean descending) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        if (filter == null || filter.isEmpty()) {
            return new ArrayList<>();
        }

        return getTransactionsByTitleFromID(filter, fromSeqNo, offset, limit, true);
    }

    protected String findFromFilterWord(String betterFilterWord, Long fromSeqNo) {
        if (fromSeqNo == null) {
            return null;
        }

        // теперь найдем текушее слово чтобы начать с него поиск
        // это нужно только если задан номер начала поиска - тогда будет искать верно
        Transaction txFrom = get(fromSeqNo);
        if (txFrom == null) {
            return null;
        }
        // теперь проверим все слова в Заголовке
        txFrom.setDC((DCSet) databaseSet);
        String[] titleArray = txFrom.getTags();
        if (titleArray == null)
            return null;

        for (int i = 0; i < titleArray.length; i++) {
            if (titleArray[i].startsWith(betterFilterWord)) {
                return titleArray[i];
            }
        }

        return null;
    }

    //@Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Transaction> getTransactionsByTitleFromID(String filter, Long fromSeqNo, int offset, int limit, boolean fillFullPage) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }
        if (filter == null || filter.isEmpty()) {
            return new ArrayList<>();
        }

        List<Transaction> txs = new ArrayList<>();

        String[] filterArray = filter.toLowerCase().split(Transaction.SPLIT_CHARS);
        Pair<String, Boolean>[] words = stepFilter(filterArray);

        // сперва выберем самый короткий набор
        int betterIndex = getTransactionsByTitleBetterIndex(words, false);

        // если нужно с заданного номера найти то нужно слово полностью взять для фильта а не начало
        String fromWord;
        if (words[betterIndex].getB() && fromSeqNo != null) {
            // поиск по фильтру и не с начала списка то
            fromWord = findFromFilterWord(words[betterIndex].getA(), fromSeqNo);
        } else {
            fromWord = null;
        }

        if (offset < 0 || limit < 0) {
            if (limit < 0)
                limit = -limit;

            if (limit <= 0 || limit > 10000)
                limit = 10000;


            // надо отмотать назад (вверх) - то есть нашли точку и в обратном направлении пропускаем
            // и по пути сосздаем список обратный что нашли по обратнму итератору
            int offsetHere = -(offset + limit);

            List<Transaction> txsReverse = getTransactionsByTitleFromBetter(words, betterIndex, fromWord, fromSeqNo, offsetHere, limit, false);
            int count = txsReverse.size();
            for (Transaction transaction : txsReverse) {
                txs.add(0, transaction);
            }

            if (fillFullPage && fromSeqNo != null && fromSeqNo != 0 && limit > 0 && count < limit) {
                // сюда пришло значит не полный список - дополним его
                // и тут идем в обратку

                for (Transaction transaction : getTransactionsByTitleFromBetter(words, betterIndex,
                        fromWord, fromSeqNo, 0, limit - count, true // здесь обратный список так как в обратну надо задать
                )
                ) {
                    boolean exist = false;
                    for (Transaction txHere : txs) {
                        if (transaction.equals(txHere)) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        txs.add(transaction);
                    }
                }
            }

        } else {

            if (limit <= 0 || limit > 10000)
                limit = 10000;

            txs = getTransactionsByTitleFromBetter(words, betterIndex, fromWord, fromSeqNo, offset, limit, true);
            int count = txs.size();

            if (fillFullPage && fromSeqNo != null && fromSeqNo != 0 && limit > 0 && count < limit) {
                // сюда пришло значит не полный список - дополним его
                int index = 0;
                int limitLeft = limit - count;
                for (Transaction transaction : getTransactionsByTitleFromBetter(words, betterIndex,
                        fromWord, fromSeqNo, -(limitLeft + (count > 0 ? 1 : 0)), limitLeft, false)) {
                    boolean exist = false;
                    for (Transaction txHere : txs) {
                        if (transaction.equals(txHere)) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        txs.add(0, transaction);
                    }
                }
            }

        }
        return txs;
    }

    public class PagedTXMap extends PagedMap<Long, Transaction> {
        DCSet dcSet;
        boolean noForge;
        int forgedCount = 0;

        public PagedTXMap(DCSet dcSet, DBTabImpl mapImpl, boolean noForge) {
            super(mapImpl);
            this.dcSet = dcSet;
            this.noForge = noForge;
        }

        @Override
        public void rowCalc() {
            currentRow.setDC(dcSet);
        }

        @Override
        public boolean filerRows() {
            if (noForge && currentRow.getType() == Transaction.CALCULATED_TRANSACTION) {
                RCalculated tx = (RCalculated) currentRow;
                String mess = tx.getMessage();
                if (mess != null && mess.equals("forging")) {
                    if (forgedCount < 100) {
                        // skip all but not 100
                        forgedCount++;
                        return true;
                    } else {
                        forgedCount = 0;
                    }
                }
            }
            return false;
        }

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Transaction> getTransactionsFromID(Long fromSeqNo, int offset, int limit,
                                                   boolean noForge, boolean fillFullPage) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        if (true) {
            PagedMap<Long, Transaction> pager = new PagedTXMap(DCSet.getInstance(), this, noForge);
            return pager.getPageList(fromSeqNo, offset, limit, fillFullPage);
        } else {

            List<Transaction> txs = new ArrayList<>();
            int forgedCount = 0;
            long timeOut = System.currentTimeMillis();

            if (offset < 0 || limit < 0) {
                if (limit < 0)
                    limit = -limit;

                // надо отмотать назад (вверх) - то есть нашли точку и в обратном направлении пропускаем
                // и по пути сосздаем список обратный что нашли по обратнму итератору
                int offsetHere = -(offset + limit);
                try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getBiDirectionIterator_old(fromSeqNo, false)) {
                    Transaction item;
                    Long key;
                    int skipped = 0;
                    int count = 0;
                    while (iterator.hasNext() && (limit <= 0 || count < limit)) {
                        key = iterator.next();
                        item = get(key);
                        if (noForge && item.getType() == Transaction.CALCULATED_TRANSACTION) {
                            RCalculated tx = (RCalculated) item;
                            String mess = tx.getMessage();
                            if (mess != null && mess.equals("forging")) {
                                if (forgedCount < 100) {
                                    // skip all but not 100
                                    forgedCount++;
                                    continue;
                                } else {
                                    if (System.currentTimeMillis() - timeOut > 5000) {
                                        break;
                                    }
                                    forgedCount = 0;
                                }
                            }
                        }

                        if (offsetHere > 0 && skipped++ < offsetHere) {
                            continue;
                        }

                        item.setDC((DCSet) databaseSet, true);

                        count++;

                        // обратный отсчет в списке
                        txs.add(0, item);
                    }

                    if (fillFullPage && fromSeqNo != null && fromSeqNo != 0 && limit > 0 && count < limit) {
                        // сюда пришло значит не полный список - дополним его
                        for (Transaction transaction : getTransactionsFromID(fromSeqNo,
                                0, limit - count, noForge, false)) {
                            boolean exist = false;
                            for (Transaction txHere : txs) {
                                if (transaction.equals(txHere)) {
                                    exist = true;
                                    break;
                                }
                            }
                            if (!exist) {
                                txs.add(transaction);
                            }
                        }
                    }

                } catch (IOException e) {
                }

            } else {

                try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getBiDirectionIterator_old(fromSeqNo, true)) {
                    Transaction item;
                    Long key;
                    int skipped = 0;
                    int count = 0;
                    while (iterator.hasNext() && (limit <= 0 || count < limit)) {
                        key = iterator.next();
                        item = get(key);
                        if (noForge && item.getType() == Transaction.CALCULATED_TRANSACTION) {
                            RCalculated tx = (RCalculated) item;
                            String mess = tx.getMessage();
                            if (mess != null && mess.equals("forging")) {
                                if (forgedCount < 100) {
                                    // skip all but not 100
                                    forgedCount++;
                                    continue;
                                } else {
                                    if (System.currentTimeMillis() - timeOut > 5000) {
                                        break;
                                    }
                                    forgedCount = 0;
                                }
                            }
                        }

                        if (offset > 0 && skipped++ < offset) {
                            continue;
                        }

                        item.setDC((DCSet) databaseSet, true);

                        count++;

                        txs.add(item);
                    }

                    if (fillFullPage && fromSeqNo != null && fromSeqNo != 0 && limit > 0 && count < limit) {
                        // сюда пришло значит не полный список - дополним его
                        int index = 0;
                        int limitLeft = limit - count;
                        for (Transaction transaction : getTransactionsFromID(fromSeqNo,
                                -(limitLeft + (count > 0 ? 1 : 0)), limitLeft, noForge, false)) {
                            boolean exist = false;
                            for (Transaction txHere : txs) {
                                if (transaction.equals(txHere)) {
                                    exist = true;
                                    break;
                                }
                            }
                            if (!exist) {
                                txs.add(index++, transaction);
                            }
                        }
                    }

                } catch (IOException e) {
                }
            }
            return txs;
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public IteratorCloseable getIteratorByAddress(byte[] addressShort, Long fromID, boolean descending) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        return ((TransactionFinalSuit) map).getBiDirectionAddressIterator(addressShort, fromID, descending);
    }

    /**
     * @param addressShort if Null - [type] and [isCreator] must be Null too.
     * @param type         if Null - use all types and [isCreator] must be Null too
     * @param isCreator    if True - only as creator, if False - only as recipient, if Null - all variants;
     * @param fromID       if Null - from begin
     * @param offset
     * @param limit
     * @param noForge      if True - skip forging transactions but not each of 100
     * @param descending
     * @return
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    // TODO ERROR - not use PARENT MAP and DELETED in FORK
    public List<Transaction> getTransactionsByAddressLimit(byte[] addressShort, Integer type, Boolean isCreator, Long fromID, int offset,
                                                           int limit, boolean noForge, boolean descending) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        int forgedCount = 0;
        long timeOut = System.currentTimeMillis();

        List<Transaction> txs = new ArrayList<>();
        try (IteratorCloseable iterator = addressShort == null ?
                type == null || type == 0 ?
                        getIterator(fromID, descending)
                        : null
                : type == null || type == 0 ?
                isCreator == null ?
                        getIteratorByAddress(addressShort, fromID, descending)
                        : null
                : getIteratorByAddressAndType(addressShort, type, isCreator, fromID, descending)) {
            Transaction item;
            Long key;
            while (iterator.hasNext() && (limit == -1 || limit > 0)) {
                key = (Long) iterator.next();
                item = get(key);
                if (noForge && item.getType() == Transaction.CALCULATED_TRANSACTION) {
                    RCalculated tx = (RCalculated) item;
                    String mess = tx.getMessage();
                    if (mess != null && mess.equals("forging")) {
                        if (forgedCount < 100) {
                            // skip all but not 100
                            forgedCount++;
                            continue;
                        } else {
                            if (System.currentTimeMillis() - timeOut > 1000) {
                                break;
                            }
                            forgedCount = 0;
                        }
                    }
                }

                if (offset > 0) {
                    --offset;
                    continue;
                }

                item.setDC((DCSet) databaseSet, true);

                --limit;

                txs.add(item);
            }
        } catch (IOException e) {
        }
        return txs;
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
        try (IteratorCloseable iterator = ((TransactionFinalSuit) map).getIteratorByRecipient(addressShort, false)) {
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
                                              final int maxHeight, int type, int service, boolean desc, int offset, int limit, Long fromSeqNo) {

        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }

        List<Transaction> txs = new ArrayList<>();
        try (IteratorCloseable<Long> iterator = findTransactionsKeys(address, sender, recipient, fromSeqNo, minHeight, maxHeight,
                type, service, desc, offset, limit)) {

            Transaction item;
            Long key;

            while (iterator.hasNext()) {
                item = get(iterator.next());
                item.setDC((DCSet) databaseSet, true);
                txs.add(item);
            }
        } catch (IOException e) {
        }
        return txs;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public int findTransactionsCount(String address, String sender, String recipient, Long fromSeqNo, final int minHeight,
                                     final int maxHeight, int type, int service, boolean desc, int offset, int limit) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return 0;
        }
        try (IteratorCloseable iterator = findTransactionsKeys(address, sender, recipient, fromSeqNo, minHeight, maxHeight,
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
     * @param fromSeqNo
     * @param minHeight
     * @param maxHeight
     * @param type
     * @param service
     * @param descending
     * @param offset
     * @param limit
     * @return
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public IteratorCloseable findTransactionsKeys(String address, String creator, String recipient, Long fromSeqNo, final int minHeight,
                                                  final int maxHeight, int type, final int service, boolean descending, int offset, int limit) {
        if (parent != null || Controller.getInstance().onlyProtocolIndexing) {
            return null;
        }
        IteratorCloseable<Long> creatorIterator = null;
        IteratorCloseable<Long> recipientIterator = null;

        Long minID;
        Long maxID;
        minID = minHeight == 0 ? null : Transaction.makeDBRef(minHeight, 0);
        maxID = maxHeight == 0 ? Long.MAX_VALUE : Transaction.makeDBRef(maxHeight, Integer.MAX_VALUE);

        if (descending) {
            Long tempID = minID;
            minID = maxID;
            maxID = tempID;
        }

        if (fromSeqNo != null) {
            minID = fromSeqNo;
        }

        if (address == null && creator == null && recipient == null) {
            return IteratorCloseableImpl.make(new TreeSet<Long>().iterator());
        }

        IteratorCloseable<Long> iterator;

        if (address != null) {
            creator = recipient = address;
        }

        if (type == 0) {

            if (creator != null) {
                if (minID == null && maxID == null) {
                    creatorIterator = ((TransactionFinalSuit) map)
                            .getIteratorByCreator(Crypto.getInstance().getShortBytesFromAddress(creator), descending);
                } else {
                    creatorIterator = ((TransactionFinalSuit) map)
                            .getIteratorByCreator(Crypto.getInstance().getShortBytesFromAddress(creator), minID, maxID, descending);
                }
            }

            if (recipient != null) {
                if (minID == null && maxID == null) {
                    recipientIterator = ((TransactionFinalSuit) map)
                            .getIteratorByRecipient(Crypto.getInstance().getShortBytesFromAddress(recipient), descending);
                } else {
                    recipientIterator = ((TransactionFinalSuit) map)
                            .getIteratorByRecipient(Crypto.getInstance().getShortBytesFromAddress(recipient), minID, maxID, descending);
                }
            }

        } else {

            if (creator != null) {
                if (minID == null && maxID == null) {
                    creatorIterator = ((TransactionFinalSuit) map)
                            .getIteratorByAddressAndType(Crypto.getInstance().getShortBytesFromAddress(creator), type, Boolean.TRUE, descending);
                } else {
                    creatorIterator = ((TransactionFinalSuit) map)
                            .getIteratorByAddressAndType(Crypto.getInstance().getShortBytesFromAddress(creator), type, Boolean.TRUE, minID, maxID, descending);
                }
            }
            if (recipient != null) {
                if (minID == null && maxID == null) {
                    recipientIterator = ((TransactionFinalSuit) map)
                            .getIteratorByAddressAndType(Crypto.getInstance().getShortBytesFromAddress(recipient), type, Boolean.FALSE, descending);
                } else {
                    recipientIterator = ((TransactionFinalSuit) map)
                            .getIteratorByAddressAndType(Crypto.getInstance().getShortBytesFromAddress(recipient), type, Boolean.FALSE, minID, maxID, descending);
                }
            }

        }

        if (creatorIterator != null) {
            if (recipientIterator != null) {
                // просто добавляет в конец iterator = Iterators.concat(creatorKeys, recipientKeys);
                // вызывает ошибку преобразования типов iterator = Iterables.mergeSorted((Iterable) ImmutableList.of(creatorKeys, recipientKeys), Fun.COMPARATOR).iterator();
                // а этот Итератор.mergeSorted - он дублирует повторяющиеся значения индекса (( и делает пересортировку асинхронно - то есть тоже не ахти то что нужно
                // поэтому нужно удалить дубли
                iterator = new MergedOR_IteratorsNoDuplicates(ImmutableList.of(creatorIterator, recipientIterator),
                        descending ? Fun.REVERSE_COMPARATOR : Fun.COMPARATOR);
            } else {
                iterator = creatorIterator;
            }
        } else {
            iterator = recipientIterator;
        }

        if (false) {
            // теперь внутри индексов блок проверяется
            // как пример работы раньше
            if (minHeight != 0 || maxHeight != 0) {
                iterator = IteratorCloseableImpl.make(Iterators.filter(iterator, new Predicate<Long>() {
                    @Override
                    public boolean apply(Long key) {
                        Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
                        return (minHeight == 0 || pair.a >= minHeight) && (maxHeight == 0 || pair.a <= maxHeight);
                    }
                }));
            }
        }

        if (false && type == Transaction.ARBITRARY_TRANSACTION && service != 0) {
            // СЕРВИС это для AT - сейчас отключен
            iterator = IteratorCloseableImpl.make(Iterators.filter(iterator, new Predicate<Long>() {
                @Override
                public boolean apply(Long key) {
                    ArbitraryTransaction tx = (ArbitraryTransaction) get(key);
                    return tx.getService() == service;
                }
            }));
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
                .getBiDirectionAddressIterator(address == null ? null : Crypto.getInstance().getShortBytesFromAddress(address), fromSeqNo, descending);
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
        long timeOut = System.currentTimeMillis();

        int forgedCount = 0;
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
                    item = get(key);
                    if (noForge && item.getType() == Transaction.CALCULATED_TRANSACTION) {
                        RCalculated tx = (RCalculated) item;
                        String mess = tx.getMessage();
                        if (mess != null && mess.equals("forging")) {
                            if (forgedCount < 100) {
                                // skip all but not 100
                                forgedCount++;
                                continue;
                            } else {
                                if (System.currentTimeMillis() - timeOut > 5000) {
                                    break;
                                }
                                forgedCount = 0;
                            }
                        }
                    }

                    if (offsetHere > 0 && skipped++ < offsetHere) {
                        continue;
                    }

                    item.setDC((DCSet) databaseSet, true);

                    count++;

                    // обратный отсчет в списке
                    txs.add(0, item);
                }

                if (fillFullPage && fromSeqNo != null && fromSeqNo != 0 && limit > 0 && count < limit) {
                    // сюда пришло значит не полный список - дополним его
                    for (Transaction transaction : getTransactionsByAddressFromID(addressShort,
                            fromSeqNo, 0, limit - count, noForge, false)) {
                        boolean exist = false;
                        for (Transaction txHere : txs) {
                            if (transaction.equals(txHere)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            txs.add(transaction);
                        }
                    }
                }

            } catch (IOException e) {
            }

        } else {

            try (IteratorCloseable<Long> iterator = ((TransactionFinalSuit) map).getBiDirectionAddressIterator(addressShort, fromSeqNo, true)) {
                Transaction item;
                Long key;
                int skipped = 0;
                int count = 0;
                while (iterator.hasNext() && (limit <= 0 || count < limit)) {
                    key = iterator.next();
                    item = get(key);
                    if (item == null) {
                        String keyStr = Transaction.viewDBRef(key);
                        boolean debug = true;
                    }
                    if (noForge && item.getType() == Transaction.CALCULATED_TRANSACTION) {
                        RCalculated tx = (RCalculated) item;
                        String mess = tx.getMessage();
                        if (mess != null && mess.equals("forging")) {
                            if (forgedCount < 100) {
                                // skip all but not 100
                                forgedCount++;
                                continue;
                            } else {
                                if (System.currentTimeMillis() - timeOut > 5000) {
                                    break;
                                }
                                forgedCount = 0;
                            }
                        }
                    }

                    if (offset > 0 && skipped++ < offset) {
                        continue;
                    }

                    item.setDC((DCSet) databaseSet, true);

                    count++;

                    boolean exist = false;
                    for (Transaction txHere : txs) {
                        if (item.equals(txHere)) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        txs.add(item);
                    }
                }

                if (fillFullPage && fromSeqNo != null && fromSeqNo != 0 && limit > 0 && count < limit) {
                    // сюда пришло значит не полный список - дополним его
                    int index = 0;
                    int limitLeft = limit - count;
                    for (Transaction transaction : getTransactionsByAddressFromID(addressShort,
                            fromSeqNo, -(limitLeft + (count > 0 ? 1 : 0)), limitLeft, noForge, false)) {
                        boolean exist = false;
                        for (Transaction txHere : txs) {
                            if (transaction.equals(txHere)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            txs.add(index++, transaction);
                        }
                    }
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
        Long key = ((DCSet) databaseSet).getTransactionFinalMapSigns().get(signature);
        if (key == null)
            return null;

        return this.get(key);
    }

    @Override
    public Transaction get(Long key) {
        // [167726]
        Transaction transaction = super.get(key);
        if (transaction == null)
            return null;

        if (transaction instanceof GenesisRecord) {
            Tuple2<Integer, Integer> seqNo = Transaction.parseDBRef(key);
            transaction.setDC((DCSet) databaseSet, Transaction.FOR_PACK, seqNo.a, seqNo.b);
        } else {
            transaction.setDC((DCSet) databaseSet);
        }

        // наращивание всех данных для скелета - так же необходимо для создания ключей tags
        if (parent == null && !transaction.isWiped()) {
            transaction.updateFromStateDB();
        }

        return transaction;
    }

    @Override
    public void put(Long key, Transaction transaction) {
        super.put(key, transaction);
    }

    @Override
    public void put(Transaction transaction) {
        put(transaction.getDBRef(), transaction);
    }

}
