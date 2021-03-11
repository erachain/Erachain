package org.erachain.datachain;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.ArrayUtils;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.FilteredByStringArray;
import org.erachain.database.serializer.ItemSerializer;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.utils.Pair;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Хранение сущностей
 * <p>
 * ключ: номер, с самоувеличением
 * Значение: Сущность
 */
public abstract class ItemMap extends DCUMap<Long, ItemCls> implements FilteredByStringArray<Long> {

    protected Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    private static int CUT_NAME_INDEX = 12;

    protected Atomic.Long atomicKey;
    protected long key;

    protected BTreeMap ownerKeyMap;

    private static final int NAME_INDEX = 1;

    private NavigableSet nameKey;
    //private NavigableSet<Fun.Tuple2<String, Long>> nameDescendingIndex;


    public ItemMap(DCSet databaseSet, DB database, int type) {
        super(databaseSet, database, ItemCls.getItemTypeName(type), new ItemSerializer(type));

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
        this.ownerKeyMap = database.createTreeMap(TAB_NAME + "_owner_item_key")
                //.comparator(Fun.TUPLE3_COMPARATOR)
                .makeOrGet();

        //BIND OWNER KEY
        Bind.secondaryKey((BTreeMap) map, this.ownerKeyMap, new Fun.Function2<String, Long, ItemCls>() {
            @Override
            public String run(Long key, ItemCls value) {
                return value.getOwner().getAddress();
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


    public Pair<Integer, HashSet<Long>> getKeysByFilterAsArrayRecurse(int step, String[] filterArray) {

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

                // поиск диаппазона
                keys = Fun.filter(this.nameKey,
                        stepFilter, true,
                        stepFilter + new String(new byte[]{(byte) 254}), true);
            }

        } else {
            // поиск целиком

            stepFilter = stepFilter.substring(0, stepFilter.length() - 1);

            if (stepFilter.length() > CUT_NAME_INDEX) {
                stepFilter = stepFilter.substring(0, CUT_NAME_INDEX);
            }

            keys = Fun.filter(this.nameKey, stepFilter);
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
     *
     * @param filter
     * @param offset
     * @param limit
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Pair<String, Iterable> getKeysIteratorByFilterAsArray(String filter, int offset, int limit) {

        String filterLower = filter.toLowerCase();
        String[] filterArray = filterLower.split(Transaction.SPLIT_CHARS);

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
    public List<Long> getKeysByFilterAsArray(String filter, String fromWord, Long fromSeqNo, int offset, int limit, boolean descending) {

        if (filter == null || filter.isEmpty()) {
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
            ItemCls item = get(key);
            if (item != null)
                result.add(key);
        }

        return result;
    }

    // get list items in name substring str
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<ItemCls> getByFilterAsArray(String filter, int offset, int limit) {

        if (filter == null || filter.isEmpty()) {
            return new ArrayList<>();
        }

        Pair<String, Iterable> resultKeys = getKeysIteratorByFilterAsArray(filter, offset, limit);
        if (resultKeys.getA() != null) {
            return new ArrayList<>();
        }

        List<ItemCls> result = new ArrayList<>();

        Iterator<Long> iterator = resultKeys.getB().iterator();

        while (iterator.hasNext()) {
            ItemCls item = get(iterator.next());
            result.add(item);
        }

        return result;
    }

    public IteratorCloseable<Long> getIteratorFrom(long fromKey, boolean descending) {

        Iterator<Long> iterator;
        if (descending) {
            iterator = ((NavigableMap) map).descendingKeySet()
                    .subSet(fromKey, 0L)
                    .iterator();
        } else {
            iterator = ((NavigableSet) map.keySet())
                    .subSet(fromKey, Long.MAX_VALUE)
                    .iterator();
        }
        return IteratorCloseableImpl.make(iterator);

    }

    public Collection<Long> getFromToKeys(long fromKey, long toKey) {
        return ((BTreeMap) map).subMap(fromKey, toKey).values();
    }

    public NavigableMap<Long, ItemCls> getOwnerItems(String ownerPublicKey) {
        return this.ownerKeyMap.subMap(ownerPublicKey, ownerPublicKey);
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
