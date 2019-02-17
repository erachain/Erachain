package org.erachain.network;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class HandledMap<K, V> extends ConcurrentHashMap {

    private int max_size;
    private List<K> handledList;

    public HandledMap(int max_size) {
        this.max_size = max_size;
    }
    /**
     * Тут значение по ссылке - как только создали Список - его не меняем как объект, а меняем внутри его список
     * @param key
     * @param item
     * @return true if LIST was empty
     */
    public boolean addHandledItem(Object key, Object item) {

        CopyOnWriteArrayList itemsList;

        if (!super.containsKey(key)) {

            // Если еще нет данных
            itemsList = new CopyOnWriteArrayList();
            if (item != null)
                itemsList.add(item);

            // добавит если пусто или выдаст список который уже есть
            itemsList = (CopyOnWriteArrayList)super.putIfAbsent(key, itemsList);

            if (itemsList == null) {
                handledList.add((K)key);
                if (handledList.size() > this.max_size) {
                    // REMOVE first KEY
                    key = this.handledList.remove(0);
                    // REMOVE this KEY in HANDLED HASHMAP
                    super.remove(key);

                }
                return true;
            }

        } else {
            itemsList = (CopyOnWriteArrayList)super.get(key);
        }

        if (item != null)
            itemsList.add(item);

        return false;

    }

    public boolean removeFirst() {

        if (handledList.isEmpty())
            return false;

        // REMOVE first KEY
        Object key = this.handledList.remove(0);
        // REMOVE this KEY in HANDLED HASHMAP
        super.remove(key);

        return true;
    }

    public void clear() {

        handledList.clear();
        super.clear();
    }
}
