package org.erachain.datachain;

import com.google.common.collect.Iterators;
import org.apache.commons.lang3.ArrayUtils;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.FilteredByStringArray;
import org.erachain.database.Pageable;
import org.erachain.database.PagedMap;
import org.erachain.database.serializer.ItemSerializer;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.utils.Pair;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Хранение сущностей
 * <p>
 * ключ: номер, с самоувеличением
 * Значение: Сущность
 */
public abstract class ItemMap extends DCUMap<Long, ItemCls> implements FilteredByStringArray<ItemCls>,
        Pageable<Long, ItemCls> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    private static int CUT_NAME_INDEX = 12;

    protected Atomic.Long atomicKey;
    protected long key;

    protected BTreeMap makerKeyMap;

    private static final int NAME_INDEX = 1;

    private NavigableSet nameKey;
    //private NavigableSet<Fun.Tuple2<String, Long>> nameDescendingIndex;


    public ItemMap(DCSet databaseSet, DB database, int type) {
        super(databaseSet, database, ItemCls.getItemTypeName(type), new ItemSerializer(type));

        HI = Long.MAX_VALUE;
        LO = 0L;

        atomicKey = database.getAtomicLong(TAB_NAME + "_key");
        key = atomicKey.get();

        makeOtherKeys(database);

    }

    public ItemMap(DCSet databaseSet, DB database,
                   int type, int observeReset, int observeAdd, int observeRemove, int observeList) {
        this(databaseSet, database, type);
        if (databaseSet.isWithObserver()) {
            if (observeReset > 0)
                this.observableData.put(DBTab.NOTIFY_RESET, observeReset);
            if (observeList > 0)
                this.observableData.put(DBTab.NOTIFY_LIST, observeList);
            if (observeAdd > 0) {
                observableData.put(DBTab.NOTIFY_ADD, observeAdd);
            }
            if (observeRemove > 0) {
                observableData.put(DBTab.NOTIFY_REMOVE, observeRemove);
            }
        }
    }

    public ItemMap(ItemMap parent, DCSet dcSet) {
        super(parent, dcSet);
        key = parent.getLastKey();
    }

    // type+name not initialized yet! - it call as Super in New
    @SuppressWarnings("unchecked")
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap(TAB_NAME)
                .valueSerializer(TAB_SERIALIZER)
                .makeOrGet();

    }

    public long getLastKey() {
        return key;
    }

    @Override
    public int size() {
        return (int) key;
    }

    public void setLastKey(long key) {
        // INCREMENT ATOMIC KEY IF EXISTS
        if (atomicKey != null) {
            atomicKey.set(key);
        }
        this.key = key;
    }

    protected void makeOtherKeys(DB database) {

        //////////////// NOT PROTOCOL INDEXES
        if (Controller.getInstance().onlyProtocolIndexing) {
            // NOT USE SECONDARY INDEXES
            return;
        }

        //CHECK IF NOT MEMORY DATABASE
        if (parent != null) {
            return;
        }

        //PAIR KEY
        this.makerKeyMap = database.createTreeMap(TAB_NAME + "_owner_item_key")
                //.comparator(Fun.TUPLE3_COMPARATOR)
                .makeOrGet();

        //BIND OWNER KEY
        Bind.secondaryKey((BTreeMap) map, this.makerKeyMap, new Fun.Function2<String, Long, ItemCls>() {
            @Override
            public String run(Long key, ItemCls value) {
                return value.getMaker().getAddress();
            }
        });

        this.nameKey = database.createTreeSet(TAB_NAME + "_name_keys").comparator(Fun.COMPARATOR).makeOrGet();

        // в БИНЕ внутри уникальные ключи создаются добавлением основного ключа
        Bind.secondaryKeys((BTreeMap) map, this.nameKey,
                new Fun.Function2<String[], Long, ItemCls>() {
                    @Override
                    public String[] run(Long key, ItemCls item) {
                        String[] keys = item.getName().toLowerCase().split(Transaction.SPLIT_CHARS);
                        for (int i = 0; i < keys.length; ++i) {
                            if (keys[i].length() > CUT_NAME_INDEX) {
                                keys[i] = keys[i].substring(0, CUT_NAME_INDEX);
                            }
                        }
                        String[] addTags = item.getTags();
                        if (addTags == null || addTags.length == 0)
                            return keys;
                        return ArrayUtils.addAll(keys, addTags);
                    }
                });
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<Long, ItemCls>();
    }

    public ItemCls get(Long key) {
        ItemCls item = super.get(key);
        if (item == null) {
            return null;
        }

        item.setKey(key);
        item.loadExtData(this);
        return item;
    }

    public long incrementPut(ItemCls item) {
        // INCREMENT ATOMIC KEY IF EXISTS
        if (atomicKey != null) {
            atomicKey.incrementAndGet();
        }

        // INCREMENT KEY
        key++;
        item.setKey(key);

        // INSERT WITH NEW KEY
        put(key, item);

        // RETURN KEY
        return key;
    }

    public ItemCls decrementRemove(long key) {

        if (key != this.key
                && !BlockChain.isNovaAsset(key)
        ) {

            LOGGER.error("delete KEY: " + key + " != map.value.key: " + this.key);

            if (key > this.key) {
                Long error = null;
                error++;
            }
        }

        ItemCls old = super.remove(key);

        if (this.key != key) {
            // it is not top of STACK (for UNIQUE items with short NUM)
            return old;
        }
        // delete on top STACK

        if (atomicKey != null) {
            atomicKey.decrementAndGet();
        }

        // DECREMENT KEY
        --this.key;

        return old;
    }

    public void decrementDelete(long key) {

        if (key != this.key
                && !BlockChain.isNovaAsset(key)
        ) {

            LOGGER.error("delete KEY: " + key + " != map.value.key: " + this.key);

            if (key > this.key) {
                Long error = null;
                error++;
            }
        }

        super.delete(key);

        if (this.key != key) {
            // it is not top of STACK (for UNIQUE items with short NUM)
            return;
        }
        // delete on top STACK

        if (atomicKey != null) {
            atomicKey.decrementAndGet();
        }

        // DECREMENT KEY
        --this.key;

    }


    /**
     * @param step
     * @param filterArray
     * @param descending  - не рекомендуется использовать, так как все равно результат смешанные ключ первичные, а лишняя суета
     * @return
     */
    public Pair<Integer, List<IteratorCloseableImpl<Long>>> getKeysByFilterAsArrayRecurse(int step, String[] filterArray, boolean descending) {

        Iterable keys;

        String stepFilter = filterArray[step];
        if (!stepFilter.endsWith("!")) {
            // это сокращение для диаппазона
            if (stepFilter.length() < 5) {
                // ошибка - ищем как полное слово
                keys = Fun.filter(this.nameKey, stepFilter);
            } else {

                if (stepFilter.length() > CUT_NAME_INDEX) {
                    stepFilter = stepFilter.substring(0, CUT_NAME_INDEX);
                }

                // поиск диапазона
                if (descending) {
                    keys = Fun.filter(this.nameKey.descendingSet(),
                            stepFilter + new String(new byte[]{(byte) 255}), true,
                            stepFilter,
                            false // ВАЖНО! иначе не ищет
                    );
                } else {
                    keys = Fun.filter(this.nameKey,
                            stepFilter, true,
                            stepFilter + new String(new byte[]{(byte) 255}), true);
                }
            }

        } else {
            // поиск целиком

            stepFilter = stepFilter.substring(0, stepFilter.length() - 1);

            if (stepFilter.length() > CUT_NAME_INDEX) {
                stepFilter = stepFilter.substring(0, CUT_NAME_INDEX);
            }

            if (descending) {
                keys = Fun.filter(this.nameKey.descendingSet(), stepFilter);
            } else {
                keys = Fun.filter(this.nameKey, stepFilter);
            }
        }

        // в рекурсии все хорошо - соберем ключи
        IteratorCloseableImpl iterator = IteratorCloseableImpl.make(keys.iterator());
        if (iterator.hasNext()) {

            if (step > 0) {

                // погнали в РЕКУРСИЮ
                Pair<Integer, List<IteratorCloseableImpl<Long>>> result = getKeysByFilterAsArrayRecurse(--step, filterArray, descending);
                if (result.getA() > 0) {
                    // в рекурсии где-то одно слово вообще не найдено - просто выход
                    return result;
                }

                result.getB().add(iterator);
                return new Pair<>(0, result.getB());

            } else {

                // последний шаг - просто возьмем этот
                List<IteratorCloseable<Long>> result = new ArrayList<>();
                result.add(iterator);
                return new Pair(0, result);

            }
        } else {
            // нет вообще значений!
            return new Pair<>(step + 1, null);
        }

    }

    /**
     * Делает поиск по нескольким ключам по Заголовкам и если ключ с ! - надо найти только это слово
     * а не как фильтр. Иначе слово принимаем как фильтр на диаппазон
     * и его длинна должна быть не мнее 5-ти символов. Например:
     * "Ермолаев Дмитр." - Найдет всех Ермолаев с Дмитр....
     *
     * @param filter
     * @param fromID
     * @param offset
     * @param descending
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Pair<String, IteratorCloseable<Long>> getKeysIteratorByFilterAsArray(String filter, Long fromID, int offset, boolean descending) {

        String filterLower = filter.toLowerCase();
        String[] filterArray = filterLower.split(Transaction.SPLIT_CHARS);

        Pair<Integer, List<IteratorCloseableImpl<Long>>> result = null;
        try {
            result = getKeysByFilterAsArrayRecurse(filterArray.length - 1, filterArray,
                    // лучше стандартно так как все равно на выходе сортировка съедет
                    // и не надо напрягать обратный поиск
                    false
            );
            if (result.getA() > 0) {
                return new Pair<>("Error: filter key at " + result.getA() + " pos has length < 5",
                        null);
            }

            Long key;
            HashSet<Long> andHashSet = null;
            for (IteratorCloseable<Long> iterator : result.getB()) {

                if (andHashSet == null) {
                    // first time - add ALL
                    andHashSet = new HashSet<>();
                    while (iterator.hasNext()) {
                        andHashSet.add(iterator.next());
                    }
                    continue;
                }

                // текущий список
                HashSet<Long> tempHashSet = new HashSet<>();
                while (iterator.hasNext()) {
                    tempHashSet.add(iterator.next());
                }

                // теперь проверим все значения в списке по И
                Iterator<Long> iteratorAND = andHashSet.iterator();
                List<Long> toRemove = new ArrayList<>();
                while (iteratorAND.hasNext()) {
                    key = iteratorAND.next();
                    if (!tempHashSet.contains(key)) {
                        toRemove.add(key);
                    }
                }
                for (Long removedKey : toRemove) {
                    andHashSet.remove(removedKey);
                }
            }

            NavigableSet<Long> treeSet = new TreeSet<>();
            for (Long keyResult : andHashSet) {
                treeSet.add(keyResult);
            }

            if (descending)
                treeSet = treeSet.descendingSet();

            if (fromID != null) {
                treeSet = (NavigableSet<Long>) treeSet.subSet(fromID, descending ? 0L : Long.MAX_VALUE);
            }

            IteratorCloseable<Long> iteratorOut = IteratorCloseableImpl.make(treeSet.iterator());

            if (offset > 0)
                Iterators.advance(iteratorOut, offset);

            return new Pair<>(null, iteratorOut);

        } finally {
            try {
                // нужно закрыть то что уже нашлось
                if (result != null && result.getB() != null) {
                    for (IteratorCloseable iterator : result.getB()) {
                        iterator.close();
                    }
                }
            } catch (IOException e) {
            }

        }

    }

    // get list items in name substring str
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<ItemCls> getByFilterAsArray(String filter, Long fromID, int offset, int limit, boolean descending) {

        if (filter == null || filter.isEmpty()) {
            return new ArrayList<>();
        }


        IteratorCloseable<Long> iterator = null;
        Pair<String, IteratorCloseable<Long>> resultKeys;
        List<ItemCls> result = new ArrayList<>();
        try {

            resultKeys = getKeysIteratorByFilterAsArray(filter, fromID, offset, descending);
            if (resultKeys.getA() != null) {
                return new ArrayList<>();
            }

            iterator = resultKeys.getB();

            int count = 0;
            if (limit <= 0 || limit > 10000)
                limit = 10000; // ограничим что бы ноду не перегрузить случайно

            while (iterator.hasNext()) {
                ItemCls item = get(iterator.next());
                result.add(item);

                if (++count >= limit)
                    break;

            }
        } finally {
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException e) {
                }
            }
        }

        return result;
    }

    public List<ItemCls> getPage(Long start, int offset, int pageSize) {
        PagedMap<Long, ItemCls> pager = new PagedMap(this);
        return pager.getPageList(start, offset, pageSize, true);
    }

    public Collection<Long> getFromToKeys(long fromKey, long toKey) {
        return ((BTreeMap) map).subMap(fromKey, toKey).values();
    }

    public NavigableMap<Long, ItemCls> getOwnerItems(String makerPublicKey) {
        return this.makerKeyMap.subMap(makerPublicKey, makerPublicKey);
    }

    @Override
    public boolean writeToParent() {
        boolean updated = super.writeToParent();
        ((ItemMap) parent).atomicKey.set(this.key);
        ((ItemMap) parent).key = this.key;
        return updated;
    }

    /**
     * Если откатить базу данных то нужно и локальные значения сбросить
     */
    @Override
    public void afterRollback() {
        this.key = atomicKey.get();
    }
}
